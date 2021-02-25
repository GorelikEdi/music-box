package com.example.musicbox;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Collections;

public class MusicTouchHelper extends ItemTouchHelper.SimpleCallback {

    private Playlist playlist;
    private SongAdapter songAdapter;
    private FragmentManager fragmentManager;
    private DeleteDialog deleteDialog;
    private static final String DELETE_DIALOG_TAG = "delete dialog";
    private DB db;

    public MusicTouchHelper(DB db, Playlist playlist, SongAdapter songAdapter,
                            FragmentManager fragmentManager, DeleteDialog deleteDialog){
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START |
                ItemTouchHelper.END, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.playlist = playlist;
        this.songAdapter = songAdapter;
        this.fragmentManager = fragmentManager;
        this.deleteDialog = deleteDialog;
        this.db = db;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();

        Collections.swap(playlist.getPlaylist(), from, to);
        songAdapter.notifyItemMoved(from, to);
        db.updatePlaylist(from, to);
        return false;
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        final Song song = playlist.getPlaylist().get(position);
        deleteDialog.setSong(song);
        deleteDialog.setPosition(position);
        deleteDialog.show(fragmentManager, DELETE_DIALOG_TAG);
    }
}
