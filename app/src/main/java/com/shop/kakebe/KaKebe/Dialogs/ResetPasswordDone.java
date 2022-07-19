package com.shop.kakebe.KaKebe.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.shop.kakebe.KaKebe.LoginMaterial;
import com.shop.kakebe.KaKebe.R;

public class ResetPasswordDone extends DialogFragment {

    // Use this instance of the interface to deliver action events
    public Button loginButton;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_reset_password_done, null);
        loginButton = view.findViewById(R.id.reset_loginButton);
        builder.setView(view);
                // Add action buttons
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), LoginMaterial.class));
            }
        });

        return builder.create();
    }
}