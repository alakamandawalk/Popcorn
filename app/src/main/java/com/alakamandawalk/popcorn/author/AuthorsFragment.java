package com.alakamandawalk.popcorn.author;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.SettingsActivity;
import com.alakamandawalk.popcorn.model.AuthorData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.alakamandawalk.popcorn.DashboardActivity.menuIb;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthorsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthorsFragment extends Fragment {

    ProgressBar authorPb;
    LinearLayout retryLl;
    Button retryBtn;
    RecyclerView authorRv;

    AuthorAdapter authorAdapter;
    List<AuthorData> authorDataList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AuthorsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AuthorsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AuthorsFragment newInstance(String param1, String param2) {
        AuthorsFragment fragment = new AuthorsFragment();
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
        View view = inflater.inflate(R.layout.fragment_authors, container, false);

        authorRv = view.findViewById(R.id.authorRv);
        authorPb = view.findViewById(R.id.authorPb);
        retryLl = view.findViewById(R.id.retryLl);
        retryBtn = view.findViewById(R.id.retryBtn);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        authorRv.setLayoutManager(layoutManager);

        authorDataList = new ArrayList<>();

        menuIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAuthors();
            }
        });

        loadAuthors();
        checkNightModeActivated();

        return view;
    }

    private void loadAuthors() {

        authorRv.setVisibility(View.GONE);
        authorPb.setVisibility(View.VISIBLE);
        retryLl.setVisibility(View.GONE);

        if (checkNetworkStatus()){

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("author");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    authorDataList.clear();
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        AuthorData authorData = ds.getValue(AuthorData.class);

                        authorDataList.add(authorData);
                        authorAdapter = new AuthorAdapter(getActivity() ,authorDataList);
                        authorRv.setAdapter(authorAdapter);

                        if (authorDataList.size()>0){
                            authorRv.setVisibility(View.VISIBLE);
                            authorPb.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else{
            authorPb.setVisibility(View.GONE);
            authorRv.setVisibility(View.GONE);
            retryLl.setVisibility(View.VISIBLE);
        }
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

    private void checkNightModeActivated() {

        SharedPreferences themePref = getActivity().getSharedPreferences(SettingsActivity.THEME_PREFERENCE, Context.MODE_PRIVATE);
        boolean isDarkMode = themePref.getBoolean(SettingsActivity.KEY_IS_NIGHT_MODE, false);

        if (isDarkMode){
            ((AppCompatActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else {
            ((AppCompatActivity)getActivity()).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
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
}