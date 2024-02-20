package com.piwo.cryptoApp.exchangeApi;

import com.piwo.cryptoApp.exchangeApi.kuCoinDto.KuCoinListResponse;
import com.piwo.cryptoApp.exchangeApi.kuCoinDto.KuCoinPairResponse;
import com.piwo.cryptoApp.exchangeApi.kuCoinDto.Ticker;
import com.piwo.cryptoApp.model.dto.CryptoListRateDto;
import com.piwo.cryptoApp.model.dto.CryptoPairRateDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class KuCoinApiClient {
    private static final String API_KUCOIN_URL = "https://api.kucoin.com";
    private static final String GET_PAIR = "/api/v1/market/orderbook/level1?symbol=";
    private static final String GET_ALL_TICKERS = "/api/v1/market/allTickers";

    /**
     * Returns cryptocurrencies pair by its symbols.
     *
     * @param baseCrypto  first symbol (like BTC)
     * @param quoteCrypto second symbol (like USDT)
     * @return cryptocurrency pair (like BTC-USDT)
     */
    public CryptoPairRateDto getPairPrice(String baseCrypto, String quoteCrypto) {
        String symbol = baseCrypto.toUpperCase() + "-" + quoteCrypto.toUpperCase();
        KuCoinPairResponse kuCoinPairResponse = callGetMethod(GET_PAIR + symbol, KuCoinPairResponse.class);
        return CryptoPairRateDto.builder()
                .timestamp(LocalDateTime.now())
                .cryptoPair(symbol)
                .data(kuCoinPairResponse.getData())
                .build();
    }

    /**
     * Returns list of cryptocurrency pairs from KuCoin API.
     * @return cryptocurrency list.
     */
    public List<CryptoListRateDto> getAllPairs() {
        KuCoinListResponse kuCoinListResponse = callGetMethod(GET_ALL_TICKERS, KuCoinListResponse.class);
        List<Ticker> tickerList = kuCoinListResponse.getData().getTicker();
        return tickerList.stream()
                .map(ticker -> {
                    CryptoListRateDto.TickerDto tickerDto = CryptoListRateDto.TickerDto.builder()
                            .symbol(ticker.getSymbol())
                            .buy(ticker.getBuy())
                            .sell(ticker.getSell())
                            .build();
                    return CryptoListRateDto.builder()
                            .timestamp(LocalDateTime.now())
                            .data(Collections.singletonList(tickerDto))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * creates response for KuCoin API
     */
    private <T> T callGetMethod(String url, Class<T> responseType) {
        return restTemplate.getForObject(API_KUCOIN_URL + url, responseType);
    }
}
