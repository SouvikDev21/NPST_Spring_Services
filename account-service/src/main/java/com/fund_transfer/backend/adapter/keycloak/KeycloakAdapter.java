package com.fund_transfer.backend.adapter.keycloak;

import java.util.Optional;

public interface KeycloakAdapter {

    String resolveCurrentCif(String fallbackCif);

    Optional<String> getCifFromCurrentToken();

    Optional<String> getUserIdFromCurrentToken();

    KeycloakUserContext getCurrentUserContext();
}
