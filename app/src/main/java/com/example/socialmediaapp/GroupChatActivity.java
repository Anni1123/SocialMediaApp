package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.adapters.AdapterGroupChat;
import com.example.socialmediaapp.models.ModelGroupChats;
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
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    String grouid,mygrprole="";
    Toolbar toolbar;
    ImageView grpicon;
    ImageButton attachbtn,sendmsgbtn;
    TextView grpTitle;
    EditText message;
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    ArrayList<ModelGroupChats> groupChatsArrayList;
    AdapterGroupChat adapterGroupChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        toolbar=findViewById(R.id.toolbargrp);
        grpicon=findViewById(R.id.groupicontv);
        attachbtn=findViewById(R.id.grpattach);
        sendmsgbtn=findViewById(R.id.sendgrpmsg);
        grpTitle=findViewById(R.id.grptitletv);
        message=findViewById(R.id.grpmsg);
        recyclerView=findViewById(R.id.grpchatrecycle);
        Intent intent=getIntent();
        grouid=intent.getStringExtra("groupid");
        firebaseAuth=FirebaseAuth.getInstance();
        loadGroupInfo();
        setSupportActionBar(toolbar);

        sendmsgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messag=message.getText().toString().trim();
                if(TextUtils.isEmpty(messag)){
                    Toast.makeText(GroupChatActivity.this,"Cant send Input Message",Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    sendMessage(messag);
                    message.setText("");
                }
            }
        });
        loadMessage();
        loadMygroupRole();
    }

    private void loadMygroupRole() {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(grouid).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            mygrprole=""+ds.child("role").getValue();
                            invalidateOptionsMenu();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadMessage(){
        groupChatsArrayList=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(grouid).child("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        groupChatsArrayList.clear();
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            ModelGroupChats model=ds.getValue(ModelGroupChats.class);
                            groupChatsArrayList.add(model);
                        }
                        adapterGroupChat=new AdapterGroupChat(GroupChatActivity.this,groupChatsArrayList);
                        recyclerView.setAdapter(adapterGroupChat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void loadGroupInfo(){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.orderByChild("grpId").equalTo(grouid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds:dataSnapshot.getChildren()){
                            String grptitle=""+ds.child("grptitle").getValue();
                            String grpdesc=""+ds.child("grpdesc").getValue();
                            String groupicon=""+ds.child("grpicon").getValue();
                            String timstamp=""+ds.child("timestamp").getValue();
                            String craetedby=""+ds.child("createBy").getValue();
                            grpTitle.setText(grptitle);
                            try {
                                Picasso.with(GroupChatActivity.this).load(groupicon).into(grpicon);
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
    private void sendMessage(String messsage){
        String timestamp=String.valueOf(System.currentTimeMillis());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",firebaseAuth.getUid());
        hashMap.put("message",""+messsage);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("type","text");
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(grouid).child("Messages").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        message.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.settings).setVisible(false);
        menu.findItem(R.id.craetegrp).setVisible(false);
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.logout).setVisible(false);

        if(mygrprole.equals("creator")||mygrprole.equals("admin")){
            menu.findItem(R.id.addparticipants).setVisible(true);
        }
        else {
            menu.findItem(R.id.addparticipants).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.addparticipants){
            Intent intent=new Intent(this,GroupParticipantsAddActivity.class);
            intent.putExtra("groupId",grouid);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}

