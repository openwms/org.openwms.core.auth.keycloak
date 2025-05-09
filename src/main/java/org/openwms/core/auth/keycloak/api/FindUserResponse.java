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
package org.openwms.core.auth.keycloak.api;

import java.util.Objects;

/**
 * A FindUserResponse.
 *
 * @author Heiko Scherrer
 */
public class FindUserResponse {

    private ExternalUser externalUser;

    /*~---------------- Methods ------------- */
    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        var that = (FindUserResponse) o;
        return Objects.equals(externalUser, that.externalUser);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(externalUser);
    }

    /*~---------------- Accessors ------------- */
    public ExternalUser getExternalUser() {
        return externalUser;
    }

    public void setExternalUser(ExternalUser externalUser) {
        this.externalUser = externalUser;
    }
}
