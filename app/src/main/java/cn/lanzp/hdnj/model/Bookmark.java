package cn.lanzp.hdnj.model;

/**
 * 书签数据模型
 */
public class Bookmark {
    private long id;
    private long chapterId;
    private String chapterTitle;
    private String volume;
    private int paragraphNo;
    private String excerpt;     // 原文片段
    private String createdAt;

    public Bookmark() {}

    public Bookmark(long id, long chapterId, String chapterTitle, String volume,
                    int paragraphNo, String excerpt, String createdAt) {
        this.id = id;
        this.chapterId = chapterId;
        this.chapterTitle = chapterTitle;
        this.volume = volume;
        this.paragraphNo = paragraphNo;
        this.excerpt = excerpt;
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

    public int getParagraphNo() { return paragraphNo; }
    public void setParagraphNo(int paragraphNo) { this.paragraphNo = paragraphNo; }

    public String getExcerpt() { return excerpt; }
    public void setExcerpt(String excerpt) { this.excerpt = excerpt; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
