package com.alakamandawalk.popcorn.model;

public class StoryData {

    String story, storyName, storyId, storyDate, storyImage, storyCategoryId, storyPlaylistId, storySearchTag, authorId, isPremium;

    public StoryData() {
    }

    public StoryData(String story, String storyName, String storyId, String storyDate, String storyImage, String storyCategoryId, String storyPlaylistId, String storySearchTag, String authorId, String isPremium) {
        this.story = story;
        this.storyName = storyName;
        this.storyId = storyId;
        this.storyDate = storyDate;
        this.storyImage = storyImage;
        this.storyCategoryId = storyCategoryId;
        this.storyPlaylistId = storyPlaylistId;
        this.storySearchTag = storySearchTag;
        this.authorId = authorId;
        this.isPremium = isPremium;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getStoryName() {
        return storyName;
    }

    public void setStoryName(String storyName) {
        this.storyName = storyName;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getStoryDate() {
        return storyDate;
    }

    public void setStoryDate(String storyDate) {
        this.storyDate = storyDate;
    }

    public String getStoryImage() {
        return storyImage;
    }

    public void setStoryImage(String storyImage) {
        this.storyImage = storyImage;
    }

    public String getStoryCategoryId() {
        return storyCategoryId;
    }

    public void setStoryCategoryId(String storyCategoryId) {
        this.storyCategoryId = storyCategoryId;
    }

    public String getStoryPlaylistId() {
        return storyPlaylistId;
    }

    public void setStoryPlaylistId(String storyPlaylistId) {
        this.storyPlaylistId = storyPlaylistId;
    }

    public String getStorySearchTag() {
        return storySearchTag;
    }

    public void setStorySearchTag(String storySearchTag) {
        this.storySearchTag = storySearchTag;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getIsPremium() {
        return isPremium;
    }

    public void setIsPremium(String isPremium) {
        this.isPremium = isPremium;
    }
}
