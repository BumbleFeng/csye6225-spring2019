package com.me.webapi.pojo;

import javax.validation.constraints.NotNull;

public class Reset {

    private String email;
    private String token;

    public Reset(){
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
