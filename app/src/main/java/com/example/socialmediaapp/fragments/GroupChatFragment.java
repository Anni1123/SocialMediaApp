package com.example.socialmediaapp.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.socialmediaapp.CreateGroupActivity;
import com.example.socialmediaapp.MainActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.adapters.AdapterGroupChatList;
import com.example.socialmediaapp.models.ModelGroupChatList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupChatFragment extends Fragment {

    RecyclerView recyclerView;
    FirebaseAuth firebaseAuth;
    ArrayList<ModelGroupChatList> groupChats;
    AdapterGroupChatList chatList;

    public GroupChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_group_chat, container, false);
        recyclerView=view.findViewById(R.id.grptv);
        firebaseAuth=FirebaseAuth.getInstance();
        loadGroupChat();
        return view;
    }
  private void loadGroupChat(){
        groupChats=new ArrayList<>();
    DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
    reference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            groupChats.clear();
            for (DataSnapshot ds:dataSnapshot.getChildren()){
                if(ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                    ModelGroupChatList chat=ds.getValue(ModelGroupChatList.class);
                    groupChats.add(chat);
                }
                chatList=new AdapterGroupChatList(getActivity(),groupChats);
                recyclerView.setAdapter(chatList);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
   }
    private void searchGroupChat(final String query){
        groupChats=new ArrayList<>();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupChats.clear();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if(ds.child("Participants").child(firebaseAuth.getUid()).exists()){
                        if(ds.child("grptitle").toString().toLowerCase().contains(query.toLowerCase())) {
                            ModelGroupChatList chat = ds.getValue(ModelGroupChatList.class);
                            groupChats.add(chat);
                        }
                    }
                    chatList=new AdapterGroupChatList(getActivity(),groupChats);
                    recyclerView.setAdapter(chatList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.settings).setVisible(false);
        menu.findItem(R.id.addparticipants).setVisible(false);
        menu.findItem(R.id.grpinfo).setVisible(false);
        menu.findItem(R.id.logout).setVisible(false);
        menu.findItem(R.id.settings).setVisible(false);
        menu.findItem(R.id.search).setVisible(false);
        MenuItem item=menu.findItem(R.id.search);

        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query.trim())){
                    searchGroupChat(query);
                }
                else {
                    loadGroupChat();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText.trim())){
                    searchGroupChat(newText);
                }
                else {
                   loadGroupChat();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if(item.getItemId()==R.id.craetegrp){
            startActivity(new Intent(getActivity(), CreateGroupActivity.class));
        }


        return super.onOptionsItemSelected(item);
    }
    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){

        }
        else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}
