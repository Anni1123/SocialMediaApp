package com.example.socialmediaapp.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.example.socialmediaapp.SettingsActivity;
import com.example.socialmediaapp.adapters.AdapterUsers;
import com.example.socialmediaapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;
    FirebaseAuth firebaseAuth;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view= inflater.inflate(R.layout.fragment_users, container, false);
       recyclerView=view.findViewById(R.id.recyclep);
       recyclerView.setHasFixedSize(true);
       recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
       usersList=new ArrayList<>();
       firebaseAuth=FirebaseAuth.getInstance();
       getAllUsers();
       return view;
    }

    private void getAllUsers() {
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelUsers modelUsers=dataSnapshot1.getValue(ModelUsers.class);
                    if(!modelUsers.getUid().equals(firebaseUser.getUid())){
                        usersList.add(modelUsers);
                    }
                    adapterUsers=new AdapterUsers(getActivity(),usersList);
                    recyclerView.setAdapter(adapterUsers);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void searchusers(final String s)
    {
        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelUsers modelUsers=dataSnapshot1.getValue(ModelUsers.class);
                    if(!modelUsers.getUid().equals(firebaseUser.getUid())){
                        if(modelUsers.getName().toLowerCase().contains(s.toLowerCase())||
                                modelUsers.getEmail().toLowerCase().contains(s.toLowerCase())) {
                            usersList.add(modelUsers);
                        }
                    }
                    adapterUsers=new AdapterUsers(getActivity(),usersList);
                    adapterUsers.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterUsers);
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
        menu.findItem(R.id.addparticipants).setVisible(false);
        menu.findItem(R.id.grpinfo).setVisible(false);
        MenuItem item=menu.findItem(R.id.search);
        menu.findItem(R.id.logout).setVisible(false);
        menu.findItem(R.id.settings).setVisible(false);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query.trim())){
                    searchusers(query);
                }
                else {
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText.trim())){
                    searchusers(newText);
                }
                else {
                    getAllUsers();
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
        else if(item.getItemId()==R.id.settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
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
