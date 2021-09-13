package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@Slf4j
public class Account {

    @NotNull
    @NotEmpty
    private final String accountId;

    @NotNull
    @Min(value = 0, message = "Initial balance must be positive.")
    private BigDecimal balance;

    @JsonIgnore
    private final transient Lock lock = new ReentrantLock();

    public Account(String accountId) {
        this.accountId = accountId;
        this.balance = BigDecimal.ZERO;
    }

    @JsonCreator
    public Account(@JsonProperty("accountId") String accountId,
                   @JsonProperty("balance") BigDecimal balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public boolean debit(BigDecimal amount) throws InterruptedException {
        try {
            if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    if (this.balance.compareTo(amount) >= 0) {
                        this.balance = this.balance.subtract(amount);
                        return true;
                    }
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("Some exception happened during debit ", e.getMessage());
            throw e;
        }
        return false;
    }

    public boolean credit(BigDecimal amount) throws InterruptedException {
        try {
            if (lock.tryLock(10000, TimeUnit.MILLISECONDS)) {
                this.balance = this.balance.add(amount);
            }
        } catch (InterruptedException e) {
            log.error("Some exception happened during credit ", e.getMessage());
            throw e;
        } finally {
            lock.unlock();
        }
        return true;
    }
}
