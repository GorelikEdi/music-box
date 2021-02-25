package com.example.musicbox;

import java.util.ArrayList;

public class Playlist {

    private ArrayList<Song> playlist;

    public Playlist(ArrayList<Song> playlist){
        this.playlist = playlist;
    }

    public ArrayList<Song> getPlaylist() {
        return playlist;
    }

    public void addSong(Song song, int index){
        this.playlist.add(index, song);
    }

    public int getSize(){
        return this.playlist.size();
    }

}
