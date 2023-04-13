package com.example.strangers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.bumptech.glide.Glide;
import com.example.strangers.R;
import com.example.strangers.databinding.ActivityConnectingBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ConnectingActivity extends AppCompatActivity {

    ActivityConnectingBinding binding;
    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    FirebaseDatabase database;

    boolean isOkay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        currentUser = firebaseAuth.getCurrentUser();

        Glide.with(this)
                .load(currentUser.getPhotoUrl())
                .into(binding.profileImageConnecting);

        database.getReference()
                .child("Rooms")
                .orderByChild("status")
                .equalTo(0)
                .limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        currentUser = firebaseAuth.getCurrentUser();

                        if(snapshot.getChildrenCount() >0){
                            // If Such room exits then join
                            // i.e Room available
                            isOkay = true;
                            for(DataSnapshot childSnap: snapshot.getChildren()){

                                database.getReference()
                                        .child("Rooms")
                                        .child(childSnap.getKey())
                                        .child("status")
                                        .setValue(1);

                                database.getReference()
                                        .child("Rooms")
                                        .child(childSnap.getKey())
                                        .child("incoming")
                                        .setValue(currentUser.getUid());


                                Intent intent = new Intent(ConnectingActivity.this,CallActivity.class);
                                String incoming = childSnap.child("incoming").getValue(String.class);
                                String createdBy = childSnap.child("createdBy").getValue(String.class);
                                Boolean isAvailable = childSnap.child("isAvailable").getValue(Boolean.class);


                                intent.putExtra("incoming",incoming);
                                intent.putExtra("createdBy",createdBy);
                                intent.putExtra("isAvailable",isAvailable);
                                startActivity(intent);
                                finish();
                            }

                        }
                        else{
                            // If there no Such Room, You gotta Create one

                            HashMap<String, Object> room = new HashMap<>();
                            room.put("incoming",currentUser.getUid());
                            room.put("createdBy",currentUser.getUid());
                            room.put("isAvailable",true);
                            room.put("status",0);


                            database.getReference()
                                    .child("Rooms")
                                    .child(currentUser.getUid())
                                    .setValue(room)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            database.getReference()
                                                    .child("Rooms")
                                                    .child(currentUser.getUid())
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                            if(snapshot.child("status").exists()){

                                                                if(snapshot.child("status").getValue(Integer.class) == 1){

                                                                    if(isOkay)
                                                                        return;
                                                                    isOkay = true;

                                                                    Intent intent = new Intent(ConnectingActivity.this,CallActivity.class);
                                                                    String incoming = snapshot.child("incoming").getValue(String.class);
                                                                    String createdBy = snapshot.child("createdBy").getValue(String.class);
                                                                    Boolean isAvailable = snapshot.child("isAvailable").getValue(Boolean.class);


                                                                    intent.putExtra("incoming",incoming);
                                                                    intent.putExtra("createdBy",createdBy);
                                                                    intent.putExtra("isAvailable",isAvailable);
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });

                                        }
                                    });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }
}