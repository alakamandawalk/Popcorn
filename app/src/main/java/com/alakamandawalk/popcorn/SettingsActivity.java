package com.alakamandawalk.popcorn;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alakamandawalk.popcorn.localDB.DBHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    ImageButton backIb;
    Switch darkModeSw;
    TextView currentFontSize;
    SeekBar textSizeSb;

    public static final String THEME_PREFERENCE = "nightModePref";
    public static final String KEY_IS_NIGHT_MODE = "isNightMode";

    public static final String TEXT_SIZE_PREFERENCE = "textSizePref";
    public static final String KEY_TEXT_SIZE = "textSize";

    public static final String ENGLISH = "en";
    public static final String SINHALA = "si";
    public int currentLang;

    public static final String LANGUAGE_KEY = "language_key";
    public static final String LANGUAGE_PREF = "language_pref";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Configuration config = new Configuration();
        setLocale(config);

        backIb = findViewById(R.id.backIb);
        darkModeSw = findViewById(R.id.darkModeSw);
        currentFontSize = findViewById(R.id.currentFontSize);
        textSizeSb = findViewById(R.id.textSizeSb);
        textSizeSb.setMax(40);

        backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        darkModeSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (darkModeSw.isChecked()){
                    saveNightModeState(true);
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);

                }else {
                    saveNightModeState(false);
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });

        textSizeSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                saveTextSize(progress);
                changeTextSize();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        checkNightModeActivated();
        changeTextSize();
    }

    private void changeTextSize() {

        SharedPreferences textSizePreference = getSharedPreferences(TEXT_SIZE_PREFERENCE, Context.MODE_PRIVATE);
        int textSize = textSizePreference.getInt(KEY_TEXT_SIZE, 18);
        float tf = Integer.valueOf(textSize).floatValue();
        currentFontSize.setText(getResources().getString(R.string.font_size)+" "+textSize);
        currentFontSize.setTextSize(tf);
        textSizeSb.setProgress(textSize);
    }

    private void saveTextSize(int progress) {

        SharedPreferences textSizePreference = getSharedPreferences(TEXT_SIZE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = textSizePreference.edit();
        editor.putInt(KEY_TEXT_SIZE, progress);
        editor.apply();
    }

    private void saveNightModeState(boolean b) {

        SharedPreferences themePreference = getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = themePreference.edit();
        editor.putBoolean(KEY_IS_NIGHT_MODE, b);
        editor.apply();
    }

    public void checkNightModeActivated() {

        SharedPreferences themePreference = getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE);
        if (themePreference.getBoolean(KEY_IS_NIGHT_MODE, false)){
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            darkModeSw.setChecked(true);
        }else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            darkModeSw.setChecked(false);
        }
    }

    private void showLangDialog() {

        final String[] languages = {"English", "සිංහල"};

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(SettingsActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Select a language");
        builder.setSingleChoiceItems(languages, currentLang, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    changeLocale(ENGLISH);
                    recreate();
                }else if (which==1){
                    changeLocale(SINHALA);
                    recreate();
                }
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void changeLocale(String language){

        SharedPreferences languagePreference = getSharedPreferences(LANGUAGE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = languagePreference.edit();
        editor.putString(LANGUAGE_KEY, language);
        editor.apply();
        Configuration config = new Configuration();
        setLocale(config);
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
            currentLang = 1;
        }else {
            language = SettingsActivity.ENGLISH;
            currentLang = 0;
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
    }

    public void rateUs(View view) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        }catch (ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    public void deleteDownloads(View view) {

        final DBHelper dbHelper = new DBHelper(this);
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.deleting));
        pd.setCanceledOnTouchOutside(false);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme);
        builder.setTitle(getResources().getString(R.string.delete_all_downloads));
        builder.setMessage(getResources().getString(R.string.delete_message));
        builder.setPositiveButton(getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.show();
                dbHelper.deleteAll();
                pd.dismiss();
                Toast.makeText(SettingsActivity.this, "deleted successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void appSugEmail(View view) {

        String[] TO = {"alakamandawalk@gmail.com"};

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "App Suggestions (POPCORN)");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }catch (android.content.ActivityNotFoundException e){
            Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void contactDevEmail(View view) {

        String[] TO = {"alakamandawalk@gmail.com"};

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }catch (android.content.ActivityNotFoundException e){
            Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void contactMpEmail(View view) {

        String[] TO = {"alakamandawalk@gmail.com"};

        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }catch (android.content.ActivityNotFoundException e){
            Toast.makeText(SettingsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void likeMpFb(View view) {
        Intent faceBookIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/meedumpaaru"));
        startActivity(faceBookIntent);
    }

    public void goToPrivacyPolicy(View view) {
        Intent privacyPolicyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://alakamamdawalk.xyz/privacy-policy/"));
        startActivity(privacyPolicyIntent);
    }

    public void setDefaultTextSize(View view) {
        SharedPreferences textSizePreference = getSharedPreferences(TEXT_SIZE_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = textSizePreference.edit();
        editor.putInt(KEY_TEXT_SIZE, 18);
        editor.apply();
        changeTextSize();
    }

    public void changeLang(View view) {
        showLangDialog();
    }
}