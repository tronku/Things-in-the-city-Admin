package com.example.things_in_city.admin;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder>{

    private List<Uri> imgList = new ArrayList<>();
    private ImageClickListener imageClickListener;

    public void setUriList(List<Uri> imgList) {
        this.imgList = imgList;
    }

    public ImageAdapter(List<Uri> imgList, ImageClickListener listener) {
        this.imgList = imgList;
        imageClickListener = listener;
    }

    public void updateList(){
        notifyDataSetChanged();
    }

    public Uri getImage(int position){
        return imgList.get(position);
    }

    public interface ImageClickListener {
        void onImageClick(int position);
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.image_item.setImageURI(imgList.get(position));
    }

    @Override
    public int getItemCount() {
        return imgList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView image_item;

        public ImageViewHolder(View itemView) {
            super(itemView);
            image_item = itemView.findViewById(R.id.image_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            imageClickListener.onImageClick(getAdapterPosition());
        }
    }

}

