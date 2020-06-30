package com.alakamandawalk.popcorn.playlist;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.StoryData;
import com.alakamandawalk.popcorn.story.ReadStoryActivity;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>{

    Context context;
    List<StoryData> storyDataList;

    public PlaylistAdapter(Context context, List<StoryData> storyDataList) {
        this.context = context;
        this.storyDataList = storyDataList;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.d_and_p_story_row, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {

        final String storyId = storyDataList.get(position).getStoryId();
        String storyName = storyDataList.get(position).getStoryName();
        String storyImage = storyDataList.get(position).getStoryImage();
        String timeStamp = storyDataList.get(position).getStoryDate();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String storyDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        holder.storyNameTv.setText(storyName);
        holder.storyDateTv.setText(storyDate);

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

    class PlaylistViewHolder extends RecyclerView.ViewHolder{

        CardView downloadedStoryCv;
        TextView storyNameTv, storyDateTv;
        ImageView storyImg;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);

            downloadedStoryCv = itemView.findViewById(R.id.downloadedStoryCv);
            storyNameTv = itemView.findViewById(R.id.storyNameTv);
            storyDateTv = itemView.findViewById(R.id.storyDateTv);
            storyImg = itemView.findViewById(R.id.storyImg);
        }
    }

}
