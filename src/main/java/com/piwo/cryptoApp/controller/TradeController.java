package com.piwo.cryptoApp.controller;

import com.itextpdf.text.DocumentException;
import com.piwo.cryptoApp.model.Trade;
import com.piwo.cryptoApp.model.dto.CryptoTradeDto;
import com.piwo.cryptoApp.model.dto.FiatTradeDto;
import com.piwo.cryptoApp.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cryptoApp/trades")
public class TradeController {
    private final TradeService tradeService;

    @GetMapping
    public List<Trade> getTrades() {
        return tradeService.getTrades();
    }


//    @GetMapping("/search")
//    public List<Trade> findTrades(@RequestParam(name = "date", required = false) String date,
//                                  @RequestParam(name = "type", required = false) String type,
//                                  @RequestParam(name = "fiatSymbol", required = false) String fiatSymbol,
//                                  @RequestParam(name = "cryptoSELL", required = false) String cryptoSELL,
//                                  @RequestParam(name = "cryptoBUY", required = false) String cryptoBUY
//    ) {
//        return tradeService.findTrades(date, type, fiatSymbol, cryptoSELL, cryptoBUY);
//    }

    @GetMapping("/search")
    public List<Trade> findTradesBySymbol(@RequestParam String symbol) {
        return tradeService.findTradesBySymbol(symbol);
    }

    @PostMapping("/transfer")
    public Trade transferFunds(@RequestBody FiatTradeDto fiatTradeDto) throws AccountNotFoundException {
        return tradeService.transferFunds(fiatTradeDto);
    }

    @PostMapping("/trade")
    public Trade tradeCrypto(@RequestBody CryptoTradeDto cryptoTradeDto) throws AccountNotFoundException {
        return tradeService.tradeCrypto(cryptoTradeDto);
    }

    @GetMapping("/download/{id}")
    public String downloadTransaction(@PathVariable Long id) throws DocumentException, FileNotFoundException {
        return tradeService.downloadTransaction(id);
    }
}
