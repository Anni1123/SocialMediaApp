package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
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
import com.example.socialmediaapp.models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String cameraPermission[];
    String storagePermission[];
    Uri imageuri = null;
    boolean notify=false;
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
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth=FirebaseAuth.getInstance();
        loadGroupInfo();
        setSupportActionBar(toolbar);
        loadMessage();
        loadMygroupRole();
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
       attachbtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               showImagePicDialog();
           }
       });
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
    private void showImagePicDialog() {
        String options[]={ "Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(GroupChatActivity.this);
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
        contentValues.put(MediaStore.Images.Media.TITLE,"Group_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Group_Description");
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
        boolean result= ContextCompat.checkSelfPermission(GroupChatActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        requestPermissions(storagePermission,STORAGE_REQUEST);
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
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        final String timestamp=""+System.currentTimeMillis();
        String filepathandname="GroupImages/"+timestamp;
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(filepathandname);
        ref.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                dialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();

                if(uriTask.isSuccessful()){
                    String timestamp=String.valueOf(System.currentTimeMillis());
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put("sender",firebaseAuth.getUid());
                    hashMap.put("message",""+downloadUri);
                    hashMap.put("timestamp",""+timestamp);
                    hashMap.put("type","image");
                    DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Groups");
                    reference.child(grouid).child("Messages").child(timestamp).setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    message.setText("");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(GroupChatActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
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
        if(item.getItemId()==R.id.grpinfo){
            Intent intent=new Intent(this,GroupInfoActivity.class);
            intent.putExtra("groupId",grouid);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}

