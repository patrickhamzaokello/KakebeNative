package com.shop.kakebe.KaKebe;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.shop.kakebe.KaKebe.Adapters.SelectOrderAddressesAdapter;
import com.shop.kakebe.KaKebe.Apis.ShopAPIBase;
import com.shop.kakebe.KaKebe.Apis.ShopApiEndPoints;
import com.shop.kakebe.KaKebe.Utils.SelectedAddressListener;
import com.shop.kakebe.KaKebe.HelperClasses.SharedPrefManager;
import com.shop.kakebe.KaKebe.Models.Address;
import com.shop.kakebe.KaKebe.Models.CreateAddressModel;
import com.shop.kakebe.KaKebe.Models.User;
import com.shop.kakebe.KaKebe.Models.UserAddress;
import com.shop.kakebe.KaKebe.Utils.PaginationScrollListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectDeliveryAddress extends AppCompatActivity implements SelectedAddressListener {

    private static final String TAG = "Delivery Address";
    ActionBar actionBar;

    TextView createnewtext;

    SelectOrderAddressesAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView rv;
    ProgressBar progressBar;
    LinearLayout errorLayout, add_address_layout;
    Button btnRetry, add_address_btn;
    MaterialButton heading_create_address;
    TextView txtError;
    SwipeRefreshLayout swipeRefreshLayout;

    private static final int PAGE_START = 1;
    CreateAddressModel createAddressModel = new CreateAddressModel();

    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private static int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;
    private int userId;

    List<UserAddress> userAddresses;

    private ShopApiEndPoints shopApiEndPoints;
    private Object PaginationAdapterCallback;


    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");
                    doRefresh();
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.setTitle("Checkout");
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        User user = SharedPrefManager.getInstance(SelectDeliveryAddress.this).getUser();
        userId = user.getId();


        rv = findViewById(R.id.main_recycler);
        progressBar = findViewById(R.id.main_progress);
        errorLayout = findViewById(R.id.error_layout);
        btnRetry = findViewById(R.id.error_btn_retry);
        txtError = findViewById(R.id.error_txt_cause);


        add_address_layout = findViewById(R.id.add_address_layout);
        add_address_btn = findViewById(R.id.add_address_btn);
        heading_create_address = findViewById(R.id.heading_create_address);
        createnewtext = findViewById(R.id.createnewtext);

        swipeRefreshLayout = findViewById(R.id.main_swiperefresh);


        adapter = new SelectOrderAddressesAdapter(SelectDeliveryAddress.this, this);

        linearLayoutManager = new LinearLayoutManager(SelectDeliveryAddress.this, LinearLayoutManager.VERTICAL, false);
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
        shopApiEndPoints = ShopAPIBase.getClient(SelectDeliveryAddress.this).create(ShopApiEndPoints.class);

        loadFirstPage();

        btnRetry.setOnClickListener(v -> loadFirstPage());
        add_address_btn.setOnClickListener(v -> createNewAddress());
        heading_create_address.setOnClickListener(v -> createNewAddress());
        createnewtext.setOnClickListener(v -> createNewAddress());
        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

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



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void createNewAddress() {
        Intent intent = new Intent(SelectDeliveryAddress.this, CreateAddress.class);
        activityResultLauncher.launch(intent);
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


        AlertDialog.Builder android = new AlertDialog.Builder(SelectDeliveryAddress.this);
        android.setTitle("Error");
        android.setIcon(R.drawable.organicfood);
        android.setMessage("There has been an error.")
                .setCancelable(false)

                .setPositiveButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //go to activity
                        Intent intent = new Intent(SelectDeliveryAddress.this, RootActivity.class);
                        startActivity(intent);
                    }
                });
        android.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //go to activity
                Intent intent = new Intent(SelectDeliveryAddress.this, RootActivity.class);
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

        if (add_address_layout.getVisibility() == View.VISIBLE) {
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


    @Override
    public void selectedAddress(UserAddress userAddress) {
        // convert addressmodel to json with gson
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", userAddress.getId());
            obj.put("user_id", userAddress.getUserId());
            obj.put("address", userAddress.getAddress());
            obj.put("country", userAddress.getCountry());
            obj.put("city", userAddress.getCity());
            obj.put("longitude", null);
            obj.put("latitude", null);
            obj.put("postal_code", 256);
            obj.put("phone", userAddress.getPhone());
            obj.put("set_default", 1);
            obj.put("created_at", null);
            obj.put("updated_at", null);
            obj.put("name", userAddress.getUsername());
            obj.put("email", userAddress.getEmail());
        } catch (
                JSONException e) {
            e.printStackTrace();
        }


        final String addressJson = obj.toString(); // <-- JSON string
        Log.i("addressString", addressJson);

        Intent i = new Intent(SelectDeliveryAddress.this, PlaceOrder.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        //PACK DATA
        i.putExtra("SELECTED_ADDRESS", "PlaceOrder");
        i.putExtra("selected_address_json", addressJson);
        i.putExtra("address",userAddress.getAddress());
        i.putExtra("city",userAddress.getCity());
        i.putExtra("phone",userAddress.getPhone());
        i.putExtra("email",userAddress.getEmail());
        i.putExtra("name",userAddress.getUsername());
        i.putExtra("shipping_fee",String.valueOf(userAddress.getShippingCost()));
        startActivity(i);
        finish();
    }


}