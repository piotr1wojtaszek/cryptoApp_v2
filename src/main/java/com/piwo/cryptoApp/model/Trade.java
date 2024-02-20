package com.piwo.cryptoApp.model;

import com.piwo.cryptoApp.model.enums.TradeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime timestamp;
    private TradeType type;
    private Long accountId;
    private String cryptoToTradeSymbol;
    private String cryptoToSellSymbol;
    private String fiatSymbol;
    private BigDecimal amount;
    private BigDecimal fee;
    private BigDecimal unitPrice;       // cena jednostkowa kryptowaluty
    private BigDecimal price;           // ???
    private BigDecimal totalValue;      // wartość zakupionego/sprzedanego kryptowaluty
    private BigDecimal total;


    public Trade() {
    }

    public Trade(LocalDateTime timestamp, TradeType type, Long accountId, String cryptoToTradeSymbol, String cryptoToSellSymbol, BigDecimal amount, BigDecimal fee, BigDecimal unitPrice, BigDecimal totalValue) {
        this.timestamp = timestamp;
        this.type = type;
        this.accountId = accountId;
        this.cryptoToTradeSymbol = cryptoToTradeSymbol;
        this.cryptoToSellSymbol = cryptoToSellSymbol;
        this.amount = amount;
        this.fee = fee;
        this.unitPrice = unitPrice;
        this.totalValue = totalValue;
    }

    public Trade(LocalDateTime timestamp, TradeType type, Long accountId, String fiatSymbol, BigDecimal amount, BigDecimal fee, BigDecimal unitPrice, BigDecimal totalValue, BigDecimal total) {
        this.timestamp = timestamp;
        this.type = type;
        this.accountId = accountId;
        this.fiatSymbol = fiatSymbol;
        this.amount = amount;
        this.fee = fee;
        this.unitPrice = unitPrice;
        this.totalValue = totalValue;
        this.total = total;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trade trade)) return false;
        return Objects.equals(id, trade.id) && Objects.equals(timestamp, trade.timestamp) && type == trade.type && Objects.equals(accountId, trade.accountId) && Objects.equals(cryptoToTradeSymbol, trade.cryptoToTradeSymbol) && Objects.equals(cryptoToSellSymbol, trade.cryptoToSellSymbol) && Objects.equals(amount, trade.amount) && Objects.equals(fee, trade.fee) && Objects.equals(unitPrice, trade.unitPrice) && Objects.equals(price, trade.price) && Objects.equals(totalValue, trade.totalValue) && Objects.equals(total, trade.total) && Objects.equals(fiatSymbol, trade.fiatSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, timestamp, type, accountId, cryptoToTradeSymbol, cryptoToSellSymbol, amount, fee, unitPrice, price, totalValue, total, fiatSymbol);
    }

    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", type=" + type +
                ", accountId=" + accountId +
                ", cryptoToTradeSymbol='" + cryptoToTradeSymbol + '\'' +
                ", cryptoToSellSymbol='" + cryptoToSellSymbol + '\'' +
                ", amount=" + amount +
                ", fee=" + fee +
                ", unitPrice=" + unitPrice +
                ", price=" + price +
                ", totalValue=" + totalValue +
                ", total=" + total +
                ", fiatSymbol='" + fiatSymbol + '\'' +
                '}';
    }
}
