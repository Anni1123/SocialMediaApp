package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.socialmediaapp.notifications.Data;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;

public class PostDetailsActivity extends AppCompatActivity {


    String myuid,myname,myemail,mydp,
    postId,plike,hisdp,hisname;
    ImageView picture,image;
    TextView name,time,title,description,like;
    ImageButton more;
    Button likebtn,share;
    LinearLayout profile;
    EditText comment;
    ImageButton sendb;
    ImageView imagep;
    ActionBar actionBar;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        postId=getIntent().getStringExtra("pid");
        picture=findViewById(R.id.pictureco);
        image=findViewById(R.id.pimagetvco);
        name=findViewById(R.id.unameco);
        time=findViewById(R.id.utimeco);
        more=findViewById(R.id.morebtn);
        title=findViewById(R.id.ptitleco);
        description=findViewById(R.id.descriptco);
        like=findViewById(R.id.plikebco);
        likebtn=findViewById(R.id.like);
        comment=findViewById(R.id.typecommet);
        sendb=findViewById(R.id.sendcomment);
        imagep=findViewById(R.id.commentimge);
        share=findViewById(R.id.share);
        profile=findViewById(R.id.profilelayout);
        progressDialog=new ProgressDialog(this);
        loadPostInfo();
        checkUserStatus();
        loadUserInfo();
        actionBar.setSubtitle("SignedInAs:" +myemail);
        sendb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
    }

    private void postComment() {
        progressDialog.setMessage("Adding Comment");

    }

    private void loadUserInfo() {

        Query myref=FirebaseDatabase.getInstance().getReference("Users");
        myref.orderByChild("uid").equalTo(myuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    myname=dataSnapshot1.child("name").getValue().toString();
                    mydp=dataSnapshot1.child("image").getValue().toString();
                    try {
                        Picasso.with(PostDetailsActivity.this).load(mydp).into(imagep);
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

    private void loadPostInfo() {

        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts");
        Query query=databaseReference.orderByChild("ptime").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String ptitle=dataSnapshot1.child("title").getValue().toString();
                    String descriptions=dataSnapshot1.child("description").getValue().toString();
                    String uimage=dataSnapshot1.child("uimage").getValue().toString();
                    hisdp=dataSnapshot1.child("udp").getValue().toString();
                    String uemail=dataSnapshot1.child("uemail").getValue().toString();
                    hisname=dataSnapshot1.child("uname").getValue().toString();
                    String ptime=dataSnapshot1.child("ptime").getValue().toString();
                    plike=dataSnapshot1.child("plike").getValue().toString();
                    name.setText(hisname);
                    title.setText(ptitle);
                    description.setText(descriptions);
                    like.setText(plike +"Likes");
                    time.setText(ptime);
                    if(uimage.equals("noImage")){
                       image.setVisibility(View.GONE);
                    }
                    else {
                        image.setVisibility(View.VISIBLE);
                        try {
                            Picasso.with(PostDetailsActivity.this).load(uimage).into(image);
                        }
                        catch (Exception e){

                        }
                    }
                    try {
                        Picasso.with(PostDetailsActivity.this).load(hisdp).into(picture);
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

    private void checkUserStatus(){
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){

            myemail=user.getEmail();
            myuid=user.getUid();
        }
        else {
            startActivity(new Intent(PostDetailsActivity.this,MainActivity.class));
            finish();
        }
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.logout){
            FirebaseAuth.getInstance().signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.add).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
}
