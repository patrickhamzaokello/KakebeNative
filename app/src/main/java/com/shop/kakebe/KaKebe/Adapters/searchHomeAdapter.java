package com.shop.kakebe.KaKebe.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.shop.kakebe.KaKebe.Models.SearchCategoriee;
import com.shop.kakebe.KaKebe.MySelectedCategory;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.Utils.SearchAdapterCallBack;
import com.shop.kakebe.KaKebe.Utils.SearchPopularTagAdapterCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman on 19/10/16.
 */

public class searchHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SearchPopularTagAdapterCallBack {

    private static final int LOADING = 0;
    private static final int HERO = 1;
    private static final int CATEGORY = 2;

    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";


    private List<SearchCategoriee> searchCategoriees;
    private final Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;


    private final SearchAdapterCallBack mCallback;
    private String errorMsg;

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    public searchHomeAdapter(Context context, SearchAdapterCallBack callback) {
        this.context = context;
        this.mCallback = callback;
        searchCategoriees = new ArrayList<>();
    }

    public List<SearchCategoriee> getMovies() {
        return searchCategoriees;
    }

    public void setMovies(List<SearchCategoriee> searchCategoriees) {
        this.searchCategoriees = searchCategoriees;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case HERO:
                View viewHero = inflater.inflate(R.layout.search_hero_layout, parent, false);
                viewHolder = new HeroVH(viewHero);
                break;
            case CATEGORY:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.pagination_item_progress, parent, false);
                viewHolder = new LoadingVH(v2);
                break;
        }
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.search_featured_category_recycler, parent, false);
        viewHolder = new CategoryVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        SearchCategoriee searchCategoriee = searchCategoriees.get(position); // Movie

        switch (getItemViewType(position)) {
            case HERO:
                final HeroVH heroVh = (HeroVH) holder;
                searchPopularQueryAdapter searchPopularQueryAdapter = new searchPopularQueryAdapter(context, this);
                StaggeredGridLayoutManager previous_search_staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.HORIZONTAL);
                heroVh.category_recycler_view.setLayoutManager(previous_search_staggeredGridLayoutManager);
                heroVh.category_recycler_view.setItemAnimator(new DefaultItemAnimator());
                heroVh.category_recycler_view.setAdapter(searchPopularQueryAdapter);
                searchPopularQueryAdapter.addAll(searchCategoriee.getPopularSearch());

                break;
            case CATEGORY:
                final CategoryVH categoryVH = (CategoryVH) holder;
                searchFeaturedCategoryAdapter searchFeaturedCategoryAdapter = new searchFeaturedCategoryAdapter(context);

                GridLayoutManager catgrid = new GridLayoutManager(context, 3);
                categoryVH.category_recycler_view.setLayoutManager(catgrid);

                categoryVH.category_recycler_view.setItemAnimator(new DefaultItemAnimator());
                categoryVH.category_recycler_view.setAdapter(searchFeaturedCategoryAdapter);
                searchFeaturedCategoryAdapter.addAll(searchCategoriee.getFeaturedCategories());


                break;

            case LOADING:
                LoadingVH loadingVH = (LoadingVH) holder;

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
                    loadingVH.mProgressBar.setVisibility(View.GONE);

                    loadingVH.mErrorTxt.setText(
                            errorMsg != null ?
                                    errorMsg :
                                    context.getString(R.string.error_msg_unknown));

                } else {
                    loadingVH.mErrorLayout.setVisibility(View.GONE);
                    loadingVH.mProgressBar.setVisibility(View.VISIBLE);
                }

                break;
        }

    }

    @Override
    public int getItemCount() {
        return searchCategoriees == null ? 0 : searchCategoriees.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HERO;
        } else {
            return (position == searchCategoriees.size() - 1 && isLoadingAdded) ?
                    LOADING : CATEGORY;
        }
    }

    public void switchContent(int id, Fragment fragment) {
        if (context == null)
            return;
        if (context instanceof RootActivity) {
            RootActivity mainActivity = (RootActivity) context;
            Fragment frag = fragment;
            mainActivity.switchContent(id, frag, "MenuDetails");
        }

    }





    /*
   Helpers
   _________________________________________________________________________________________________
    */


    public void add(SearchCategoriee r) {
        searchCategoriees.add(r);
        notifyItemInserted(searchCategoriees.size() - 1);
    }

    public void addAll(List<SearchCategoriee> searchCategoriees) {
        for (SearchCategoriee searchCategoriee : searchCategoriees) {
            add(searchCategoriee);
        }
    }

    public void remove(SearchCategoriee r) {
        int position = searchCategoriees.indexOf(r);
        if (position > -1) {
            searchCategoriees.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new SearchCategoriee());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = searchCategoriees.size() - 1;
        SearchCategoriee searchCategoriee = getItem(position);

        if (searchCategoriee != null) {
            searchCategoriees.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(searchCategoriees.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public SearchCategoriee getItem(int position) {
        return searchCategoriees.get(position);
    }

    @Override
    public void searchpopulartag(String searchquery) {
//        Toast.makeText(context, searchquery, Toast.LENGTH_SHORT).show();
        //send the tag to Search Java
        mCallback.searchinput(searchquery);
    }

   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */

    protected class HeroVH extends RecyclerView.ViewHolder {

        private final RecyclerView category_recycler_view;
        private final SearchView search_bar;

        public HeroVH(View itemView) {
            super(itemView);
            // init views
            category_recycler_view = itemView.findViewById(R.id.search_category_recycler_view);
            search_bar = itemView.findViewById(R.id.search_bar);

            search_bar.setQueryHint("Search for Products");
            search_bar.setActivated(true);
            search_bar.onActionViewExpanded();
            search_bar.setIconified(false);
            search_bar.clearFocus();

            // below line is to call set on query text listener method.
            search_bar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    mCallback.searchinput(query);

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    // inside on query text change method we are
                    // calling a method to filter our recycler view.
//                    mCallback.searchinput(newText);

                    return false;
                }
            });


        }
    }

    protected class CategoryVH extends RecyclerView.ViewHolder {

        private final RecyclerView category_recycler_view;

        public CategoryVH(View itemView) {
            super(itemView);
            // init views
            category_recycler_view = itemView.findViewById(R.id.category_recycler_view);

        }
    }



    protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ProgressBar mProgressBar;
        private final ImageButton mRetryBtn;
        private final TextView mErrorTxt;
        private final LinearLayout mErrorLayout;

        public LoadingVH(View itemView) {
            super(itemView);

            mProgressBar = itemView.findViewById(R.id.loadmore_progress);
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry);
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt);
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout);

            mRetryBtn.setOnClickListener(this);
            mErrorLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.loadmore_retry:
                case R.id.loadmore_errorlayout:
                    showRetry(false, null);
                    mCallback.retryPageLoad();
                    break;
            }
        }
    }


}