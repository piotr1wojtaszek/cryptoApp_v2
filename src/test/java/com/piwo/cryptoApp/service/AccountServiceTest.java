package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.AccountAlreadyExistsException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Wallet;
import com.piwo.cryptoApp.model.dto.AccountDto;
import com.piwo.cryptoApp.repository.AccountRepository;
import com.piwo.cryptoApp.repository.WalletRepository;
import com.piwo.cryptoApp.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AccountService accountService;

    @Test
    public void getAllUsers_ReturnListOfAccounts() {
        //given
        List<Account> accounts = List.of(new Account(), new Account());
        when(accountRepository.findAll()).thenReturn(accounts);
        //when
        List<Account> resultList = accountService.getAllUsers();
        //then
        assertThat(resultList.size(), equalTo(2));
        assertThat(resultList, equalTo(accounts));
    }

    @Test
    public void getAllUsers_ReturnEmptyList() {
        //given
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());
        //when
        List<Account> resultList = accountService.getAllUsers();
        //then
        assertThat(resultList, empty());
    }

    @Test
    public void getAccountById_ReturnsAccount() throws AccountNotFoundException {
        //given
        Long accountId = 1L;
        Account account = new Account("user1", "user1@example.com", "password");
        account.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        //when
        Account result = accountService.getAccountById(accountId);
        //then
        assertThat(result, notNullValue());
        assertThat(result, equalTo(account));
    }

    @Test
    public void getAccountById_throwsAccountNotFoundException() {
        //given
        Long accountId = 100L;
        //when,then
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(accountId));
    }

    @Test
    public void getAccountByEmail_returnsAccount() throws AccountNotFoundException {
        //given
        String email = "test@example.com";
        Account account = new Account();
        account.setEmail(email);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        //when
        Account result = accountService.getAccountByEmail(email);
        //then
        assertThat(result, notNullValue());
        assertThat(result, equalTo(account));
    }

    @Test
    public void getAccountByEmail_throwsAccountNotFoundException() {
        //when
        String email = "notfound@email.com";
        //when/then
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountByEmail(email));
    }

    @Test
    public void createNewAccount_CraatesAndSavesAccountWithWallet() throws AccountAlreadyExistsException {
        //when
        String username = "user1";
        String email = "user@example.com";
        String password = "password";
        AccountDto accountDto = new AccountDto(username, email, password);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        Wallet wallet = new Wallet();
        when(walletRepository.save(any())).thenReturn(wallet);      // jak to zaimplementować ?
        // Zwróci ten sam account który próbuje zapisać metoda void
        doAnswer(invocation -> invocation.getArgument(0)).when(accountRepository).save(any());
        //when
        Account result = accountService.createNewAccount(accountDto);
        //then
        verify(accountRepository, times(2)).save(any());
        verify(walletRepository, times(1)).save(any());
        assertThat(result.getUsername(), equalTo(username));
        assertThat(result.getEmail(), equalTo(email));
        assertThat(result.getPassword(), equalTo("encodedPassword"));
        assertThat(result.getWallet(), equalTo(wallet));
    }

    @Test
    public void createNewAccount_throwsAccountAlreadyExistException() {
        //given
        String username = "user1";
        String email = "user@example.com";
        String password = "password";
        AccountDto accountDto = new AccountDto(username, email, password);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(new Account()));
        //when/then
        assertThrows(AccountAlreadyExistsException.class, () -> accountService.createNewAccount(accountDto));
    }

    @Test
    public void deleteAccount_succeed() throws AccountNotFoundException {
        //given
        String email = "user@example.com";
        Account account = new Account();
        account.setEmail(email);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        //when
        String resultText = accountService.deleteAccount(email);
        //then
        verify(accountRepository, times(1)).deleteByEmail(email);
        assertThat(resultText, equalTo("Account deleted."));
    }

    @Test
    public void deletedAccount_throwsAccountNotFoundException() {
        //given
        String email = "notFound@email.com";
        //when/then
        assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccount(email));
    }

    /* ta metoda jest sprawdzana jedynie na wywołanie bo ma tylko jeden możliwy scenariusz dla konkretnego emaila */
    @Test
    public void enableAccount_succeed() {
        //given
        String email = "user@example.com";
        //when
        accountService.enableAccount(email);
        //then
        verify(accountRepository, times(1)).enableAccount(email);
    }

    @Test
    public void getAuthenticatedAccount_returnsAuthenticatedAccount() throws AccountNotFoundException {
        //given
        Account authenticatedAccount = new Account("user1", "user1@example.com", "password1");
        authenticatedAccount.setId(1L);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(authenticatedAccount);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(accountRepository.findByEmail(authenticatedAccount.getEmail()))
                .thenReturn(Optional.of(authenticatedAccount));
        //when
        Account result = accountService.getAuthenticatedAccount();
        //then
        assertThat(result, equalTo(authenticatedAccount));
    }

    @Test
    public void getAuthenticatedAccount_throwsAccountNotFoundException() {
        //given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //when/then
        assertThrows(AccountNotFoundException.class, () -> accountService.getAuthenticatedAccount());
    }

    @Test
    public void saveAccount_succeed() {
        //given
        Account account = new Account("user1", "user1@example.com", "password1");
        account.setId(1L);
        //when
        accountService.saveAccount(account);
        //then
        verify(accountRepository, times(1)).save(account);
    }
}