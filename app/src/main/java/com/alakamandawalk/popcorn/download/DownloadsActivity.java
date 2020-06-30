package com.alakamandawalk.popcorn.download;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alakamandawalk.popcorn.DashboardActivity;
import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.SettingsActivity;
import com.alakamandawalk.popcorn.localDB.DBHelper;

public class DownloadsActivity extends AppCompatActivity {

    DBHelper dbHelper;

    RecyclerView downloadedStoryRv;
    DownloadedStoryAdapter downloadedStoryAdapter;

    ImageButton backIb, menuIb;
    Button retryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        downloadedStoryRv = findViewById(R.id.downloadedStoryRv);
        backIb = findViewById(R.id.backIb);
        menuIb = findViewById(R.id.menuIb);
        retryBtn = findViewById(R.id.retryBtn);

        dbHelper = new DBHelper(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        downloadedStoryRv.setLayoutManager(layoutManager);

        backIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        menuIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkNetworkStatus()){
                    onBackPressed();
                }else {
                    Toast.makeText(DownloadsActivity.this, "not connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loadStories();
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

    private void loadStories() {

        downloadedStoryAdapter = new DownloadedStoryAdapter(this, dbHelper.getAllStories());
        downloadedStoryRv.setAdapter(downloadedStoryAdapter);

    }

    private void showPopupMenu(){

        final PopupMenu popupMenu = new PopupMenu(this, menuIb, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0,0,getResources().getString(R.string.settings));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0){
                    startActivity(new Intent(DownloadsActivity.this, SettingsActivity.class));
                }

                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DownloadsActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("from", "DownloadActivity");
        startActivity(intent);
    }
}