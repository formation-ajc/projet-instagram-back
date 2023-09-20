package com.projet.instagram.schema.response.security;

import java.util.List;

public class SignupResponse {
    private String token;
    private String type = "Bearer";
    private String refreshToken;
    private Long id;
    private String email;
    private String firstname;
    private String lastname;
    private final List<String> roles;

    public SignupResponse(
        String accessToken,
        String refreshToken,
        Long id,
        String email,
        String firstname,
        String lastname,
        List<String> roles
    ) {
        this.token = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.roles = roles;
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}