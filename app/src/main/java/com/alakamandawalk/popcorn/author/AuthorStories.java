package com.alakamandawalk.popcorn.author;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alakamandawalk.popcorn.R;
import com.alakamandawalk.popcorn.model.AuthCatData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthorStories#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthorStories extends Fragment {

    RecyclerView authorCRv;
    List<AuthCatData> authCatDataList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AuthorStories() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AuthorPlaylists.
     */
    // TODO: Rename and change types and number of parameters
    public static AuthorStories newInstance(String param1, String param2) {
        AuthorStories fragment = new AuthorStories();
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
        final View view = inflater.inflate(R.layout.fragment_author_stories, container, false);

        String authorId = AuthorProfileActivity.authorId;

        authorCRv = view.findViewById(R.id.authorCRv);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        authorCRv.setLayoutManager(layoutManager);

        authCatDataList = new ArrayList<>();

        loadCategories(authorId);

        return view;
    }


    private void loadCategories(final String authorId) {

        DatabaseReference catAuthRef = FirebaseDatabase.getInstance().getReference("catAuth").child(authorId);
        catAuthRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                authCatDataList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    AuthCatData authCatData = ds.getValue(AuthCatData.class);
                    authCatDataList.add(authCatData);
                    AuthorCategoryAdapter adapter = new AuthorCategoryAdapter(getActivity(), authCatDataList, authorId);
                    authorCRv.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}