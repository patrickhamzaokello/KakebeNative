package com.shop.kakebe.KaKebe.Adapters;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.shop.kakebe.KaKebe.Models.FoodDBModel;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.localDatabase.CartDBManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Suleiman on 19/10/16.
 */

public class PlaceOrderCartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;


    private List<FoodDBModel> foodDBModelList;
    CartDBManager db;


    private final Context context;

    private final boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;




    private String errorMsg;



    public PlaceOrderCartAdapter(Context context, List<FoodDBModel>tweetList) {
        this.context = context;
        this.foodDBModelList = tweetList;
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
        View v1 = inflater.inflate(R.layout.place_order_cart_item_design, parent, false);
        viewHolder = new CartDesignVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        FoodDBModel foodDBModel = foodDBModelList.get(position); // Food
        db = new CartDBManager(holder.itemView.getContext());



        switch (getItemViewType(position)) {
            case ITEM:
                final CartDesignVH movieVH = (CartDesignVH) holder;

                movieVH.cart_product_name.setText(foodDBModel.getMenuName());

                movieVH.cart_product_price.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(foodDBModel.getPrice()));

                movieVH.itemQuanEt.setText((foodDBModel.getQuantity()).toString());
                movieVH.fooditemtotal.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format((foodDBModel.getPrice())*(foodDBModel.getQuantity())));


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
        return foodDBModelList == null ? 0 : foodDBModelList.size();
    }

    @Override
    public int getItemViewType(int position) {

        return (position == foodDBModelList.size() - 1 && isLoadingAdded) ?
                LOADING : ITEM;
    }







    public void remove(FoodDBModel r) {
        int position = foodDBModelList.indexOf(r);
        if (position > -1) {
            foodDBModelList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateFood(FoodDBModel r) {
        int position = foodDBModelList.indexOf(r);
        if (position > -1) {
            db = new CartDBManager(context.getApplicationContext());
            foodDBModelList = db.listProducts();
            notifyDataSetChanged();
        }
    }





    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(foodDBModelList.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }



    protected class CartDesignVH extends RecyclerView.ViewHolder {
        private final TextView cart_product_name;
        private final TextView cart_product_price;
        private final TextView itemQuanEt;
        private final TextView fooditemtotal;

        public CartDesignVH(View itemView) {
            super(itemView);
            cart_product_name = itemView.findViewById(R.id.cart_product_name);
            cart_product_price = itemView.findViewById(R.id.cart_product_price);
            itemQuanEt = itemView.findViewById(R.id.fooditemQTN);
            fooditemtotal = itemView.findViewById(R.id.fooditemtotal);
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