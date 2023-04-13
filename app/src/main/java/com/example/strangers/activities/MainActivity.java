package com.example.strangers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.strangers.R;
import com.example.strangers.databinding.ActivityMainBinding;
import com.example.strangers.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    FirebaseUser currentUser;

    long coins = 0;
    private int REQUEST_CODE = 1;



    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();




        findViewById(R.id.findButton_Main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isPermissionsGranted()) {
                    if (coins >= 5) {
                        coins = coins - 5;
                        database.getReference()
                                .child("Users")
                                .child(firebaseAuth.getUid())
                                .child("coins")
                                .setValue(coins);

                        Toast.makeText(MainActivity.this, "Finding Stranger...", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, ConnectingActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient Balance", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    askPermissions();
                }

//                String coinstring = totalCointTextView.getText().toString();
//                int coin = 0;
//                for(int i=0;i<coinstring.length();i++){

//                    if(Character.isDigit(coinstring.charAt(i))) {
//
//                        Log.d("main", "coint: " + coin);
//                        Log.d("main", "String: " + coinstring.substring(i, coinstring.length()));
//                        coin = Integer.parseInt(coinstring.substring(i, coinstring.length()));
//
//                        break;
//                    }}
//
//
//
//                        coin -= 5;
//
//                        Toast.makeText(MainActivity.this, "5 Coin Used", Toast.LENGTH_SHORT).show();
//                        totalCointTextView.setText();
//
//

            }


        });

    }

    private boolean isPermissionsGranted() {

        for (String permission : permissions) {

            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        }

        return true;
    }

    private void askPermissions() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();

        database.getReference()
                .child("Users")
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        UserModel userModel = snapshot.getValue(UserModel.class);

                        Glide.with(MainActivity.this)
                                .load(userModel.getProfile())
                                .into(binding.profileImageMain);

                        coins = userModel.getCoins();
                        binding.totalCoinsTextViewMain.setText("You have: " + coins);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}