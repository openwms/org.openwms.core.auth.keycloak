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

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.util.Objects;

/**
 * A FindUserRequest.
 *
 * @author Heiko Scherrer
 */
public class FindUserRequest implements Serializable {

    private String username;
    private String email;

    /*~---------------- Constructors ------------- */
    @ConstructorProperties({"username", "email"})
    public FindUserRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    /*~---------------- Methods ------------- */

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FindUserRequest that = (FindUserRequest) o;
        return Objects.equals(username, that.username) && Objects.equals(email, that.email);
    }

    /**
     * {@inheritDoc}
     *
     * All fields.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, email);
    }

    /*~---------------- Accessors ------------- */
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
