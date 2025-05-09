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

import java.io.Serializable;
import java.util.Objects;

/**
 * A CombinedUserKey.
 *
 * @author Heiko Scherrer
 */
public final class CombinedUserKey implements Serializable {

    private final String userProperty;
    private final String realmId;

    public CombinedUserKey(String userProperty, String realmId) {
        this.userProperty = userProperty;
        this.realmId = realmId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        var that = (CombinedUserKey) o;
        return Objects.equals(userProperty, that.userProperty) && Objects.equals(realmId, that.realmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userProperty, realmId);
    }
}
