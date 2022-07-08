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

import androidx.annotation.Nullable;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.material.card.MaterialCardView;
import com.shop.kakebe.KaKebe.Models.CategoryProduct;
import com.shop.kakebe.KaKebe.MyMenuDetail;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.localDatabase.SenseDBHelper;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SelectedCategoryProductItemAdapter extends RecyclerView.Adapter<SelectedCategoryProductItemAdapter.ItemViewHolder> {


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
    private final List<CategoryProduct> categoryInfos;
    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";


    SenseDBHelper db;
    boolean food_db_itemchecker;

    int minteger = 1;

    public static final int MENU_SYNCED_WITH_SERVER = 1;
    public static final int MENU_NOT_SYNCED_WITH_SERVER = 0;

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();


    public SelectedCategoryProductItemAdapter(Context context, List<CategoryProduct> categoryInfos) {
        this.context = context;
        this.categoryInfos = categoryInfos;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_category_pagination_item_list, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final CategoryProduct categoryInfo = categoryInfos.get(position);


        db = new SenseDBHelper(context);

        food_db_itemchecker = db.checktweetindb(String.valueOf(categoryInfo.getId()));

        updatecartCount();

        holder.item_name.setText(categoryInfo.getName());

        if ((categoryInfo.getUnitPrice()) != null) {
            holder.item_price.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(categoryInfo.getDiscount()));
        } else {
            holder.item_price.setText("Null");
        }

        if (compare_values(categoryInfo.getUnitPrice(), categoryInfo.getDiscount())) {
            holder.item_rating.setPaintFlags(holder.item_rating.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.discount_card.setVisibility(View.GONE);
        } else {
            holder.discount_card.setVisibility(View.VISIBLE);
            holder.discoutpercent.setText(cal_percentage(categoryInfo.getDiscount(), categoryInfo.getUnitPrice()));
            holder.item_rating.setPaintFlags(holder.item_rating.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.item_rating.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(categoryInfo.getUnitPrice()));
            holder.item_rating.setVisibility(View.VISIBLE);
        }


        if (food_db_itemchecker) {


            holder.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));
            holder.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.black));


        } else {


            holder.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));
            holder.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.purple_200));


        }


        Glide
                .with(context)
                .load(BASE_URL_IMG + categoryInfo.getThumbnailImg())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.mProgress.setVisibility(View.GONE);
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

        //show toast on click of show all button
        holder.itemimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                Intent i = new Intent(context.getApplicationContext(), MyMenuDetail.class);
                //PACK DATA
                i.putExtra("SENDER_KEY", "MenuDetails");
                i.putExtra("selectMenuId", categoryInfo.getId());
                i.putExtra("category_selected_key", categoryInfo.getCategoryId());
                context.startActivity(i);
            }
        });

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                food_db_itemchecker = db.checktweetindb(String.valueOf(categoryInfo.getId()));


                if (food_db_itemchecker) {
                    db.addTweet(
                            categoryInfo.getId(),
                            categoryInfo.getName(),
                            categoryInfo.getUnitPrice(),
                            categoryInfo.getCategoryId(),
                            categoryInfo.getThumbnailImg(),
                            minteger,
                            MENU_NOT_SYNCED_WITH_SERVER
                    );


                    holder.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_check_btn));
                    holder.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.purple_200));

                    updatecartCount();


                } else {
                    db.deleteTweet(String.valueOf(categoryInfo.getId()));

                    holder.home_cart_state.setBackground(context.getResources().getDrawable(R.drawable.custom_plus_btn));
                    holder.home_addToCart_card.setCardBackgroundColor(context.getResources().getColor(R.color.black));

                    updatecartCount();

                }
            }
        });


    }


    private boolean compare_values(int unitprice, int discount) {
        boolean val;
        if (unitprice == discount) {
            val = true;
        } else {
            val = false;
        }
        return val;
    }

    public String cal_percentage(int discount, int unit_price) {
        int val = (100 * discount / unit_price);
        val = Math.round(val - 0.5f);
        val = val - 100;
        String per_discount = val + "%";
        return per_discount;
    }

    private void updatecartCount() {
        db = new SenseDBHelper(context);
        String mycartcount = String.valueOf(db.countCart());
        Intent intent = new Intent(context.getString(R.string.cartcoutAction));
        intent.putExtra(context.getString(R.string.cartCount), mycartcount);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }



    @Override
    public int getItemCount() {
        return categoryInfos.size();
    }


}