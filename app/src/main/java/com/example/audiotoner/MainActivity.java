package com.example.audiotoner;

import android.databinding.DataBindingUtil;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.example.audiotoner.databinding.ActivityMainBinding;


import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements RvAddRowChannelAdapter.OnClickListener {

    private final String TAG = getClass().getName();
    private int sampleRate = 44100;
    private int numSamples = 300 * sampleRate;
    private double sample[] = new double[numSamples];

    private HashMap<Integer, AudioTrack> audioTrackList;
    private LinearLayoutManager linearLayoutManager;
    private RvAddRowChannelAdapter adapter;
    private HashMap<Integer, Thread> threadMap = new HashMap<>();


    Handler handler = new Handler();

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioTrackList = new HashMap<>();
        Log.d(TAG, "onCreate: " + numSamples);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        adapter = new RvAddRowChannelAdapter(this, this);
        linearLayoutManager = new LinearLayoutManager(this);
        binding.rv.setAdapter(adapter);
        binding.rv.setLayoutManager(linearLayoutManager);

        binding.addChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.addNewRow();
            }
        });

    }

    byte[] genTone(double freqOfTone) {
        byte generatedSnd[] = new byte[2 * numSamples];
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (double dVal : sample) {
            short val = (short) (dVal * 32767);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        return generatedSnd;
    }

    private AudioTrack playSound(byte[] generatedSnd, int volume) {
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                numSamples,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, sample.length);
        audioTrack.setVolume((float) volume / 100);

        return audioTrack;
    }

    @Override
    public void onClickPlay(double freq, int volume, boolean isPlaying, final int position) {

        if (isPlaying) {
            //Stop
            stopAudioTrack(position);

        } else {
            byte[] tone = genTone(freq);
            final AudioTrack audioTrack = playSound(tone, volume);
            audioTrackList.put(position, audioTrack);

            threadInterruptedMap.put(position, false);
            final Thread thread = new Thread(new Runnable() {

                public void run() {
                    for (int i=0;i<10;i++){

                        if (threadInterruptedMap.get(position)){
                            Log.d(TAG, "run: thread is interrupted");
                            audioTrack.stop();
                            audioTrack.release();
                            return;
                        }

                        if (audioTrack != null) {
                            audioTrack.play();
                            Log.d(TAG, "doInBackground: " + i);
                            try {
                                Thread.sleep(60_000);
                                audioTrack.stop();
                            } catch (Exception e) {

                            }
                        }

                    }
                }
            });

            threadMap.put(position, thread);
            threadMap.get(position).start();
        }
    }

    HashMap<Integer, Boolean> threadInterruptedMap = new HashMap<>();

    private void stopAudioTrack(int position) {
        threadMap.get(position).interrupt();
        threadInterruptedMap.put(position, true);

        AudioTrack audioTrack = audioTrackList.get(position);
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "onClick: ");
        }
    }

    @Override
    public void onClickDelete(boolean isPlaying, int position) {
        if (isPlaying && audioTrackList.get(position) != null) {
            stopAudioTrack(position);
        }
        if (audioTrackList.get(position) != null) {
            audioTrackList.remove(position);
        } else {
            Log.d(TAG, "onClickDelete: AudioTrack not found, unable to perform delete operation");
        }
    }


}
