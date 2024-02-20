package com.piwo.cryptoApp.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletCrypto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Crypto crypto;
    @Column(precision = 20, scale = 8)
    private BigDecimal amount;
    @Column(precision = 20, scale = 8)
    private BigDecimal value;

    private WalletCrypto(Crypto crypto, BigDecimal amount, BigDecimal value) {
        this.crypto = crypto;
        this.amount = amount;
        this.value = value;
    }

    public static WalletCryptoBuilder builder() {
        return new WalletCryptoBuilder();
    }

    public static class WalletCryptoBuilder {
        private Crypto crypto;
        private BigDecimal amount;
        private BigDecimal value;


        public WalletCryptoBuilder crypto(Crypto crypto) {
            this.crypto = crypto;
            return this;
        }

        public WalletCryptoBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public WalletCryptoBuilder value(BigDecimal value) {
            this.value = value;
            return this;
        }

        public WalletCrypto build() {
            return new WalletCrypto(crypto, amount, value);
        }
    }
}





