package com.example.socialmediaapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.AddPostActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.ThereProfileActivity;
import com.example.socialmediaapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{


    Context context;
    String myuid;

    public AdapterPosts(Context context, List<ModelPost> modelPosts) {
        this.context = context;
        this.modelPosts = modelPosts;
        myuid= FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    List<ModelPost> modelPosts;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_posts,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        final String uid=modelPosts.get(position).getUid();
        String nameh=modelPosts.get(position).getUname();
        String titlee=modelPosts.get(position).getTitle();
        String descri=modelPosts.get(position).getDescription();
        final String time=modelPosts.get(position).getPtime();
        String dp=modelPosts.get(position).getUdp();
        final String pid=modelPosts.get(position).getPid();
        final String image=modelPosts.get(position).getUimage();
        String email=modelPosts.get(position).getUemail();
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(time));
        String timedate= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
        holder.name.setText(nameh);
        holder.title.setText(titlee);
        holder.description.setText(descri);
        holder.time.setText(timedate);
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
                showMoreOptions(holder.more,uid, myuid,time,image);
            }
        });
        holder.likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Like",Toast.LENGTH_LONG).show();
            }
        });
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Comment",Toast.LENGTH_LONG).show();
            }
        });
        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Share",Toast.LENGTH_LONG).show();
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
    }

    private void showMoreOptions(ImageButton more, String uid, String myuid,final String pid,final String image) {

        PopupMenu popupMenu=new PopupMenu(context,more, Gravity.END);
        if(uid.equals(myuid)){
            popupMenu.getMenu().add(Menu.NONE,0,0,"DELETE");
            popupMenu.getMenu().add(Menu.NONE,1,0,"EDIT");
        }
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
                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete(String pid, String image) {

        if(image.equals("noImage")){
            deleteWithoutImage(pid);
        }
        else {
            deltewithImage(pid,image);
        }
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
        TextView name,time,title,description,like;
        ImageButton more;
        Button likes,comment,share;
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
            likes=itemView.findViewById(R.id.like);
            comment=itemView.findViewById(R.id.comment);
            share=itemView.findViewById(R.id.share);
            profile=itemView.findViewById(R.id.profilelayout);
        }
    }
}
