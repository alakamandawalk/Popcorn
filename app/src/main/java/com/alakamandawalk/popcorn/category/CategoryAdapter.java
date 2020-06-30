package com.alakamandawalk.popcorn.category;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.alakamandawalk.popcorn.BlurTransformation;
import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.CategoryData;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>{

    Context context;
    List<CategoryData> categoryDataList;

    public CategoryAdapter(Context context, List<CategoryData> categoryDataList) {
        this.context = context;
        this.categoryDataList = categoryDataList;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.category_row, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, int position) {

        final String categoryId = categoryDataList.get(position).getCategoryId();
        String categoryName = categoryDataList.get(position).getCategoryName();
        final String categoryImage = categoryDataList.get(position).getCategoryImage();

        holder.categoryNameTv.setText(categoryName);

        try {
            Picasso.get()
                    .load(categoryImage)
                    .fit()
                    .transform(new BlurTransformation(context))
                    .centerCrop()
                    .into(holder.categoryImageIv);
        }catch (Exception e){

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CategoryActivity.class);
                intent.putExtra("categoryId", categoryId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryDataList.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder{

        CardView categoryCv;
        TextView categoryNameTv;
        ImageView categoryImageIv;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryCv = itemView.findViewById(R.id.categoryCv);
            categoryNameTv = itemView.findViewById(R.id.categoryNameTv);
            categoryImageIv = itemView.findViewById(R.id.categoryImageIv);
        }
    }
}
