package com.exchainger.exchainger;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by OPEYEMI OLORUNLEKE on 10/3/2017.
 */

public class AddRequestDialog extends DialogFragment {


    private static final String MESSAGE_KEY = "message.key.dialog";
    public String message ;

    public static AddRequestDialog newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(MESSAGE_KEY, message);
        AddRequestDialog fragment = new AddRequestDialog();
        fragment.setArguments(args);
        return fragment;
    }

    private onClickListener mListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        Bundle bundle = getArguments();
        message = bundle.getString(MESSAGE_KEY);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.add_request_layout, null);
        TextView textView = (TextView) view.findViewById(R.id.text_view);
        textView.setText(message);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Add this Request?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onClickYes();
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

    public interface onClickListener {
        void onClickYes();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (onClickListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
