package com.pkasemer.kakebeshoplira.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.pkasemer.kakebeshoplira.Adapters.UserAddressesAdapter;
import com.pkasemer.kakebeshoplira.Apis.MovieApi;
import com.pkasemer.kakebeshoplira.Apis.MovieService;
import com.pkasemer.kakebeshoplira.Dialogs.OrderConfirmationDialog;
import com.pkasemer.kakebeshoplira.HelperClasses.SharedPrefManager;
import com.pkasemer.kakebeshoplira.HttpRequests.RequestHandler;
import com.pkasemer.kakebeshoplira.HttpRequests.URLs;
import com.pkasemer.kakebeshoplira.LoginMaterial;
import com.pkasemer.kakebeshoplira.ManageOrders;
import com.pkasemer.kakebeshoplira.Models.Address;
import com.pkasemer.kakebeshoplira.Models.CreateAddress;
import com.pkasemer.kakebeshoplira.Models.CreateAddressResponse;
import com.pkasemer.kakebeshoplira.Models.OrderRequest;
import com.pkasemer.kakebeshoplira.Models.OrderResponse;
import com.pkasemer.kakebeshoplira.Models.User;
import com.pkasemer.kakebeshoplira.Models.UserAddress;
import com.pkasemer.kakebeshoplira.Models.UserModel;
import com.pkasemer.kakebeshoplira.R;
import com.pkasemer.kakebeshoplira.RootActivity;
import com.pkasemer.kakebeshoplira.Utils.PaginationScrollListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Profile extends Fragment implements com.pkasemer.kakebeshoplira.Utils.PaginationAdapterCallback {


    TextView textViewUsername, textViewEmail, full_name_text, card_email_text, card_phone_text;


    MaterialCardView manageOrders;


    private static final String TAG = "profile";

    UserAddressesAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView rv;
    ProgressBar progressBar;
    LinearLayout errorLayout, add_address_layout;
    Button btnRetry, add_address_btn;
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

    private MovieService movieService;
    private Object PaginationAdapterCallback;


    public Profile() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //if the user is not logged in
        //starting the login activity
        if (!SharedPrefManager.getInstance(getContext()).isLoggedIn()) {
//            finish();
            startActivity(new Intent(getContext(), LoginMaterial.class));
        }


        textViewUsername = view.findViewById(R.id.full_name);
        textViewEmail = view.findViewById(R.id.email_text);


        full_name_text = view.findViewById(R.id.full_name_text);
        card_email_text = view.findViewById(R.id.card_email_text);
        card_phone_text = view.findViewById(R.id.card_phone_text);
        manageOrders = view.findViewById(R.id.manageOrders);

        //getting the current user
        UserModel userModel = SharedPrefManager.getInstance(getContext()).getUser();
//        userModel.getId();

        //setting the values to the textviews


        full_name_text.setText(userModel.getFullname());
        card_email_text.setText(userModel.getEmail());
        card_phone_text.setText(userModel.getPhone());
        userId = userModel.getId();


        //when the user presses logout button
        //calling the logout method
        view.findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPrefManager.getInstance(getContext()).logout();

            }
        });

        manageOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("show past order ", "orders");
                Intent i = new Intent(getContext(), ManageOrders.class);
                startActivity(i);
            }
        });


        rv = view.findViewById(R.id.main_recycler);
        progressBar = view.findViewById(R.id.main_progress);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);


        add_address_layout = view.findViewById(R.id.add_address_layout);
        add_address_btn = view.findViewById(R.id.add_address_btn);

        swipeRefreshLayout = view.findViewById(R.id.main_swiperefresh);


        adapter = new UserAddressesAdapter(getContext(), this);

        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
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
        movieService = MovieApi.getClient(getContext()).create(MovieService.class);

        loadFirstPage();

        btnRetry.setOnClickListener(v -> loadFirstPage());
        add_address_btn.setOnClickListener(v -> createNewAddress());

        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

        return view;
    }

    private void createNewAddress() {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_show_round_dialog);

        TextInputEditText user_phone, user_location, user_district;
        Button save_address;


        user_phone = dialog.findViewById(R.id.user_phone);
        user_location = dialog.findViewById(R.id.user_location);
        user_district = dialog.findViewById(R.id.user_district);
        save_address = dialog.findViewById(R.id.save_address);


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
        progressBar = dialog.findViewById(R.id.progressBar);

        createAddress.setUserId(String.valueOf(userId));
        createAddress.setPhone(phone);
        createAddress.setLocation(location);
        createAddress.setDistrict(district);

        if (TextUtils.isEmpty(district)) {
            user_district.setError("Specify your District");
            user_district.requestFocus();
            return;
        }


        if (TextUtils.isEmpty(location)) {
            user_location.setError("Provide your location");
            user_location.requestFocus();
            return;
        }

        if (phone.length() < 9) {
            user_phone.setError("Invalid Phone number");
            user_phone.requestFocus();
            return;
        }

        if (phone.length() > 10) {
            user_phone.setError("Use format 07xxxxxxxx ");
            user_phone.requestFocus();
            return;
        }

        postCreateUserAddress().enqueue(new Callback<CreateAddressResponse>() {
            @Override
            public void onResponse(Call<CreateAddressResponse> call, Response<CreateAddressResponse> response) {

                //set response body to match OrderResponse Model
                CreateAddressResponse createAddressResponse = response.body();
                progressBar.setVisibility(View.VISIBLE);


                //if orderResponses is not null
                if (createAddressResponse != null) {

                    //if no error- that is error = false
                    if (!createAddressResponse.getError()) {

                        Log.i("Order Success", createAddressResponse.getMessage() + createAddressResponse.getError());

                    } else {
                        Log.i("Ress", "message: " + (createAddressResponse.getMessage()));
                        Log.i("et", "error false: " + (createAddressResponse.getError()));
                        save_address.setEnabled(true);
                        save_address.setClickable(true);

//                        ShowOrderFailed();

                    }


                } else {
                    Log.i("Order Response null", "Order is null Try Again: " + createAddressResponse);
                    save_address.setEnabled(true);
                    save_address.setClickable(true);
                    return;

                }

            }

            @Override
            public void onFailure(Call<CreateAddressResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

                save_address.setEnabled(true);
                save_address.setClickable(true);

                t.printStackTrace();
                Log.i("Order Failed", "Order Failed Try Again: " + t);
            }
        });


    }


    private Call<CreateAddressResponse> postCreateUserAddress() {
        return movieService.postCreateAddress(createAddress);
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
        return movieService.getAddresses(
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


        AlertDialog.Builder android = new AlertDialog.Builder(getContext());
        android.setTitle("Coming Soon");
        android.setIcon(R.drawable.africanwoman);
        android.setMessage("This Menu Category will be updated with great tastes soon, Stay Alert for Updates.")
                .setCancelable(false)

                .setPositiveButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //go to activity
                        Intent intent = new Intent(getActivity(), RootActivity.class);
                        startActivity(intent);
                    }
                });
        android.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //go to activity
                Intent intent = new Intent(getActivity(), RootActivity.class);
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
            add_address_layout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(getContext().CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

}