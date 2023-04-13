package com.example.strangers.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.strangers.R;
import com.example.strangers.databinding.ActivityCallBinding;
import com.example.strangers.models.InterfaceJava;
import com.example.strangers.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

import kotlin.math.UMathKt;

public class CallActivity extends AppCompatActivity {

    ActivityCallBinding binding;

    private String uniqueId;
    private FirebaseAuth firebaseAuth;


    String userName = "";
    String friendUserName = "";

    boolean isPeerConnected = false;

    DatabaseReference firebaseRef;

    boolean isAudio = true;
    boolean isVideo = true;
    String createdBy;

    boolean pageExit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child("Rooms");


        userName = firebaseAuth.getUid();
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");

        friendUserName = incoming;

        setupWebView();

        binding.micButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isAudio = !isAudio;

                callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")");

                if(isAudio){
                    binding.micButtonCall.setImageResource(R.drawable.btn_unmute_normal);
                }
                else{
                    binding.micButtonCall.setImageResource(R.drawable.btn_mute_normal);
                }

            }
        });



        binding.videoButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                isVideo = !isVideo;

                callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\")");

                if(isVideo){
                    binding.videoButtonCall.setImageResource(R.drawable.btn_video_normal);
                }
                else{
                    binding.videoButtonCall.setImageResource(R.drawable.btn_video_muted);
                }

            }
        });

        binding.endButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    binding.webView.destroy();

                Toast.makeText(CallActivity.this, "Call Ended!", Toast.LENGTH_SHORT).show();
               finishAffinity();
               startActivity(new Intent(CallActivity.this,MainActivity.class));


            }
        });

    }

    private void setupWebView() {

        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new InterfaceJava(this),"Android");

        loadVideoCall();

    }

    public void loadVideoCall() {

        String filePath = "file:android_asset/call.html";
        binding.webView.loadUrl(filePath);


        binding.webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();

            }
        });

    }

    public void initializePeer() {
        uniqueId = getUniqueId();

        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        Log.d("call", "initializePeer: inside peer method ");

        if(createdBy.equalsIgnoreCase(userName)){


            Log.d("call", "initializePeer: inside peer method 2");

            firebaseRef.child(userName).child("connId").setValue(uniqueId);
            firebaseRef.child(userName).child("isAvailable").setValue(true);

            binding.loadingAnimationGroup.setVisibility(View.GONE);
            binding.controlsLinearLayoutCall.setVisibility(View.VISIBLE);

            // Getting User data
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .child(friendUserName)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(pageExit)
                                return;

                            UserModel userModel = snapshot.getValue(UserModel.class);

                            Glide.with(CallActivity.this).load(userModel.getProfile()).into(binding.profileImageCall);

                            binding.nameTextViewCall.setText(userModel.getName());
                            binding.cityTextViewCall.setText(userModel.getCity());

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        }
        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    friendUserName = createdBy;
                    Log.d("call", "initializePeer: inside peer method, on else part 1");

                    binding.loadingAnimationGroup.setVisibility(View.GONE);
                    binding.controlsLinearLayoutCall.setVisibility(View.VISIBLE);

                    FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .child(friendUserName)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if(pageExit)
                                        return;

                                    UserModel userModel = snapshot.getValue(UserModel.class);

                                    Glide.with(CallActivity.this).load(userModel.getProfile()).into(binding.profileImageCall);

                                    binding.nameTextViewCall.setText(userModel.getName());
                                    binding.cityTextViewCall.setText(userModel.getCity());

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                    FirebaseDatabase.getInstance().getReference()
                            .child("Rooms")
                            .child(friendUserName)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if(snapshot.getValue() != null){
                                        Log.d("call", "initializePeer: inside peer method, on else part 2 and snapshot has data");
                                        sendCallRequest();
                                    }
                                    Log.d("call", "initializePeer: inside peer method, on else part 2 and snapshot has no data");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }
            },3000);
        }

    }

    public void onPeerConnected(){
        isPeerConnected = true;
    }

    void sendCallRequest(){

        if(!isPeerConnected){
            Toast.makeText(this, "You are not Connected, Please Check Your Internet", Toast.LENGTH_SHORT).show();
            return;
        }

        listenConnId();

    }

    void listenConnId(){

        Log.d("call", "listenConnId: outside the ListenConnId" );
            firebaseRef.child(friendUserName)
                    .child("connId")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.getValue() == null){
                                return;
                            }

                            Log.d("call", "listenConnId: Inside the ListenConnId and Its not null and working");

                            binding.loadingAnimationGroup.setVisibility(View.GONE);
                            binding.controlsLinearLayoutCall.setVisibility(View.VISIBLE);


                            String connId = snapshot.getValue(String.class);
                            callJavaScriptFunction("javascript:startCall(\""+connId+"\")");

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

    }

    private void callJavaScriptFunction(String function) {

        binding.webView.post(new Runnable() {
            @Override
            public void run() {

                binding.webView.evaluateJavascript(function, null);



            }
        });

    }

    String getUniqueId(){
        return UUID.randomUUID().toString();
    }

    @Override
    public void onBackPressed() {

        if(binding.loadingAnimationGroup.getVisibility() == View.GONE){
            finish();
        }
        super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (binding.webView != null) {
            binding.webView.destroy();
        }

        pageExit = true;
        firebaseRef.child(createdBy).setValue(null);
        finish();

    }
}