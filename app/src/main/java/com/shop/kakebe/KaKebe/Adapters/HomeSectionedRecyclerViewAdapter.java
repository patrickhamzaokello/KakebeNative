package com.shop.kakebe.KaKebe.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.shop.kakebe.KaKebe.HelperClasses.Utility;
import com.shop.kakebe.KaKebe.Models.Category;
import com.shop.kakebe.KaKebe.MySelectedCategory;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.Utils.PaginationAdapterCallback;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman on 19/10/16.
 */

public class HomeSectionedRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final int HERO = 2;
    private static final int HOMECATEGORY = 5;
    private static final int CATEGORY = 3;
    private static final int FLASHDEAL = 4;

    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";


    private List<Category> categories;
    private final Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;


    private final PaginationAdapterCallback mCallback;
    private String errorMsg;

    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    public HomeSectionedRecyclerViewAdapter(Context context, PaginationAdapterCallback callback) {
        this.context = context;
        this.mCallback = callback;
        categories = new ArrayList<>();
    }

    public List<Category> getMovies() {
        return categories;
    }

    public void setMovies(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case HERO:
                View viewHero = inflater.inflate(R.layout.home_hero_layout, parent, false);
                viewHolder = new HeroVH(viewHero);
                break;
            case CATEGORY:
                View viewCategory = inflater.inflate(R.layout.home_category_recycler, parent, false);
                viewHolder = new CategoryVH(viewCategory);
                break;
            case FLASHDEAL:
                View viewFLASHDEAL = inflater.inflate(R.layout.home_flashdeals_recycler, parent, false);
                viewHolder = new FLASHDEALSVH(viewFLASHDEAL);
                break;
            case HOMECATEGORY:
                View viewHOMECATEGORY = inflater.inflate(R.layout.home_top_section_custom_row_layout, parent, false);
                viewHolder = new HOMECATEGORYVH(viewHOMECATEGORY);
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
        View v1 = inflater.inflate(R.layout.home_section_custom_row_layout, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Category category = categories.get(position); // Movie

        switch (getItemViewType(position)) {
            case HERO:
                final HeroVH heroVh = (HeroVH) holder;

                Glide.with(context)
                        .load(category.getHeader_ad())
                        .into(heroVh.headertopslider);

                heroVh.sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);
                heroVh.sliderView.setScrollTimeInSec(3);
                heroVh.sliderView.setAutoCycle(true);
                heroVh.sliderView.startAutoCycle();
                // passing this array list inside our adapter class.
                HomeSliderAdapter slideradapter = new HomeSliderAdapter(context, category.getSliderBanners());
                heroVh.sliderView.setSliderAdapter(slideradapter);
                break;
            case CATEGORY:
                final CategoryVH categoryVH = (CategoryVH) holder;
                HomeMenuCategoryAdapter homeMenuCategoryAdapter = new HomeMenuCategoryAdapter(context);
                int mNoOfColumns = Utility.calculateNoOfColumns(context, 160);

                GridLayoutManager catgridLayoutManager = new GridLayoutManager(context, mNoOfColumns);
                categoryVH.category_recycler_view.setLayoutManager(catgridLayoutManager);

                categoryVH.category_recycler_view.setItemAnimator(new DefaultItemAnimator());
                categoryVH.category_recycler_view.setAdapter(homeMenuCategoryAdapter);
                homeMenuCategoryAdapter.addAll(category.getFeaturedCategories());


                break;
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;
                movieVH.sectionLabel.setText(category.getName());

                //recycler view for items
                movieVH.itemRecyclerView.setHasFixedSize(true);
                movieVH.itemRecyclerView.setNestedScrollingEnabled(false);
                int mNo_OfColumns = Utility.calculateNoOfColumns(context, 152);

                /* set layout manager on basis of recyclerview enum type */
                GridLayoutManager gridLayoutManager = new GridLayoutManager(context, mNo_OfColumns);
                movieVH.itemRecyclerView.setLayoutManager(gridLayoutManager);


                HomeSectionedRecyclerViewItemAdapter adapter = new HomeSectionedRecyclerViewItemAdapter(context, category.getProducts());
                movieVH.itemRecyclerView.setAdapter(adapter);


                //show toast on click of show all button
                movieVH.showAllButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context.getApplicationContext(), MySelectedCategory.class);
                        //PACK DATA
                        i.putExtra("SENDER_KEY", "MyFragment");
//                        i.putExtra("category_selected_key", category.getParentId());
                        i.putExtra("category_selected_key", category.getId());

                        context.startActivity(i);

//                        Toast.makeText(v.getContext(), category.getName(), Toast.LENGTH_SHORT).show();

                    }
                });


                break;

            case HOMECATEGORY:
                final HOMECATEGORYVH homecategoryvh = (HOMECATEGORYVH) holder;
                homecategoryvh.sectionLabel.setText(category.getName());
//                homecategoryvh.sectionLabel.setText(category.getMetaTitle());

                //recycler view for items
                homecategoryvh.itemRecyclerView.setHasFixedSize(true);
                homecategoryvh.itemRecyclerView.setNestedScrollingEnabled(false);
                int mN_o_OfColumns = Utility.calculateNoOfColumns(context, 152);

                /* set layout manager on basis of recyclerview enum type */
                GridLayoutManager gridhometopLayoutManager = new GridLayoutManager(context, mN_o_OfColumns);
                homecategoryvh.itemRecyclerView.setLayoutManager(gridhometopLayoutManager);


                HomeSectionedRecyclerViewItemAdapter adapterTop = new HomeSectionedRecyclerViewItemAdapter(context, category.getProducts());
                homecategoryvh.itemRecyclerView.setAdapter(adapterTop);

                break;

            case FLASHDEAL:
                final FLASHDEALSVH flashdealsvh = (FLASHDEALSVH) holder;
                flashdealsvh.itemRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                HomeMainFlashDealsAdapter homeMainFlashDealsAdapter = new HomeMainFlashDealsAdapter(context);
                flashdealsvh.itemRecyclerView.setAdapter(homeMainFlashDealsAdapter);
                homeMainFlashDealsAdapter.addAll(category.getFlashDeals());

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
        return categories == null ? 0 : categories.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HERO;
        } else if(position == 1){
            return CATEGORY;
        }else if(position == 2){
            return FLASHDEAL;
        } else if (position == 3 || position == 4 || position == 5){
            return HOMECATEGORY;
        }
        else {
            return (position == categories.size() - 1 && isLoadingAdded) ?
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


    public void add(Category r) {
        categories.add(r);
        notifyItemInserted(categories.size() - 1);
    }

    public void addAll(List<Category> moveSelectedCategoryMenuItemResults) {
        for (Category selectedCategoryMenuItemResult : moveSelectedCategoryMenuItemResults) {
            add(selectedCategoryMenuItemResult);
        }
    }

    public void remove(Category r) {
        int position = categories.indexOf(r);
        if (position > -1) {
            categories.remove(position);
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
        add(new Category());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = categories.size() - 1;
        Category selectedCategoryMenuItemResult = getItem(position);

        if (selectedCategoryMenuItemResult != null) {
            categories.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(categories.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public Category getItem(int position) {
        return categories.get(position);
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */

    protected class HeroVH extends RecyclerView.ViewHolder {

        private final SliderView sliderView;
        private final ImageView headertopslider;

        public HeroVH(View itemView) {
            super(itemView);
            // init views
            sliderView = itemView.findViewById(R.id.home_slider);
            headertopslider  = itemView.findViewById(R.id.headertopslider);


        }
    }

    protected class CategoryVH extends RecyclerView.ViewHolder {

        private final RecyclerView category_recycler_view;

        public CategoryVH(View itemView) {
            super(itemView);
            // init views
            category_recycler_view = itemView.findViewById(R.id.category_recycler_view);

        }
    }


    protected class MovieVH extends RecyclerView.ViewHolder {
        private TextView sectionLabel;
        private TextView showAllButton;
        private RecyclerView itemRecyclerView;

        public MovieVH(View itemView) {
            super(itemView);

            sectionLabel = (TextView) itemView.findViewById(R.id.section_label);
            showAllButton = (TextView) itemView.findViewById(R.id.section_show_all_button);
            itemRecyclerView = (RecyclerView) itemView.findViewById(R.id.item_recycler_view);
        }
    }

    protected class HOMECATEGORYVH extends RecyclerView.ViewHolder {
        private TextView sectionLabel, section_label_desc;
        private RecyclerView itemRecyclerView;

        public HOMECATEGORYVH(View itemView) {
            super(itemView);

            sectionLabel = (TextView) itemView.findViewById(R.id.section_label);
            section_label_desc = (TextView) itemView.findViewById(R.id.section_label_desc);
            itemRecyclerView = (RecyclerView) itemView.findViewById(R.id.item_recycler_view);
        }
    }

    protected class FLASHDEALSVH extends RecyclerView.ViewHolder {
        private RecyclerView itemRecyclerView;

        public FLASHDEALSVH(View itemView) {
            super(itemView);
            itemRecyclerView = (RecyclerView) itemView.findViewById(R.id.main_flash_deals_recycler);
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