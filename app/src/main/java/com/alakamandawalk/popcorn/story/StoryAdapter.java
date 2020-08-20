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

import com.alakamandawalk.popcorn.CountDays;
import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.StoryData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder>{

    Context context;
    List<StoryData> storyDataList;

    public StoryAdapter(Context context, List<StoryData> storyDataList) {
        this.context = context;
        this.storyDataList = storyDataList;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.story_row, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StoryViewHolder holder, int position) {

        final String storyId = storyDataList.get(position).getStoryId();
        String storyName = storyDataList.get(position).getStoryName();
        String timeStamp = storyDataList.get(position).getStoryDate();
        String authorId = storyDataList.get(position).getAuthorId();
        String readingTimeInSec = storyDataList.get(position).getReadingTimeSec();
        final String isPremium = storyDataList.get(position).getIsPremium();
        final String storyImage = storyDataList.get(position).getStoryImage();
        String now = String.valueOf(System.currentTimeMillis());
        String readingTimeInMin = Math.round((Integer.parseInt(readingTimeInSec)/60)+1)+" "+context.getResources().getString(R.string.minute);

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String storyDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        Calendar calendarNow = Calendar.getInstance(Locale.getDefault());
        calendarNow.setTimeInMillis(Long.parseLong(now));
        String timeNow = DateFormat.format("dd/MM/yyyy", calendar).toString();

        CountDays countDays = new CountDays();
        String days = countDays.getCountOfDays(storyDate, timeNow);

        if (isPremium.equals("NO")){
            holder.premiumIcon.setVisibility(View.GONE);
        }

        try {

            Picasso.get()
                    .load(storyImage)
                    .placeholder(R.drawable.img_place_holder)
                    .fit()
                    .centerCrop()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(holder.storyImageIv, new Callback() {

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get()
                            .load(storyImage)
                            .placeholder(R.drawable.img_place_holder)
                            .fit()
                            .centerCrop()
                            .into(holder.storyImageIv);
                }
            });

        }catch (Exception e){
            Picasso.get().load(R.drawable.img_place_holder).into(holder.storyImageIv);
        }

        holder.storyNameTv.setText(storyName);
        getAuthor(authorId, days, readingTimeInMin, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, ReadStoryActivity.class);
                intent.putExtra("storyId",storyId);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return storyDataList.size();
    }

    private void getAuthor(String authorId, final String date, final String readingTime, final StoryViewHolder holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("author");
        Query query = reference.orderByChild("authorId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    String authorName = ds.child("authorName").getValue().toString();
                    holder.authorNameAndDateTv.setText(date+" . "+authorName+" . "+readingTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder{

        public CardView storyCv;
        public ImageView storyImageIv, premiumIcon;
        public TextView storyNameTv, authorNameAndDateTv;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);

            storyCv = itemView.findViewById(R.id.storyCv);
            premiumIcon = itemView.findViewById(R.id.premiumIcon);
            storyImageIv = itemView.findViewById(R.id.storyImageIv);
            storyNameTv = itemView.findViewById(R.id.storyNameTv);
            authorNameAndDateTv = itemView.findViewById(R.id.authorNameAndDateTv);
        }
    }
}
