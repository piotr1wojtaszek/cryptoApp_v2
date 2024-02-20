package com.piwo.cryptoApp.model.dto;

import com.piwo.cryptoApp.exchangeApi.kuCoinDto.KuCoinData;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CryptoPairRateDto {
    private LocalDateTime timestamp;
    private String code;
    private String cryptoPair;
    private KuCoinData data;
}