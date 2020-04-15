package com.example.socialmediaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {

    Context context;

    public AdapterUsers(Context context, List<ModelUsers> list) {
        this.context = context;
        this.list = list;
    }

    List<ModelUsers> list;
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users,parent,false);
       return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        String userImage=list.get(position).getImage();
        String username=list.get(position).getName();
        String usermail=list.get(position).getEmail();
        holder.name.setText(username);
        holder.email.setText(usermail);
        try {
            Picasso.with(context).load(userImage).into(holder.profiletv);
        }
        catch (Exception e){
            Picasso.with(context).load(userImage).placeholder(R.drawable.profile_image).into(holder.profiletv);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        CircleImageView profiletv;
        TextView name,email;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profiletv=itemView.findViewById(R.id.imagep);
            name=itemView.findViewById(R.id.namep);
            email=itemView.findViewById(R.id.emailp);
        }
    }
}
