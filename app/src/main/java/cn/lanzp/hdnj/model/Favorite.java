package cn.lanzp.hdnj.model;

/**
 * 收藏数据模型
 */
public class Favorite {
    private long id;
    private long chapterId;
    private String chapterTitle;
    private String volume;
    private String createdAt;

    public Favorite() {}

    public Favorite(long id, long chapterId, String chapterTitle, String volume, String createdAt) {
        this.id = id;
        this.chapterId = chapterId;
        this.chapterTitle = chapterTitle;
        this.volume = volume;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getChapterId() { return chapterId; }
    public void setChapterId(long chapterId) { this.chapterId = chapterId; }

    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
