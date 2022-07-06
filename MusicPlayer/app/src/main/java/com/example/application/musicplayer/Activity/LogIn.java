package com.example.application.musicplayer.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.application.musicplayer.Model.SongsList;
import com.example.application.musicplayer.R;
import com.example.application.musicplayer.User.UserData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;

public class LogIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(LogIn.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            setContentView(R.layout.log_in);

            TextView email = (TextView) findViewById(R.id.email);
            TextView password = (TextView) findViewById(R.id.password);

            TextView signup = (TextView) findViewById(R.id.signup);
            Button loginbtn = (Button) findViewById(R.id.loginbtn);

            loginbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (TextUtils.isEmpty(email.getText())) {
                        email.setError("USERNAME IS EMPTY");
                        email.requestFocus();
                    } else if (TextUtils.isEmpty(password.getText())) {
                        password.setError("PASSWORD IS EMPTY");
                        password.requestFocus();
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                        email.setError("INVALID EMAIL!");
                        email.requestFocus();
                    } else {
                        ProgressDialog progressDialog = ProgressDialog.show(
                                LogIn.this, null, null, false, false
                        );
                        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        progressDialog.getWindow().setContentView(R.layout.progress_bar);
                        progressDialog.show();
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (progressDialog.isShowing()){
                                            progressDialog.dismiss();
                                        }
                                        if (task.isSuccessful()) {
//                                MainActivity.isLoggedIn = true;
                                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                            if (user.isEmailVerified()) {
                                                Toast.makeText(LogIn.this, "LOGIN SUCCESS!", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(LogIn.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(LogIn.this, "PLEASE CHECK YOUR EMAIL TO VERIFY YOUR ACCOUNT!", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(LogIn.this, "WRONG USER NAME OR PASSWORD, PLEASE RETRY!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                    }
                }
            });

            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(LogIn.this, SignUp.class));
                }
            });
        }
    }
}