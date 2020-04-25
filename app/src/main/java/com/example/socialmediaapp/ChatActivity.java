package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.socialmediaapp.adapters.AdapterChat;
import com.example.socialmediaapp.models.ModelChat;
import com.example.socialmediaapp.models.ModelUsers;
import com.example.socialmediaapp.notifications.Data;
import com.example.socialmediaapp.notifications.Sender;
import com.example.socialmediaapp.notifications.Token;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profile,block;
    TextView name,userstatus;
    EditText msg;
    ImageButton send,attach;
    FirebaseAuth firebaseAuth;
    String uid,myuid,image;
    ValueEventListener valueEventListener;
    DatabaseReference userforseen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;

    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String cameraPermission[];
    String storagePermission[];
    Uri imageuri = null;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference users;
    private RequestQueue requestQueue;
    boolean notify=false;
    boolean isBlocked=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        firebaseAuth=FirebaseAuth.getInstance();
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("");
        profile=findViewById(R.id.profiletv);
        name=findViewById(R.id.nameptv);
        userstatus=findViewById(R.id.onlinetv);
        msg=findViewById(R.id.messaget);
        send=findViewById(R.id.sendmsg);
        attach=findViewById(R.id.attachbtn);
        block=findViewById(R.id.block);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView=findViewById(R.id.chatrecycle);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        requestQueue= Volley.newRequestQueue(getApplicationContext());
        uid=getIntent().getStringExtra("uid");
        firebaseDatabase=FirebaseDatabase.getInstance();
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        checkUserStatus();
        users=firebaseDatabase.getReference("Users");

        Query userquery=users.orderByChild("uid").equalTo(uid);
        userquery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()) {
                    String nameh = "" + dataSnapshot1.child("name").getValue();
                    image = "" + dataSnapshot1.child("image").getValue();
                    String onlinestatus = "" + dataSnapshot1.child("onlineStatus").getValue();
                    String typingto = "" + dataSnapshot1.child("typingTo").getValue();
                    if(typingto.equals(myuid)){
                        userstatus.setText("Typing....");
                    }
                    else {
                        if (onlinestatus.equals("online")) {
                            userstatus.setText(onlinestatus);
                        } else {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(Long.parseLong(onlinestatus));
                            String timedate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();
                            userstatus.setText("Last Seen:" + timedate);
                        }
                    }
                    name.setText(nameh);
                    try {
                        Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.profile_image).into(profile);
                    }
                    catch (Exception e){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
                String message=msg.getText().toString().trim();
                if (TextUtils.isEmpty(message)){
                    Toast.makeText(ChatActivity.this,"Please Write Something Here",Toast.LENGTH_LONG).show();
                }
                else {

                    sendmessage(message);
                }
                msg.setText("");
            }

        });
        block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isBlocked){
                    unBlockUser();
                }
                else {
                    blockUser();
                }
            }
        });
        readMessages();
        checkisBlocked();
        seenMessgae();
    }

    private void checkisBlocked() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("BlockUsers").orderByChild("uid").equalTo(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            if(dataSnapshot1.exists()) {

                               block.setImageResource(R.drawable.ic_block);
                               isBlocked=true;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void blockUser() {
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("uid",uid);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("BlockUsers").child(uid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ChatActivity.this,"Blocked Users",Toast.LENGTH_LONG).show();
                        block.setImageResource(R.drawable.ic_block);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this,"Failed",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void unBlockUser() {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("BlockUsers").orderByChild("uid")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            if(dataSnapshot1.exists()){
                                dataSnapshot1.getRef().removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(ChatActivity.this,"UnBlocked Users",Toast.LENGTH_LONG).show();
                                                block.setImageResource(R.drawable.ic_unblock);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(ChatActivity.this,"Failed",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    private void seenMessgae() {
        userforseen=FirebaseDatabase.getInstance().getReference("Chats");
        valueEventListener=userforseen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelChat chat=dataSnapshot1.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myuid)&&chat.getSender().equals(uid)){
                        HashMap<String ,Object> hashMap=new HashMap<>();
                        hashMap.put("dilihat",true);
                        dataSnapshot1.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }
                else {
                    checkTypingStatus(uid);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().length()==0){
                    checkTypingStatus("noOne");
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp= String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userforseen.removeEventListener(valueEventListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void checkOnlineStatus(String status){

        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Users").child(myuid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus", status);
        dbref.updateChildren(hashMap);
    }
    private void checkTypingStatus(String typing){

        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Users").child(myuid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("typingTo", typing);
        dbref.updateChildren(hashMap);
    }
    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }
    private void readMessages() {

        chatList=new ArrayList<>();
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference().child("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                chatList.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelChat modelChat=dataSnapshot1.getValue(ModelChat.class);
                    if(modelChat.getSender().equals(myuid)&&
                            modelChat.getReceiver().equals(uid)||
                            modelChat.getReceiver().equals(myuid)
                                    && modelChat.getSender().equals(uid)){
                        chatList.add(modelChat);
                    }
                    adapterChat=new AdapterChat(ChatActivity.this,chatList,image);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void showImagePicDialog() {
        String options[]={ "Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }else if(which==1){
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }

                }
            }
        });
        builder.create().show();
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST:{
                if(grantResults.length>0){
                    boolean camera_accepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(camera_accepted&&writeStorageaccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this,"Please Enable Camera and Storage Permissions",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST:{
                if(grantResults.length>0){
                    boolean writeStorageaccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageaccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this,"Please Enable Storage Permissions",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode==IMAGEPICK_GALLERY_REQUEST){
                imageuri=data.getData();
                try {
                    sendImageMessage(imageuri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(requestCode==IMAGE_PICKCAMERA_REQUEST){
                try {
                    sendImageMessage(imageuri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri imageuri) throws IOException {
        notify=true;
        final ProgressDialog dialog=new ProgressDialog(this);
        dialog.setMessage("Sending Image");
        dialog.show();

        final String timestamp=""+System.currentTimeMillis();
        String filepathandname="ChatImages/"+"post"+timestamp;
        Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageuri);
        ByteArrayOutputStream arrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,arrayOutputStream);
        final byte[] data=arrayOutputStream.toByteArray();
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(filepathandname);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    DatabaseReference re=FirebaseDatabase.getInstance().getReference();
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("sender",myuid);
                    hashMap.put("receiver",uid);
                    hashMap.put("message",downloadUri);
                    hashMap.put("timestamp",timestamp);
                    hashMap.put("dilihat",false);
                    hashMap.put("type","images");
                    re.child("Chats").push().setValue(hashMap);
                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(myuid);
                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          ModelUsers users=dataSnapshot.getValue(ModelUsers.class);
                          if(notify){
                              sendNotification(uid,users.getName(),"Sent You a Photo");
                          }
                          notify=false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    final DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("ChatList").child(uid).child(myuid);
                    ref1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(!dataSnapshot.exists()){
                                ref1.child("id").setValue(myuid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    final DatabaseReference ref2=FirebaseDatabase.getInstance().getReference("ChatList").child(myuid).child(uid);
                    ref2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(!dataSnapshot.exists()){
                                ref2.child("id").setValue(uid);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private Boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        requestPermissions(cameraPermission,CAMERA_REQUEST);
    }
    private void pickFromCamera(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        imageuri=this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent camerIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri);
        startActivityForResult(camerIntent,IMAGE_PICKCAMERA_REQUEST);
    }
    private void pickFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGEPICK_GALLERY_REQUEST);
    }
    private Boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(ChatActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        requestPermissions(storagePermission,STORAGE_REQUEST);
    }
    private void sendmessage(final String message) {

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        String timestamp=String.valueOf(System.currentTimeMillis());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",myuid);
        hashMap.put("receiver",uid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("dilihat",false);
        hashMap.put("type","text");
        databaseReference.child("Chats").push().setValue(hashMap);
        DatabaseReference databaseReference1=FirebaseDatabase.getInstance().getReference("Users").child(myuid);
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUsers modelUsers=dataSnapshot.getValue(ModelUsers.class);
                if(notify){
                    sendNotification(uid,modelUsers.getName(),message);
                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("ChatList").child(uid).child(myuid);
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    ref1.child("id").setValue(myuid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final DatabaseReference ref2=FirebaseDatabase.getInstance().getReference("ChatList").child(myuid).child(uid);
        ref2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists()){
                    ref2.child("id").setValue(uid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final String uid, final String name, final String message) {
        DatabaseReference alltoken=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=alltoken.orderByKey().equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    Token token=dataSnapshot1.getValue(Token.class);
                    Data data=new Data(
                            ""+myuid,
                            "ChatNotification",
                            ""+name + ": " + message,
                            "New Message",
                            ""+uid
                    ,R.drawable.profile_image);
                    Sender sender=new Sender(data,token.getToken());
                   try {
                       JSONObject jsonObject = new JSONObject(new Gson().toJson(sender));
                       JsonObjectRequest objectRequest =new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", jsonObject, new Response.Listener<JSONObject>() {
                           @Override
                           public void onResponse(JSONObject response) {

                               Log.d("JSON_RESPONSE","onResponse: " +response.toString());
                           }
                       }, new Response.ErrorListener() {
                           @Override
                           public void onErrorResponse(VolleyError error) {
                               Log.d("JSON_RESPONSE","onResponse: " +error.toString());

                           }
                       }) {
                           @Override
                           public Map<String, String> getHeaders() throws AuthFailureError {
                               Map<String ,String > map=new HashMap<>();
                               map.put("Content-Type","application/json");
                               map.put("Authorization","key=AAAAux-y-Cc:APA91bFXuXd6jvnnZ2ZC3kGL4tEfkug7ruuxI1HrDimSboYCAL0ZrdxZnCD0y949pW6Xf15n28iDe3H7GesRtmqvOlh60XNLGVkgaCYcYjYeC3Gmg2UXJtzo5GK3ws9FTRh6FQqVp5r5");

                               return map;
                           }
                       };
                       requestQueue.add(objectRequest);
                   }catch (JSONException e){

                       e.printStackTrace();
                   }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.craetegrp).setVisible(false);
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.addparticipants).setVisible(false);
        menu.findItem(R.id.grpinfo).setVisible(false);
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


}
