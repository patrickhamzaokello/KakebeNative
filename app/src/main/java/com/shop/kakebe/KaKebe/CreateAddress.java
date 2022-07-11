package com.shop.kakebe.KaKebe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.shop.kakebe.KaKebe.Apis.ShopAPIBase;
import com.shop.kakebe.KaKebe.Apis.ShopApiEndPoints;
import com.shop.kakebe.KaKebe.HelperClasses.SharedPrefManager;
import com.shop.kakebe.KaKebe.Models.CreateAddressModel;
import com.shop.kakebe.KaKebe.Models.CreateAddressResponse;
import com.shop.kakebe.KaKebe.Models.User;
import com.shop.kakebe.KaKebe.localDatabase.SenseDBHelper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAddress extends AppCompatActivity {

    String[] cities;
    AutoCompleteTextView autoCompleteTextView;
    ArrayAdapter<String> adapterItems;
    ProgressBar addressprogressBar;
    CreateAddressModel createAddressModel = new CreateAddressModel();
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private int userId;

    private ShopApiEndPoints shopApiEndPoints;
    private SenseDBHelper db;
    TextInputEditText user_phone, user_location;
    Button save_address;
    String Area;
    ActionBar actionBar;
    MaterialButton create_cancel_btn;
    private GpsTracker gpsTracker;
    double latitude = 0;
    double longitude = 0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_address);

        actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.setTitle("New Delivery Address");
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        autoCompleteTextView = findViewById(R.id.auto_complete_txt);
        user_phone = findViewById(R.id.user_phone);
        user_location = findViewById(R.id.user_location);
        addressprogressBar = findViewById(R.id.main_progress);
        save_address = findViewById(R.id.save_address);
        create_cancel_btn = findViewById(R.id.create_cancel_btn);

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

        create_cancel_btn.setOnClickListener(view -> finish());


        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        gpsTracker = new GpsTracker(CreateAddress.this);
        getLocation();

    }

    public void getLocation(){
        if(gpsTracker.canGetLocation()){
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }else{
            gpsTracker.showSettingsAlert();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void AddUserAddress(String area, TextInputEditText user_location, TextInputEditText user_phone) {

        String location = user_location.getText().toString().trim();
        String phone = user_phone.getText().toString().trim();


        addressprogressBar.setVisibility(View.VISIBLE);

        createAddressModel.setUserId(String.valueOf(userId));
        createAddressModel.setPhone(phone);
        createAddressModel.setLocation(location);
        createAddressModel.setDistrict(area);
        createAddressModel.setLatitude(latitude);
        createAddressModel.setLongitude(longitude);

        if(gpsTracker.canGetLocation()){
            if (TextUtils.isEmpty(area)) {
                autoCompleteTextView.setError("Choose Area");
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
                user_phone.setError("Use format 07xxxxxxxx ");
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

                    CreateAddressResponse createAddressResponse = response.body();
                    addressprogressBar.setVisibility(View.VISIBLE);


                    if (createAddressResponse != null) {

                        //if no error- that is error = false
                        if (!createAddressResponse.getError()) {

                            addressprogressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(), "Address Saved", Toast.LENGTH_SHORT).show();
                            finish();

                        } else {
                            save_address.setEnabled(true);
                            save_address.setClickable(true);
                            addressprogressBar.setVisibility(View.GONE);
                            Toast.makeText(getApplicationContext(),
                                    "Adding Address Failed: " + createAddressResponse.getError(), Toast.LENGTH_SHORT).show();
                        }


                    } else {
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
        }else{
            addressprogressBar.setVisibility(View.GONE);
            gpsTracker.showSettingsAlert();
            return;
        }


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