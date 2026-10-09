package com.vaccination.backend.service;

import com.vaccination.backend.dto.LoginRequestDTO;
import com.vaccination.backend.dto.LoginResponseDTO;
import com.vaccination.backend.util.JwtUtil;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(JdbcTemplate jdbcTemplate, JwtUtil jwtUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        String[] userRow;
        try {
            userRow = jdbcTemplate.queryForObject(
                    "SELECT id::text, encrypted_password FROM auth.users WHERE email = ? AND email_confirmed_at IS NOT NULL",
                    (rs, rowNum) -> new String[]{rs.getString("id"), rs.getString("encrypted_password")},
                    request.getEmail()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        if (userRow == null || !passwordEncoder.matches(request.getPassword(), userRow[1])) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        UUID userId = UUID.fromString(userRow[0]);

        String role;
        try {
            role = jdbcTemplate.queryForObject(
                    "SELECT r.name FROM user_profiles up JOIN roles r ON r.id = up.role_id WHERE up.id = ?",
                    String.class,
                    userId
            );
        } catch (EmptyResultDataAccessException e) {
            role = "user";
        }

        String token = jwtUtil.generateToken(userId, role);
        return new LoginResponseDTO(token, role);
    }
}
