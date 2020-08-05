package com.alakamandawalk.popcorn.model;

public class AuthCatData {

    String catId, catImgUrl, catName, storyCount;

    public AuthCatData(String catId, String catImgUrl, String catName, String storyCount) {
        this.catId = catId;
        this.catImgUrl = catImgUrl;
        this.catName = catName;
        this.storyCount = storyCount;
    }

    public AuthCatData() {
    }

    public String getCatId() {
        return catId;
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public String getCatImgUrl() {
        return catImgUrl;
    }

    public void setCatImgUrl(String catImgUrl) {
        this.catImgUrl = catImgUrl;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getStoryCount() {
        return storyCount;
    }

    public void setStoryCount(String storyCount) {
        this.storyCount = storyCount;
    }
}
