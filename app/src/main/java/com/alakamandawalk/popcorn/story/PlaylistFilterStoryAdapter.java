package com.alakamandawalk.popcorn.story;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alakamandawalk.popcorn.CountDays;
import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.AuthorData;
import com.alakamandawalk.popcorn.model.PlaylistData;
import com.alakamandawalk.popcorn.model.StoryData;
import com.alakamandawalk.popcorn.playlist.PlaylistActivity;
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

public class PlaylistFilterStoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    List<StoryData> storyDataList;
    Context context;

    public PlaylistFilterStoryAdapter(List<StoryData> storyDataList, Context context) {
        this.storyDataList = storyDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public StoryAdapter.StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.story_row, parent, false);

        return new StoryAdapter.StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.StoryViewHolder holder, int position) {

        final String storyId = storyDataList.get(position).getStoryId();
        String storyName = storyDataList.get(position).getStoryName();
        String timeStamp = storyDataList.get(position).getStoryDate();
        String authorId = storyDataList.get(position).getAuthorId();
        final String playlistId = storyDataList.get(position).getStoryPlaylistId();
        String isPremium = storyDataList.get(position).getIsPremium();
        String storyImage = storyDataList.get(position).getStoryImage();
        String now = String.valueOf(System.currentTimeMillis());

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String storyDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        Calendar calendarNow = Calendar.getInstance(Locale.getDefault());
        calendarNow.setTimeInMillis(Long.parseLong(now));
        String timeNow = DateFormat.format("dd/MM/yyyy", calendar).toString();

        CountDays countDays = new CountDays();
        String days = countDays.getCountOfDays(storyDate, timeNow);

        if (playlistId.equals("no")){

            if (isPremium.equals("NO")){
                holder.premiumIcon.setVisibility(View.GONE);
            }

            try {
                Picasso.get()
                        .load(storyImage)
                        .placeholder(R.drawable.img_place_holder)
                        .fit()
                        .centerCrop()
                        .into(holder.storyImageIv);
            }catch (Exception e){
                Picasso.get().load(R.drawable.img_place_holder).into(holder.storyImageIv);
            }

            holder.storyNameTv.setText(storyName);
            getAuthor(authorId, days, holder);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, ReadStoryActivity.class);
                    intent.putExtra("storyId",storyId);
                    context.startActivity(intent);
                }
            });
        }else {
            getDataFromPlaylist(playlistId, holder);
            holder.premiumIcon.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(context, PlaylistActivity.class);
                    intent.putExtra("playlistId",playlistId);
                    context.startActivity(intent);
                }
            });
        }
    }

    private void getDataFromPlaylist(final String playListId, final StoryAdapter.StoryViewHolder holder) {

        DatabaseReference imgRef = FirebaseDatabase.getInstance().getReference("playlist");
        Query query = imgRef.orderByChild("playlistId").equalTo(playListId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){

                    String playlistImage = ds.child("playlistImage").getValue().toString();
                    String playlistName = ds.child("playlistName").getValue().toString();
                    String playlistAuthorId = ds.child("playlistAuthor").getValue().toString();

                    try {
                        Picasso.get()
                                .load(playlistImage)
                                .placeholder(R.drawable.img_place_holder)
                                .fit()
                                .centerCrop()
                                .into(holder.storyImageIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.img_place_holder).into(holder.storyImageIv);
                    }

                    holder.storyNameTv.setText(playlistName);
                    getPlaylistAuthor(playlistAuthorId, playListId, holder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPlaylistAuthor(String playlistAuthorId, final String playlistId, final StoryAdapter.StoryViewHolder holder) {

        DatabaseReference authRef = FirebaseDatabase.getInstance().getReference("author");
        Query query = authRef.orderByChild("authorId").equalTo(playlistAuthorId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    String authorName = ds.child("authorName").getValue().toString();
                    getStoryCount(authorName, playlistId, holder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getStoryCount(final String authorName, String playlistId, final StoryAdapter.StoryViewHolder holder) {

        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("story");
        Query query = storyRef.orderByChild("storyPlaylistId").equalTo(playlistId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                holder.authorNameAndDateTv.setText(count+" "+context.getResources().getString(R.string.stories)+" . "+authorName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getAuthor(String authorId, final String date, final StoryAdapter.StoryViewHolder holder) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("author");
        Query query = reference.orderByChild("authorId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    String authorName = ds.child("authorName").getValue().toString();
                    holder.authorNameAndDateTv.setText(date+" . "+authorName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyDataList.size();
    }
}
