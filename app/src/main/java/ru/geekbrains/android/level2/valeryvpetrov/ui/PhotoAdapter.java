package ru.geekbrains.android.level2.valeryvpetrov.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.TypeConverter;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.model.Photo;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private List<Photo> photoList;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageViewPhoto;
        private TextView textViewCameraName;
        private TextView textViewEarthDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.image_view_photo);
            textViewCameraName = itemView.findViewById(R.id.text_view_camera_name);
            textViewEarthDate = itemView.findViewById(R.id.text_view_shot_earth_date);
        }

        void bind(@NonNull Photo photo) {
            Glide.with(itemView.getContext())
                    .load(photo.imgSrc)
                    .thumbnail(Glide.with(itemView.getContext()).load(R.drawable.loading))
                    .into(imageViewPhoto);

            textViewCameraName.setText(photo.camera.fullName);
            textViewEarthDate.setText(TypeConverter.dateToString(photo.earthDate));

            imageViewPhoto.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.image_view_photo) {
                showImageDialog();
            }
        }

        private void showImageDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            LayoutInflater layoutInflater = (LayoutInflater) itemView.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (layoutInflater != null) {
                View viewDialogPhoto = layoutInflater.inflate(R.layout.dialog_photo_zoom, null);
                PhotoView photoView = viewDialogPhoto.findViewById(R.id.photo_view_photo);
                photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                photoView.setImageDrawable(imageViewPhoto.getDrawable());
                builder.setView(viewDialogPhoto);
                AlertDialog dialogZoomPhoto = builder.create();
                dialogZoomPhoto.show();
            }
        }
    }

    PhotoAdapter(List<Photo> photoList) {
        this.photoList = photoList;
    }

    void updatePhotoList(List<Photo> photoList) {
        this.photoList = photoList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(photoList.get(position));
    }

    @Override
    public int getItemCount() {
        return photoList.size();
    }
}
