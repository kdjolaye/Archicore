package com.klaye.monolith.user.mapper;

import com.klaye.monolith.user.dto.PermissionResponse;
import com.klaye.monolith.user.dto.RoleResponse;
import com.klaye.monolith.user.dto.UserResponse;
import com.klaye.monolith.user.entity.Role;
import com.klaye.monolith.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper pour la conversion entre Entités User/Role et DTOs.
 */
@Component
public class UserMapper {

    /**
     * Convertit une entité User en réponse DTO.
     */
    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getPhone(),
                user.getStatus() != null ? user.getStatus().name() : null,
                user.getCreatedAt(),
                toRoleResponse(user.getRole())
        );
    }

    /**
     * Convertit une entité Role en réponse DTO.
     */
    public RoleResponse toRoleResponse(Role role) {
        if (role == null) return null;

        return new RoleResponse(
                role.getName(),
                role.getPermissions().stream()
                        .map(p -> new PermissionResponse(p.getName()))
                        .collect(Collectors.toSet())
        );
    }
}
