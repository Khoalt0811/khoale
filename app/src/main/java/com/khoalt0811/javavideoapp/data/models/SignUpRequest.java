package com.khoalt0811.javavideoapp.data.models;

public class SignUpRequest {
    private String email;
    private String password;

    public SignUpRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    // Setters để hỗ trợ serialization
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}