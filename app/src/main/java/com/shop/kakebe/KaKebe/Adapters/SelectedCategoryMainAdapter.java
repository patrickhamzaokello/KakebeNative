package com.shop.kakebe.KaKebe.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.shop.kakebe.KaKebe.HelperClasses.Utility;
import com.shop.kakebe.KaKebe.Models.SelectedCategoryResult;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.Utils.GlideApp;
import com.shop.kakebe.KaKebe.Utils.PaginationAdapterCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman on 19/10/16.
 */

public class SelectedCategoryMainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int HERO = 2;

    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";


    private List<SelectedCategoryResult> selectedCategoryResults;
    private final Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    private final PaginationAdapterCallback mCallback;
    private String errorMsg;

    public SelectedCategoryMainAdapter(Context context, PaginationAdapterCallback callback) {
        this.context = context;
        this.mCallback = callback;
        selectedCategoryResults = new ArrayList<>();
    }

    public List<SelectedCategoryResult> getMovies() {
        return selectedCategoryResults;
    }

    public void setMovies(List<SelectedCategoryResult> selectedCategoryResults) {
        this.selectedCategoryResults = selectedCategoryResults;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case HERO:
                View viewHero = inflater.inflate(R.layout.selected_category_item_hero, parent, false);
                viewHolder = new HeroVH(viewHero);
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
        View v1 = inflater.inflate(R.layout.selected_category_hero_design, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        SelectedCategoryResult selectedCategoryResult = selectedCategoryResults.get(position); // Movie

        switch (getItemViewType(position)) {
            case HERO:
                final HeroVH heroVh = (HeroVH) holder;
                heroVh.section_label.setText(selectedCategoryResult.getName());
                break;
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;

                //recycler view for items
                movieVH.itemRecyclerView.setHasFixedSize(true);
                movieVH.itemRecyclerView.setNestedScrollingEnabled(false);
                int mNo_OfColumns = Utility.calculateNoOfColumns(context, 152);

                /* set layout manager on basis of recyclerview enum type */
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, mNo_OfColumns);
                movieVH.itemRecyclerView.setLayoutManager(gridLayoutManager);


                SelectedCategoryProductItemAdapter adapter = new SelectedCategoryProductItemAdapter(context, selectedCategoryResult.getCategoryProducts());
                movieVH.itemRecyclerView.setAdapter(adapter);

                break;

            case LOADING:
//                Do nothing
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
        return selectedCategoryResults == null ? 0 : selectedCategoryResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HERO;
        } else {
            return (position == selectedCategoryResults.size() - 1 && isLoadingAdded) ?
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


    private RequestBuilder<Drawable> loadImage(@NonNull String posterPath) {
        return GlideApp
                .with(context)
                .load(BASE_URL_IMG + posterPath)
                .centerCrop();
    }

    public void add(SelectedCategoryResult r) {
        selectedCategoryResults.add(r);
        notifyItemInserted(selectedCategoryResults.size() - 1);
    }

    public void addAll(List<SelectedCategoryResult> selectedCategoryResults) {
        for (SelectedCategoryResult selectedCategoryResult : selectedCategoryResults) {
            add(selectedCategoryResult);
        }
    }

    public void remove(SelectedCategoryResult r) {
        int position = selectedCategoryResults.indexOf(r);
        if (position > -1) {
            selectedCategoryResults.remove(position);
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
        add(new SelectedCategoryResult());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = selectedCategoryResults.size() - 1;
        SelectedCategoryResult selectedCategoryResult = getItem(position);

        if (selectedCategoryResult != null) {
            selectedCategoryResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(selectedCategoryResults.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public SelectedCategoryResult getItem(int position) {
        return selectedCategoryResults.get(position);
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */

    protected class HeroVH extends RecyclerView.ViewHolder {
        private final TextView section_label;

        public HeroVH(View itemView) {
            super(itemView);
            section_label = itemView.findViewById(R.id.section_label);
        }
    }

    protected class MovieVH extends RecyclerView.ViewHolder {
        private RecyclerView itemRecyclerView;

        public MovieVH(View itemView) {
            super(itemView);
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