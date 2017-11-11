package com.exchainger.exchainger.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.exchainger.exchainger.AddRequestDialog;
import com.exchainger.exchainger.Model.Constants;
import com.exchainger.exchainger.Model.TransactionRequest;
import com.exchainger.exchainger.R;

import java.text.DecimalFormat;

import static com.exchainger.exchainger.Activity.MainActivity.ADD_TRANS_REQUEST_CODE;
import static com.exchainger.exchainger.Activity.MainActivity.DIALOG_FRAGMENT_TAG;
import static com.exchainger.exchainger.Activity.MainActivity.DOLLAR_PRICE_KEY_RESULT;
import static com.exchainger.exchainger.Activity.MainActivity.EX_RATE_KEY_RESULT;
import static com.exchainger.exchainger.Activity.MainActivity.IS_GIFT_CARD_KEY_RESULT;
import static com.exchainger.exchainger.Activity.MainActivity.IS_IN_PARTS_KEY_RESULT;
import static com.exchainger.exchainger.Activity.MainActivity.IS_SELLING_KEY_RESULT;
import static com.exchainger.exchainger.Activity.MainActivity.ITEM_NAME_KEY_RESULT;

public class AddRequestActivity extends AppCompatActivity
        implements AddRequestDialog.onClickListener {
    private static final String IS_SELLING_KEY = "is.selling.key.addrequest";
    private final String[] supportedCryptos = {"Bitcoin", "Ethereum", "Ripple", "Litecoin", "Dash", "Steem", "Bitcoin Cash"};
    private final String[] supportedDigitals = {"Payooneer", "Neteller", "Skrill", "Perfect Money", "Wells Fargo"};
    //private final String[] supportedGiftCards = {"iTunes", "Amazon", "eBay", "BestBuy", "Kohls", "Walmart"};
    private Spinner spinner;
    private boolean isSelling;
    private RadioButton yesSellAtOnceRadioBtn;
    private RadioButton dontSellAtOnceRadioBtn;
    private TextInputEditText dollarPriceEditTxt;
    private TextInputEditText exchangeRateEditTxt;
    private TransactionRequest request;
    private final DecimalFormat mFormat = new DecimalFormat("#,###");
    public static Intent getIntent(Context c, boolean isSelling) {
        Intent intent = new Intent(c, AddRequestActivity.class);
        intent.putExtra(IS_SELLING_KEY, isSelling);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_request);

        Intent intent = getIntent();
        isSelling = intent.getBooleanExtra(IS_SELLING_KEY, false);

        RadioButton buy = (RadioButton) findViewById(R.id.buy);
        RadioButton sell = (RadioButton) findViewById(R.id.sell);
        final TextView textView = (TextView) findViewById(R.id.itemPrice);

        if (isSelling)
            sell.setChecked(true);
        else
            buy.setChecked(true);


        sell.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleGroupOfYes(isChecked);
                isSelling = isChecked;
            }
        });
        RadioButton cryptoRadioBtn = (RadioButton) findViewById(R.id.crypto);
        cryptoRadioBtn.setChecked(true);
        spinner = (Spinner) findViewById(R.id.spinner);
        updateSpinner(supportedCryptos);
        cryptoRadioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) updateSpinner(supportedCryptos);
                else updateSpinner(supportedDigitals);
            }
        });

        yesSellAtOnceRadioBtn = (RadioButton) findViewById(R.id.yes_sell_once);
        dontSellAtOnceRadioBtn = (RadioButton) findViewById(R.id.no_donot_sell_once);
        yesSellAtOnceRadioBtn.setChecked(true);

        toggleGroupOfYes(isSelling);

        dollarPriceEditTxt = (TextInputEditText) findViewById(R.id.dollarPrice);
        exchangeRateEditTxt = (TextInputEditText) findViewById(R.id.exchangeRate);


        exchangeRateEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1 || dollarPriceEditTxt.getText().toString().length() < 1)
                    return;
                int eRate = Integer.parseInt(s.toString());
                int dolPrice = Integer.parseInt(dollarPriceEditTxt.getText().toString());
                int total_price = eRate * dolPrice;
                textView.setText(Constants.NAIRA_SIGN+mFormat.format(total_price));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        dollarPriceEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 1 || exchangeRateEditTxt.getText().toString().length() < 1)
                    return;
                int dolPrice = Integer.parseInt(s.toString());
                int eRate = Integer.parseInt(exchangeRateEditTxt.getText().toString());
                int total_price = eRate * dolPrice;
                textView.setText(Constants.NAIRA_SIGN+mFormat.format(total_price));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.submit_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dollarPrice = dollarPriceEditTxt.getText().toString();
                String exchangeRate = exchangeRateEditTxt.getText().toString();
                if (dollarPrice.length() < 1) {
                    dollarPriceEditTxt.setError("");
                    return;
                } else
                    dollarPriceEditTxt.setError(null);
                if (exchangeRate.length() < 2) {
                    exchangeRateEditTxt.setError("");
                    return;
                } else
                    exchangeRateEditTxt.setError(null);

                String itemName = spinner.getSelectedItem().toString();
                boolean isInParts = dontSellAtOnceRadioBtn.isChecked();
                int dollPrice = Integer.parseInt(dollarPrice);
                int exRate = Integer.parseInt(exchangeRate);
                request = new TransactionRequest(isSelling, false, exRate, dollPrice, isInParts, itemName);
                if ((exRate * dollPrice) < 5000) {
                    Toast.makeText(AddRequestActivity.this, "Minimum of " + Constants.NAIRA_SIGN + "5,000", Toast.LENGTH_LONG).show();
                    return;
                }
                AddRequestDialog.newInstance(request.getRequestString()).show(getSupportFragmentManager(), DIALOG_FRAGMENT_TAG);

            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void toggleGroupOfYes(boolean isSelling) {
        if (isSelling) {
            yesSellAtOnceRadioBtn.setEnabled(true);
            dontSellAtOnceRadioBtn.setEnabled(true);
        } else {
            yesSellAtOnceRadioBtn.setChecked(true);
            dontSellAtOnceRadioBtn.setChecked(false);
            yesSellAtOnceRadioBtn.setEnabled(false);
        }
    }

    private void updateSpinner(String[] strings) {
        spinner.setAdapter(
                new ArrayAdapter<>(
                        this
                        , android.R.layout.simple_spinner_dropdown_item
                        , strings)
        );

    }

    @Override
    public void onClickYes() {
        Intent intent = new Intent();
        intent.putExtra(IS_SELLING_KEY_RESULT, request.getIsSelling());
        intent.putExtra(IS_GIFT_CARD_KEY_RESULT, false);
        intent.putExtra(IS_IN_PARTS_KEY_RESULT, request.getIsInParts());
        intent.putExtra(EX_RATE_KEY_RESULT, request.getExchangePrice());
        intent.putExtra(DOLLAR_PRICE_KEY_RESULT, request.getDollarPrice());
        intent.putExtra(ITEM_NAME_KEY_RESULT, request.getItemName());
        setResult(ADD_TRANS_REQUEST_CODE, intent);
        finish();
    }
}
