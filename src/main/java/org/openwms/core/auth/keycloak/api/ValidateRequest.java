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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * A ValidateRequest.
 *
 * @author Heiko Scherrer
 */
public record ValidateRequest(String username, char[] password) implements Serializable {

    /*~---------------- Constructors ------------- */
    public ValidateRequest(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    /*~---------------- Methods ------------- */

    /**
     * {@inheritDoc}
     */
    @Override
    public String username() {
        return username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char[] password() {
        return password;
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ValidateRequest) o;
        return Objects.equals(username, that.username) && Objects.deepEquals(password, that.password);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, Arrays.hashCode(password));
    }
}
