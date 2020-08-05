package com.alakamandawalk.popcorn.message;

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
import androidx.recyclerview.widget.RecyclerView;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.AuthorData;
import com.alakamandawalk.popcorn.model.MessageData;
import com.alakamandawalk.popcorn.story.ReadStoryActivity;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<MessageData> msgList;
    Context context;

    public MessageAdapter(List<MessageData> msgList, Context context) {
        this.msgList = msgList;
        this.context = context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.chat_layout, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        String msg = msgList.get(position).getMessage();
        String timeStamp = msgList.get(position).getMessageTime();
        final String storyId = msgList.get(position).getStoryId();
        String storyName = msgList.get(position).getStoryName();
        String authorId = msgList.get(position).getAuthorId();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String msgTime = DateFormat.format("dd/MM/yyyy  HH:mm", calendar).toString();

        holder.messageTv.setText(msg);
        holder.timeTv.setText(msgTime);

        if (storyId.equals("noAttachment")){
            holder.storyTitleTv.setVisibility(View.GONE);
        } else {
            holder.storyTitleTv.setText(storyName);
        }

        holder.storyTitleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReadStoryActivity.class);
                intent.putExtra("storyId", storyId);
                context.startActivity(intent);
            }
        });

        getAuthorData(authorId, holder);

    }

    private void getAuthorData(String authorId, final MessageViewHolder holder) {

        DatabaseReference authRef = FirebaseDatabase.getInstance().getReference("author");
        Query query = authRef.orderByChild("authorId").equalTo(authorId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds: snapshot.getChildren()){
                    AuthorData authorData = ds.getValue(AuthorData.class);

                    holder.authorNameTv.setText(authorData.getAuthorName());

                    try {
                        Picasso.get()
                                .load(authorData.getAuthorProfileImage())
                                .placeholder(R.drawable.img_place_holder)
                                .fit()
                                .centerCrop()
                                .into(holder.authorProIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.img_place_holder).into(holder.authorProIv);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{

        TextView messageTv, storyTitleTv, timeTv, authorNameTv;
        ImageView authorProIv;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageTv = itemView.findViewById(R.id.messageTv);
            storyTitleTv = itemView.findViewById(R.id.storyTitleTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            authorProIv = itemView.findViewById(R.id.authorProIv);
            authorNameTv = itemView.findViewById(R.id.authorNameTv);
        }
    }
}
