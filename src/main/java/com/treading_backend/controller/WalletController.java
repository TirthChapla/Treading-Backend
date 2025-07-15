package com.treading_backend.controller;

import com.treading_backend.domain.WalletTransactionType;
import com.treading_backend.model.*;
import com.treading_backend.response.PaymentResponse;
import com.treading_backend.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WalletController {

    @Autowired
    private WalletService walleteService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private PaymentService paymentService;

    // ✅ auth token mathi user nu wallet lavvano che bhai
    @GetMapping("/api/wallet")
    public ResponseEntity<?> getUserWallet(@RequestHeader("Authorization") String jwt) throws Exception {
        // 👉 pela to jwt mathi user lai levano
        User user = userService.findUserProfileByJwt(jwt);

        // pachhi aa user no wallet fetch karvano
        Wallet wallet = walleteService.getUserWallet(user);

        // done! backend thi wallet mukli didho 😎
        return new ResponseEntity<>(wallet, HttpStatus.OK);
    }

    // 👉✅✅ user na badha transactions levana che
    @GetMapping("/api/wallet/transactions")
    public ResponseEntity<List<WalletTransaction>> getWalletTransaction(
            @RequestHeader("Authorization") String jwt) throws Exception {

        // token mathi user lai lidho – same j rite
        User user = userService.findUserProfileByJwt(jwt);

        // user no wallet fetch kari lidho che
        Wallet wallet = walleteService.getUserWallet(user);

        // badha transactions lavya, filter vagar j
        List<WalletTransaction> transactions = walletTransactionService.getTransactions(wallet, null);

        return new ResponseEntity<>(transactions, HttpStatus.OK); // lo bhai transactions lai lo 🚀
    }

    // 🧪 test mate wallet ma paisa mukvana che directly
    @PutMapping("/api/wallet/deposit/amount/{amount}")
    public ResponseEntity<PaymentResponse> depositMoney(@RequestHeader("Authorization") String jwt,
                                                        @PathVariable Long amount) throws Exception {

        // jwt mathi user lavyo
        User user = userService.findUserProfileByJwt(jwt);

        // wallet pan lai lidho user no
        Wallet wallet = walleteService.getUserWallet(user);

        // payment gateway skip kari didho 😂
//        PaymentResponse res = walleteService.depositFunds(user,amount);
        PaymentResponse res = new PaymentResponse();
        res.setPayment_url("deposite success"); // spelling joyo? chalvanu bhai

        // paisa add kari lidha wallet ma
        walleteService.addBalanceToWallet(wallet, amount);

        return new ResponseEntity<>(res, HttpStatus.OK); // paisa mukai gaya ✅
    }

    // 💸 real payment gateway thi paisa add karvana
    @PutMapping("/api/wallet/deposit")
    public ResponseEntity<Wallet> addMoneyToWallet(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(name = "order_id") Long orderId,
            @RequestParam(name = "payment_id") String paymentId
    ) throws Exception {

        // user lai lidho jwt mathi
        User user = userService.findUserProfileByJwt(jwt);

        // wallet pan lai lidho
        Wallet wallet = walleteService.getUserWallet(user);

        // payment order id thi order lavyo
        PaymentOrder order = paymentService.getPaymentOrderById(orderId);

        // payment verify kariye payment_id thi
        Boolean status = paymentService.ProccedPaymentOrder(order, paymentId);

        PaymentResponse res = new PaymentResponse();
        res.setPayment_url("deposite success"); // bhai aa same msg rakhyo

        // payment sachu che to paisa add kariye
        if (status) {
            wallet = walleteService.addBalanceToWallet(wallet, order.getAmount());
        }

        return new ResponseEntity<>(wallet, HttpStatus.OK); // paisa add thayi gaya che ✅
    }

    // 🛑 aa withdraw ni API bandh rakhi che — future ma kariye to khyal che
//    @PutMapping("/api/wallet/withdraw/amount/{amount}/user/{userId}")
//    public ResponseEntity<PaymentResponse> withdrawMoney(@PathVariable Long userId, @PathVariable Long amount) throws Exception {
//
//        String wallet = walleteService.depositFunds(userId,amount);
//
//        return new ResponseEntity<>(wallet,HttpStatus.OK);
//    }

    // 🤝 ek wallet mathi bija wallet ma paisa transfer karvana che
    @PutMapping("/api/wallet/{walletId}/transfer")
    public ResponseEntity<Wallet> walletToWalletTransfer(@RequestHeader("Authorization") String jwt,
                                                         @PathVariable Long walletId,
                                                         @RequestBody WalletTransaction req
    ) throws Exception {

        // sender user lai lidho jwt thi
        User senderUser = userService.findUserProfileByJwt(jwt);

        // receiver no wallet find kariyo id thi
        Wallet reciverWallet = walleteService.findWalletById(walletId);

        // transfer kari didho bhai 🎯
        Wallet wallet = walleteService.walletToWalletTransfer(senderUser, reciverWallet, req.getAmount());

        // transaction save kariye sender mate
        WalletTransaction walletTransaction = walletTransactionService.createTransaction(
                wallet,
                WalletTransactionType.WALLET_TRANSFER,
                reciverWallet.getId().toString(),
                req.getPurpose(),
                -req.getAmount() // paisa gaya che etle minus 👍
        );

        return new ResponseEntity<>(wallet, HttpStatus.OK); // transfer successfull 🚀
    }

    // ✅ order no paiso wallet mathi pay karvano che
    @PutMapping("/api/wallet/order/{orderId}/pay")
    public ResponseEntity<Wallet> payOrderPayment(@PathVariable Long orderId,
                                                  @RequestHeader("Authorization") String jwt) throws Exception {
        // jwt thi user lai lidho
        User user = userService.findUserProfileByJwt(jwt);

        // just checking output 😂
        System.out.println("-------- " + orderId);

        // order lavyo DB mathi
        Order order = orderService.getOrderById(orderId);

        // order no paiso wallet mathi katyo
        Wallet wallet = walleteService.payOrderPayment(order, user);

        return new ResponseEntity<>(wallet, HttpStatus.OK); // order paid ✅
    }

}
