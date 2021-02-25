package com.example.musicbox;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class Song implements Serializable {

    private HashMap<String, String> song;

    public Song(String songName, String singer, String link, String image, String uuid, String visible) {
        this.song = new HashMap<String, String>();
        song.put("songName", songName);
        song.put("singer", singer);

        if (uuid != null)
            song.put("uuid", uuid);
        else
            song.put("uuid", UUID.randomUUID().toString());

        song.put("link", link);

        if (image != null) {
            if (image.contains("temp.jpg")) {
                File file = new File(image);
                File file2 = new File(image.replace("temp", song.get("uuid")));
                file.renameTo(file2);
                image = image.replace("temp", song.get("uuid"));
            }
            song.put("image", image);
        }
        else
            song.put("image", "musicbox_def_default_image_song");

        if (visible == null)
            song.put("visible", "false");
        else
            song.put("visible", visible);
    }

    public HashMap<String, String> song() {
        return song;
    }

    public String getSongName(){
        return this.song.get("songName");
    }

    public String getSinger(){
        return this.song.get("singer");
    }

    public String getImage(){
        return this.song.get("image");
    }

    public String getLink(){
        return this.song.get("link");
    }

    public String getUUID() {return this.song.get("uuid");}

    public void setVisible(String visibility) {this.song.replace("visible", visibility);}

    public void setId(String id) {
        this.song.put("id", id);
    }


}
