package com.example.application.musicplayer.Fragments;


import static com.example.application.musicplayer.Activity.MainActivity.currentOfflineList;
import static com.example.application.musicplayer.Activity.MainActivity.playMode;

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

import com.example.application.musicplayer.Adapter.SongAdapter;
import com.example.application.musicplayer.Model.SongsList;
import com.example.application.musicplayer.R;

import java.util.ArrayList;
import java.util.Collections;

public class DeviceSongFragment extends ListFragment {


    private static ContentResolver contentResolver1;
    public static ArrayList<SongsList> deviceSongList;
    public ArrayList<SongsList> songsList;
    public ArrayList<SongsList> newList;

    private ListView listView;
    private ViewPager mViewPager;
    private createDataParse createDataParse;
    private ContentResolver contentResolver;

    public static Fragment getInstance(int position, ContentResolver mcontentResolver) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        DeviceSongFragment tabFragment = new DeviceSongFragment();
        tabFragment.setArguments(bundle);
        contentResolver1 = mcontentResolver;
        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = (ViewPager) getActivity().findViewById(R.id.songs_viewpager);
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
        Log.e("contentResolver", " " + contentResolver);
        setContent();
    }

    /**
     * Setting the content in the listView and sending the data to the Activity
     */
    public void setContent() {
        boolean searchedList = false;
        songsList = new ArrayList<>();
        newList = new ArrayList<>();
        getMusic();
        SongAdapter adapter = new SongAdapter(getContext(), songsList);
        if (!createDataParse.queryText().equals("")) {
            adapter = onQueryTextChange();
            adapter.notifyDataSetChanged();
            searchedList = true;
        } else {
            searchedList = false;
        }
        createDataParse.getLength(songsList.size());
        listView.setAdapter(adapter);

        final boolean finalSearchedList = searchedList;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentOfflineList.clear();
                for (int i = 0; i < songsList.size(); i++) {
                    currentOfflineList.add(songsList.get(i));
                }
                if (playMode == 3) Collections.shuffle(currentOfflineList);
                // Toast.makeText(getContext(), "You clicked :\n" + songsList.get(position), Toast.LENGTH_SHORT).show();
                if (!finalSearchedList) {
                    createDataParse.onDataPass(songsList.get(position).getTitle(), songsList.get(position).getLink());
                    createDataParse.fullSongList(songsList, position);
                } else {
                    createDataParse.onDataPass(newList.get(position).getTitle(), newList.get(position).getLink());
                    createDataParse.fullSongList(songsList, position);
                }
//                mViewPager.setCurrentItem(5);
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


    public void getMusic() {
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.v("contentResolver", "" + contentResolver);
        contentResolver = getContext().getContentResolver();
        Cursor songCursor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            Log.v("version1", "S");
            songCursor = contentResolver.query(songUri, null, MediaStore.Audio.Media.IS_MUSIC + "!= 0 and " +

                            MediaStore.Audio.Media.IS_ALARM + " = 0 and " + MediaStore.Audio.Media.IS_RECORDING + " = 0 and " +
                            MediaStore.Audio.Media.IS_NOTIFICATION + " = 0 and " + MediaStore.Audio.Media.IS_RINGTONE + " = 0"
                    , null, MediaStore.Audio.Media.TITLE + " ASC");
        } else {
            Log.v("version1", "not S");
            songCursor = contentResolver.query(songUri, null, MediaStore.Audio.Media.IS_MUSIC + "!= 0 and " +
                            MediaStore.Audio.Media.IS_ALARM + " = 0 and " +
                            MediaStore.Audio.Media.IS_NOTIFICATION + " = 0 and " + MediaStore.Audio.Media.IS_RINGTONE + " = 0"
                    , null, MediaStore.Audio.Media.TITLE + " ASC");
        }
        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songAlbum = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int songPath = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do {
                String path = songCursor.getString(songPath);
                if (!path.toLowerCase().contains("record")){
                    songsList.add(new SongsList(songCursor.getString(songTitle), songCursor.getString(songArtist),
                            path, songCursor.getString(songAlbum)));
                }
            } while (songCursor.moveToNext());
            songCursor.close();
        }
        Collections.sort(songsList);
        deviceSongList = songsList;
    }

    public SongAdapter onQueryTextChange() {
        String text = createDataParse.queryText();
        for (SongsList songs : songsList) {
            String title = songs.getTitle().toLowerCase();
            if (title.contains(text)) {
                newList.add(songs);
            }
        }
        return new SongAdapter(getContext(), newList);

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
