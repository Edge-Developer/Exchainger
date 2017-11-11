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

public class SellFragment extends Fragment {
    private static final String ACTION_BUTTON_TEXT = "BUY NOW";
    private static SellItemListener mListener;
    private RecyclerAdapter mSellRecyclerAdapter;
    private DatabaseReference sellRequestReference;
    private RecyclerView mRecyclerView;


    public SellFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_sell, container, false);

        mRecyclerView = view.findViewById(R.id.sell_request_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        sellRequestReference = firebaseDatabase.getReference().child(Constants.FIREBASE_CHILD_SELL);
        Query sellQuery = sellRequestReference.orderByKey();
        FirebaseRecyclerOptions<TransactionRequest> sellRequestOptions = new FirebaseRecyclerOptions
                .Builder<TransactionRequest>()
                .setQuery(sellQuery, TransactionRequest.class)
                .build();

        mSellRecyclerAdapter = new RecyclerAdapter(sellRequestOptions);
        mRecyclerView.setAdapter(mSellRecyclerAdapter);
        mSellRecyclerAdapter.startListening();

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
        mListener = (SellItemListener) context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mSellRecyclerAdapter.stopListening();
    }

    public interface SellItemListener {
        void onSellItemClicked(TransactionRequest request, String key);
    }

    public static class SellHolder extends RecyclerView.ViewHolder {
        private TextView nairaEquivalent;
        private TextView item_n_value;
        private TextView exchangeRate;
        private TextView transactionString;
        private Button actionBtn;

        public SellHolder(View itemView, final OnButtonClickedListener clickListener) {
            super(itemView);
            item_n_value = itemView.findViewById(R.id.item_n_value);
            exchangeRate = itemView.findViewById(R.id.exchangeRate);
            nairaEquivalent = itemView.findViewById(R.id.nairaEquivalent);
            transactionString = itemView.findViewById(R.id.transaction_string);
            actionBtn = itemView.findViewById(R.id.action_button);
            actionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onButtonClicked(getAdapterPosition());
                }
            });
        }

        public interface OnButtonClickedListener {
            void onButtonClicked(int position);
        }
    }

    private class RecyclerAdapter extends FirebaseRecyclerAdapter<TransactionRequest, SellHolder>
            implements SellHolder.OnButtonClickedListener {

        public RecyclerAdapter(FirebaseRecyclerOptions<TransactionRequest> options) {
            super(options);
        }

        @Override
        public SellHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_request_layout, parent, false);
            return new SellHolder(view, this);
        }

        @Override
        public void onButtonClicked(int position) {
            mListener.onSellItemClicked(mSellRecyclerAdapter.getItem(position), mSellRecyclerAdapter.getRef(position).getKey());
        }

        @Override
        protected void onBindViewHolder(SellHolder holder, int position, TransactionRequest model) {
            holder.actionBtn.setText(ACTION_BUTTON_TEXT);
            holder.nairaEquivalent.setText(model.getNairaEquivalent());
            holder.item_n_value.setText(model.getItemNameAndValue());
            holder.exchangeRate.setText(model.getExchangeRate());
            holder.transactionString.setText(model.getRequestString());
        }
    }
}
