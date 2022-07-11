package com.shop.kakebe.KaKebe.Adapters;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.shop.kakebe.KaKebe.Models.UserAddress;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.Utils.SelectedAddressListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Suleiman on 19/10/16.
 */

public class AddressesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;


    private List<UserAddress> userAddresses;
    private final Context context;

    private boolean isLoadingAdded = false;
    private boolean retryPageLoad = false;


    private final SelectedAddressListener mCallback;
    private String errorMsg;

    public AddressesAdapter(Context context, SelectedAddressListener callback) {
        this.context = context;
        this.mCallback = callback;
        userAddresses = new ArrayList<>();
    }


    public List<UserAddress> getMovies() {
        return userAddresses;
    }

    public void setMovies(List<UserAddress> categories) {
        this.userAddresses = categories;
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
        View v1 = inflater.inflate(R.layout.addresslayout, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        UserAddress userAddress = userAddresses.get(position); // Movie

        switch (getItemViewType(position)) {

            case ITEM:
                final MovieVH movieVH = (MovieVH) holder;
                if (userAddress != null) {
                    movieVH.email.setText(userAddress.getUsername() + ", "+ userAddress.getEmail());
                    movieVH.phone.setText(userAddress.getPhone());
                    movieVH.city.setText( userAddress.getCity() + " , " + userAddress.getAddress()+ " , " +userAddress.getCountry() );
                    movieVH.shipping_cost.setText("Ugx " + NumberFormat.getNumberInstance(Locale.US).format(userAddress.getShippingCost()));

                    movieVH.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallback.selectedAddress(userAddress);
                        }
                    });

                } else {
                    mCallback.requestfailed();
                }
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
        return userAddresses == null ? 0 : userAddresses.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == userAddresses.size() - 1 && isLoadingAdded) ?
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





    /*
   Helpers
   _________________________________________________________________________________________________
    */


    public void add(UserAddress r) {
        userAddresses.add(r);
        notifyItemInserted(userAddresses.size() - 1);
    }

    public void addAll(List<UserAddress> userAddresses) {
        for (UserAddress userAddress : userAddresses) {
            add(userAddress);
        }
    }

    public void remove(UserAddress r) {
        int position = userAddresses.indexOf(r);
        if (position > -1) {
            userAddresses.remove(position);
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
        add(new UserAddress());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = userAddresses.size() - 1;
        UserAddress userAddress = getItem(position);

        if (userAddress != null) {
            userAddresses.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void showRetry(boolean show, @Nullable String errorMsg) {
        retryPageLoad = show;
        notifyItemChanged(userAddresses.size() - 1);

        if (errorMsg != null) this.errorMsg = errorMsg;
    }

    public UserAddress getItem(int position) {
        return userAddresses.get(position);
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */


    protected class MovieVH extends RecyclerView.ViewHolder {
        private TextView email, phone, city,shipping_cost;

        public MovieVH(View itemView) {
            super(itemView);

            email = (TextView) itemView.findViewById(R.id.email);
            phone = (TextView) itemView.findViewById(R.id.phone);
            city = (TextView) itemView.findViewById(R.id.city);
            shipping_cost = (TextView) itemView.findViewById(R.id.shipping_cost);

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