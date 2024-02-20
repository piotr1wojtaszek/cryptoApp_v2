package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exchangeApi.KuCoinApiClient;
import com.piwo.cryptoApp.model.dto.CryptoListRateDto;
import com.piwo.cryptoApp.model.dto.CryptoPairRateDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock
    private KuCoinApiClient kuCoinApiClient;

    @InjectMocks
    private ExchangeService exchangeService;

    @Test
    public void getCurrencyList_returnsList() {
        //given
        int page = 0;
        int size = 10;
        List<CryptoListRateDto> allPairs = new ArrayList<>();

        CryptoListRateDto pair1 = CryptoListRateDto.builder().build();
        CryptoListRateDto pair2 = CryptoListRateDto.builder().build();
        CryptoListRateDto pair3 = CryptoListRateDto.builder().build();

        allPairs.add(pair1);
        allPairs.add(pair2);
        allPairs.add(pair3);

        when(kuCoinApiClient.getAllPairs()).thenReturn(allPairs);
        //when
        List<CryptoListRateDto> resultList = exchangeService.getCurrencyList(page, size);
        //then
        assertThat(resultList.get(1), equalTo(allPairs.get(1)));
        assertThat(resultList.size(), equalTo(allPairs.size()));
    }

    @Test
    public void getCurrencyList_returnsEmptyList() {
        //given
        int page = 0;
        int size = 10;
        when(kuCoinApiClient.getAllPairs()).thenReturn(Collections.emptyList());
        //when
        List<CryptoListRateDto> resultList = exchangeService.getCurrencyList(page, size);
        //then
        assertThat(resultList, empty());
    }

    @Test
    public void getCurrency_returnsCurrency() {
        //given
        String baseCrypto = "BTC";
        String quoteCrypto = "USDT";
        String pairSymbol = "BTC-USDT";
        CryptoPairRateDto expectedPairRate = new CryptoPairRateDto();
        expectedPairRate.setCode("200");
        expectedPairRate.setCryptoPair(pairSymbol);
        when(kuCoinApiClient.getPairPrice(baseCrypto, quoteCrypto)).thenReturn(expectedPairRate);
        //when
        CryptoPairRateDto result = exchangeService.getCurrency(baseCrypto, quoteCrypto);
        //then
        assertThat(result, notNullValue());
        assertThat(result.getCryptoPair(), equalTo(pairSymbol));
    }

    @Test
    public void getCurrency_returnsNullValue() {
//given
        String baseCrypto = "ADA";
        String quoteCrypto = "LUNA";
        when(kuCoinApiClient.getPairPrice(baseCrypto, quoteCrypto)).thenReturn(null);
        //when
        CryptoPairRateDto result = exchangeService.getCurrency(baseCrypto, quoteCrypto);
        //then
        assertThat(result, nullValue());
    }

}