package com.shop.kakebe.KaKebe;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.shop.kakebe.KaKebe.Apis.ShopAPIBase;
import com.shop.kakebe.KaKebe.Apis.ShopApiEndPoints;
import com.shop.kakebe.KaKebe.Models.CreateAddressModel;
import com.shop.kakebe.KaKebe.Models.CreateAddressResponse;
import com.shop.kakebe.KaKebe.Models.ResetPassword;
import com.shop.kakebe.KaKebe.Models.Result;

import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPassword extends AppCompatActivity {

    ProgressBar addressprogressBar;
    ResetPassword resetPassword = new ResetPassword();
    private ShopApiEndPoints shopApiEndPoints;
    Button send_reset_code_btn;
    TextInputEditText inputuseremail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.hide();
        addressprogressBar = findViewById(R.id.progressBar);
        addressprogressBar.setVisibility(View.GONE);
        send_reset_code_btn = findViewById(R.id.send_reset_code);
        inputuseremail = findViewById(R.id.inputuseremail);
        shopApiEndPoints = ShopAPIBase.getNewBase(getApplicationContext()).create(ShopApiEndPoints.class);
        send_reset_code_btn.setOnClickListener(view -> SendResetCode(inputuseremail,"email"));
    }


    private void SendResetCode(TextInputEditText email_or_phone, String send_code_by) {

        String userEmail = email_or_phone.getText().toString().trim();


        addressprogressBar.setVisibility(View.VISIBLE);

        resetPassword.setEmailOrPhone(userEmail);
        resetPassword.setSendCodeBy(send_code_by);

        if (TextUtils.isEmpty(userEmail)) {
            email_or_phone.setError("Enter Email");
            email_or_phone.requestFocus();
            addressprogressBar.setVisibility(View.GONE);
            return;
        }

        postResetUserPassword().enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {


//                Log.d("weekly", "onResponse: "+ response);
//
//                Result result = response.body();
//                addressprogressBar.setVisibility(View.VISIBLE);
//
//                if (result != null) {
//                    //if no error- that is error = false
//                    if (!result.getResult()) {
//
//                        addressprogressBar.setVisibility(View.GONE);
//                        Toast.makeText(getApplicationContext(), "Address Saved", Toast.LENGTH_SHORT).show();
//                        finish();
//
//                    } else {
//                        send_reset_code_btn.setEnabled(true);
//                        send_reset_code_btn.setClickable(true);
//                        addressprogressBar.setVisibility(View.GONE);
//                        Toast.makeText(getApplicationContext(),
//                                 result.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//
//
//                } else {
//                    Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
//                    addressprogressBar.setVisibility(View.GONE);
//                    send_reset_code_btn.setEnabled(true);
//                    send_reset_code_btn.setClickable(true);
//                    return;
//
//                }

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                addressprogressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Connection Failed!", Toast.LENGTH_SHORT).show();
                send_reset_code_btn.setEnabled(true);
                send_reset_code_btn.setClickable(true);
                t.printStackTrace();

            }
        });


    }

    private Call<Result> postResetUserPassword() {
        return shopApiEndPoints.postForget_Request(resetPassword);
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