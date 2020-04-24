package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.adapters.AdapterPosts;
import com.example.socialmediaapp.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    RecyclerView postrecycle;
    List<ModelPost> posts;
    AdapterPosts adapterPosts;
    String uid,fuid;
    ImageView avatartv,covertv;
    TextView nam,email,phone;
    ActionBar actionBar;
    ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Profile Activity");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        postrecycle=findViewById(R.id.recyclerposts);
        avatartv=findViewById(R.id.avatartv);
        covertv=findViewById(R.id.cavertv);
        nam=findViewById(R.id.nametv);
        email=findViewById(R.id.emailtv);
        pd=new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        phone=findViewById(R.id.phonetv);
        firebaseAuth=FirebaseAuth.getInstance();
        uid=getIntent().getStringExtra("uid");
        posts=new ArrayList<>();
        Query query=FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String name=""+dataSnapshot1.child("name").getValue();
                    String emaill=""+dataSnapshot1.child("email").getValue();
                    String phonee=""+dataSnapshot1.child("phone").getValue();
                    String image=""+dataSnapshot1.child("image").getValue();
                    String cover=""+dataSnapshot1.child("cover").getValue();
                    nam.setText(name);
                    email.setText(emaill);
                    phone.setText(phonee);
                    try {
                        Picasso.with(ThereProfileActivity.this).load(image).into(avatartv);
                    }catch (Exception e){

                    }
                    try {
                        Picasso.with(ThereProfileActivity.this).load(cover).into(covertv);
                    }catch (Exception e){
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        checkUserStatus();
        loadHisPosts();

    }
    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            fuid=user.getUid();
        }
        else {
            startActivity(new Intent(ThereProfileActivity.this,MainActivity.class));
           finish();
        }
    }
    private void loadHisPosts() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        postrecycle.setLayoutManager(layoutManager);

        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts");
        Query query=databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelPost modelPost=dataSnapshot1.getValue(ModelPost.class);
                    posts.add(modelPost);
                    adapterPosts=new AdapterPosts(ThereProfileActivity.this,posts);
                    postrecycle.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(ThereProfileActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void searchMyPosts(final String search) {
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        postrecycle.setLayoutManager(layoutManager);

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts");
        Query query=databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelPost modelPost=dataSnapshot1.getValue(ModelPost.class);
                    if(modelPost.getTitle().toLowerCase().contains(search.toLowerCase())||
                            modelPost.getDescription().toLowerCase().contains(search.toLowerCase())) {
                        posts.add(modelPost);
                    }
                    adapterPosts=new AdapterPosts(ThereProfileActivity.this,posts);
                    postrecycle.setAdapter(adapterPosts);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(ThereProfileActivity.this,databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.craetegrp).setVisible(false);
        MenuItem item=menu.findItem(R.id.search);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchMyPosts(query);
                }
                else {
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    searchMyPosts(newText);
                }
                else {
                    loadHisPosts();
                }
                return false;
            }
        });
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
