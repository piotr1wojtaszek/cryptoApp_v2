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
public class FiatTradeDto {
    private TradeType tradeType;
    private String fiatSymbol;
    private BigDecimal amount;
}
