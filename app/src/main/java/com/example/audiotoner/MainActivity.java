package com.example.audiotoner;

import android.databinding.DataBindingUtil;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.example.audiotoner.databinding.ActivityMainBinding;


import java.util.HashMap;

public class MainActivity extends AppCompatActivity{

    private final String TAG = getClass().getName();

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame,
                MultiSoundGeneratorFragment.newInstance("", "")).commit();
    }




}
