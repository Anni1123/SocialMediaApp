package com.example.socialmediaapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.PostDetailsActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.models.ModelNotifications;
import com.example.socialmediaapp.notifications.Data;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.MyHolder> {

    Context context;
    FirebaseAuth firebaseAuth;
    public AdapterNotification(Context context, ArrayList<ModelNotifications> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        firebaseAuth=FirebaseAuth.getInstance();
    }

    ArrayList<ModelNotifications> arrayList;

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_notifications,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {

        final ModelNotifications modelNotifications=arrayList.get(position);
        String name=modelNotifications.getSname();
        String notification=modelNotifications.getNotification();
        final String images=modelNotifications.getSimage();
        String senderId=modelNotifications.getSuid();
        final String pid=modelNotifications.getPid();
        final String timestamp=modelNotifications.getTimestamp();
        Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String timedate= DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                            String name=dataSnapshot1.child("name").getValue().toString();
                            String image=dataSnapshot1.child("image").getValue().toString();
                            String email=dataSnapshot1.child("email").getValue().toString();
                            modelNotifications.setSname(name);
                            modelNotifications.setSimage(image);
                            modelNotifications.setSemail(email);
                            holder.name.setText(name);
                            try {
                                Picasso.with(context).load(images).into(holder.pimage);
                            }
                            catch (Exception e){

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        holder.text.setText(notification);
        holder.time.setText(timedate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PostDetailsActivity.class);
                intent.putExtra("pid",pid);
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you Sure to delete this Notifications?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                        reference.child(firebaseAuth.getUid()).child("Notifications").child(timestamp).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context,"Deleted Notifications",Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context,"Failed to delete",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{
        ImageView pimage;
        TextView name,text,time;
        public MyHolder(@NonNull View itemView) {
            super(itemView);
            pimage=itemView.findViewById(R.id.notifyimage);
            name=itemView.findViewById(R.id.notifyname);
            text=itemView.findViewById(R.id.notifytext);
            time=itemView.findViewById(R.id.notifytime);
        }
    }
}
