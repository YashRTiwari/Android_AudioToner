package com.example.audiotoner;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.audiotoner.databinding.FragmentMultiSoundGeneratorBinding;

import java.util.HashMap;


public class MultiSoundGeneratorFragment extends Fragment implements RvAddRowChannelAdapter.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private final String TAG = getClass().getName();
    private int sampleRate = 44100;
    private int numSamples = 100 * sampleRate;
    private double sample[] = new double[numSamples];

    private HashMap<Integer, AudioTrack> audioTrackList;
    private LinearLayoutManager linearLayoutManager;
    private RvAddRowChannelAdapter adapter;
    private HashMap<Integer, Thread> threadMap = new HashMap<>();

    private FragmentMultiSoundGeneratorBinding binding;


    public MultiSoundGeneratorFragment() {
        // Required empty public constructor
    }

    public static MultiSoundGeneratorFragment newInstance(String param1, String param2) {
        MultiSoundGeneratorFragment fragment = new MultiSoundGeneratorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_multi_sound_generator, container, false);
        audioTrackList = new HashMap<>();


        adapter = new RvAddRowChannelAdapter(getContext(), this);
        linearLayoutManager = new LinearLayoutManager(getContext());
        binding.rv.setAdapter(adapter);
        binding.rv.setLayoutManager(linearLayoutManager);

        adapter.addNewRow();

        binding.addChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.addNewRow();
            }
        });
        return binding.getRoot();
    }

    byte[] genSawToothWaveTone(double frequency, int v){
        byte generatedSnd[] = new byte[2 * numSamples];
        // fill out the array
        float vol = (float) v / 100;

        for (int i=0; i<numSamples; ++i){
            sample[i]= vol*(2*(i%(sampleRate/frequency))/(sampleRate/frequency)-1);
        }
        int idx = 0;
        for (double dVal : sample) {

            short val = (short) (dVal * 32767);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        return generatedSnd;
    }

    byte[] genSquareWaveTone(double freqOfTone, int v) {
        byte generatedSnd[] = new byte[2 * numSamples];
        // fill out the array
        float vol = (float) v / 100;
        for (int i = 0; i < numSamples; ++i) {

            double angle1 = i / (sampleRate / freqOfTone) * 1.0 * 2.0 * Math.PI;
            double angle2 = i / (sampleRate / freqOfTone) * 3.0 * 2.0 * Math.PI;
            double angle3 = i / (sampleRate / freqOfTone) * 5.0 * 2.0 * Math.PI;
            double angle4 = i / (sampleRate / freqOfTone) * 7.0 * 2.0 * Math.PI;
            double angle5 = i / (sampleRate / freqOfTone) * 9.0 * 2.0 * Math.PI;
            double angle6 = i / (sampleRate / freqOfTone) * 11.0 * 2.0 * Math.PI;
            double angle7 = i / (sampleRate / freqOfTone) * 13.0 * 2.0 * Math.PI;
            double angle8 = i / (sampleRate / freqOfTone) * 15.0 * 2.0 * Math.PI;
            sample[i] = vol * (Math.sin(angle1) +
                    Math.sin(angle2) / 3 +
                    Math.sin(angle3) / 5 +
                    Math.sin(angle4) / 7 +
                    Math.sin(angle5) / 9 +
                    Math.sin(angle6) / 11 +
                    Math.sin(angle7) / 13 +
                    Math.sin(angle8) / 15);
        }

        int idx = 0;
        for (double dVal : sample) {

            generatedSnd[idx++] = (byte)((short) ((dVal > 0.0) ? 1 : -1));

            /*short val = (short) (dVal * 32767);
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);*/
        }

        return generatedSnd;
    }

    byte[] genSinewaveTone(double freqOfTone, int vol) {
        byte generatedSnd[] = new byte[2 * numSamples];
        // fill out the array
        float v = (float) vol / 100;
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = (double) v * Math.sin(2 * Math.PI * i / (sampleRate / freqOfTone));
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
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT,
                numSamples,
                AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, sample.length);
        //audioTrack.setVolume((float) volume / 100);

        return audioTrack;
    }

    byte[] tone = null;

    @Override
    public void onClickPlay(double freq, final int volume, boolean isPlaying, final int position, int waveform) {

        if (isPlaying) {
            //Stop
            stopAudioTrack(position);

        } else {
            tone = null;
            if (waveform == RvAddRowChannelAdapter.SINEWAVE) {
                tone = genSinewaveTone(freq, volume);
            } else if (waveform == RvAddRowChannelAdapter.SQUAREWAVE) {
                tone = genSquareWaveTone(freq, volume);
            } else if (waveform == RvAddRowChannelAdapter.SAWTOOTH_WAVE){
                tone = genSawToothWaveTone(freq, volume);
            }

            final AudioTrack audioTrack = playSound(tone, volume);
            audioTrackList.put(position, audioTrack);
            threadInterruptedMap.put(position, false);
            final Thread thread = new Thread(new Runnable() {

                public void run() {

                    for (int i = 0; i < 100; i++) {

                        if (threadInterruptedMap.get(position)) {
                            Log.d(TAG, "run: thread is interrupted");

                            return;
                        }

                        if (audioTrack != null) {
                            audioTrack.play();
                            Log.d(TAG, "doInBackground: " + i);
                            try {
                                Thread.sleep(20_000);
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
