package com.example.application.musicplayer.Model;

import com.example.application.musicplayer.R;

public class SongsList implements Comparable<SongsList>{

    private String title;
    private String subTitle;
    private String link;
    private int thumbnail = R.drawable.header_background;

    private String album;

    public SongsList(){}

    public SongsList(String title, String subTitle, String path) {
        this.title = title;
        this.subTitle = subTitle;
        this.link = path;
    }

    public SongsList(String title, String subTitle, String path, String album) {
        this.title = title;
        this.subTitle = subTitle;
        this.link = path;
        this.album = album;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
//        Bitmap graphic;
//        try {
//            graphic = retrievThumbnail(this.link);
//            Log.v("thumnail1",   "retriev: " + graphic);
//        }catch (NullPointerException exception){
//            Log.v("thumnail1",   context + "");
//            graphic = BitmapFactory.decodeResource(context.getResources(), R.drawable.header_background);
//            Log.v("thumnail1",   graphic + "");
//        }
//        this.thumbnail = graphic;
//        Log.v("thumnail1",   "thun: " + thumbnail + "");
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String title){
        this.subTitle = title;
    }

    public int getThumbnail() {
        return this.thumbnail;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public int compareTo(SongsList songsList) {
        return this.getTitle().compareTo(songsList.getTitle());
    }

//    private Bitmap retrievThumbnail(String path) throws NullPointerException{
//        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//        byte[] imgbyte;
//        Bitmap bitmp;
//        BitmapFactory.Options option =new BitmapFactory.Options();
//        mmr.setDataSource(path);
//        imgbyte = mmr.getEmbeddedPicture();
//        bitmp = BitmapFactory.decodeByteArray(imgbyte, 0,imgbyte.length, option);
//        return bitmp;
//    }
}
