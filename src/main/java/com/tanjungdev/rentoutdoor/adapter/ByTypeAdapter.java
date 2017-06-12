package com.tanjungdev.rentoutdoor.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tanjungdev.rentoutdoor.R;
import com.tanjungdev.rentoutdoor.fragment.ByTypeFragment;
import com.tanjungdev.rentoutdoor.model.Category;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by everjn on 4/20/16.
 */
public class ByTypeAdapter extends ArrayAdapter<Category> implements View.OnClickListener {

    private final LayoutInflater inflater;
    private final ArrayList<Category> categoryList;
    private Activity activity;
    private ByTypeFragment.CategorySelectionCallbacks mCallbacks;
    private String dummyUrl = "http://www.howiwork.org";
    AbsListView.LayoutParams params;

    public ByTypeAdapter(Activity activity, ByTypeFragment.CategorySelectionCallbacks mCallbacks, ArrayList<Category> categoryList) {
        super(activity, R.layout.layout_category_list);
        this.activity = activity;
        this.inflater = LayoutInflater.from(activity.getApplicationContext());
        this.categoryList = categoryList;
        this.mCallbacks = mCallbacks;
    }


    private static class MyViewHolder {
        public ImageView bannerImage;
        public TextView categoryName;
        public ImageView categoryImage;

        MyViewHolder(View v){
            bannerImage = (ImageView)v.findViewById(R.id.catBannerImageView);
            categoryName = (TextView)v.findViewById(R.id.catNameTV);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder;
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.layout_category_list_type, null);
            holder = new MyViewHolder(row);

            row.setTag(holder);

        } else {
            holder = (MyViewHolder) row.getTag();

        }

        Category category = categoryList.get(position);
        holder.categoryName.setText(category.getTitle());
        Picasso.with(activity).load(category.getImageUrl())
                .placeholder(R.drawable.ic_placeholder2)
                .into(holder.bannerImage);
        holder.bannerImage.setOnClickListener(this);
        holder.categoryName.setTag(position);
        holder.bannerImage.setTag(position);
        return row;
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public void onClick(View v) {
        int position = Integer.parseInt(v.getTag().toString());
        mCallbacks.onCategorySelected(categoryList.get(position).getId(),
                categoryList.get(position).getTitle());
    }


}

