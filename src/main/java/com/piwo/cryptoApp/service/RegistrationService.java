package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.AccountAlreadyExistsException;
import com.piwo.cryptoApp.exception.EmailAlreadyConfirmedException;
import com.piwo.cryptoApp.exception.ExpiredTokenException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Token;
import com.piwo.cryptoApp.model.dto.AccountDto;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final AccountService accountService;
    private final TokenService tokenService;
    private final MailService mailService;
    private final TemplateEngine templateEngine;

    @Value("${app.host}")
    private String host;
    @Value("${app.registration.endpoint}")
    private String registrationEndpoint;

    /**
     * Registers new Account user based on the submitted data, and then sends the created token to the email
     * so that the user can complete the registration.
     * @param accountDto submitted data.
     * @return token to finish registration
     * @throws AccountAlreadyExistsException if Account was already finished registration process.
     */
    @Transactional
    public String signUp(AccountDto accountDto) throws AccountAlreadyExistsException {
        Account account = accountService.createNewAccount(accountDto);
        Token token = tokenService.createToken(account);
        String tokenNumber = token.getTokenNumber();
        final String link = host + registrationEndpoint + "?tokenNumber=" + tokenNumber;
        mailService.send(account.getEmail(), buildEmail(account.getUsername(), link));
        return tokenNumber;
    }

    /**
     * Confirms registration by the user and enables the user to use the application.
     * @param tokenNumber tToken sent to the user's email
     * @return Message about successful registration.
     * @throws ExpiredTokenException when the token expires.
     * @throws EmailAlreadyConfirmedException when the user has already confirmed the registration via email.
     */
    public String confirmRegistration(String tokenNumber) throws ExpiredTokenException, EmailAlreadyConfirmedException {
        Token token = tokenService.getToken(tokenNumber);
        checkTokenConfirmation(token);
        checkTokenExpiration(token);
        tokenService.setConfirmedAt(tokenNumber);
        accountService.enableAccount(token.getAccount().getEmail());
        return "Registration confirmed.";
    }

    /**
     * Checks token expiration time.
     * @param token token sent to users email.
     * @throws ExpiredTokenException when token has expired.
     */
    private void checkTokenExpiration(Token token) throws ExpiredTokenException {
        LocalDateTime expiredAt = token.getExpiredAt();
        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new ExpiredTokenException();
        }
    }

    /**
     * Checks whether the token has been confirmed.
     * @param token token sent to users email.
     * @throws EmailAlreadyConfirmedException when the email with the token has already been confirmed.
     */
    private void checkTokenConfirmation(Token token) throws EmailAlreadyConfirmedException {
        if (token.getConfirmedAt() != null) {
            throw new EmailAlreadyConfirmedException();
        }
    }

    /**
     * Creates an email with a token in the message.
     * @param name users name;
     * @param link link with registry confirmation.
     * @return email to send.
     */
    public String buildEmail(String name, String link) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", link);
        return templateEngine.process("email-template", context);
    }
}


