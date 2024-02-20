package com.piwo.cryptoApp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany
    private List<WalletCrypto> walletCryptoList;
    @OneToMany
    private List<Trade> transactionHistory;
    @Column(precision = 20, scale = 8)
    private BigDecimal balance;

    public Wallet() {
        this.walletCryptoList = new ArrayList<>();
        this.transactionHistory = new ArrayList<>();
        this.balance = BigDecimal.ZERO;     // 0E-8 zamiast 0, bo scale=8 (wart. naukowa)
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", walletCryptoList=" + walletCryptoList +
                ", tradeList=" + transactionHistory +
                ", balance=" + balance +
                '}';
    }
}
