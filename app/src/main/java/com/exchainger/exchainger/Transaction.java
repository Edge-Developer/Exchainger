package com.exchainger.exchainger;

import com.exchainger.exchainger.Model.Constants;

/**
 * Created by OPEYEMI OLORUNLEKE on 9/20/2017.
 */

public class Transaction {
    private boolean isOpen;
    private String type;
    private long time;
    private int dollarPrice;
    private int exchangeRate;
    private String transactionKey;

    public Transaction() {
    }

    public Transaction(boolean isOpen, String type, long time, int dollarPrice, int exchangeRate, String transactionKey) {
        this.isOpen = isOpen;
        this.type = type;
        this.time = time;
        this.dollarPrice = dollarPrice;
        this.exchangeRate = exchangeRate;
        this.transactionKey = transactionKey;
    }

    public boolean getIsOpen() {
        return isOpen;
    }

    public String getTransactionKey() {
        return transactionKey;
    }

    public String getType() {
        return type;
    }

    public long getTime() {
        return time;
    }


    public int getDollarPrice() {
        return dollarPrice;
    }

    public int getExchangeRate() {
        return exchangeRate;
    }

    public String getNairaEquivalent() {
        return Constants.NAIRA_SIGN+Constants.FORMATTER.format((exchangeRate * dollarPrice));
    }
}
