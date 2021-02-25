package com.example.musicbox;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import java.util.Objects;

public class DeleteDialog extends DialogFragment {

    interface DeleteDialogListener {
        void onPositiveBtnClicked();
        void onNegativeBtnClicked();
        void applyPosition(int position);
    }

    DeleteDialogListener deleteDialogListener;
    private int position;
    private Song song;

    public void setPosition(int position){
        this.position = position;
    }

    public void setSong(Song song){
        this.song = song;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        deleteDialogListener = (DeleteDialogListener) context;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        View view = inflater.inflate(R.layout.delete_dialog, null);

        TextView songName = view.findViewById(R.id.song_name);
        TextView singer = view.findViewById(R.id.singer);
        songName.setText(song.getSongName());
        singer.setText(song.getSinger());

        builder.setView(view)
                .setTitle("Want to delete this song?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteDialogListener.applyPosition(position);
                        deleteDialogListener.onNegativeBtnClicked();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteDialogListener.applyPosition(position);
                        deleteDialogListener.onPositiveBtnClicked();
                    }
                });

        return builder.create();
    }
}