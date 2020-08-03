package com.alakamandawalk.popcorn.author;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.CategoryData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AuthorCategoryAdapter extends RecyclerView.Adapter<AuthorCategoryAdapter.AuthorCategoryViewHolder>{

    Context context;
    List<CategoryData> categoryDataList;
    String authorId;

    public AuthorCategoryAdapter(Context context, List<CategoryData> categoryDataList, String authorId) {
        this.context = context;
        this.categoryDataList = categoryDataList;
        this.authorId = authorId;
    }

    @NonNull
    @Override
    public AuthorCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.author_category_row, parent, false);

        return new AuthorCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorCategoryViewHolder holder, int position) {

        String categoryName = categoryDataList.get(position).getCategoryName();
        String categoryImage = categoryDataList.get(position).getCategoryImage();
        String categoryId = categoryDataList.get(position).getCategoryId();

        try {
            Picasso.get()
                    .load(categoryImage)
                    .placeholder(R.drawable.img_place_holder)
                    .fit()
                    .centerCrop()
                    .into(holder.categoryImgIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.img_place_holder).into(holder.categoryImgIv);
        }

        holder.categoryNameTv.setText(categoryName);

        getStoryCount(categoryId, holder);

        loadStories(authorId, categoryId, holder);

    }

    private void loadStories(String authorId, String categoryId, AuthorCategoryViewHolder holder) {



    }

    private void getStoryCount(String categoryId, final AuthorCategoryViewHolder holder) {

        DatabaseReference idRef = FirebaseDatabase.getInstance().getReference("story");
        Query query = idRef.orderByChild("authorId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                holder.categoryStoryCountTv.setText(count);
            }

            @Override 
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryDataList.size();
    }

    class AuthorCategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView categoryImgIv;
        TextView categoryNameTv, categoryStoryCountTv;
        RecyclerView underCategoryRv;

        public AuthorCategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryImgIv = itemView.findViewById(R.id.categoryImgIv);
            categoryNameTv = itemView.findViewById(R.id.categoryNameTv);
            categoryStoryCountTv = itemView.findViewById(R.id.categoryStoryCountTv);
            underCategoryRv = itemView.findViewById(R.id.underCategoryRv);

        }
    }
}
