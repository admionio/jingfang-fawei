package cn.lanzp.hdnj.model;

/**
 * 段落数据模型
 */
public class Paragraph {
    private long id;
    private long chapterId;
    private int paragraphNo;
    private String originalText;  // 原文
    private String pinyinText;    // 拼音
    private String translation;   // 现代文翻译
    private int sortOrder;

    public Paragraph() {}

    public Paragraph(long id, long chapterId, int paragraphNo,
                     String originalText, String pinyinText, String translation, int sortOrder) {
        this.id = id;
        this.chapterId = chapterId;
        this.paragraphNo = paragraphNo;
        this.originalText = originalText;
        this.pinyinText = pinyinText;
        this.translation = translation;
        this.sortOrder = sortOrder;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getChapterId() { return chapterId; }
    public void setChapterId(long chapterId) { this.chapterId = chapterId; }

    public int getParagraphNo() { return paragraphNo; }
    public void setParagraphNo(int paragraphNo) { this.paragraphNo = paragraphNo; }

    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }

    public String getPinyinText() { return pinyinText; }
    public void setPinyinText(String pinyinText) { this.pinyinText = pinyinText; }

    public String getTranslation() { return translation; }
    public void setTranslation(String translation) { this.translation = translation; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
