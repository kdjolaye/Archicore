package com.klaye.monolith.auth.dto;

import com.klaye.monolith.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResult {
    private User user;
    private String accessToken;
    private String refreshToken;
}
