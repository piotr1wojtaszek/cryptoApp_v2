package com.piwo.cryptoApp.repository;

import com.piwo.cryptoApp.model.Crypto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CryptoRepositoryTest {

    @Autowired
    private CryptoRepository cryptoRepository;


    @AfterEach
    void tearDown() {
        cryptoRepository.deleteAll();
    }

    @Test
    void findBySymbol_Success() {
        //given
        String symbol = "SYM";
        Crypto crypto = new Crypto();
        crypto.setSymbol(symbol);
        cryptoRepository.save(crypto);
        //when
        Optional<Crypto> bySymbol = cryptoRepository.findBySymbol(symbol);
        //then
        assertThat(bySymbol, not(nullValue()));
        assertThat(bySymbol.get().getSymbol(), equalTo(symbol));

    }

    @Test
    void findBySymbol_NotFound() {
        //given
        String symbol = "SYM";
        //when
        Optional<Crypto> bySymbol = cryptoRepository.findBySymbol(symbol);
        //then
        assertThat(bySymbol,equalTo(Optional.empty()));
    }

    @Test
    void deleteBySymbol() {
        //given
        String symbol = "SYM";
        Crypto crypto = new Crypto();
        crypto.setSymbol(symbol);
        cryptoRepository.save(crypto);
        //when
        cryptoRepository.deleteBySymbol(symbol);
        //then
        Optional<Crypto> bySymbol = cryptoRepository.findBySymbol(symbol);
        assertThat(bySymbol,equalTo(Optional.empty()));
    }
}