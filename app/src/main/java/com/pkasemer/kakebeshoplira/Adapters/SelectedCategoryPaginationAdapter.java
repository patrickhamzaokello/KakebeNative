package com.pkasemer.kakebeshoplira.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.pkasemer.kakebeshoplira.Models.SelectedCategoryMenuItemResult;
import com.pkasemer.kakebeshoplira.Models.SelectedCategoryResult;
import com.pkasemer.kakebeshoplira.MyMenuDetail;
import com.pkasemer.kakebeshoplira.R;
import com.pkasemer.kakebeshoplira.RootActivity;
import com.pkasemer.kakebeshoplira.Utils.GlideApp;
import com.pkasemer.kakebeshoplira.Utils.PaginationAdapterCallback;
import com.pkasemer.kakebeshoplira.localDatabase.SenseDBHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Suleiman on 19/10/16.
 */

public class SelectedCategoryPaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int HERO = 2;

//    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";


    private List<SelectedCategoryResult> selectedCategoryResults;
    private final Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    SenseDBHelper db;
    boolean food_db_itemchecker;


    int minteger = 1;
    int totalPrice;

    public static final int MENU_SYNCED_WITH_SERVER = 1;
    public static final int MENU_NOT_SYNCED_WITH_SERVER = 0;

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    private final PaginationAdapterCallback mCallback;
    private String errorMsg;

    public SelectedCategoryPaginationAdapter(Context context, PaginationAdapterCallback callback) {
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
        View v1 = inflater.inflate(R.layout.selected_category_pagination_item_list, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        SelectedCategoryResult selectedCategoryResult = selectedCategoryResults.get(position); // Movie

        db = new SenseDBHelper(context);

        food_db_itemchecker = db.checktweetindb(String.valueOf(selectedCategoryResult.getId()));

        updatecartCount();

        switch (getItemViewType(position)) {
            case HERO:
                final HeroVH heroVh = (HeroVH) holder;

//                heroVh.mMovieTitle.setText(selectedCategoryMenuItemResult.getMenuName());
//                heroVh.mYear.setText(formatYearLabel(selectedCategoryMenuItemResult));
//                heroVh.mMovieDesc.setText(selectedCategoryMenuItemResult.getDescription());
//
//                loadImage(selectedCategoryMenuItemResult.getBackgroundImage())
//                        .into(heroVh.mPosterImg);
                HomeMenuCategoryAdapter homeMenuCategoryAdapter = new HomeMenuCategoryAdapter(context);
//                StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
//                categoryVH.category_recycler_view.setLayoutManager(staggeredGridLayoutManager);

//                GridLayoutManager catgridLayoutManager = new GridLayoutManager(context, 3);
//                heroVh.category_recycler_view.setLayoutManager(catgridLayoutManager);
//
//                heroVh.category_recycler_view.setItemAnimator(new DefaultItemAnimator());
//                heroVh.category_recycler_view.setAdapter(homeMenuCategoryAdapter);
//                homeMenuCategoryAdapter.addAll(selectedCategoryResult.getCategoryInfo());
                break;
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;

                movieVH.mMovieTitle.setText(selectedCategoryResult.getName());

                movieVH.mMoviePrice.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(selectedCategoryResult.getUnitPrice()));

                movieVH.mYear.setText(
                        "5"
                );
                movieVH.mMovieDesc.setText(selectedCategoryResult.getMetaDescription());



                if (food_db_itemchecker) {


                    movieVH.selected_Category_plus.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));


                } else {


                    movieVH.selected_Category_plus.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));


                }


                Glide
                        .with(context)
                        .load(BASE_URL_IMG + selectedCategoryResult.getThumbnailImg())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                movieVH.mProgress.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                movieVH.mProgress.setVisibility(View.GONE);
                                return false;
                            }

                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                        .centerCrop()
                        .transition(withCrossFade(factory))
                        .into(movieVH.mPosterImg);

                //show toast on click of show all button
                movieVH.mPosterImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//
                        Intent i = new Intent(context.getApplicationContext(), MyMenuDetail.class);
                        //PACK DATA
                        i.putExtra("SENDER_KEY", "MenuDetails");
                        i.putExtra("selectMenuId", selectedCategoryResult.getId());
                        i.putExtra("category_selected_key", selectedCategoryResult.getCategoryId());
                        context.startActivity(i);
                    }
                });

                movieVH.selected_Category_plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        food_db_itemchecker = db.checktweetindb(String.valueOf(selectedCategoryResult.getId()));


                        if (food_db_itemchecker) {
//                            db.addTweet(
//                                    selectedCategoryMenuItemResult.getMenuId(),
//                                    selectedCategoryMenuItemResult.getMenuName(),
//                                    selectedCategoryMenuItemResult.getPrice(),
//                                    selectedCategoryMenuItemResult.getDescription(),
//                                    selectedCategoryMenuItemResult.getMenuTypeId(),
//                                    selectedCategoryMenuItemResult.getMenuImage(),
//                                    selectedCategoryMenuItemResult.getBackgroundImage(),
//                                    selectedCategoryMenuItemResult.getIngredients(),
//                                    selectedCategoryMenuItemResult.getMenuStatus(),
//                                    selectedCategoryMenuItemResult.getCreated(),
//                                    selectedCategoryMenuItemResult.getModified(),
//                                    selectedCategoryMenuItemResult.getRating(),
//                                    minteger,
//                                    MENU_NOT_SYNCED_WITH_SERVER
//                            );



                            movieVH.selected_Category_plus.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));

                            updatecartCount();


                        } else {
                            db.deleteTweet(String.valueOf(selectedCategoryResult.getId()));

                            movieVH.selected_Category_plus.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));


                            updatecartCount();

                        }
                    }
                });

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


    private void updatecartCount() {
        db = new SenseDBHelper(context);
        String mycartcount = String.valueOf(db.countCart());
        Intent intent = new Intent(context.getString(R.string.cartcoutAction));
        intent.putExtra(context.getString(R.string.cartCount), mycartcount);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }



    /*
   Helpers
   _________________________________________________________________________________________________
    */



    private RequestBuilder< Drawable > loadImage(@NonNull String posterPath) {
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
        private final TextView mMovieTitle;
        private final TextView mMovieDesc;
        private final TextView mYear;
        private final ImageView mPosterImg;

        public HeroVH(View itemView) {
            super(itemView);
            // init views
            mMovieTitle = itemView.findViewById(R.id.movie_title);
            mMovieDesc = itemView.findViewById(R.id.movie_desc);
            mYear = itemView.findViewById(R.id.movie_year);
            mPosterImg = itemView.findViewById(R.id.movie_poster);
        }
    }

    protected class MovieVH extends RecyclerView.ViewHolder {
        private final TextView mMovieTitle;
        private final TextView mMovieDesc;
        private final TextView mMoviePrice;
        private final TextView mYear; // displays "year | language"
        private final ImageView mPosterImg;
        private final ProgressBar mProgress;

        private final Button selected_Category_plus;

        public MovieVH(View itemView) {
            super(itemView);

            mMovieTitle = itemView.findViewById(R.id.movie_title);
            mMovieDesc = itemView.findViewById(R.id.movie_desc);
            mMoviePrice = itemView.findViewById(R.id.movie_price);
            mYear = itemView.findViewById(R.id.movie_year);
            mPosterImg = itemView.findViewById(R.id.movie_poster);
            mProgress = itemView.findViewById(R.id.movie_progress);
            selected_Category_plus = itemView.findViewById(R.id.selected_Category_plus);
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