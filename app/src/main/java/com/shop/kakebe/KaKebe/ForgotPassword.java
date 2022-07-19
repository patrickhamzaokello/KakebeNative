package com.shop.kakebe.KaKebe;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shop.kakebe.KaKebe.Apis.ShopAPIBase;
import com.shop.kakebe.KaKebe.Apis.ShopApiEndPoints;
import com.shop.kakebe.KaKebe.Models.ForgotPasswordModel;
import com.shop.kakebe.KaKebe.Models.Result;

import java.lang.reflect.Type;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPassword extends AppCompatActivity {

    ProgressBar addressprogressBar;
    ForgotPasswordModel forgotPasswordModel = new ForgotPasswordModel();
    private ShopApiEndPoints shopApiEndPoints;
    Button send_reset_code_btn;
    TextInputEditText inputuseremail;
    Result result;

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
        send_reset_code_btn.setOnClickListener(view -> SendResetCode(inputuseremail, "email"));
    }


    private void SendResetCode(TextInputEditText email_or_phone, String send_code_by) {

        String userEmail = email_or_phone.getText().toString().trim();



        if (TextUtils.isEmpty(userEmail)) {
            email_or_phone.setError("Enter Email");
            email_or_phone.requestFocus();
            addressprogressBar.setVisibility(View.GONE);
            return;
        }

        addressprogressBar.setVisibility(View.VISIBLE);


        forgotPasswordModel.setEmailOrPhone(userEmail);
        forgotPasswordModel.setSendCodeBy(send_code_by);
        send_reset_code_btn.setEnabled(false);
        send_reset_code_btn.setClickable(false);

        postResetUserPassword().enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                addressprogressBar.setVisibility(View.VISIBLE);
                send_reset_code_btn.setEnabled(false);
                send_reset_code_btn.setClickable(false);

                switch (response.code()){
                    case 404:
                        Gson gson = new Gson();
                        Type type = new TypeToken<Result>() {
                        }.getType();
                        result = gson.fromJson(response.errorBody().charStream(), type);
//                        Toast.makeText(ForgotPassword.this, "ERROR " + result.getMessage(), Toast.LENGTH_SHORT).show();
                        break;

                    case 200:
//                        Toast.makeText(ForgotPassword.this, "200 success", Toast.LENGTH_SHORT).show();
                        // get response...
                        result = response.body();
                        break;

                    default:
//                        Toast.makeText(ForgotPassword.this, "Error: " + response.code() , Toast.LENGTH_SHORT).show();
                        // get response...
                        result = null;
                        break;

                }


                if (result != null) {
                    //if no error- that is error = false
                    if (result.getResult()) {
                        addressprogressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Result-"+result.getResult() + ", Message-"+result.getMessage(), Toast.LENGTH_SHORT).show();
                                    // open forgot password screen
                        Intent intent = new Intent(ForgotPassword.this, NewPassword.class);
                        startActivity(intent);

                    } else {
                        send_reset_code_btn.setEnabled(true);
                        send_reset_code_btn.setClickable(true);
                        addressprogressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Result-"+result.getResult() + ", Message-"+result.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
                    addressprogressBar.setVisibility(View.GONE);
                    send_reset_code_btn.setEnabled(true);
                    send_reset_code_btn.setClickable(true);
                    return;

                }

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
        return shopApiEndPoints.postForget_Request(forgotPasswordModel);
    }

    private void showErrorView(Throwable throwable) {
        Toast.makeText(getApplicationContext(), fetchErrorMessage(throwable), Toast.LENGTH_SHORT).show();
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