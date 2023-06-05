package com.vova.musik.databases;


public class RecentlyPlayedPair {

    private long id;
    private int played;

    public RecentlyPlayedPair(long id, int played) {
        this.id = id;
        this.played = played;
    }

    public long getID() {
        return this.id;
    }

    public int getValues() {
        return this.played;
    }


    @Override
    public boolean equals(Object other) {
        RecentlyPlayedPair otherPair = (RecentlyPlayedPair) other;
        return (otherPair.getID() == this.getID());
    }}


