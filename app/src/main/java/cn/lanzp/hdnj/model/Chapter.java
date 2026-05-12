package cn.lanzp.hdnj.model;

/**
 * 篇章数据模型
 */
public class Chapter {
    private long id;
    private String volume;     // 素问/灵枢
    private int chapterNo;     // 篇序号
    private String title;      // 篇名（如"上古天真论"）
    private String chapterTag; // 篇标记（如"篇第一"）
    private int sortOrder;     // 排序
    private boolean isFavorite;

    public Chapter() {}

    public Chapter(long id, String volume, int chapterNo, String title, String chapterTag, int sortOrder) {
        this.id = id;
        this.volume = volume;
        this.chapterNo = chapterNo;
        this.title = title;
        this.chapterTag = chapterTag;
        this.sortOrder = sortOrder;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public int getChapterNo() { return chapterNo; }
    public void setChapterNo(int chapterNo) { this.chapterNo = chapterNo; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getChapterTag() { return chapterTag; }
    public void setChapterTag(String chapterTag) { this.chapterTag = chapterTag; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    /** 获取完整显示名：上古天真论篇第一 */
    public String getDisplayName() {
        return title + chapterTag;
    }

    /** 获取短显示名：上古天真论 */
    public String getShortName() {
        return title;
    }
}
