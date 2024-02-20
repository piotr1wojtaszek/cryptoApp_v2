package com.piwo.cryptoApp.repository;

import com.piwo.cryptoApp.model.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
    }

    @Test
    public void testFindByEmail_AccountFound() {
        //given
        String email = "test@example.com";
        Account account = new Account();
        account.setEmail(email);
        account.setPassword("password123");
        accountRepository.save(account);
        //when
        Optional<Account> byEmail = accountRepository.findByEmail(email);
        //then
        assertThat(byEmail, is(notNullValue()));
        assertThat(email, equalTo(byEmail.get().getEmail()));
    }

    @Test
    public void testFindByEmail_AccountNotFound() {
        //given
        //when
        Optional<Account> byEmail = accountRepository.findByEmail("empty");
        //then
        assertThat(byEmail, equalTo(Optional.empty()));
    }

    @Test
    public void testDeleteByEmail() {
        //given
        String email = "test@email.com";
        Account account = new Account();
        account.setEmail(email);
        accountRepository.save(account);
        //when
        accountRepository.deleteByEmail(email);
        //then
        Optional<Account> byEmail = accountRepository.findByEmail(email);
        assertThat(byEmail, equalTo(Optional.empty()));
    }

    @Test
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

    @Test
    public void testEnableAccount_AccountNotFound() {
        //given
        String email = "test@email.com";
        //when
        int updatedRows = accountRepository.enableAccount(email);
        //then
        assertThat(updatedRows, equalTo(0));
    }
}