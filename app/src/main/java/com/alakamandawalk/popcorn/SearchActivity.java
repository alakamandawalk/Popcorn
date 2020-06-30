package com.alakamandawalk.popcorn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alakamandawalk.popcorn.category.CategoryAdapter;
import com.alakamandawalk.popcorn.model.CategoryData;
import com.alakamandawalk.popcorn.model.StoryData;
import com.alakamandawalk.popcorn.story.SearchStoryAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {

    RecyclerView categoryRv, searchRv;
    CategoryAdapter categoryAdapter;
    SearchStoryAdapter searchStoryAdapter;
    ImageButton backIb, searchIb;
    EditText searchEt;
    List<CategoryData> categoryDataList;
    List<StoryData> storyDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Configuration configuration = new Configuration();
        setLocale(configuration);

        categoryRv = findViewById(R.id.categoryRv);
        searchRv = findViewById(R.id.searchRv);
        backIb = findViewById(R.id.backIb);
        searchEt = findViewById(R.id.searchEt);
        searchIb = findViewById(R.id.searchIb);

        LinearLayoutManager searchLm = new LinearLayoutManager(this);
        searchLm.setStackFromEnd(true);
        searchLm.setReverseLayout(true);
        searchRv.setLayoutManager(searchLm);

        LinearLayoutManager categoryLm = new LinearLayoutManager(this);
        categoryLm.setStackFromEnd(true);
        categoryLm.setReverseLayout(true);
        categoryRv.setLayoutManager(categoryLm);

        categoryDataList = new ArrayList<>();
        storyDataList = new ArrayList<>();

        searchIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = searchEt.getText().toString().trim();
                if (!TextUtils.isEmpty(searchText)){
                    loadSearchList(searchText);
                }
            }
        });

        backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = searchEt.getText().toString().trim();
                if (!TextUtils.isEmpty(searchText)){
                    loadSearchList(searchEt.getText().toString().trim());
                    searchRv.setVisibility(View.VISIBLE);
                }else {
                    storyDataList.clear();
                    searchRv.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = searchEt.getText().toString().trim();
                if (!TextUtils.isEmpty(searchText)){
                    loadSearchList(searchEt.getText().toString().trim());
                    searchRv.setVisibility(View.VISIBLE);
                }else {
                    storyDataList.clear();
                    searchRv.setVisibility(View.GONE);
                }
            }
        });

        loadCategories();
        checkNightModeActivated();
    }

    private void loadSearchList(final String searchText) {

        DatabaseReference searchRef = FirebaseDatabase.getInstance().getReference("story");
        searchRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                storyDataList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    StoryData storyData = ds.getValue(StoryData.class);

                    if (storyData.getStoryName().contains(searchText) || storyData.getStorySearchTag().contains(searchText)){
                        storyDataList.add(storyData);
                    }

                    searchStoryAdapter = new SearchStoryAdapter(SearchActivity.this, storyDataList);
                    searchRv.setAdapter(searchStoryAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadCategories() {

        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("category");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryDataList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    CategoryData categoryData = ds.getValue(CategoryData.class);
                    categoryDataList.add(categoryData);
                    categoryAdapter = new CategoryAdapter(SearchActivity.this, categoryDataList);
                    categoryRv.setAdapter(categoryAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {

        if (storyDataList.size()>0 || searchEt.getText().toString().trim().length()>0){
            storyDataList.clear();
            searchRv.setVisibility(View.GONE);
            searchEt.getText().clear();
        }else {
            super.onBackPressed();
        }
    }
}