package com.piwo.cryptoApp;

import com.piwo.cryptoApp.exchangeApi.KuCoinApiClient;
import com.piwo.cryptoApp.repository.CryptoRepository;
import com.piwo.cryptoApp.service.CryptoService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CryptoAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(CryptoAppApplication.class, args);
    }

}
