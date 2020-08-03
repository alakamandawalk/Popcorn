package com.alakamandawalk.popcorn.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.SearchActivity;
import com.alakamandawalk.popcorn.SettingsActivity;
import com.alakamandawalk.popcorn.model.StoryData;
import com.alakamandawalk.popcorn.story.StoryAdapter;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.alakamandawalk.popcorn.DashboardActivity.menuIb;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    RecyclerView storyRv;
    ProgressBar homePb;
    LinearLayout retryLl;
    Button retryBtn;
    NestedScrollView contentHomeNsv;
    TemplateView templateView;

    StoryAdapter storyAdapter;
    List<StoryData> storyList;
    FirebaseAuth firebaseAuth;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        storyRv = view.findViewById(R.id.storyRv);
        homePb = view.findViewById(R.id.homePb);
        retryLl = view.findViewById(R.id.retryLl);
        retryBtn = view.findViewById(R.id.retryBtn);
        contentHomeNsv = view.findViewById(R.id.contentHomeNsv);
        templateView = view.findViewById(R.id.my_template);
        templateView.setVisibility(View.GONE);

        LinearLayoutManager storyLayoutManager = new LinearLayoutManager(getActivity());
        storyLayoutManager.setStackFromEnd(true);
        storyLayoutManager.setReverseLayout(true);
        storyRv.setLayoutManager(storyLayoutManager);
        storyList = new ArrayList<>();

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        AdLoader.Builder builder = new AdLoader.Builder(getActivity(),getString(R.string.nativead_ad_unit_id));
        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {

                templateView.setVisibility(View.VISIBLE);
                templateView.setNativeAd(unifiedNativeAd);
            }
        });

        AdLoader adLoader = builder.build();
        AdRequest adRequest = new AdRequest.Builder().build();
        adLoader.loadAd(adRequest);



        menuIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        loadStories("byDateAsc");

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStories("byDateAsc");
            }
        });

        checkNightModeActivated();

        return view;
    }

    private boolean checkNetworkStatus(){

        boolean conStatus = false;

        ConnectivityManager conMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

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

    public void loadStories(final String sort) {

        if (checkNetworkStatus()){

            contentHomeNsv.setVisibility(View.GONE);
            homePb.setVisibility(View.VISIBLE);
            retryLl.setVisibility(View.GONE);

            if (sort.equals("shuffle")){

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("story");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        storyList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            StoryData storyData = ds.getValue(StoryData.class);
                            storyList.add(storyData);

                            if (storyList.size()>0){
                                contentHomeNsv.setVisibility(View.VISIBLE);
                                homePb.setVisibility(View.GONE);
                            }
                        }
                        Collections.shuffle(storyList);
                        storyAdapter = new StoryAdapter(getActivity(), storyList);
                        storyRv.setAdapter(storyAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (sort.equals("byDateAsc")){

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("story");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        storyList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            StoryData storyData = ds.getValue(StoryData.class);
                            storyList.add(storyData);
                            storyAdapter = new StoryAdapter(getActivity(), storyList);
                            storyRv.setAdapter(storyAdapter);

                            if (storyList.size()>0){
                                contentHomeNsv.setVisibility(View.VISIBLE);
                                homePb.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (sort.equals("byDateDsc")){

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("story");
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        storyList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            StoryData storyData = ds.getValue(StoryData.class);
                            storyList.add(storyData);

                            if (storyList.size()>0){
                                contentHomeNsv.setVisibility(View.VISIBLE);
                                homePb.setVisibility(View.GONE);
                            }
                        }
                        Collections.reverse(storyList);
                        storyAdapter = new StoryAdapter(getActivity(), storyList);
                        storyRv.setAdapter(storyAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), ""+ databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        homePb.setVisibility(View.GONE);
                    }
                });
            }

        }else {

            homePb.setVisibility(View.GONE);
            contentHomeNsv.setVisibility(View.GONE);
            retryLl.setVisibility(View.VISIBLE);
        }
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

    private void showPopupMenu(final View v){

        final PopupMenu popupMenu = new PopupMenu(getActivity(), menuIb, Gravity.END);
        popupMenu.getMenu().add(Menu.NONE, 0,0, getResources().getString(R.string.sort_by));
        popupMenu.getMenu().add(Menu.NONE, 1,1, getResources().getString(R.string.categories));
        popupMenu.getMenu().add(Menu.NONE, 2,2,getResources().getString(R.string.settings));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id==0){
                    registerForContextMenu(menuIb);
                    getActivity().openContextMenu(v);
                }

                if (id==1){
                    startActivity(new Intent(getActivity(), SearchActivity.class));
                }

                if (id==2){
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                }

                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.sort_stories_menu,menu);
        menu.setHeaderTitle(getResources().getString(R.string.sort_by));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.sort_shuffle:
                loadStories("shuffle");
                break;

            case R.id.sort_by_date_asc:
                loadStories("byDateAsc");
                break;

            case R.id.sort_by_date_dsc:
                loadStories("byDateDsc");
                break;
        }
        return true;
    }

}