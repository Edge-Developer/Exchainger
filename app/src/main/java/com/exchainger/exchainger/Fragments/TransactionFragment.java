package com.exchainger.exchainger.Fragments;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.exchainger.exchainger.Model.Constants;
import com.exchainger.exchainger.R;
import com.exchainger.exchainger.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TransactionFragment extends Fragment {

    private static TransactionItem mListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;


    public TransactionFragment() {
    }

    public static TransactionFragment newInstance() {
        return new TransactionFragment();
    }

    private static CharSequence formatDate(long date) {
        return DateFormat.format("h:mm a d/MMM/yy", date);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser != null)
            mDatabaseReference = mFirebaseDatabase
                    .getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(mUser.getUid())
                    .child(Constants.FIREBASE_CHILD_TRANSACTIONS);
        else
            mDatabaseReference = mFirebaseDatabase.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.transactionRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
/*
        FirebaseRecyclerAdapter<Transaction, TransactionHolder> mRecyclerAdapter = new FirebaseRecyclerAdapter<Transaction, TransactionHolder>(
                Transaction.class,
                R.layout.transaction_layout,
                TransactionHolder.class,
                mDatabaseReference) {
            @Override
            protected void populateViewHolder(TransactionHolder viewHolder, Transaction model, int position) {
                viewHolder.bind(model, model.getTransactionKey());
            }
        };

        recyclerView.setAdapter(mRecyclerAdapter);*/
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (TransactionItem) context;
    }

    public interface TransactionItem {
        void onTransactionItemClick(String key, String transactionId);
    }

    public static class TransactionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView transPrice;
        private TextView transDate;
        private TextView transStatus;
        private TextView transType;
        private String transactionKey;

        public TransactionHolder(View itemView) {
            super(itemView);
            transType = (TextView) itemView.findViewById(R.id.transaction_type);
            transDate = (TextView) itemView.findViewById(R.id.transaction_date);
            transPrice = (TextView) itemView.findViewById(R.id.transaction_price);
            transStatus = (TextView) itemView.findViewById(R.id.transaction_status);
            itemView.setOnClickListener(this);
        }

        private void bind(Transaction model, String key) {
            transactionKey = key;
            transType.setText(model.getType());
            String tStatus;
            int tColor;
            if (model.getIsOpen()) {
                tStatus = "In Progress";
                tColor = Color.parseColor("#fb8c00");
            } else {
                tStatus = "Completed";
                tColor = Color.parseColor("#ff0f9d58");
            }
            transStatus.setText(tStatus);
            transStatus.setBackgroundColor(tColor);
            transDate.setText(formatDate(model.getTime()));
            transPrice.setText(model.getNairaEquivalent());
        }

        @Override
        public void onClick(View v) {
            String str = transType.getText().toString();
            String T_USER_ID; // Transaction User ID
            if (str.contains("Selling")) T_USER_ID = Constants.BUYER_ID;
            else T_USER_ID = Constants.SELLER_ID;

            mListener.onTransactionItemClick(transactionKey, T_USER_ID);
        }
    }

}