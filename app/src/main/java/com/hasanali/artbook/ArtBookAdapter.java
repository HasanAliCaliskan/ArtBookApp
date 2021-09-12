package com.hasanali.artbook;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hasanali.artbook.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtBookAdapter extends RecyclerView.Adapter<ArtBookAdapter.ArtBookHolder> {

    ArrayList<Art> arrayList;
    public ArtBookAdapter(ArrayList<Art> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ArtBookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtBookHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtBookAdapter.ArtBookHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(arrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(),ArtActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("id",arrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ArtBookHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;
        public ArtBookHolder(@NonNull RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
