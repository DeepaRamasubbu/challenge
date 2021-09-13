package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.ValidationException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class TransactionControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private TransactionService txnService;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        accountsService.getAccountsRepository().clearAccounts();
        Account debitAcc = createAccount("12", new BigDecimal(100));
        accountsService.createAccount(debitAcc);

        Account creditAcc = createAccount("13", new BigDecimal(100));
        accountsService.createAccount(creditAcc);
    }

    private Account createAccount(String accountId, BigDecimal amount) {
        return new Account(accountId, amount);
    }

    @Test
    public void transfer() throws Exception {
        this.mockMvc.perform(post("/v1/transaction/transfer/{fromAccountId}/{toAccountId}/{amount}", "12", "13", 3).
                contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

    }

    @Test
    public void transferWithInvalidAccount() throws Exception {
        this.mockMvc.perform(post("/v1/transaction/transfer/{fromAccountId}/{toAccountId}/{amount}", "11", "13", 3).
                        contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ValidationException))
                .andExpect(result -> assertEquals("Account not found", result.getResolvedException().getMessage()));

    }

    @Test
    public void transferWithInsufficientFund() throws Exception {
        this.mockMvc.perform(post("/v1/transaction/transfer/{fromAccountId}/{toAccountId}/{amount}", "12", "13", 1000).
                        contentType(MediaType.APPLICATION_JSON)).andExpect(status().is5xxServerError())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InsufficientFundsException))
                .andExpect(result -> assertEquals("Insufficient funds", result.getResolvedException().getMessage()));


    }


}
