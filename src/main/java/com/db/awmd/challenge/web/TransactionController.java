package com.db.awmd.challenge.web;

import com.db.awmd.challenge.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Controller class that handles requests related to transactions
 */
@RestController
@RequestMapping("/v1/transaction")
@Slf4j
public class TransactionController {

    private final TransactionService txnService;

    @Autowired
    public TransactionController(TransactionService txnService) {
        this.txnService = txnService;
    }

    /**
     * POST method to start fund transfer between two accounts
     *
     * @param fromAccountId From Account id (debit)
     * @param toAccountId   To Account id (credit)
     * @param amount        Amount to be transferred
     * @return ResponseEntity
     */
    @PostMapping(path = "/transfer/{fromAccountId}/{toAccountId}/{amount}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> transfer(@PathVariable String fromAccountId, @PathVariable String toAccountId,
                                           @PathVariable BigDecimal amount) {
        log.info("Transferring money");

        try {
            this.txnService.transfer(fromAccountId, toAccountId, amount);
        } catch (Exception exception) {
            return new ResponseEntity<>(exception.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
