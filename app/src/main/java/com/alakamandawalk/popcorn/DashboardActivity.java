package com.alakamandawalk.popcorn;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alakamandawalk.popcorn.author.AuthorsFragment;
import com.alakamandawalk.popcorn.download.DownloadsActivity;
import com.alakamandawalk.popcorn.download.DownloadsFragment;
import com.alakamandawalk.popcorn.home.HomeFragment;
import com.alakamandawalk.popcorn.message.MessagesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ImageButton searchIb;
    public static ImageButton menuIb;
    public static TextView titleTv;

    FrameLayout frameLayout;
    BottomNavigationView bottomNav;

    boolean doubleBackPressedToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Intent intent = getIntent();
        String from = intent.getStringExtra("from");

        firebaseAuth = FirebaseAuth.getInstance();
        Configuration configuration = new Configuration();
        setLocale(configuration);

        frameLayout = findViewById(R.id.frameLayout);
        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnNavigationItemSelectedListener(selectedListener);

        menuIb = findViewById(R.id.menuIb);
        searchIb = findViewById(R.id.searchIb);
        titleTv = findViewById(R.id.titleTv);

        searchIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, SearchActivity.class));
            }
        });

        checkUserStatus();
        checkNetworkStatus(from);
        checkNightModeActivated();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull final MenuItem item) {

                    switch (item.getItemId()){

                        case R.id.nav_home:

                            titleTv.setText(getString(R.string.home));
                            HomeFragment homeFragment = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.frameLayout, homeFragment, "");
                            ft1.commit();
                            return true;

                        case R.id.nav_messages:

                            titleTv.setText(getString(R.string.messages));
                            MessagesFragment messagesFragment = new MessagesFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.frameLayout, messagesFragment, "");
                            ft2.commit();
                            return true;

                        case R.id.nav_authors:

                            titleTv.setText(getString(R.string.authors));
                            AuthorsFragment authorsFragment = new AuthorsFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.frameLayout, authorsFragment, "");
                            ft3.commit();
                            return true;

                        case R.id.nav_downloads:

                            titleTv.setText(getString(R.string.downloads));
                            DownloadsFragment downloadsFragment = new DownloadsFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.frameLayout, downloadsFragment, "");
                            ft4.commit();
                            return true;
                    }

                    return false;
                }
            };

    private void checkNetworkStatus(String from){

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ) {

            titleTv.setText(getString(R.string.home));
            HomeFragment homeFragment = new HomeFragment();
            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
            ft1.replace(R.id.frameLayout, homeFragment, "");
            ft1.commit();

        }
        else if ( conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {

            if (from != null){

                titleTv.setText(getString(R.string.home));
                HomeFragment homeFragment = new HomeFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.frameLayout, homeFragment, "");
                ft1.commit();

            }else {
                Intent intent = new Intent(DashboardActivity.this, DownloadsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
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
        super.onResume();
        checkNightModeActivated();
        Configuration configuration = new Configuration();
        setLocale(configuration);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackPressedToExitPressedOnce){
            super.onBackPressed();
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

    private void checkUserStatus() {

        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }
}
