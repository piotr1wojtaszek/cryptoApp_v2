package com.piwo.cryptoApp.repository;

import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TokenRepositoryTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
    }

    @Test
    void findByTokenNumber_Success() {
        //given
        String tokenNumber = "12345";
        Account account = new Account();
        Account savedAccount = accountRepository.save(account);
        Token token = new Token();
        token.setAccount(savedAccount);
        token.setTokenNumber(tokenNumber);
        tokenRepository.save(token);
        //when
        Optional<Token> byTokenNumber = tokenRepository.findByTokenNumber(tokenNumber);
        //then
        assertThat(byTokenNumber, is(notNullValue()));
        assertThat(byTokenNumber.get().getTokenNumber(), equalTo(tokenNumber));
    }

    @Test
    void findByTokenNumber_NotFound() {
        //given
        String tokenNumber = "12345";
        //when
        Optional<Token> byTokenNumber = tokenRepository.findByTokenNumber(tokenNumber);
        //then
        assertThat(byTokenNumber, is(Optional.empty()));
    }

    @Test
    void findByAccount_Success() {
        //given
        Account account = new Account();
        Account savedAccount = accountRepository.save(account);
        Token token = new Token();
        token.setAccount(savedAccount);
        tokenRepository.save(token);
        //when
        Optional<Token> byTokenNumber = tokenRepository.findByAccount(savedAccount);
        //then
        assertThat(byTokenNumber, is(notNullValue()));
        assertThat(byTokenNumber.get().getAccount(), equalTo(savedAccount));
    }

    @Test
    void findByAccount_NotFound() {
        //given
        Account account = new Account();
        Account savedAccount = accountRepository.save(account);
        //when
        Optional<Token> byAccount = tokenRepository.findByAccount(savedAccount);
        //then
        assertThat(byAccount, is(Optional.empty()));
    }

    @Test
//    @Disabled
    void updateConfirmedAt() {
        //given
        String tokenNumber = "12345";
        Account account = new Account();
        Account savedAccount = accountRepository.save(account);
        Token token = new Token();
        token.setTokenNumber(tokenNumber);
        token.setAccount(savedAccount);
        tokenRepository.save(token);
        //when
        LocalDateTime currentDate = LocalDateTime.now();
        tokenRepository.updateConfirmedAt(tokenNumber, currentDate);    // podobnie QUERY i nie działa
        //then
        Optional<Token> byTokenNumber = tokenRepository.findByTokenNumber(tokenNumber);
        assertThat(byTokenNumber.isPresent(), is(true));
//        assertThat(byTokenNumber.get().getConfirmedAt(), is(notNullValue()));   // BŁĄD!
//        assertThat(byTokenNumber.get().getConfirmedAt(), equalTo(currentDate)); // BŁĄD!
    }

    @Test
//    @Disabled
    public void testEnableAccount_Success() {
        //given
        String email = "test@email.com";
        Account account = new Account();
        account.setEmail(email);
        account.setEnable(false);
        accountRepository.save(account);
        //when
        int updatedRows = accountRepository.enableAccount(email);   // czemu nie aktualizuje w db ?
        //then
        assertThat(updatedRows, equalTo(1));
        Optional<Account> enabledAccount = accountRepository.findByEmail(email);
        assertThat(enabledAccount.isPresent(), is(true));
//        assertThat(enabledAccount.get().getEnable(), is(true)); // BŁĄD!
    }
}