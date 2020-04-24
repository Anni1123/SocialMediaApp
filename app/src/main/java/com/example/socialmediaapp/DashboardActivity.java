package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import com.example.socialmediaapp.fragments.ChatListFragment;
import com.example.socialmediaapp.fragments.GroupChatFragment;
import com.example.socialmediaapp.fragments.HomeFragment;
import com.example.socialmediaapp.fragments.NotificationsFragment;
import com.example.socialmediaapp.fragments.ProfileFragment;
import com.example.socialmediaapp.fragments.UsersFragment;
import com.example.socialmediaapp.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity  {

    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String myuid;
    ActionBar actionBar;
    BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        actionBar=getSupportActionBar();
        actionBar.setTitle("Profile Activity");
        firebaseAuth=FirebaseAuth.getInstance();

        navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        actionBar.setTitle("Home");
        HomeFragment fragment=new HomeFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content,fragment,"");
        fragmentTransaction.commit();
        checkUserStatus();

    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1=new Token(token);
        ref.child(myuid).setValue(token1);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.nav_home:
                    actionBar.setTitle("Home");
                    HomeFragment fragment=new HomeFragment();
                    FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.content,fragment,"");
                    fragmentTransaction.commit();

                    return true;
                case R.id.nav_profile:
                    actionBar.setTitle("Profile");
                    ProfileFragment fragment1=new ProfileFragment();
                    FragmentTransaction fragmentTransaction1=getSupportFragmentManager().beginTransaction();
                    fragmentTransaction1.replace(R.id.content,fragment1);
                    fragmentTransaction1.commit();
                    return true;
                case R.id.nav_users:
                    actionBar.setTitle("Users");
                    UsersFragment fragment2=new UsersFragment();
                    FragmentTransaction fragmentTransaction2=getSupportFragmentManager().beginTransaction();
                    fragmentTransaction2.replace(R.id.content,fragment2,"");
                    fragmentTransaction2.commit();
                    return true;
                case R.id.nav_chat:
                    actionBar.setTitle("Chats");
                    ChatListFragment listFragment=new ChatListFragment();
                    FragmentTransaction fragmentTransaction3=getSupportFragmentManager().beginTransaction();
                    fragmentTransaction3.replace(R.id.content,listFragment,"");
                    fragmentTransaction3.commit();
                    return true;
                case R.id.nav_more:
                    showMoreOptions();
            }
            return false;
        }
    };

    private void showMoreOptions() {
        PopupMenu menu=new PopupMenu(this,navigationView, Gravity.END);
        menu.getMenu().add(Menu.NONE,0,0,"Notifications");
        menu.getMenu().add(Menu.NONE,1,0,"Group Chats");
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id=item.getItemId();
                if(id==0){
                    actionBar.setTitle("Chats");
                    NotificationsFragment notificationsFragment=new NotificationsFragment();
                    FragmentTransaction fragmentTransactiona=getSupportFragmentManager().beginTransaction();
                    fragmentTransactiona.replace(R.id.content,notificationsFragment,"");
                    fragmentTransactiona.commit();
                }
                if(id==1){
                    actionBar.setTitle("Chats");
                    GroupChatFragment groupChatFragment=new GroupChatFragment();
                    FragmentTransaction fragmentTransactionb=getSupportFragmentManager().beginTransaction();
                    fragmentTransactionb.replace(R.id.content,groupChatFragment,"");
                    fragmentTransactionb.commit();
                }
                return false;
            }
        });
        menu.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }


    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            myuid=user.getUid();
            SharedPreferences sharedPreferences=getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("CURRENT_USERID",myuid);
            editor.apply();
            updateToken(FirebaseInstanceId.getInstance().getToken());
        }
        else {
            startActivity(new Intent(DashboardActivity.this,MainActivity.class));
            finish();
        }
    }




}
