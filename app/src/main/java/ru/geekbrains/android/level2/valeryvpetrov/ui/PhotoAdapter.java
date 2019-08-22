package ru.geekbrains.android.level2.valeryvpetrov.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.geekbrains.android.level2.valeryvpetrov.R;
import ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.model.Photo;

import static ru.geekbrains.android.level2.valeryvpetrov.data.network.nasa.TypeConverter.dateToString;

@UiThread
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    @NonNull
    private List<Photo> photoList;

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image_view_photo)
        ImageView imageViewPhoto;
        @BindView(R.id.text_view_camera_name)
        TextView textViewCameraName;
        @BindView(R.id.text_view_shot_earth_date)
        TextView textViewEarthDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(@NonNull Photo photo) {
            loadImage(photo.getImgSrc());
            textViewCameraName.setText(photo.getCamera().getFullName());
            textViewEarthDate.setText(dateToString(photo.getEarthDate()));
        }

        private void loadImage(String imgSrc) {
            Glide.with(itemView.getContext())   // item view lifecycle aware
                    .load(imgSrc)
                    .thumbnail(Glide.with(itemView.getContext())
                            .load(R.drawable.loading))
                    .into(imageViewPhoto);
        }

        @OnClick(R.id.image_view_photo)
        void onPhotoClick() {
            showImageDialog();
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

    PhotoAdapter(@NonNull List<Photo> photoList) {
        this.photoList = photoList;
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

    void updatePhotoList(@NonNull List<Photo> photoList) {
        this.photoList = photoList;
        notifyDataSetChanged();
    }

    void clearPhotoList() {
        photoList.clear();
        notifyDataSetChanged();
    }
}
