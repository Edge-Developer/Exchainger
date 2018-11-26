package com.exchainger.exchainger.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.exchainger.exchainger.Model.Constants;
import com.exchainger.exchainger.Model.TransactionRequest;
import com.exchainger.exchainger.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class BuyFragment extends Fragment {

    private static final String ACTION_BUTTON_TEXT = "SELL NOW";
    private static BuyItemListener mListener;
    private DatabaseReference buyRequestReference;
    private RecyclerView mRecyclerView;
    private RecyclerAdapter mBuyRecyclerAdapter;


    public BuyFragment() {
    }

    public static BuyFragment newInstance() {
        return new BuyFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_buy, container, false);
        mRecyclerView = view.findViewById(R.id.buyRequestRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        buyRequestReference = firebaseDatabase.getReference().child(Constants.FIREBASE_CHILD_BUY);
        Query buyQuery = buyRequestReference.orderByKey();
        FirebaseRecyclerOptions<TransactionRequest> sellRequestOptions = new FirebaseRecyclerOptions
                .Builder<TransactionRequest>()
                .setQuery(buyQuery, TransactionRequest.class)
                .build();
        mRecyclerView.setHasFixedSize(true);
        mBuyRecyclerAdapter = new RecyclerAdapter(sellRequestOptions);
        mRecyclerView.setAdapter(mBuyRecyclerAdapter);
        mBuyRecyclerAdapter.startListening();
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mBuyRecyclerAdapter.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (BuyItemListener) context;
    }

    public interface BuyItemListener {
        void onBuyItemClicked(TransactionRequest request, String transactionKey);
    }

    public static class BuyHolder extends RecyclerView.ViewHolder {
        private TextView nairaEquivalent;
        private TextView item_n_value;
        private TextView exchangeRate;
        private TextView transactionString;
        private Button actionBtn;


        public BuyHolder(View itemView, final OnButtonClickedListener listener) {
            super(itemView);
            item_n_value = itemView.findViewById(R.id.item_n_value);
            exchangeRate = itemView.findViewById(R.id.exchangeRate);
            nairaEquivalent = itemView.findViewById(R.id.nairaEquivalent);
            transactionString = itemView.findViewById(R.id.transaction_string);
            actionBtn = itemView.findViewById(R.id.action_button);
            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onButtonClicked(getAdapterPosition());
                }
            });
        }


        public interface OnButtonClickedListener {
            void onButtonClicked(int position);
        }
    }

    private class RecyclerAdapter extends FirebaseRecyclerAdapter<TransactionRequest, BuyHolder>
            implements BuyHolder.OnButtonClickedListener {

        public RecyclerAdapter(FirebaseRecyclerOptions<TransactionRequest> options) {
            super(options);
        }

        @Override
        public BuyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_request_layout, parent, false);
            return new BuyHolder(view, this);
        }

        @Override
        public void onButtonClicked(int position) {
            mListener.onBuyItemClicked(mBuyRecyclerAdapter.getItem(position), mBuyRecyclerAdapter.getRef(position).getKey());
        }

        @Override
        protected void onBindViewHolder(BuyHolder holder, int position, TransactionRequest model) {
            holder.actionBtn.setText(ACTION_BUTTON_TEXT);
            holder.nairaEquivalent.setText(model.getNairaEquivalent());
            holder.item_n_value.setText(model.getItemNameAndValue());
            holder.exchangeRate.setText(model.getExchangeRate());
            holder.transactionString.setText(model.getRequestString());
        }
    }
}