package com.alakamandawalk.popcorn.playlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.alakamandawalk.popcorn.story.SmallStoryAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class PlaylistActivity extends AppCompatActivity {

    ImageButton backIb;
    TextView titleTv, playlistDescriptionTv, authorNameTv;
    ImageView authorProfileImg, playlistImgIv;
    RecyclerView playlistStoryRv;
    ProgressBar playlistPb;
    LinearLayout retryLl;
    NestedScrollView contentPlaylistNsv;
    Button retryBtn;

    SmallStoryAdapter smallStoryAdapter;
    List<StoryData> storyDataList;

    String playlistImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        Configuration configuration = new Configuration();
        setLocale(configuration);

        Intent intent = getIntent();
        final String playlistId = intent.getStringExtra("playlistId");

        backIb = findViewById(R.id.backIb);
        titleTv = findViewById(R.id.titleTv);
        playlistDescriptionTv = findViewById(R.id.playlistDescriptionTv);
        authorNameTv = findViewById(R.id.authorNameTv);
        authorProfileImg = findViewById(R.id.authorProfileImg);
        playlistImgIv = findViewById(R.id.playlistImgIv);
        playlistStoryRv = findViewById(R.id.playlistStoryRv);
        playlistPb = findViewById(R.id.playlistPb);
        retryLl = findViewById(R.id.retryLl);
        contentPlaylistNsv = findViewById(R.id.contentPlaylistNsv);
        retryBtn = findViewById(R.id.retryBtn);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(false);
        layoutManager.setReverseLayout(true);
        playlistStoryRv.setLayoutManager(layoutManager);

        storyDataList = new ArrayList<>();

        backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        loadContents(playlistId);
        checkNightModeActivated();
    }

    private void loadContents(String playlistId) {

        contentPlaylistNsv.setVisibility(View.GONE);
        playlistPb.setVisibility(View.VISIBLE);
        retryLl.setVisibility(View.GONE);

        if (checkNetworkStatus()){

            DatabaseReference playlistRef = FirebaseDatabase.getInstance().getReference("playlist");
            Query query = playlistRef.orderByChild("playlistId").equalTo(playlistId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot ds: snapshot.getChildren()){

                        String playlistName = ds.child("playlistName").getValue().toString();
                        playlistImage = ds.child("playlistImage").getValue().toString();
                        String playlistDescription = ds.child("playlistDescription").getValue().toString();
                        String playlistAuthorId = ds.child("playlistAuthor").getValue().toString();

                        try {
                            Picasso.get()
                                    .load(playlistImage)
                                    .fit()
                                    .centerCrop()
                                    .placeholder(R.drawable.img_place_holder)
                                    .into(playlistImgIv);
                        }catch (Exception e){
                        }

                        titleTv.setText(playlistName);
                        playlistDescriptionTv.setText(playlistDescription);
                        loadAuthorData(playlistAuthorId);

                    }

                    contentPlaylistNsv.setVisibility(View.VISIBLE);
                    playlistPb.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PlaylistActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    playlistPb.setVisibility(View.GONE);
                }
            });

            loadStories(playlistId);

        }else {
            playlistPb.setVisibility(View.GONE);
            contentPlaylistNsv.setVisibility(View.GONE);
            retryLl.setVisibility(View.VISIBLE);
        }
    }

    private void loadStories(String playlistId) {

        DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference("story");
        Query query = storyRef.orderByChild("storyPlaylistId").equalTo(playlistId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                storyDataList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    StoryData storyData = ds.getValue(StoryData.class);
                    storyDataList.add(storyData);
                }

                Collections.reverse(storyDataList);
                smallStoryAdapter = new SmallStoryAdapter(PlaylistActivity.this, storyDataList);
                playlistStoryRv.setAdapter(smallStoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlaylistActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadAuthorData(String playlistAuthorId) {

        DatabaseReference authorRef = FirebaseDatabase.getInstance().getReference("author");
        Query query = authorRef.orderByChild("authorId").equalTo(playlistAuthorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds: snapshot.getChildren()){

                    String authorName = ds.child("authorName").getValue().toString();
                    String authorProfileImage = ds.child("authorProfileImage").getValue().toString();

                    authorNameTv.setText(authorName);

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
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlaylistActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

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