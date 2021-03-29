package com.example.mymusicmp3playertest1;

import java.util.Objects;

public class MusicData extends Object{
    private String id;
    private String artist;
    private String title;
    private String albumArt;
    private String duration;
    private int playCount;
    private int liked;

    //디폴트 생성자.
    public MusicData() {
    }

    //매개변수 생성자.
    public MusicData(String id, String artist, String title, String albumArt, String duration, int playCount, int liked) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.albumArt = albumArt;
        this.duration = duration;
        this.playCount = playCount;
        this.liked = liked;
    }

    //getters,setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public int getLiked() {
        return liked;
    }

    public void setLiked(int liked) {
        this.liked = liked;
    }

    @Override
    public boolean equals(Object object) {
        boolean equal = false;
        //MusicData 객체가 object에 들어가 있는 지 확인.
        if (object instanceof MusicData){
            MusicData musicData = (MusicData) object;
            equal = (this.id).equals(musicData.getId());
        }
        return equal;
    }

}
