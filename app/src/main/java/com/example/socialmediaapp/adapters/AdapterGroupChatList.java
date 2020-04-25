package com.example.socialmediaapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.GroupChatActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.models.ModelGroupChatList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.MyHolder> {

    Context context;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> modelGroupChats) {
        this.context = context;
        this.modelGroup = modelGroupChats;
    }

    ArrayList<ModelGroupChatList> modelGroup;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.new_groupchat_list,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelGroupChatList modelGroupChat=modelGroup.get(position);
        final String groupId=modelGroupChat.getGrpId();
        String groupIcon=modelGroupChat.getGrpicon();
        String grpdescription=modelGroupChat.getGrpdesc();
        String grptitle=modelGroupChat.getGrptitle();
        holder.title.setText(grptitle);
        holder.sendername.setText("");
        holder.time.setText("");
        holder.msg.setText("");
        loadlastmessage(modelGroupChat,holder);
        try {
            Picasso.with(context).load(groupIcon).into(holder.icon);
        }
        catch (Exception e){

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupid",groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadlastmessage(ModelGroupChatList modelGroupChat, final MyHolder holder) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Groups");
        reference.child(modelGroupChat.getGrpId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         for (DataSnapshot ds:dataSnapshot.getChildren()){
                             String message=""+ds.child("message").getValue();
                             String sender=""+ds.child("sender").getValue();
                             String timestamp=""+ds.child("timestamp").getValue();
                             String type=""+ds.child("type").getValue();
                             Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
                             calendar.setTimeInMillis(Long.parseLong(timestamp));
                             String timedate= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
                             if(type.equals("text")){
                                 holder.msg.setText(message);
                             }
                             else{
                                 holder.msg.setText("Sent a Photo");
                             }
                             holder.time.setText(timedate);
                             DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Users");
                             reference1.orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                                 @Override
                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                   for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                                       String name=""+dataSnapshot1.child("name").getValue();
                                       holder.sendername.setText(name);
                                   }
                                 }

                                 @Override
                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                 }
                             });
                         }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return modelGroup.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView icon;
        TextView title,sendername,msg,time;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            icon=itemView.findViewById(R.id.grpicontv);
            title=itemView.findViewById(R.id.grptitletv);
            sendername=itemView.findViewById(R.id.sendername);
            msg=itemView.findViewById(R.id.sendingmsg);
            time=itemView.findViewById(R.id.sendingtime);
        }
    }
}
