package com.alakamandawalk.popcorn.story;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.SettingsActivity;
import com.alakamandawalk.popcorn.author.AuthorProfileActivity;
import com.alakamandawalk.popcorn.localDB.DBHelper;
import com.alakamandawalk.popcorn.localDB.LocalDBContract;
import com.alakamandawalk.popcorn.model.StoryData;
import com.alakamandawalk.popcorn.playlist.PlaylistActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ReadStoryActivity extends AppCompatActivity {

    ImageButton authorIb, downloadIb, playListIb, relatedStoriesIb;
    TextView titleTv, storyTv, dateTv, authorNameTv, downloadBtnTipTv, counterTv;
    ImageView storyImg;
    RelativeLayout showRelRl;
    ProgressBar relStoryPb;
    RecyclerView relatedStoryRv;

    private boolean isDownloaded = false;
    private boolean showRel = false;

    DBHelper localDb;

    String storyId, storyName, story, storyImage, storyDate, storyCategoryId, storyPlaylistId, storySearchTag;
    String authorId, authorName;

    int readingTime;
    boolean showAds = false;
    int counter;

    ProgressDialog pd;

    RelatedStoryAdapter relatedStoryAdapter;
    List<StoryData> relStoryList;

    boolean doubleBackPressedToExitPressedOnce;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_story);

        Configuration configuration = new Configuration();
        setLocale(configuration);

        Intent intent = getIntent();
        storyId = intent.getStringExtra("storyId");

        localDb = new DBHelper(this);

        pd = new ProgressDialog(this);

        relatedStoryRv = findViewById(R.id.relatedStoryRv);
        relStoryPb = findViewById(R.id.relStoryPb);
        showRelRl = findViewById(R.id.showRelRl);
        authorIb = findViewById(R.id.authorIb);
        playListIb = findViewById(R.id.playListIb);
        relatedStoriesIb = findViewById(R.id.relatedStoriesIb);
        downloadIb = findViewById(R.id.downloadIb);
        titleTv = findViewById(R.id.titleTv);
        counterTv = findViewById(R.id.counterTv);
        storyTv = findViewById(R.id.storyTv);
        storyImg = findViewById(R.id.storyImg);
        dateTv = findViewById(R.id.dateTv);
        authorNameTv = findViewById(R.id.authorNameTv);
        downloadBtnTipTv = findViewById(R.id.downloadBtnTipTv);

        MobileAds.initialize(this, "ca-app-pub-4079566491683275~2327287115");
        mInterstitialAd = new InterstitialAd(ReadStoryActivity.this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        LinearLayoutManager relStoriesLm =
                new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL,
                        true);
        relStoriesLm.setStackFromEnd(true);
        relatedStoryRv.setLayoutManager(relStoriesLm);

        relStoryList = new ArrayList<>();
        showRel = false;
        showRelStories(storyCategoryId);

        isOnDownloads(storyId);

        downloadIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadOrRemove(storyId);
            }
        });

        playListIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (storyPlaylistId.equals("no")){
                    Toast.makeText(ReadStoryActivity.this, "no playlist!", Toast.LENGTH_SHORT).show();

                }else {

                    Intent intent = new Intent(ReadStoryActivity.this, PlaylistActivity.class);
                    intent.putExtra("playlistId",storyPlaylistId);
                    startActivity(intent);
                }
            }
        });

        authorIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ReadStoryActivity.this, AuthorProfileActivity.class);
                intent.putExtra("authorId", authorId);
                startActivity(intent);
            }
        });

        relatedStoriesIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkNetworkStatus()){
                    if (showRel){
                        showRel=false;
                        showRelStories(storyCategoryId);
                    }else {
                        showRel=true;
                        showRelStories(storyCategoryId);
                    }
                }else {
                    Toast.makeText(ReadStoryActivity.this, "no internet!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkNightModeActivated();

        initAds();
    }

    private void initAds() {

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                ReadStoryActivity.super.onBackPressed();
            }
        });
    }

    private void countTimeToShowAds() {

        counter=readingTime;

        new CountDownTimer(readingTime*1000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (checkNetworkStatus()){
                    showAds=true;
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            }
        }.start();
    }

    private void isOnDownloads(String id) {

        Cursor cursor = localDb.getStory(id);
        cursor.moveToFirst();

        if (cursor.getCount()>0){

            isDownloaded = true;
            downloadIb.setImageResource(R.drawable.ic_delete);
            downloadBtnTipTv.setText(getString(R.string.remove));
            loadStoryOffline(id);

        }else {
            isDownloaded=false;
            downloadIb.setImageResource(R.drawable.ic_download);
            downloadBtnTipTv.setText(getString(R.string.download));
            loadStoryOnline(id);
        }

    }

    private void loadStoryOffline(String id) {

        Cursor cursor = localDb.getStory(id);
        cursor.moveToFirst();

        storyName = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_NAME));
        story = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_STORY));
        storyDate = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_DATE));
        storyPlaylistId = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_PLAYLIST_ID));
        storyCategoryId = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_CATEGORY_ID));
        authorId = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_AUTHOR_ID));
        authorName = cursor.getString(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_AUTHOR_NAME));
        byte[] storyImage = cursor.getBlob(cursor.getColumnIndex(LocalDBContract.LocalDBEntry.KEY_IMAGE));

        if (!cursor.isClosed()){
            cursor.close();
        }

        Bitmap bmp = BitmapFactory.decodeByteArray(storyImage, 0, storyImage.length);

        try {
            storyImg.setImageBitmap(bmp);
        }catch (Exception e){
            Toast.makeText(ReadStoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            storyImg.setImageResource(R.drawable.img_place_holder);
        }

        int wordCount = countWords(story);
        readingTime = (wordCount/2);

        countTimeToShowAds();

        counterTv.setText(Math.round(readingTime/60)+" min reading");
        titleTv.setText(storyName);
        storyTv.setText(story);
        dateTv.setText(storyDate);
        authorNameTv.setText(authorName);
    }

    private void loadStoryOnline(String id) {

        pd.setMessage("Loading...");
        pd.show();
        pd.setCanceledOnTouchOutside(false);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("story");
        Query query = ref.orderByChild("storyId").equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    storyName = ds.child("storyName").getValue().toString();
                    story = ds.child("story").getValue().toString();
                    storyImage = ds.child("storyImage").getValue().toString();
                    storyCategoryId = ds.child("storyCategoryId").getValue().toString();
                    storyPlaylistId = ds.child("storyPlaylistId").getValue().toString();
                    storySearchTag = ds.child("storySearchTag").getValue().toString();
                    String timeStamp  = ds.child("storyDate").getValue().toString();
                    authorId = ds.child("authorId").getValue().toString();

                    java.util.Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(timeStamp));
                    storyDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

                    Transformation transformation = new Transformation() {
                        @Override
                        public Bitmap transform(Bitmap source) {

                            int  targetWidth = storyImg.getWidth();

                            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                            int targetHeight = (int) (targetWidth * aspectRatio);
                            Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight,false);
                            if (result != source){
                                source.recycle();
                            }
                            return result;
                        }

                        @Override
                        public String key() {
                            return "transformation" + "desireWidth";
                        }
                    };

                    try {
                        Picasso.get()
                                .load(storyImage)
                                .transform(transformation)
                                .placeholder(R.drawable.img_place_holder)
                                .into(storyImg);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.img_place_holder).into(storyImg);
                    }

                    int wordCount = countWords(story);
                    readingTime = (wordCount/2);

                    countTimeToShowAds();

                    counterTv.setText(Math.round(readingTime/60)+" min reading");
                    titleTv.setText(storyName);
                    storyTv.setText(story);
                    dateTv.setText(storyDate);
                    getAuthorDetails();
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pd.dismiss();
                Toast.makeText(ReadStoryActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int countWords(String story){

        int count = 0;

        char ch[] = new char[story.length()];

        for (int i=0; i<story.length(); i++){

            ch[i] = story.charAt(i);

            if (((i>0)&&(ch[i]!=' ')&&(ch[i-1]==' '))||((ch[0]!=' ')&&(i==0))){
                count++;
            }
        }

        return count;
    }

    private void showRelStories(String categoryId) {

        if (showRel){
            relatedStoryRv.setVisibility(View.GONE);
            showRelRl.setVisibility(View.VISIBLE);
            relStoryPb.setVisibility(View.VISIBLE);

            DatabaseReference relRef = FirebaseDatabase.getInstance().getReference("story");
            Query query = relRef.orderByChild("storyCategoryId").equalTo(categoryId);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    relStoryList.clear();
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        StoryData storyData = ds.getValue(StoryData.class);

                        if (!storyData.getStoryId().equals(storyId)){

                            relStoryList.add(storyData);
                            Collections.shuffle(relStoryList);
                            relatedStoryAdapter = new RelatedStoryAdapter(ReadStoryActivity.this, relStoryList);
                            relatedStoryRv.setAdapter(relatedStoryAdapter);
                            relStoryPb.setVisibility(View.GONE);
                            relatedStoryRv.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ReadStoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    relStoryPb.setVisibility(View.GONE);
                }
            });
        } else {
            showRelRl.setVisibility(View.GONE);
        }
    }

    private void downloadOrRemove(final String id) {

        if (isDownloaded){

            pd.setMessage(getString(R.string.removing));

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
            builder.setTitle(getString(R.string.delete));
            builder.setMessage(getString(R.string.delete_message));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    pd.show();
                    pd.setCanceledOnTouchOutside(false);

                    localDb.deleteStory(id);
                    Toast.makeText(ReadStoryActivity.this, "Removed!", Toast.LENGTH_SHORT).show();
                    isOnDownloads(id);
                    pd.dismiss();
                }
            });
            builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        }else {

            pd.setMessage(getString(R.string.downloading));
            pd.show();
            pd.setCanceledOnTouchOutside(false);

            Bitmap bitmap = ((BitmapDrawable)storyImg.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            final byte[] data = baos.toByteArray();

            localDb.insertStory(id, storyName, story, storyDate, storyCategoryId, storyPlaylistId, storySearchTag,authorId, authorName, data);

            Toast.makeText(ReadStoryActivity.this, "Downloaded!", Toast.LENGTH_SHORT).show();
            isOnDownloads(id);
            pd.dismiss();
        }
    }

    private void getAuthorDetails() {

        DatabaseReference authorRef = FirebaseDatabase.getInstance().getReference("author");
        Query query = authorRef.orderByChild("authorId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){

                    authorName = ds.child("authorName").getValue().toString();
                    authorNameTv.setText(authorName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ReadStoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        showRel=false;
        showRelStories(storyCategoryId);
        super.onResume();
    }

    @Override
    public void onBackPressed() {

        if (doubleBackPressedToExitPressedOnce){

            if (showAds){
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    super.onBackPressed();
                }
            }else {
                super.onBackPressed();
            }
            return;
        }
        this.doubleBackPressedToExitPressedOnce = true;
        Toast.makeText(this, "press BACK again to EXIT", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackPressedToExitPressedOnce = false;
            }
        }, 2000);
    }

}