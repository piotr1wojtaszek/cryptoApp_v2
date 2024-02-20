package com.piwo.cryptoApp.service;

import com.piwo.cryptoApp.exception.AccountAlreadyExistsException;
import com.piwo.cryptoApp.model.Account;
import com.piwo.cryptoApp.model.Wallet;
import com.piwo.cryptoApp.model.dto.AccountDto;
import com.piwo.cryptoApp.repository.AccountRepository;
import com.piwo.cryptoApp.repository.WalletRepository;
import com.piwo.cryptoApp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

   /**
     * Returns a list of all Account users from the repository.
     *
     * @return list of Account users.
     */
    public List<Account> getAllUsers() {
        return accountRepository.findAll();
    }

    /**
     * Returns the user Account by his ID from the repository.
     *
     * @param accountId an Id of Account user
     * @return Account user from repository.
     * @throws AccountNotFoundException when the id does not match any user in database.
     */
    public Account getAccountById(Long accountId) throws AccountNotFoundException {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountId));
    }

    /**
     * Returns the user Account by his email from the repository.
     *
     * @param email users email account
     * @return Account user from repository.
     * @throws AccountNotFoundException when the email does not match any user in database.
     */
    public Account getAccountByEmail(String email) throws AccountNotFoundException {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with email: " + email));
    }

    /**
     * Checks whether the user with the provided data already exists. If not,
     * it creates a new user and saves it to the repository.
     *
     * @param accountDto data provided by the user.
     * @return new Account user
     * @throws AccountAlreadyExistsException if the user Account already exists in the repository.
     */
    @Transactional
    public Account createNewAccount(AccountDto accountDto) throws AccountAlreadyExistsException {
        String email = accountDto.getEmail();
        if (accountRepository.existsByEmail(email)) {
            throw new AccountAlreadyExistsException(email);
        }
        Account accountBuild = Account.builder()
                .username(accountDto.getUsername())
                .email(email)
                .password(accountDto.getPassword())
                .build();
        emailTakenChecker(accountDto);
        accountBuild.setPassword(encodePassword(accountBuild));
        accountRepository.save(accountBuild);
        // jest 2x save doi repo bo setWallet musi mieć utworzony account
        accountBuild.setWallet(createWalletForAccount());
        return accountRepository.save(accountBuild);
    }

    /**
     * Creates new wallet for Account user
     *
     * @return new Wallet
     */
    private Wallet createWalletForAccount() {
        return walletRepository.save(new Wallet());
    }

    /**
     * encodes the password of the Account user
     *
     * @param account user with password to encode
     * @return encoded password
     */
    private String encodePassword(Account account) {
        return passwordEncoder.encode(account.getPassword());
    }

    /**
     * Checks if email from dto is taken.
     *
     * @param accountDto data provided by the user.
     * @throws AccountAlreadyExistsException if provided data is already in database.
     */
    private void emailTakenChecker(AccountDto accountDto) throws AccountAlreadyExistsException {
        if (accountRepository.findByEmail(accountDto.getEmail()).isPresent()) {
            throw new AccountAlreadyExistsException(accountDto.getEmail());
        }
    }

    /**
     * Removes Account user from database.
     *
     * @param email users email.
     * @return simple info about successful delete.
     * @throws AccountNotFoundException if the user with the provided email address is not in the database
     */
    public String deleteAccount(String email) throws AccountNotFoundException {
        if (accountRepository.findByEmail(email).isPresent()) {
            accountRepository.deleteByEmail(email);
            return "Account deleted.";
        }
        throw new AccountNotFoundException("Account not found with email: " + email);
    }

    /**
     * Enables new user to use an application
     *
     * @param email users email
     */
    public void enableAccount(String email) {
        accountRepository.enableAccount(email);
    }

    /**
     * Checks if the user is authenticated and returns that user.
     *
     * @return authenticated user.
     * @throws AccountNotFoundException if there is no specific user in database.
     */
    @Transactional
    public Account getAuthenticatedAccount() throws AccountNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String email = userDetails.getAccount().getEmail();
            return accountRepository.findByEmail(email).orElseThrow();
        }
        throw new AccountNotFoundException();
    }

    /**
     * Saves new Account user in database.
     *
     * @param account account to save in database.
     */
    public void saveAccount(Account account) {
        accountRepository.save(account);
    }
}
