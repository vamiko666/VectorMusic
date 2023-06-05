
package com.vova.musik.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.vova.musik.R;
import com.vova.musik.activities.MainActivity;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaDataUtils;


public class SongChangeArtistDialog extends DialogFragment {

    private static final String TAG = SongChangeArtistDialog.class.getName();

    private EditText input;
    private View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = View.inflate(getActivity(), R.layout.dialog_song_change_artist, null);
        input = (EditText) view.findViewById(R.id.change_song_artist);
        builder.setView(view);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MediaDataUtils.changeArtist(getArguments().getLong(Constants.SONG_ID), input.getText().toString().trim(), getActivity());
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
