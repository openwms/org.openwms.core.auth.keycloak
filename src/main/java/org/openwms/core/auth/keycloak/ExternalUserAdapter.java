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

import org.openwms.core.auth.keycloak.api.ExternalUser;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.adapter.AbstractUserAdapter;

import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A ExternalUserAdapter.
 *
 * @author Heiko Scherrer
 */
class ExternalUserAdapter extends AbstractUserAdapter implements Serializable {

    private String username;
    private String firstName;
    private String lastName;
    private String email;

    /*~---------------- Constructors ------------- */
    /**
     * {@inheritDoc}
     */
    public ExternalUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel) {
        super(session, realm, storageProviderModel);
    }

    /**
     * Creates a new instance of AbstractUserAdapter based on the provided Keycloak session, realm, storage provider model, and external
     * user. The created adapter is initialized with the specified user's details.
     *
     * @param session The Keycloak session to associate with this adapter
     * @param realm The realm model representing the Keycloak realm
     * @param componentModel The storage provider model used for storage operations
     * @param user The external user whose details will populate the adapter
     * @return An instance of AbstractUserAdapter initialized with the given external user's details
     */
    public static AbstractUserAdapter of(KeycloakSession session, RealmModel realm, ComponentModel componentModel, ExternalUser user) {
        var result = new ExternalUserAdapter(session, realm, componentModel);
        result.setUsername(user.getUsername());
        result.setFirstName(user.getFirstName());
        result.setLastName(user.getLastName());
        result.setEmail(user.getEmail());
        return result;
    }

    /*~---------------- Methods ------------- */
    /**
     * {@inheritDoc}
     */
    @Override
    public SubjectCredentialManager credentialManager() {
        return new UserCredentialManager(session, realm, this);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public String toString() {
        return new StringJoiner(", ", ExternalUserAdapter.class.getSimpleName() + "[", "]")
                .add("username='" + username + "'")
                .add("firstName='" + firstName + "'")
                .add("lastName='" + lastName + "'")
                .add("email='" + email + "'")
                .toString();
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ExternalUserAdapter that = (ExternalUserAdapter) o;
        return Objects.equals(username, that.username) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(email, that.email);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username, firstName, lastName, email);
    }

    /*~---------------- Accessors ------------- */
    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFirstName() {
        return firstName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLastName() {
        return lastName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEmail() {
        return email;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEmail(String email) {
        this.email = email == null ? null : email.toLowerCase().trim();
    }
}
