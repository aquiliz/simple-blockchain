package com.aquiliz.blockchain.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

@Slf4j
@Getter
@ToString
public class Block {
    private static final int BLOCK_MINE_REWARD = 20;
    private static final String BLOCKCHAIN_SYSTEM_ADDRESS = "blockchainSystemAddress";
    private String hash;
    private final String previousHash;
    private final List<Transaction> transactions;
    private final long timestamp;
    private int nonce;

    public Block(@NonNull List<Transaction> transactions, String previousHash, @NonNull long timestamp) {
        this.transactions = transactions;
        this.previousHash = previousHash;
        this.timestamp = timestamp;
        this.hash = calculateHash();
    }

    private String calculateHash() {
        String preHashed = previousHash != null ? previousHash : "" + String.valueOf(timestamp) + nonce + transactions.hashCode();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Attempted to generate block hash with non existing hashing algorithm.");
        }
        byte[] hash = digest.digest(preHashed.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public String mine(int prefixZeroesCount, String minerAddress) {
        String expectedPrefix = generateZeroesString(prefixZeroesCount);
        while (!hash.substring(0, prefixZeroesCount).equals(expectedPrefix)) {
            nonce++;
            this.hash = calculateHash();
        }
        //Mining is successful. Add a reward transaction.
        transactions.add(generateRewardTransaction(minerAddress));
        return hash;
    }

    private Transaction generateRewardTransaction(String receiverAddress) {
        return new Transaction(BLOCKCHAIN_SYSTEM_ADDRESS, receiverAddress, BigDecimal.valueOf(BLOCK_MINE_REWARD));
    }

    private String generateZeroesString(int zeroesCount) {
        return "0".repeat(Math.max(0, zeroesCount));
    }
}