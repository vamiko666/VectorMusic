package com.vova.musik.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.vova.musik.R;
import com.vova.musik.activities.MainActivity;
import com.vova.musik.models.Song;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaDataUtils;

import java.util.List;


public class PlaylistFromSongsDialog extends DialogFragment {

    private static final String TAG = PlaylistFromSongsDialog.class.getName();

    private EditText input;
    private View view;
    private List<Song> songList;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        songList = getArguments().getParcelableArrayList(Constants.SONG_LIST);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = View.inflate(getActivity(), R.layout.dialog_playlist_create, null);
        input = (EditText) view.findViewById(R.id.rename_playlist);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long id = MediaDataUtils.MakePlaylist(input.getText().toString().trim(), getActivity());
                MediaDataUtils.addToPlaylist(id, songList, getActivity());
                ((MainActivity) getActivity()).getAll();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });

        return builder.create();
    }
}
