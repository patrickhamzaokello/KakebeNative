package com.shop.kakebe.KaKebe.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.shop.kakebe.KaKebe.Models.CategoryInfo;
import com.shop.kakebe.KaKebe.R;

import java.util.List;

public class SelectedCategoryInfoAdapter extends RecyclerView.Adapter<SelectedCategoryInfoAdapter.ItemViewHolder> {

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final ImageView selected_cat_poster;
        private final TextView section_label;
//        private final ProgressBar mProgress;

        Button home_st_carttn;

        public ItemViewHolder(View itemView) {
            super(itemView);
            selected_cat_poster = itemView.findViewById(R.id.selected_cat_poster);
            section_label = itemView.findViewById(R.id.section_label);
//            mProgress = itemView.findViewById(R.id.home_product_image_progress);


        }
    }

    private final Context context;
    private final List<CategoryInfo> categoryInfos;
    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();


    public SelectedCategoryInfoAdapter(Context context, List<CategoryInfo> categoryInfos) {
        this.context = context;
        this.categoryInfos = categoryInfos;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_category_hero_design, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final CategoryInfo categoryInfo = categoryInfos.get(position);


//        holder.item_name.setText(categoryInfo.getName());
        holder.section_label.setText(categoryInfo.getName());

        Glide
                .with(context)
                .load(BASE_URL_IMG + categoryInfo.getBanner())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        holder.mProgress.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        holder.mProgress.setVisibility(View.GONE);
                        return false;
                    }

                })
                .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                .centerCrop()
                .transition(withCrossFade(factory))
                .into(holder.selected_cat_poster);


    }


    @Override
    public int getItemCount() {
        return categoryInfos.size();
    }


}