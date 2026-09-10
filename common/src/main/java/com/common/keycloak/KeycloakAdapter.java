package com.common.keycloak;

import java.util.Optional;

public interface KeycloakAdapter {

    String resolveCurrentCif(String fallbackCif);

    Optional<String> getCifFromCurrentToken();

    Optional<String> getUserIdFromCurrentToken();

    KeycloakUserContext getCurrentUserContext();
}
