package com.alakamandawalk.popcorn.author;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.SettingsActivity;
import com.alakamandawalk.popcorn.model.StoryData;
import com.alakamandawalk.popcorn.story.SmallStoryAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Author#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Author extends Fragment {

    ImageView authorCoverImg, authorProfileImg;
    TextView storyCountTv, authorNameTv, authorPostTv, authorDescriptionTv;
    RecyclerView authorStoryRv;
    ProgressBar authorPb;
    LinearLayout retryLl;
    NestedScrollView contentAuthorNsv;
    Button retryBtn;


    String authorCoverImage, authorProfileImage;
    String storiesCountStr="STORIES";

    SmallStoryAdapter storyAdapter;
    List<StoryData> storyDataList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Author() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Author.
     */
    // TODO: Rename and change types and number of parameters
    public static Author newInstance(String param1, String param2) {
        Author fragment = new Author();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_author, container, false);

        final String authorId = AuthorProfileActivity.authorId;

        authorPb = view.findViewById(R.id.authorPb);
        retryLl = view.findViewById(R.id.retryLl);
        contentAuthorNsv = view.findViewById(R.id.contentAuthorNsv);
        retryBtn = view.findViewById(R.id.retryBtn);
        authorCoverImg = view.findViewById(R.id.authorCoverImg);
        authorProfileImg = view.findViewById(R.id.authorProfileImg);
        storyCountTv = view.findViewById(R.id.storyCountTv);
        authorNameTv = view.findViewById(R.id.authorNameTv);
        authorPostTv = view.findViewById(R.id.authorPostTv);
        authorDescriptionTv = view.findViewById(R.id.authorDescriptionTv);
        authorStoryRv = view.findViewById(R.id.authorStoryRv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        authorStoryRv.setLayoutManager(layoutManager);

        storyDataList = new ArrayList<>();

        loadContents(authorId);

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadContents(authorId);
            }
        });

        checkNightModeActivated();

        return view;
    }

    private void loadContents(String authorId) {

        contentAuthorNsv.setVisibility(View.GONE);
        authorPb.setVisibility(View.VISIBLE);
        retryLl.setVisibility(View.GONE);

        if (checkNetworkStatus()){

            DatabaseReference authorRef = FirebaseDatabase.getInstance().getReference("author");
            Query query = authorRef.orderByChild("authorId").equalTo(authorId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds: dataSnapshot.getChildren()){

                        String authorName = ds.child("authorName").getValue().toString();
                        String authorPost = ds.child("authorPost").getValue().toString();
                        String authorDescription = ds.child("authorDescription").getValue().toString();
                        authorCoverImage = ds.child("authorCoverImage").getValue().toString();
                        authorProfileImage = ds.child("authorProfileImage").getValue().toString();

                        authorNameTv.setText(authorName);
                        authorPostTv.setText(authorPost);
                        authorDescriptionTv.setText(authorDescription);

                        try {
                            Picasso.get()
                                    .load(authorCoverImage)
                                    .fit()
                                    .centerCrop()
                                    .placeholder(R.drawable.img_place_holder)
                                    .into(authorCoverImg);
                        }catch (Exception e){
                            Picasso.get().load(R.drawable.img_place_holder).into(authorCoverImg);
                        }

                        try {
                            Picasso.get()
                                    .load(authorProfileImage)
                                    .fit()
                                    .centerCrop()
                                    .placeholder(R.drawable.img_place_holder)
                                    .into(authorProfileImg);
                        }catch (Exception e){
                            Picasso.get().load(R.drawable.img_place_holder).into(authorProfileImg);
                        }

                        contentAuthorNsv.setVisibility(View.VISIBLE);
                        authorPb.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    authorPb.setVisibility(View.GONE);
                }
            });

            loadAuthorStories(authorId);
            getStoryCount(authorId);

        }else {
            authorPb.setVisibility(View.GONE);
            contentAuthorNsv.setVisibility(View.GONE);
            retryLl.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkNetworkStatus(){

        boolean conStatus = false;

        ConnectivityManager conMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ) {

            conStatus = true;

        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            conStatus = false;
        }

        return conStatus;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        storiesCountStr = getResources().getString(R.string.stories);
    }

    private void getStoryCount(String authorId){

        DatabaseReference idRef = FirebaseDatabase.getInstance().getReference("story");
        Query query = idRef.orderByChild("authorId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                storyCountTv.setText(count+" "+storiesCountStr);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadAuthorStories(String authorId) {

        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("story");
        Query query = storyRef.orderByChild("authorId").equalTo(authorId).limitToLast(5);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                storyDataList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    StoryData storyData = ds.getValue(StoryData.class);

                    storyDataList.add(storyData);
                    storyAdapter = new SmallStoryAdapter(getActivity(), storyDataList);
                    authorStoryRv.setAdapter(storyAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkNightModeActivated() {

        SharedPreferences themePref = getActivity().getSharedPreferences(SettingsActivity.THEME_PREFERENCE, Context.MODE_PRIVATE);
        boolean isDarkMode = themePref.getBoolean(SettingsActivity.KEY_IS_NIGHT_MODE, false);

        if (isDarkMode){
            ((AppCompatActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            ((AppCompatActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}