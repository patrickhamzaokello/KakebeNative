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
import com.shop.kakebe.KaKebe.Models.ResetPassword;
import com.shop.kakebe.KaKebe.Models.Result;

import java.lang.reflect.Type;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewPassword extends AppCompatActivity {

    ProgressBar addressprogressBar;
    ResetPassword resetPasswordModel = new ResetPassword();
    private ShopApiEndPoints shopApiEndPoints;
    Button reset_password;
    TextInputEditText verification_code,newpassword,confirmPassword;
    Result result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.hide();
        addressprogressBar = findViewById(R.id.progressBar);
        addressprogressBar.setVisibility(View.GONE);
        reset_password = findViewById(R.id.reset_password);
        verification_code = findViewById(R.id.verification_code);
        newpassword = findViewById(R.id.newpassword);
        confirmPassword = findViewById(R.id.confirmPassword);

        shopApiEndPoints = ShopAPIBase.getNewBase(getApplicationContext()).create(ShopApiEndPoints.class);
        reset_password.setOnClickListener(view -> SendResetCode(verification_code, newpassword, confirmPassword));
    }
    private void SendResetCode(TextInputEditText code,TextInputEditText password,TextInputEditText confirm_password) {

        String userCode = code.getText().toString().trim();
        String userPassword = password.getText().toString().trim();
        String userConfirm_password = confirm_password.getText().toString().trim();


        if (TextUtils.isEmpty(userCode)) {
            code.setError("Enter code that was sent to your email");
            code.requestFocus();
            addressprogressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(userPassword)) {
            password.setError("Enter New Password");
            password.requestFocus();
            addressprogressBar.setVisibility(View.GONE);
            return;

        } if (TextUtils.isEmpty(userConfirm_password)) {
            confirm_password.setError("Retype your password");
            confirm_password.requestFocus();
            addressprogressBar.setVisibility(View.GONE);
            return;
        }

        if (!userPassword.equals(userConfirm_password)) {
            confirm_password.setError("Passwords Does not Match");
            confirm_password.requestFocus();
            addressprogressBar.setVisibility(View.GONE);
            return;
        }


        resetPasswordModel.setVerificationCode(userCode);
        resetPasswordModel.setPassword(userPassword);

        addressprogressBar.setVisibility(View.VISIBLE);
        reset_password.setEnabled(false);
        reset_password.setClickable(false);



        postResetPassword().enqueue(new Callback<Result>() {


            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

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

//                         open forgot password screen
                        Intent intent = new Intent(NewPassword.this, LoginMaterial.class);
                        startActivity(intent);

                    } else {
                        reset_password.setEnabled(true);
                        reset_password.setClickable(true);
                        addressprogressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Result-"+result.getResult() + ", Message-"+result.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
                    addressprogressBar.setVisibility(View.GONE);
                    reset_password.setEnabled(true);
                    reset_password.setClickable(true);
                    return;

                }

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                addressprogressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Connection Failed!", Toast.LENGTH_SHORT).show();
                reset_password.setEnabled(true);
                reset_password.setClickable(true);
                t.printStackTrace();

            }
        });


    }

    private Call<Result> postResetPassword() {
        return shopApiEndPoints.postResetPassword(resetPasswordModel);
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