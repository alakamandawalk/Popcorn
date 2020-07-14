package com.alakamandawalk.popcorn.download;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.alakamandawalk.popcorn.localDB.LocalDBContract;
import com.alakamandawalk.popcorn.story.ReadStoryActivity;

public class DownloadedStoryAdapter extends RecyclerView.Adapter<DownloadedStoryAdapter.DownloadedStoryViewHolder>{

    Context context;
    Cursor cursor;

    public DownloadedStoryAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public DownloadedStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.small_story_row, parent, false);
        return new DownloadedStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DownloadedStoryViewHolder holder, int position) {

        if (!cursor.moveToPosition(position)){
            return;
        }

        final String storyId = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_ID));
        final String storyName = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_NAME));
        String storyDate = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_DATE));
        String authorName = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_AUTHOR_NAME));
        byte[] storyImage = cursor.getBlob(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_IMAGE));

        Bitmap bmp = BitmapFactory.decodeByteArray(storyImage, 0, storyImage.length);

        try {
            holder.storyImg.setImageBitmap(bmp);
        }catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            holder.storyImg.setImageResource(R.drawable.img_place_holder);
        }

        holder.premiumIcon.setVisibility(View.GONE);
        holder.storyNameTv.setText(storyName);
        holder.authorNameTv.setText(authorName);
        holder.storyDateTv.setText(storyDate);
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
        return cursor.getCount();
    }

    class DownloadedStoryViewHolder extends RecyclerView.ViewHolder{

        CardView downloadedStoryCv;
        ImageView storyImg, premiumIcon;
        TextView storyNameTv, storyDateTv, authorNameTv;

        public DownloadedStoryViewHolder(@NonNull View itemView) {
            super(itemView);

            downloadedStoryCv = itemView.findViewById(R.id.downloadedStoryCv);
            storyImg = itemView.findViewById(R.id.storyImg);
            storyNameTv = itemView.findViewById(R.id.storyNameTv);
            storyDateTv = itemView.findViewById(R.id.storyDateTv);
            premiumIcon = itemView.findViewById(R.id.premiumIcon);
            authorNameTv = itemView.findViewById(R.id.authorNameTv);

        }
    }
}
