package com.example.socialmediaapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.ChatActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.models.ModelUsers;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdapterChatList extends RecyclerView.Adapter<AdapterChatList.Myholder> {

    Context context;

    public AdapterChatList(Context context, List<ModelUsers> users) {
        this.context = context;
        this.usersList = users;
       lastMessageMap = new HashMap<>();
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
    public void onBindViewHolder(@NonNull Myholder holder, int position) {

        final String hisuid=usersList.get(position).getUid();
        String userimage=usersList.get(position).getImage();
        String username=usersList.get(position).getName();
        String lastmess=lastMessageMap.get(hisuid);
        holder.name.setText(username);
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
            holder.status.setImageResource(R.drawable.circle_online);
        }
        else {
            holder.status.setImageResource(R.drawable.circle_offline);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ChatActivity.class);
              intent.putExtra("uid",hisuid);
                context.startActivity(intent);
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
        ImageView profile,status;
        TextView name,lastmessage;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            profile=itemView.findViewById(R.id.profileimage);
            status=itemView.findViewById(R.id.onlinestatus);
            name=itemView.findViewById(R.id.nameonline);
            lastmessage=itemView.findViewById(R.id.lastmessge);

        }
    }
}
