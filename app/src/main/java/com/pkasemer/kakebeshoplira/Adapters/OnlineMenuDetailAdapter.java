package com.pkasemer.kakebeshoplira.Adapters;

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
import androidx.core.content.ContextCompat;
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
import com.pkasemer.kakebeshoplira.Models.SelectedCategoryMenuItemResult;
import com.pkasemer.kakebeshoplira.Models.SelectedProduct;
import com.pkasemer.kakebeshoplira.MyMenuDetail;
import com.pkasemer.kakebeshoplira.PlaceOrder;
import com.pkasemer.kakebeshoplira.R;
import com.pkasemer.kakebeshoplira.RootActivity;
import com.pkasemer.kakebeshoplira.Utils.GlideApp;
import com.pkasemer.kakebeshoplira.Utils.MenuDetailListener;
import com.pkasemer.kakebeshoplira.localDatabase.SenseDBHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Suleiman on 19/10/16.
 */

public class OnlineMenuDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int HERO = 2;

    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";

    public static final int MENU_SYNCED_WITH_SERVER = 1;
    public static final int MENU_NOT_SYNCED_WITH_SERVER = 0;


    private List<SelectedProduct> selectedProducts;
    private final Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    private final MenuDetailListener mCallback;
    private String errorMsg;


    int minteger = 1;
    int totalPrice;

    SenseDBHelper db;
    boolean food_db_itemchecker;

    public OnlineMenuDetailAdapter(Context context, MenuDetailListener callback) {
        this.context = context;
        this.mCallback = callback;
        selectedProducts = new ArrayList<>();
    }

    public List<SelectedProduct> getMovies() {
        return selectedProducts;
    }

    public void setMovies(List<SelectedProduct> selectedProducts) {
        this.selectedProducts = selectedProducts;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case HERO:
                View viewHero = inflater.inflate(R.layout.online_menudetail_hero, parent, false);
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
        View v1 = inflater.inflate(R.layout.online_menudetail_pagination_item_list, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        SelectedProduct selectedProduct = selectedProducts.get(position); // Movie
        db = new SenseDBHelper(context);

        switch (getItemViewType(position)) {
            case HERO:
                final HeroVH heroVh = (HeroVH) holder;


                heroVh.menu_name.setText(selectedProduct.getName());

                if ((selectedProduct.getMetaDescription()).length() >= 20) {
                    heroVh.descriptionlayout.setVisibility(View.VISIBLE);
                    heroVh.menu_description.setText(selectedProduct.getMetaDescription());
                } else {
                    heroVh.descriptionlayout.setVisibility(View.GONE);
                }


                Glide
                        .with(context)
                        .load(BASE_URL_IMG + selectedProduct.getThumbnailImg())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                heroVh.mProgress.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                heroVh.mProgress.setVisibility(View.GONE);
                                return false;
                            }

                        })
                        .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                        .centerCrop()
                        .transition(withCrossFade(factory))
                        .into(heroVh.menu_image);


                heroVh.menu_price.setText(NumberFormat.getNumberInstance(Locale.US).format(selectedProduct.getUnitPrice()));
                heroVh.menu_qtn.setText("1");
                heroVh.menu_total_price.setText(NumberFormat.getNumberInstance(Locale.US).format(selectedProduct.getUnitPrice()));

                food_db_itemchecker = db.checktweetindb(String.valueOf(selectedProduct.getId()));

                updatecartCount();

                if (food_db_itemchecker) {
                    heroVh.btnAddtoCart.setText("Add to Cart");
                    heroVh.btnAddtoCart.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_200));
                    heroVh.btnAddtoCart.setTextColor(ContextCompat.getColor(context, R.color.white));
                    heroVh.itemQuanEt.setText("1");

                    heroVh.menu_detail_st_cartbtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_btn));
                    heroVh.menu_detail_st_likebtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_like_btn));


                } else {
                    heroVh.btnAddtoCart.setText("Remove");
                    heroVh.btnAddtoCart.setBackgroundColor(ContextCompat.getColor(context, R.color.buttonGreen));
                    heroVh.btnAddtoCart.setTextColor(ContextCompat.getColor(context, R.color.white));

                    minteger = db.getMenuQtn(String.valueOf(selectedProduct.getId()));

                    display(minteger, heroVh, selectedProduct);

                    heroVh.menu_detail_st_cartbtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_btn_done));
                    heroVh.menu_detail_st_likebtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_like_btn_done));


                }

                heroVh.incrementQtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        minteger = minteger + 1;
                        display(minteger, heroVh, selectedProduct);
                    }
                });

                heroVh.decreaseQtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        minteger = minteger - 1;

                        if (minteger <= 1) {
                            minteger = 1;
                            display(minteger, heroVh, selectedProduct);
                        } else {
                            display(minteger, heroVh, selectedProduct);
                        }
                    }
                });

                heroVh.btnOrderNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        food_db_itemchecker = db.checktweetindb(String.valueOf(selectedProduct.getId()));


                        if (food_db_itemchecker) {
                            db.addTweet(
                                    selectedProduct.getId(),
                                    selectedProduct.getName(),
                                    selectedProduct.getUnitPrice(),
                                    selectedProduct.getCategoryId(),
                                    selectedProduct.getThumbnailImg(),
                                    minteger,
                                    MENU_NOT_SYNCED_WITH_SERVER
                            );


                            updatecartCount();


                            Intent i = new Intent(context.getApplicationContext(), PlaceOrder.class);
                            //PACK DATA
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);


                        } else {
                            updatecartCount();
                            Intent i = new Intent(context.getApplicationContext(), PlaceOrder.class);
                            //PACK DATA
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);
                        }
                    }
                });

                heroVh.btnAddtoCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        food_db_itemchecker = db.checktweetindb(String.valueOf(selectedProduct.getId()));


                        if (food_db_itemchecker) {
                            db.addTweet(
                                    selectedProduct.getId(),
                                    selectedProduct.getName(),
                                    selectedProduct.getUnitPrice(),
                                    selectedProduct.getCategoryId(),
                                    selectedProduct.getThumbnailImg(),
                                    minteger,
                                    MENU_NOT_SYNCED_WITH_SERVER
                            );


                            heroVh.btnAddtoCart.setText("Remove");
                            heroVh.btnAddtoCart.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.buttonGreen));
                            heroVh.btnAddtoCart.setTextColor(ContextCompat.getColor(v.getContext(), R.color.white));

                            heroVh.menu_detail_st_cartbtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_btn_done));
                            heroVh.menu_detail_st_likebtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_like_btn_done));


                            updatecartCount();


                        } else {
                            db.deleteTweet(String.valueOf(selectedProduct.getId()));

                            heroVh.btnAddtoCart.setText("Add to Cart");
                            heroVh.btnAddtoCart.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_200));
                            heroVh.btnAddtoCart.setTextColor(ContextCompat.getColor(context, R.color.white));


                            heroVh.menu_detail_st_cartbtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_btn));
                            heroVh.menu_detail_st_likebtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_like_btn));

                            updatecartCount();

                        }
                    }
                });


                heroVh.menu_detail_st_cartbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        food_db_itemchecker = db.checktweetindb(String.valueOf(selectedProduct.getId()));


                        if (food_db_itemchecker) {
                            db.addTweet(
                                    selectedProduct.getId(),
                                    selectedProduct.getName(),
                                    selectedProduct.getUnitPrice(),
                                    selectedProduct.getCategoryId(),
                                    selectedProduct.getThumbnailImg(),
                                    minteger,
                                    MENU_NOT_SYNCED_WITH_SERVER
                            );


                            heroVh.btnAddtoCart.setText("Remove");
                            heroVh.btnAddtoCart.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.buttonGreen));
                            heroVh.btnAddtoCart.setTextColor(ContextCompat.getColor(v.getContext(), R.color.white));

                            heroVh.menu_detail_st_cartbtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_btn_done));
                            heroVh.menu_detail_st_likebtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_like_btn_done));

                            updatecartCount();


                        } else {
                            db.deleteTweet(String.valueOf(selectedProduct.getId()));

                            heroVh.btnAddtoCart.setText("Add to Cart");
                            heroVh.btnAddtoCart.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_200));
                            heroVh.btnAddtoCart.setTextColor(ContextCompat.getColor(context, R.color.white));


                            heroVh.menu_detail_st_cartbtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_btn));
                            heroVh.menu_detail_st_likebtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_like_btn));


                            updatecartCount();

                        }
                    }
                });


                heroVh.menu_detail_st_likebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        food_db_itemchecker = db.checktweetindb(String.valueOf(selectedProduct.getId()));


                        if (food_db_itemchecker) {
                            db.addTweet(
                                    selectedProduct.getId(),
                                    selectedProduct.getName(),
                                    selectedProduct.getUnitPrice(),
                                    selectedProduct.getCategoryId(),
                                    selectedProduct.getThumbnailImg(),
                                    minteger,
                                    MENU_NOT_SYNCED_WITH_SERVER
                            );


                            heroVh.btnAddtoCart.setText("Remove");
                            heroVh.btnAddtoCart.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.buttonGreen));
                            heroVh.btnAddtoCart.setTextColor(ContextCompat.getColor(v.getContext(), R.color.white));

                            heroVh.menu_detail_st_cartbtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_btn_done));
                            heroVh.menu_detail_st_likebtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_like_btn_done));

                            updatecartCount();


                        } else {
                            db.deleteTweet(String.valueOf(selectedProduct.getId()));

                            heroVh.btnAddtoCart.setText("Add to Cart");
                            heroVh.btnAddtoCart.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_200));
                            heroVh.btnAddtoCart.setTextColor(ContextCompat.getColor(context, R.color.white));


                            heroVh.menu_detail_st_cartbtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_btn));
                            heroVh.menu_detail_st_likebtn.setBackground(context.getResources().getDrawable(R.drawable.custom_cart_like_btn));


                            updatecartCount();

                        }
                    }
                });


                break;
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;

                //recycler view for grid items products

                //recycler view for items
                movieVH.itemRecyclerView.setHasFixedSize(true);
                movieVH.itemRecyclerView.setNestedScrollingEnabled(false);

                /* set layout manager on basis of recyclerview enum type */
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
                movieVH.itemRecyclerView.setLayoutManager(gridLayoutManager);


                SimilarProductsAdapter adapter = new SimilarProductsAdapter(context, selectedProduct.getSimilarProducts());
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
        return selectedProducts == null ? 0 : selectedProducts.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HERO;
        } else {
            return (position == selectedProducts.size() - 1 && isLoadingAdded) ?
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

    public void add(SelectedProduct r) {
        selectedProducts.add(r);
        notifyItemInserted(selectedProducts.size() - 1);
    }

    public void addAll(List<SelectedProduct> selectedProducts) {
        for (SelectedProduct selectedProduct : selectedProducts) {
            add(selectedProduct);
        }
    }

    public void remove(SelectedProduct r) {
        int position = selectedProducts.indexOf(r);
        if (position > -1) {
            selectedProducts.remove(position);
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
        add(new SelectedProduct());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = selectedProducts.size() - 1;
        SelectedProduct selectedProduct = getItem(position);

        if (selectedProduct != null) {
            selectedProducts.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(selectedProducts.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public SelectedProduct getItem(int position) {
        return selectedProducts.get(position);
    }


    private void updatecartCount() {
        db = new SenseDBHelper(context);
        String mycartcount = String.valueOf(db.countCart());
        Intent intent = new Intent(context.getString(R.string.cartcoutAction));
        intent.putExtra(context.getString(R.string.cartCount), mycartcount);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private void display(int number, HeroVH heroVh, SelectedProduct selectedProduct) {

        totalPrice = number * (selectedProduct.getUnitPrice());

        heroVh.itemQuanEt.setText("" + number);
        heroVh.menu_total_price.setText(NumberFormat.getNumberInstance(Locale.US).format(totalPrice));

        food_db_itemchecker = db.checktweetindb(String.valueOf(selectedProduct.getId()));

        if (food_db_itemchecker) {
            return;
            //item doesnt exist
        } else {
            db.updateMenuCount(number, selectedProduct.getId());
            //item exists

        }
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */

    protected class HeroVH extends RecyclerView.ViewHolder {
        private final ImageView menu_image;
        private final TextView menu_name;
        private final TextView menu_shortinfo;
        private final TextView menu_description;
        private final TextView menu_price;
        private final TextView menu_qtn;
        private final TextView itemQuanEt;
        private final TextView menu_total_price;
        private final Button incrementQtn;
        private final Button decreaseQtn;
        private final Button btnAddtoCart;
        private final Button btnOrderNow;
        private final Button menu_detail_st_cartbtn;
        private final Button menu_detail_st_likebtn;
        private final ProgressBar mProgress;

        private final LinearLayout descriptionlayout, relatedProductslayout;


        public HeroVH(View itemView) {
            super(itemView);
            // init views
            menu_image = itemView.findViewById(R.id.menu_image);
            menu_name = itemView.findViewById(R.id.menu_name);
            menu_shortinfo = itemView.findViewById(R.id.menu_shortinfo);
            menu_description = itemView.findViewById(R.id.menu_description);
            menu_price = itemView.findViewById(R.id.menu_price);
            menu_qtn = itemView.findViewById(R.id.menu_qtn);
            itemQuanEt = itemView.findViewById(R.id.itemQuanEt);
            menu_total_price = itemView.findViewById(R.id.menu_total_price);
            incrementQtn = itemView.findViewById(R.id.addBtn);
            decreaseQtn = itemView.findViewById(R.id.removeBtn);
            btnAddtoCart = itemView.findViewById(R.id.btnAddtoCart);
            btnOrderNow = itemView.findViewById(R.id.btnOrderNow);

            menu_detail_st_cartbtn = itemView.findViewById(R.id.menu_detail_st_cartbtn);
            menu_detail_st_likebtn = itemView.findViewById(R.id.menu_detail_st_likebtn);

            mProgress = itemView.findViewById(R.id.product_detail_image_progress);
            descriptionlayout = itemView.findViewById(R.id.descriptionlayout);
            relatedProductslayout = itemView.findViewById(R.id.relatedProductslayout);


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