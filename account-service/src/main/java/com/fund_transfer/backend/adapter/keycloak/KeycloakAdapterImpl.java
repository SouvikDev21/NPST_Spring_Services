package com.fund_transfer.backend.adapter.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fund_transfer.backend.common.constants.CustomerConstants;
import com.fund_transfer.backend.common.constants.KeycloakConstants;
import com.fund_transfer.backend.config.KeycloakProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class KeycloakAdapterImpl implements KeycloakAdapter {

    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;
    private final KeycloakProperties keycloakProperties;

    public KeycloakAdapterImpl(HttpServletRequest request,
                               ObjectMapper objectMapper,
                               KeycloakProperties keycloakProperties) {
        this.request = request;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.keycloakProperties = keycloakProperties;
    }

    @Override
    public String resolveCurrentCif(String fallbackCif) {
        Optional<String> tokenCif = getCifFromCurrentToken();
        if (tokenCif.isPresent() && !tokenCif.get().isBlank()) {
            return tokenCif.get();
        }

        if (fallbackCif != null && !fallbackCif.isBlank()) {
            return fallbackCif;
        }

        throw new SecurityException(CustomerConstants.ERR_CUSTOMER_ID_REQUIRED);
    }

    @Override
    public Optional<String> getCifFromCurrentToken() {
        // 1. Try to read from SecurityContextHolder (Spring Security OAuth2 Resource Server)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String cif = extractCifFromClaims(jwt.getClaims());
            if (cif != null && !cif.isBlank()) {
                return Optional.of(cif);
            }
        }

        // 2. Try to read from HttpServletRequest Authorization header directly
        try {
            if (request != null) {
                String authHeader = request.getHeader(KeycloakConstants.HEADER_AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith(KeycloakConstants.BEARER_PREFIX)) {
                    String token = authHeader.substring(KeycloakConstants.BEARER_PREFIX.length()).trim();
                    Map<String, Object> claims = parseJwtClaims(token);
                    String cif = extractCifFromClaims(claims);
                    if (cif != null && !cif.isBlank()) {
                        return Optional.of(cif);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Unable to parse JWT token from Authorization header: {}", e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<String> getUserIdFromCurrentToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            if (sub != null && !sub.isBlank()) {
                return Optional.of(sub);
            }
        }

        try {
            if (request != null) {
                String authHeader = request.getHeader(KeycloakConstants.HEADER_AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith(KeycloakConstants.BEARER_PREFIX)) {
                    String token = authHeader.substring(KeycloakConstants.BEARER_PREFIX.length()).trim();
                    Map<String, Object> claims = parseJwtClaims(token);
                    Object subObj = claims.get(KeycloakConstants.CLAIM_SUB);
                    if (subObj != null && !String.valueOf(subObj).isBlank()) {
                        return Optional.of(String.valueOf(subObj));
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Unable to parse user ID from token: {}", e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public KeycloakUserContext getCurrentUserContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            Map<String, Object> claims = jwt.getClaims();
            String userId = jwt.getSubject();
            String cif = extractCifFromClaims(claims);
            String username = (String) claims.getOrDefault(KeycloakConstants.CLAIM_PREFERRED_USERNAME, userId);
            String email = (String) claims.get(KeycloakConstants.CLAIM_EMAIL);
            Set<String> roles = extractRoles(claims);

            return KeycloakUserContext.builder()
                    .userId(userId)
                    .cif(cif)
                    .username(username)
                    .email(email)
                    .roles(roles)
                    .authenticated(true)
                    .build();
        }

        // Fallback check on request header
        try {
            if (request != null) {
                String authHeader = request.getHeader(KeycloakConstants.HEADER_AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith(KeycloakConstants.BEARER_PREFIX)) {
                    String token = authHeader.substring(KeycloakConstants.BEARER_PREFIX.length()).trim();
                    Map<String, Object> claims = parseJwtClaims(token);
                    String userId = (String) claims.get(KeycloakConstants.CLAIM_SUB);
                    String cif = extractCifFromClaims(claims);
                    String username = (String) claims.getOrDefault(KeycloakConstants.CLAIM_PREFERRED_USERNAME, userId);
                    String email = (String) claims.get(KeycloakConstants.CLAIM_EMAIL);
                    Set<String> roles = extractRoles(claims);

                    return KeycloakUserContext.builder()
                            .userId(userId)
                            .cif(cif)
                            .username(username)
                            .email(email)
                            .roles(roles)
                            .authenticated(true)
                            .build();
                }
            }
        } catch (Exception e) {
            log.debug("Failed parsing user context from header: {}", e.getMessage());
        }

        return KeycloakUserContext.anonymous();
    }

    private String extractCifFromClaims(Map<String, Object> claims) {
        if (claims == null) {
            return null;
        }

        // Priority 0: Configured key if custom
        if (keycloakProperties != null && keycloakProperties.getClaimCifKey() != null) {
            Object customKeyVal = claims.get(keycloakProperties.getClaimCifKey());
            if (customKeyVal != null) {
                return String.valueOf(customKeyVal);
            }
        }

        // Priority 1: cif
        Object cifObj = claims.get(KeycloakConstants.CLAIM_CIF);
        if (cifObj != null) {
            return String.valueOf(cifObj);
        }

        // Priority 2: cif_id
        Object cifIdObj = claims.get(KeycloakConstants.CLAIM_CIF_ID);
        if (cifIdObj != null) {
            return String.valueOf(cifIdObj);
        }

        // Priority 3: customer_id
        Object customerIdObj = claims.get(KeycloakConstants.CLAIM_CUSTOMER_ID);
        if (customerIdObj != null) {
            return String.valueOf(customerIdObj);
        }

        // Priority 4: check if preferred_username is in CIF format (e.g. CIF100001)
        Object usernameObj = claims.get(KeycloakConstants.CLAIM_PREFERRED_USERNAME);
        if (usernameObj != null && String.valueOf(usernameObj).toUpperCase().startsWith("CIF")) {
            return String.valueOf(usernameObj);
        }

        // Priority 5: check if sub starts with CIF
        Object subObj = claims.get(KeycloakConstants.CLAIM_SUB);
        if (subObj != null && String.valueOf(subObj).toUpperCase().startsWith("CIF")) {
            return String.valueOf(subObj);
        }

        return null;
    }

    private Set<String> extractRoles(Map<String, Object> claims) {
        Set<String> roles = new HashSet<>();
        if (claims == null) {
            return roles;
        }

        Object realmAccess = claims.get(KeycloakConstants.CLAIM_REALM_ACCESS);
        if (realmAccess instanceof Map<?, ?> realmMap) {
            Object rolesObj = realmMap.get(KeycloakConstants.CLAIM_ROLES);
            if (rolesObj instanceof Collection<?> roleList) {
                for (Object r : roleList) {
                    roles.add(String.valueOf(r));
                }
            }
        }
        return roles;
    }

    private Map<String, Object> parseJwtClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length >= 2) {
                byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
                String payload = new String(decodedBytes, StandardCharsets.UTF_8);
                JsonNode jsonNode = objectMapper.readTree(payload);
                Map<String, Object> map = new HashMap<>();
                jsonNode.fields().forEachRemaining(entry -> {
                    if (entry.getValue().isTextual()) {
                        map.put(entry.getKey(), entry.getValue().asText());
                    } else if (entry.getValue().isNumber()) {
                        map.put(entry.getKey(), entry.getValue().numberValue());
                    } else if (entry.getValue().isBoolean()) {
                        map.put(entry.getKey(), entry.getValue().asBoolean());
                    } else {
                        map.put(entry.getKey(), objectMapper.convertValue(entry.getValue(), Map.class));
                    }
                });
                return map;
            }
        } catch (Exception e) {
            log.debug("Could not parse JWT token: {}", e.getMessage());
        }
        return Collections.emptyMap();
    }
}
