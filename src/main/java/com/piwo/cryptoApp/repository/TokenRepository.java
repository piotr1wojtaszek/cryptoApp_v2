package com.piwo.cryptoApp.repository;

import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenNumber(String tokenNumber);

    Optional<Token> findByAccount(Account account);

    @Transactional
    @Modifying
    @Query("UPDATE Token t " +
            "SET t.confirmedAt = ?2 " +
            "WHERE t.tokenNumber =?1")
    int updateConfirmedAt(String tokenNumber, LocalDateTime confirmedAt);
}
