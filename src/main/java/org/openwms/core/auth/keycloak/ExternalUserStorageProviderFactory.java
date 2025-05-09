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

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A ExternalUserStorageProviderFactory.
 *
 * @author Heiko Scherrer
 */
public class ExternalUserStorageProviderFactory implements UserStorageProviderFactory<ExternalUserProvider> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalUserStorageProviderFactory.class);
    public static final String PROVIDER_ID = "external-http";

    private final List<ProviderConfigProperty> configMetadata;

    public ExternalUserStorageProviderFactory() {
        configMetadata = ProviderConfigurationBuilder.create()
                .property()
                .name("restSchema")
                .label("Rest schema")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("http")
                .helpText("Rest schema to call external services")

                .add()
                .property()
                .name("serviceHostname")
                .label("Service hostname")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("localhost")
                .helpText("Hostname of the external service")

                .add()
                .property()
                .name("servicePort")
                .label("Service port")
                .type(ProviderConfigProperty.STRING_TYPE)
                .defaultValue("8080")
                .helpText("Port of the external service")

                .add()
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExternalUserProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        LOGGER.info("Creating custom user federation provider.");
        return new ExternalUserProvider(keycloakSession, componentModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpText() {
        return "External user provider for federation";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }
}
