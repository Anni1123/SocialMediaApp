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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    EditText title, des;
    private static final int CAMERA_REQUEST = 100;
    private static final int STORAGE_REQUEST = 200;
    String cameraPermission[];
    String storagePermission[];
    ProgressDialog pd;
    ImageView image,image2;
    String edititle, editdes, editimage;
    private static final int IMAGEPICK_GALLERY_REQUEST = 300;
    private static final int IMAGE_PICKCAMERA_REQUEST = 400;

    Uri imageuri = null;
    String name, email, uid, dp;
    DatabaseReference databaseReference;
    Button upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Post Activity");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();
        title = findViewById(R.id.ptitle);
        des = findViewById(R.id.pdes);
        image = findViewById(R.id.imagep);
        upload = findViewById(R.id.pupload);
        pd = new ProgressDialog(this);
        pd.setCanceledOnTouchOutside(false);
        Intent intent = getIntent();
        String action=intent.getAction();
        String type=intent.getType();
        if(Intent.ACTION_SEND.equals(action) && type!=null){
            if("text/plain".equals(type)){

                handlesendText(intent);
            }
            else if(type.startsWith("image")){
                handlesendImage(intent);
            }
        }
        final String updatekey = "" + intent.getStringExtra("key");
        final String editpost = "" + intent.getStringExtra("editpostId");
        if (updatekey.equals("editpost")) {
            actionBar.setTitle("Edit Post");
            upload.setText("Update Post");
            loadTextData(editpost);
        } else {
            actionBar.setTitle("Add New Post");
            upload.setText("Upload");
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        checkUserStatus();
        Query query = databaseReference.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ModelUsers modelUsers = dataSnapshot.getValue(ModelUsers.class);
                    name = dataSnapshot1.child("name").getValue().toString();
                    email = "" + dataSnapshot1.child("email").getValue();
                    dp = "" + dataSnapshot1.child("image").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePicDialog();
            }
        });
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titl = ""+title.getText().toString().trim();
                String description = ""+des.getText().toString().trim();
                if (TextUtils.isEmpty(titl)) {
                    Toast.makeText(AddPostActivity.this, "Title can't be left empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    Toast.makeText(AddPostActivity.this, "Description can't be left empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if (updatekey.equals("editpost")) {
                    beginupdate(titl, description, editpost);
                } else {
                    uploadData(titl, description);
                }


            }
        });
        actionBar.setSubtitle(email);
    }

    private void handlesendImage(Intent intent) {
        Uri imageUri=(Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if(imageUri!=null){
            imageuri=imageUri;
            image.setImageURI(imageuri);
        }
    }

    private void handlesendText(Intent intent) {
        String sharedText=intent.getStringExtra(Intent.EXTRA_TEXT);
        if(sharedText!=null){
            des.setText(sharedText);
        }
    }

    private void beginupdate(String titl, String description, String editpost) {

        pd.setMessage("Updating");
        pd.show();
        if (!editimage.equals("noImage")) {
            updatewithImage(titl, description, editpost);
        } else if(image.getDrawable()!=null) {
            updatewithNowImage(titl, description, editpost);
        }
        else {
            updatewithOutImage(titl,description,editpost);
        }
    }

    private void updatewithOutImage(String titl, String description, String editpost) {
        pd.show();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("uname", name);
        hashMap.put("uemail", email);
        hashMap.put("udp", dp);
        hashMap.put("title", titl);
        hashMap.put("description", description);
        hashMap.put("uimage", "noImage");
        hashMap.put("plike","0");
        hashMap.put("pcomments","0");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        databaseReference.child(editpost).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Published", Toast.LENGTH_LONG).show();
                        title.setText("");
                        des.setText("");
                        image.setImageURI(null);
                        imageuri = null;
                        startActivity(new Intent(AddPostActivity.this,DashboardActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddPostActivity.this, "Failed ", Toast.LENGTH_LONG).show();
            }
        });


    }

    private void updatewithNowImage(final String titl, final String description, final String editpost) {
        final String timestamp = String.valueOf(System.currentTimeMillis());
        String filepathname = "Posts/" + "post" + timestamp;
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child(filepathname);
        storageReference1.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful()) ;
                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful()) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("uid", uid);
                    hashMap.put("uname", name);
                    hashMap.put("uemail", email);
                    hashMap.put("udp", dp);
                    hashMap.put("title", titl);
                    hashMap.put("description", description);
                    hashMap.put("uimage", downloadUri);
                    hashMap.put("plike","0");
                    hashMap.put("pcomments","0");
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                    databaseReference.child(editpost).updateChildren(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Published", Toast.LENGTH_LONG).show();
                                    title.setText("");
                                    des.setText("");
                                    image.setImageURI(null);
                                    imageuri = null;
                                    startActivity(new Intent(AddPostActivity.this,DashboardActivity.class));
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Failed", Toast.LENGTH_LONG).show();
                        }
                    });


                }
            }
        });
    }

    private void updatewithImage(final String titl, final String description, final String editpost) {
        pd.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(editimage);
        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                final String timestamp = String.valueOf(System.currentTimeMillis());
                String filepathname = "Posts/" + "post" + timestamp;
                Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] data = byteArrayOutputStream.toByteArray();

                StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child(filepathname);
                storageReference1.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) ;
                        String downloadUri = uriTask.getResult().toString();
                        if (uriTask.isSuccessful()) {
                            HashMap<String,Object> hashMap = new HashMap<>();
                            hashMap.put("uid", uid);
                            hashMap.put("uname", name);
                            hashMap.put("uemail", email);
                            hashMap.put("udp", dp);
                            hashMap.put("title", titl);
                            hashMap.put("description", description);
                            hashMap.put("uimage", downloadUri);
                            hashMap.put("plike","0");
                            hashMap.put("pcomments","0");
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
                            databaseReference.child(editpost).updateChildren(hashMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            pd.dismiss();
                                            Toast.makeText(AddPostActivity.this, "Published", Toast.LENGTH_LONG).show();
                                            title.setText("");
                                            des.setText("");
                                            image.setImageURI(null);
                                            imageuri = null;
                                            startActivity(new Intent(AddPostActivity.this,DashboardActivity.class));
                                            finish();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(AddPostActivity.this, "Failed", Toast.LENGTH_LONG).show();
                                }
                            });

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Failed", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    private void showImagePicDialog() {
        String options[]={ "Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(AddPostActivity.this);
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



    private void loadTextData(final String editpost) {
        DatabaseReference databaseRef=FirebaseDatabase.getInstance().getReference("Posts");
        Query query=databaseRef.orderByChild("ptime").equalTo(editpost);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){

                   editdes=dataSnapshot1.child("description").getValue().toString();
                    edititle=""+dataSnapshot1.child("title").getValue();
                    editimage=""+dataSnapshot1.child("uimage").getValue().toString();
                    title.setText(edititle);
                    des.setText(editdes);
                    try {
                        Picasso.with(AddPostActivity.this).load(editimage).into(image);
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
    private void prepareNotification(String pId,String title,String description,String notificationtype,String notificationtopic) throws JSONException {

        String NOTIFICATION_TOPIC="/topics/" + notificationtopic;
        String NOTIFICATION_TILE=title;
        String NOTIFICATION_ESSAGE=description;
        String NOTIFICATION_TYPE=notificationtype;

        JSONObject object=new JSONObject();
        JSONObject notificationObject=new JSONObject();
        notificationObject.put("notificationType",NOTIFICATION_TYPE);
        notificationObject.put("sender",uid);
        notificationObject.put("pId",pId);
        notificationObject.put("pTitle",NOTIFICATION_TILE);
        notificationObject.put("pDescription",NOTIFICATION_ESSAGE);
        object.put("to",NOTIFICATION_TOPIC);
        object.put("data",notificationObject);

        sendNotification(object);
    }

    private void sendNotification(JSONObject object) {
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("FCM_RESPONSE","onResponse:"+response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AddPostActivity.this,""+error.getMessage(),Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String ,String > map=new HashMap<>();
                map.put("Content-Type","application/json");
                map.put("Authorization","key=AAAAux-y-Cc:APA91bFXuXd6jvnnZ2ZC3kGL4tEfkug7ruuxI1HrDimSboYCAL0ZrdxZnCD0y949pW6Xf15n28iDe3H7GesRtmqvOlh60XNLGVkgaCYcYjYeC3Gmg2UXJtzo5GK3ws9FTRh6FQqVp5r5");

                return map;
            }
        };
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void checkUserStatus(){
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

    private void uploadData(final String titl, final String description) {

        pd.setMessage("Publishing Post");
        pd.show();
        final String timestamp=String.valueOf(System.currentTimeMillis());
        String filepathname=  "Posts/" + "post" +timestamp;
        if(image.getDrawable()!=null)
        {

            Bitmap bitmap=((BitmapDrawable)image.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
            byte[] data=byteArrayOutputStream.toByteArray();

            StorageReference storageReference1= FirebaseStorage.getInstance().getReference().child(filepathname);
            storageReference1.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                        hashMap.put("plike","0");
                        hashMap.put("pcomments","0");
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
                                        try {
                                            prepareNotification(
                                                    ""+timestamp
                                            ,""+name+" Added new post ",
                                                    ""+titl+"\n"+description,
                                                    "POST_NOTIFICATION",
                                                    "POST");

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        startActivity(new Intent(AddPostActivity.this,DashboardActivity.class));
                                        finish();
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

            pd.show();
                HashMap<Object,String > hashMap=new HashMap<>();
                hashMap.put("uid",uid);
                hashMap.put("uname",name);
                hashMap.put("uemail",email);
                hashMap.put("udp",dp);
                hashMap.put("title",titl);
                hashMap.put("description",description);
                hashMap.put("uimage","noImage");
                hashMap.put("ptime",timestamp);
                hashMap.put("plike","0");
                hashMap.put("pcomments","0");
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
                                try {
                                    prepareNotification(
                                            ""+timestamp
                                            ,""+name+" added new post ",
                                            ""+titl+ " " ,
                                            "POST_NOTIFICATION","POST");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                startActivity(new Intent(AddPostActivity.this,DashboardActivity.class));
                                finish();
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

    private Boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(AddPostActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                //function end
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
