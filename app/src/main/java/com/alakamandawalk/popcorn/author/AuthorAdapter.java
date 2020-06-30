package com.alakamandawalk.popcorn.author;

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

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.AuthorData;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AuthorAdapter extends RecyclerView.Adapter<AuthorAdapter.AuthorViewHolder>{

    Context context;
    List<AuthorData> authorDataList;

    public AuthorAdapter(Context context, List<AuthorData> authorDataList) {
        this.context = context;
        this.authorDataList = authorDataList;
    }

    @NonNull
    @Override
    public AuthorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.author_row, parent, false);
        return new AuthorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuthorViewHolder holder, int position) {

        String authorName = authorDataList.get(position).getAuthorName();
        final String authorId = authorDataList.get(position).getAuthorId();
        String authorProPic = authorDataList.get(position).getAuthorProfileImage();
        String authorPost = authorDataList.get(position).getAuthorPost();

        try {
            Picasso.get()
                    .load(authorProPic)
                    .placeholder(R.drawable.img_place_holder)
                    .fit()
                    .centerCrop()
                    .into(holder.authorProfileImg);
        }catch (Exception e){
            Picasso.get().load(R.drawable.img_place_holder).into(holder.authorProfileImg);
        }

        holder.authorNameTv.setText(authorName);
        holder.authorPostTv.setText(authorPost);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AuthorProfileActivity.class);
                intent.putExtra("authorId", authorId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return authorDataList.size();
    }

    static class AuthorViewHolder extends RecyclerView.ViewHolder{

        CardView authorCv;
        ImageView authorProfileImg;
        TextView authorNameTv, authorPostTv;

        public AuthorViewHolder(@NonNull View itemView) {
            super(itemView);

            authorCv = itemView.findViewById(R.id.authorCv);
            authorProfileImg = itemView.findViewById(R.id.authorProfileImg);
            authorNameTv = itemView.findViewById(R.id.authorNameTv);
            authorPostTv = itemView.findViewById(R.id.authorPostTv);

        }
    }
}
