package com.alakamandawalk.popcorn.category;

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
import com.alakamandawalk.popcorn.model.CategoryData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HomeCatAdapter extends RecyclerView.Adapter<HomeCatAdapter.HomeCatViewHolder>{

    Context context;
    List<CategoryData> categoryDataList;

    public HomeCatAdapter(Context context, List<CategoryData> categoryDataList) {
        this.context = context;
        this.categoryDataList = categoryDataList;
    }

    @NonNull
    @Override
    public HomeCatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.home_cat_row, parent, false);

        return new HomeCatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeCatViewHolder holder, int position) {

        final String catID = categoryDataList.get(position).getCategoryId();
        String catName = categoryDataList.get(position).getCategoryName();
        final String catImage = categoryDataList.get(position).getCategoryImage();

        holder.catNameTv.setText(catName);

        try {

            Picasso.get()
                    .load(catImage)
                    .placeholder(R.drawable.img_place_holder)
                    .fit()
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.catImgIv, new Callback() {

                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get()
                                    .load(catImage)
                                    .placeholder(R.drawable.img_place_holder)
                                    .fit()
                                    .centerCrop()
                                    .into(holder.catImgIv);
                        }
                    });

        }catch (Exception e){
            Picasso.get().load(R.drawable.img_place_holder).into(holder.catImgIv);
        }

        getStoryCount(catID, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CategoryActivity.class);
                intent.putExtra("categoryId", catID);
                intent.putExtra("authId", "no");
                context.startActivity(intent);
            }
        });
    }

    private void getStoryCount(String catID, final HomeCatViewHolder holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("story");
        Query query = ref.orderByChild("storyCategoryId").equalTo(catID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String storyCount = String.valueOf(snapshot.getChildrenCount());
                holder.catStoryCountTv.setText(storyCount+" "+context.getResources().getString(R.string.stories));

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

    class HomeCatViewHolder extends RecyclerView.ViewHolder{

        TextView catStoryCountTv, catNameTv;
        ImageView catImgIv;


        public HomeCatViewHolder(@NonNull View itemView) {
            super(itemView);

            catImgIv = itemView.findViewById(R.id.catImgIv);
            catStoryCountTv = itemView.findViewById(R.id.catStoryCountTv);
            catNameTv = itemView.findViewById(R.id.catNameTv);
        }
    }
}
