package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exchangeApi.KuCoinApiClient;
import com.piwo.cryptoApp.model.dto.CryptoListRateDto;
import com.piwo.cryptoApp.model.dto.CryptoPairRateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    private final KuCoinApiClient kuCoinApiClient;

    /**
     * Returns a list of cryptocurrencies from the KuCoin API. The list is paginated.
     * @param page list page
     * @param size number of elements on the page
     * @return cryptocurrency list.
     */
    public List<CryptoListRateDto> getCurrencyList(int page, int size) {
        List<CryptoListRateDto> allPairs = kuCoinApiClient.getAllPairs();

        return allPairs.isEmpty() ? Collections.emptyList() : paginateList(allPairs, page, size);
    }

    /**
     * Returns cryptocurrency pair from the KuCoin API by its symbols.
     * @param baseCrypto first symbol (like BTC)
     * @param quoteCrypto second symbol (like USDT)
     * @return cryptocurrency pair (like BTC-USDT)
     */
    public CryptoPairRateDto getCurrency(String baseCrypto, String quoteCrypto) {
        return kuCoinApiClient.getPairPrice(baseCrypto, quoteCrypto);
    }

    /**
     * Paginates List.
     * @param list list to paginate.
     * @param page page of list.
     * @param size size of elements on page
     * @return Page of list.
     * @param <T> type of list.
     */
    private <T> List<T> paginateList(List<T> list, int page, int size) {
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, list.size());

        return startIndex > endIndex ? Collections.emptyList() : list.subList(startIndex, endIndex);
    }
}
