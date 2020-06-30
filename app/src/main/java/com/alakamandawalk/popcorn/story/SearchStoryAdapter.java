package com.alakamandawalk.popcorn.story;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.StoryData;

import java.util.List;

public class SearchStoryAdapter extends RecyclerView.Adapter<SearchStoryAdapter.SearchStoryViewHolder>{

    Context context;
    List<StoryData> storyDataList;

    public SearchStoryAdapter(Context context, List<StoryData> storyDataList) {
        this.context = context;
        this.storyDataList = storyDataList;
    }

    @NonNull
    @Override
    public SearchStoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.search_story_row, parent, false);
        return new SearchStoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchStoryViewHolder holder, int position) {

        String name = storyDataList.get(position).getStoryName();
        final String storyId = storyDataList.get(position).getStoryId();

        holder.searchStoryNameTv.setText(name);
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

    class SearchStoryViewHolder extends RecyclerView.ViewHolder{

        TextView searchStoryNameTv;

        public SearchStoryViewHolder(@NonNull View itemView) {
            super(itemView);

            searchStoryNameTv = itemView.findViewById(R.id.searchStoryNameTv);
        }
    }
}
