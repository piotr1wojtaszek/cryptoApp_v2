package com.piwo.cryptoApp.controller;

import com.piwo.cryptoApp.model.Crypto;
import com.piwo.cryptoApp.service.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cryptoApp/crypto")
public class CryptoController {
    private final CryptoService cryptoService;

    @GetMapping("/createList")
    public List<Crypto> createCryptoList() {
        return cryptoService.createCryptoList();
    }

    @DeleteMapping("/delete")
    public String deleteCrypto(@RequestParam String symbol){
        return cryptoService.deleteCrypto(symbol);
    }
}
