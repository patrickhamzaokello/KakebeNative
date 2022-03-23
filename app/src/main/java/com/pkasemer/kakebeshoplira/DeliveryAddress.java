package com.pkasemer.kakebeshoplira;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.pkasemer.kakebeshoplira.Adapters.UserAddressesAdapter;
import com.pkasemer.kakebeshoplira.Apis.ShopAPIBase;
import com.pkasemer.kakebeshoplira.Apis.ShopApiEndPoints;
import com.pkasemer.kakebeshoplira.HelperClasses.SharedPrefManager;
import com.pkasemer.kakebeshoplira.Models.Address;
import com.pkasemer.kakebeshoplira.Models.CreateAddress;
import com.pkasemer.kakebeshoplira.Models.CreateAddressResponse;
import com.pkasemer.kakebeshoplira.Models.User;
import com.pkasemer.kakebeshoplira.Models.UserAddress;
import com.pkasemer.kakebeshoplira.Utils.PaginationScrollListener;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryAddress extends AppCompatActivity implements  com.pkasemer.kakebeshoplira.Utils.PaginationAdapterCallback {

    ActionBar actionBar;

    TextView createnewtext, textViewUsername, textViewEmail, full_name_text, card_email_text, card_phone_text;


    MaterialCardView manageOrders;


    private static final String TAG = "profile";

    UserAddressesAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView rv;
    ProgressBar progressBar,addressprogressBar;
    LinearLayout errorLayout, add_address_layout;
    Button btnRetry, add_address_btn, addNewAddress;
    TextView txtError;
    SwipeRefreshLayout swipeRefreshLayout;

    private static final int PAGE_START = 1;
    CreateAddress createAddress = new CreateAddress();

    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private static int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;
    private int userId;

    List<UserAddress> userAddresses;

    private ShopApiEndPoints shopApiEndPoints;
    private Object PaginationAdapterCallback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.setTitle("Delivery Address");
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


//        addNewAddress = findViewById(R.id.addnewAddress);


        User user = SharedPrefManager.getInstance(DeliveryAddress.this).getUser();
        userId = user.getId();


        rv = findViewById(R.id.main_recycler);
        progressBar = findViewById(R.id.main_progress);
        errorLayout = findViewById(R.id.error_layout);
        btnRetry = findViewById(R.id.error_btn_retry);
        txtError = findViewById(R.id.error_txt_cause);


        add_address_layout = findViewById(R.id.add_address_layout);
        add_address_btn = findViewById(R.id.add_address_btn);
        createnewtext = findViewById(R.id.createnewtext);

        swipeRefreshLayout = findViewById(R.id.main_swiperefresh);


        adapter = new UserAddressesAdapter(DeliveryAddress.this, this);

        linearLayoutManager = new LinearLayoutManager(DeliveryAddress.this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(linearLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);

        rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;
                loadNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        //init service and load data
        shopApiEndPoints = ShopAPIBase.getClient(DeliveryAddress.this).create(ShopApiEndPoints.class);

        loadFirstPage();

        btnRetry.setOnClickListener(v -> loadFirstPage());
        add_address_btn.setOnClickListener(v -> createNewAddress());
        createnewtext.setOnClickListener(v -> createNewAddress());

        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void createNewAddress() {
        final Dialog dialog = new Dialog(DeliveryAddress.this);
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
                        dialog.hide();

                        Toast.makeText(DeliveryAddress.this, "Address Saved", Toast.LENGTH_SHORT).show();

                        //refresh adapter
                        doRefresh();

                    } else {
                        Log.i("Ress", "message: " + (createAddressResponse.getMessage()));
                        Log.i("et", "error false: " + (createAddressResponse.getError()));
                        save_address.setEnabled(true);
                        save_address.setClickable(true);
                        addressprogressBar.setVisibility(View.GONE);
                        Toast.makeText(DeliveryAddress.this, "Adding Address Failed", Toast.LENGTH_SHORT).show();


//                        ShowOrderFailed();

                    }


                } else {
                    Log.i("Address Response null", "Address is null Try Again: " + createAddressResponse);
                    Toast.makeText(DeliveryAddress.this, "Adding Address Failed", Toast.LENGTH_SHORT).show();
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


    private Call<CreateAddressResponse> postCreateUserAddress() {
        return shopApiEndPoints.postCreateAddress(createAddress);
    }


    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callUserAddresses().isExecuted())
            callUserAddresses().cancel();

        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        adapter.getMovies().clear();
        adapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ");

        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();
        currentPage = PAGE_START;

        callUserAddresses().enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                hideErrorView();

//                Log.i(TAG, "onResponse: " + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                userAddresses = fetchResults(response);


                if (userAddresses != null) {
                    Log.i("userAddresses", "not null " + String.valueOf(userAddresses));

                    progressBar.setVisibility(View.GONE);
                    if (userAddresses.isEmpty()) {
                        showCategoryErrorView();
                        return;
                    } else {
                        adapter.addAll(userAddresses);
                    }

                    if (currentPage < TOTAL_PAGES) adapter.addLoadingFooter();
                    else isLastPage = true;
                } else {
                    Log.i("userAddresses", String.valueOf(userAddresses));

                    progressBar.setVisibility(View.GONE);
                    add_address_layout.setVisibility(View.VISIBLE);
                    errorLayout.setVisibility(View.GONE);

                }


            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }


    private List<UserAddress> fetchResults(Response<Address> response) {
        Address address = response.body();
        TOTAL_PAGES = address.getTotalPages();

        int total_results = address.getTotalResults();
        if (total_results > 0) {
            return address.getUserAddress();
        } else {
            return null;
        }

    }

    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callUserAddresses().enqueue(new Callback<Address>() {
            @Override
            public void onResponse(Call<Address> call, Response<Address> response) {
                Log.i(TAG, "onResponse: " + currentPage
                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                adapter.removeLoadingFooter();
                isLoading = false;

                userAddresses = fetchResults(response);
                adapter.addAll(userAddresses);

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<Address> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
                add_address_layout.setVisibility(View.GONE);
            }
        });
    }


    /**
     * Performs a Retrofit call to the top rated movies API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<Address> callUserAddresses() {
        return shopApiEndPoints.getAddresses(
                userId,
                currentPage
        );
    }

    @Override
    public void retryPageLoad() {
        loadNextPage();
    }

    @Override
    public void requestfailed() {
//        OrderNotFound orderNotFound = new OrderNotFound();
//        orderNotFound.show(getSupportFragmentManager(), "Order Not Found");

    }


    /**
     * @param throwable required for {@link #fetchErrorMessage(Throwable)}
     * @return
     */
    private void showErrorView(Throwable throwable) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            add_address_layout.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    private void showCategoryErrorView() {

        progressBar.setVisibility(View.GONE);
        add_address_layout.setVisibility(View.GONE);


        AlertDialog.Builder android = new AlertDialog.Builder(DeliveryAddress.this);
        android.setTitle("Coming Soon");
        android.setIcon(R.drawable.africanwoman);
        android.setMessage("This Menu Category will be updated with great tastes soon, Stay Alert for Updates.")
                .setCancelable(false)

                .setPositiveButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //go to activity
                        Intent intent = new Intent(DeliveryAddress.this, RootActivity.class);
                        startActivity(intent);
                    }
                });
        android.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //go to activity
                Intent intent = new Intent(DeliveryAddress.this, RootActivity.class);
                startActivity(intent);
            }
        });
        android.create().show();

    }


    /**
     * @param throwable to identify the type of error
     * @return appropriate error message
     */
    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
    }

    // Helpers -------------------------------------------------------------------------------------


    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        if( add_address_layout.getVisibility() == View.VISIBLE){
            add_address_layout.setVisibility(View.GONE);
        }
    }

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }


}