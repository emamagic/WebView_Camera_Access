package com.danovin.app.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.danovin.app.R;


public class ConfirmationDialogFragment extends DialogFragment {

    private static final String ARG_RESOURCES = "resources";


    public static ConfirmationDialogFragment newInstance(String[] resources) {
        ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_RESOURCES, resources);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] resources = getArguments().getStringArray(ARG_RESOURCES);
        return new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.confirmation, TextUtils.join("\n", resources)))
                .setNegativeButton(R.string.deny, (dialog, which) -> ((Listener) getActivity()).onConfirmation(false, resources))
                .setPositiveButton(R.string.allow, (dialog, which) -> ((Listener) getActivity()).onConfirmation(true, resources))
                .create();
    }


    interface Listener {
        void onConfirmation(boolean allowed, String[] resources);
    }

}