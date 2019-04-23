package com.example.audiotoner;

import android.databinding.DataBindingUtil;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.audiotoner.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private  int duration = 3; // seconds
    private  int sampleRate = 8000;
    private  int numSamples;
    private  double sample[] ;
    private  double freqOfTone = 100; // hz
    private float volume = 1;

    private  byte generatedSnd[];

    Handler handler = new Handler();

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use a new tread as this can take a while

                try {
                    duration = Integer.parseInt(binding.duration.getText().toString());
                    //sampleRate = Integer.parseInt(binding.samplerate.getText().toString());
                    freqOfTone = Double.parseDouble(binding.freq.getText().toString());
                }catch (Exception e){
                    return;
                }
                if(duration == 0 || sampleRate ==0 || freqOfTone == 0){
                    Toast.makeText(MainActivity.this, "Invalid entry", Toast.LENGTH_SHORT).show();
                    return;
                }

                numSamples = duration * sampleRate;
                sample = new double[numSamples];
                generatedSnd = new byte[2 * numSamples];
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        genTone();
                        handler.post(new Runnable() {

                            public void run() {
                                playSound();
                            }
                        });
                    }
                });
                thread.start();
            }
        });

        binding.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = ((float)progress)/100;
                Log.d(getClass().getName(), "onProgressChanged: "+volume+" "+progress);
                if (audioTrack != null)
                    audioTrack.setVolume(volume);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    void genTone(){
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (double dVal : sample) {
            short val = (short) (dVal * 32767);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

    }

    AudioTrack audioTrack;
    void playSound(){
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, numSamples,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, numSamples);
        audioTrack.setVolume(volume);
        audioTrack.play();

    }
}
