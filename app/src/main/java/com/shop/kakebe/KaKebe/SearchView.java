package com.shop.kakebe.KaKebe;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shop.kakebe.KaKebe.Adapters.SearchQueryAdapter;
import com.shop.kakebe.KaKebe.Apis.ShopAPIBase;
import com.shop.kakebe.KaKebe.Apis.ShopApiEndPoints;
import com.shop.kakebe.KaKebe.Models.Product;
import com.shop.kakebe.KaKebe.Models.SearchResult;
import com.shop.kakebe.KaKebe.Utils.PaginationScrollListener;
import com.shop.kakebe.KaKebe.Utils.SearchAdapterCallBack;
import com.shop.kakebe.KaKebe.R;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchView extends AppCompatActivity implements SearchAdapterCallBack {

    private static final String TAG = "MainActivity";

    SearchQueryAdapter searchQueryAdapter;
    GridLayoutManager gridLayoutManager;
    private String queryString;
    List<Product> products;
    RecyclerView rv;
    ProgressBar progressBar;
    LinearLayout errorLayout, notFound_layout;
    Button btnRetry;
    TextView txtError;
    SwipeRefreshLayout swipeRefreshLayout;

    private static final int PAGE_START = 1;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private static int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;
    private final int selectCategoryId = 3;

    ActionBar actionBar;
    private ShopApiEndPoints shopApiEndPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);
        actionBar = getSupportActionBar(); // or getActionBar();
        actionBar.setTitle("Search Result");
        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        rv = findViewById(R.id.main_recycler);
        progressBar = findViewById(R.id.main_progress);
        errorLayout = findViewById(R.id.error_layout);
        notFound_layout = findViewById(R.id.notFound_layout);
        btnRetry = findViewById(R.id.error_btn_retry);

        txtError = findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = findViewById(R.id.main_swiperefresh);

        searchQueryAdapter = new SearchQueryAdapter(SearchView.this, this);
        gridLayoutManager = new GridLayoutManager(SearchView.this, 3);
        rv.setLayoutManager(gridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());

        rv.addOnScrollListener(new PaginationScrollListener(gridLayoutManager) {
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
        shopApiEndPoints = ShopAPIBase.getClient(SearchView.this).create(ShopApiEndPoints.class);


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
        final String sender = this.getIntent().getExtras().getString("SENDER_KEY");

        //IF ITS THE FRAGMENT THEN RECEIVE DATA
        if (sender != null) {
            this.receiveData();
        }
    }


    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callSearchAPI().isExecuted())
            callSearchAPI().cancel();

        // TODO: Check if data is stale.
        //  Execute network request if cache is expired; otherwise do not update data.
        searchQueryAdapter.getMovies().clear();
//        searchQueryAdapter.notifyDataSetChanged();
        loadFirstPage();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ");

        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();
        currentPage = PAGE_START;

        callSearchAPI().enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                hideErrorView();

//                Log.i(TAG, "onResponse: " + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                products = fetchqueryResults(response);
                progressBar.setVisibility(View.GONE);
                if (products != null) {
                    searchQueryAdapter.addAll(products);
                } else {
                    showCategoryErrorView();
                    return;
                }

                if (currentPage < TOTAL_PAGES) searchQueryAdapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }

    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callSearchAPI().enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                Log.i(TAG, "onResponse: " + currentPage
                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                searchQueryAdapter.removeLoadingFooter();
                isLoading = false;

                products = fetchqueryResults(response);

                if (products != null) {
                    searchQueryAdapter.addAll(products);
                } else {
                    showCategoryErrorView();
                    return;
                }

                if (currentPage != TOTAL_PAGES) searchQueryAdapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                t.printStackTrace();
                searchQueryAdapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    private List<Product> fetchqueryResults(Response<SearchResult> response) {
        SearchResult searchResult = response.body();
        TOTAL_PAGES = searchResult != null ? searchResult.getTotalPages() : null;
        System.out.println("total pages" + TOTAL_PAGES);

        return searchResult.getProducts();
    }

    private Call<SearchResult> callSearchAPI() {
        return shopApiEndPoints.getSearch(
                queryString,
                currentPage
        );
    }


    @Override
    public void retryPageLoad() {

    }

    @Override
    public void searchinput(String searchquery) {

    }



    private void showErrorView(Throwable throwable) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            notFound_layout.setVisibility(View.GONE);
            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    private void showCategoryErrorView() {

        progressBar.setVisibility(View.GONE);
        if (notFound_layout.getVisibility() == View.GONE) {
            notFound_layout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

        }

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
            notFound_layout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) SearchView.this.getSystemService(SearchView.this.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void receiveData() {
        //RECEIVE DATA VIA INTENT
        Intent i = getIntent();
        queryString = i.getStringExtra("queryString");

        rv.setAdapter(searchQueryAdapter);
        searchQueryAdapter.getMovies().clear();
        loadFirstPage();


    }

}