package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.TokenNotFoundException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Token;
import com.piwo.cryptoApp.repository.TokenRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.security.auth.login.AccountNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @Test
    public void createToken_returnsNewToken() {
        //given
        Account account = new Account();
        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        when(tokenRepository.save(tokenCaptor.capture())).thenReturn(null);
        //when
        tokenService.createToken(account);
        //then
        Token capturedToken = tokenCaptor.getValue();
        assertThat(capturedToken.getTokenNumber(), notNullValue());
        assertThat(capturedToken.getCreatedAt(), notNullValue());
        assertThat(capturedToken.getExpiredAt(), notNullValue());
        assertThat(capturedToken.getAccount(), is(equalTo(account)));
    }

    @Test
    public void getToken_byTokenNumber_returnsToken() {
        //given
        String tokenNumber = "random_token_number";
        Token expectedToken = Token.builder()
                .tokenNumber(tokenNumber)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .account(new Account())
                .build();
        when(tokenRepository.findByTokenNumber(tokenNumber)).thenReturn(Optional.of(expectedToken));
        //when
        Token result = tokenService.getToken(tokenNumber);
        //then
        assertThat(result, equalTo(expectedToken));
    }

    @Test
    public void getToken_byTokenNumber_throwsTokenNotFoundException() {
        //given
        String tokenNumber = "random_token_number";
        //when/then
        assertThrows(TokenNotFoundException.class, () -> tokenService.getToken(tokenNumber));
    }

    @Test
    public void getToken_byAccount_returnsToken() throws AccountNotFoundException {
        //given
        Account account = new Account();
        String tokenNumber = "random_token_number";
        Token expectedToken = Token.builder()
                .tokenNumber(tokenNumber)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .account(account)
                .build();
        when(tokenRepository.findByAccount(account)).thenReturn(Optional.of(expectedToken));
        //when
        Token result = tokenService.getToken(account);
        //then
        assertThat(result, equalTo(expectedToken));
    }

    @Test
    public void getToken_byAccount_throwsAccountNotFoundException()  {
        //given
        Account account = new Account();
        //when/then
        assertThrows(AccountNotFoundException.class, () -> tokenService.getToken(account));
    }

    @Test
    public void deleteToken_success() {
        //given
        long tokenId = 1L;
        //when
        String result = tokenService.deleteToken(tokenId);
        //then
        verify(tokenRepository).deleteById(tokenId);
        assertThat(result, equalTo("token deleted."));
    }

    @Test
    @Disabled
    public void setConfirmedAt_success() {
        //given
        String tokenNumber = "random_token_number";
        LocalDateTime confirmedAt = LocalDateTime.now();
        //when
        int result = tokenService.setConfirmedAt(tokenNumber);
        //then
        verify(tokenRepository).updateConfirmedAt(eq(tokenNumber), eq(confirmedAt));
        assertEquals(1, result);
    }
}