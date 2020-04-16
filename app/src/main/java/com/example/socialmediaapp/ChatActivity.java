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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profile;
    TextView name,userstatus;
    EditText msg;
    ImageButton send;
    FirebaseAuth firebaseAuth;
    String uid,myuid;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        firebaseAuth=FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView=findViewById(R.id.chatrecycle);
        profile=findViewById(R.id.profiletv);
        name=findViewById(R.id.nameptv);
        userstatus=findViewById(R.id.onlinetv);
        msg=findViewById(R.id.messaget);
        send=findViewById(R.id.sendmsg);

        uid=getIntent().getStringExtra("uid");
        firebaseDatabase=FirebaseDatabase.getInstance();
        users=firebaseDatabase.getReference("Users");
        Query userquery=users.orderByChild("uid").equalTo(uid);
        userquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String nameh=""+dataSnapshot1.child("name").getValue();
                    String image=""+dataSnapshot1.child("image").getValue();
                    name.setText(nameh);
                    Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.profile_image).into(profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=msg.getText().toString().trim();
                if (TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this,"Please Write Something Here",Toast.LENGTH_LONG).show();
                }
                else {

                    sendmessage(message);
                }
            }
        });
    }

    private void sendmessage(String message) {

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",myuid);
        hashMap.put("receiver",uid);
        hashMap.put("message",message);
        databaseReference.child("Chats").push().setValue(hashMap);
        msg.setText("");
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            myuid=user.getUid();
        }
        else {
            startActivity(new Intent(ChatActivity.this,MainActivity.class));
           finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
