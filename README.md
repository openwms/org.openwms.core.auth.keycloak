# OpenWMS.org User Authentication Extension for Keycloak
This is extension is a `UserStorageProvider` implementation that can be integrated into [Keycloak](https://www.keycloak.org). Keycloak will
then integrate it into the user authentication procedure to authenticate against a remote http service.

## Usage in Keycloak
The jar file must be copied to Keycloaks `providers` directory. In case of the Keycloak Docker image this is at `/opt/keycloak/providers`.
A simple Dockerfile that adopts the original Keycloak Dockerfile looks like:

```dockerfile
FROM quay.io/keycloak/keycloak:26.1.4 AS builder

WORKDIR /opt/keycloak
COPY /target/external-http-auth.jar /opt/keycloak/providers

FROM quay.io/keycloak/keycloak:26.1.4
COPY --from=builder --chown=keycloak:root /opt/keycloak/ /opt/keycloak/
CMD ["start-dev"]
```

After the customized Keycloak image has been built and started, the `External-http` provider should be selectable and can now be created: 
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
Keycloak calls the configured service to authenticate unknown user. This service must have the two following endpoints.

**Find User Endpoint**

`HTTP POST {Rest Schema}://{Service hostname}:{Service port}/auth/{tenantId}/users`

whereas `tenantId` must not be null and is the Keycloak realm id. The server must accept a JSON request body of the following structure.
Keycloak can either send the `username` or the `email` to authenticate, this depends on the users input.

```json
{
  "username": "...",
  "email": "..."
}
```

If the server can find the user it responds with a `200 OK` and sends back the JSON representation of the mandatory user attributes in the
response body:

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

As soon as the user has been found, Keycloak calls the Validate endpoint with the username and the entered password in order to validate 
both are correct in the external service:

`HTTP POST {Rest Schema}://{Service hostname}:{Service port}/auth/{tenantId}/users/validate`

whereas `tenantId` must not be null and is the Keycloak realm id. The server must accept a JSON request body with the username and the
inputted password.

```json
{
  "username": "...",
  "password": "..."
}
```

If the username/password combination is correct, the server responds with `200-OK` and an emtpy response body. If the combination is invalid
and doesn't match the server must respond with a http status code `400-BAD_REQUEST`.

[1]: src/site/resources/images/add-providers.png
[2]: src/site/resources/images/provider-details.png
