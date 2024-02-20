package com.piwo.cryptoApp.repository;

import com.piwo.cryptoApp.model.Crypto;
import com.piwo.cryptoApp.model.WalletCrypto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletCryptoRepository extends JpaRepository<WalletCrypto, Long> {

    Optional<WalletCrypto> findByCrypto(Crypto crypto);
}
