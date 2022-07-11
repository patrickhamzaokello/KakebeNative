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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
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
import com.shop.kakebe.KaKebe.Models.Product;
import com.shop.kakebe.KaKebe.MyMenuDetail;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.Utils.GlideApp;
import com.shop.kakebe.KaKebe.Utils.SearchAdapterCallBack;
import com.shop.kakebe.KaKebe.localDatabase.SenseDBHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Suleiman on 19/10/16.
 */

public class SearchQueryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";


    private List<Product> products;
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

    private final SearchAdapterCallBack mCallback;
    private String errorMsg;

    public SearchQueryAdapter(Context context, SearchAdapterCallBack callback) {
        this.context = context;
        this.mCallback = callback;
        products = new ArrayList<>();
    }

    public List<Product> getMovies() {
        return products;
    }

    public void setMovies(List<Product> movieSelectedCategoryMenuItemResults) {
        this.products = movieSelectedCategoryMenuItemResults;
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
        View v1 = inflater.inflate(R.layout.search_pagination_item_list, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        Product product = products.get(position); // Movie

        db = new SenseDBHelper(context);

        food_db_itemchecker = db.checktweetindb(String.valueOf(product.getId()));

        updatecartCount();



        switch (getItemViewType(position)) {
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;
                if (food_db_itemchecker) {


                    movieVH.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));
                    movieVH.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.black));

                } else {


                    movieVH.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));
                    movieVH.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.purple_200));

                }

                movieVH.item_name.setText(product.getName());
                movieVH.item_rating.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(product.getUnitPrice()));

                if(compare_values(product.getUnitPrice(),product.getDiscount())){
                    movieVH.item_rating.setPaintFlags(movieVH.item_rating.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    movieVH.discount_card.setVisibility(View.GONE);
                } else {
                    movieVH.discount_card.setVisibility(View.VISIBLE);
                    movieVH.discoutpercent.setText(cal_percentage(product.getDiscount(), product.getUnitPrice()));
                    movieVH.item_rating.setPaintFlags(movieVH.item_rating.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                movieVH.item_price.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(product.getDiscount()));


                Glide
                        .with(context)
                        .load(BASE_URL_IMG + product.getThumbnailImg())
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


                movieVH.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        food_db_itemchecker = db.checktweetindb(String.valueOf(product.getId()));


                        if (food_db_itemchecker) {
                            db.addTweet(
                                    product.getId(),
                                    product.getName(),
                                    product.getUnitPrice(),
                                    product.getCategoryId(),
                                    product.getThumbnailImg(),
                                    minteger,
                                    MENU_NOT_SYNCED_WITH_SERVER
                            );


                            movieVH.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));
                            movieVH.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.purple_200));
                            updatecartCount();


                        } else {
                            db.deleteTweet(String.valueOf(product.getId()));

                            movieVH.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));
                            movieVH.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.black));

                            updatecartCount();

                        }
                    }
                });

                //show toast on click of show all button
                movieVH.itemimage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context.getApplicationContext(), MyMenuDetail.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                        //PACK DATA
                        i.putExtra("SENDER_KEY", "MenuDetails");
                        i.putExtra("selectMenuId", product.getId());
                        i.putExtra("category_selected_key", product.getCategoryId());
                        context.startActivity(i);
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
        return products == null ? 0 : products.size();
    }

    @Override
    public int getItemViewType(int position) {

        return (position == products.size() - 1 && isLoadingAdded) ?
                LOADING : ITEM;
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



    private RequestBuilder<Drawable> loadImage(@NonNull String posterPath) {
        return GlideApp
                .with(context)
                .load(BASE_URL_IMG + posterPath)
                .centerCrop();
    }

    public void add(Product r) {
        products.add(r);
        notifyItemInserted(products.size() - 1);
    }

    public void addAll(List<Product> moveSelectedCategoryMenuItemResults) {
        for (Product selectedCategoryMenuItemResult : moveSelectedCategoryMenuItemResults) {
            add(selectedCategoryMenuItemResult);
        }
    }

    public void remove(Product r) {
        int position = products.indexOf(r);
        if (position > -1) {
            products.remove(position);
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
        add(new Product());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = products.size() - 1;
        Product selectedCategoryMenuItemResult = getItem(position);

        if (selectedCategoryMenuItemResult != null) {
            products.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(products.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public Product getItem(int position) {
        return products.get(position);
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