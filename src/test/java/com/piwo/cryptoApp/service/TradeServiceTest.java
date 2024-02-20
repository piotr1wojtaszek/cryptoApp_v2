package com.piwo.cryptoApp.service;

import com.itextpdf.text.DocumentException;
import com.piwo.cryptoApp.exception.*;
import com.piwo.cryptoApp.model.*;
import com.piwo.cryptoApp.model.dto.CryptoTradeDto;
import com.piwo.cryptoApp.model.dto.FiatTradeDto;
import com.piwo.cryptoApp.model.enums.TradeType;
import com.piwo.cryptoApp.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private AccountService accountService;
    @Mock
    private TradeRepository tradeRepository;
    @Mock
    private CryptoService cryptoService;
    @Mock
    private WalletService walletService;
    @Mock
    private WalletCryptoService walletCryptoService;
    @InjectMocks
    private TradeService tradeService;

    @Test
    public void getTrades_returnsListOfTrades() {
        //given
        Trade trade1 = new Trade();
        Trade trade2 = new Trade();
        Trade trade3 = new Trade();
        List<Trade> tradeList = List.of(trade1, trade2, trade3);
        when(tradeRepository.findAll()).thenReturn(tradeList);
        //when
        List<Trade> result = tradeService.getTrades();
        //then
        assertThat(result, equalTo(tradeList));
    }

    @Test
    public void findTradesBySymbol_returnsListOfTrades() {
        //given
        String symbol = "DO";
        Trade trade1 = new Trade();
        trade1.setCryptoToTradeSymbol("ADA");
        Trade trade2 = new Trade();
        trade2.setCryptoToTradeSymbol("DOGE");
        Trade trade3 = new Trade();
        trade3.setCryptoToTradeSymbol("DOT");
        List<Trade> tradeList = List.of(trade2, trade3);
        when(tradeRepository.findAll()).thenReturn(tradeList);
        //when
        List<Trade> result = tradeService.findTradesBySymbol(symbol);
        //then
        assertThat(result.size(), equalTo(2));
        assertThat(result, equalTo(tradeList));
    }

    @Test   //sprawdź tą metodę po zmianach
    public void buyCrypto_createsTradeAndUpdatesWallet() throws AccountNotFoundException {
        //given
        Account authenticatedAccount = new Account();
        Long accountId = 1L;
        authenticatedAccount.setId(accountId);
        Wallet wallet = new Wallet();
        authenticatedAccount.setWallet(wallet);

        String cryptoToBuySymbol = "BTC";
        String cryptoToSellSymbol = "USDT";
        BigDecimal amount = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("20000.00");
        CryptoTradeDto cryptoTradeDto =
                new CryptoTradeDto(TradeType.BUY, cryptoToBuySymbol, cryptoToSellSymbol, amount, unitPrice);

        BigDecimal fee = new BigDecimal("0.02");
        BigDecimal totalValue = new BigDecimal("9800.00");

        Trade trade = new Trade(LocalDateTime.now(), TradeType.BUY, accountId, cryptoToBuySymbol, cryptoToSellSymbol, amount, unitPrice, fee, totalValue);
//        trade.setId(1L);
        Crypto cryptoBTC = new Crypto(1L, cryptoToBuySymbol, unitPrice.subtract(BigDecimal.ONE));
        Crypto cryptoUSDT = new Crypto(2L, cryptoToSellSymbol, new BigDecimal(1));
        WalletCrypto walletCrypto1 = new WalletCrypto(1L, cryptoUSDT, new BigDecimal("15000.00"), new BigDecimal("15000.00"));
        List<WalletCrypto> walletCryptoList = new ArrayList<>(List.of(walletCrypto1));
        wallet.setBalance(walletCrypto1.getValue());
        wallet.setWalletCryptoList(walletCryptoList);
        List<Trade> transactionHistory = new ArrayList<>();
        wallet.setTransactionHistory(transactionHistory);
        WalletCrypto walletCrypto2 = new WalletCrypto(1L, cryptoBTC, new BigDecimal("0.5"), new BigDecimal("10000.00"));
        walletCryptoList.add(walletCrypto2);
        wallet.setBalance((walletCrypto1.getValue().add(walletCrypto2.getValue())));

        when(accountService.getAuthenticatedAccount()).thenReturn(authenticatedAccount);
        when(cryptoService.getCrypto(cryptoToBuySymbol)).thenReturn(cryptoBTC);
        when(cryptoService.getCrypto(cryptoToSellSymbol)).thenReturn(cryptoUSDT);
//        when(tradeRepository.save(trade)).thenReturn(trade);
        doReturn(trade).when(tradeRepository).save(any(Trade.class));
        //when
//        Trade result = tradeService.buyCrypto(cryptoTradeDto);
        Trade result = tradeService.tradeCrypto(cryptoTradeDto);
        //then
        assertThat(result, equalTo(trade));
    }

    @Test   //sprawdź tą metodę po zmianach
    public void buyCrypto_throwsInvalidPriceException() {
        //given
        String cryptoToBuySymbol = "BTC";
        String cryptoToSellSymbol = "USDT";
        BigDecimal amount = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("20000.00");
        CryptoTradeDto cryptoTradeDto =
                new CryptoTradeDto(TradeType.BUY, cryptoToBuySymbol, cryptoToSellSymbol, amount, unitPrice);
        Crypto cryptoBTC = new Crypto(1L, cryptoToBuySymbol, unitPrice);
        Crypto cryptoUSDT = new Crypto(2L, cryptoToSellSymbol, new BigDecimal(1));

        when(cryptoService.getCrypto(cryptoToBuySymbol)).thenReturn(cryptoBTC);
        when(cryptoService.getCrypto(cryptoToSellSymbol)).thenReturn(cryptoUSDT);
        //when/then
//        assertThrows(InvalidPriceException.class, () -> tradeService.buyCrypto(cryptoTradeDto));
        assertThrows(InvalidPriceException.class, () -> tradeService.tradeCrypto(cryptoTradeDto));
    }

    @Test   //sprawdź tą metodę po zmianach
    public void buyCrypto_throwsAccountNotFoundException() throws AccountNotFoundException {
        //given
        String cryptoToBuySymbol = "BTC";
        String cryptoToSellSymbol = "USDT";
        BigDecimal amount = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("20000.00");
        CryptoTradeDto cryptoTradeDto =
                new CryptoTradeDto(TradeType.BUY, cryptoToBuySymbol, cryptoToSellSymbol, amount, unitPrice);
        when(accountService.getAuthenticatedAccount()).thenThrow(AccountNotFoundException.class);
        //when/then
//        assertThrows(AccountNotFoundException.class, () -> tradeService.buyCrypto(cryptoTradeDto));
        assertThrows(AccountNotFoundException.class, () -> tradeService.tradeCrypto(cryptoTradeDto));
    }

    @Test   //sprawdź tą metodę po zmianach
    public void buyCrypto_throwsCryptoNotFoundException() {
        //given
        String cryptoToBuySymbol = "BTC";
        String cryptoToSellSymbol = "USDT";
        BigDecimal amount = new BigDecimal("0.5");
        BigDecimal unitPrice = new BigDecimal("20000.00");
        CryptoTradeDto cryptoTradeDto =
                new CryptoTradeDto(TradeType.BUY, cryptoToBuySymbol, cryptoToSellSymbol, amount, unitPrice);
        //when/then
//        assertThrows(CryptoNotFoundException.class, () -> tradeService.buyCrypto(cryptoTradeDto));
        assertThrows(CryptoNotFoundException.class, () -> tradeService.tradeCrypto(cryptoTradeDto));
    }

    @Test
    public void buyCrypto_createsTradeAndNewCryptoInWallet() {
    }

    @Test   //sprawdź tą metodę po zmianach
    public void sellCrypto() throws AccountNotFoundException {
        //given
        Account authenticatedAccount = new Account();
        Long accountId = 1L;
        authenticatedAccount.setId(accountId);

        String cryptoToBuySymbol = "USDT";
        String cryptoToSellSymbol = "BTC";
        BigDecimal amount = new BigDecimal("0.1");          // czyli 2k
        BigDecimal unitPrice = new BigDecimal("20000.00");
        BigDecimal currentPrice = new BigDecimal("20005.00");
        CryptoTradeDto cryptoTradeDto =
                new CryptoTradeDto(TradeType.BUY, cryptoToBuySymbol, cryptoToSellSymbol, amount, unitPrice);

        BigDecimal fee = new BigDecimal("0.02");
        BigDecimal totalValue = new BigDecimal("2000.00");
        Trade trade = new Trade(LocalDateTime.now(), TradeType.SELL, accountId, cryptoToSellSymbol, cryptoToSellSymbol, amount, unitPrice, fee, totalValue);

        Crypto cryptoBTC = new Crypto(1L, cryptoToSellSymbol, currentPrice);
        Crypto cryptoUSDT = new Crypto(2L, cryptoToBuySymbol, BigDecimal.ONE);

        List<WalletCrypto> walletCryptoList = new ArrayList<>();
        WalletCrypto walletCryptoToSell = new WalletCrypto(1L, cryptoBTC, new BigDecimal("0.85"), currentPrice);
        WalletCrypto walletCryptoToBuy = new WalletCrypto(2L, cryptoUSDT, new BigDecimal("750.45"), new BigDecimal("750.45"));
        walletCryptoList.add(walletCryptoToSell);
        walletCryptoList.add(walletCryptoToBuy);

        List<Trade> transactionHistory = new ArrayList<>();

        Wallet wallet = new Wallet();
        wallet.setWalletCryptoList(walletCryptoList);
        wallet.setTransactionHistory(transactionHistory);

        authenticatedAccount.setWallet(wallet);
        when(accountService.getAuthenticatedAccount()).thenReturn(authenticatedAccount);
        when(cryptoService.getCrypto(cryptoToSellSymbol)).thenReturn(cryptoBTC);
        when(cryptoService.getCrypto(cryptoToBuySymbol)).thenReturn(cryptoUSDT);
//        when(tradeRepository.save(trade)).thenReturn(trade);
        doReturn(trade).when(tradeRepository).save(any(Trade.class));

        //when
//        Trade result = tradeService.sellCrypto(cryptoTradeDto);
        Trade result = tradeService.tradeCrypto(cryptoTradeDto);
        //then
        assertThat(result, equalTo(trade));
    }


    @Test       //sprawdź tą metodę po zmianach
    public void deposit_createsFiatTrade() throws AccountNotFoundException {
        //given
        Wallet wallet = new Wallet();
        List<Trade> tradeHistory = new ArrayList<>();
        List<WalletCrypto> walletCryptoList = new ArrayList<>();
        wallet.setTransactionHistory(tradeHistory);
        wallet.setWalletCryptoList(walletCryptoList);

        Account authenticatedAccount = new Account();
        Long accountId = 1L;
        authenticatedAccount.setId(accountId);
        authenticatedAccount.setWallet(wallet);

        BigDecimal amount = new BigDecimal("1000.00");
        String fiatSymbol = "USD";
        TradeType tradeType = TradeType.DEPOSIT;
        FiatTradeDto fiatTradeDto = new FiatTradeDto(tradeType, fiatSymbol, amount);
        BigDecimal fee = new BigDecimal("20.00");

        BigDecimal price = new BigDecimal("0.98");
        Crypto crypto = new Crypto(1L, "USDT", price);

        when(accountService.getAuthenticatedAccount()).thenReturn(authenticatedAccount);
        when(cryptoService.getCrypto("USDT")).thenReturn(crypto);
        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalValue = new BigDecimal("960.40");
        BigDecimal total = amount;
        amount = new BigDecimal("980.00");
        Trade trade = new Trade(now, TradeType.DEPOSIT, accountId, fiatSymbol, amount, fee, price, totalValue, total);
        trade.setId(1L);
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);
        //when
//        Trade result = tradeService.deposit(fiatTradeDto);
        Trade result = tradeService.transferFunds(fiatTradeDto);
        //then
        assertThat(result, equalTo(trade));
    }


    @Test   //sprawdź tą metodę po zmianach
    public void deposit_throwsAccountNotFoundException() throws AccountNotFoundException {
        //given
        BigDecimal amount = new BigDecimal("1000.00");
        String fiatSymbol = "USD";
        TradeType tradeType = TradeType.DEPOSIT;
        FiatTradeDto fiatTradeDto = new FiatTradeDto(tradeType, fiatSymbol, amount);
        when(accountService.getAuthenticatedAccount()).thenThrow(AccountNotFoundException.class);
        //when/then
//        assertThrows(AccountNotFoundException.class, () -> tradeService.deposit(fiatTradeDto));
        assertThrows(AccountNotFoundException.class, () -> tradeService.transferFunds(fiatTradeDto));
    }

    @Test   //sprawdź tą metodę po zmianach
    public void deposit_throwsDepositFailedException() throws AccountNotFoundException {
        //given
        Wallet wallet = new Wallet();
        wallet.setTransactionHistory(new ArrayList<>());

        Account authenticatedAccount = new Account();
        Long accountId = 1L;
        authenticatedAccount.setId(accountId);
        authenticatedAccount.setWallet(wallet);

        String fakeFiatSymbol = "fake_symbol";
        FiatTradeDto fiatTradeDto = new FiatTradeDto(TradeType.DEPOSIT, fakeFiatSymbol, new BigDecimal(100));
        Crypto crypto = new Crypto(1L, "cryptoSymbol", new BigDecimal(1));

        when(accountService.getAuthenticatedAccount()).thenReturn(authenticatedAccount);
        when(cryptoService.getCrypto("USDT")).thenReturn(crypto);
        //when/then
//        assertThrows(DepositFailedException.class, () -> tradeService.deposit(fiatTradeDto));
        assertThrows(DepositFailedException.class, () -> tradeService.transferFunds(fiatTradeDto));
    }

    @Test   //sprawdź tą metodę po zmianach
    public void withdraw() throws AccountNotFoundException {
        //given
        BigDecimal price = new BigDecimal("0.98");
        Crypto crypto = new Crypto(1L, "USDT", price);

        Wallet wallet = new Wallet();
        List<Trade> tradeHistory = new ArrayList<>();
        List<WalletCrypto> walletCryptoList = new ArrayList<>();
        BigDecimal total = new BigDecimal(980);
        WalletCrypto walletCrypto = new WalletCrypto(1L, crypto, new BigDecimal(1000), total);
        walletCryptoList.add(walletCrypto);
        wallet.setBalance(total);
        wallet.setTransactionHistory(tradeHistory);
        wallet.setWalletCryptoList(walletCryptoList);

        Account authenticatedAccount = new Account();
        Long accountId = 1L;
        authenticatedAccount.setId(accountId);
        authenticatedAccount.setWallet(wallet);

        BigDecimal amount = new BigDecimal("500.00");
        String fiatSymbol = "USD";
        FiatTradeDto fiatTradeDto = new FiatTradeDto(TradeType.WITHDRAW, fiatSymbol, amount);
        BigDecimal fee = new BigDecimal("10.00");

        when(accountService.getAuthenticatedAccount()).thenReturn(authenticatedAccount);
        when(cryptoService.getCrypto("USDT")).thenReturn(crypto);
        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalValue = new BigDecimal("480.20");
        Trade trade = new Trade(now, TradeType.DEPOSIT, accountId, fiatSymbol, amount, fee, price, totalValue, total);        // to na pewno jest źle
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);
        //when
//        Trade result = tradeService.withdraw(fiatTradeDto);
        Trade result = tradeService.transferFunds(fiatTradeDto);
        //then
        assertThat(result, equalTo(trade));
    }

    @Test   //sprawdź tą metodę po zmianach
    public void withdraw_throwsWithdrawalFailedException() throws AccountNotFoundException {
        //given
        Wallet wallet = new Wallet();
        List<WalletCrypto> walletCryptoList = new ArrayList<>();
        wallet.setWalletCryptoList(walletCryptoList);

        Account authenticatedAccount = new Account();
        Long accountId = 1L;
        authenticatedAccount.setId(accountId);
        authenticatedAccount.setWallet(wallet);

        BigDecimal amount = new BigDecimal("500.00");
        String fiatSymbol = "USD";
        FiatTradeDto fiatTradeDto = new FiatTradeDto(TradeType.WITHDRAW, fiatSymbol, amount);
        Crypto crypto = new Crypto(1L, "USDT", new BigDecimal(1));

        when(accountService.getAuthenticatedAccount()).thenReturn(authenticatedAccount);
        when(cryptoService.getCrypto("USDT")).thenReturn(crypto);
        //when/then
//        assertThrows(WithdrawalFailedException.class, () -> tradeService.withdraw(fiatTradeDto));
        assertThrows(WithdrawalFailedException.class, () -> tradeService.transferFunds(fiatTradeDto));
    }

    @Test
    public void downloadTransaction_returnsDownloadedFile() throws DocumentException, FileNotFoundException {
        //given
        Long tradeId = 1L;
        Trade trade = new Trade();
        trade.setId(tradeId);
        trade.setTotalValue(new BigDecimal("140.55"));
        trade.setFee(new BigDecimal("2.05"));
        trade.setAmount(new BigDecimal("10"));
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));
        //when
        String result = tradeService.downloadTransaction(tradeId);
        //then
        assertThat(result, notNullValue());
    }

    @Test
    public void downloadTransaction_throwsTradeNotFoundException() {
        //given
        Long tradeId = 1L;
        when(tradeRepository.findById(tradeId)).thenReturn(Optional.empty());
        //when/then
        assertThrows(TradeNotFoundException.class, () -> tradeService.downloadTransaction(tradeId));
    }

}