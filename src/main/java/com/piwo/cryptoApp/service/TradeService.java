package com.piwo.cryptoApp.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.piwo.cryptoApp.exception.*;
import com.piwo.cryptoApp.model.*;
import com.piwo.cryptoApp.model.dto.CryptoTradeDto;
import com.piwo.cryptoApp.model.dto.FiatTradeDto;
import com.piwo.cryptoApp.model.enums.TradeType;
import com.piwo.cryptoApp.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService {
    private static final String USDT_SYMBOL = "USDT";
    //Zrobiłem na sztywno USDT_PRICE, bo co chwilę kuCoin zmienia coś w listach api i kurs USD_USDT się nie zgadza
    //    private static final BigDecimal USDT_PRICE = new BigDecimal("0.995");
    private static final BigDecimal FEE = new BigDecimal("0.02");
    public static final String DOWNLOAD_AREA = "C://Users/Piwo/IdeaProjects/PIWO/downloads/";
    private final AccountService accountService;
    private final TradeRepository tradeRepository;
    private final CryptoService cryptoService;
    private final WalletService walletService;
    private final WalletCryptoService walletCryptoService;

    /**
     * Returns list of trades from database.
     *
     * @return list of trades.
     */
    public List<Trade> getTrades() {
        return tradeRepository.findAll();
    }

    /**
     * Returns trades from database by its symbol.
     *
     * @param symbol crypto symbol.
     * @return list of trades.
     */
    public List<Trade> findTradesBySymbol(String symbol) {
        return getTrades().stream()
                .filter(trade ->
                        trade.getCryptoToTradeSymbol() != null && trade.getCryptoToTradeSymbol().contains(symbol) ||
                                trade.getCryptoToSellSymbol() != null && trade.getCryptoToSellSymbol().contains(symbol)
                )
                .collect(Collectors.toList());
    }

//wyszukiwanie z varargs?
//    public List<Trade> findTrades(String... searchParams) {
//        List<Trade> trades = getTrades();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//        return trades.stream()
//                .filter(trade ->
//                        (date == null || trade.getDate().contains(date)) &&
//                                (type == null || trade.getType().equals(type)) &&
//                                (fiatSymbol == null || trade.getFiatSymbol().contains(fiatSymbol)) &&
//                                (cryptoSELL == null || trade.getCryptoSELL().contains(cryptoSELL)) &&
//                                (cryptoBUY == null || trade.getCryptoBUY().contains(cryptoBUY)))
//                .collect(Collectors.toList());
//    }

    /**
     * Creates transaction of cryptocurrency based on cryptoTradeDto.
     *
     * @param cryptoTradeDto data provided by user.
     * @return created trade.
     * @throws AccountNotFoundException if account authentication failed.
     */
    public Trade tradeCrypto(CryptoTradeDto cryptoTradeDto) throws AccountNotFoundException {
        Account authenticatedAccount = accountService.getAuthenticatedAccount();
        TradeType tradeType = cryptoTradeDto.getTradeType();

        //Sprawdź, czy cryptoToTrade istnieje na liście
        String cryptoToTradeSymbol = cryptoTradeDto.getCryptoToTradeSymbol();
        Crypto cryptoToTrade = checkIfCryptoExists(cryptoToTradeSymbol);
        String baseCryptoSymbol = cryptoTradeDto.getBaseCryptoSymbol();
        Crypto baseCrypto = checkIfCryptoExists(baseCryptoSymbol);

        //Sprawdź, czy podana cena jednostkowa jest wyższa od aktualnej ceny kryptowaluty (wystarczy o 0.00000001)
        priceValidation(cryptoTradeDto, cryptoToTrade);

        //Oblicz wstępnie wartość transakcji
        BigDecimal unitPrice = cryptoToTrade.getPrice();
//        BigDecimal unitPriceToUSDT = unitPrice.divide(baseCrypto.getPrice(), 18, RoundingMode.HALF_UP);
        BigDecimal amount = cryptoTradeDto.getAmount();
        BigDecimal totalValueInUSD = amount.multiply(unitPrice);
//        BigDecimal totalValueInUSDT = amount.multiply(unitPriceToUSDT);

        //Pobierz portfel i sprawdź, czy masz krypto na wymianę
        Wallet wallet = authenticatedAccount.getWallet();
        List<WalletCrypto> walletCryptoList = wallet.getWalletCryptoList();
        Optional<WalletCrypto> optionalCrypto = Optional.empty();
        String cryptoSymbol = "";

        if (tradeType.equals(TradeType.BUY)) {
            optionalCrypto = getOptionalWalletCrypto(baseCryptoSymbol, walletCryptoList);
            cryptoSymbol = baseCryptoSymbol;
        } else if (tradeType.equals(TradeType.SELL)) {
            optionalCrypto = getOptionalWalletCrypto(cryptoToTradeSymbol, walletCryptoList);
            cryptoSymbol = cryptoToTradeSymbol;
        }

        if (optionalCrypto.isEmpty()) {
            throw new CryptoNotFoundException(baseCryptoSymbol);
        }
        // Sprawdź, czy wystarczy krypto na wymianę
        WalletCrypto walletCrypto = optionalCrypto.get();
        if (totalValueInUSD.compareTo(walletCrypto.getValue()) > 0) {
            throw new BuyCryptoException(cryptoSymbol);
        }

        // Oblicz opłatę FEE
//        BigDecimal priceFee = calculateFee(totalValueInUSD);
        BigDecimal amountFee = calculateFee(amount);

        BigDecimal currentAmount = amount.subtract(amountFee);
        BigDecimal currentValueInUSD = currentAmount.multiply(unitPrice);
//        BigDecimal currentValueInUSDT = currentAmount.multiply(totalValueInUSDT);

        //Transakcja
        Trade savedTrade = createTrade(authenticatedAccount, tradeType, cryptoToTradeSymbol, baseCryptoSymbol, unitPrice, amount, amountFee, totalValueInUSD);

        //Dodaj trade do walletHistory
        addTransactionToHistory(authenticatedAccount, savedTrade);

        if (tradeType.equals(TradeType.BUY)) {
            //Zaktualizuj pozycję bazowej kryptowaluty w portfelu
            updateSoldCrypto(baseCrypto, walletCrypto, totalValueInUSD, tradeType);
            //Zaktualizuj zakupioną krypto w portfelu w walletCryptoList
            updatePurchasedCrypto(authenticatedAccount, cryptoToTrade, currentAmount, walletCryptoList);
        } else {
            // Zaktualizuj pozycję kryptowaluty do sprzedaży w portfelu
            updateSoldCrypto(cryptoToTrade, walletCrypto, amount, tradeType);
            //Zaktualizuj pozycję kryptowaluty po zakupie
            updatePurchasedCrypto(authenticatedAccount, baseCrypto, currentValueInUSD, walletCryptoList);
        }
        return savedTrade;
    }

    /**
     * Checks if wallet includes crypto that we want to update. Updates purchased crypto in wallet, or creates new one.
     *
     * @param authenticatedAccount account to authenticate.
     * @param crypto               cryptocurrency to update.
     * @param amount               amount to change.
     * @param walletCryptoList     list of cryptocurrencies from accounts wallet.
     * @throws AccountNotFoundException if authentication failed.
     */
    private void updatePurchasedCrypto(Account authenticatedAccount, Crypto crypto, BigDecimal amount, List<WalletCrypto> walletCryptoList) throws AccountNotFoundException {
        String cryptoSymbol = crypto.getSymbol();
        WalletCrypto walletCrypto = getWalletCrypto(authenticatedAccount, cryptoSymbol);
        if (walletCrypto != null) {
            walletCryptoService.updateWalletCrypto(cryptoSymbol, amount);
        } else {
            WalletCrypto newWalletCrypto = walletCryptoService.createWalletCrypto(crypto, amount);
            walletCryptoList.add(newWalletCrypto);
        }
        walletService.recalculateBalance();
    }

    /**
     * Checks if wallet includes crypto that we want to update. Updates sold crypto in wallet.
     *
     * @param crypto       crypto to sell.
     * @param walletCrypto crypto in accounts wallet.
     * @param amount       amount to update.
     * @param tradeType    trade type (buy or sell)
     */
    private void updateSoldCrypto(Crypto crypto, WalletCrypto walletCrypto, BigDecimal amount, TradeType tradeType) {    //to powinno ogarnąć 2 powyższe metody ForBUY i ForSELL
        if (tradeType.equals(TradeType.BUY)) {
            walletCrypto.setAmount(walletCrypto.getAmount().subtract(amount));
        } else {
            walletCrypto.setAmount(walletCrypto.getAmount().subtract(amount));
        }
        BigDecimal cryptoValue = walletCrypto.getAmount().multiply(crypto.getPrice().setScale(18, RoundingMode.HALF_UP));
        walletCrypto.setValue(cryptoValue);
    }

    /**
     * Creates trade and saves it to database.
     *
     * @return created trade.
     */
    private Trade createTrade(Account authenticatedAccount, TradeType tradeType, String tradeCryptoSymbol, String baseCryptoSymbol, BigDecimal unitPrice, BigDecimal amount, BigDecimal fee, BigDecimal totalPrice) {
        Trade trade = new Trade();
        trade.setTimestamp(LocalDateTime.now());
        trade.setType(tradeType);
        trade.setAccountId(authenticatedAccount.getId());
        trade.setCryptoToTradeSymbol(tradeCryptoSymbol);
        trade.setCryptoToSellSymbol(baseCryptoSymbol);
        trade.setAmount(amount);
        trade.setUnitPrice(unitPrice);
        trade.setFee(fee);
        trade.setTotalValue(totalPrice);
        return tradeRepository.save(trade);
    }

    /**
     * validates price for currency transactions.
     *
     * @param cryptoTradeDto Cryptocurrency transaction data provided.
     * @param crypto         cryptocurrency to which the validation applies.
     */
    private void priceValidation(CryptoTradeDto cryptoTradeDto, Crypto crypto) {
        BigDecimal unitPrice = cryptoTradeDto.getUnitPrice();
        BigDecimal currentPrice = crypto.getPrice();
        TradeType tradeType = cryptoTradeDto.getTradeType();

        boolean isBuy = tradeType.equals(TradeType.BUY);
        if ((isBuy && unitPrice.compareTo(currentPrice) <= 0) || (!isBuy && unitPrice.compareTo(currentPrice) >= 0)) {
            String message = isBuy ? "Price must be higher than current price" : "Price must be lower than current price";
            throw new InvalidPriceException(message);
        }
    }

    /**
     * Retrieves the optional wallet crypto object from the list.
     * @param baseCryptoSymbol symbol of crypto in wallet.
     * @param walletCryptoList list of cryptos in wallet.
     * @return optional of crypto in wallet.
     */
    private Optional<WalletCrypto> getOptionalWalletCrypto(String baseCryptoSymbol, List<WalletCrypto> walletCryptoList) {
        return walletCryptoList.stream()
                .filter(wc -> wc.getCrypto().getSymbol().startsWith(baseCryptoSymbol))
                .findFirst();
    }

    /**
     * Checks if cryptocurrency is in database.
     * @param cryptoToSellSymbol crypto symbol.
     * @return cryptocurrency.
     */
    private Crypto checkIfCryptoExists(String cryptoToSellSymbol) {
        Crypto cryptoToSell = cryptoService.getCrypto(cryptoToSellSymbol);
        if (cryptoToSell == null) {
            throw new CryptoNotFoundException(cryptoToSellSymbol);
        }
        return cryptoToSell;
    }

    /**
     * Transfer funds like deposit/withdraw. Authenticates account, gets data from dto, calculates fee and total value.
     * Creates transaction, updates and saves new data.
     * @param fiatTradeDto provided data
     * @return created Trade
     * @throws AccountNotFoundException if authentication fails.
     * @throws DepositFailedException if something went wrong in deposit.
     * @throws WithdrawalFailedException if something went wrong in withdraw.
     */
    public Trade transferFunds(FiatTradeDto fiatTradeDto) throws AccountNotFoundException, DepositFailedException, WithdrawalFailedException {
        Account authenticatedAccount = accountService.getAuthenticatedAccount();
        Long accountId = authenticatedAccount.getId();
        //Pobierz dane z dto
        TradeType tradeType = fiatTradeDto.getTradeType();
        String fiatSymbol = fiatTradeDto.getFiatSymbol();
        BigDecimal amount = fiatTradeDto.getAmount();
        //Oblicz fee
        BigDecimal fee = calculateFee(amount);
        BigDecimal currentAmount = amount.subtract(fee);
        //Oblicz totalValue
        WalletCrypto walletCrypto = getWalletCrypto(authenticatedAccount, USDT_SYMBOL);
        BigDecimal currentPrice = calculateCurrentPrice(USDT_SYMBOL);
        BigDecimal totalValue = tradeType.equals(TradeType.DEPOSIT) ? calculateTotalValue(currentAmount, USDT_SYMBOL) : currentPrice;
        if ((tradeType.equals(TradeType.WITHDRAW) && (walletCrypto == null || walletCrypto.getAmount().compareTo(amount) < 0))) {
            throw new WithdrawalFailedException("Insufficient funds.");
        }
        //Transakcja
        Trade fiatTrade = createFiatTrade(tradeType, accountId, fiatSymbol, currentAmount, fee, currentPrice, totalValue, amount);
        Trade savedTrade = tradeRepository.save(fiatTrade);
        //Dodaj transakcję do historii
        addTransactionToHistory(authenticatedAccount, savedTrade);
        if (tradeType.equals(TradeType.DEPOSIT)) {
            //Dodaj środki do konta jako USDT
            walletService.addToBalance(totalValue);
            if (fiatSymbol.equals("USD")) {
                depositUSDT(currentAmount);
                return savedTrade;
            } else {
                throw new DepositFailedException();
            }
        } else if (tradeType.equals(TradeType.WITHDRAW)) {
            // Zmiana w obiekcie wallet użytkownika
            BigDecimal newAmount = walletCrypto.getAmount().subtract(amount);
            if (newAmount.compareTo(BigDecimal.ZERO) > 0) {
                walletCrypto.setAmount(newAmount);
                BigDecimal newValue = newAmount.multiply(walletCrypto.getCrypto().getPrice());
//                BigDecimal newValue = newAmount.multiply(USDT_PRICE);
                walletCrypto.setValue(newValue);
                walletService.recalculateBalance();
                walletCryptoService.save(walletCrypto);
            } else {
                //Jeśli ilość kryptowaluty jest <= 0, usuń ją z listy
                authenticatedAccount.getWallet().getWalletCryptoList().remove(walletCrypto);
                walletCryptoService.delete(walletCrypto);
            }
            BigDecimal balance = authenticatedAccount.getWallet().getBalance();
            walletService.recalculateBalance(); // ponownie, żeby zaktualizować, gdy wyprzedamy krypto
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                throw new WithdrawalFailedException("Insufficient balance.");
            }
            return savedTrade;
        }
        return savedTrade;
    }

    /**
     * Calculates total value of crypto.
     *
     * @param currentAmount    amount of crypto.
     * @param stableCoinSymbol symbol of stable coin (like USDT)
     * @return returns total value of passed crypto.
     */
    private BigDecimal calculateTotalValue(BigDecimal currentAmount, String stableCoinSymbol) {
        BigDecimal currentPrice = calculateCurrentPrice(stableCoinSymbol);
        return currentAmount.multiply(currentPrice);
    }

    /**
     * Authenticates account, checks USDT(Theter) coin in wallet and adds/updates it.
     *
     * @param currentAmount amount to deposit.
     * @throws AccountNotFoundException if authentication fails.
     */
    private void depositUSDT(BigDecimal currentAmount) throws AccountNotFoundException {
        Account authenticatedAccount = accountService.getAuthenticatedAccount();
        String coinSymbol = "USDT";
        WalletCrypto cryptoFromWallet = getWalletCrypto(authenticatedAccount, coinSymbol);

        if (cryptoFromWallet != null) {
            walletCryptoService.updateWalletCrypto(coinSymbol, currentAmount);
        } else {
            List<Crypto> cryptoList = cryptoService.createCryptoList();
            Crypto USDT = cryptoList.stream()
                    .filter(crypto -> crypto.getSymbol().equals(coinSymbol))
                    .findFirst().orElse(null);
            List<WalletCrypto> walletCryptoList = authenticatedAccount.getWallet().getWalletCryptoList();

            boolean found = false;
            for (WalletCrypto walletCrypto : walletCryptoList) {
                if (walletCrypto.getCrypto().getSymbol().equals(coinSymbol)) {
                    walletCryptoService.updateWalletCrypto(coinSymbol, currentAmount);
                    found = true;
                    break;
                }
            }
            if (!found) {
                WalletCrypto walletCrypto = walletCryptoService.createWalletCrypto(USDT, currentAmount);
                walletCryptoList.add(walletCrypto);
            }
            walletService.recalculateBalance();
            authenticatedAccount.getWallet().setWalletCryptoList(walletCryptoList);
            accountService.saveAccount(authenticatedAccount);
        }
    }

    /**
     * Calculates current price of crypto.
     *
     * @param stableCoinSymbol like USD GBP EUR
     * @return new price of calculated cryptocurrency.
     */
    private BigDecimal calculateCurrentPrice(String stableCoinSymbol) {// czasami lista się zmienia i źle zaciąga cenę USDT
//        cryptoService.updateCryptoPrice(stableCoinSymbol);
        return cryptoService.getCrypto(stableCoinSymbol).getPrice();
//        return stableCoinSymbol.equals(USDT_SYMBOL) ? USDT_PRICE : BigDecimal.ZERO;
    }

    /**
     * Saves transaction in list of transactions history in users account.     *
     *
     * @param account users account .
     * @param trade   trade to safe in history.
     */
    private void addTransactionToHistory(Account account, Trade trade) {
        List<Trade> transactionHistory = account.getWallet().getTransactionHistory();
        transactionHistory.add(trade);
    }

    /**
     * Returns crypto from users wallet by its symbol.
     *
     * @param account      users account.
     * @param cryptoSymbol symbol of crypto to get.
     * @return crypto from wallet.
     */
    private WalletCrypto getWalletCrypto(Account account, String cryptoSymbol) {
        return account.getWallet().getWalletCryptoList().stream()
                .filter(walletCrypto -> walletCrypto.getCrypto().getSymbol().equals(cryptoSymbol))
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates Fiat trade
     *
     * @return fiat trade.
     */
    private Trade createFiatTrade(TradeType tradeType, Long accountId, String fiatSymbol, BigDecimal amount, BigDecimal
            fee, BigDecimal unitPrice, BigDecimal totalValue, BigDecimal total) {
        return new Trade(
                LocalDateTime.now(),
                tradeType,
                accountId,
                fiatSymbol,
                amount,
                fee,
                unitPrice,
                totalValue,
                total
        );
    }

    /**
     * Calculates fee for transaction.
     *
     * @param amount amount from which the fee is calculated.
     * @return fee.
     */
    private BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(FEE);
    }

    /**
     * Downloads users transaction from database.
     *
     * @param id transaction id.
     * @return message of downloaded transaction.
     * @throws FileNotFoundException if not found file to download.
     * @throws DocumentException     if document fails.
     */
    public String downloadTransaction(Long id) throws FileNotFoundException, DocumentException {
        Trade tradeById = tradeRepository.findById(id).orElseThrow(() -> new TradeNotFoundException(id));
        DateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentSimpleDate = simpleDateFormat.format(new Date());
        String fileName = "transaction" + currentSimpleDate + "_" + tradeById.getId() + ".pdf";
        BigDecimal coinValue = tradeById.getTotalValue().subtract(tradeById.getFee());

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream((DOWNLOAD_AREA + fileName)));
        document.open();
        document.add(new Paragraph(
                "Transaction " + fileName + ":\n\n" +
                        "\tDate: " + tradeById.getTimestamp() + "\n" +
                        "\tTransaction id: " + tradeById.getId() + "\n" +
                        "\tAccount id: " + tradeById.getAccountId() + "\n" +
                        "\tTransaction type:" + tradeById.getType() + "\n" +
                        "\tFiat symbol: " + tradeById.getFiatSymbol() + "\n" +
                        "\tCurrency to buy: " + tradeById.getCryptoToTradeSymbol() + "\n" +
                        "\tCurrency to sell: " + tradeById.getCryptoToSellSymbol() + "\n" +
                        "\tAmount: " + tradeById.getAmount() + "\n" +
                        "\tFee: " + tradeById.getFee() + "\n" +
                        "\tUnit Price: " + tradeById.getUnitPrice() + "\n" +
                        "\tCoin Value: " + coinValue + "\n" +
                        "\tTotal: " + tradeById.getAmount().add(tradeById.getFee())
        ));
        document.close();
        return "file " + fileName + " downloaded";
    }
}