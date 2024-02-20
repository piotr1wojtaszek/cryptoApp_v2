package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.CryptoNotFoundException;
import com.piwo.cryptoApp.exchangeApi.KuCoinApiClient;
import com.piwo.cryptoApp.model.Crypto;
import com.piwo.cryptoApp.model.dto.CryptoListRateDto;
import com.piwo.cryptoApp.model.dto.CryptoListRateDto.TickerDto;
import com.piwo.cryptoApp.repository.CryptoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CryptoService {
    private static final String USDT_SYMBOL = "USDT";
    private static final BigDecimal USDT_PRICE = new BigDecimal("0.9997");
    private final CryptoRepository cryptoRepository;
    private final KuCoinApiClient kuCoinApiClient;

    /**
     * Returns cryptocurrency from database by its symbol.
     * @param symbol cryptocurrency symbol
     * @return Crypto.
     */
    public Crypto getCrypto(String symbol) {
        return cryptoRepository.findBySymbol(symbol)
                .orElseThrow(() -> new CryptoNotFoundException(symbol));
    }

//only for test purposes
//    public List<Crypto> getCryptoList() {
//        return cryptoRepository.findAll();
//    }

    /**
     * creates an ticker list based on data from the KuCoin API.
     * @return new ticker list.
     */
    public List<TickerDto> createTickerList() {
        List<CryptoListRateDto> cryptoListRateDtoList = kuCoinApiClient.getAllPairs();
        return cryptoListRateDtoList.stream()
                .map(CryptoListRateDto::getData)
                .flatMap(List::stream)
                .toList();
    }

    /**
     * Creates ticker list, and returns from it list of cryptocurrency symbols.
     * @return list of cryptocurrency symbols.
     */
    public List<String> getSymbolList() {
        List<TickerDto> tickerList = createTickerList();
        return tickerList.stream()
                .map(TickerDto::getSymbol)
                .map(symbol -> symbol.split("-")[0])
                .toList();
    }

    /**
     * Creates a list of cryptocurrencies when the application starts
     * @return cryptocurrency list.
     */
    @PostConstruct  //run with application context startup
    public List<Crypto> createCryptoList() {
        cryptoRepository.deleteAll();

        List<Crypto> cryptoList = new ArrayList<>();
        List<TickerDto> tickerList = createTickerList();
        List<String> symbolList = getSymbolList();

        for (String symbol : symbolList) {
            Optional<TickerDto> optionalTickerDto = tickerList.stream()
                    .filter(tickerDto -> tickerDto.getSymbol().startsWith(symbol))
                    .findFirst();

            if (optionalTickerDto.isPresent()) {
                TickerDto tickerDto = optionalTickerDto.get();

                if (tickerDto.getSymbol() != null && tickerDto.getBuy() != null && tickerDto.getSell() != null) {
                    BigDecimal newPrice = new BigDecimal(tickerDto.getBuy());

                    //Sprawdzanie, czy krypto o tym symbolu już istnieje w cryptoList
                    boolean isAlreadyExists = cryptoList.stream()
                            .anyMatch(crypto -> crypto.getSymbol().equals(symbol));
//
                    if (!isAlreadyExists) {
                        //nie używam wzorca builder, bo ternary operator potrzebuje ustawionego symbolu
                        Crypto crypto = new Crypto();
                        crypto.setSymbol(symbol);
                        crypto.setPrice(crypto.getSymbol().equals(USDT_SYMBOL) ? USDT_PRICE : newPrice);
                        cryptoList.add(crypto);
                    }
                }
            }
        }
        return cryptoRepository.saveAll(cryptoList);
    }

    /**
     * Periodically updates cryptocurrency prices in the existing list.
     */
    @Scheduled(fixedDelay = 60000) //schedule every 1 min
    public void updateCryptoPrices() {
        List<TickerDto> tickerList = createTickerList();
        List<String> symbolList = getSymbolList();

        for (String symbol : symbolList) {
            Optional<TickerDto> optionalTickerDto = tickerList.stream()
                    .filter(tickerDto -> tickerDto.getSymbol().startsWith(symbol))
                    .findFirst();

            if (optionalTickerDto.isPresent()) {
                TickerDto tickerDto = optionalTickerDto.get();
                BigDecimal newPrice = new BigDecimal(tickerDto.getBuy());

                Optional<Crypto> bySymbol = cryptoRepository.findBySymbol(symbol);
                if (bySymbol.isPresent()) {
                    Crypto crypto = bySymbol.get();
                    if (!crypto.getPrice().equals(newPrice) && !crypto.getSymbol().equals(USDT_SYMBOL)) {
                        crypto.setPrice(newPrice);
                        cryptoRepository.save(crypto);
                    }
                }
            }
        }
    }

    /**
     * Updates specific cryptocurrency on demand.
     * @param symbolToUpdate cryptocurrency symbol.
     */
    public void updateCryptoPrice(String symbolToUpdate) {
        List<TickerDto> tickerList = createTickerList();
        Optional<TickerDto> optionalTickerDto = tickerList.stream()
                .filter(tickerDto -> tickerDto.getSymbol().startsWith(symbolToUpdate))
                .findFirst();

        if (optionalTickerDto.isPresent()) {
            TickerDto tickerDto = optionalTickerDto.get();
            BigDecimal newPrice = new BigDecimal(tickerDto.getBuy());

            Optional<Crypto> bySymbol = cryptoRepository.findBySymbol(symbolToUpdate);
            if (bySymbol.isPresent()) {
                Crypto crypto = bySymbol.get();
                if (!crypto.getPrice().equals(newPrice)) {
                    crypto.setPrice(newPrice);
                    cryptoRepository.save(crypto);
                }
            }
        }
    }

    /** for testing purposes
     * removes cryptocurrency from list.
     * @param symbol cryptocurrency symbol.
     * @return note about successful deleting.
     */
    public String deleteCrypto(String symbol) {
        if (cryptoRepository.findBySymbol(symbol).isPresent()) {
            cryptoRepository.deleteBySymbol(symbol);
            return "Crypto deleted with symbol: " + symbol;
        }
        throw new CryptoNotFoundException(symbol);
    }
}