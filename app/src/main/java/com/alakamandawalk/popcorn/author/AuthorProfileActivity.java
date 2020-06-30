package com.alakamandawalk.popcorn.author;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
import com.alakamandawalk.popcorn.story.StoryAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuthorProfileActivity extends AppCompatActivity {

    ImageButton backIb;
    ImageView authorCoverImg, authorProfileImg;
    TextView storyCountTv, authorNameTv, authorPostTv, authorDescriptionTv;
    RecyclerView authorStoryRv;
    ProgressBar authorPb;
    LinearLayout retryLl;
    NestedScrollView contentAuthorNsv;
    Button retryBtn;


    String authorCoverImage, authorProfileImage;

    StoryAdapter storyAdapter;
    List<StoryData> storyDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_profile);

        Intent intent = getIntent();
        final String authorId = intent.getStringExtra("authorId");

        Configuration configuration = new Configuration();
        setLocale(configuration);

        authorPb = findViewById(R.id.authorPb);
        retryLl = findViewById(R.id.retryLl);
        contentAuthorNsv = findViewById(R.id.contentAuthorNsv);
        retryBtn = findViewById(R.id.retryBtn);
        backIb = findViewById(R.id.backIb);
        authorCoverImg = findViewById(R.id.authorCoverImg);
        authorProfileImg = findViewById(R.id.authorProfileImg);
        storyCountTv = findViewById(R.id.storyCountTv);
        authorNameTv = findViewById(R.id.authorNameTv);
        authorPostTv = findViewById(R.id.authorPostTv);
        authorDescriptionTv = findViewById(R.id.authorDescriptionTv);
        authorStoryRv = findViewById(R.id.authorStoryRv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        authorStoryRv.setLayoutManager(layoutManager);

        storyDataList = new ArrayList<>();

        backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadContents(authorId);

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadContents(authorId);
            }
        });

        checkNightModeActivated();
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
                    Toast.makeText(AuthorProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    authorPb.setVisibility(View.GONE);
                }
            });

            loadAuthorStories(authorId);

        }else {
            authorPb.setVisibility(View.GONE);
            contentAuthorNsv.setVisibility(View.GONE);
            retryLl.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkNetworkStatus(){

        boolean conStatus = false;

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

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

    private void loadAuthorStories(String authorId) {

        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("story");
        Query query = storyRef.orderByChild("authorId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                storyDataList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    StoryData storyData = ds.getValue(StoryData.class);

                    storyDataList.add(storyData);
                    storyAdapter = new StoryAdapter(AuthorProfileActivity.this, storyDataList);
                    authorStoryRv.setAdapter(storyAdapter);
                    storyCountTv.setText(storyDataList.size() + " STORIES");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AuthorProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkNightModeActivated(){

        SharedPreferences themePref = getSharedPreferences(SettingsActivity.THEME_PREFERENCE, MODE_PRIVATE);
        boolean isDarkMode = themePref.getBoolean(SettingsActivity.KEY_IS_NIGHT_MODE, false);

        if (isDarkMode){
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
            setLocale(overrideConfiguration);
            applyOverrideConfiguration(overrideConfiguration);
        }
    }

    public void setLocale(Configuration config) {

        SharedPreferences languagePreference = getSharedPreferences(SettingsActivity.LANGUAGE_PREF, Context.MODE_PRIVATE);
        String lang =  languagePreference.getString(SettingsActivity.LANGUAGE_KEY, SettingsActivity.ENGLISH);
        String language;
        if (lang.equals(SettingsActivity.SINHALA)){
            language = SettingsActivity.SINHALA;
        }else {
            language = SettingsActivity.ENGLISH;
        }

        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        if (Build.VERSION.SDK_INT>=17){
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
    }
}