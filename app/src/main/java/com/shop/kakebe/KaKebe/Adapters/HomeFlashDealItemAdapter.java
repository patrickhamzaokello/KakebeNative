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
import com.shop.kakebe.KaKebe.Models.FlashProduct;
import com.shop.kakebe.KaKebe.Models.Product;
import com.shop.kakebe.KaKebe.MyMenuDetail;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.Utils.GlideApp;
import com.shop.kakebe.KaKebe.localDatabase.SenseDBHelper;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HomeFlashDealItemAdapter extends RecyclerView.Adapter<HomeFlashDealItemAdapter.ItemViewHolder> {

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView item_name,item_price,discoutpercent,original_price;
        private final ImageView itemimage;
        private final ProgressBar mProgress;
        private final MaterialCardView discount_card;

        public ItemViewHolder(View itemView) {
            super(itemView);
            itemimage = itemView.findViewById(R.id.product_imageview);
            item_name = itemView.findViewById(R.id.item_name);
            item_price = itemView.findViewById(R.id.item_price);
            mProgress = itemView.findViewById(R.id.home_product_image_progress);
            discoutpercent = itemView.findViewById(R.id.discoutpercent);
            original_price = itemView.findViewById(R.id.original_price);
            discount_card = itemView.findViewById(R.id.discount_card);

            original_price.setVisibility(View.GONE);

        }
    }

    private final Context context;
    private final List<FlashProduct> products;
    private static final String BASE_URL_IMG = "";

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();



    public HomeFlashDealItemAdapter(Context context, List<FlashProduct> products) {
        this.context = context;
        this.products = products;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_flash_deal_product, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final FlashProduct product = products.get(position);



        holder.item_name.setText(product.getName());
        holder.item_price.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(product.getDiscount()));

        if(compare_values(product.getUnitPrice(),product.getDiscount())){
            holder.original_price.setPaintFlags(holder.original_price.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.discount_card.setVisibility(View.GONE);
        } else {
            holder.discount_card.setVisibility(View.VISIBLE);
            holder.original_price.setVisibility(View.VISIBLE);
            holder.discoutpercent.setText(cal_percentage(product.getDiscount(), product.getUnitPrice()));
            holder.original_price.setPaintFlags(holder.original_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.original_price.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(product.getUnitPrice()));
        }

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

    public String cal_percentage(int discount, int unit_price){
        int val = (100 * discount / unit_price);
        val = Math.round(val - 0.5f);
        val = val - 100;
        String per_discount = val +"%";
        return per_discount;
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

    public void switchContent(int id, Fragment fragment) {
        if (context == null)
            return;
        if (context instanceof RootActivity) {
            RootActivity mainActivity = (RootActivity) context;
            Fragment frag = fragment;
            mainActivity.switchContent(id, frag, "MenuDetails");
        }

    }

    private RequestBuilder<Drawable> loadImage(@NonNull String posterPath) {
        return GlideApp
                .with(context)
                .load(BASE_URL_IMG + posterPath)
                .centerCrop();
    }


    @Override
    public int getItemCount() {
        return products.size();
    }


}