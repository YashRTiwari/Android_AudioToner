package com.example.audiotoner;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.audiotoner.databinding.RvAddChannelRowBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class RvAddRowChannelAdapter extends RecyclerView.Adapter<RvAddRowChannelAdapter.ViewHolder> {

    int x = 0;
    OnClickListener listener;
    Context context;
    HashMap<Integer, Boolean> playMap = new HashMap<>();

    public interface OnClickListener {

        void onClickPlay(double freq, int volume, boolean isPlaying, int position);

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

        public ViewHolder(@NonNull final RvAddChannelRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.ivPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (binding.edtFreq.getText().toString().isEmpty()){
                        Toast.makeText(context, "Please enter Frequency", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (binding.edtAmplitude.getText().toString().isEmpty()){
                        Toast.makeText(context, "Please enter Amplitude", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (Integer.parseInt(binding.edtAmplitude.getText().toString()) > 100){
                        Toast.makeText(context, "Please enter Amplitude less than 100", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Set<Integer> keys = playMap.keySet();
                    ArrayList<Integer> keyAL = new ArrayList<>();
                    for (int x : keys){
                        keyAL.add(x);
                    }

                    int key = keyAL.get(getAdapterPosition());
                    Log.d("MainActivity", "onClick: "+getAdapterPosition()+" " +key);

                    listener.onClickPlay(Double.parseDouble(binding.edtFreq.getText().toString()),
                            Integer.parseInt(binding.edtAmplitude.getText().toString()),
                            playMap.get(key),
                            key);
                    if (playMap.get(key) == false) {
                        playMap.put(key, true);
                        binding.ivPlay.setImageResource(android.R.drawable.ic_media_pause);
                    } else {
                        playMap.put(key, false);
                        binding.ivPlay.setImageResource(android.R.drawable.ic_media_play);
                    }
                }
            });

            binding.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Set<Integer> keys = playMap.keySet();
                    ArrayList<Integer> keyAL = new ArrayList<>();
                    for (int x : keys){
                        keyAL.add(x);
                    }
                    int key = keyAL.get(getAdapterPosition());
                    listener.onClickDelete(playMap.get(key) == null ? false : playMap.get(key), key);
                    playMap.remove(key);
                    x= x - 1;
                    notifyItemRemoved(getAdapterPosition());
                }
            });
        }
    }
}
