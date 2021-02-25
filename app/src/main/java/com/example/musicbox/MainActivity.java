package com.example.musicbox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DeleteDialog.DeleteDialogListener, SongAdapter.SongListener, AddDialog.AddDialogListener {

    private Playlist playlist;
    private SongAdapter songAdapter;
    private int positionToDelete;
    private DB db;
    private static final String ADD_DIALOG_TAG = "add dialog";
    private static int DEFAULT_INT = -1627752;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = new DB(MainActivity.this);
        db.setPlaylist(playlist);
        playlist = new Playlist(db.readPlaylist());
        songAdapter = new SongAdapter(playlist);
        songAdapter.setSongListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        DeleteDialog deleteDialog = new DeleteDialog();
        MusicTouchHelper musicTouchHelper = new MusicTouchHelper(db, playlist, songAdapter,
                fragmentManager, deleteDialog);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(musicTouchHelper);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(songAdapter);
        db.setPlaylist(playlist);
        db.setSongAdapter(songAdapter);

        startPlayerService("new");
    }

    @Override
    protected void onResume() {
        db = new DB(MainActivity.this);
        super.onResume();
    }

    public void addSong(View view){
        AddDialog addDialog = new AddDialog();
        addDialog.show(getSupportFragmentManager(), ADD_DIALOG_TAG);
    }

    public void prevSong(View view){
        startPlayerService("prev");
    }

    public void playSong(View view){
        startPlayerService("play");
    }

    public void pauseSong(View view){
        startPlayerService("pause");
    }

    public void nextSong(View view){
        startPlayerService("next");
    }

    private void startPlayerService(String cmd){
        if (playlist.getPlaylist().size() != 0) {
            Intent intent = new Intent(MainActivity.this, PlayerService.class);
            intent.putExtra("playlist", playlist.getPlaylist());
            intent.putExtra("cmd", cmd);
            startService(intent);
        }
    }

    @Override
    public void onPositiveBtnClicked() {
        int currentPlaying = db.readNowPlaying();
        db.deleteSong(playlist.getPlaylist().get(positionToDelete).getUUID(), positionToDelete);
        playlist.getPlaylist().remove(positionToDelete);
        songAdapter.notifyItemRemoved(positionToDelete);

        if (currentPlaying == positionToDelete) {
            startPlayerService("delete");
        }
    }

    @Override
    public void onNegativeBtnClicked() {
         songAdapter.notifyItemChanged(positionToDelete);
    }

    @Override
    public void applyPosition(int position) {
        this.positionToDelete = position;
    }

    @Override
    public void onSongClicked(int position, View view) {
        Intent intent = new Intent(this, SongActivity.class);
        intent.putExtra("song", playlist.getPlaylist().get(position));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.updateNowPlaying(db.readNowPlaying(), -2);
    }

    @Override
    public void onDoneClicked(String songName, String singer, String link, int position, String imageUri, String uuid) {
        AddDialog addDialog;

        if (link == null || (position <= 0 && position != DEFAULT_INT)) {
            if (link == null)
                Toast.makeText(this, "Song URL is required", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Position must be greater than or equal to 1", Toast.LENGTH_SHORT).show();
            addDialog = new AddDialog(songName, singer, link, position, uuid,true);
            addDialog.show(getSupportFragmentManager(), ADD_DIALOG_TAG);
        }

        else {
            if (position >= playlist.getSize() || position <= 0)
                position = playlist.getSize();
            else
                position -= 1;
            playlist.addSong(new Song(songName, singer, link, imageUri, uuid, null), position);
            songAdapter.notifyItemInserted(position);
            db.addSong(playlist.getPlaylist().get(position), position);
        }
    }
}