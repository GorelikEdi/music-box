package com.example.musicbox;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AddDialog extends DialogFragment {

    private static int DEFAULT_INT = -1627752;
    private EditText songName;
    private EditText singer;
    private EditText link;
    private EditText position;
    private String songNameDone = "Song";
    private String singerDone = "Singer";
    private String imageDone = null;
    private int positionDone = DEFAULT_INT;
    private String linkDone = null;
    private String tempSongName;
    private String tempSinger;
    private int tempPosition;
    private String tempLink;
    private boolean returnedDialog = false;
    private final static int CAMERA_REQUEST = 1;
    private final static int EXTERNAL_STORAGE_REQUEST = 2;
    private File file;
    private String AUTHORITY = ".provider";
    private String uuid = null;
    private String tempUuid;

    interface AddDialogListener {
        void onDoneClicked(String songName, String singer, String link, int position, String image, String uuid);
    }

    public AddDialog(String songName, String singer, String link, int position, String uuid, boolean returnedDialog){
        this.tempSongName = songName;
        this.tempSinger = singer;
        this.tempPosition = position;
        this.tempLink = link;
        this.returnedDialog = returnedDialog;
        this.tempUuid = uuid;
    }

    public AddDialog(){}

    AddDialogListener addDialogListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        addDialogListener = (AddDialogListener) context;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_dialog, null);

        songName = view.findViewById(R.id.song_name);
        singer = view.findViewById(R.id.singer);
        position = view.findViewById(R.id.position);
        ImageButton imageCameraBtn = view.findViewById(R.id.songImageCamera);
        ImageButton imageGalleryBtn = view.findViewById(R.id.songImageGallery);
        link = view.findViewById(R.id.link);

        if (returnedDialog){
            if (!tempSongName.equals("Song"))
                songName.setText(tempSongName);
            if (!tempSinger.equals("Singer"))
                singer.setText(tempSinger);
            if (tempPosition != DEFAULT_INT)
                position.setText(String.valueOf(tempPosition));
            if (tempLink != null)
                link.setText(tempLink);
            if (uuid != null)
                uuid = tempUuid;
        }

        imageCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String permission = Manifest.permission.CAMERA;
                if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, CAMERA_REQUEST);
                else
                    startCamera();
            }
        });

        imageGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, EXTERNAL_STORAGE_REQUEST);
                else
                    startGallery();
            }
        });



        builder.setView(view)
                .setTitle("Add song")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!TextUtils.isEmpty(songName.getText()))
                            songNameDone = songName.getText().toString();
                        if (!TextUtils.isEmpty(singer.getText()))
                            singerDone = singer.getText().toString();
                        if (!TextUtils.isEmpty(link.getText()))
                            linkDone = link.getText().toString();
                        if (!TextUtils.isEmpty(position.getText()))
                            positionDone = Integer.parseInt(position.getText().toString());
                        addDialogListener.onDoneClicked(songNameDone, singerDone, linkDone, positionDone, imageDone, uuid);
                    }
                });
        return builder.create();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            if (requestCode == CAMERA_REQUEST)
                startCamera();
            else if (requestCode == EXTERNAL_STORAGE_REQUEST)
                startGallery();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK){
            switch (requestCode) {
                case CAMERA_REQUEST:
                    imageDone = file.getAbsolutePath();
                    break;

                case EXTERNAL_STORAGE_REQUEST:
                    imageDone = data.getData().toString();
                    break;
            }
        }
    }

    private void startCamera() {
        AUTHORITY = getContext().getPackageName() + AUTHORITY;
        if (uuid == null)
            uuid = UUID.randomUUID().toString();
        file = new File(new File(getContext().getFilesDir(), "photos"), uuid + ".jpg");

        if (file.exists()) {
            file.delete();
        }
        else {
            file.getParentFile().mkdirs();
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri outputUri = FileProvider.getUriForFile(getContext(), AUTHORITY, file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        else if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN) {
            ClipData clip = ClipData.newUri(getContext().getContentResolver(), "A photo", outputUri);

            intent.setClipData(clip);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        else {
            List<ResolveInfo> resInfoList=
                    getContext().getPackageManager()
                            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                getContext().grantUriPermission(packageName, outputUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }

        startActivityForResult(intent, CAMERA_REQUEST);

    }

    private void startGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, EXTERNAL_STORAGE_REQUEST);
    }
}