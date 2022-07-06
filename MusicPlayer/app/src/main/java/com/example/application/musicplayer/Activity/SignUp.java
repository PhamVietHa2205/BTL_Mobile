package com.example.application.musicplayer.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application.musicplayer.R;
import com.example.application.musicplayer.User.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignUp extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        TextView return_login = (TextView) findViewById(R.id.return_login);
        TextView signup_btn = (TextView) findViewById(R.id.signup_btn);
        TextView email =(TextView) findViewById(R.id.email);
        TextView username =(TextView) findViewById(R.id.username);
        TextView password =(TextView) findViewById(R.id.password);
        TextView confirm_password = (TextView) findViewById(R.id.confirm_password);
        return_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, LogIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("ABC", password.getText().toString() + " " + confirm_password.getText().toString());
                if (TextUtils.isEmpty(username.getText())) {
                    username.setError("USERNAME IS EMPTY");
                    username.requestFocus();
                } else
                if (TextUtils.isEmpty(password.getText())) {
                    password.setError("PASSWORD IS EMPTY");
                    password.requestFocus();
                } else if (!password.getText().toString().equals(confirm_password.getText().toString())) {
                    confirm_password.setError("PASS CONFIRM IS NOT THE SAME!");
                    confirm_password.requestFocus();
                } else
                if (!isValidEmail(email.getText().toString())) {
                    email.setError("INVALID EMAIL, PLEASE RETRY!");
                    email.requestFocus();
                } else {
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Log.v("ABC", email.getText().toString());
                                        UserData userData = new UserData(email.getText().toString(), username.getText().toString(), password.getText().toString(), false);
                                        FirebaseDatabase.getInstance().getReference("user").
                                                child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                                       @Override
                                                                                       public void onComplete(@NonNull Task<AuthResult> task) {
                                                                                           if (task.isSuccessful()) {
                                                                                               FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                                                               user.sendEmailVerification();
                                                                                               FirebaseAuth.getInstance().signOut();
                                                                                           }
                                                                                       }
                                                                                   });
                                                    Toast.makeText(SignUp.this, "SIGN UP SUCCESS, PLEASE RETURN TO LOGIN!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(SignUp.this, "FAIL TO REGISTER, PLEASE RETRY!!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(SignUp.this, "ACCOUNT EXISTED!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
    });
    }
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
