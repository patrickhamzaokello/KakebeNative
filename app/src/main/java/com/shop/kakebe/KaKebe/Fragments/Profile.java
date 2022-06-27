package com.shop.kakebe.KaKebe.Fragments;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.shop.kakebe.KaKebe.Apis.ShopAPIBase;
import com.shop.kakebe.KaKebe.Apis.ShopApiEndPoints;
import com.shop.kakebe.KaKebe.HelperClasses.SharedPrefManager;
import com.shop.kakebe.KaKebe.LoginMaterial;
import com.shop.kakebe.KaKebe.ManageOrders;
import com.shop.kakebe.KaKebe.Models.CreateAddress;
import com.shop.kakebe.KaKebe.Models.CreateAddressResponse;
import com.shop.kakebe.KaKebe.Models.User;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.localDatabase.SenseDBHelper;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Profile extends Fragment  {


    TextView textViewUsername, textViewEmail, full_name_text, card_email_text, card_phone_text;


    MaterialCardView manageOrders;



    Button  addNewAddress;
    ProgressBar addressprogressBar;
    private static final int PAGE_START = 1;
    CreateAddress createAddress = new CreateAddress();

    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private static int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;
    private int userId;


    private ShopApiEndPoints shopApiEndPoints;
    private SenseDBHelper db;


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

        //if the user is not logged in
        //starting the login activity
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

        addNewAddress = view.findViewById(R.id.addnewAddress);


        //getting the current user
        User user = SharedPrefManager.getInstance(getContext()).getUser();
        db = new SenseDBHelper(getContext());
//        userModel.getId();

        //setting the values to the textviews


        full_name_text.setText(user.getFullname());
        card_email_text.setText(user.getEmail());
        card_phone_text.setText(user.getPhone());
        userId = user.getId();


        //when the user presses logout button
        //calling the logout method
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

                Log.i("show past order ", "orders");
                Intent i = new Intent(getContext(), ManageOrders.class);
                startActivity(i);
            }
        });



        addNewAddress.setOnClickListener(v -> createNewAddress());
        shopApiEndPoints = ShopAPIBase.getClient(getContext()).create(ShopApiEndPoints.class);


        return view;
    }

    private void createNewAddress() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_add_address_round_dialog);

        TextInputEditText user_phone, user_location, user_district;
        Button save_address;


        user_phone = dialog.findViewById(R.id.user_phone);
        user_location = dialog.findViewById(R.id.user_location);
        user_district = dialog.findViewById(R.id.user_district);
        save_address = dialog.findViewById(R.id.save_address);
        addressprogressBar = dialog.findViewById(R.id.addressprogressBar);



        save_address.setOnClickListener(v -> AddUserAddress(save_address,dialog, user_phone, user_location, user_district));


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void AddUserAddress(Button save_address, Dialog dialog, TextInputEditText user_phone, TextInputEditText user_location, TextInputEditText user_district) {
        final String phone = user_phone.getText().toString().trim();
        final String location = user_location.getText().toString().trim();
        final String district = user_district.getText().toString().trim();
        addressprogressBar.setVisibility(View.VISIBLE);


        createAddress.setUserId(String.valueOf(userId));
        createAddress.setPhone(phone);
        createAddress.setLocation(location);
        createAddress.setDistrict(district);

        if (TextUtils.isEmpty(district)) {
            user_district.setError("Specify your District");
            user_district.requestFocus();
            addressprogressBar.setVisibility(View.GONE);

            return;
        }


        if (TextUtils.isEmpty(location)) {
            user_location.setError("Provide your location");
            user_location.requestFocus();
            addressprogressBar.setVisibility(View.GONE);

            return;
        }

        if (phone.length() < 9) {
            user_phone.setError("Invalid Phone number");
            user_phone.requestFocus();
            addressprogressBar.setVisibility(View.GONE);

            return;
        }

        if (phone.length() > 10) {
            user_phone.setError("Use format 07xxxxxxxx ");
            user_phone.requestFocus();
            addressprogressBar.setVisibility(View.GONE);

            return;
        }

        postCreateUserAddress().enqueue(new Callback<CreateAddressResponse>() {
            @Override
            public void onResponse(Call<CreateAddressResponse> call, Response<CreateAddressResponse> response) {

                //set response body to match OrderResponse Model
                CreateAddressResponse createAddressResponse = response.body();
                addressprogressBar.setVisibility(View.VISIBLE);


                //if orderResponses is not null
                if (createAddressResponse != null) {

                    //if no error- that is error = false
                    if (!createAddressResponse.getError()) {

                        Log.i("Address Success", createAddressResponse.getMessage() + createAddressResponse.getError());
                        addressprogressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Address Saved", Toast.LENGTH_SHORT).show();
                        dialog.hide();

                    } else {
                        Log.i("Ress", "message: " + (createAddressResponse.getMessage()));
                        Log.i("et", "error false: " + (createAddressResponse.getError()));
                        save_address.setEnabled(true);
                        save_address.setClickable(true);
                        addressprogressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Adding Address Failed", Toast.LENGTH_SHORT).show();


//                        ShowOrderFailed();

                    }


                } else {
                    Log.i("Address Response null", "Address is null Try Again: " + createAddressResponse);
                    Toast.makeText(getContext(), "Adding Address Failed", Toast.LENGTH_SHORT).show();
                    addressprogressBar.setVisibility(View.GONE);
                    save_address.setEnabled(true);
                    save_address.setClickable(true);
                    return;

                }

            }

            @Override
            public void onFailure(Call<CreateAddressResponse> call, Throwable t) {
                addressprogressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Address Can't be Added now, Try again!", Toast.LENGTH_SHORT).show();

                save_address.setEnabled(true);
                save_address.setClickable(true);
                t.printStackTrace();

            }
        });


    }


    private Call<CreateAddressResponse> postCreateUserAddress() {
        return shopApiEndPoints.postCreateAddress(createAddress);
    }




}