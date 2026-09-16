package com.fund_transfer.backend.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class RolePermissionMapper {

    private final Map<String, Set<String>> rolePermissions = Map.of(

            "RETAIL_CUSTOMER",
            Set.of(
                    "transfer:create",
                    "transfer:view",
                    "beneficiary:create",
                    "beneficiary:view",
                    "schedule:create",
                    "schedule:view"
            ),

            "BANK_ADMIN",
            Set.of(
                    "transfer:view"
            ),

            "BANK_SUPER_ADMIN",
            Set.of(
                    "transfer:create",
                    "transfer:view",
                    "beneficiary:create",
                    "beneficiary:view",
                    "schedule:create",
                    "schedule:view"
            )
    );

    public boolean hasPermission(
            String role,
            String permission) {

        return rolePermissions
                .getOrDefault(role, Set.of())
                .contains(permission);
    }
}