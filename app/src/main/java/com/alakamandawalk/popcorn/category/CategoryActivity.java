package com.alakamandawalk.popcorn.category;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategoryActivity extends AppCompatActivity {

    ImageButton backIb, optionIb;
    RecyclerView categoryStoryRv;
    TextView titleTv;
    ImageView categoryImgIv;
    ProgressBar categoryPb;
    LinearLayout retryLl;
    NestedScrollView contentCategoryNsv;
    Button retryBtn;

    StoryAdapter storyAdapter;
    List<StoryData> storyDataList;

    String categoryImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Configuration configuration = new Configuration();
        setLocale(configuration);

        Intent intent = getIntent();
        final String categoryId = intent.getStringExtra("categoryId");

        backIb = findViewById(R.id.backIb);
        optionIb = findViewById(R.id.optionIb);
        categoryStoryRv = findViewById(R.id.categoryStoryRv);
        titleTv = findViewById(R.id.titleTv);
        categoryImgIv = findViewById(R.id.categoryImgIv);
        categoryPb = findViewById(R.id.categoryPb);
        retryLl = findViewById(R.id.retryLl);
        contentCategoryNsv = findViewById(R.id.contentCategoryNsv);
        retryBtn = findViewById(R.id.retryBtn);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        categoryStoryRv.setLayoutManager(layoutManager);

        storyDataList = new ArrayList<>();

        loadContent(categoryId);

        backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        checkNightModeActivated();
    }

    private void loadContent(String categoryId) {

        contentCategoryNsv.setVisibility(View.GONE);
        categoryPb.setVisibility(View.VISIBLE);
        retryLl.setVisibility(View.GONE);

        if (checkNetworkStatus()){

            DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("category");
            Query query = categoryRef.orderByChild("categoryId").equalTo(categoryId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds: snapshot.getChildren()){

                        String categoryName = ds.child("categoryName").getValue().toString();
                        categoryImage = ds.child("categoryImage").getValue().toString();
                        titleTv.setText(categoryName);

                        try {
                            Picasso.get()
                                    .load(categoryImage)
                                    .placeholder(R.drawable.img_place_holder)
                                    .fit()
                                    .centerCrop()
                                    .into(categoryImgIv);
                        }catch (Exception e){

                        }

                    }

                    contentCategoryNsv.setVisibility(View.VISIBLE);
                    categoryPb.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CategoryActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            loadCategoryStories(categoryId);

        }else {
            categoryPb.setVisibility(View.GONE);
            contentCategoryNsv.setVisibility(View.GONE);
            retryLl.setVisibility(View.VISIBLE);
        }

    }

    private void loadCategoryStories(String categoryId) {

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("story");
        Query query = categoryRef.orderByChild("storyCategoryId").equalTo(categoryId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                storyDataList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    StoryData storyData = ds.getValue(StoryData.class);

                    storyDataList.add(storyData);
                    storyAdapter = new StoryAdapter(CategoryActivity.this, storyDataList);
                    categoryStoryRv.setAdapter(storyAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CategoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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