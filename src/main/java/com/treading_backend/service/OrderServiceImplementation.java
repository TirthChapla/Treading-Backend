package com.treading_backend.service;

import com.treading_backend.domain.OrderStatus;
import com.treading_backend.domain.OrderType;
import com.treading_backend.model.*;
import com.treading_backend.repository.*;

import com.treading_backend.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImplementation implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    public OrderServiceImplementation(OrderRepository orderRepository, AssetService assetService) {
        this.orderRepository = orderRepository;
        this.assetService = assetService;
    }


    /**
     ---->> @Transactional su kare chhe?

         ✅ Ek transaction start kare when the method is called
         ✅ And commit kare automatically when the method completes successfully
         ❌ But if koi exception throw thay, then rollback thai jase (nothing will be saved in DB)

     */


    // ✅ order create karvani basic logic che
    @Override
    @Transactional
    public Order createOrder(User user, OrderItem orderItem, OrderType orderType) {
        // price calculate kariye = coin price * qty
        double price = orderItem.getCoin().getCurrentPrice() * orderItem.getQuantity();

        // new order object banavyo
        Order order = new Order();
        order.setUser(user); // user set kariye
        order.setOrderItem(orderItem); // order item attach kariye
        order.setOrderType(orderType); // buy/sell type set
        order.setPrice(BigDecimal.valueOf(price)); // final price set
        order.setTimestamp(LocalDateTime.now()); // current time nakhi didho
        order.setStatus(OrderStatus.PENDING); // pending rakhiye first ma

        return orderRepository.save(order); // DB ma save karine return
    }

    // 🎯 get order by ID (jo na male to error throw)
    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    // 🔍 badha orders user na filter kari levana (type/symbol optional)
    @Override
    public List<Order> getAllOrdersForUser(Long userId, String orderType, String assetSymbol) {
        List<Order> allUserOrders = orderRepository.findByUserId(userId); // pela badha lavye

        if (orderType != null && !orderType.isEmpty()) {
            // type match thay to j filter karie
            OrderType type = OrderType.valueOf(orderType.toUpperCase());
            allUserOrders = allUserOrders.stream()
                    .filter(order -> order.getOrderType() == type)
                    .collect(Collectors.toList());
        }

        if (assetSymbol != null && !assetSymbol.isEmpty()) {
            // coin symbol match kari filter
            allUserOrders = allUserOrders.stream()
                    .filter(order -> order.getOrderItem().getCoin().getSymbol().equals(assetSymbol))
                    .collect(Collectors.toList());
        }

        return allUserOrders; // filtered list return
    }

    // ❌ cancel karvu che to check status and update
    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrderById(orderId); // pela order lavye

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED); // pending hoy to cancel
            orderRepository.save(order); // DB ma update
        } else {
            // else error nakhiye
            throw new IllegalStateException("Cannot cancel order, it is already processed or cancelled.");
        }
    }

    // 🧱 order item create karvu (coin, qty, prices)
    private OrderItem createOrderItem(Coin coin, double quantity, double buyPrice, double sellPrice) {
        OrderItem orderItem = new OrderItem();

        orderItem.setCoin(coin); // coin set
        orderItem.setQuantity(quantity); // qty set
        orderItem.setBuyPrice(coin.getCurrentPrice()); // overwrite thase niche
        orderItem.setBuyPrice(buyPrice); // real buy price
        orderItem.setSellPrice(sellPrice); // sell price jode nakhiye

        return orderItemRepository.save(orderItem); // DB ma save
    }

    // 🟢 asset buy karvani process
    @Transactional
    public Order buyAsset(Coin coin, double quantity, User user) throws Exception {
        if (quantity < 0) throw new Exception("quantity should be > 0"); // validation

        double buyPrice = coin.getCurrentPrice(); // latest price lavyo

        // new order item banavyo
        OrderItem orderItem = createOrderItem(coin, quantity, buyPrice, 0);

        // order create kariyo
        Order order = createOrder(user, orderItem, OrderType.BUY);
        orderItem.setOrder(order); // link kari didho

        // 💳 paisa katega wallet mathi
        walletService.payOrderPayment(order, user);

        order.setStatus(OrderStatus.SUCCESS); // order done
        order.setOrderType(OrderType.BUY); // buy confirm

        Order savedOrder = orderRepository.save(order); // final save

        // check if asset already exists
        Asset oldAsset = assetService.findAssetByUserIdAndCoinId(
                order.getUser().getId(),
                order.getOrderItem().getCoin().getId()
        );

        if (oldAsset == null) {
            // navi asset create kariye
            assetService.createAsset(user, orderItem.getCoin(), orderItem.getQuantity());
        } else {
            // existing asset update kariye
            assetService.updateAsset(oldAsset.getId(), quantity);
        }

        return savedOrder;
    }

    // 🔴 asset sell process full logic
    @Transactional
    public Order sellAsset(Coin coin, double quantity, User user) throws Exception {
        double sellPrice = coin.getCurrentPrice(); // current price lavyo

        // pela check kariye ke asset che ke nai
        Asset assetToSell = assetService.findAssetByUserIdAndCoinId(
                user.getId(),
                coin.getId()
        );

        if (assetToSell != null) {
            // order item create for selling
            OrderItem orderItem = createOrderItem(coin, quantity, assetToSell.getBuyPrice(), sellPrice);
            Order order = createOrder(user, orderItem, OrderType.SELL);
            orderItem.setOrder(order);

            Order savedOrder = orderRepository.save(order);

            if (assetToSell.getQuantity() >= quantity) {
                // enough qty che to sell allow
                walletService.payOrderPayment(order, user);

                Asset updatedAsset = assetService.updateAsset(assetToSell.getId(), -quantity);

                // 👀 jo ghanaj ochho baki hoy to delete kariye
                if (updatedAsset.getQuantity() * coin.getCurrentPrice() <= 1) {
                    assetService.deleteAsset(updatedAsset.getId());
                }

                return savedOrder;
            } else {
                // asset ochho hoy to delete & error throw
                orderRepository.delete(order);
                throw new Exception("Insufficient quantity to sell");
            }
        }

        // asset j nathi malto
        throw new Exception("Asset not found for selling");
    }

    // 🌀 order type pramane route – buy or sell
    @Override
    @Transactional
    public Order processOrder(Coin coin, double quantity, OrderType orderType, User user) throws Exception
    {
        if (orderType == OrderType.BUY)
        {
            return buyAsset(coin, quantity, user); // buy path
        }
        else if (orderType == OrderType.SELL)
        {
            return sellAsset(coin, quantity, user); // sell path
        }
        else
        {
            throw new Exception("Invalid order type"); // wrong input
        }
    }
}
