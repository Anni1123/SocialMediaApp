package com.example.socialmediaapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.ThereProfileActivity;
import com.example.socialmediaapp.models.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder>{


    Context context;

    public AdapterPosts(Context context, List<ModelPost> modelPosts) {
        this.context = context;
        this.modelPosts = modelPosts;
    }

    List<ModelPost> modelPosts;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_posts,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final String uid=modelPosts.get(position).getUid();
        String nameh=modelPosts.get(position).getUname();
        String titlee=modelPosts.get(position).getTitle();
        String descri=modelPosts.get(position).getDescription();
        String time=modelPosts.get(position).getPtime();
        String dp=modelPosts.get(position).getUdp();
        String image=modelPosts.get(position).getUimage();
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
            try {
                Picasso.with(context).load(image).into(holder.image);
            }
            catch (Exception e){

            }
        }
        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"More",Toast.LENGTH_LONG).show();
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
