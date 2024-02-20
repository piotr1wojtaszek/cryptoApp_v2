package com.piwo.cryptoApp.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDto {
    private String username;
    private String password;
    private String email;

    public AccountDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
