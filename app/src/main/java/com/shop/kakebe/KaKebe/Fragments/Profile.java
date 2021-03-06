package com.shop.kakebe.KaKebe.Fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.shop.kakebe.KaKebe.CreateAddress;
import com.shop.kakebe.KaKebe.HelperClasses.SharedPrefManager;
import com.shop.kakebe.KaKebe.LoginMaterial;
import com.shop.kakebe.KaKebe.ManageOrders;
import com.shop.kakebe.KaKebe.Models.User;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.ViewDeliveryAddress;
import com.shop.kakebe.KaKebe.localDatabase.CartDBManager;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class Profile extends Fragment {

    TextView textViewUsername, textViewEmail, full_name_text, card_email_text, card_phone_text;
    MaterialButton manageOrders,addNewAddress,view_address;
    private CartDBManager db;
    private int userId;


    public Profile() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (!SharedPrefManager.getInstance(getContext()).isLoggedIn()) {
//            finish();
            startActivity(new Intent(getContext(), LoginMaterial.class));
        }


        textViewUsername = view.findViewById(R.id.full_name);
        textViewEmail = view.findViewById(R.id.email_text);


        full_name_text = view.findViewById(R.id.full_name_text);
        card_email_text = view.findViewById(R.id.card_email_text);
        card_phone_text = view.findViewById(R.id.card_phone_text);
        manageOrders = view.findViewById(R.id.manageOrders);

        addNewAddress = view.findViewById(R.id.addNewAddress);
        view_address = view.findViewById(R.id.view_address);

        User user = SharedPrefManager.getInstance(getContext()).getUser();
        db = new CartDBManager(getContext());

        try {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        full_name_text.setText(user.getFullname());
        card_email_text.setText(user.getEmail());
        card_phone_text.setText(user.getPhone());
        userId = user.getId();


        view.findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Logout!")
                        .setContentText("You will be required to Sign in next time")
                        .setConfirmText("OK")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {

                                sDialog.dismissWithAnimation();

                                SharedPrefManager.getInstance(getContext()).logout();
                                db.clearCart();
                            }
                        }).show();

            }
        });

        manageOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getContext(), ManageOrders.class);
                startActivity(i);
            }
        });


        addNewAddress.setOnClickListener(v -> createNewAddress());
        view_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), ViewDeliveryAddress.class);
                startActivity(i);
            }
        });

        return view;
    }

    private void createNewAddress() {
        Intent i = new Intent(getContext(), CreateAddress.class);
        startActivity(i);
    }
}