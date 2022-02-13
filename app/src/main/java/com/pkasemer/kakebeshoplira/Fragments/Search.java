package com.pkasemer.kakebeshoplira.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pkasemer.kakebeshoplira.Adapters.SearchAdapter;
import com.pkasemer.kakebeshoplira.Adapters.SearchQueryAdapter;
import com.pkasemer.kakebeshoplira.Adapters.searchHomeAdapter;
import com.pkasemer.kakebeshoplira.Apis.MovieApi;
import com.pkasemer.kakebeshoplira.Apis.MovieService;
import com.pkasemer.kakebeshoplira.Models.Product;
import com.pkasemer.kakebeshoplira.Models.SearchCategoriee;
import com.pkasemer.kakebeshoplira.Models.SearchHome;
import com.pkasemer.kakebeshoplira.Models.SearchResult;
import com.pkasemer.kakebeshoplira.R;
import com.pkasemer.kakebeshoplira.RootActivity;
import com.pkasemer.kakebeshoplira.Utils.PaginationScrollListener;
import com.pkasemer.kakebeshoplira.Utils.SearchAdapterCallBack;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Search extends Fragment implements SearchAdapterCallBack {


    private static final String TAG = "MainActivity";

    searchHomeAdapter adapter;
    SearchQueryAdapter searchQueryAdapter;
    LinearLayoutManager linearLayoutManager;
    private String queryString;
    List<Product> products;
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
    private final int selectCategoryId = 3;

    List<SearchCategoriee> searchCategoriees;

    private MovieService movieService;
    private Object PaginationAdapterCallback;


    public Search() {
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        rv = view.findViewById(R.id.main_recycler);
        progressBar = view.findViewById(R.id.main_progress);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);

        txtError = view.findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = view.findViewById(R.id.main_swiperefresh);

        adapter = new searchHomeAdapter(getContext(), this);

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

        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);


        return view;
    }

    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callMostSearched().isExecuted())
            callMostSearched().cancel();

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

        callMostSearched().enqueue(new Callback<SearchHome>() {
            @Override
            public void onResponse(Call<SearchHome> call, Response<SearchHome> response) {
                hideErrorView();

//                Log.i(TAG, "onResponse: " + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                searchCategoriees = fetchResults(response);
                progressBar.setVisibility(View.GONE);
                if (searchCategoriees.isEmpty()) {
                    showCategoryErrorView();
                    return;
                } else {
                    adapter.addAll(searchCategoriees);
                }

                if (currentPage < TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<SearchHome> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }


    private List<SearchCategoriee> fetchResults(Response<SearchHome> response) {
        SearchHome searchHome = response.body();
        TOTAL_PAGES = searchHome.getTotalPages();
        System.out.println("total pages" + TOTAL_PAGES);

        return searchHome.getSearchCategoriees();
    }

    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callMostSearched().enqueue(new Callback<SearchHome>() {
            @Override
            public void onResponse(Call<SearchHome> call, Response<SearchHome> response) {
                Log.i(TAG, "onResponse: " + currentPage
                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                adapter.removeLoadingFooter();
                isLoading = false;

                searchCategoriees = fetchResults(response);
                adapter.addAll(searchCategoriees);

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<SearchHome> call, Throwable t) {
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
    private Call<SearchHome> callMostSearched() {
        return movieService.getMostSearched(
                currentPage
        );
    }

    @Override
    public void retryPageLoad() {
        loadNextPage();
    }

    @Override
    public void searchinput(String searchquery) {

        searchQueryAdapter = new SearchQueryAdapter(getContext(),  this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rv.setLayoutManager(gridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(searchQueryAdapter);


        rv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                loadQueryNextPage();
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


        queryString = searchquery;
        searchQueryAdapter.getMovies().clear();
        searchQueryAdapter.notifyDataSetChanged();
        loadQueryFirstPage();
        Toast.makeText(getContext(), "searchquery", Toast.LENGTH_SHORT).show();
    }



    private void loadQueryFirstPage() {
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
                if(products != null){
                    searchQueryAdapter.addAll(products);
                } else {
                    showCategoryErrorView();
                    return;
                }

                if (currentPage < TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }





    private List<Product> fetchqueryResults(Response<SearchResult> response) {
        SearchResult searchResult = response.body();
        TOTAL_PAGES = searchResult.getTotalPages();
        System.out.println("total pages" + TOTAL_PAGES);

        return searchResult.getProducts();
    }

    private void loadQueryNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callSearchAPI().enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                Log.i(TAG, "onResponse: " + currentPage
                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                adapter.removeLoadingFooter();
                isLoading = false;

                products = fetchqueryResults(response);

                if(products != null){
                    searchQueryAdapter.addAll(products);
                } else {
                    showCategoryErrorView();
                    return;
                }

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }



    private Call<SearchResult> callSearchAPI() {
        return movieService.getSearch(
                queryString,
                currentPage
        );
    }



    /**
     * @param throwable required for {@link #fetchErrorMessage(Throwable)}
     * @return
     */
    private void showErrorView(Throwable throwable) {

        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    private void showCategoryErrorView() {

        progressBar.setVisibility(View.GONE);

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