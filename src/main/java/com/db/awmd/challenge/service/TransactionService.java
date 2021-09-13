package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.ValidationException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service class that handles the logic involved for the transaction related end points
 */
@Service
@Slf4j
public class TransactionService {

    @Getter
    private final AccountsRepository accountsInMemory;

    private EmailNotificationService notificationService;


    @Autowired
    public TransactionService(AccountsRepository accountsRepository, EmailNotificationService notificationService) {
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
    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) throws InterruptedException {
        log.info("transfer money");

        Account debit = accountsInMemory.getAccount(fromAccountId);
        Account credit = accountsInMemory.getAccount(toAccountId);
        // 1. Validate the account
        validate(debit, credit, amount);

        // 2. transfer the amount
        runTransfer(debit, credit, amount);
        // 3. Send notification
        notificationService.notifyAboutTransfer(debit,"Amount " + amount + " has been transferred to account id " +
                credit.getAccountId());
        notificationService.notifyAboutTransfer(credit,"Amount " + amount + " has been transferred from account id " +
                debit.getAccountId());


    }

    private void validate(Account debit, Account credit, BigDecimal amount) {
        if (null == credit || null == debit) {
            throw new ValidationException("Account not found");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new ValidationException("Amount should be greater than 0");

    }

    private void runTransfer(Account debit, Account credit, BigDecimal amount) throws InterruptedException {
        Lock debitlock = debit.getLock();
        try {
            if (debitlock.tryLock(10000, TimeUnit.MILLISECONDS)) {
                Lock creditLock = credit.getLock();
                try {
                    if (creditLock.tryLock(100, TimeUnit.MILLISECONDS)) {
                        if (debit.debit(amount)) {
                            credit.credit(amount);
                        } else {
                            log.info("Insufficient funds");
                            throw new InsufficientFundsException("Insufficient funds");
                        }
                    }
                } finally {
                    creditLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.info("concurrency error - Something unexpected happened");
            throw e;
        } finally {
            debitlock.unlock();
        }

    }

}


