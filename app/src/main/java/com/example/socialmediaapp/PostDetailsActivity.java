package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;


import com.example.socialmediaapp.adapters.AdapterComment;
import com.example.socialmediaapp.models.ModelComment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailsActivity extends AppCompatActivity {


    String hisuid,ptime,myuid,myname,myemail,mydp,uimage,
    postId,plike,hisdp,hisname;
    ImageView picture,image;
    TextView name,time,title,description,like,tcomment;
    ImageButton more;
    Button likebtn,share;
    LinearLayout profile;
    EditText comment;
    ImageButton sendb;
    RecyclerView recyclerView;
    List<ModelComment> commentList;
    AdapterComment adapterComment;
    ImageView imagep;
    boolean mlike=false;
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
        recyclerView=findViewById(R.id.recyclecomment);
        checkUserStatus();
        picture=findViewById(R.id.pictureco);
        image=findViewById(R.id.pimagetvco);
        name=findViewById(R.id.unameco);
        time=findViewById(R.id.utimeco);
        more=findViewById(R.id.morebtn);
        title=findViewById(R.id.ptitleco);
        description=findViewById(R.id.descriptco);
        tcomment=findViewById(R.id.pcommenttv);
        like=findViewById(R.id.plikebco);
        likebtn=findViewById(R.id.like);
        comment=findViewById(R.id.typecommet);
        sendb=findViewById(R.id.sendcomment);
        imagep=findViewById(R.id.commentimge);
        share=findViewById(R.id.share);
        profile=findViewById(R.id.profilelayout);
        progressDialog=new ProgressDialog(this);
        loadPostInfo();

        loadUserInfo();
        setLikes();
        actionBar.setSubtitle("SignedInAs:" +myemail);
        loadComments();
        sendb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
        likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likepost();
            }
        });
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showmoreoptions();
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titlee=title.getText().toString().trim();
                String des=description.getText().toString().trim();
                BitmapDrawable bitmapDrawable=(BitmapDrawable)image.getDrawable();
                if(bitmapDrawable==null){
                    shareTextOnly(titlee,des);
                }
                else {
                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    shareImageandText(titlee,des,bitmap);
                }
            }
        });
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PostDetailsActivity.this, PostLikedByActivity.class);
                intent.putExtra("pid",postId);
                startActivity(intent);
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
    private void shareTextOnly(String titlee, String descri) {

        String sharebody= titlee + "\n" + descri;
        Intent intentt=new Intent(Intent.ACTION_SEND);
        intentt.setType("text/plain");
        intentt.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        intentt.putExtra(Intent.EXTRA_TEXT,sharebody);
        startActivity(Intent.createChooser(intentt,"Share Via"));
    }

    private void shareImageandText(String titlee, String descri, Bitmap bitmap) {
        Uri uri=saveImageToShare(bitmap);
        String sharebody= titlee + "\n" + descri;
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,sharebody);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        intent.setType("image/png");
        startActivity(Intent.createChooser(intent,"Share Via"));
    }
    private void addToHisNotification(String hisUid,String pid,String notification){
        String timestamp=""+System.currentTimeMillis();
        HashMap<Object,String> hashMap=new HashMap<>();
        hashMap.put("pid",pid);
        hashMap.put("timestamp",timestamp);
        hashMap.put("puid",hisUid);
        hashMap.put("notification",notification);
        hashMap.put("suid",myuid);
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }
    private Uri saveImageToShare(Bitmap bitmap) {
        File imagefolder=new File(getCacheDir(),"images");
        Uri uri=null;
        try {
            imagefolder.mkdirs();
            File file=new File(imagefolder,"shared_image.png");
            FileOutputStream outputStream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,outputStream);
            outputStream.flush();
            outputStream.close();
            uri= FileProvider.getUriForFile(this,"com.example.socialmediaapp.fileprovider",file);
        }
        catch (Exception e){

            Toast.makeText(PostDetailsActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return uri;
    }

    private void loadComments() {

        LinearLayoutManager layoutManager=new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        commentList=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                commentList.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelComment modelComment=dataSnapshot1.getValue(ModelComment.class);
                    commentList.add(modelComment);
                    adapterComment=new AdapterComment(getApplicationContext(),commentList,myuid,postId);
                    recyclerView.setAdapter(adapterComment);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showmoreoptions() {
        PopupMenu popupMenu=new PopupMenu(PostDetailsActivity.this,more, Gravity.END);
        if(ptime.equals(myuid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"DELETE");
            popupMenu.getMenu().add(Menu.NONE,1,0,"EDIT");
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==0){
                    beginDelete();
                }
                else if(item.getItemId()==1){
                    Intent intent=new Intent(PostDetailsActivity.this, AddPostActivity.class);
                    intent.putExtra("key","editpost");
                    intent.putExtra("editpostId",postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {

        if(uimage.equals("noImage")){
            deleteWithoutImage();
        }
        else {
            deltewithImage();
        }
    }

    private void deltewithImage(){
        final ProgressDialog pd=new ProgressDialog(this);
        pd.setMessage("Deleting");
        StorageReference picref= FirebaseStorage.getInstance().getReferenceFromUrl(uimage);
        picref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query query= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("ptime").equalTo(postId);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            dataSnapshot1.getRef().removeValue();
                        }
                        pd.dismiss();
                        Toast.makeText(PostDetailsActivity.this,"Deleted Sucessfully",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    private void deleteWithoutImage() {
        final ProgressDialog pd=new ProgressDialog(PostDetailsActivity.this);
        pd.setMessage("Deleting");
        Query query= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("ptime").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    dataSnapshot1.getRef().removeValue();
                }
                pd.dismiss();
                Toast.makeText(PostDetailsActivity.this,"Deleted Sucessfully",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes() {
        final DatabaseReference liekeref=FirebaseDatabase.getInstance().getReference().child("Likes");
        liekeref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(postId).hasChild(myuid)){
                    likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    likebtn.setText("Liked");
                }
                else {
                   likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    likebtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likepost() {

        mlike=true;
        final DatabaseReference liekeref=FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postref=FirebaseDatabase.getInstance().getReference().child("Posts");
        liekeref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(mlike){
                    if(dataSnapshot.child(postId).hasChild(myuid)){
                        postref.child(postId).child("plike").setValue(""+(Integer.parseInt(plike)-1));
                        liekeref.child(postId).child(myuid).removeValue();
                        mlike=false;

                    }
                    else {
                        postref.child(postId).child("plike").setValue(""+(Integer.parseInt(plike)+1));
                        liekeref.child(postId).child(myuid).setValue("Liked");
                        mlike=false;
                        addToHisNotification(""+hisuid,""+myuid,"Liked Your Post");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void postComment() {
        progressDialog.setMessage("Adding Comment");

        final String commentss=comment.getText().toString().trim();
        if(TextUtils.isEmpty(commentss)){
            Toast.makeText(PostDetailsActivity.this,"Empty comment",Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.show();
        String timestamp=String.valueOf(System.currentTimeMillis());
        DatabaseReference datarf=FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        HashMap<String ,Object> hashMap=new HashMap<>();
        hashMap.put("cId",timestamp);
        hashMap.put("comment",commentss);
        hashMap.put("ptime",timestamp);
        hashMap.put("uid",myuid);
        hashMap.put("uemail",myemail);
        hashMap.put("udp",mydp);
        hashMap.put("uname",myname);
        datarf.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailsActivity.this,"Added",Toast.LENGTH_LONG).show();
                addToHisNotification(""+hisuid,""+myuid,"Commented On Your Post");
                comment.setText("");
                updatecommetcount();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailsActivity.this,"Failed",Toast.LENGTH_LONG).show();
            }
        });
    }

    boolean count=false;
    private void updatecommetcount() {
        count=true;
        final DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(count){
                    String comments=""+dataSnapshot.child("pcomments").getValue();
                    int newcomment=Integer.parseInt(comments)+1;
                    reference.child("pcomments").setValue(""+newcomment);
                    count=false;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                  uimage=dataSnapshot1.child("uimage").getValue().toString();
                    hisdp=dataSnapshot1.child("udp").getValue().toString();
                    hisuid=dataSnapshot1.child("uid").getValue().toString();
                    String uemail=dataSnapshot1.child("uemail").getValue().toString();
                    hisname=dataSnapshot1.child("uname").getValue().toString();
                    ptime=dataSnapshot1.child("ptime").getValue().toString();
                    plike=dataSnapshot1.child("plike").getValue().toString();
                    String commentcount=dataSnapshot1.child("pcomments").getValue().toString();
                    Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
                    calendar.setTimeInMillis(Long.parseLong(ptime));
                    String timedate= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
                    name.setText(hisname);
                    title.setText(ptitle);
                    description.setText(descriptions);
                    like.setText(plike +" Likes");
                    time.setText(timedate);
                    tcomment.setText(commentcount + " Comments");
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
        menu.findItem(R.id.grpinfo).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }
}
