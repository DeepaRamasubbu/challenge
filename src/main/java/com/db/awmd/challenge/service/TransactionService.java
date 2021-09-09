package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service class that handles the logic involved for the transaction related end points
 */
@Service
@Slf4j
public class TransactionService {

    @Getter
    private final AccountsRepository accountsInMemory;

    private NotificationService notificationService;


    @Autowired
    public TransactionService(AccountsRepository accountsRepository, NotificationService notificationService) {
        this.accountsInMemory = accountsRepository;
        this.notificationService = notificationService;
    }

    /**
     * This method validates the request and performs the actual money transfer
     *
     * @param fromAccountId From Account id
     * @param toAccountId   To Account id
     * @param amount        Amount to be transferred
     */
    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        log.info("transfer money");

        Account debit = accountsInMemory.getAccount(fromAccountId);
        Account credit = accountsInMemory.getAccount(toAccountId);
        // 1. Validate the account
        validate(debit, credit, amount);

        // 2. transfer the amount
        runTransfer(debit, credit, amount);
        // 3. Send notification

    }

    private void validate(Account debit, Account credit, BigDecimal amount) {
        if (null == credit || null == debit) {
            throw new IllegalArgumentException("Account not found");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount should be greater than 0");

    }

    private void runTransfer(Account debit, Account credit, BigDecimal amount) {

    }

}


