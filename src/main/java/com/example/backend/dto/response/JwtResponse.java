package com.example.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private Long id;
    private String name;
    private String email;
    private String role;
    private Long donViId;
    
    // Default prefix for JWT
    public String getType() {
        return "Bearer";
    }
}
