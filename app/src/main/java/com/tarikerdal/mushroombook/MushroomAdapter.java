package com.tarikerdal.mushroombook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tarikerdal.mushroombook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class MushroomAdapter extends RecyclerView.Adapter<MushroomAdapter.MushroomHolder> {

    ArrayList<Mushroom> mushroomArrayList;

    public MushroomAdapter(ArrayList<Mushroom> mushroomArrayList) {
        this.mushroomArrayList = mushroomArrayList;
    }

    @NonNull
    @Override
    public MushroomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()) ,parent ,false);
        return new MushroomHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MushroomHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.binding.recyclerViewTextView.setText(mushroomArrayList.get(position).originalname);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext() ,MushroomActivity.class);
                intent.putExtra("info" ,"old");
                intent.putExtra("mushroomId" ,mushroomArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mushroomArrayList.size();
    }

    //View Holder
    public class MushroomHolder extends RecyclerView.ViewHolder {
        private RecyclerRowBinding binding;

        public MushroomHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
