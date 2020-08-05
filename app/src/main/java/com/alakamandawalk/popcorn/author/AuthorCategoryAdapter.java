package com.alakamandawalk.popcorn.author;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.category.CategoryActivity;
import com.alakamandawalk.popcorn.model.AuthCatData;
import com.alakamandawalk.popcorn.model.StoryData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AuthorCategoryAdapter extends RecyclerView.Adapter<AuthorCategoryAdapter.AuthorCategoryViewHolder>{

    Context context;
    List<AuthCatData> authCatData;
    String authorId;



    public AuthorCategoryAdapter(Context context, List<AuthCatData> authCatData, String authorId) {
        this.context = context;
        this.authCatData = authCatData;
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

        String categoryName = authCatData.get(position).getCatName();
        String categoryImage = authCatData.get(position).getCatImgUrl();
        final String categoryId = authCatData.get(position).getCatId();

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
        getStoryCount(authorId, categoryId, holder);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CategoryActivity.class);
                intent.putExtra("authId", authorId);
                intent.putExtra("categoryId", categoryId);
                context.startActivity(intent);
            }
        });
    }

    private void getStoryCount(String authorId, final String categoryId, final AuthorCategoryViewHolder holder) {

        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("story");
        Query query = storyRef.orderByChild("authorId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> idList = new ArrayList<>();
                idList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){

                    StoryData storyData = ds.getValue(StoryData.class);

                    if (storyData.getStoryCategoryId().equals(categoryId)) {
                        idList.add(storyData.getStoryId());
                    }
                }
                holder.categoryStoryCountTv.setText(String.valueOf(idList.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.categoryStoryCountTv.setText("...");
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return authCatData.size();
    }



    class AuthorCategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView categoryImgIv;
        TextView categoryNameTv, categoryStoryCountTv;

        public AuthorCategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryImgIv = itemView.findViewById(R.id.categoryImgIv);
            categoryNameTv = itemView.findViewById(R.id.categoryNameTv);
            categoryStoryCountTv = itemView.findViewById(R.id.categoryStoryCountTv);

        }
    }
}
