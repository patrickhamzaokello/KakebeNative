package com.shop.kakebe.KaKebe;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shop.kakebe.KaKebe.Adapters.PlaceOrderCartAdapter;
import com.shop.kakebe.KaKebe.Apis.ShopAPIBase;
import com.shop.kakebe.KaKebe.Apis.ShopApiEndPoints;
import com.shop.kakebe.KaKebe.Dialogs.OrderFailed;
import com.shop.kakebe.KaKebe.HelperClasses.SharedPrefManager;
import com.shop.kakebe.KaKebe.Models.FoodDBModel;
import com.shop.kakebe.KaKebe.Models.OrderRequest;
import com.shop.kakebe.KaKebe.Models.OrderResponse;
import com.shop.kakebe.KaKebe.Models.User;
import com.shop.kakebe.KaKebe.localDatabase.SenseDBHelper;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaceOrder extends AppCompatActivity {

    ActionBar actionBar;
    private ShopApiEndPoints shopApiEndPoints;
    private SenseDBHelper db;
    List<FoodDBModel> cartitemlist;

    ProgressBar placeorder_main_progress;
    OrderRequest orderRequest = new OrderRequest();
    TextView btn_change_location, grandsubvalue, grandshipvalue, grandtotalvalue, location_address_view, order_page_fullname, order_page_phoneno, order_page_username;
    Button btnCheckout;
    String selected_address_json;
    String address, city, phone,email,name;

    private ProgressBar progressBar;
    PlaceOrderCartAdapter placeOrderCartAdapter;
    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        if (!SharedPrefManager.getInstance(PlaceOrder.this).isLoggedIn()) {
//            finish();
            startActivity(new Intent(PlaceOrder.this, LoginMaterial.class));
        }
        actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.setTitle("Checkout");

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }



        shopApiEndPoints = ShopAPIBase.getClient(PlaceOrder.this).create(ShopApiEndPoints.class);
        db = new SenseDBHelper(PlaceOrder.this);



        btn_change_location = findViewById(R.id.btn_change_location);
        btnCheckout = findViewById(R.id.btnCheckout);
        grandsubvalue = findViewById(R.id.grandsubvalue);
        grandshipvalue = findViewById(R.id.grandshipvalue);
        grandtotalvalue = findViewById(R.id.grandtotalvalue);
        location_address_view = findViewById(R.id.location_address_view);

        order_page_fullname = findViewById(R.id.order_page_fullname);
        order_page_phoneno = findViewById(R.id.order_page_phoneno);

        recyclerView = findViewById(R.id.place_order_main_recycler);
        progressBar = findViewById(R.id.place_order_main_progress);
        placeorder_main_progress = findViewById(R.id.placeorder_main_progress);
        placeorder_main_progress.setVisibility(View.GONE);



        OrderTotalling();

        User user = SharedPrefManager.getInstance(PlaceOrder.this).getUser();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);



        btn_change_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PlaceOrder.this, DeliveryAddress.class);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS );
                startActivity(i);
            }
        });


        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("addressStringxs",selected_address_json );

                orderRequest.setOrderAddress(selected_address_json);
                orderRequest.setCustomerId(String.valueOf(user.getId()));
                orderRequest.setTotalAmount(String.valueOf(db.sumPriceCartItems()));
                orderRequest.setOrderStatus("pending");
                orderRequest.setProcessedBy("1");
                orderRequest.setOrderItemList(cartitemlist);

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


                                Log.i("Order Success", orderResponses.getMessage() + orderResponses.getError() );
                                db.clearCart();
                                updatecartCount();
                                OrderTotalling();

                                Intent i = new Intent(PlaceOrder.this, OrderCompleted.class);
                                startActivity(i);
                                finish();


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




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private Call<OrderResponse> postAllCartItems() {
        return shopApiEndPoints.postCartOrder(orderRequest);
    }


    public void OrderTotalling() {
        grandsubvalue.setText("" + NumberFormat.getNumberInstance(Locale.US).format(db.sumPriceCartItems()));
        grandshipvalue.setText("2000");
        grandtotalvalue.setText("" + NumberFormat.getNumberInstance(Locale.US).format(db.sumPriceCartItems() + 2000));
        order_page_fullname.setText(name);
        order_page_phoneno.setText(phone);
        location_address_view.setText(address + ", "+city);

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






    @Override
    protected void onResume() {

        super.onResume();

        //DETERMINE WHO STARTED THIS ACTIVITY
        final String sender=this.getIntent().getExtras().getString("SELECTED_ADDRESS");

        //IF ITS THE FRAGMENT THEN RECEIVE DATA
        if(sender != null)
        {
            this.receiveData();
        }
        cartitemlist = db.listTweetsBD();

        if (cartitemlist.size() > 0) {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            placeOrderCartAdapter = new PlaceOrderCartAdapter(this, cartitemlist);
            recyclerView.setAdapter(placeOrderCartAdapter);
            placeOrderCartAdapter.notifyDataSetChanged();
        } else {
            recyclerView.setVisibility(View.GONE);
//            emptycartwarning();
        }
    }


    private void receiveData()
    {
        //RECEIVE DATA VIA INTENT
        Intent i = getIntent();
        selected_address_json = i.getStringExtra("selected_address_json");
        address = i.getStringExtra("address");
        city = i.getStringExtra("city");
        phone = i.getStringExtra("phone");
        email = i.getStringExtra("email");
        name = i.getStringExtra("name");

        Log.i("recieve", address +city +phone+ email +name);

        //SET DATA
        OrderTotalling();


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