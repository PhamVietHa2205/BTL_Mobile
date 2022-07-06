package com.example.application.musicplayer.Adapter;

import android.content.ContentResolver;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.application.musicplayer.Fragments.CloudSongFragment;
import com.example.application.musicplayer.Fragments.DeviceAlbumFragment;
import com.example.application.musicplayer.Fragments.DeviceSongFragment;
import com.example.application.musicplayer.Fragments.CurrentSongFragment;
import com.example.application.musicplayer.Fragments.FavSongFragment;
import com.example.application.musicplayer.Fragments.DeviceArtistFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private ContentResolver contentResolver;
    private String title[] = {"ONLINE SONGS","DEVICE SONGS", "ARTIST", "ALBUM", "FAVORITES", "CURRENT PLAYLIST"};

    public ViewPagerAdapter(FragmentManager fm, ContentResolver contentResolver) {
        super(fm);
        this.contentResolver = contentResolver;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return CloudSongFragment.getInstance(position);
            case 1:
                return DeviceSongFragment.getInstance(position, contentResolver);
            case 2:
                return DeviceArtistFragment.getInstance(position);
            case 3:
                return DeviceAlbumFragment.getInstance(position);
            case 4:
                return FavSongFragment.getInstance(position);
            case 5:
                return CurrentSongFragment.getInstance(position);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return title.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return title[position];
    }
}