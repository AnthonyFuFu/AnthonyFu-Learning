package com.anthonyfu.designPattern.behavioral.strategy;

class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    private String cvv;
    private String expiryDate;

    public CreditCardPayment(String cardNumber, String cvv, String expiryDate) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.expiryDate = expiryDate;
    }

    @Override
    public void pay(double amount) {
        System.out.println(amount + " paid using Credit Card (Number: " + cardNumber + ", cvv:" + cvv + ", expiryDate:" + expiryDate + ")");
    }
}
