package com.alakamandawalk.popcorn.story;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.StoryData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SmallStoryAdapter extends RecyclerView.Adapter<SmallStoryAdapter.PlaylistViewHolder>{

    Context context;
    List<StoryData> storyDataList;

    public SmallStoryAdapter(Context context, List<StoryData> storyDataList) {
        this.context = context;
        this.storyDataList = storyDataList;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.small_story_row, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {

        final String storyId = storyDataList.get(position).getStoryId();
        String storyName = storyDataList.get(position).getStoryName();
        String storyImage = storyDataList.get(position).getStoryImage();
        String timeStamp = storyDataList.get(position).getStoryDate();
        String authorId = storyDataList.get(position).getAuthorId();
        String isPremium = storyDataList.get(position).getIsPremium();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String storyDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        getAuthor(authorId, holder);
        holder.storyNameTv.setText(storyName);
        holder.storyDateTv.setText(storyDate);
        if (isPremium.equals("NO")){
            holder.premiumIcon.setVisibility(View.GONE);
        }

        try {
            Picasso.get()
                    .load(storyImage)
                    .placeholder(R.drawable.img_place_holder)
                    .fit()
                    .centerCrop()
                    .into(holder.storyImg);
        }catch (Exception e){
            Picasso.get().load(R.drawable.img_place_holder).into(holder.storyImg);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReadStoryActivity.class);
                intent.putExtra("storyId", storyId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyDataList.size();
    }

    private void getAuthor(String authorId, final SmallStoryAdapter.PlaylistViewHolder holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("author");
        Query query = reference.orderByChild("authorId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    String authorName = ds.child("authorName").getValue().toString();
                    holder.authorNameTv.setText(authorName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class PlaylistViewHolder extends RecyclerView.ViewHolder{

        CardView downloadedStoryCv;
        TextView storyNameTv, storyDateTv, authorNameTv;
        ImageView storyImg, premiumIcon;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);

            downloadedStoryCv = itemView.findViewById(R.id.downloadedStoryCv);
            storyNameTv = itemView.findViewById(R.id.storyNameTv);
            storyDateTv = itemView.findViewById(R.id.storyDateTv);
            storyImg = itemView.findViewById(R.id.storyImg);
            authorNameTv = itemView.findViewById(R.id.authorNameTv);
            premiumIcon = itemView.findViewById(R.id.premiumIcon);
        }
    }
}
