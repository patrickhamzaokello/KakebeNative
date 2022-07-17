package com.shop.kakebe.KaKebe.Adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.material.card.MaterialCardView;
import com.shop.kakebe.KaKebe.HelperClasses.Utility;
import com.shop.kakebe.KaKebe.Models.ChoiceOption;
import com.shop.kakebe.KaKebe.Models.PopularSearch;
import com.shop.kakebe.KaKebe.Models.ProAttribute;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.Utils.ProductAttributeListener;
import com.shop.kakebe.KaKebe.Utils.SearchPopularTagAdapterCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman on 19/10/16.
 */

public class ProductDetailAttributeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ProductAttributeListener {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    //    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/w150";
    private static final String BASE_URL_IMG = "";
    private List<ProAttribute> mModelList;


    private List<ChoiceOption> featuredCategories;
    private final Context context;

    String attributelable;
    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;
    private final ProductAttributeListener mproductAttributeListener;


    DrawableCrossFadeFactory factory =
            new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();

    private String errorMsg;


    public ProductDetailAttributeAdapter(Context context, ProductAttributeListener productAttributeListener) {
        this.context = context;
        featuredCategories = new ArrayList<>();
        this.mproductAttributeListener = productAttributeListener;

    }

    public List<ChoiceOption> getMovies() {
        return featuredCategories;
    }

    public void setMovies(List<ChoiceOption> featuredCategories) {
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
        View v1 = inflater.inflate(R.layout.product_attribute_row_layout, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ChoiceOption choiceOption = featuredCategories.get(position); // Movie
        mModelList = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            mModelList.add(new ProAttribute((choiceOption.getValues())));
        }

        switch (getItemViewType(position)) {
            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;

                movieVH.attribute_lable.setText(choiceOption.getAttributeId());
                attributelable = choiceOption.getAttributeId();

                ProductAttributeValueAdapter productAttributeValueAdapter = new ProductAttributeValueAdapter(context, choiceOption.getValues(),mModelList,this);
                StaggeredGridLayoutManager previous_search_staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
                movieVH.attribute_value_recycler.setLayoutManager(previous_search_staggeredGridLayoutManager);
                movieVH.attribute_value_recycler.setItemAnimator(new DefaultItemAnimator());
                movieVH.attribute_value_recycler.setAdapter(productAttributeValueAdapter);


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


    public void add(ChoiceOption r) {
        featuredCategories.add(r);
        notifyItemInserted(featuredCategories.size() - 1);
    }

    public void addAll(List<ChoiceOption> popularSearches) {
        for (ChoiceOption popularSearch : popularSearches) {
            add(popularSearch);
        }
    }

    public void remove(ChoiceOption r) {
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
        add(new ChoiceOption());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = featuredCategories.size() - 1;
        ChoiceOption popularSearch = getItem(position);

        if (popularSearch != null) {
            featuredCategories.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(featuredCategories.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public ChoiceOption getItem(int position) {
        return featuredCategories.get(position);
    }


    @Override
    public void selectedAttribute(String feature) {
        mproductAttributeListener.selectedAttribute( attributelable + " - "+ feature);
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */


    protected class MovieVH extends RecyclerView.ViewHolder {

        private final TextView attribute_lable;
        private final RecyclerView attribute_value_recycler;

        public MovieVH(View itemView) {
            super(itemView);
            attribute_lable = itemView.findViewById(R.id.attribute_lable);
            attribute_value_recycler = itemView.findViewById(R.id.attribute_value_recycler);
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