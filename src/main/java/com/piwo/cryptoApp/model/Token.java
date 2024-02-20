package com.piwo.cryptoApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tokenNumber;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private LocalDateTime confirmedAt;

    @ManyToOne
    @JoinColumn(nullable = false, name = "account_id")
    private Account account;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Token token)) return false;
        return Objects.equals(id, token.id) && Objects.equals(tokenNumber, token.tokenNumber) && Objects.equals(createdAt, token.createdAt) && Objects.equals(expiredAt, token.expiredAt) && Objects.equals(confirmedAt, token.confirmedAt) && Objects.equals(account, token.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tokenNumber, createdAt, expiredAt, confirmedAt, account);
    }
}
