package com.pkasemer.kakebeshoplira;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.pkasemer.kakebeshoplira.Apis.MovieApi;
import com.pkasemer.kakebeshoplira.Apis.MovieService;
import com.pkasemer.kakebeshoplira.Dialogs.ChangeLocation;
import com.pkasemer.kakebeshoplira.Dialogs.ChangePaymentMethod;
import com.pkasemer.kakebeshoplira.Dialogs.OrderConfirmationDialog;
import com.pkasemer.kakebeshoplira.Dialogs.OrderFailed;
import com.pkasemer.kakebeshoplira.HelperClasses.SharedPrefManager;
import com.pkasemer.kakebeshoplira.Models.FoodDBModel;
import com.pkasemer.kakebeshoplira.Models.OrderRequest;
import com.pkasemer.kakebeshoplira.Models.OrderResponse;
import com.pkasemer.kakebeshoplira.Models.User;
import com.pkasemer.kakebeshoplira.Models.UserModel;
import com.pkasemer.kakebeshoplira.localDatabase.SenseDBHelper;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceOrder extends AppCompatActivity implements ChangeLocation.NoticeDialogListener, OrderConfirmationDialog.OrderConfirmLister {

    private MovieService movieService;
    private SenseDBHelper db;
    boolean food_db_itemchecker;
    List<FoodDBModel> cartitemlist;

    ProgressBar placeorder_main_progress;
    OrderRequest orderRequest = new OrderRequest();
    TextView btn_change_location, moneyChangeButton, grandsubvalue, grandshipvalue, grandtotalvalue, location_address_view, order_page_fullname, order_page_phoneno, order_page_username;

    RelativeLayout placeorde_relative_layout;
    LinearLayout orderRecommendLayout;
    Button btnCheckout,btnTodaysMEnu,btnGoHOme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        if (!SharedPrefManager.getInstance(PlaceOrder.this).isLoggedIn()) {
//            finish();
            startActivity(new Intent(PlaceOrder.this, LoginMaterial.class));
        }

        ActionBar actionBar = getSupportActionBar(); // or getActionBar();
        getSupportActionBar().setTitle("Zodongo Foods"); // set the top title
        String title = actionBar.getTitle().toString(); // get the title
        actionBar.hide();

        movieService = MovieApi.getClient(PlaceOrder.this).create(MovieService.class);
        db = new SenseDBHelper(PlaceOrder.this);
        cartitemlist = db.listTweetsBD();


        btn_change_location = findViewById(R.id.btn_change_location);
        moneyChangeButton = findViewById(R.id.moneyChangeButton);
        btnCheckout = findViewById(R.id.btnCheckout);
        grandsubvalue = findViewById(R.id.grandsubvalue);
        grandshipvalue = findViewById(R.id.grandshipvalue);
        grandtotalvalue = findViewById(R.id.grandtotalvalue);
        location_address_view = findViewById(R.id.location_address_view);

        order_page_fullname = findViewById(R.id.order_page_fullname);
        order_page_phoneno = findViewById(R.id.order_page_phoneno);
        order_page_username = findViewById(R.id.order_page_username);

        placeorde_relative_layout = findViewById(R.id.placeorde_relative_layout);

        placeorder_main_progress = findViewById(R.id.placeorder_main_progress);
        placeorder_main_progress.setVisibility(View.GONE);
        orderRecommendLayout = findViewById(R.id.order_recommend_layout);
        btnTodaysMEnu = findViewById(R.id.order_btn_todayMenu);
        btnGoHOme = findViewById(R.id.btnGoHOme);


        OrderTotalling();

        User user = SharedPrefManager.getInstance(PlaceOrder.this).getUser();

        order_page_fullname.setText(user.getFullname());
//        order_page_username.setText(user.getUsername());
        order_page_phoneno.setText(user.getPhone());
//        location_address_view.setText(user.getAddress());


//        orderRequest.setOrderAddress(user.getAddress() + " - " + user.getPhone());
        orderRequest.setCustomerId(String.valueOf(user.getId()));
        orderRequest.setTotalAmount(String.valueOf(db.sumPriceCartItems()));
        orderRequest.setOrderStatus("pending");
        orderRequest.setProcessedBy("1");
        orderRequest.setOrderItemList(cartitemlist);

        btn_change_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeLocation changeLocation = new ChangeLocation();
                changeLocation.show(getSupportFragmentManager(), "change Location");
            }
        });

        moneyChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangePaymentMethod changePaymentMethod = new ChangePaymentMethod();
                changePaymentMethod.show(getSupportFragmentManager(), "change payment method");
            }
        });

        btnTodaysMEnu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaceOrder.this, RootActivity.class);
                startActivity(i);
            }
        });

        btnGoHOme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaceOrder.this, RootActivity.class);
                startActivity(i);
            }
        });

        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnCheckout.setEnabled(false);
                btnCheckout.setClickable(false);

                placeorder_main_progress.setVisibility(View.VISIBLE);

                postAllCartItems().enqueue(new Callback<OrderResponse>() {
                    @Override
                    public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {

                        //set response body to match OrderResponse Model
                        OrderResponse orderResponses = response.body();
                        placeorder_main_progress.setVisibility(View.GONE);


                        //if orderResponses is not null
                        if (orderResponses != null) {

                            //if no error- that is error = false
                            if (!orderResponses.getError()) {

                                placeorde_relative_layout.setVisibility(View.GONE);


                                Log.i("Order Success", orderResponses.getMessage() + orderResponses.getError() );
                                db.clearCart();
                                updatecartCount();
                                OrderTotalling();
                                OrderConfirmationDialog orderConfirmationDialog = new OrderConfirmationDialog();
                                orderConfirmationDialog.show(getSupportFragmentManager(), "Order Confirmation Dialog");

                                orderRecommendLayout.setVisibility(View.VISIBLE);

                            } else {
                                Log.i("Ress", "message: " + (orderResponses.getMessage()));
                                Log.i("et", "error false: " + (orderResponses.getError()));
                                btnCheckout.setEnabled(true);
                                btnCheckout.setClickable(true);

                                ShowOrderFailed();

                            }


                        } else {
                            Log.i("Order Response null", "Order is null Try Again: " + orderResponses);
                            btnCheckout.setEnabled(true);
                            btnCheckout.setClickable(true);

                            ShowOrderFailed();

                            return;

                        }

                    }

                    @Override
                    public void onFailure(Call<OrderResponse> call, Throwable t) {
                        placeorder_main_progress.setVisibility(View.GONE);
                        btnCheckout.setEnabled(true);
                        btnCheckout.setClickable(true);


                        t.printStackTrace();
                        Log.i("Order Failed", "Order Failed Try Again: " + t);
                        ShowOrderFailed();
                    }
                });
            }
        });


    }




    private Call<OrderResponse> postAllCartItems() {
        return movieService.postCartOrder(orderRequest);
    }


    public void OrderTotalling() {
        grandsubvalue.setText("" + NumberFormat.getNumberInstance(Locale.US).format(db.sumPriceCartItems()));
        grandshipvalue.setText("2000");
        grandtotalvalue.setText("" + NumberFormat.getNumberInstance(Locale.US).format(db.sumPriceCartItems() + 2000));
    }

    private void updatecartCount() {
        String mycartcount = String.valueOf(db.countCart());
        Intent intent = new Intent(getApplicationContext().getResources().getString(R.string.cartcoutAction));
        intent.putExtra(getApplicationContext().getResources().getString(R.string.cartCount), mycartcount);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    private void ShowOrderFailed(){
        OrderFailed orderFailed = new OrderFailed();
        orderFailed.show(getSupportFragmentManager(), "Order Failed Dialog");
    }

    private void updatelocationView(String location) {
        location_address_view.setText(location);
        orderRequest.setOrderAddress(location);
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog, TextInputEditText inputUserNewLocation, TextInputEditText inputUserNewPhone) {

        String location_name = inputUserNewLocation.getText().toString();
        String phone = inputUserNewPhone.getText().toString();

        if(location_name != null && !location_name.isEmpty() && phone != null && !phone.isEmpty()){
            Log.i("dialog", "Positive Method2: " + location_name + " - " + phone);
            order_page_phoneno.setText(phone);
            updatelocationView(location_name + "-" + phone);

        } else {
            Toast.makeText(getApplicationContext(), "Location and Phone not Changed", Toast.LENGTH_SHORT);
        }


    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.i("dialog", "Negative: ");
    }

    @Override
    public void onOrderDialogPositiveClick(DialogFragment dialog) {
        Intent i = new Intent(PlaceOrder.this, RootActivity.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}