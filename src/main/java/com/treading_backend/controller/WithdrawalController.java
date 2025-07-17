package com.treading_backend.controller;

import com.treading_backend.domain.WalletTransactionType;
import com.treading_backend.model.User;
import com.treading_backend.model.Wallet;
import com.treading_backend.model.WalletTransaction;
import com.treading_backend.model.Withdrawal;
import com.treading_backend.service.UserService;
import com.treading_backend.service.WalletService;
import com.treading_backend.service.WalletTransactionService;
import com.treading_backend.service.WithdrawalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WithdrawalController {

    @Autowired
    private WithdrawalService withdrawalService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletTransactionService walletTransactionService;


    // ✅ user withdrawal request mukse 💸
    @PostMapping("/api/withdrawal/{amount}")
    public ResponseEntity<?> withdrawalRequest(
            @PathVariable Long amount,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        // 👉 pela user ne auth karie jwt thi
        User user = userService.findUserProfileByJwt(jwt);

        // 👉 wallet lavie to cut paisa from balance
        Wallet userWallet = walletService.getUserWallet(user);

        // ✅ withdrawal request DB ma save kariye
        Withdrawal withdrawal = withdrawalService.requestWithdrawal(amount, user);

        // 👉 user na wallet mathi paisa ghatadva
        walletService.addBalanceToWallet(userWallet, -withdrawal.getAmount());

        // ❤️ ek withdrawal transaction create kariye
        WalletTransaction walletTransaction = walletTransactionService.createTransaction(
                userWallet,
                WalletTransactionType.WITHDRAWAL,
                null,
                "bank account withdrawal",
                withdrawal.getAmount()
        );

        return new ResponseEntity<>(withdrawal, HttpStatus.OK); // ✅ done
    }

    // ✅ admin taraf thi approve/decline karvani API
    @PatchMapping("/api/admin/withdrawal/{id}/proceed/{accept}")
    public ResponseEntity<?> proceedWithdrawal(
            @PathVariable Long id,
            @PathVariable boolean accept,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        // 👉 admin ne validate kariye (jwt mathi)
        User user = userService.findUserProfileByJwt(jwt);

        // ✅ withdrawal approve / decline thase
        Withdrawal withdrawal = withdrawalService.procedWithdrawal(id, accept);

        // ❤️ paisa return karva hoy to wallet ma add
        Wallet userWallet = walletService.getUserWallet(user);
        if (!accept) {
            walletService.addBalanceToWallet(userWallet, withdrawal.getAmount());
        }

        return new ResponseEntity<>(withdrawal, HttpStatus.OK); // ✅ response jema status hase
    }

    // ✅ user ni personal withdrawal history lavvani
    @GetMapping("/api/withdrawal")
    public ResponseEntity<List<Withdrawal>> getWithdrawalHistory(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        // 👉 user fetch from jwt
        User user = userService.findUserProfileByJwt(jwt);

        // ❤️ get user ni request history
        List<Withdrawal> withdrawal = withdrawalService.getUsersWithdrawalHistory(user);

        return new ResponseEntity<>(withdrawal, HttpStatus.OK); // ✅ send all
    }

    // ✅ admin ne badhi withdrawal requests jova mate
    @GetMapping("/api/admin/withdrawal")
    public ResponseEntity<List<Withdrawal>> getAllWithdrawalRequest(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        // 👉 admin ne auth karavie pela
        User user = userService.findUserProfileByJwt(jwt);

        // ✅ get all withdrawals from DB
        List<Withdrawal> withdrawal = withdrawalService.getAllWithdrawalRequest();

        return new ResponseEntity<>(withdrawal, HttpStatus.OK); // ❤️ full list
    }
}
