package com.cinlogic.jwtexampleserver.services.dtos;

public class CreateAccountRequest extends UpdateAccountRequest {
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
