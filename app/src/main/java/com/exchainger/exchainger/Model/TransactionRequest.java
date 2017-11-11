package com.exchainger.exchainger.Model;

import static com.exchainger.exchainger.Model.Constants.DOLLAR_SIGN;
import static com.exchainger.exchainger.Model.Constants.FORMATTER;
import static com.exchainger.exchainger.Model.Constants.NAIRA_SIGN;

/**
 * Created by OPEYEMI OLORUNLEKE on 9/24/2017.
 */

public class TransactionRequest {

    private boolean isSelling;
    private boolean isGiftCard;
    private int exchangePrice;
    private int dollarPrice;
    private boolean isInParts;
    private String itemName;

    public TransactionRequest() {
    }

    public TransactionRequest(boolean isSelling,
                              boolean isGiftCard,
                              int exchangeRate,
                              int dollarPrice,
                              boolean isInParts,
                              String itemName) {

        this.isSelling = isSelling;
        this.isGiftCard = isGiftCard;
        this.exchangePrice = exchangeRate;
        this.dollarPrice = dollarPrice;
        this.isInParts = isInParts;
        this.itemName = itemName;
    }


    public String getItemName() {
        return itemName;
    }


    public boolean getIsGiftCard() {
        return isGiftCard;
    }

    public int getExchangePrice() {
        return exchangePrice;
    }

    public int getDollarPrice() {
        return dollarPrice;
    }

    public boolean getIsInParts() {
        return isInParts;
    }


    public String getNairaEquivalent() {
        String string = NAIRA_SIGN + FORMATTER.format((exchangePrice * dollarPrice));
        if (!getIsSelling() || getIsGiftCard()){
            return string;
        }else if (getIsInParts()) {
            string += " or Less";
        }
        return string;
    }

    public String getSellingMode() {
        String s;
        if (getIsGiftCard() || !getIsSelling()) {
            s = "";
        } else {
            if (getIsInParts()) {
                s = "In Parts";
            } else {
                s = "All at Once";
            }
        }
        return s;
    }

    public String getExchangeRate(){
        return NAIRA_SIGN+ exchangePrice +"/"+DOLLAR_SIGN;
    }

    public String getRequestString() {
        String string = DOLLAR_SIGN + dollarPrice + " " + itemName + " at " + getExchangeRate();
        if (getIsSelling()) {
            string = "I Want To Sell " + string;
            if (getIsGiftCard()) {
                return string;
            } else {
                if (getIsInParts()) {
                    string += " In Parts";
                } else {
                    string += " All at Once";
                }
            }
        } else {
            string = "I Want To Buy " + string;
        }
        return string;
    }

    public String getItemNameAndValue() {
        return Constants.DOLLAR_SIGN + Constants.FORMATTER.format(dollarPrice) + " " + itemName;

    }

    public boolean getIsSelling() {
        return isSelling;
    }

    public String getTranactionType() {
        String string;
        if (getIsSelling()) {
            string = "Selling ";
        } else {
            string = "Buying ";
        }
        return string+itemName;
    }
}