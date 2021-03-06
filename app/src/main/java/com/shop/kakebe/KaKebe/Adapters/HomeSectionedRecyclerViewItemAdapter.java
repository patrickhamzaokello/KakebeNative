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
import android.widget.ImageView;
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
import com.shop.kakebe.KaKebe.localDatabase.CartDBManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HomeSectionedRecyclerViewItemAdapter extends RecyclerView.Adapter<HomeSectionedRecyclerViewItemAdapter.ItemViewHolder> {

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView item_name;
        private final TextView item_price;
        private final TextView item_rating;
        private final TextView discoutpercent;
        private final ImageView itemimage;
        private final ProgressBar mProgress;

        private final MaterialCardView discount_card,home_addToCart_card;

        Button home_cart_state;
        View view;


        public ItemViewHolder(View itemView) {
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

    private final Context context;
    private final List<Product> products;
    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    CartDBManager db;
    boolean food_db_itemchecker;

    int minteger = 1;
    int totalPrice;

    public static final int MENU_SYNCED_WITH_SERVER = 1;
    public static final int MENU_NOT_SYNCED_WITH_SERVER = 0;

    public HomeSectionedRecyclerViewItemAdapter(Context context, List<Product> products) {
        this.context = context;
        this.products = products;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_section_item_custom_row_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final Product product = products.get(position);

        db = new CartDBManager(context);

        food_db_itemchecker = db.checkProductID(String.valueOf(product.getId()));

        updatecartCount();

        if (food_db_itemchecker) {


            holder.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));
            holder.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.product_not_selected));

        } else {


            holder.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));
            holder.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.product_selected));

        }


        holder.item_name.setText(product.getName());
        holder.item_rating.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(product.getUnitPrice()));

        if(compare_values(product.getUnitPrice(),product.getDiscount())){
            holder.item_rating.setPaintFlags(holder.item_rating.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.discount_card.setVisibility(View.GONE);
        } else {
            holder.discount_card.setVisibility(View.VISIBLE);
            holder.discoutpercent.setText(cal_percentage(product.getDiscount(), product.getUnitPrice()));
            holder.item_rating.setPaintFlags(holder.item_rating.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        holder.item_price.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(product.getDiscount()));

        Glide
                .with(context)
                .load(BASE_URL_IMG + product.getThumbnailImg())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.mProgress.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.mProgress.setVisibility(View.GONE);
                        return false;
                    }

                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                .centerCrop()
                .transition(withCrossFade(factory))
                .into(holder.itemimage);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                food_db_itemchecker = db.checkProductID(String.valueOf(product.getId()));


                if (food_db_itemchecker) {
                    db.addProduct(
                            product.getId(),
                            product.getName(),
                            product.getUnitPrice(),
                            product.getCategoryId(),
                            product.getThumbnailImg(),
                            minteger,
                            MENU_NOT_SYNCED_WITH_SERVER
                    );


                    holder.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));
                    holder.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.product_selected));
                    updatecartCount();


                } else {
                    db.deleteProduct(String.valueOf(product.getId()));

                    holder.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));
                    holder.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.product_not_selected));

                    updatecartCount();

                }
            }
        });


        //show toast on click of show all button
        holder.itemimage.setOnClickListener(new View.OnClickListener() {
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


    private RequestBuilder<Drawable> loadImage(@NonNull String posterPath) {
        return GlideApp
                .with(context)
                .load(BASE_URL_IMG + posterPath)
                .centerCrop();
    }

    private void updatecartCount() {
        db = new CartDBManager(context);
        String mycartcount = String.valueOf(db.countCart());
        Intent intent = new Intent(context.getString(R.string.cartcoutAction));
        intent.putExtra(context.getString(R.string.cartCount), mycartcount);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


}