package com.treading_backend.service;

import com.treading_backend.domain.WalletTransactionType;
import com.treading_backend.model.Wallet;
import com.treading_backend.model.WalletTransaction;
import com.treading_backend.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
@Service
public class WalletTransactionServiceImpl implements WalletTransactionService {

    // 👉 Injecting the WalletTransactionRepository to interact with the DB
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    // ✅ This method is used to create a new wallet transaction
    @Override
    public WalletTransaction createTransaction(Wallet wallet,
                                               WalletTransactionType type,
                                               String transferId,
                                               String purpose,
                                               Long amount
    ) {
        // 👉 Create new WalletTransaction object
        WalletTransaction transaction = new WalletTransaction();

        // 👉 Set wallet reference
        transaction.setWallet(wallet);

        // 👉 Set current date as transaction date
        transaction.setDate(LocalDate.now());

        // 👉 Set transaction type (DEPOSIT / WITHDRAWAL etc.)
        transaction.setType(type);

        // 👉 Set transfer ID for tracking
        transaction.setTransferId(transferId);

        // 👉 Set purpose of transaction (e.g., "Add Funds", "Buy Crypto", etc.)
        transaction.setPurpose(purpose);

        // 👉 Set the amount being transacted
        transaction.setAmount(amount);

        // ✅ Save transaction to DB and return the saved entity
        return walletTransactionRepository.save(transaction);
    }

    // ✅ This method retrieves all transactions of a given wallet (can be filtered later by type)
    @Override
    public List<WalletTransaction> getTransactions(Wallet wallet, WalletTransactionType type) {
        // 👉 Currently fetching all transactions for the given wallet ordered by date (latest first)
        return walletTransactionRepository.findByWalletOrderByDateDesc(wallet);
    }
}