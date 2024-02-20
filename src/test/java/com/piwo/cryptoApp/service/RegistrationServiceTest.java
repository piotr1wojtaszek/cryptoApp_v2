package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.AccountAlreadyExistsException;
import com.piwo.cryptoApp.exception.EmailAlreadyConfirmedException;
import com.piwo.cryptoApp.exception.ExpiredTokenException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Token;
import com.piwo.cryptoApp.model.dto.AccountDto;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AccountService accountService;
    @Mock
    private TokenService tokenService;
    @Mock
    private MailService mailService;
    @InjectMocks
    private RegistrationService registrationService;

    @Test
    public void signUp_success() throws AccountAlreadyExistsException, MessagingException {
        //given
        AccountDto accountDto = new AccountDto("user1", "user@email.com", "password1");

        Account createdAccount = new Account();
        createdAccount.setUsername(accountDto.getUsername());
        createdAccount.setEmail(accountDto.getEmail());
        createdAccount.setPassword(accountDto.getPassword());

        Token token = new Token();
        token.setTokenNumber("testTokenNumber");
        token.setAccount(createdAccount);

        when(accountService.createNewAccount(accountDto)).thenReturn(createdAccount);
        when(tokenService.createToken(createdAccount)).thenReturn(token);
        //when
        String result = registrationService.signUp(accountDto);
        //then
        assertThat(result, equalTo(token.getTokenNumber()));
        verify(mailService).send(eq(accountDto.getEmail()), anyString());
    }

    @Test
    public void signUp_throwsAccountAlreadyExistsException() throws AccountAlreadyExistsException, MessagingException {
        //given
        AccountDto accountDto = new AccountDto("user1", "user@email.com", "password1");
        when(accountService.createNewAccount(accountDto))
                .thenThrow(new AccountAlreadyExistsException(accountDto.getEmail()));
        //when/then
        assertThrows(AccountAlreadyExistsException.class, () -> registrationService.signUp(accountDto));
        verify(tokenService, never()).createToken(any());
        verify(mailService, never()).send(any(), any());
    }

    @Test
    public void confirmRegistration_success() throws ExpiredTokenException, EmailAlreadyConfirmedException {
        //given
        String tokenNumber = "testTokenNumber";

        Account account = new Account();
        account.setEmail("user1@example.com");

        Token token = new Token();
        token.setExpiredAt(LocalDateTime.now().plusHours(1));
        token.setTokenNumber(tokenNumber);
        token.setAccount(account);

        when(tokenService.getToken(tokenNumber)).thenReturn(token);
        //when
        String result = registrationService.confirmRegistration(tokenNumber);
        //then
        assertThat(result, equalTo("Registration confirmed."));
        verify(tokenService).setConfirmedAt(tokenNumber);
        verify(accountService).enableAccount(account.getEmail());
    }

    @Test
    public void confirmRegistration_expiredToken_throwsExpiredTokenException() throws MessagingException {
        //given
        String tokenNumber = "testTokenNumber";
        Account account = new Account();
        account.setEmail("user1@example.com");

        Token token = new Token();
        token.setExpiredAt(LocalDateTime.now().minusMinutes(1));
        token.setTokenNumber(tokenNumber);
        token.setAccount(account);

        when(tokenService.getToken(tokenNumber)).thenReturn(token);
        //when/then
        assertThrows(ExpiredTokenException.class, () -> registrationService.confirmRegistration(tokenNumber));
        verify(tokenService, never()).createToken(any());
        verify(mailService, never()).send(any(), any());
    }

    @Test
    public void confirmRegistration_confirmedToken_throwsEmailAlreadyConfirmedException() throws MessagingException {
        //given
        String tokenNumber = "testTokenNumber";
        Account account = new Account();
        account.setEmail("user1@example.com");

        Token token = new Token();
        token.setExpiredAt(LocalDateTime.now().minusMinutes(1));
        token.setTokenNumber(tokenNumber);
        token.setAccount(account);
        token.setConfirmedAt(LocalDateTime.now());

        when(tokenService.getToken(tokenNumber)).thenReturn(token);
        //when/then
        assertThrows(EmailAlreadyConfirmedException.class, () -> registrationService.confirmRegistration(tokenNumber));
        verify(tokenService, never()).createToken(any());
        verify(mailService, never()).send(any(), any());
    }

}