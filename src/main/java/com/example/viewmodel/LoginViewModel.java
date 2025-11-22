package com.example.viewmodel;

public class LoginViewModel {
    private String clientId;

    public LoginViewModel(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
