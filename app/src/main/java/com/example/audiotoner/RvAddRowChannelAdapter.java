package com.example.audiotoner;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.audiotoner.databinding.RvAddChannelRowBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RvAddRowChannelAdapter extends RecyclerView.Adapter<RvAddRowChannelAdapter.ViewHolder> {

    static int x = 0;
    OnClickListener listener;
    Context context;
    HashMap<Integer, Boolean> playMap = new HashMap<>();
    public static int SINEWAVE = 0;
    public static int SQUAREWAVE = 1;
    public static int SAWTOOTH_WAVE = 2;
    private static final String TAG = "RvAddRowChannelAdapter";


    public interface OnClickListener {

        void onClickPlay(double freq, int volume, boolean isPlaying, int position, int wavetype);

        void onClickDelete(boolean isPlaying, int position);

    }


    RvAddRowChannelAdapter(Context context, OnClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        RvAddChannelRowBinding binding = DataBindingUtil.inflate(inflater, R.layout.rv_add_channel_row, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.binding.edtAmplitude.setText("");
        holder.binding.edtFreq.setText("");
        playMap.put(position, false);
        holder.binding.ivPlay.setImageResource(android.R.drawable.ic_media_play);


    }

    @Override
    public int getItemCount() {
        return x;
    }

    public void addNewRow() {
        playMap.put(x, false);
        x++;
        notifyItemInserted(x - 1);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RvAddChannelRowBinding binding;
        private int waveSelected = SINEWAVE;

        public ViewHolder(@NonNull final RvAddChannelRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            waveSelected = SQUAREWAVE;

            binding.ivPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Utils.vibrate(context, 50, 25);
                    action();
                }
            });

            binding.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Utils.vibrate(context, 50, 25);

                    if (RvAddRowChannelAdapter.x == 1) {
                        binding.edtFreq.setText("");
                        binding.edtAmplitude.setText("");
                        return;
                    }

                    Set<Integer> keys = playMap.keySet();
                    ArrayList<Integer> keyAL = new ArrayList<>();
                    for (int x : keys) {
                        keyAL.add(x);
                    }

                    if (getAdapterPosition() <= keyAL.size() && getAdapterPosition() >= 0) {
                        int key = keyAL.get(getAdapterPosition());
                        listener.onClickDelete(playMap.get(key) == null ? false : playMap.get(key), key);
                        playMap.remove(key);
                        x = x - 1;
                        notifyItemRemoved(getAdapterPosition());
                    }


                }
            });

            binding.ivSinewave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Utils.vibrate(context, 50, 25);

                    PopupMenu popup = new PopupMenu(context, binding.ivSinewave);
                    popup.getMenuInflater().inflate(R.menu.popup_select_wave, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()){

                                case R.id.sine:
                                    waveSelected = SINEWAVE;
                                    binding.ivSinewave.setImageDrawable(context.getResources().getDrawable(R.drawable.sinewave, null));
                                    break;

                                case R.id.sawtooth:
                                    waveSelected = SAWTOOTH_WAVE;
                                    binding.ivSinewave.setImageDrawable(context.getResources().getDrawable(R.drawable.sawtooth, null));
                                    break;

                                case R.id.square:
                                    waveSelected = SQUAREWAVE;
                                    binding.ivSinewave.setImageDrawable(context.getResources().getDrawable(R.drawable.squarewave, null));
                                    break;


                            }

                            return true;
                        }
                    });

                    popup.show();

                }
            });

            binding.ivSquarewave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Utils.vibrate(context, 50, 25);

                    waveSelected = SINEWAVE;
                    binding.ivSinewave.setVisibility(View.VISIBLE);
                    binding.ivSquarewave.setVisibility(View.GONE);
                }
            });
        }

        private void action() {

            binding.edtAmplitude.setError(null);
            binding.edtFreq.setError(null);

            if (binding.edtFreq.getText().toString().isEmpty()) {
                Toast.makeText(context, "Please enter Frequency", Toast.LENGTH_SHORT).show();
                binding.edtFreq.setError("Invalid");
                return;
            }

            if (Double.parseDouble(binding.edtFreq.getText().toString()) > 20_000) {
                Toast.makeText(context, "Please enter frequency between 0 - 20kHz", Toast.LENGTH_LONG).show();
                binding.edtFreq.setError("0 - 20kHz");
                return;
            }

            if (binding.edtAmplitude.getText().toString().isEmpty()) {
                Toast.makeText(context, "Please enter Amplitude", Toast.LENGTH_SHORT).show();
                binding.edtAmplitude.setError("Invalid");
                return;
            }
            if (Integer.parseInt(binding.edtAmplitude.getText().toString()) > 100) {
                Toast.makeText(context, "Please enter Amplitude less than 100", Toast.LENGTH_SHORT).show();
                binding.edtAmplitude.setError("0 - 100");
                return;
            }

            if (getAdapterPosition() >= 0) {

                Set<Integer> keys = playMap.keySet();
                ArrayList<Integer> keyAL = new ArrayList<>();
                for (int x : keys) {
                    keyAL.add(x);
                }

                int key = keyAL.get(getAdapterPosition());
                Log.d("MainActivity", "onClick: " + getAdapterPosition() + " " + key);

                listener.onClickPlay(Double.parseDouble(binding.edtFreq.getText().toString()),
                        Integer.parseInt(binding.edtAmplitude.getText().toString()),
                        playMap.get(key),
                        key, waveSelected);
                if (playMap.get(key) == false) {
                    playMap.put(key, true);
                    binding.ivPlay.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    playMap.put(key, false);
                    binding.ivPlay.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        }
    }
}
