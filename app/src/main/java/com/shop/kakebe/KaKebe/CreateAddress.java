package com.shop.kakebe.KaKebe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.shop.kakebe.KaKebe.Apis.ShopAPIBase;
import com.shop.kakebe.KaKebe.Apis.ShopApiEndPoints;
import com.shop.kakebe.KaKebe.HelperClasses.SharedPrefManager;
import com.shop.kakebe.KaKebe.Models.Address;
import com.shop.kakebe.KaKebe.Models.CreateAddressModel;
import com.shop.kakebe.KaKebe.Models.CreateAddressResponse;
import com.shop.kakebe.KaKebe.Models.User;
import com.shop.kakebe.KaKebe.Models.UserAddress;
import com.shop.kakebe.KaKebe.localDatabase.SenseDBHelper;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAddress extends AppCompatActivity {

    String[] items = {"Material", "Design", "Components", "Andorid", "5.0 Lollipop", "Components", "Andorid", "5.0 Lollipop", "Components", "Andorid", "5.0 Lollipop"};
    String[] cities;
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    ProgressBar addressprogressBar;
    CreateAddressModel createAddressModel = new CreateAddressModel();
    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private static int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;
    private int userId;

    private ShopApiEndPoints shopApiEndPoints;
    private SenseDBHelper db;
    TextInputEditText user_phone, user_location;
    Button save_address;
    String Area;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_address);
        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        user_phone = findViewById(R.id.user_phone);
        user_location = findViewById(R.id.user_location);
        addressprogressBar = findViewById(R.id.main_progress);
        save_address = findViewById(R.id.save_address);

        addressprogressBar.setVisibility(View.GONE);
        User user = SharedPrefManager.getInstance(CreateAddress.this).getUser();
        userId = user.getId();


        shopApiEndPoints = ShopAPIBase.getClient(getApplicationContext()).create(ShopApiEndPoints.class);


        save_address.setOnClickListener(view -> AddUserAddress(Area, user_location, user_phone));

        loadFirstPage();

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Area = parent.getItemAtPosition(position).toString();
            }
        });

    }

    private void AddUserAddress(String area, TextInputEditText user_location, TextInputEditText user_phone) {

        String location = user_location.getText().toString().trim();
        String phone = user_phone.getText().toString().trim();

        Log.w("AddUserAddress", area + location + phone);

        addressprogressBar.setVisibility(View.VISIBLE);

        createAddressModel.setUserId(String.valueOf(userId));
        createAddressModel.setPhone(phone);
        createAddressModel.setLocation(location);
        createAddressModel.setDistrict(area);


        if (TextUtils.isEmpty(area)) {
            autoCompleteTextView.setError("Choose Version");
            autoCompleteTextView.requestFocus();
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
                        Toast.makeText(getApplicationContext(), "Address Saved", Toast.LENGTH_SHORT).show();

                    } else {
                        Log.i("Ress", "message: " + (createAddressResponse.getMessage()));
                        Log.i("et", "error false: " + (createAddressResponse.getError()));
                        save_address.setEnabled(true);
                        save_address.setClickable(true);
                        addressprogressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Adding Address Failed", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Log.i("Address Response null", "Address is null Try Again: " + createAddressResponse);
                    Toast.makeText(getApplicationContext(), "Adding Address Failed", Toast.LENGTH_SHORT).show();
                    addressprogressBar.setVisibility(View.GONE);
                    save_address.setEnabled(true);
                    save_address.setClickable(true);
                    return;

                }

            }

            @Override
            public void onFailure(Call<CreateAddressResponse> call, Throwable t) {
                addressprogressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Address Can't be Added now, Try again!", Toast.LENGTH_SHORT).show();
                save_address.setEnabled(true);
                save_address.setClickable(true);
                t.printStackTrace();

            }
        });


    }

    private void loadFirstPage() {

        // To ensure list is visible when retry button in error view is clicked

        getCities().enqueue(new Callback<String[]>() {
            @Override
            public void onResponse(Call<String[]> call, Response<String[]> response) {

                // Got data. Send it to adapter
                cities = fetchCitiesResults(response);

                if (cities != null) {
                    adapterItems = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_item, cities);
                    autoCompleteTextView.setAdapter(adapterItems);
                } else {
//                    showErrorView(t);
                }


            }

            @Override
            public void onFailure(Call<String[]> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }


    private String[] fetchCitiesResults(Response<String[]> response) {
        String[] cities = response.body();
        return cities;
    }


    private Call<CreateAddressResponse> postCreateUserAddress() {
        return shopApiEndPoints.postCreateAddress(createAddressModel);
    }


    private Call<String[]> getCities() {
        return shopApiEndPoints.getCities();
    }


    private void showErrorView(Throwable throwable) {

//        if (errorLayout.getVisibility() == View.GONE) {
//            errorLayout.setVisibility(View.VISIBLE);
//            add_address_layout.setVisibility(View.GONE);
//            progressBar.setVisibility(View.GONE);
//
//            txtError.setText(fetchErrorMessage(throwable));
//        }
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
    }

}