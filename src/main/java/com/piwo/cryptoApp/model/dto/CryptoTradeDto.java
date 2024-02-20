package com.piwo.cryptoApp.model.dto;

import com.piwo.cryptoApp.model.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class CryptoTradeDto {
    private TradeType tradeType;
    private String cryptoToTradeSymbol;
    private String baseCryptoSymbol;
    private BigDecimal amount;
    private BigDecimal unitPrice;
}