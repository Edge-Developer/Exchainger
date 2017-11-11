package com.exchainger.exchainger.Model;

import java.text.DecimalFormat;

/**
 * Created by OPEYEMI OLORUNLEKE on 9/18/2017.
 */

public class Constants {
    public static final String FIREBASE_CHILD_TRANSACTIONS = "Transactions";
    public static final String FIREBASE_CHILD_USERS = "Users";
    public static final String FIREBASE_CHILD_SELL = "Sell";
    public static final String FIREBASE_CHILD_BUY = "Buy";
    public static final String FIREBASE_CHILD_TRANSACTION_CHATS= "TransactionChats";
    public static final String FIREBASE_CHILD_TRANSACTION_INFO= "TransactionInfo";
    public static final String FIREBASE_CHILD_CHATS= "Chats";
    public static final DecimalFormat FORMATTER = new DecimalFormat("#,###");
    public static final String NAIRA_SIGN ="₦";
    public static final String DOLLAR_SIGN="$";

    public static final String SELLER_ID = "sellerId";
    public static final String BUYER_ID = "buyerId";
}

