package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.TokenNotFoundException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Token;
import com.piwo.cryptoApp.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;

    /**
     * Creates token to confirm users registration
     * @param account users account
     * @return returns new token
     */
    public Token createToken(Account account) {
        Token token = Token.builder()
                .tokenNumber(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .account(account)
                .build();
        return tokenRepository.save(token);
    }

    /**
     * Returns token from database by its number.
     * @param tokenNumber number of token.
     * @return returns token from database.
     * @throws TokenNotFoundException if the token does not exist.
     */
    public Token getToken(String tokenNumber) throws TokenNotFoundException {
        return tokenRepository.findByTokenNumber(tokenNumber).orElseThrow(() -> new  TokenNotFoundException(tokenNumber));
    }

    /**
     * returns a token from the database based on the user account related to it.
     * @param account users account.
     * @return token.
     * @throws AccountNotFoundException if account does not exist.
     */
    public Token getToken(Account account) throws AccountNotFoundException {
        return tokenRepository.findByAccount(account).orElseThrow(AccountNotFoundException::new);
    }

    /**only for test purposes
     * Deletes token from database by id.
     * @param tokenId token id.
     * @return message about successful token removal.
     */
    //only for test purposes
    public String deleteToken(Long tokenId) {
        tokenRepository.deleteById(tokenId);
        return "token deleted.";
    }

    /**
     * Sets date of users mail confirmation.
     * @param tokenNumber token number
     * @return sets date of confirmation in users data.
     */
    public int setConfirmedAt(String tokenNumber) {
        return tokenRepository.updateConfirmedAt(tokenNumber, LocalDateTime.now());
    }
}
