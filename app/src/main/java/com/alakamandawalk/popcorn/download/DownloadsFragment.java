package com.alakamandawalk.popcorn.download;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.SettingsActivity;
import com.alakamandawalk.popcorn.localDB.DBHelper;

import static com.alakamandawalk.popcorn.DashboardActivity.menuIb;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DownloadsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadsFragment extends Fragment {

    DBHelper dbHelper;

    RecyclerView downloadsRv;
    ProgressBar downloadPb;
    DownloadedStoryAdapter downloadedStoryAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DownloadsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DownloadsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DownloadsFragment newInstance(String param1, String param2) {
        DownloadsFragment fragment = new DownloadsFragment();
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
        View view = inflater.inflate(R.layout.fragment_downloads, container, false);

        downloadsRv = view.findViewById(R.id.downloadsRv);
        downloadPb = view.findViewById(R.id.downloadPb);

        dbHelper = new DBHelper(getActivity());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        downloadsRv.setLayoutManager(layoutManager);

        loadStories();

        menuIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });

        checkNightModeActivated();

        return view;
    }

    private void loadStories() {

        downloadsRv.setVisibility(View.GONE);
        downloadPb.setVisibility(View.VISIBLE);

        downloadedStoryAdapter = new DownloadedStoryAdapter(getActivity(), dbHelper.getAllStories());
        downloadsRv.setAdapter(downloadedStoryAdapter);

        downloadsRv.setVisibility(View.VISIBLE);
        downloadPb.setVisibility(View.GONE);

    }

    private void showPopupMenu(){

        final PopupMenu popupMenu = new PopupMenu(getActivity(), menuIb, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0,0,getResources().getString(R.string.settings));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0){
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                }
                return false;
            }
        });
        popupMenu.show();
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