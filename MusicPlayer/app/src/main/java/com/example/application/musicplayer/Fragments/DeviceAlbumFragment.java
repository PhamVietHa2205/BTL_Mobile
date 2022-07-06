package com.example.application.musicplayer.Fragments;


import static com.example.application.musicplayer.Activity.MainActivity.currentOfflineList;
import static com.example.application.musicplayer.Activity.MainActivity.playMode;

import android.app.TabActivity;
import android.bluetooth.BluetoothClass;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.application.musicplayer.Adapter.AlbumAdapter;
import com.example.application.musicplayer.Adapter.ArtistAdapter;
import com.example.application.musicplayer.Adapter.SongAdapter;
import com.example.application.musicplayer.Model.Album;
import com.example.application.musicplayer.Model.SongsList;
import com.example.application.musicplayer.R;

import java.util.ArrayList;
import java.util.Collections;

public class DeviceAlbumFragment extends ListFragment {


    private static ContentResolver contentResolver1;
    private ViewPager mViewPager;
    public ArrayList<SongsList> songsList;
    public ArrayList<Album> newList;

    private ListView listView;
    private ArrayList<Album> albumList = new ArrayList<>();

    private createDataParse createDataParse;
    private ContentResolver contentResolver;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        DeviceAlbumFragment tabFragment = new DeviceAlbumFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songsList = DeviceSongFragment.deviceSongList;
        mViewPager = (ViewPager) getActivity().findViewById(R.id.songs_viewpager);
        Log.e("Created", Integer.toString(songsList.size()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        createDataParse = (createDataParse) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.list_playlist);
        contentResolver = contentResolver1;
        setContent();
    }

    /**
     * Setting the content in the listView and sending the data to the Activity
     */
    public void setContent() {
        boolean searchedList = false;
        newList = new ArrayList<>();
        //getMusic();
        getAlbum();
        AlbumAdapter adapter = new AlbumAdapter(getContext(), albumList);
        if (!createDataParse.queryText().equals("")) {
            adapter = onQueryTextChange();
            adapter.notifyDataSetChanged();
            searchedList = true;
        } else {
            searchedList = false;
        }
        createDataParse.getLength(albumList.size());
        listView.setAdapter(adapter);
        Log.e("Created", Integer.toString(albumList.size()));
        final boolean finalSearchedList = searchedList;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getContext(), "You clicked :\n" + songsList.get(position), Toast.LENGTH_SHORT).show();
//                if (!finalSearchedList) {
//                    createDataParse.onDataPass(songsList.get(position).getTitle(), songsList.get(position).getLink());
//                    createDataParse.fullSongList(songsList, position);
//                } else {
//                    createDataParse.onDataPass(newList.get(position).getTitle(), newList.get(position).getLink());
//                    createDataParse.fullSongList(songsList, position);
//                }
                String albumName = albumList.get(position).getName();
                String artist = albumList.get(position).getArtist();
                currentOfflineList.clear();
                for (int i = 0; i < songsList.size(); i++) {
                    if (songsList.get(i).getAlbum().equals(albumName)) {
                        Log.e("Song", songsList.get(i).getTitle());
                        currentOfflineList.add(songsList.get(i));
                    }
                }
                if (playMode == 3) Collections.shuffle(currentOfflineList);
                mViewPager.setCurrentItem(5);
            }
        });

//        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                showDialog(position);
//                return true;
//            }
//        });
    }

    public void getSongFromAlbum() {
        boolean searchedList = false;
        songsList = new ArrayList<>();
        newList = new ArrayList<>();
        SongAdapter adapter = new SongAdapter(getContext(), currentOfflineList);
//        if (!createDataParse.queryText().equals("")) {
//            adapter = onQueryTextChange();
//            adapter.notifyDataSetChanged();
//            searchedList = true;
//        } else {
//            searchedList = false;
//        }
        createDataParse.getLength(currentOfflineList.size());
        listView.setAdapter(adapter);

        final boolean finalSearchedList = searchedList;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getContext(), "You clicked :\n" + songsList.get(position), Toast.LENGTH_SHORT).show();
                if (!finalSearchedList) {
                    createDataParse.onDataPass(currentOfflineList.get(position).getTitle(), currentOfflineList.get(position).getLink());
                    createDataParse.fullSongList(currentOfflineList, position);
                }
//                else {
//                    createDataParse.onDataPass(newList.get(position).getTitle(), newList.get(position).getLink());
//                    createDataParse.fullSongList(songsList, position);
//                }
                mViewPager.setCurrentItem(5);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog(position);
                return true;
            }
        });
    }

    public void getAlbum() {
        for (int i = 0; i < songsList.size(); i++) {
            Album album = new Album(songsList.get(i).getAlbum(), songsList.get(i).getSubTitle());
            if (!albumList.contains(album)) {
                albumList.add(album);
            }
        }
    }
    public AlbumAdapter onQueryTextChange() {
        String text = createDataParse.queryText();
        for (Album album : albumList) {
            String title = album.getName().toLowerCase();
            if (title.contains(text)) {
                newList.add(album);
            }
        }
        return new AlbumAdapter(getContext(), newList);

    }

    private void showDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.play_next))
                .setCancelable(true)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createDataParse.currentSong(songsList.get(position));
                        setContent();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public interface createDataParse {
        public void onDataPass(String name, String path);

        public void fullSongList(ArrayList<SongsList> songList, int position);

        public String queryText();

        public void currentSong(SongsList songsList);
        public void getLength(int length);
    }

}
