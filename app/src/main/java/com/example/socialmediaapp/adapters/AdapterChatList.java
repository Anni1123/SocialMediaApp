package com.example.socialmediaapp.adapters;

import android.content.Context;
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
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.models.ModelChat;
import com.example.socialmediaapp.models.ModelUsers;
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

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.Myholder> {

    Context context;
    FirebaseAuth firebaseAuth;
    String uid;
    public AdapterChatList(Context context, List<ModelUsers> users) {
        this.context = context;
        this.usersList = users;
       lastMessageMap = new HashMap<>();
        firebaseAuth=FirebaseAuth.getInstance();
        uid=firebaseAuth.getUid();
    }

    List<ModelUsers> usersList;
    private HashMap<String,String> lastMessageMap;
    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_chatlist,parent,false);
        return new Myholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder,final int position) {

        final String hisuid=usersList.get(position).getUid();
        String userimage=usersList.get(position).getImage();
        String username=usersList.get(position).getName();
        String lastmess=lastMessageMap.get(hisuid);
        holder.name.setText(username);
        holder.block.setImageResource(R.drawable.ic_unblock);
        checkisBlocked(hisuid,holder,position);
        if(lastmess==null || lastmess.equals("default")){
            holder.lastmessage.setVisibility(View.GONE);
        }
        else {
            holder.lastmessage.setVisibility(View.VISIBLE);
            holder.lastmessage.setText(lastmess);
        }
        try {
            Picasso.with(context).load(userimage).into(holder.profile);
        }
        catch (Exception e){

        }
        if(usersList.get(position).getOnlineStatus().equals("online")){
            holder.status.setImageResource(R.drawable.online);
        }
        else {
            holder.status.setImageResource(R.drawable.circle_offline);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBlockedOrNot(hisuid);
            }
        });
        holder.block.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(usersList.get(position).isBlocked()){
                    unBlockUser(hisuid);
                }
                else {
                    blockUser(hisuid);
                }
            }
        });
    }

    private void checkisBlocked(String hisuid, final Myholder holder, final int position) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("BlockUsers").orderByChild("uid").equalTo(hisuid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            if(dataSnapshot1.exists()) {

                                holder.block.setImageResource(R.drawable.ic_block);
                                usersList.get(position).setBlocked(true);
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
    public void setlastMessageMap(String userId,String lastmessage){
        lastMessageMap.put(userId,lastmessage);
    }
    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class Myholder extends RecyclerView.ViewHolder{
        ImageView profile,status,block,seen;
        TextView name,lastmessage;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            profile=itemView.findViewById(R.id.profileimage);
            status=itemView.findViewById(R.id.onlinestatus);
            name=itemView.findViewById(R.id.nameonline);
            lastmessage=itemView.findViewById(R.id.lastmessge);
            block=itemView.findViewById(R.id.blocking);
            seen=itemView.findViewById(R.id.seen);
        }
    }
}
