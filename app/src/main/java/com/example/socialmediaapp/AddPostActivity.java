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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.socialmediaapp.models.ModelUsers;
import com.example.socialmediaapp.notifications.Data;
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

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    EditText title,des;
    private static final int CAMERA_REQUEST=100;
    private static final int STORAGE_REQUEST=200;
    String cameraPermission[];
    String storagePermission[];
    ProgressDialog pd;
    ImageView image;
    private static final int IMAGEPICK_GALLERY_REQUEST=300;
    private static final int IMAGE_PICKCAMERA_REQUEST=400;

    Uri imageuri=null;
    String name,email,uid,dp;
    DatabaseReference databaseReference;
    Button upload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Post Activity");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        firebaseAuth=FirebaseAuth.getInstance();
        pd=new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        databaseReference= FirebaseDatabase.getInstance().getReference("Users");

        checkUserStatus();
        Query query=databaseReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelUsers modelUsers=dataSnapshot.getValue(ModelUsers.class);
                    name=dataSnapshot1.child("name").getValue().toString();
                    email=""+dataSnapshot1.child("email").getValue();
                    dp=""+dataSnapshot1.child("image").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        title=findViewById(R.id.ptitle);
        des=findViewById(R.id.pdes);
        image=findViewById(R.id.imagep);
        upload=findViewById(R.id.pupload);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titl=title.getText().toString().trim();
                String description=des.getText().toString().trim();
                if(TextUtils.isEmpty(titl)){
                    Toast.makeText(AddPostActivity.this,"Title cant be empty",Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this,"Description cant be empty",Toast.LENGTH_LONG).show();
                    return;
                }
                if(imageuri==null){
                    uploadData(titl,description,"noImage");
                }
                else {
                    uploadData(titl,description,String.valueOf(imageuri));
                }


            }
        });
        actionBar.setSubtitle(email);
    } private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            email=user.getEmail();
            uid=user.getUid();
        }
        else {
            startActivity(new Intent(AddPostActivity.this,MainActivity.class));
            finish();
        }
    }

    private void uploadData(final String titl, final String description, final String uri) {

        pd.setMessage("Publishing Post");
        pd.show();
        final String timestamp=String.valueOf(System.currentTimeMillis());
        String filepathname=  "Posts/" + "post" +timestamp;
        if(!uri.equals("noImage")){

            StorageReference storageReference1= FirebaseStorage.getInstance().getReference().child(filepathname);
            storageReference1.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    String downloadUri=uriTask.getResult().toString();
                    if(uriTask.isSuccessful()){
                        HashMap<Object,String > hashMap=new HashMap<>();
                        hashMap.put("uid",uid);
                        hashMap.put("uname",name);
                        hashMap.put("uemail",email);
                        hashMap.put("udp",dp);
                        hashMap.put("title",titl);
                        hashMap.put("description",description);
                        hashMap.put("uimage",downloadUri);
                        hashMap.put("ptime",timestamp);
                        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts");
                        databaseReference.child(timestamp).setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this,"Published",Toast.LENGTH_LONG).show();
                                        title.setText("");
                                        des.setText("");
                                        image.setImageURI(null);
                                        imageuri=null;
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this,"Failed",Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this,"Failed",Toast.LENGTH_LONG).show();
                }
            });
        }
        else {

                HashMap<Object,String > hashMap=new HashMap<>();
                hashMap.put("uid",uid);
                hashMap.put("uname",name);
                hashMap.put("uemail",email);
                hashMap.put("udp",dp);
                hashMap.put("title",titl);
                hashMap.put("description",description);
                hashMap.put("uimage","noImage");
                hashMap.put("ptime",timestamp);
                DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts");
                databaseReference.child(timestamp).setValue(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this,"Published",Toast.LENGTH_LONG).show();
                                title.setText("");
                                des.setText("");
                                image.setImageURI(null);
                                imageuri=null;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this,"Failed",Toast.LENGTH_LONG).show();
                    }
                });

            }

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

    private Boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
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

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.add).setVisible(false);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode==IMAGEPICK_GALLERY_REQUEST){
                imageuri=data.getData();
                image.setImageURI(imageuri);
            }
            if(requestCode==IMAGE_PICKCAMERA_REQUEST){
             image.setImageURI(imageuri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
