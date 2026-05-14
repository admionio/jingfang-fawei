package cn.lanzp.jffw.model;

/**
 * 段落数据模型
 * 经方发微：原文 + 注释（无拼音）
 */
public class Paragraph {
    private long id;
    private long chapterId;
    private int paragraphNo;
    private String originalText;  // 原文
    private String annotation;    // 曹颖甫注释（替代translation）
    private int sortOrder;

    public Paragraph() {}

    public Paragraph(long id, long chapterId, int paragraphNo,
                     String originalText, String annotation, int sortOrder) {
        this.id = id;
        this.chapterId = chapterId;
        this.paragraphNo = paragraphNo;
        this.originalText = originalText;
        this.annotation = annotation;
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

    public String getAnnotation() { return annotation; }
    public void setAnnotation(String annotation) { this.annotation = annotation; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
