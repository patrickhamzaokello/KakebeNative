package com.pkasemer.kakebeshoplira.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pkasemer.kakebeshoplira.Models.Category;
import com.pkasemer.kakebeshoplira.MySelectedCategory;
import com.pkasemer.kakebeshoplira.R;
import com.pkasemer.kakebeshoplira.RootActivity;

import java.util.List;

public class HomeSectionedRecyclerViewAdapter extends RecyclerView.Adapter<HomeSectionedRecyclerViewAdapter.SectionViewHolder> {


    class SectionViewHolder extends RecyclerView.ViewHolder {
        private final TextView sectionLabel;
        private final TextView showAllButton;
        private final RecyclerView itemRecyclerView;

        public SectionViewHolder(View itemView) {
            super(itemView);
            sectionLabel = itemView.findViewById(R.id.section_label);
            showAllButton = itemView.findViewById(R.id.section_show_all_button);
            itemRecyclerView = itemView.findViewById(R.id.item_recycler_view);
        }
    }

    private final Context context;
    List<Category> categories;

    public HomeSectionedRecyclerViewAdapter(Context context, List<Category> categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public SectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_section_custom_row_layout, parent, false);
        return new SectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SectionViewHolder holder, int position) {
        final Category category = categories.get(position);
        holder.sectionLabel.setText(category.getName());

        //recycler view for items
//        holder.itemRecyclerView.setHasFixedSize(true);
        holder.itemRecyclerView.setNestedScrollingEnabled(false);

        /* set layout manager on basis of recyclerview enum type */
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        holder.itemRecyclerView.setLayoutManager(gridLayoutManager);


        HomeSectionedRecyclerViewItemAdapter adapter = new HomeSectionedRecyclerViewItemAdapter(context, category.getProducts());
        holder.itemRecyclerView.setAdapter(adapter);

        //show toast on click of show all button
        holder.showAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context.getApplicationContext(), MySelectedCategory.class);
                //PACK DATA
                i.putExtra("SENDER_KEY", "MyFragment");
                i.putExtra("category_selected_key", category.getId());
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
            mainActivity.switchContent(id, frag, "CategoryDetails");
        }

    }

    @Override
    public int getItemCount() {
        return categories.size();
    }


}