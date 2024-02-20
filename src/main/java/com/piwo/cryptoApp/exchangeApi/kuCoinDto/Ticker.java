package com.piwo.cryptoApp.exchangeApi.kuCoinDto;

import lombok.Getter;

@Getter
public class Ticker {
    private String symbol;
    private String buy;
    private String sell;
}
