/*
 * Copyright 2005-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openwms.core.auth.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;
import org.openwms.core.auth.keycloak.api.FindUserRequest;
import org.openwms.core.auth.keycloak.api.FindUserResponse;
import org.openwms.core.auth.keycloak.api.ValidateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A ExternalUserProvider is the implementation of the required Keycloak Provider interfaces in order to authenticate a user. It builds up
 * an internal cache for users, that should be purged continuously (Keycloak Cache policy). Two {@link ConcurrentHashMap}s are used and
 * synchronized for the cache. The user could log in with an email address or by username, hence not all user information is always
 * available.
 *
 * @author Heiko Scherrer
 * @see org.keycloak.storage.UserStorageProvider
 * @see org.keycloak.storage.user.UserLookupProvider
 * @see org.keycloak.credential.CredentialInputValidator
 */
public class ExternalUserProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalUserProvider.class);
    public static final String APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";

    private final KeycloakSession keycloakSession;
    private final ComponentModel model;
    private final HttpClient httpClient;
    private final Map<CombinedUserKey, UserModel> usersMap = new ConcurrentHashMap<>();
    private final Map<String, UserModel> usersMapByEmail = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    public ExternalUserProvider(KeycloakSession keycloakSession, ComponentModel componentModel) {
        this.keycloakSession = keycloakSession;
        this.model = componentModel;
        this.httpClient = keycloakSession.getProvider(HttpClientProvider.class).getHttpClient();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        if (!supportsCredentialType(credentialInput.getType())) {
            LOGGER.error("Credential type is not supported: [{}]", credentialInput.getType());
            return false;
        }
        return validate(realmModel, userModel.getUsername(), credentialInput.getChallengeResponse());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        LOGGER.debug("Find user by id [{}] in realm [{}]", id, realm.getId());
        return getUserByUsernameInternal(realm, new StorageId(id).getExternalId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        LOGGER.debug("Find user by username [{}] in realm [{}]", username, realm.getId());
        return getUserByUsernameInternal(realm, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        LOGGER.debug("Find user by email [{}] in realm [{}]", email, realm.getId());
        final var user = usersMapByEmail.get(email);
        if (user != null) {
            return user;
        }
        final var userOpt = resolveByEmail(realm, email);
        if (userOpt.isPresent()) {
            LOGGER.info("Found user by email in external system [{}]", userOpt.get());
            usersMapByEmail.put(email, userOpt.get());
            usersMap.computeIfAbsent(new CombinedUserKey(userOpt.get().getUsername(), realm.getId()), k -> userOpt.get());
            return userOpt.get();
        }
        return null;
    }

    private UserModel getUserByUsernameInternal(RealmModel realm, String username) {
        final var key = new CombinedUserKey(username, realm.getId());
        final var user = usersMap.get(key);
        if (user != null) {
            return user;
        }
        final var userOpt = resolveByUsername(realm, username);
        if (userOpt.isPresent()) {
            LOGGER.info("Found user by username in external system [{}]", userOpt.get());
            usersMap.put(key, userOpt.get());
            if (userOpt.get().getEmail() != null) {
                usersMapByEmail.put(userOpt.get().getEmail(), userOpt.get());
            }
            return userOpt.get();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * Clear the cache.
     */
    @Override
    public void close() {
        usersMap.clear();
    }

    /**
     * {@inheritDoc}
     *
     * Only {@link PasswordCredentialModel} is supported.
     */
    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    /**
     * {@inheritDoc}
     *
     * @see #supportsCredentialType(String)
     */
    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    private Optional<AbstractUserAdapter> resolveByUsername(RealmModel realmModel, String username) {
        final var lowercasedUsername = username.toLowerCase().trim();
        LOGGER.debug("Resolve user by username [{}] and realm [{}] in external system", lowercasedUsername, realmModel.getId());
        final var om = new ObjectMapper();
        try {
            final var httpPost = new HttpPost(URI.create("%s://%s:%s/%s".formatted(
                    model.getConfig().getFirst("restSchema"),
                    model.getConfig().getFirst("serviceHostname"),
                    model.getConfig().getFirst("servicePort"),
                    "/auth/" + realmModel.getId() + "/users"
            )));
            httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            httpPost.setHeader(ACCEPT, APPLICATION_JSON);
            httpPost.setEntity(new StringEntity(om.writeValueAsString(new FindUserRequest(lowercasedUsername, null))));
            httpPost.setConfig(getDefaultRequestConfig());
            var httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                var user = om.readValue(httpResponse.getEntity().getContent(), FindUserResponse.class).getExternalUser();
                LOGGER.info("Resolved user [{}] and realm [{}] in external system", user, realmModel.getId());
                return Optional.of(ExternalUserAdapter.of(keycloakSession, realmModel, model, user));
            }
            LOGGER.error("Failed to get user from external service, by username [{}], and realm [{}], status code [{}]", lowercasedUsername,
                    realmModel.getId(), httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Optional<AbstractUserAdapter> resolveByEmail(RealmModel realmModel, String email) {
        final var lowercasedEmail = email.toLowerCase().trim();
        LOGGER.debug("Resolve user by email [{}] and realm [{}] in external system", email, realmModel.getId());
        final var om = new ObjectMapper();
        try {
            final var httpPost = new HttpPost(URI.create("%s://%s:%s/%s".formatted(
                    model.getConfig().getFirst("restSchema"),
                    model.getConfig().getFirst("serviceHostname"),
                    model.getConfig().getFirst("servicePort"),
                    "/auth/" + realmModel.getId() + "/users"
            )));
            httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            httpPost.setHeader(ACCEPT, APPLICATION_JSON);
            httpPost.setEntity(new StringEntity(om.writeValueAsString(new FindUserRequest(null, lowercasedEmail))));
            httpPost.setConfig(getDefaultRequestConfig());
            var httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                var user = om.readValue(httpResponse.getEntity().getContent(), FindUserResponse.class).getExternalUser();
                LOGGER.info("Resolved user [{}] and realm [{}] in external system", user, realmModel.getId());
                return Optional.of(ExternalUserAdapter.of(keycloakSession, realmModel, model, user));
            }
            LOGGER.error("Failed to get user from external service, by email [{}], and realm [{}], status code [{}]", email,
                    realmModel.getId(), httpResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    private boolean validate(RealmModel realmModel, String username, String rawPassword) {
        final var lowercasedUsername = username.toLowerCase().trim();
        LOGGER.info("Validate credentials in external system for user with username [{}] and tenantId [{}]", lowercasedUsername, realmModel.getId());
        final var om = new ObjectMapper();
        try {
            final var httpPost = new HttpPost(URI.create("%s://%s:%s/%s".formatted(
                    model.getConfig().getFirst("restSchema"),
                    model.getConfig().getFirst("serviceHostname"),
                    model.getConfig().getFirst("servicePort"),
                    "/auth/" + realmModel.getId() + "/users/validate"
            )));
            httpPost.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            httpPost.setEntity(new StringEntity(om.writeValueAsString(new ValidateRequest(lowercasedUsername, rawPassword.toCharArray()))));
            httpPost.setConfig(getDefaultRequestConfig());
            var httpResponse = httpClient.execute(httpPost);
            return httpResponse.getStatusLine().getStatusCode() > 199 && httpResponse.getStatusLine().getStatusCode() < 300;
        } catch (Exception e) {
            LOGGER.error("Failed to validate credentials with message [{}]", e.getMessage(), e);
            return false;
        }
    }

    private RequestConfig getDefaultRequestConfig() {
        return RequestConfig.custom().setConnectTimeout(3000).setConnectionRequestTimeout(3000).setSocketTimeout(3000).build();
    }
}