package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.socialmediaapp.adapters.AdapterParticipantsAd;
import com.example.socialmediaapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.ArrayList;

public class GroupParticipantsAddActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    String groupid,mygrprole;
    ArrayList<ModelUsers> modelUsers;
    AdapterParticipantsAd participantsAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participants_add);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Add Participants");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        firebaseAuth=FirebaseAuth.getInstance();
        recyclerView=findViewById(R.id.usersadd);
        groupid=getIntent().getStringExtra("groupId");
        loadGroupInfo();
    }

    private void getAllUsers() {
        modelUsers=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                modelUsers.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelUsers users=dataSnapshot1.getValue(ModelUsers.class);
                    if(!firebaseAuth.getUid().equals(users.getUid())){
                        modelUsers.add(users);
                    }
                    participantsAd=new AdapterParticipantsAd(GroupParticipantsAddActivity.this,modelUsers,""+groupid,""+mygrprole);
                    recyclerView.setAdapter(participantsAd);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupInfo() {
        final DatabaseReference reference1= FirebaseDatabase.getInstance().getReference("Groups");
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("grpId").equalTo(groupid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    String groupId=""+ ds.child("grpId").getValue();
                    final String grptit=""+ds.child("grptitle").getValue();
                    String grpdesc=""+ds.child("grpdesc").getValue();
                    String grpicon=""+ds.child("grpicon").getValue();
                    String createdBy=""+ds.child("createBy").getValue();
                    String timestamp=""+ds.child("timestamp").getValue();
                    actionBar.setTitle("Add Participants");
                    reference1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    mygrprole=""+dataSnapshot.child("role").getValue();
                                    actionBar.setTitle(grptit + "(" + mygrprole + ")");
                                    getAllUsers();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
