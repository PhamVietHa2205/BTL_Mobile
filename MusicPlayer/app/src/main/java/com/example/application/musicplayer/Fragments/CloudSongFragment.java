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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.viewpager.widget.ViewPager;

import com.example.application.musicplayer.Adapter.SongAdapter;
import com.example.application.musicplayer.Model.SongsList;
import com.example.application.musicplayer.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class CloudSongFragment extends ListFragment {
    public ArrayList<SongsList> songsList;
    public ArrayList<SongsList> newList;

    private ListView listView;
    private DatabaseReference db;
    private ViewPager mViewPager;
    private createDataParse createDataParse;

    public static Fragment getInstance(int position){
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        CloudSongFragment tabFragment = new CloudSongFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewPager = (ViewPager) getActivity().findViewById(R.id.songs_viewpager);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listView = view.findViewById(R.id.list_playlist);
        setContent();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        createDataParse = (createDataParse) context;
    }

    private void setContent() {
        songsList = new ArrayList<>();
        newList = new ArrayList<>();
        getMusic();
    }

    private void getMusic() {
        db = FirebaseDatabase.getInstance().getReference().child("songs");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                songsList.clear();
                for (DataSnapshot dt: snapshot.getChildren()){
                    SongsList s = dt.getValue(SongsList.class);
                    if (s.getSubTitle() == null){
                        s.setSubTitle("<unknown>");
                    }
                    songsList.add(s);
                }
                Collections.sort(songsList);
                addListViewListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void addListViewListener(){
        boolean searchedList = false;
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
                if (!finalSearchedList) {
                    createDataParse.onDataPass(songsList.get(position).getTitle(), songsList.get(position).getLink());
                    createDataParse.fullSongList(songsList, position);
                } else {
                    createDataParse.onDataPass(newList.get(position).getTitle(), newList.get(position).getLink());
                    createDataParse.fullSongList(songsList, position);
                }
                //mViewPager.setCurrentItem(5);
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

    private SongAdapter onQueryTextChange() {
        String text = createDataParse.queryText();
        for (SongsList songs : songsList) {
            String title = songs.getTitle().toLowerCase();
            if (title.contains(text)) {
                newList.add(songs);
            }
        }
        return new SongAdapter(getContext(), newList);
    }

    private void showDialog(int position) {
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
