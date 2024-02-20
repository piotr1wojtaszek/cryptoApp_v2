package com.piwo.cryptoApp.exchangeApi.kuCoinDto;

import lombok.Getter;

import java.util.List;
@Getter
public class KuCoinDataList {
    private List<Ticker> ticker;
}
