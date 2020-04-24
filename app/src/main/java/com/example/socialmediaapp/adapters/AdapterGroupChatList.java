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

import com.example.socialmediaapp.GroupChatActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.models.ModelGroupChatList;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
