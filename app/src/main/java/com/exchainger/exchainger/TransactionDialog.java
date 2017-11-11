package com.exchainger.exchainger;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.exchainger.exchainger.Model.Constants;

import static com.exchainger.exchainger.Model.Constants.FORMATTER;

/**
 * Created by OPEYEMI OLORUNLEKE on 9/14/2017.
 */

public class TransactionDialog extends DialogFragment {

    public static final String PARTS_KEY = "parts_key";
    public static final String EXCHANGE_RATE_KEY = "exch_rate_key";
    public static final String QUANTITY_KEY = "quantity_key";
    public static final String PRICE_KEY = "price_key";
    public static final String ITEM_NAME_AND_VALUE_KEY = "item.name.value_key";
    public static final String IS_BUYING_KEY = "is.buying_key";
    private static final String TAG = "TransactionDialog";
    int totalPrice;
    int serviceCharge;
    private DialogListener mListener;
    private TextInputEditText quantityEdtTxt;
    private TextView eRateTxtView;
    private TextView quantityTxtView;
    private TextView priceTxtView;
    private TextView totalPriceTxtView;
    private TextView finalMsgTxtView;
    private TextView serviceChargeTxtView;
    private String itemNameAndPrice;
    private boolean isPart;
    private int exchangeRate;
    private int maxQuantity;
    private int price;
    private boolean isSelling;
    private TextWatcher mWatcher;

    public TransactionDialog() {
    }

    public static TransactionDialog newInstance(String itemNameValue, boolean isInParts, int exchangeRate, int quantity, int price, boolean isSelling) {
        Bundle args = new Bundle();
        args.putBoolean(PARTS_KEY, isInParts);
        args.putBoolean(IS_BUYING_KEY, isSelling);
        args.putString(ITEM_NAME_AND_VALUE_KEY, itemNameValue);
        args.putInt(EXCHANGE_RATE_KEY, exchangeRate);
        args.putInt(QUANTITY_KEY, quantity);
        args.putInt(PRICE_KEY, price);
        TransactionDialog fragment = new TransactionDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        itemNameAndPrice = bundle.getString(ITEM_NAME_AND_VALUE_KEY);
        isPart = bundle.getBoolean(PARTS_KEY);
        exchangeRate = bundle.getInt(EXCHANGE_RATE_KEY);
        maxQuantity = bundle.getInt(QUANTITY_KEY);
        price = bundle.getInt(PRICE_KEY);
        isSelling = bundle.getBoolean(IS_BUYING_KEY);

        mWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() < 1) {
                    price = 0;
                    serviceCharge = (int) (price * 0.0119);
                    totalPrice = serviceCharge + price;
                    quantityTxtView.setText("0");
                    priceTxtView.setText("" + price);
                    serviceChargeTxtView.setText("" + serviceCharge);
                    totalPriceTxtView.setText("" + totalPrice);
                    finalMsgTxtView.setText(getContext().getString(R.string.proceed_to_pay, Constants.NAIRA_SIGN + FORMATTER.format(totalPrice)));
                    return;
                }
                int val = Integer.parseInt(s.toString());
                if (val > maxQuantity) {
                    quantityTxtView.setText("");
                    priceTxtView.setText("");
                    serviceChargeTxtView.setText("");
                    finalMsgTxtView.setText("");
                    totalPriceTxtView.setText("");
                    quantityEdtTxt.setError("Maximum of " + maxQuantity);
                } else {
                    quantityEdtTxt.setError(null);
                    price = val * exchangeRate;
                    serviceCharge = (int) (price * 0.0119);
                    totalPrice = serviceCharge + price;
                    quantityTxtView.setText("" + val);
                    priceTxtView.setText("" + price);
                    serviceChargeTxtView.setText("" + serviceCharge);
                    totalPriceTxtView.setText("" + totalPrice);
                    finalMsgTxtView.setText(getContext().getString(R.string.proceed_to_pay, Constants.NAIRA_SIGN + FORMATTER.format(totalPrice)));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.transaction_dailog, null);

        finalMsgTxtView = (TextView) view.findViewById(R.id.final_msg_txt);
        serviceChargeTxtView = (TextView) view.findViewById(R.id.service_charge);
        quantityEdtTxt = (TextInputEditText) view.findViewById(R.id.quantity_edit_txt);
        eRateTxtView = (TextView) view.findViewById(R.id.eRate);
        quantityTxtView = (TextView) view.findViewById(R.id.quantity_txt_view);
        priceTxtView = (TextView) view.findViewById(R.id.price);
        totalPriceTxtView = (TextView) view.findViewById(R.id.total_price);

        serviceCharge = (int) (0.0119 * price);

        if(isSelling) {
            totalPrice = price - serviceCharge;
            finalMsgTxtView.setText(getContext().getString(R.string.proceed_to_receive, Constants.NAIRA_SIGN + FORMATTER.format(totalPrice)));
            quantityEdtTxt.setVisibility(View.GONE);
        }else {
            totalPrice =  price + serviceCharge;
            finalMsgTxtView.setText(getContext().getString(R.string.proceed_to_pay, Constants.NAIRA_SIGN + FORMATTER.format(totalPrice)));
            if (!isPart) {
                quantityEdtTxt.setVisibility(View.GONE);
            } else {
                quantityEdtTxt.setVisibility(View.VISIBLE);
            }
        }

        quantityEdtTxt.setText("" + maxQuantity);
        eRateTxtView.setText("" + exchangeRate);
        quantityTxtView.setText("" + maxQuantity);
        priceTxtView.setText("" + price);
        serviceChargeTxtView.setText("" + serviceCharge);
        totalPriceTxtView.setText("" + totalPrice);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(itemNameAndPrice)
                .setPositiveButton(getContext().getString(R.string.proceed), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //mListener.clickedOkDialog(message);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }

    @Override
    public void onPause() {
        super.onPause();
        quantityEdtTxt.removeTextChangedListener(mWatcher);
    }

    @Override
    public void onResume() {
        super.onResume();
        quantityEdtTxt.addTextChangedListener(mWatcher);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (DialogListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface DialogListener {
        void clickedOkDialog(String message);
    }
}
