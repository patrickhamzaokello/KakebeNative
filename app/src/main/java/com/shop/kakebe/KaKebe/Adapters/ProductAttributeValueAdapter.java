package com.shop.kakebe.KaKebe.Adapters;


import android.content.Context;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;


import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.card.MaterialCardView;
import com.shop.kakebe.KaKebe.R;
import com.shop.kakebe.KaKebe.RootActivity;
import com.shop.kakebe.KaKebe.localDatabase.SenseDBHelper;


import java.util.ArrayList;
import java.util.List;


public class ProductAttributeValueAdapter extends RecyclerView.Adapter<ProductAttributeValueAdapter.ItemViewHolder> {

    List<String> val_list = new ArrayList<>();

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView item_name;
        private final MaterialCardView attribute_card;

        public ItemViewHolder(View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_name);
            attribute_card = itemView.findViewById(R.id.attribute_card);
        }
    }

    private final Context context;
    private final List<String> products;


    public ProductAttributeValueAdapter(Context context, List<String> products) {
        this.context = context;
        this.products = products;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_attribute_value, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        final String product = products.get(position);
        holder.item_name.setText(product);


        if (!(val_list.contains(product))) {
//            holder.attribute_card.setCardBackgroundColor(context.getResources().getColor(R.color.attribute_color_default));
            holder.attribute_card.setBackground(context.getResources().getDrawable(R.drawable.attribute_not_selected));
        } else {
//            holder.attribute_card.setCardBackgroundColor(context.getResources().getColor(R.color.purple_200));
            holder.attribute_card.setBackground(context.getResources().getDrawable(R.drawable.attribute_selected));
        }


        holder.item_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                val_list.clear();

                if (!(val_list.contains(product))) {
                    val_list.clear();
                    val_list.add(product);
                    holder.attribute_card.setBackground(context.getResources().getDrawable(R.drawable.attribute_selected));

                } else {
                    holder.attribute_card.setBackground(context.getResources().getDrawable(R.drawable.attribute_not_selected));
                    val_list.remove(product);

                }

                updateAttributeCount(product);
                notifyDataSetChanged();

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

    private void updateAttributeCount(String product ) {
        String attributeList = String.valueOf(product);
        Intent intent = new Intent(context.getString(R.string.prod_attrib_action));
        intent.putExtra(context.getString(R.string.prod_attrib), attributeList);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


}