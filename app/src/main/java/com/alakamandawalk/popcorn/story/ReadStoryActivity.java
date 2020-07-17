package com.alakamandawalk.popcorn.story;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
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

import static com.alakamandawalk.popcorn.SettingsActivity.KEY_TEXT_SIZE;
import static com.alakamandawalk.popcorn.SettingsActivity.TEXT_SIZE_PREFERENCE;

public class ReadStoryActivity extends AppCompatActivity implements RewardedVideoAdListener{

    NestedScrollView contentRSNsv;
    Button retryBtn;
    LinearLayout retryLl;
    ImageButton authorIb, downloadIb, playListIb, relatedStoriesIb, backIb;
    TextView titleTv, storyTv, dateTv, authorNameTv, downloadBtnTipTv, readingTimeTv;
    ImageView storyImg, premiumIcon;
    RelativeLayout showRelRl;
    ProgressBar relStoryPb, readStoryPb;
    RecyclerView relatedStoryRv;
    SeekBar textSizeSb;
    TextView fontSizeTv;

    private boolean showRel = false;

    DBHelper localDb;

    String storyId, storyName, story, storyImage, storyDate, storyCategoryId, storyPlaylistId, storySearchTag, isPremium;
    String authorId, authorName;

    int readingTime;
    boolean showAds = false;
    int counter;

    int textSize;

    ProgressDialog pd;

    RelatedStoryAdapter relatedStoryAdapter;
    List<StoryData> relStoryList;

    boolean doubleBackPressedToExitPressedOnce;
    boolean isRewarded = false;

    private InterstitialAd mInterstitialAd;

    private RewardedVideoAd mRewardedVideoAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_story);

        Configuration configuration = new Configuration();
        setLocale(configuration);

        Intent intent = getIntent();
        storyId = intent.getStringExtra("storyId");

        initViews();
        initButtons();

        localDb = new DBHelper(this);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4079566491683275~2327287115");

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4079566491683275/5969413997");

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        LinearLayoutManager relStoriesLm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        relStoriesLm.setStackFromEnd(true);
        relatedStoryRv.setLayoutManager(relStoriesLm);

        relStoryList = new ArrayList<>();
        showRel = false;

        interstitialAdListener();

        if (isOnDownloads()){

            downloadIb.setImageResource(R.drawable.ic_delete);
            downloadBtnTipTv.setText(getString(R.string.remove));

        }else {

            downloadIb.setImageResource(R.drawable.ic_download);
            downloadBtnTipTv.setText(getString(R.string.download));
        }

        loadStory();
        showRelStories();
        changeTextSize();
        checkNightModeActivated();
    }

    private void loadStory() {

        if (isOnDownloads()){
            loadStoryOffline();
        }else {
            loadStoryOnline();
        }
    }

    private void initViews() {

        pd = new ProgressDialog(this);
        contentRSNsv = findViewById(R.id.contentRSNsv);
        retryLl = findViewById(R.id.retryLl);
        retryBtn = findViewById(R.id.retryBtn);
        readStoryPb = findViewById(R.id.readStoryPb);
        relatedStoryRv = findViewById(R.id.relatedStoryRv);
        relStoryPb = findViewById(R.id.relStoryPb);
        showRelRl = findViewById(R.id.showRelRl);
        authorIb = findViewById(R.id.authorIb);
        playListIb = findViewById(R.id.playListIb);
        relatedStoriesIb = findViewById(R.id.relatedStoriesIb);
        downloadIb = findViewById(R.id.downloadIb);
        titleTv = findViewById(R.id.titleTv);
        readingTimeTv = findViewById(R.id.readingTimeTv);
        storyTv = findViewById(R.id.storyTv);
        storyImg = findViewById(R.id.storyImg);
        dateTv = findViewById(R.id.dateTv);
        authorNameTv = findViewById(R.id.authorNameTv);
        downloadBtnTipTv = findViewById(R.id.downloadBtnTipTv);
        backIb = findViewById(R.id.backIb);
        premiumIcon = findViewById(R.id.premiumIcon);
    }

    private void initButtons() {

        relatedStoriesIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkNetworkStatus()){
                    if (showRel){
                        showRel=false;
                        showRelStories();
                    }else {
                        showRel=true;
                        showRelStories();
                    }
                }else {
                    Toast.makeText(ReadStoryActivity.this, "no internet!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        downloadIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadOrRemove();
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

        backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        storyTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                createChangeTSDialog();
                return false;
            }
        });
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            fontSizeTv.setText(getResources().getString(R.string.font_size)+" "+progress);
            saveTextSize(progress);
            changeTextSize();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void createChangeTSDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        textSizeSb = new SeekBar(this);
        textSizeSb.setPadding(20,20,20,20);
        textSizeSb.setMax(40);
        textSizeSb.setProgress(textSize);
        fontSizeTv = new TextView(this);
        fontSizeTv.setTextSize((float) 22.0);
        fontSizeTv.setText(getResources().getString(R.string.font_size)+" "+textSize);
        fontSizeTv.setPadding(10,10,10,10);
        TextView setDefTv = new TextView(this);
        setDefTv.setTextSize((float) 22.0);
        setDefTv.setText(getResources().getString(R.string.set_to_default));
        setDefTv.setPadding(10,10,10,10);
        layout.addView(fontSizeTv);
        layout.addView(textSizeSb);
        layout.addView(setDefTv);
        builder.setView(layout);
        textSizeSb.setOnSeekBarChangeListener(seekBarChangeListener);
        setDefTv.setOnClickListener(onSetToDefClickListener);
        builder.create().show();
    }

    View.OnClickListener onSetToDefClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SharedPreferences textSizePreference = getSharedPreferences(TEXT_SIZE_PREFERENCE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = textSizePreference.edit();
            editor.putInt(KEY_TEXT_SIZE, 18);
            editor.apply();
            textSizeSb.setProgress(18);
            fontSizeTv.setText(getResources().getString(R.string.font_size)+" "+18);
            changeTextSize();
        }
    };

    private void loadRewardedVideoAd() {
        if (!mRewardedVideoAd.isLoaded()){
            mRewardedVideoAd.loadAd("ca-app-pub-4079566491683275/5929460511", new AdRequest.Builder().build());
        }
    }

    private void interstitialAdListener() {

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

    private boolean isOnDownloads() {

        boolean isDownloaded;
        Cursor cursor = localDb.getStory(storyId);
        cursor.moveToFirst();

        if (cursor.getCount()>0){

            isDownloaded = true;

        }else {
            isDownloaded=false;
        }
        return isDownloaded;
    }

    private void loadStoryOffline() {

        readStoryPb.setVisibility(View.GONE);
        retryLl.setVisibility(View.GONE);
        premiumIcon.setVisibility(View.GONE);

        Cursor cursor = localDb.getStory(storyId);
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
        readingTime = (wordCount/3);

        countTimeToShowAds();

        readingTimeTv.setText(Math.round((readingTime/60)+1)+" "+getResources().getString(R.string.minute));
        titleTv.setText(storyName);
        storyTv.setText(story);
        dateTv.setText(storyDate);
        authorNameTv.setText(authorName);
    }

    private void showStory(){

        try {

            Picasso.get()
                    .load(storyImage)
                    .placeholder(R.drawable.img_place_holder)
                    .into(storyImg);

        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (isPremium.equals("NO")){
            premiumIcon.setVisibility(View.GONE);
        }

        int wordCount = countWords(story);
        readingTime = (wordCount/3);

        countTimeToShowAds();

        readingTimeTv.setText(Math.round((readingTime/60)+1)+" "+getResources().getString(R.string.minute));
        titleTv.setText(storyName);
        storyTv.setText(story);
        dateTv.setText(storyDate);
        getAuthorDetails();
    }

    private void loadStoryOnline() {

        contentRSNsv.setVisibility(View.GONE);
        readStoryPb.setVisibility(View.VISIBLE);
        retryLl.setVisibility(View.GONE);

        if (checkNetworkStatus()){

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("story");
            Query query = ref.orderByChild("storyId").equalTo(storyId);
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
                        isPremium = ds.child("isPremium").getValue().toString();

                        java.util.Calendar calendar = Calendar.getInstance(Locale.getDefault());
                        calendar.setTimeInMillis(Long.parseLong(timeStamp));
                        storyDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

                        if (isPremium.equals("YES")){
                            loadRewardedVideoAd();
                            Toast.makeText(ReadStoryActivity.this, "Loading Ads...", Toast.LENGTH_LONG).show();
                        }else {
                            contentRSNsv.setVisibility(View.VISIBLE);
                            readStoryPb.setVisibility(View.GONE);
                            showStory();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    readStoryPb.setVisibility(View.GONE);
                    Toast.makeText(ReadStoryActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else {

            readStoryPb.setVisibility(View.GONE);
            contentRSNsv.setVisibility(View.GONE);
            retryLl.setVisibility(View.VISIBLE);
        }
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

    private void showRelStories() {

        if (showRel){
            relatedStoryRv.setVisibility(View.GONE);
            showRelRl.setVisibility(View.VISIBLE);
            relStoryPb.setVisibility(View.VISIBLE);

            DatabaseReference relRef = FirebaseDatabase.getInstance().getReference("story");
            Query query = relRef.orderByChild("storyCategoryId").equalTo(storyCategoryId);
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

    private void downloadOrRemove() {

        if (isOnDownloads()){

            pd.setMessage(getString(R.string.removing));

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
            builder.setTitle(getString(R.string.delete));
            builder.setMessage(getString(R.string.delete_message));
            builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    pd.show();
                    pd.setCanceledOnTouchOutside(false);

                    localDb.deleteStory(storyId);
                    Toast.makeText(ReadStoryActivity.this, "Removed!", Toast.LENGTH_SHORT).show();
                    downloadIb.setImageResource(R.drawable.ic_download);
                    downloadBtnTipTv.setText(getString(R.string.download));
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

            localDb.insertStory(storyId, storyName, story, storyDate, storyCategoryId, storyPlaylistId, storySearchTag,authorId, authorName, data);

            Toast.makeText(ReadStoryActivity.this, "Downloaded!", Toast.LENGTH_SHORT).show();
            downloadIb.setImageResource(R.drawable.ic_delete);
            downloadBtnTipTv.setText(getString(R.string.remove));
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

    private void saveTextSize(int progress) {

        SharedPreferences textSizePreference = getSharedPreferences(TEXT_SIZE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = textSizePreference.edit();
        editor.putInt(KEY_TEXT_SIZE, progress);
        editor.apply();
    }

    private void changeTextSize() {

        SharedPreferences textSizePreference = getSharedPreferences(TEXT_SIZE_PREFERENCE, Context.MODE_PRIVATE);
        textSize = textSizePreference.getInt(KEY_TEXT_SIZE, 22);
        float tf = Integer.valueOf(textSize).floatValue();
        storyTv.setTextSize(tf);
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
        showRelStories();
        mRewardedVideoAd.resume(this);
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
        Toast.makeText(this, "press again to go BACK", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackPressedToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onRewardedVideoAdLoaded() {

        if (isPremium.equals("YES")){
            if (mRewardedVideoAd.isLoaded()) {
                mRewardedVideoAd.show();
            }
        }
        Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
        Toast.makeText(this, "onRewardedVideoAdStarted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {

        if (!isRewarded){
            Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
            builder.setTitle(getResources().getString(R.string.notice));
            builder.setMessage(getResources().getString(R.string.rewarded_msg));
            builder.setPositiveButton(getResources().getString(R.string.watch_video), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    mRewardedVideoAd.resume();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.left), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        isRewarded=true;
        contentRSNsv.setVisibility(View.VISIBLE);
        readStoryPb.setVisibility(View.GONE);
        Toast.makeText(this, "Access Allowed", Toast.LENGTH_SHORT).show();
        showStory();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        Toast.makeText(this, "onRewardedVideoAdLeftApplication",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
        if (isPremium.equals("YES")){
            finish();
        }
    }

    @Override
    public void onRewardedVideoCompleted() {
        Toast.makeText(this, "onRewardedVideoCompleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        mRewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mRewardedVideoAd.destroy(this);
        super.onDestroy();
    }
}