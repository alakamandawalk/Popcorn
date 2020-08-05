package com.alakamandawalk.popcorn.message;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.alakamandawalk.popcorn.model.MessageData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
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
 * Use the {@link MessagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MessagesFragment extends Fragment {

    List<MessageData> msgList;
    MessageAdapter messageAdapter;
    RecyclerView messagesRv;
    Button retryBtn;
    LinearLayout retryLl;
    ProgressBar messagePb;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MessagesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MessagesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MessagesFragment newInstance(String param1, String param2) {
        MessagesFragment fragment = new MessagesFragment();
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
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        messagesRv = view.findViewById(R.id.messagesRv);
        retryBtn = view.findViewById(R.id.retryBtn);
        retryLl = view.findViewById(R.id.retryLl);
        messagePb = view.findViewById(R.id.messagePb);

        LinearLayoutManager msgRvLM = new LinearLayoutManager(getActivity());
        msgRvLM.setStackFromEnd(true);
        messagesRv.setLayoutManager(msgRvLM);
        messagesRv.setHasFixedSize(true);

        msgList = new ArrayList<>();

        menuIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu();
            }
        });

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMessages();
            }
        });

        loadMessages();

        return view;
    }

    private void loadMessages() {

        if (checkNetworkStatus()){

            messagesRv.setVisibility(View.GONE);
            messagePb.setVisibility(View.VISIBLE);
            retryLl.setVisibility(View.GONE);

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("message");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    msgList.clear();
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        MessageData messageData = ds.getValue(MessageData.class);

                        msgList.add(messageData);
                        messageAdapter = new MessageAdapter(msgList, getActivity());
                        messagesRv.setAdapter(messageAdapter);

                        if (msgList.size()>0){
                            messagesRv.setVisibility(View.VISIBLE);
                            messagePb.setVisibility(View.GONE);
                        }else {

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else {

            messagePb.setVisibility(View.GONE);
            messagesRv.setVisibility(View.GONE);
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