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

import com.example.application.musicplayer.Model.Album;
import com.example.application.musicplayer.Model.SongsList;
import com.example.application.musicplayer.R;

import java.util.ArrayList;

public class AlbumAdapter extends ArrayAdapter<Album> implements Filterable{

    private Context mContext;
    private ArrayList<Album> albumList = new ArrayList<>();
    private RelativeLayout songDetails;

    public AlbumAdapter(Context mContext, ArrayList<Album> albumList) {
        super(mContext, 0, albumList);
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.playlist_items, parent, false);
        }
        Album album = albumList.get(position);
        String albumName = album.getName();
        String albumArtist = album.getArtist();
        TextView tvTitle = listItem.findViewById(R.id.tv_music_name);
        TextView tvSubtitle = listItem.findViewById(R.id.tv_music_subtitle);
        tvTitle.setText(albumName);
        tvSubtitle.setText(albumArtist);
        //tvTitle.setText(currentSong.getTitle());
        //tvSubtitle.setText(currentSong.getSubTitle());
        return listItem;
    }
}
