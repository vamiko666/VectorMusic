package com.vova.musik.databases;


public class TopTracksPair {

    private long id;
    private int played;

    public TopTracksPair(long id, int played) {
        this.id = id;
        this.played = played;
    }

    public long getID() {
        return this.id;
    }

    public int getValues() {
        return this.played;
    }
}