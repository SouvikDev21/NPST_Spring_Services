package com.fund_transfer.backend.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component("permissionService")
public class PermissionService {

    private final RolePermissionMapper rolePermissionMapper;

    public PermissionService(RolePermissionMapper rolePermissionMapper) {
        this.rolePermissionMapper = rolePermissionMapper;
    }

    public boolean hasPermission(
            Authentication authentication,
            String permission) {

        if (authentication == null || permission == null) {
            return false;
        }

        for (GrantedAuthority authority :
                authentication.getAuthorities()) {

            if (rolePermissionMapper.hasPermission(
                    authority.getAuthority(),
                    permission)) {

                return true;
            }
        }

        return false;
    }
}