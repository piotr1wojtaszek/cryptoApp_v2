package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.CryptoNotFoundException;
import com.piwo.cryptoApp.exchangeApi.KuCoinApiClient;
import com.piwo.cryptoApp.model.Crypto;
import com.piwo.cryptoApp.model.dto.CryptoListRateDto;
import com.piwo.cryptoApp.repository.CryptoRepository;
import org.checkerframework.checker.nullness.Opt;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    @Mock
    private CryptoRepository cryptoRepository;

    @Mock
    private KuCoinApiClient kuCoinApiClient;

    @InjectMocks
    private CryptoService cryptoService;

    @Test
    public void getCrypto_returnsCrypto() {
        String symbol = "BTC";
        Crypto crypto = new Crypto();
        when(cryptoRepository.findBySymbol(symbol)).thenReturn(Optional.of(crypto));
        //when
        Crypto result = cryptoService.getCrypto(symbol);
        assertThat(result, equalTo(crypto));
    }

    @Test
    public void getCrypto_throwsCryptoNotFoundException() {
        String symbol = "fakeSymbol";
        when(cryptoRepository.findBySymbol(symbol)).thenReturn(Optional.empty());
        //when/then
        assertThrows(CryptoNotFoundException.class, () -> cryptoService.getCrypto(symbol));
    }

    @Test
    public void createTickerList_success() {
        //given
        CryptoListRateDto.TickerDto tickerDto1 = CryptoListRateDto.TickerDto.builder()
                .symbol("BTC-USDT").buy("30000").sell("31000").build();

        List<CryptoListRateDto.TickerDto> tickerDtoList = List.of(tickerDto1);
        List<CryptoListRateDto> cryptoListRateDtoList = List.of(
                CryptoListRateDto.builder()
                        .timestamp(LocalDateTime.now()).data(tickerDtoList).build()
        );
        when(kuCoinApiClient.getAllPairs()).thenReturn(cryptoListRateDtoList);
        //when
        List<CryptoListRateDto.TickerDto> result = cryptoService.createTickerList();
        //then
        assertThat(result, notNullValue());
        assertThat(result, hasSize(1));

        CryptoListRateDto.TickerDto tickerDtoResult = result.get(0);
        assertThat(tickerDtoResult.getSymbol(), equalTo("BTC-USDT"));
        assertThat(tickerDtoResult.getBuy(), equalTo("30000"));
        assertThat(tickerDtoResult.getSell(), equalTo("31000"));
    }

    @Test
    public void getSymbolList_ReturnsListOfSymbols() {
        //given
        CryptoListRateDto.TickerDto tickerDto1 = CryptoListRateDto.TickerDto.builder()
                .symbol("BTC-USDT").buy("30000").sell("31000").build();
        CryptoListRateDto.TickerDto tickerDto2 = CryptoListRateDto.TickerDto.builder()
                .symbol("ETH-USDT").buy("2000").sell("2100").build();

        List<CryptoListRateDto.TickerDto> tickerDtoList = List.of(tickerDto1, tickerDto2);

        List<CryptoListRateDto> cryptoListRateDtoList = List.of(
                CryptoListRateDto.builder()
                        .timestamp(LocalDateTime.now()).data(tickerDtoList).build()
        );
        when(kuCoinApiClient.getAllPairs()).thenReturn(cryptoListRateDtoList);
        //when
        List<String> resultList = cryptoService.getSymbolList();
        //then
        assertThat(resultList, notNullValue());
        assertThat(resultList, hasSize(2));
        assertThat(resultList, contains("BTC", "ETH"));
    }

    @Test
    @Disabled   // resultList jest pusta, znajdź błąd
    public void createCryptoList_returnsCryptoList() {
        // given
        List<String> symbolList = List.of("BTC", "ETH");
        List<CryptoListRateDto.TickerDto> tickerList = new ArrayList<>();
        tickerList.add(CryptoListRateDto.TickerDto.builder()
                .symbol("BTC-USDT")
                .buy("32000")
                .sell("31000")
                .build());
        tickerList.add(CryptoListRateDto.TickerDto.builder()
                .symbol("ETH-USDT")
                .buy("2200")
                .sell("2100")
                .build());

        // Mockowanie zachowania metody getSymbolList()
        when(cryptoService.getSymbolList()).thenReturn(symbolList);
        // Mockowanie zachowania metody createTickerList()
        when(cryptoService.createTickerList()).thenReturn(tickerList);      //tu jest niepoprawnie mockowane
        // when
        List<Crypto> result = cryptoService.createCryptoList();
        // then
        assertThat(result, notNullValue());
        assertThat(result, hasSize(2));
        // dodaj asercje, które potwierdzą czy dane się zgadzają
    }

    @Test
    public void createCryptoList_noTickers() {
        //given
        when(kuCoinApiClient.getAllPairs()).thenReturn(Collections.emptyList());
        //when
        List<Crypto> resultList = cryptoService.createCryptoList();
        //then
        assertThat(resultList, emptyCollectionOf(Crypto.class));
        verify(cryptoRepository).saveAll(anyList());
    }

    @Test
    public void updateCryptoPrices_success() {
        //given
        String btcSymbol = "BTC";
        String ethSymbol = "ETH";
        BigDecimal newBTCPrice = new BigDecimal(35000);
        BigDecimal newETHPrice = new BigDecimal(2500);
        //Symulacja pobrania danych przez API (reszta niepotrzebna)
        List<CryptoListRateDto> cryptoListRateDtoList = List.of(
                CryptoListRateDto.builder()
                        .data(List.of(
                                CryptoListRateDto.TickerDto.builder()
                                        .symbol(btcSymbol + "-USDT")
                                        .buy(newBTCPrice.toString())
                                        .build(),
                                CryptoListRateDto.TickerDto.builder()
                                        .symbol(ethSymbol + "-USDT")
                                        .buy(newETHPrice.toString())
                                        .build()
                        ))
                        .build());
        when(kuCoinApiClient.getAllPairs()).thenReturn(cryptoListRateDtoList);
        //Dane `istniejących` krypto z repozytorium
        Crypto btc = new Crypto(1L, btcSymbol, new BigDecimal(30000));
        Crypto eth = new Crypto(2L, ethSymbol, new BigDecimal(2000));
        List<Crypto> existingCryptos = List.of(btc, eth);
        when(cryptoRepository.findBySymbol(btcSymbol)).thenReturn(Optional.of(btc));
        when(cryptoRepository.findBySymbol(ethSymbol)).thenReturn(Optional.of(eth));
        //when
        cryptoService.updateCryptoPrices();
        //then
        verify(cryptoRepository).save(btc);
        verify(cryptoRepository).save(eth);
        assertThat(btc.getPrice(), equalTo(newBTCPrice));
        assertThat(eth.getPrice(), equalTo(newETHPrice));
    }

    @Test
    public void updateCryptoPrice_success() {
        //given
        String btcSymbol = "BTC";
        BigDecimal newBTCPrice = new BigDecimal(35000);
        //Symulacja danych z API
        List<CryptoListRateDto> cryptoListRateDtoList = List.of(
                CryptoListRateDto.builder()
                        .data(List.of(
                                CryptoListRateDto.TickerDto.builder()
                                        .symbol(btcSymbol + "-USDT")
                                        .buy(newBTCPrice.toString())
                                        .build()
                        ))
                        .build());
        when(kuCoinApiClient.getAllPairs()).thenReturn(cryptoListRateDtoList);
        //Symulacja danych `istniejących` z repozytorium
        Crypto btc = new Crypto(1L, btcSymbol, new BigDecimal(30000));
        when(cryptoRepository.findBySymbol(btcSymbol)).thenReturn(Optional.of(btc));
        //when
        cryptoService.updateCryptoPrice(btcSymbol);
        //then
        verify(cryptoRepository).save(btc);
        assertThat(btc.getPrice(), equalTo(newBTCPrice));

    }

    @Test
    public void deleteCrypto_success() {
        //given
        String symbol = "BTC";
        Crypto cryptoToDelete = new Crypto(1L, symbol, new BigDecimal(1));
        when(cryptoRepository.findBySymbol(symbol)).thenReturn(Optional.of(cryptoToDelete));
        //when
        String result = cryptoService.deleteCrypto(symbol);
        //then
        verify(cryptoRepository).deleteBySymbol(symbol);
        assertThat(result, equalTo("Crypto deleted with symbol: " + symbol));
    }

    @Test
    public void deleteCrypto_throwsCryptoNotFoundException() {
        //given
        String symbol = "BTC";
        when(cryptoRepository.findBySymbol(symbol)).thenReturn(Optional.empty());
        //when/then
        assertThrows(CryptoNotFoundException.class, () -> cryptoService.deleteCrypto(symbol));
    }
}