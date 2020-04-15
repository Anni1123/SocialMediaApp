package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText email,password;
    private Button mlogin;
    private TextView exist;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Create Account");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        email=findViewById(R.id.emailEtlog);
        password=findViewById(R.id.passwordEtlog);
        exist=findViewById(R.id.createhaveaaccount);
        mAuth=FirebaseAuth.getInstance();
        mlogin=findViewById(R.id.loginac);
        loadingBar=new ProgressDialog(this);
        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emaill=email.getText().toString().trim();
                String pass=password.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(emaill).matches()){
                    email.setError("Invalid Email");
                    email.setFocusable(true);

                }
                else {
                    loginUser(emaill,pass);
                }
            }
        });
        exist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));

            }
        });
        loadingBar.setMessage("Logging In....");
    }

    private void loginUser(String emaill, String pass) {
        loadingBar.show();
        mAuth.signInWithEmailAndPassword(emaill, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    loadingBar.dismiss();
                    FirebaseUser user=mAuth.getCurrentUser();
                    Toast.makeText(LoginActivity.this,"Registered User " +user.getEmail(),Toast.LENGTH_LONG).show();
                    Intent mainIntent=new Intent(LoginActivity.this,ProfileActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }
                else {
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this,"Login Failed",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingBar.dismiss();
                Toast.makeText(LoginActivity.this,"Error Occured",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


}
