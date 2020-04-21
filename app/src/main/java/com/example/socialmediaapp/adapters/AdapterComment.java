package com.example.socialmediaapp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.models.ModelComment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterComment extends RecyclerView.Adapter<AdapterComment.MyHolder>{

    Context context;

    public AdapterComment(Context context, List<ModelComment> list) {
        this.context = context;
        this.list = list;
    }

    List<ModelComment> list;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_comments,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String uid=list.get(position).getUid();
        String name=list.get(position).getUname();
        String email=list.get(position).getUemail();
        String image=list.get(position).getUdp();
        String cid=list.get(position).getCid();
        String comment=list.get(position).getComment();
        String timestamp=list.get(position).getPtime();
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String timedate= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        holder.name.setText(name);
        holder.time.setText(timedate);
        holder.comment.setText(comment);
        try {
            Picasso.with(context).load(image).into(holder.imagea);
        }
        catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView imagea;
        TextView name,comment,time;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imagea=itemView.findViewById(R.id.loadcomment);
            name=itemView.findViewById(R.id.commentname);
            comment=itemView.findViewById(R.id.commenttext);
            time=itemView.findViewById(R.id.commenttime);
        }
    }
}
