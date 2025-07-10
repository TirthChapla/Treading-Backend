package com.treading_backend.service;

import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;
import com.treading_backend.domain.PaymentMethod;
import com.treading_backend.model.PaymentOrder;
import com.treading_backend.model.User;
import com.treading_backend.response.PaymentResponse;

public interface PaymentService {

    PaymentOrder createOrder(User user, Long amount, PaymentMethod paymentMethod);

    PaymentOrder getPaymentOrderById(Long id) throws Exception;

    Boolean ProccedPaymentOrder (PaymentOrder paymentOrder,
                                 String paymentId) throws RazorpayException;

    PaymentResponse createRazorpayPaymentLink(User user,
                                              Long Amount,
                                              Long orderId) throws RazorpayException;

    PaymentResponse createStripePaymentLink(User user, Long Amount,
                                            Long orderId) throws StripeException;
}
