package com.example.socialmediaapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.AddPostActivity;
import com.example.socialmediaapp.PostDetailsActivity;
import com.example.socialmediaapp.PostLikedByActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.ThereProfileActivity;
import com.example.socialmediaapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{


    Context context;
    String myuid;
    private DatabaseReference liekeref,postref;
    boolean mprocesslike=false;


    public AdapterPosts(Context context, List<ModelPost> modelPosts) {
        this.context = context;
        this.modelPosts = modelPosts;
        myuid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        liekeref=FirebaseDatabase.getInstance().getReference().child("Likes");
        postref=FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    List<ModelPost> modelPosts;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_posts,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        final String uid=modelPosts.get(position).getUid();
        String nameh=modelPosts.get(position).getUname();
        final String titlee=modelPosts.get(position).getTitle();
        final String descri=modelPosts.get(position).getDescription();
        final String ptime=modelPosts.get(position).getPtime();
        String dp=modelPosts.get(position).getUdp();
        String plike=modelPosts.get(position).getPlike();
        final String image=modelPosts.get(position).getUimage();
        String email=modelPosts.get(position).getUemail();
        String comm=modelPosts.get(position).getPcomments();
        final String pid=modelPosts.get(position).getPid();
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(ptime));
        String timedate= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
        holder.name.setText(nameh);
        holder.title.setText(titlee);
        holder.description.setText(descri);
        holder.time.setText(timedate);
        holder.like.setText(plike + " Likes");
        holder.comments.setText(comm + " Comments");
        setLikes(holder,ptime);
        try {
            Picasso.with(context).load(dp).into(holder.picture);
        } catch (Exception e) {

        }

        if(image.equals("noImage")){

            holder.image.setVisibility(View.GONE);
        }
        else {
            holder.image.setVisibility(View.VISIBLE);
            try {
                Picasso.with(context).load(image).into(holder.image);
            }
            catch (Exception e){

            }
        }
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.more,uid, myuid,ptime,image);
            }
        });
        holder.likebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final int plike=Integer.parseInt(modelPosts.get(position).getPlike());
                mprocesslike=true;
                final String postid=modelPosts.get(position).getPtime();
                liekeref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(mprocesslike){
                            if(dataSnapshot.child(postid).hasChild(myuid)){
                                postref.child(postid).child("plike").setValue(""+(plike-1));
                                liekeref.child(postid).child(myuid).removeValue();
                                mprocesslike=false;
                            }
                            else {
                                postref.child(postid).child("plike").setValue(""+(plike+1));
                                liekeref.child(postid).child(myuid).setValue("Liked");
                                mprocesslike=false;
                                addToHisNotification(""+uid,""+postid,"Liked Your Post");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PostDetailsActivity.class);
                intent.putExtra("pid",ptime);
                context.startActivity(intent);
            }
        });
        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable bitmapDrawable=(BitmapDrawable)holder.image.getDrawable();
                if(bitmapDrawable==null){
                    shareTextOnly(titlee,descri);
                }
                else {
                    Bitmap bitmap=bitmapDrawable.getBitmap();
                    shareImageandText(titlee,descri,bitmap);
                }
            }
        });
        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PostLikedByActivity.class);
                intent.putExtra("pid",ptime);
                context.startActivity(intent);
            }
        });
    }
    private void shareTextOnly(String titlee, String descri) {

        String sharebody= titlee + "\n" + descri;
        Intent intentt=new Intent(Intent.ACTION_SEND);
        intentt.setType("text/plain");
        intentt.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        intentt.putExtra(Intent.EXTRA_TEXT,sharebody);
        context.startActivity(Intent.createChooser(intentt,"Share Via"));
    }

    private void shareImageandText(String titlee, String descri, Bitmap bitmap) {
        Uri uri=saveImageToShare(bitmap);
        String sharebody= titlee + "\n" + descri;
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        intent.putExtra(Intent.EXTRA_TEXT,sharebody);
        intent.putExtra(Intent.EXTRA_SUBJECT,"Subject Here");
        intent.setType("image/png");
        context.startActivity(Intent.createChooser(intent,"Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imagefolder=new File(context.getCacheDir(),"images");
        Uri uri=null;
        try {
            imagefolder.mkdirs();
            File file=new File(imagefolder,"shared_image.png");
            FileOutputStream outputStream=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,90,outputStream);
            outputStream.flush();
            outputStream.close();
            uri= FileProvider.getUriForFile(context,"com.example.socialmediaapp.fileprovider",file);
        }
        catch (Exception e){

            Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_LONG).show();
        }
        return uri;
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
    private void setLikes(final MyHolder holder,final String pid) {
        liekeref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(pid).hasChild(myuid)){
                    holder.likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    holder.likebtn.setText("Liked");
                }
                else {
                    holder.likebtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like,0,0,0);
                    holder.likebtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void showMoreOptions(ImageButton more, String uid, String myuid,final String pid,final String image) {

        PopupMenu popupMenu=new PopupMenu(context,more, Gravity.END);
        if(uid.equals(myuid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"DELETE");
            popupMenu.getMenu().add(Menu.NONE,1,0,"EDIT");
        }
        popupMenu.getMenu().add(Menu.NONE,2,0,"View Detail");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==0){
                    beginDelete(pid,image);
                }
                else if(item.getItemId()==1){
                    Intent intent=new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editpost");
                    intent.putExtra("editpostId",pid);
                    context.startActivity(intent);
                }
                else if(item.getItemId()==2){
                    Intent intent=new Intent(context, PostDetailsActivity.class);
                    intent.putExtra("pid",pid);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pid, String image) {

        if(image.equals("noImage")){
            deleteWithoutImage(pid);
            deletelike(pid);
        }
        else {
            deltewithImage(pid,image);
            deletelike(pid);
        }
    }

    private void deletelike(String pid) {
        Query query= FirebaseDatabase.getInstance().getReference("Likes").child(pid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    dataSnapshot.getRef().removeValue();
                Toast.makeText(context,"Deleted Sucessfully",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deltewithImage(final String pid, String image) {
        final ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting");
        StorageReference picref= FirebaseStorage.getInstance().getReferenceFromUrl(image);
        picref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Query query= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("ptime").equalTo(pid);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                          dataSnapshot1.getRef().removeValue();
                      }

                      pd.dismiss();
                      Toast.makeText(context,"Deleted Sucessfully",Toast.LENGTH_LONG).show();
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


    private void deleteWithoutImage(String pid) {
        final ProgressDialog pd=new ProgressDialog(context);
        pd.setMessage("Deleting");
        Query query= FirebaseDatabase.getInstance().getReference("Posts").orderByChild("ptime").equalTo(pid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    dataSnapshot1.getRef().removeValue();
                }
                pd.dismiss();
                Toast.makeText(context,"Deleted Sucessfully",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return modelPosts.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView picture,image;
        TextView name,time,title,description,like,comments;
        ImageButton more;
        Button likebtn,comment,share;
        LinearLayout profile;
        public MyHolder(@NonNull View itemView) {
            super(itemView);



            picture=itemView.findViewById(R.id.picturetv);
            image=itemView.findViewById(R.id.pimagetv);
            name=itemView.findViewById(R.id.unametv);
            time=itemView.findViewById(R.id.utimetv);
            more=itemView.findViewById(R.id.morebtn);
            title=itemView.findViewById(R.id.ptitletv);
            description=itemView.findViewById(R.id.descript);
            like=itemView.findViewById(R.id.plikeb);
            comments=itemView.findViewById(R.id.pcommentco);
            likebtn=itemView.findViewById(R.id.like);
            comment=itemView.findViewById(R.id.comment);
            share=itemView.findViewById(R.id.share);
            profile=itemView.findViewById(R.id.profilelayout);
        }
    }
}
