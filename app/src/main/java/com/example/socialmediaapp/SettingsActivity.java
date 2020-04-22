package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    SwitchCompat switchCompat;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String Topic_Post_Notification="POST";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        switchCompat=findViewById(R.id.postswitch);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        sharedPreferences=getSharedPreferences("NOTIFICATIO_SP",MODE_PRIVATE);
        boolean ispostEnabled=sharedPreferences.getBoolean(""+Topic_Post_Notification,false);
        if(ispostEnabled){
            switchCompat.setChecked(true);
        }
        else {
            switchCompat.setChecked(false);
        }
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor=sharedPreferences.edit();
                editor.putBoolean(""+Topic_Post_Notification,isChecked);
                editor.apply();
                if(isChecked){
                    subscribepostNotifiaction();
                }
                else {
                    unsubscribepostNotifiaction();
                }
            }
        });
    }

    private void subscribepostNotifiaction() {
        FirebaseMessaging.getInstance().subscribeToTopic(""+Topic_Post_Notification)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg="You Will Receive Post Notifications";
                        if(!task.isSuccessful()){
                            msg="Subscription Failed";
                        }
                        Toast.makeText(SettingsActivity.this,msg,Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void unsubscribepostNotifiaction() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(""+Topic_Post_Notification)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg="You Will Not Receive Post Notifications";
                        if(!task.isSuccessful()){
                            msg="UnSubscription Failed";
                        }
                        Toast.makeText(SettingsActivity.this,msg,Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
