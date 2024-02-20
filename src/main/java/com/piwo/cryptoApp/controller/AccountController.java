package com.piwo.cryptoApp.controller;

import com.piwo.cryptoApp.exception.AccountAlreadyExistsException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.dto.AccountDto;
import com.piwo.cryptoApp.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cryptoApp/account")
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    public List<Account> getAllUsers() {
        return accountService.getAllUsers();
    }

    //    @ExceptionHandler(AccountNotFoundException.class)
    @GetMapping("/{email}")
    public Account getAccountByEmail(@RequestParam String email) throws AccountNotFoundException {
        System.out.println("Trying to get account by email: {} " + email);
        return accountService.getAccountByEmail(email);
    }

    @GetMapping("/{accountId}")
    public Account getAccountById(@PathVariable Long accountId) throws AccountNotFoundException {
        return accountService.getAccountById(accountId);
    }

    @PostMapping
    public Account newAccount(@RequestBody AccountDto accountDto) throws AccountAlreadyExistsException {
        return accountService.createNewAccount(accountDto);
    }

    @DeleteMapping("/delete")
    public String deleteAccount(@RequestParam String email) throws AccountNotFoundException {
        return accountService.deleteAccount(email);
    }
}
