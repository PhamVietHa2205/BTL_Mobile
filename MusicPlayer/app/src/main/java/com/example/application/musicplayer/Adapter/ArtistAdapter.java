package com.example.application.musicplayer.Adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.application.musicplayer.Model.SongsList;
import com.example.application.musicplayer.R;

import java.util.ArrayList;

public class ArtistAdapter extends ArrayAdapter<String> implements Filterable{

    private Context mContext;
    private ArrayList<String> artistList = new ArrayList<>();
    private RelativeLayout songDetails;

    public ArtistAdapter(Context mContext, ArrayList<String> artistList) {
        super(mContext, 0, artistList);
        this.mContext = mContext;
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.playlist_items, parent, false);
        }
        String currentArtist = artistList.get(position);
        TextView tvTitle = listItem.findViewById(R.id.tv_music_name);
        TextView tvSubtitle = listItem.findViewById(R.id.tv_music_subtitle);
        tvTitle.setText(currentArtist);
        tvSubtitle.setText("");
        //tvTitle.setText(currentSong.getTitle());
        //tvSubtitle.setText(currentSong.getSubTitle());
        return listItem;
    }
}
