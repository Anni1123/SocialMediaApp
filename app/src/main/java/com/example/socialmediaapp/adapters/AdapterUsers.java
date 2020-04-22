package com.example.socialmediaapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.ChatActivity;
import com.example.socialmediaapp.ThereProfileActivity;
import com.example.socialmediaapp.models.ModelUsers;
import com.example.socialmediaapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;
    FirebaseAuth firebaseAuth;
    String uid;

    public AdapterUsers(Context context, List<ModelUsers> list) {
        this.context = context;
        this.list = list;
        firebaseAuth=FirebaseAuth.getInstance();
        uid=firebaseAuth.getUid();
    }

    List<ModelUsers> list;
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users,parent,false);
       return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        final String hisuid=list.get(position).getUid();
        String userImage=list.get(position).getImage();
        String username=list.get(position).getName();
        String usermail=list.get(position).getEmail();
        holder.name.setText(username);
        holder.email.setText(usermail);
        try {
            Picasso.with(context).load(userImage).into(holder.profiletv);
        }
        catch (Exception e){
        }
        holder.block.setImageResource(R.drawable.ic_unblock);
        checkisBlocked(hisuid,holder,position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which==0){
                            Intent intent=new Intent(context, ThereProfileActivity.class);
                            intent.putExtra("uid",hisuid);
                            context.startActivity(intent);
                        }
                        if(which==1){
                            isBlockedOrNot(hisuid);
                        }
                    }
                });
                builder.create().show();
            }
        });
        holder.block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(list.get(position).isBlocked()){
                    unBlockUser(hisuid);
                }
                else {
                    blockUser(hisuid);
                }
            }
        });
    }

    private void checkisBlocked(String hisuid, final MyHolder holder, final int position) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("BlockUsers").orderByChild("uid").equalTo(hisuid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            if(dataSnapshot1.exists()) {

                              holder.block.setImageResource(R.drawable.ic_block);
                              list.get(position).setBlocked(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void blockUser(String hisuid) {
        HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("uid",hisuid);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("BlockUsers").child(hisuid).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"Blocked Users",Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,"Failed",Toast.LENGTH_LONG).show();
            }
        });
    }

    private void unBlockUser(String hisuid) {

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("BlockUsers").orderByChild("uid").equalTo(hisuid)
              .addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                          if(dataSnapshot1.exists()){
                              dataSnapshot1.getRef().removeValue()
                                      .addOnSuccessListener(new OnSuccessListener<Void>() {
                                          @Override
                                          public void onSuccess(Void aVoid) {
                                              Toast.makeText(context,"UnBlocked Users",Toast.LENGTH_LONG).show();
                                          }
                                      }).addOnFailureListener(new OnFailureListener() {
                                  @Override
                                  public void onFailure(@NonNull Exception e) {
                                      Toast.makeText(context,"Failed",Toast.LENGTH_LONG).show();
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
    private void isBlockedOrNot(final String hisUid){
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(uid).child("BlockUsers").orderByChild("uid").equalTo(hisUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            if(dataSnapshot1.exists()) {
                                Toast.makeText(context,"You Can't message ..You are blocked by this User",Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        Intent intent=new Intent(context, ChatActivity.class);
                        intent.putExtra("uid",hisUid);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        CircleImageView profiletv;
        TextView name,email;
        ImageView block;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv=itemView.findViewById(R.id.imagep);
            name=itemView.findViewById(R.id.namep);
            email=itemView.findViewById(R.id.emailp);
            block=itemView.findViewById(R.id.blocked);
        }
    }
}
