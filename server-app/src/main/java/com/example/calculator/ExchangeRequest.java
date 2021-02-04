package com.example.calculator;

public class ExchangeRequest {
    private double amount;
    private String source;
    private String target;

    public ExchangeRequest() {
    }

    public ExchangeRequest(double amount, String source, String target) {
        this.amount = amount;
        this.source = source;
        this.target = target;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
