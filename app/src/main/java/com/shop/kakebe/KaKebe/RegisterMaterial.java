package com.shop.kakebe.KaKebe;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.shop.kakebe.KaKebe.HelperClasses.SharedPrefManager;
import com.shop.kakebe.KaKebe.HttpRequests.RequestHandler;
import com.shop.kakebe.KaKebe.HttpRequests.URLs;
import com.shop.kakebe.KaKebe.Models.User;
import com.shop.kakebe.KaKebe.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RegisterMaterial extends AppCompatActivity {

    TextView logoText, sloganText;
    Button callLogIN, register_btn;
    TextInputLayout username_layout, password_layout;

    TextInputEditText inputTextFullname, inputTextEmail, inputTextPhone, inputTextPassword, inputTextConfirmPassword;
    RadioGroup radioGroupGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register_material);

        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        getSupportActionBar().setTitle("My new title"); // set the top title
        String title = actionBar.getTitle().toString(); // get the title
        actionBar.hide();

        //if the user is already logged in we will directly start the profile activity
        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, RootActivity.class));
            return;
        }


        //Hooks

        //Hooks
        callLogIN = findViewById(R.id.login_screen);
        register_btn = findViewById(R.id.register_btn);
        logoText = findViewById(R.id.register_welcomeback);
        sloganText = findViewById(R.id.register_subtext);

        username_layout = findViewById(R.id.login_username);
        password_layout = findViewById(R.id.login_password);

        //get text from input boxes
        inputTextFullname = findViewById(R.id.inputTextFullname);
        inputTextEmail = findViewById(R.id.inputTextEmail);
        inputTextPhone = findViewById(R.id.inputTextPhone);
        inputTextPassword = findViewById(R.id.inputTextPassword);
        inputTextConfirmPassword = findViewById(R.id.inputTextConfirmPassword);

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        callLogIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if user pressed on login
                //we will open the login screen
                Intent intent = new Intent(RegisterMaterial.this, LoginMaterial.class);

//                Pair[] pairs = new Pair[6];
//                pairs[0] = new Pair<View, String>(logoText,"logo_text");
//                pairs[1] = new Pair<View, String>(sloganText,"logo_desc");
//                pairs[2] = new Pair<View, String>(username_layout,"username_input");
//                pairs[3] = new Pair<View, String>(password_layout,"password_input");
//                pairs[4] = new Pair<View, String>(register_btn,"account_button");
//                pairs[5] = new Pair<View, String>(callLogIN,"account_change");
//
//                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(RegisterMaterial.this, pairs);
//                startActivity(intent, options.toBundle());
                startActivity(intent);
            }
        });


    }


    private void registerUser() {
        final String full_name = inputTextFullname.getText().toString().trim();
        final String user_email = inputTextEmail.getText().toString().trim();
        final String user_phone = inputTextPhone.getText().toString().trim();
        final String user_password = inputTextPassword.getText().toString().trim();
        final String confirm_password = inputTextConfirmPassword.getText().toString().trim();

//        final String gender = ((RadioButton) findViewById(radioGroupGender.getCheckedRadioButtonId())).getText().toString();


        //first we will do the validations

        if (TextUtils.isEmpty(full_name)) {
            inputTextFullname.setError("Enter Full Name");
            inputTextFullname.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(user_email)) {
            inputTextEmail.setError("Please enter your email");
            inputTextEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(user_email).matches()) {
            inputTextEmail.setError("Enter a valid email");
            inputTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(user_password)) {
            inputTextPassword.setError("Enter a password");
            inputTextPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(user_phone)) {
            inputTextPhone.setError("Enter a Phone Number");
            inputTextPhone.requestFocus();
            return;
        }


        if (!user_password.equals(confirm_password)) {
            inputTextPassword.setError("Password Does not Match");
            inputTextPassword.requestFocus();
            return;
        }


        //if it passes all the validations

        class RegisterUser extends AsyncTask<Void, Void, String> {

            private ProgressBar progressBar;

            @Override
            protected String doInBackground(Void... voids) {
                //creating request handler object
                RequestHandler requestHandler = new RequestHandler();

                //creating request parameters
                HashMap<String, String> params = new HashMap<>();
                params.put("full_name", full_name);
                params.put("email", user_email);
                params.put("user_phone", user_phone);
                params.put("password", user_password);

                //returing the response
                return requestHandler.sendPostRequest(URLs.URL_REGISTER, params);
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //displaying the progress bar while user registers on the server
                progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //hiding the progressbar after completion
                progressBar.setVisibility(View.GONE);

                if(s.isEmpty()){
                    //show network error
                    showErrorAlert();
                    return;
                }


                try {
                    //converting response to json object
                    Log.i("obj", URLs.URL_REGISTER);

                    JSONObject obj = new JSONObject(s);



                    //if no error in response
                    if (!obj.getBoolean("error")) {
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();

                        //getting the user from the response
                        JSONObject userJson = obj.getJSONObject("user");

                        //creating a new user object
                        User userModel = new User(
                                userJson.getInt("id"),
                                userJson.getString("fullname"),
                                userJson.getString("email"),
                                userJson.getString("phone")
                        );

                        //storing the user in shared preferences
                        SharedPrefManager.getInstance(getApplicationContext()).userLogin(userModel);

                        //starting the profile activity
                        finish();
                        startActivity(new Intent(getApplicationContext(), RootActivity.class));
                    } else {
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        showUserExists();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        //executing the async task
        RegisterUser ru = new RegisterUser();
        ru.execute();
    }


    private void showErrorAlert() {
        new SweetAlertDialog(
                this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Oops...")
                .setContentText("Something went wrong!")
                .show();
    }

    private void showUserExists() {

        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("User already Exists")
                .setContentText("Try different Username and Email")
                .show();
    }

}