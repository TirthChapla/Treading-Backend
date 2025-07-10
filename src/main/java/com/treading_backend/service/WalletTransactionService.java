package com.treading_backend.service;

import com.treading_backend.domain.WalletTransactionType;
import com.treading_backend.model.Wallet;
import com.treading_backend.model.WalletTransaction;

import java.util.List;

public interface WalletTransactionService {
    WalletTransaction createTransaction(Wallet wallet,
                                        WalletTransactionType type,
                                        String transferId,
                                        String purpose,
                                        Long amount
    );

    List<WalletTransaction> getTransactions(Wallet wallet, WalletTransactionType type);

}
