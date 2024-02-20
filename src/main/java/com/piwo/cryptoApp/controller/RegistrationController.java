package com.piwo.cryptoApp.controller;

import com.piwo.cryptoApp.exception.AccountAlreadyExistsException;
import com.piwo.cryptoApp.exception.EmailAlreadyConfirmedException;
import com.piwo.cryptoApp.exception.ExpiredTokenException;
import com.piwo.cryptoApp.model.dto.AccountDto;
import com.piwo.cryptoApp.service.RegistrationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cryptoApp/registration")
public class RegistrationController {
    private final RegistrationService registrationService;

    @PostMapping
    public String register(@RequestBody AccountDto accountDto) throws AccountAlreadyExistsException, MessagingException {
        return registrationService.signUp(accountDto);
    }

    @GetMapping("/confirm")
    public String confirm(@RequestParam("tokenNumber") String tokenNumber) throws ExpiredTokenException, EmailAlreadyConfirmedException {
        return registrationService.confirmRegistration(tokenNumber);
    }
}
