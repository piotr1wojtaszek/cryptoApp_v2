package com.piwo.cryptoApp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String role;
    private Boolean enable;
    private LocalDateTime createdAt;
    @OneToOne
    private Wallet wallet;

    public Account(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = Role.USER.getAuthority();
        this.enable = false;
        this.createdAt = LocalDateTime.now();
    }

    public Account(AccountBuilder accountBuilder) {
        this.username = accountBuilder.username;
        this.email = accountBuilder.email;
        this.password = accountBuilder.password;
        this.role = Role.USER.getAuthority();
        this.enable = false;
        this.createdAt = LocalDateTime.now();
    }

    public static AccountBuilder builder() {
        return new AccountBuilder();
    }

    public static class AccountBuilder {
        private String username;
        private String email;
        private String password;
        private String role;
        private Boolean enable;
        private LocalDateTime createdAt;

        public AccountBuilder() {
        }

        public AccountBuilder(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }

        public AccountBuilder role(String role) {
            this.role = role;
            return this;
        }

        public AccountBuilder enable(Boolean enable) {
            this.enable = enable;
            return this;
        }

        public AccountBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AccountBuilder username(String username) {
            this.username = username;
            return this;
        }

        public AccountBuilder email(String email) {
            this.email = email;
            return this;
        }

        public AccountBuilder password(String password) {
            this.password = password;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                ", enable=" + enable +
                ", createdAt=" + createdAt +
                ", wallet=" + wallet +
                '}';
    }
}