package com.treading_backend.service;


import com.treading_backend.domain.OrderType;
import com.treading_backend.exception.WalletException;
import com.treading_backend.model.*;

import com.treading_backend.repository.WalletRepository;
import com.treading_backend.repository.WalletTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class WalleteServiceImplementation implements WalletService {


    @Autowired
    private WalletRepository walletRepository;


    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    /**
     * ✅ Generate new wallete for user
     * 👉 this is called when user don’t have wallete yet
     */
    public Wallet genrateWallete(User user)
    {
        Wallet wallet = new Wallet();
        wallet.setUser(user); // 👉 user is connected with this wallet
        return walletRepository.save(wallet); // ✅ saved in database
    }

    /**
     * ✅ get wallet of user, if not found then create new one
     * 👉 very helpful in first time login or signup
     */
    @Override
    public Wallet getUserWallet(User user) throws WalletException {
        Wallet wallet = walletRepository.findByUserId(user.getId());

        if (wallet != null) {
            return wallet;
        }

        // 👉 if wallet not exist, create it
        wallet = genrateWallete(user);
        return wallet;
    }

    /**
     * ✅ find wallet using id
     * 👉 if wallet not found then throw exception
     */
    @Override
    public Wallet findWalletById(Long id) throws WalletException {
        Optional<Wallet> wallet = walletRepository.findById(id);

        if (wallet.isPresent()) {
            return wallet.get();
        }

        // ❌ wallet not found
        throw new WalletException("Wallet not found with id " + id);
    }

    /**
     * ✅ transfer money from sender to receiver wallet
     * 👉 usefull when we want to do wallet to wallet transfer
     */
    @Override
    public Wallet walletToWalletTransfer(User sender, Wallet receiverWallet, Long amount) throws WalletException {
        Wallet senderWallet = getUserWallet(sender); // 👉 get senders wallet

        // ❌ check if sender has enough money
        if (senderWallet.getBalance().compareTo(BigDecimal.valueOf(amount)) < 0) {
            throw new WalletException("Insufficient balance...");
        }

        // 👉 remove from sender
        BigDecimal senderBalance = senderWallet.getBalance().subtract(BigDecimal.valueOf(amount));
        senderWallet.setBalance(senderBalance);
        walletRepository.save(senderWallet);

        // 👉 add to receiver
        BigDecimal receiverBalance = receiverWallet.getBalance().add(BigDecimal.valueOf(amount));
        receiverWallet.setBalance(receiverBalance);
        walletRepository.save(receiverWallet);

        return senderWallet;
    }

    /**
     * ✅ pay money for buy or sell order
     * 👉 deduct money for BUY and add for SELL
     */
    @Override
    public Wallet payOrderPayment(Order order, User user) throws WalletException {
        Wallet wallet = getUserWallet(user); // 👉 get current users wallet

        WalletTransaction walletTransaction = new WalletTransaction();
        walletTransaction.setWallet(wallet);
        walletTransaction.setPurpose(order.getOrderType() + " " + order.getOrderItem().getCoin().getId());
        walletTransaction.setDate(LocalDate.now()); // 👉 date of tranaction
        walletTransaction.setTransferId(order.getOrderItem().getCoin().getSymbol()); // 👉 using coin symbol as ID

        if (order.getOrderType().equals(OrderType.BUY)) {
            // 👉 buying, so remove balance
            walletTransaction.setAmount(-order.getPrice().longValue());

            BigDecimal newBalance = wallet.getBalance().subtract(order.getPrice());

            // ❌ again check if money is enough
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("inside");
                throw new WalletException("Insufficient funds for this transaction.");
            }

            System.out.println("outside---------- ");
            wallet.setBalance(newBalance);
        } else if (order.getOrderType().equals(OrderType.SELL)) {
            // 👉 selling, so add balance
            walletTransaction.setAmount(order.getPrice().longValue());
            BigDecimal newBalance = wallet.getBalance().add(order.getPrice());
            wallet.setBalance(newBalance);
        }

        // ✅ save both wallet and transaction
        walletTransactionRepository.save(walletTransaction);
        walletRepository.save(wallet);
        return wallet;
    }

    /**
     * ✅ just add money to wallete
     * 👉 used when user add funds to account
     */
    @Override
    public Wallet addBalanceToWallet(Wallet wallet, Long money) throws WalletException {
        BigDecimal newBalance = wallet.getBalance().add(BigDecimal.valueOf(money));

        // 👉 update new balance
        wallet.setBalance(newBalance);

        // ✅ save in DB
        walletRepository.save(wallet);
        System.out.println("updated wallet - " + wallet);
        return wallet;
    }
}