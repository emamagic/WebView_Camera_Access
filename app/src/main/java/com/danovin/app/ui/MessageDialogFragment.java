package com.danovin.app.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

public class MessageDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE_RES_ID = "message_res_id";

    public static MessageDialogFragment newInstance(@StringRes int message) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE_RES_ID, message);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setMessage(getArguments().getInt(ARG_MESSAGE_RES_ID))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> ((Listener) getParentFragment()).onOkClicked())
                .create();
    }

    interface Listener {
        void onOkClicked();
    }

}