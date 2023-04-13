package com.example.strangers.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.strangers.R;
import com.example.strangers.models.UserModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {


    public static final int REQUEST_CODE_For_GoogleSignIn = 11;
    GoogleSignInClient googleSignInClient;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);


        findViewById(R.id.signInLinearLayout_Login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = googleSignInClient.getSignInIntent();

                startActivityForResult(intent, REQUEST_CODE_For_GoogleSignIn);

                // startActivity(new Intent(LoginActivity.this, MainActivity.class));

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_For_GoogleSignIn) {

            if (data != null) {

                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

                GoogleSignInAccount account = task.getResult();
                authWithGoogle(account.getIdToken());
            } else {
                Toast.makeText(this, "Data Is Null", Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void authWithGoogle(String idToken) {

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {


                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    assert user != null;
                    UserModel userModel = new UserModel(user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString(), "Unknown",500);

                    database.getReference()
                            .child("Users")
                            .child(user.getUid())
                            .setValue(userModel)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        startActivity(new Intent(LoginActivity.this,MainActivity.class));
                                        finishAffinity();
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "Task Failed", Toast.LENGTH_SHORT).show();
                                        Log.e("login", "onComplete: "+ task.getException().getLocalizedMessage());
                                    }

                                }
                            });

                    Log.d("login", "onComplete: " + user.getPhotoUrl().toString());

                } else {

                    Log.e("login", "onComplete: " + Objects.requireNonNull(task.getException()).getLocalizedMessage());

                }

            }
        });

    }
}