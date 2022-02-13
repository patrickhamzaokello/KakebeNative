package com.pkasemer.kakebeshoplira.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.pkasemer.kakebeshoplira.Models.SearchCategoriee;
import com.pkasemer.kakebeshoplira.R;
import com.pkasemer.kakebeshoplira.RootActivity;
import com.pkasemer.kakebeshoplira.Utils.PaginationAdapterCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman on 19/10/16.
 */

public class SearchModernRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int HERO = 2;
    private static final int CATEGORY = 3;

    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";


    private List<SearchCategoriee> searchCategoriees;
    private final Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;


    private final PaginationAdapterCallback mCallback;
    private String errorMsg;

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    public SearchModernRecyclerViewAdapter(Context context, PaginationAdapterCallback callback) {
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
                View viewCategory = inflater.inflate(R.layout.home_category_recycler, parent, false);
                viewHolder = new CategoryVH(viewCategory);
                break;
            case ITEM:
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
        View v1 = inflater.inflate(R.layout.home_section_custom_row_layout, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        SearchCategoriee searchCategoriee = searchCategoriees.get(position); // Movie

        switch (getItemViewType(position)) {
            case HERO:
                final HeroVH heroVh = (HeroVH) holder;
                SearchPreviousModernAdapter searchPreviousModernAdapter = new SearchPreviousModernAdapter(context);
                StaggeredGridLayoutManager previous_search_staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.HORIZONTAL);
                heroVh.category_recycler_view.setLayoutManager(previous_search_staggeredGridLayoutManager);
                heroVh.category_recycler_view.setItemAnimator(new DefaultItemAnimator());
                heroVh.category_recycler_view.setAdapter(searchPreviousModernAdapter);
                searchPreviousModernAdapter.addAll(searchCategoriee.getPopularSearch());

                break;
            case CATEGORY:
                final CategoryVH categoryVH = (CategoryVH) holder;
                HomeMenuCategoryAdapter homeMenuCategoryAdapter = new HomeMenuCategoryAdapter(context);
                StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
                categoryVH.category_recycler_view.setLayoutManager(staggeredGridLayoutManager);
                categoryVH.category_recycler_view.setItemAnimator(new DefaultItemAnimator());
                categoryVH.category_recycler_view.setAdapter(homeMenuCategoryAdapter);
                homeMenuCategoryAdapter.addAll(searchCategoriee.getFeaturedCategories());


                break;
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;
                movieVH.sectionLabel.setText(searchCategoriee.getName());

                //recycler view for items
                movieVH.itemRecyclerView.setHasFixedSize(true);
                movieVH.itemRecyclerView.setNestedScrollingEnabled(false);

                /* set layout manager on basis of recyclerview enum type */
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
                movieVH.itemRecyclerView.setLayoutManager(gridLayoutManager);


                HomeSectionedRecyclerViewItemAdapter adapter = new HomeSectionedRecyclerViewItemAdapter(context, searchCategoriee.getProducts());
                movieVH.itemRecyclerView.setAdapter(adapter);


                //show toast on click of show all button
                movieVH.showAllButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(v.getContext(), searchCategoriee.getName(), Toast.LENGTH_SHORT).show();

                    }
                });


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
        } else if(position == 1){
            return CATEGORY;
        }
        else {
            return (position == searchCategoriees.size() - 1 && isLoadingAdded) ?
                    LOADING : ITEM;
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

            search_bar.setQueryHint("Search for Products on Kakebe");


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

    protected class MovieVH extends RecyclerView.ViewHolder {
        private TextView sectionLabel, showAllButton;
        private RecyclerView itemRecyclerView;

        public MovieVH(View itemView) {
            super(itemView);

            sectionLabel = (TextView) itemView.findViewById(R.id.section_label);
            showAllButton = (TextView) itemView.findViewById(R.id.section_show_all_button);
            itemRecyclerView = (RecyclerView) itemView.findViewById(R.id.item_recycler_view);
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