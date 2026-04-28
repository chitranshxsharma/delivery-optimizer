package com.delivery.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type;
    private Long id;
    private String name;
    private String email;
    private String role;
    public JwtResponse(String token, Long id, String name, String email, String role) {
        this.token = token; this.type = "Bearer"; this.id = id;
        this.name = name; this.email = email; this.role = role;
    }
}
