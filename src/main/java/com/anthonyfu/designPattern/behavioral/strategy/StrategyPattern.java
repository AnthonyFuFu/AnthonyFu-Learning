package com.anthonyfu.designPattern.behavioral.strategy;

public class StrategyPattern {
    public static void main(String[] args) {
        ShoppingCart cart = new ShoppingCart();

        // 使用信用卡支付
        cart.setPaymentStrategy(new CreditCardPayment("1234-5678-9012-3456", "123", "12/25"));
        cart.checkout(100.50);

        // 切換到PayPal支付
        cart.setPaymentStrategy(new PayPalPayment("user@example.com", "password"));
        cart.checkout(200.75);
    }
}
