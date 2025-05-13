# OpenWMS.org HTTP User Authentication Extension for Keycloak
This extension is a `UserStorageProvider` implementation that can be integrated into [Keycloak](https://www.keycloak.org). Keycloak will
then recognize it as part of the user authentication procedure to authenticate against a remote http service.

![Arch][3]

## Usage in Keycloak
After building the project, the jar file must be copied to Keycloak's `providers` directory. In case of the Keycloak Docker image this is at
`/opt/keycloak/providers`. A simple Dockerfile that adopts the original Keycloak Dockerfile looks like:

```dockerfile
FROM quay.io/keycloak/keycloak:26.1.4 AS builder

WORKDIR /opt/keycloak
COPY /target/external-http-auth.jar /opt/keycloak/providers

FROM quay.io/keycloak/keycloak:26.1.4
COPY --from=builder --chown=keycloak:root /opt/keycloak/ /opt/keycloak/
CMD ["start-dev"]
```

After the customized Keycloak image has been built and started, the `External-http` provider shows up in the admin console under the menu
`User federation` and can be selected: 

![Add Providers][1]

Provide the basic details for the external http provider in the details form:

![Provider Details][2]

| Component | Description                                                                      |
| --------- |----------------------------------------------------------------------------------|
| UI display name | An arbitrary name shown in the Keycloak admin console                            |
| Rest schema | HTTP schema of the REST API to use, either HTTP or HTTPS supported               |
| Service hostname | Full-qualilfied domain name or ip address of the external authentication service |
| Service port | The port number where the external authentication service is accessible at |
| Cache policy | How frequently Keycloak shall call the provider implementation to clear the user cache |

## External HTTP Authentication API
Keycloak calls the configured service to authenticate unknown users. The **API implementation** must offer the below listed endpoints. All
defined data exchange models are provided as a `client-jar` and can be easily imported into a service implementation with the following
Maven dependency:

```xml
<dependency>
    <groupId>org.openwms</groupId>
    <artifactId>org.openwms.core.auth.keycloak</artifactId>
    <version>${core.auth.keycloak.version}</version>
    <classifier>client</classifier>
</dependency>
```

With this dependency inclusion the service implementation has at least the defined data types for request and response formats. 

**Find User Endpoint**

`HTTP POST {Rest Schema}://{Service hostname}:{Service port}/auth/{tenantId}/users`

whereas `tenantId` must not be null and is the Keycloak realm id. The server must accept a JSON request body of the following structure.
Keycloak can either send the `username` or the `email` to authenticate, this depends on the user input.

```json
{
  "username": "...",
  "email": "..."
}
```

If the server can find the user it must respond with a `200-OK` and send back the JSON representation of the mandatory user attributes in
the response body:

```json
{
  "externalUser": {
    "id": "...",
    "username": "...",
    "firstName": "...",
    "lastName": "...",
    "email": "..."
  }
}
```

If the user does not exist on server side, the response must be a `404-NOT_FOUND`.

**Validate User Endpoint**

As soon as the user has been found, Keycloak calls this endpoint with the username and the entered password in order to validate both are
correct:

`HTTP POST {Rest Schema}://{Service hostname}:{Service port}/auth/{tenantId}/users/validate`

whereas `tenantId` must not be null and is the Keycloak realm id. The server must accept a JSON request body with the username and the
inputted password.

```json
{
  "username": "...",
  "password": "..."
}
```

If the username/password combination is correct, the server responds with `200-OK` and an empty response body. If the combination is invalid
and doesn't match, the server must respond with a http status code `400-BAD_REQUEST`.

**Note**: Take into account, that the raw password is sent between Keycloak and the external service. So at least http over TLS must be used
between both parties.

# Theming
Keycloak can be themed externally by injecting CSS/JS and FTL files. On the other hand there is [Keycloakify](https://www.keycloakify.dev)
as a powerful solution for this.

[1]: src/site/resources/images/add-providers.png
[2]: src/site/resources/images/provider-details.png
[3]: src/site/resources/images/arch.jpg
