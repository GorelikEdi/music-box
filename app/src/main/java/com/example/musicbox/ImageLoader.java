package com.example.musicbox;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageLoader {

    private ImageView imageView;
    private Context context;
    private String AUTHORITY = ".provider";

    public ImageLoader(Context context, ImageView imageView){
        this.context = context;
        this.imageView = imageView;
        AUTHORITY = context.getPackageName() + AUTHORITY;
    }

    public ImageLoader(Context context){
        this.context = context;
        AUTHORITY = context.getPackageName() + AUTHORITY;
    }

    public Bitmap getImage(String name){
        Resources resources = this.context.getResources();
        String packageName = this.context.getPackageName();
        Bitmap bitmap = null;

        if (name.contains("musicbox_def")) {
            bitmap = BitmapFactory.decodeResource(resources,
                    context.getResources().getIdentifier(name, "drawable", packageName));
        }

        else {
            Uri uri;

            if (name.contains("com.example.musicbox")) {
                File file = new File(name);
                uri = FileProvider.getUriForFile(context, AUTHORITY, file);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            else
                try {
                    name = parseURI(name);
                    InputStream is = context.getContentResolver().openInputStream(Uri.parse(name));
                    if (is != null) {
                        bitmap = BitmapFactory.decodeStream(is);
                    }
                } catch (FileNotFoundException e) {
                    bitmap = BitmapFactory.decodeResource(resources,
                            context.getResources().getIdentifier(
                                    "musicbox_def_default_image_song", "drawable",
                                    packageName));
                }
        }
        return bitmap;
    }

    public void loadImage(String imageUri, Bitmap bitmap, boolean circle){
        if (bitmap == null){
            Uri imageUri1;
            if (imageUri.contains("com.example.musicbox")){
                File file = new File(imageUri);
                Uri outputUri = FileProvider.getUriForFile(context, AUTHORITY, file);
                imageUri1 = outputUri;
            }
            else {
                try {
                    InputStream is = context.getContentResolver().openInputStream(Uri.parse(parseURI(imageUri)));
                    imageUri1 = Uri.parse(imageUri);
                } catch (FileNotFoundException e) {
                    bitmap = BitmapFactory.decodeResource(context.getResources(),
                            context.getResources().getIdentifier("musicbox_def_default_image_song",
                                    "drawable", context.getPackageName()));
                    loadImage(null, bitmap, circle);
                    return;
                }
            }
            if (circle)
                Glide.with(this.context)
                        .load(imageUri1)
                        .circleCrop()
                        .into(this.imageView);
            else
                Glide.with(this.context)
                        .load(imageUri1)
                        .into(this.imageView);
        }
        else {
            if (circle)
                Glide.with(this.context)
                        .load(bitmap)
                        .circleCrop()
                        .into(this.imageView);
            else
                Glide.with(this.context)
                        .load(bitmap)
                        .into(this.imageView);
        }
    }

    private String parseURI(String oldUri){
        String newUri = oldUri;
        if (oldUri.contains("com.google.android.apps.photos.contentprovider")) {
            newUri = oldUri.split("content")[3].split("/ORIGINAL")[0];
            newUri = newUri.replace("%3A", ":").replace("%2F", "/");
            newUri = "content" + newUri;
        }
        return newUri;
    }
}
