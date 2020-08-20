package com.alakamandawalk.popcorn.author;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.SettingsActivity;
import com.alakamandawalk.popcorn.message.MessageAdapter;
import com.alakamandawalk.popcorn.model.MessageData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthorMessages#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthorMessages extends Fragment {

    RecyclerView authMsgRv;
    List<MessageData> messageDataList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AuthorMessages() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AuthorMessages.
     */
    // TODO: Rename and change types and number of parameters
    public static AuthorMessages newInstance(String param1, String param2) {
        AuthorMessages fragment = new AuthorMessages();
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
        View view = inflater.inflate(R.layout.fragment_author_messages, container, false);

        String authorId = AuthorProfileActivity.authorId;

        authMsgRv = view.findViewById(R.id.authMsgRv);
        messageDataList = new ArrayList<>();

        LinearLayoutManager msgRvLM = new LinearLayoutManager(getActivity());
        msgRvLM.setStackFromEnd(true);
        authMsgRv.setLayoutManager(msgRvLM);
        authMsgRv.setHasFixedSize(true);

        loadMessages(authorId);

        checkNightModeActivated();

        return view;
    }

    private void loadMessages(String authorId) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("message");
        Query query = reference.orderByChild("authorId").equalTo(authorId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageDataList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    MessageData messageData = ds.getValue(MessageData.class);

                    messageDataList.add(messageData);
                    MessageAdapter messageAdapter = new MessageAdapter(messageDataList, getActivity());
                    authMsgRv.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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