package com.example.strangers.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.strangers.databinding.ActivityRewardBinding;

public class RewardActivity extends AppCompatActivity {

    ActivityRewardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRewardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}