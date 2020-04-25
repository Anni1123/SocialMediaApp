package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class CreateGroupActivity extends AppCompatActivity {

    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    ImageView icon;
    EditText title,desc;
    FloatingActionButton craetegrp;

    private static final int CAMERA_REQUEST=100;
    private static final int STORAGE_REQUEST=200;
    private static final int IMAGEPICK_GALLERY_REQUEST=300;
    private static final int IMAGE_PICKCAMERA_REQUEST=400;
    String cameraPermission[];
    String storagePermission[];
    ProgressDialog progressDialog;
    Uri imageuri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        actionBar=getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Create Group");
        icon=findViewById(R.id.groupicon);
        title=findViewById(R.id.grptitle);
        desc=findViewById(R.id.grpdesc);
        craetegrp=findViewById(R.id.crtgrpbtbn);
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });
        craetegrp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creategroup();
            }
        });
    }

    private void creategroup() {
        progressDialog=new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Creating Group");
        final String grptitle=title.getText().toString().trim();
        final String grpdes=desc.getText().toString().trim();
        if(TextUtils.isEmpty(grptitle)){
            Toast.makeText(CreateGroupActivity.this,"Title Cant Be Empty",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(grpdes)){
            Toast.makeText(CreateGroupActivity.this,"Description Cant Be Empty",Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.show();
        final String timestamp=""+System.currentTimeMillis();
        if(imageuri==null){

            createGrp(""+timestamp,
                    ""+grptitle,
                    ""+grpdes,
                    "");
        }
        else {
            String filepathname="GroupProfileImages/"+ "Img" + timestamp;
            StorageReference storageReference1= FirebaseStorage.getInstance().getReference(filepathname);
            storageReference1.putFile(imageuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            final Uri downloadUri=uriTask.getResult();
                            if(uriTask.isSuccessful()){
                                createGrp(""+timestamp,
                                        ""+grptitle,
                                        ""+grpdes,
                                        ""+downloadUri);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    private void createGrp(final String time, String title, String description, String grpicon) {

        final HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("grpId",time);
        hashMap.put("grptitle",title);
        hashMap.put("grpdesc",description);
        hashMap.put("grpicon",grpicon);
        hashMap.put("timestamp",time);
        hashMap.put("createBy",firebaseAuth.getUid());
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(time).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        HashMap<String ,String > hashMap1=new HashMap<>();
                        hashMap1.put("uid",firebaseAuth.getUid());
                        hashMap1.put("role","creator");
                        hashMap1.put("timestamp",time);
                        DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Groups");
                        reference1.child(time).child("Participants").child(firebaseAuth.getUid())
                                .setValue(hashMap1)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Toast.makeText(CreateGroupActivity.this,"Created Sucessfully",Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(CreateGroupActivity.this,"Failed to create",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(CreateGroupActivity.this,"Failed to create",Toast.LENGTH_LONG).show();
            }
        });

    }

    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            actionBar.setSubtitle(user.getEmail());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
    private void showImagePicDialog() {
        String options[]={ "Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST:{
                if(grantResults.length>0){
                    boolean camera_accepted=grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(camera_accepted&&writeStorageaccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(CreateGroupActivity.this,"Please Enable Camera and Storage Permissions",Toast.LENGTH_LONG).show();
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
                        Toast.makeText(CreateGroupActivity.this,"Please Enable Storage Permissions",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }

    }
    private void pickFromCamera(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Grp_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Grp Description");
        imageuri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
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
        boolean result= ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        requestPermissions(storagePermission,STORAGE_REQUEST);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode==IMAGEPICK_GALLERY_REQUEST){
                imageuri=data.getData();
                icon.setImageURI(imageuri);
            }
            if(requestCode==IMAGE_PICKCAMERA_REQUEST){
                icon.setImageURI(imageuri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
