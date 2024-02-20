package com.piwo.cryptoApp.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CryptoListRateDto {
    private LocalDateTime timestamp;
    private List<TickerDto> data;


    @Getter
    @Builder
    public static class TickerDto {
        private String symbol;
        private String buy;
        private String sell;
    }

}
