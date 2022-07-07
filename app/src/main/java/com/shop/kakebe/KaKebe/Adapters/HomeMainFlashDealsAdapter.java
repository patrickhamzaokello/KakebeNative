package com.shop.kakebe.KaKebe.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.shop.kakebe.KaKebe.HelperClasses.Utility;
import com.shop.kakebe.KaKebe.Models.FeaturedCategory;
import com.shop.kakebe.KaKebe.Models.FlashDeal;
import com.shop.kakebe.KaKebe.Models.SelectedCategoryMenuItemResult;
import com.shop.kakebe.KaKebe.MySelectedCategory;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.Utils.GlideApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman on 19/10/16.
 */

public class HomeMainFlashDealsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";


    private List<FlashDeal> featuredCategories;
    private final Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    private String errorMsg;

    public HomeMainFlashDealsAdapter(Context context) {
        this.context = context;
        featuredCategories = new ArrayList<>();
    }

    public List<FlashDeal> getMovies() {
        return featuredCategories;
    }

    public void setMovies(List<FlashDeal> featuredCategories) {
        this.featuredCategories = featuredCategories;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
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
        View v1 = inflater.inflate(R.layout.home_flashdeals_design_design, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        FlashDeal featuredCategory = featuredCategories.get(position); // Movie

        switch (getItemViewType(position)) {
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;

                movieVH.flash_main_name.setText(featuredCategory.getName());
                movieVH.flash_name.setText("Expires in "+ featuredCategory.getTimeleft());

                LinearLayoutManager HorizontalLayout = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                movieVH.flash_product_recycler_view.setLayoutManager(HorizontalLayout);
//              movieVH.flash_product_recycler_view.setBackgroundColor(Color.parseColor(featuredCategory.getBackgroundColor()));
                HomeFlashDealItemAdapter adapter = new HomeFlashDealItemAdapter(context, featuredCategory.getFlashProducts());
                movieVH.flash_product_recycler_view.setAdapter(adapter);

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
        return featuredCategories == null ? 0 : featuredCategories.size();
    }

    @Override
    public int getItemViewType(int position) {

        return (position == featuredCategories.size() - 1 && isLoadingAdded) ?
                LOADING : ITEM;

    }


    public void switchContent(int id, Fragment fragment) {
        if (context == null)
            return;
        if (context instanceof RootActivity) {
            RootActivity mainActivity = (RootActivity) context;
            Fragment frag = fragment;
            mainActivity.switchContent(id, frag, "CategoryDetails");
        }

    }




    /*
   Helpers
   _________________________________________________________________________________________________
    */

    private String formatYearLabel(SelectedCategoryMenuItemResult selectedCategoryMenuItemResult) {
        return "Created | " +
                selectedCategoryMenuItemResult.getCreated().substring(0, 4);
    }

    private RequestBuilder<Drawable> loadImage(@NonNull String posterPath) {
        return GlideApp
                .with(context)
                .load(BASE_URL_IMG + posterPath)
                .centerCrop();
    }

    public void add(FlashDeal r) {
        featuredCategories.add(r);
        notifyItemInserted(featuredCategories.size() - 1);
    }

    public void addAll(List<FlashDeal> featuredCategories) {
        for (FlashDeal featuredCategory : featuredCategories) {
            add(featuredCategory);
        }
    }

    public void remove(FlashDeal r) {
        int position = featuredCategories.indexOf(r);
        if (position > -1) {
            featuredCategories.remove(position);
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
        add(new FlashDeal());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = featuredCategories.size() - 1;
        FlashDeal featuredCategory = getItem(position);

        if (featuredCategory != null) {
            featuredCategories.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(featuredCategories.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public FlashDeal getItem(int position) {
        return featuredCategories.get(position);
    }

    @Override
    public Filter getFilter() {
        return null;
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */



    protected class MovieVH extends RecyclerView.ViewHolder {

        private final ProgressBar mProgress;
        private  final  TextView flash_main_name,flash_name;
        private  final  RecyclerView flash_product_recycler_view;

        public MovieVH(View itemView) {
            super(itemView);
            mProgress = itemView.findViewById(R.id.cat_product_image_progress);
            flash_main_name = itemView.findViewById(R.id.flash_main_name);
            flash_name = itemView.findViewById(R.id.flash_name);
            flash_product_recycler_view = itemView.findViewById(R.id.flash_product_recycler_view);
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
                    break;
            }
        }
    }


}