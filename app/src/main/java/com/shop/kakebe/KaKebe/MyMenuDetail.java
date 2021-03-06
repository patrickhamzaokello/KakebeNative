package com.shop.kakebe.KaKebe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.shop.kakebe.KaKebe.Adapters.OnlineMenuDetailAdapter;
import com.shop.kakebe.KaKebe.Apis.ShopAPIBase;
import com.shop.kakebe.KaKebe.Apis.ShopApiEndPoints;
import com.shop.kakebe.KaKebe.Models.FoodDBModel;
import com.shop.kakebe.KaKebe.Models.ProductDetail;
import com.shop.kakebe.KaKebe.Models.SelectedCategoryMenuItemResult;
import com.shop.kakebe.KaKebe.Models.SelectedProduct;
import com.shop.kakebe.KaKebe.Utils.MenuDetailListener;
import com.shop.kakebe.KaKebe.Utils.PaginationScrollListener;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyMenuDetail extends AppCompatActivity implements MenuDetailListener {


    private static final String TAG = "MyMenuDetail";
    OnlineMenuDetailAdapter adapter;
    LinearLayoutManager linearLayoutManager;

    RecyclerView rv;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError;
    SwipeRefreshLayout swipeRefreshLayout;

    private static final int PAGE_START = 1;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private static int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;
    private int selectCategoryId;
    private int selectMenuId;

    private ShopApiEndPoints shopApiEndPoints;
    ActionBar actionBar;
    List<SelectedProduct> categories;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_selected_category);
        actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.setTitle("Details");
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.purple_200)));
        actionBar.setElevation(0);
        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rv = findViewById(R.id.main_recycler);
        progressBar = findViewById(R.id.main_progress);
        errorLayout = findViewById(R.id.error_layout);
        btnRetry = findViewById(R.id.error_btn_retry);
        txtError = findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = findViewById(R.id.main_swiperefresh);

        adapter = new OnlineMenuDetailAdapter(MyMenuDetail.this,  this);

        linearLayoutManager = new LinearLayoutManager(MyMenuDetail.this, LinearLayoutManager.VERTICAL, false);
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
        shopApiEndPoints = ShopAPIBase.getClient(MyMenuDetail.this).create(ShopApiEndPoints.class);
        btnRetry.setOnClickListener(v -> loadFirstPage());
        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //DETERMINE WHO STARTED THIS ACTIVITY
        final String sender=this.getIntent().getExtras().getString("SENDER_KEY");

        //IF ITS THE FRAGMENT THEN RECEIVE DATA
        if(sender != null)
        {
            this.receiveData();
        }
    }


    private void receiveData()
    {
        //RECEIVE DATA VIA INTENT
        Intent i = getIntent();
        int menu_detail_id = i.getIntExtra("selectMenuId",1);
        int category_selected_key = i.getIntExtra("category_selected_key",0);


        System.out.println("category_selected_key "+category_selected_key);
        //SET DATA TO TEXTVIEWS
        selectCategoryId = category_selected_key;
        selectMenuId = menu_detail_id;
        loadFirstPage();

    }



    @Override
    public void incrementqtn(int qty, FoodDBModel foodDBModel) {
    }

    @Override
    public void decrementqtn(int qty, FoodDBModel foodDBModel) {

    }

    @Override
    public void addToCartbtn(SelectedCategoryMenuItemResult selectedCategoryMenuItemResult) {

    }

    @Override
    public void orderNowMenuBtn(SelectedCategoryMenuItemResult selectedCategoryMenuItemResult) {

    }

    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callProductDetail().isExecuted())
            callProductDetail().cancel();

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

        callProductDetail().enqueue(new Callback<ProductDetail>() {
            @Override
            public void onResponse(Call<ProductDetail> call, Response<ProductDetail> response) {
                hideErrorView();

//                Log.i(TAG, "onResponse: " + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                categories = fetchResults(response);
                progressBar.setVisibility(View.GONE);
                if(categories.isEmpty()){
                    showCategoryErrorView();
                    return;
                } else {
                    adapter.addAll(categories);
                }

                if (currentPage < TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<ProductDetail> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }


    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callProductDetail().enqueue(new Callback<ProductDetail>() {
            @Override
            public void onResponse(Call<ProductDetail> call, Response<ProductDetail> response) {
                Log.i(TAG, "onResponse: " + currentPage
                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                adapter.removeLoadingFooter();
                isLoading = false;

                categories = fetchResults(response);
                adapter.addAll(categories);

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<ProductDetail> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }



    private List<SelectedProduct> fetchResults(Response<ProductDetail> response) {
        ProductDetail productDetail = response.body();
        TOTAL_PAGES = productDetail.getTotalPages();
        System.out.println("total pages" + TOTAL_PAGES);

        return productDetail.getSelectedProduct();
    }


    private Call<ProductDetail> callProductDetail() {
        return shopApiEndPoints.getMenuDetails(
                selectMenuId,
                selectCategoryId,
                currentPage
        );
    }


    @Override
    public void retryPageLoad() {
        loadNextPage();
    }




    private void showErrorView(Throwable throwable) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    private void showCategoryErrorView() {

        progressBar.setVisibility(View.GONE);

        AlertDialog.Builder android = new AlertDialog.Builder(MyMenuDetail.this);
        android.setTitle("From Kakebe Shop");
        android.setIcon(R.drawable.demoproduct);
        android.setMessage("We can't find what you are looking for. Please Return to home")
                .setCancelable(false)

                .setPositiveButton("Home", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //go to activity
                        Intent intent = new Intent(MyMenuDetail.this, RootActivity.class);
                        startActivity(intent);
                    }
                });
        android.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //go to activity
                Intent intent = new Intent(MyMenuDetail.this, RootActivity.class);
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