package com.treading_backend.controller;

import com.treading_backend.model.Coin;
import com.treading_backend.model.Order;
import com.treading_backend.model.User;
import com.treading_backend.request.CreateOrderRequest;
import com.treading_backend.service.CoinService;
import com.treading_backend.service.OrderService;
import com.treading_backend.service.UserService;


import com.treading_backend.service.WalletTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController
{
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userSerivce;

    @Autowired
    private CoinService coinService;

    @Autowired
    private WalletTransactionService walletTransactionService;

//    private

    // ✅ constructor through dependency inject karavi didhu
    @Autowired
    public OrderController(OrderService orderService, UserService userSerivce)
    {
        this.orderService = orderService;
        this.userSerivce = userSerivce;
    }

    // 🟢 order buy/sell karvani API che (coin + qty + type)
    @PostMapping("/pay")
    public ResponseEntity<Order> payOrderPayment(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateOrderRequest req
    ) throws Exception
    {

        // 🔐 jwt mathi user extract kariye
        User user = userSerivce.findUserProfileByJwt(jwt);

        // 📀 coin id thi coin ni detail lavvi
        Coin coin = coinService.findById(req.getCoinId());

        // 💼 order process karavie (buy/sell based on req)
        Order order = orderService.processOrder(
                coin,
                req.getQuantity(),
                req.getOrderType(),
                user
        );

        return ResponseEntity.ok(order); // ✅ order done
    }

    // 🔍 order id thi ek j order fetch karvani API
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @RequestHeader("Authorization") String jwtToken,
            @PathVariable Long orderId
    ) throws Exception
    {
        // ⚠️ token nathi to error throw kariye
        if (jwtToken == null)
        {
            throw new Exception("token missing...");
        }

        // 🔐 user ni profile lai lidhi
        User user = userSerivce.findUserProfileByJwt(jwtToken);

        // 🧾 order fetch from DB
        Order order = orderService.getOrderById(orderId);

        // ✅ same user che to ok else forbidden
        if (order.getUser().getId().equals(user.getId()))
        {
            return ResponseEntity.ok(order);
        }
        else
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // ❌ access denied
        }
    }

    // 📋 badha orders fetch karvani (with optional filter)
    @GetMapping()
    public ResponseEntity<List<Order>> getAllOrdersForUser(
            @RequestHeader("Authorization") String jwtToken,
            @RequestParam(required = false) String order_type,     // BUY/SELL
            @RequestParam(required = false) String asset_symbol    // e.g. BTC
    ) throws Exception {
        // ⚠️ token check kariye pela
        if (jwtToken == null)
        {
            throw new Exception("token missing...");
        }

        // 🔐 user id lai lidho from token
        Long userId = userSerivce.findUserProfileByJwt(jwtToken).getId();

        // 🧾 user na orders fetch + filters apply
        List<Order> userOrders = orderService.getAllOrdersForUser(
                userId,
                order_type,
                asset_symbol
        );

        return ResponseEntity.ok(userOrders); // ✨ list return
    }

}
