package com.example.musicbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Playlist playlist;
    private Context context;
    private SongListener songListener;

    interface SongListener {
        void onSongClicked(int position, View view);
    }

    public SongAdapter(Playlist playlist){
        this.playlist = playlist;
    }

    public void setSongListener(SongListener songListener) {
        this.songListener = songListener;
    }

    public class SongViewHolder extends RecyclerView.ViewHolder{

        private TextView songName;
        private TextView singer;
        private ImageView image;
        private LottieAnimationView nowPlaying;

        public SongViewHolder(final View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.songImage);
            songName = itemView.findViewById(R.id.songName);
            singer = itemView.findViewById(R.id.singer);
            nowPlaying = itemView.findViewById(R.id.nowPlaying);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(songListener!= null)
                        songListener.onSongClicked(getAdapterPosition(), view);
                }
            });
        }
    }


    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.song_layout, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SongViewHolder holder, int position) {
        Song song = playlist.getPlaylist().get(position);

        if (song.song().get("visible").equals( "true")) {
            holder.nowPlaying.setVisibility(View.VISIBLE);
        }
        else {
            holder.nowPlaying.setVisibility(View.INVISIBLE);
        }

        holder.songName.setText(song.getSongName());
        holder.singer.setText(song.getSinger());

        if (song.getImage().contains("musicbox_def")) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(song.getImage(), "drawable", context.getPackageName()));
            new ImageLoader(this.context, holder.image).loadImage(null, bitmap, true);
        }
        else {
            new ImageLoader(this.context, holder.image).loadImage(song.getImage(), null, true);
        }
    }

    @Override
    public int getItemCount() {
        return this.playlist.getSize();
    }
}
