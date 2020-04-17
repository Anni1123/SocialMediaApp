package com.example.socialmediaapp.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.socialmediaapp.ChatActivity;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
        String savecurrentuser=sp.getString("CURRENT_USERID", "None");
        String sent=remoteMessage.getData().get("sent");
        String user=remoteMessage.getData().get("user");
        FirebaseUser user1= FirebaseAuth.getInstance().getCurrentUser();
        if(user1!=null&&sent.equals(user1.getUid())){
            if(!savecurrentuser.equals(user)){
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

                    sendAboveNotificationd(remoteMessage);
                }
                else {
                    sendNormalNotification(remoteMessage);
                }
            }
        }
    }
    private void sendAboveNotificationd(RemoteMessage remoteMessage) {
        String user=remoteMessage.getData().get("user");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");
        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int i=Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, ChatActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("uid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri sounduri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
       OreoAndAboveNotification oreoAndAboveNotification=new OreoAndAboveNotification(this);
       Notification.Builder builder=oreoAndAboveNotification.getOnNotification(title,body,pendingIntent,sounduri,icon);

        int j=0;
        if(i>0){
            j=1;
        }
       oreoAndAboveNotification.getNotificationManager().notify(j,builder.build());

    }
    private void sendNormalNotification(RemoteMessage remoteMessage) {

        String user=remoteMessage.getData().get("user");
        String icon=remoteMessage.getData().get("icon");
        String title=remoteMessage.getData().get("title");
        String body=remoteMessage.getData().get("body");
        RemoteMessage.Notification notification=remoteMessage.getNotification();
        int i=Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent=new Intent(this, ChatActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("uid",user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri sounduri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(sounduri)
                .setAutoCancel(true)
                .setSmallIcon(Integer.parseInt(icon));
        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j=0;
        if(i>0){
            j=1;
        }
        notificationManager.notify(j,builder.build());


    }
}
