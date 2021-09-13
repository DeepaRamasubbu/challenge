package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.ValidationException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.EmailNotificationService;
import com.db.awmd.challenge.service.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceTest {
    private TransactionService transactionService;

    @Mock
    private AccountsRepository mockAccountRepo;

    @Mock
    private EmailNotificationService mockNotificationService;

    private Account debitAcc, creditAcc;


    @Before
    public void prepare() {
        transactionService = new TransactionService(mockAccountRepo, mockNotificationService);
        debitAcc = createAccount("12", new BigDecimal(100));
        creditAcc = createAccount("13", new BigDecimal(100));
    }

    private Account createAccount(String accountId, BigDecimal amount) {
        return new Account(accountId, amount);
    }

    @Test
    public void transfer() throws InterruptedException {
        when(mockAccountRepo.getAccount("12")).thenReturn(debitAcc);
        when(mockAccountRepo.getAccount("13")).thenReturn(creditAcc);
        transactionService.transfer("12", "13", new BigDecimal(100));
        verify(mockNotificationService, times(2)).notifyAboutTransfer(any(Account.class), anyString());
    }

    @Test(expected = InterruptedException.class)
    public void transferWithException() throws InterruptedException {
        when(mockAccountRepo.getAccount("12")).thenReturn(debitAcc);
        when(mockAccountRepo.getAccount("13")).thenReturn(creditAcc);

        debitAcc.getLock().lock();
        creditAcc.getLock().lock();

        Thread.currentThread().interrupt();
        transactionService.transfer("12", "13", new BigDecimal(100));
        verify(mockNotificationService, times(0)).notifyAboutTransfer(any(Account.class), anyString());

    }

    @Test(expected = InsufficientFundsException.class)
    public void transferWithInsufficientFunds() throws InterruptedException {
        Account debit = createAccount("14", new BigDecimal(10));
        when(mockAccountRepo.getAccount("14")).thenReturn(debit);
        when(mockAccountRepo.getAccount("13")).thenReturn(creditAcc);

        transactionService.transfer("14", "13", new BigDecimal(100));
        verify(mockNotificationService, times(0)).notifyAboutTransfer(any(Account.class), anyString());
    }

    @Test(expected = ValidationException.class)
    public void transferWithInvalidAccount() throws InterruptedException {
        transactionService.transfer("15", "13", new BigDecimal(100));
        verify(mockNotificationService, times(0)).notifyAboutTransfer(any(Account.class), anyString());
    }

    @Test(expected = ValidationException.class)
    public void transferWithZeroAmount() throws InterruptedException {
        when(mockAccountRepo.getAccount("12")).thenReturn(creditAcc);
        when(mockAccountRepo.getAccount("13")).thenReturn(debitAcc);
        transactionService.transfer("12", "13", new BigDecimal(0));
        verify(mockNotificationService, times(0)).notifyAboutTransfer(any(Account.class), anyString());
    }

}
