package com.example.musicbox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.ArrayList;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener{

    private MediaPlayer player = new MediaPlayer();
    private boolean init = false;
    private ArrayList<Song> playlist;
    private int currentPlaying = 0;
    private DB db;
    private String cmd;
    private boolean prepared = false;
    private final int NOTIF_ID = 1;
    private NotificationCompat.Builder builder;
    private NotificationManager manager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        String channelID = "MusicBox";
        String channelName = "MusicBox channel";

        db = new DB(PlayerService.this);
        playlist = db.readPlaylist();

        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            manager.createNotificationChannel(channel);
        }

        builder = new NotificationCompat.Builder(this, channelID);

        Intent playIntent = new Intent(this, PlayerService.class);
        playIntent.putExtra("cmd", "play");
        playIntent.putExtra("playlist", playlist);
        PendingIntent playPendingIntent = PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, PlayerService.class);
        pauseIntent.putExtra("cmd", "pause");
        pauseIntent.putExtra("playlist", playlist);
        PendingIntent pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(this, PlayerService.class);
        prevIntent.putExtra("cmd", "prev");
        prevIntent.putExtra("playlist", playlist);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 2, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, PlayerService.class);
        nextIntent.putExtra("cmd", "next");
        nextIntent.putExtra("playlist", playlist);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent closeIntent = new Intent(this, PlayerService.class);
        closeIntent.putExtra("cmd", "close");
        closeIntent.putExtra("playlist", playlist);
        PendingIntent closePendingIntent = PendingIntent.getService(this, 4, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent activityIntent = new Intent(this, Intro.class);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 5, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setSmallIcon(R.drawable.note)
                .addAction(R.drawable.prev_btn, "Prev", prevPendingIntent)
                .addAction(R.drawable.play_btn, "Play", playPendingIntent)
                .addAction(R.drawable.pause_btn, "Pause", pausePendingIntent)
                .addAction(R.drawable.next_btn, "Next", nextPendingIntent)
                .addAction(R.drawable.close_btn, "Close", closePendingIntent)
                .setContentTitle("Song")
                .setContentText("Singer")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.musicbox_def_default_image_song))
                .setColor(ContextCompat.getColor(this, (R.color.colorAccent)))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(activityPendingIntent);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.reset();

        startForeground(NOTIF_ID, builder.build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.cmd = intent.getStringExtra("cmd");
        playlist = (ArrayList<Song>) intent.getSerializableExtra("playlist");
        db = new DB(PlayerService.this);



        if (!init && cmd.equals("new"))
            init = true;
        else if (init && cmd.equals("new"))
            init = false;
        if (init) {
            switch (this.cmd) {
                case "new":
                        try {
                            Bitmap bitmap = new ImageLoader(this).getImage(playlist.get(currentPlaying).getImage());
                            builder.setContentTitle(playlist.get(currentPlaying).getSongName());
                            builder.setContentText(playlist.get(currentPlaying).getSinger());
                            builder.setLargeIcon(bitmap);
                            manager.notify(NOTIF_ID, builder.build());
                            player.setDataSource(playlist.get(currentPlaying).getLink());
                            player.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                case "delete":
                    if (currentPlaying == playlist.size())
                        currentPlaying = 0;
                    if (player.isPlaying()) {
                        cmd = "";
                        db.updateNowPlaying(currentPlaying, -1);
                    }
                    player.stop();
                    player.reset();
                    try {
                        player.setDataSource(playlist.get(currentPlaying).getLink());
                        player.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case "play":
                    if (!player.isPlaying()) {
                        if (this.prepared) {
                            player.start();
                            db.updateNowPlaying(currentPlaying, -1);
                        } else {
                            Toast.makeText(this, "Wait until the song will be prepared", Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                case "next":
                    if (player.isPlaying()) {
                        changeCurrent(true);
                    }
                    break;

                case "prev":
                    if (player.isPlaying()) {
                        changeCurrent(false);
                    }
                    break;

                case "pause":
                    if (player.isPlaying()) {
                        player.pause();
                        db.updateNowPlaying(currentPlaying, -2);
                    }
                    break;

                case "close":
                    stopSelf();
            }
        }

        else {
                Bitmap bitmap = new ImageLoader(this).getImage(playlist.get(currentPlaying).getImage());
                builder.setContentTitle(playlist.get(currentPlaying).getSongName());
                builder.setContentText(playlist.get(currentPlaying).getSinger());
                builder.setLargeIcon(bitmap);
                manager.notify(NOTIF_ID, builder.build());
                init = true;
                if (player.isPlaying())
                    db.updateNowPlaying(currentPlaying, -1);
        }
        prepared = false;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        db.updateNowPlaying(currentPlaying, -2);

        if(player != null){
            if (player.isPlaying())
                player.stop();
            player.release();
        }
        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (!this.cmd.equals("delete"))
            changeCurrent(true);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        this.prepared = true;

        if (!cmd.equals("new") && !cmd.equals("delete")) {
            db.updateNowPlaying(currentPlaying, -1);
            player.start();

            Song song = playlist.get(currentPlaying);
            Bitmap bitmap = new ImageLoader(this).getImage(song.getImage());
            builder.setContentTitle(song.getSongName());
            builder.setContentText(song.getSinger());
            builder.setLargeIcon(bitmap);
            manager.notify(NOTIF_ID, builder.build());
        }
    }

    private void changeCurrent(boolean isNext) {
        int prevPlayed = currentPlaying;

        if (isNext) {
            currentPlaying++;
        }
        else {
            currentPlaying--;
        }

        if (currentPlaying == playlist.size())
            currentPlaying = 0;
        if (currentPlaying < 0)
            currentPlaying = playlist.size() - 1;
        player.stop();

        try {
            player.reset();
            player.setDataSource(playlist.get(currentPlaying).getLink());
            player.prepareAsync();
            db.updateNowPlaying(currentPlaying, prevPlayed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
