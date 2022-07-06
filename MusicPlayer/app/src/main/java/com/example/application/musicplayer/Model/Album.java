package com.example.application.musicplayer.Model;

import com.example.application.musicplayer.R;

import java.util.Objects;

public class Album implements Comparable<Album>{

    private String name;
    private String artist;

    public Album(){}

    public Album(String name, String artist) {
        this.name = name;
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public int compareTo(Album album) {
        return this.getName().compareTo(album.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return Objects.equals(name, album.name) && Objects.equals(artist, album.artist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, artist);
    }
}
