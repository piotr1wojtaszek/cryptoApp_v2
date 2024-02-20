package com.piwo.cryptoApp.repository;

import com.piwo.cryptoApp.model.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CryptoRepository extends JpaRepository<Crypto, Long> {
    Optional<Crypto> findBySymbol(String symbol);

    void deleteBySymbol(String symbol);
}
