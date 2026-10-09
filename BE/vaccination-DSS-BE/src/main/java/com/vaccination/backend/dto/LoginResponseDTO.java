package com.vaccination.backend.dto;

public class LoginResponseDTO {

    private String token;
    private String role;

    public LoginResponseDTO(String token, String role) {
        this.token = token;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getRole() { return role; }
}
