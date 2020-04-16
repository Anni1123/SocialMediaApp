package com.example.socialmediaapp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.R;
import com.example.socialmediaapp.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.Myholder>{
    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPR_RIGHT=1;
    Context context;
    List<ModelChat> list;
    String imageurl;
    FirebaseUser firebaseUser;

    public AdapterChat(Context context, List<ModelChat> list, String imageurl) {
        this.context = context;
        this.list = list;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public Myholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_LEFT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new Myholder(view);
        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new Myholder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull Myholder holder, int position) {

        String message=list.get(position).getMessage();
        String timeStamp=list.get(position).getTimestamp();
        Calendar calendar=Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String timedate= DateFormat.format("dd/MM/YYYY hh:mm aa",calendar).toString();
        holder.message.setText(message);
        holder.time.setText(timedate);
        Picasso.with(context).load(imageurl).into(holder.image);

        if(position==list.size()-1){
            if (list.get(position).isDilihat()){
                holder.isSee.setText("Seen");
            }
            else {
                holder.isSee.setText("Delivered");
            }
        }
        else {
            holder.isSee.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        if(list.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPR_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    class Myholder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView message,time,isSee;
        public Myholder(@NonNull View itemView) {
            super(itemView);
            image=itemView.findViewById(R.id.profilec);
            message=itemView.findViewById(R.id.msgc);
            time=itemView.findViewById(R.id.timetv);
            isSee=itemView.findViewById(R.id.isSeen);
        }
    }
}
