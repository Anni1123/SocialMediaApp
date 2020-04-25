package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.adapters.AdapterParticipantsAd;
import com.example.socialmediaapp.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private String myGrprole="";
    FirebaseAuth firebaseAuth;
    String groupid;
    ActionBar actionBar;
    ImageView grpicon;
    TextView descri,createdby,editgrp,addparticipant,leavegrp,totalp;
    ArrayList<ModelUsers> users;
    AdapterParticipantsAd participantsAd;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        groupid=getIntent().getStringExtra("groupId");
        grpicon=findViewById(R.id.grpicons);
        descri=findViewById(R.id.descri);
        createdby=findViewById(R.id.createdby);
        editgrp=findViewById(R.id.editgrp);
        addparticipant=findViewById(R.id.addparticipan);
        leavegrp=findViewById(R.id.leavegrp);
        totalp=findViewById(R.id.totalparticipants);
        recyclerView=findViewById(R.id.particpantstv);
        firebaseAuth=FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGrpRole();
        addparticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupParticipantsAddActivity.class);
                intent.putExtra("groupId",groupid);
                startActivity(intent);
            }
        });
        editgrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId",groupid);
                startActivity(intent);
            }
        });
        leavegrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dialogTitle="";
                String dialogdes="";
                String poitiviebtntitle="";
                if(myGrprole.equals("creator")){
                    dialogTitle = "Delete Group";
                    dialogdes="Are You Sure To delete Group prmanently";
                    poitiviebtntitle="DELETE";
                }
                else {
                    dialogTitle = "Leave Group";
                    dialogdes="Are You Sure To Leave Group prmanently";
                    poitiviebtntitle="LEAVE";
                }
                AlertDialog.Builder builder=new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogdes)
                        .setPositiveButton(poitiviebtntitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(myGrprole.equals("creator")){
                                    deleteGroup();
                                }
                                else {
                                    leavegroup();
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
    }

    private void deleteGroup() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupid).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this,"Group Deleted Sucessfully",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(GroupInfoActivity.this,DashboardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void leavegroup() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupid).child("Participants").child(firebaseAuth.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this,"Removed Sucessfully",Toast.LENGTH_LONG).show();
                        startActivity(new Intent(GroupInfoActivity.this,DashboardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupInfoActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMyGrpRole() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupid).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            myGrprole=""+ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() + "(" + myGrprole + ")");
                            if(myGrprole.equals("participants")){
                                editgrp.setVisibility(View.GONE);
                                addparticipant.setVisibility(View.GONE);
                                leavegrp.setText("Leave Group");
                            }
                            else if(myGrprole.equals("admin")){
                                editgrp.setVisibility(View.GONE);
                                addparticipant.setVisibility(View.VISIBLE);
                                leavegrp.setText("Leave Group");
                            }
                            else if(myGrprole.equals("creator")){
                                editgrp.setVisibility(View.VISIBLE);
                                addparticipant.setVisibility(View.VISIBLE);
                                leavegrp.setText("Exit Group");
                            }

                        }
                        loadParticipants();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadParticipants() {

        users=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(groupid).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    final String uid=""+dataSnapshot1.child("uid").getValue();
                    DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Users");
                    reference1.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           for (DataSnapshot dataSnapshot11:dataSnapshot.getChildren()){
                               ModelUsers userss=dataSnapshot11.getValue(ModelUsers.class);
                               users.add(userss);

                           }
                           participantsAd=new AdapterParticipantsAd(GroupInfoActivity.this,users,groupid,myGrprole);
                           recyclerView.setAdapter(participantsAd);
                           totalp.setText("Participants (" +users.size() + ")");
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

    private void loadGroupInfo() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("grpId").equalTo(groupid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            String grptitle=""+ds.child("grptitle").getValue();
                            String grpdesc=""+ds.child("grpdesc").getValue();
                            String groupicon=""+ds.child("grpicon").getValue();
                            String timstamp=""+ds.child("timestamp").getValue();
                            String craetedby=""+ds.child("createBy").getValue();
                            Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
                            calendar.setTimeInMillis(Long.parseLong(timstamp));
                            String timedate= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
                            loadCreatorInfo(timedate,craetedby);
                            actionBar.setTitle(grptitle);
                            descri.setText(grpdesc);
                            try {
                                Picasso.with(GroupInfoActivity.this).load(groupicon).into(grpicon);
                            }
                            catch (Exception e){

                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadCreatorInfo(final String timedate, String craetedby) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(craetedby).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String name=""+dataSnapshot1.child("name").getValue();
                    createdby.setText("Created By " + name + "on " + timedate);
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
