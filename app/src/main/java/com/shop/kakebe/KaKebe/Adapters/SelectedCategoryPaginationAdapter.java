package com.shop.kakebe.KaKebe.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
import com.google.android.material.card.MaterialCardView;
import com.shop.kakebe.KaKebe.Models.SelectedCategoryResult;
import com.shop.kakebe.KaKebe.MyMenuDetail;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.Utils.GlideApp;
import com.shop.kakebe.KaKebe.Utils.PaginationAdapterCallback;
import com.shop.kakebe.KaKebe.localDatabase.SenseDBHelper;

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

                //recycler view for items
                heroVh.itemRecyclerView.setHasFixedSize(true);
                heroVh.itemRecyclerView.setNestedScrollingEnabled(false);

                /* set layout manager on basis of recyclerview enum type */
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 1);
                heroVh.itemRecyclerView.setLayoutManager(gridLayoutManager);

                SelectedCategoryInfoAdapter adapter = new SelectedCategoryInfoAdapter(context, selectedCategoryResult.getCategoryInfo());
                heroVh.itemRecyclerView.setAdapter(adapter);


                break;
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;

                movieVH.item_name.setText(selectedCategoryResult.getName());

                if((selectedCategoryResult.getUnitPrice()) != null){
                    movieVH.item_price.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(selectedCategoryResult.getDiscount()));
                } else {
                    movieVH.item_price.setText("Null");
                }

                if(compare_values(selectedCategoryResult.getUnitPrice(),selectedCategoryResult.getDiscount())){
                    movieVH.item_rating.setPaintFlags(movieVH.item_rating.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    movieVH.discount_card.setVisibility(View.GONE);
                } else {
                    movieVH.discount_card.setVisibility(View.VISIBLE);
                    movieVH.discoutpercent.setText(cal_percentage(selectedCategoryResult.getDiscount(), selectedCategoryResult.getUnitPrice()));
                    movieVH.item_rating.setPaintFlags(movieVH.item_rating.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    movieVH.item_rating.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(selectedCategoryResult.getUnitPrice()));
                    movieVH.item_rating.setVisibility(View.VISIBLE);
                }



                if (food_db_itemchecker) {


                    movieVH.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));
                    movieVH.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.black));


                } else {


                    movieVH.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));
                    movieVH.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.purple_200));


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
                        .into(movieVH.itemimage);

                //show toast on click of show all button
                movieVH.itemimage.setOnClickListener(new View.OnClickListener() {
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

                movieVH.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        food_db_itemchecker = db.checktweetindb(String.valueOf(selectedCategoryResult.getId()));


                        if (food_db_itemchecker) {
                            db.addTweet(
                                    selectedCategoryResult.getId(),
                                    selectedCategoryResult.getName(),
                                    selectedCategoryResult.getUnitPrice(),
                                    selectedCategoryResult.getCategoryId(),
                                    selectedCategoryResult.getThumbnailImg(),
                                    minteger,
                                    MENU_NOT_SYNCED_WITH_SERVER
                            );



                            movieVH.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));
                            movieVH.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.purple_200));

                            updatecartCount();


                        } else {
                            db.deleteTweet(String.valueOf(selectedCategoryResult.getId()));

                            movieVH.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));
                            movieVH.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.black));

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


    private boolean compare_values(int unitprice, int discount){
        boolean val;
        if(unitprice == discount){
            val = true;
        } else {
            val = false;
        }
        return val;
    }

    public String cal_percentage(int discount, int unit_price){
        int val = (100 * discount / unit_price);
        val = Math.round(val - 0.5f);
        val = val - 100;
        String per_discount = val +"%";
        return per_discount;
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
        private TextView sectionLabel;
        private RecyclerView itemRecyclerView;

        public HeroVH(View itemView) {
            super(itemView);
            sectionLabel = (TextView) itemView.findViewById(R.id.section_label);
            itemRecyclerView = (RecyclerView) itemView.findViewById(R.id.item_recycler_view);
        }
    }

    protected class MovieVH extends RecyclerView.ViewHolder {
        private final TextView item_name;
        private final TextView item_price;
        private final TextView item_rating;
        private final TextView discoutpercent;
        private final ImageView itemimage;
        private final ProgressBar mProgress;

        private final MaterialCardView discount_card,home_addToCart_card;

        Button home_cart_state;
        View view;

        public MovieVH(View itemView) {
            super(itemView);

            itemimage = itemView.findViewById(R.id.product_imageview);
            item_name = itemView.findViewById(R.id.item_name);
            item_rating = itemView.findViewById(R.id.item_rating);
            item_price = itemView.findViewById(R.id.item_price);
            mProgress = itemView.findViewById(R.id.home_product_image_progress);
            discoutpercent = itemView.findViewById(R.id.discoutpercent);
            home_cart_state = itemView.findViewById(R.id.home_st_carttn);
            discount_card = (MaterialCardView) itemView.findViewById(R.id.discount_card);
            home_addToCart_card = (MaterialCardView) itemView.findViewById(R.id.home_addToCart);
            view = itemView;
            item_rating.setVisibility(View.GONE);
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