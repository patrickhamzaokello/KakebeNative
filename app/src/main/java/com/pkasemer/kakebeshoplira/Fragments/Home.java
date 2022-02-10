package com.pkasemer.kakebeshoplira.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.pkasemer.kakebeshoplira.Adapters.HomeMenuCategoryAdapter;
import com.pkasemer.kakebeshoplira.Adapters.HomeSectionedRecyclerViewAdapter;
import com.pkasemer.kakebeshoplira.Adapters.HomeSliderAdapter;
import com.pkasemer.kakebeshoplira.Apis.MovieApi;
import com.pkasemer.kakebeshoplira.Apis.MovieService;
import com.pkasemer.kakebeshoplira.Models.Banner;
import com.pkasemer.kakebeshoplira.Models.Category;
import com.pkasemer.kakebeshoplira.Models.HomeBannerModel;
import com.pkasemer.kakebeshoplira.Models.HomeCategories;
import com.pkasemer.kakebeshoplira.Models.HomeMenuCategoryModel;
import com.pkasemer.kakebeshoplira.Models.HomeMenuCategoryModelResult;
import com.pkasemer.kakebeshoplira.R;
import com.pkasemer.kakebeshoplira.RootActivity;
import com.smarteist.autoimageslider.SliderView;

import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Home extends Fragment  {

    public Home() {
        // Required empty public constructor
    }

    private static final String TAG = "MainActivity";
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError;

    RecyclerView sectionedmenurecyclerView;
    List<Category> categories;
    private MovieService movieService;
    private Object PaginationAdapterCallback;




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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // new
        progressBar = view.findViewById(R.id.main_progress);
        errorLayout = view.findViewById(R.id.error_layout);
        btnRetry = view.findViewById(R.id.error_btn_retry);
        txtError = view.findViewById(R.id.error_txt_cause);


        //init service and load data
        movieService = MovieApi.getClient(getContext()).create(MovieService.class);

        setUpHomeSectionRecyclerView(view);

        btnRetry.setOnClickListener(v -> {
            loadFirstPage();
        });


        return view;
    }



    @Override
    public void onResume() {
        super.onResume();
        loadFirstPage();

    }




    private void loadFirstPage() {

        class LoadFirstPage extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                Log.d(TAG, "loadFirstPage: ");

                // To ensure list is visible when retry button in error view is clicked
                hideErrorView();
                populateHomeSectionRecyclerView();
            }

            @Override
            protected String doInBackground(Void... voids) {
                return "done";
            }
        }

        LoadFirstPage ulLoadFirstPage = new LoadFirstPage();
        ulLoadFirstPage.execute();
    }


    //populate Sectioned view
    private void populateHomeSectionRecyclerView() {
        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();
        callGetSectionedCategoriesApi().enqueue(new Callback<HomeCategories>() {
            @Override
            public void onResponse(Call<HomeCategories> call, Response<HomeCategories> response) {
                hideErrorView();
                // Got data. Send it to adapter
                categories = fetchSectionedResults(response);
                progressBar.setVisibility(View.GONE);
                if (categories.isEmpty()) {
                    return;
                } else {
                    HomeSectionedRecyclerViewAdapter adapter = new HomeSectionedRecyclerViewAdapter(getContext(), categories);
                    sectionedmenurecyclerView.setAdapter(adapter);
                }

            }

            @Override
            public void onFailure(Call<HomeCategories> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });

    }

    private void setUpHomeSectionRecyclerView(View view) {
        sectionedmenurecyclerView = (RecyclerView) view.findViewById(R.id.main_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        sectionedmenurecyclerView.setLayoutManager(linearLayoutManager);
    }





    private List<Category> fetchSectionedResults(Response<HomeCategories> response) {
        HomeCategories homeCategories = response.body();
        int TOTAL_PAGES = homeCategories.getTotalPages();
        System.out.println("total pages cat" + TOTAL_PAGES);
        return homeCategories.getCategories();
    }





    private Call<HomeCategories> callGetSectionedCategoriesApi() {
        return movieService.getMenuCategoriesSection(
                1
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

            showCategoryErrorView();

        }
    }

    private void showCategoryErrorView() {

        progressBar.setVisibility(View.GONE);

        AlertDialog.Builder android = new AlertDialog.Builder(getContext());
        android.setTitle("No Internet Connection");
        android.setIcon(R.drawable.africanwoman);
        android.setMessage("Check your Internet Connection and  Try again.!  Error Code: Zodongo4M301.")
//                .setCancelable(false)

                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
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