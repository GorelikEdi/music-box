package com.example.musicbox;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SongActivity extends AppCompatActivity{

    ImageView imageView;
    TextView songNameTV;
    TextView singerTV;
    TextView linkTV;
    TextView positionTV;
    DB db = new DB(this);

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_activity);

        imageView = findViewById(R.id.image_full);
        songNameTV = findViewById(R.id.song_name);
        singerTV = findViewById(R.id.singer);
        linkTV = findViewById(R.id.song_link);
        positionTV = findViewById(R.id.position);

        Intent intent = getIntent();
        Song song = (Song) intent.getSerializableExtra("song");

        if (song.getImage().contains("musicbox_def")) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(song.getImage(), "drawable", getPackageName()));
            new ImageLoader(this, imageView).loadImage(null, bitmap, false);
        }
        else{
            new ImageLoader(this, imageView).loadImage(song.getImage(), null, false);
        }

        int position = Integer.parseInt(db.getPosition(song.getUUID())) + 1;
        songNameTV.setText(song.getSongName());
        singerTV.setText(song.getSinger());
        linkTV.setText(song.getLink());
        positionTV.setText("Position: " + position);
    }

}
