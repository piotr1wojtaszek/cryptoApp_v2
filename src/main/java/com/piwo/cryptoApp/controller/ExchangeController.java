package com.piwo.cryptoApp.controller;

import com.piwo.cryptoApp.model.dto.CryptoListRateDto;
import com.piwo.cryptoApp.model.dto.CryptoPairRateDto;
import com.piwo.cryptoApp.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cryptoApp/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    //only for test purposes
    @GetMapping("/health")
    public String health() {
        return "connected to the exchange endpoint.";
    }


    @GetMapping("/currency_list")
    public List<CryptoListRateDto> getCurrencyList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return exchangeService.getCurrencyList(page, size);
    }

    @GetMapping("/currency/")
    public CryptoPairRateDto getCurrencyRate(@RequestParam String baseCrypto, @RequestParam String quoteCrypto) {
        return exchangeService.getCurrency(baseCrypto, quoteCrypto);
    }
}
