# OpenWMS.org User Authentication Extension for Keycloak
This is extension is a `UserStorageProvider` implementation that can be integrated into [Keycloak](https://www.keycloak.org). Keycloak will
then integrate it into the user authentication procedure to authenticate against a remote http service.

## Usage
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

[1]: src/site/resources/images/add-providers.png
[2]: src/site/resources/images/provider-details.png
