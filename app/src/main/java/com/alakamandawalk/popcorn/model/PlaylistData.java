package com.alakamandawalk.popcorn.model;

public class PlaylistData {

    String playlistId, playlistName, playlistImage, playlistCategory, playlistDescription, playlistAuthorId;

    public PlaylistData() {
    }

    public PlaylistData(String playlistId, String playlistName, String playlistImage, String playlistCategory, String playlistDescription, String playlistAuthorId) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistImage = playlistImage;
        this.playlistCategory = playlistCategory;
        this.playlistDescription = playlistDescription;
        this.playlistAuthorId = playlistAuthorId;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(String playlistId) {
        this.playlistId = playlistId;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistImage() {
        return playlistImage;
    }

    public void setPlaylistImage(String playlistImage) {
        this.playlistImage = playlistImage;
    }

    public String getPlaylistCategory() {
        return playlistCategory;
    }

    public void setPlaylistCategory(String playlistCategory) {
        this.playlistCategory = playlistCategory;
    }

    public String getPlaylistDescription() {
        return playlistDescription;
    }

    public void setPlaylistDescription(String playlistDescription) {
        this.playlistDescription = playlistDescription;
    }

    public String getPlaylistAuthorId() {
        return playlistAuthorId;
    }

    public void setPlaylistAuthorId(String playlistAuthorId) {
        this.playlistAuthorId = playlistAuthorId;
    }
}
