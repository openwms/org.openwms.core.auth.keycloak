FROM quay.io/keycloak/keycloak:26.1.4 AS builder

WORKDIR /opt/keycloak
COPY /target/external-http-auth.jar /opt/keycloak/providers

FROM quay.io/keycloak/keycloak:26.1.4
COPY --from=builder --chown=keycloak:root /opt/keycloak/ /opt/keycloak/

CMD ["start"]