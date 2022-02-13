package com.pkasemer.kakebeshoplira.Fragments;


import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pkasemer.kakebeshoplira.Adapters.SearchAdapter;
import com.pkasemer.kakebeshoplira.Apis.MovieApi;
import com.pkasemer.kakebeshoplira.Apis.MovieService;
import com.pkasemer.kakebeshoplira.Models.Product;
import com.pkasemer.kakebeshoplira.Models.SearchCategoriee;
import com.pkasemer.kakebeshoplira.Models.SearchHome;
import com.pkasemer.kakebeshoplira.Models.SearchResult;
import com.pkasemer.kakebeshoplira.R;
import com.pkasemer.kakebeshoplira.Utils.GridPaginationScrollListener;
import com.pkasemer.kakebeshoplira.Utils.PaginationAdapterCallback;
import com.pkasemer.kakebeshoplira.Utils.PaginationScrollListener;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchOrig extends Fragment implements PaginationAdapterCallback {

    private static final String TAG = "MainActivity";

    SearchAdapter adapter;
    GridLayoutManager gridLayoutManager;

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
    private String queryString;
    private final int selectCategoryId = 3;

    List<Product> products;

    private MovieService movieService;
    private Object PaginationAdapterCallback;



    public SearchOrig() {
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


        rv = view.findViewById(R.id.seach_main_recycler);
        progressBar = view.findViewById(R.id.seach_main_progress);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = view.findViewById(R.id.seach_main_swiperefresh);

        adapter = new SearchAdapter(getContext(),  this);

        gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rv.setLayoutManager(gridLayoutManager);


        rv.setItemAnimator(new DefaultItemAnimator());

        rv.setAdapter(adapter);

        rv.addOnScrollListener(new GridPaginationScrollListener(gridLayoutManager) {
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


        btnRetry.setOnClickListener(v -> loadFirstPage());

        swipeRefreshLayout.setOnRefreshListener(this::doRefresh);


        // Locate the EditText in listview_main.xml
        SearchView searchView = view.findViewById(R.id.search_bar);

        searchView.setActivated(true);
        searchView.setQueryHint("Type your keyword here");
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.clearFocus();

        // below line is to call set on query text listener method.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // inside on query text change method we are
                // calling a method to filter our recycler view.
                queryString = newText;
                adapter.getMovies().clear();
                adapter.notifyDataSetChanged();
                loadFirstPage();
                return false;
            }
        });


        return view;
    }



    private void doRefresh() {
        progressBar.setVisibility(View.VISIBLE);
        if (callSearchAPI().isExecuted())
            callSearchAPI().cancel();

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

        callSearchAPI().enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                hideErrorView();

//                Log.i(TAG, "onResponse: " + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                // Got data. Send it to adapter
                products = fetchResults(response);
                progressBar.setVisibility(View.GONE);
                if(products != null){
                    adapter.addAll(products);
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





    private List<Product> fetchResults(Response<SearchResult> response) {
        SearchResult searchResult = response.body();
        TOTAL_PAGES = searchResult.getTotalPages();
        System.out.println("total pages" + TOTAL_PAGES);

        return searchResult.getProducts();
    }

    private void loadNextPage() {
        Log.d(TAG, "loadNextPage: " + currentPage);

        callSearchAPI().enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                Log.i(TAG, "onResponse: " + currentPage
                        + (response.raw().cacheResponse() != null ? "Cache" : "Network"));

                adapter.removeLoadingFooter();
                isLoading = false;

                products = fetchResults(response);

                if(products != null){
                    adapter.addAll(products);
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


    /**
     * Performs a Retrofit call to the top rated movies API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<SearchResult> callSearchAPI() {
        return movieService.getSearch(
                queryString,
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
            progressBar.setVisibility(View.GONE);

            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    private void showCategoryErrorView() {

        progressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), "No Result" ,Toast.LENGTH_SHORT).show();

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