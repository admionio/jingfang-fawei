package cn.lanzp.jffw.reader;

import android.text.TextUtils;

import java.util.List;

import cn.lanzp.jffw.model.Paragraph;

/**
 * 生成经方发微阅读页的HTML字符串
 * 无拼音标注，仅显示原文 + 曹颖甫注释
 */
public class ArticleHtmlBuilder {

    private ArticleHtmlBuilder() {}

    /**
     * 构建完整的文章HTML
     * 段落格式：
     * 【原文】
     * 原文内容
     * ──────────
     * 【曹氏发微】
     * 注释内容
     *
     * @param chapterTitle     篇目标题
     * @param paragraphs       段落列表
     * @param fontSizePx       正文字体大小（CSS px）
     * @param textColor        正文文字颜色
     * @param annotationColor  注释文字颜色
     * @param labelColor       标签颜色
     * @param dividerColor     分隔线颜色
     * @param backgroundColor  背景颜色
     */
    public static String buildFullArticleHtml(String chapterTitle,
                                               List<Paragraph> paragraphs,
                                               int fontSizePx,
                                               String textColor,
                                               String annotationColor,
                                               String labelColor,
                                               String dividerColor,
                                               String backgroundColor) {
        StringBuilder body = new StringBuilder();

        // 篇目标题
        if (!TextUtils.isEmpty(chapterTitle)) {
            body.append("<h2 class='chapter-title'>")
                .append(escapeHtml(chapterTitle))
                .append("</h2>");
            body.append("<hr class='title-divider'>");
        }

        // 逐段渲染
        for (int i = 0; i < paragraphs.size(); i++) {
            Paragraph p = paragraphs.get(i);
            String original = p.getOriginalText();
            String annotation = p.getAnnotation();
            int paraNo = p.getParagraphNo();

            if (original == null) original = "";

            // 原文段落（带anchor用于js定位）
            body.append("<div class='para-section' id='para-").append(paraNo).append("'>");

            // 【原文】标签
            body.append("<div class='label-row'>")
                .append("<span class='label label-original'>📜 原文</span>")
                .append("<span class='label-divider'></span>")
                .append("</div>");

            // 原文内容（带拼音ruby标注）
            String pinyin = p.getPinyinText();
            body.append("<div class='original-text'>")
                .append(buildRubyBody(original, pinyin))
                .append("</div>");

            // 注释
            if (!TextUtils.isEmpty(annotation)) {
                body.append("<div class='label-row' style='margin-top:12px;'>")
                    .append("<span class='label label-annotation'>📝 曹氏发微</span>")
                    .append("<span class='label-divider'></span>")
                    .append("</div>");
                body.append("<div class='annotation-text'>")
                    .append(escapeHtml(annotation))
                    .append("</div>");
            }

            body.append("</div>");

            // 段落间分隔
            if (i < paragraphs.size() - 1) {
                body.append("<hr class='para-separator'>");
            }
        }

        return buildFullPage(body.toString(), fontSizePx, textColor,
                annotationColor, labelColor, dividerColor, backgroundColor);
    }

    private static String buildFullPage(String body, int fontSizePx,
                                         String textColor,
                                         String annotationColor,
                                         String labelColor,
                                         String dividerColor,
                                         String backgroundColor) {
        return "<!DOCTYPE html><html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0'>" +
                "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "html, body { " +
                "  width: 100%; " +
                "  min-width: 100%; " +
                "  overflow-x: hidden; " +
                "}" +
                "body { " +
                "  font-size: " + fontSizePx + "px; " +
                "  line-height: 2.0; " +
                "  color: " + textColor + "; " +
                "  background: " + backgroundColor + "; " +
                "  word-wrap: break-word; " +
                "  word-break: break-word; " +
                "  white-space: normal; " +
                "  padding-left: 20px; padding-right: 20px; padding-top: 12px; padding-bottom: 0; " +
                "  display: block; " +
                "}" +
                ".chapter-title { " +
                "  font-size: " + (fontSizePx * 122 / 100) + "px; " +
                "  font-weight: bold; " +
                "  margin: 0 0 8px 0; " +
                "  padding: 0; " +
                "  line-height: 1.4; " +
                "}" +
                "hr.title-divider { " +
                "  border: none; " +
                "  height: 1px; " +
                "  background-color: " + dividerColor + "; " +
                "  margin: 0 0 16px 0; " +
                "}" +
                "hr.para-separator { " +
                "  border: none; " +
                "  height: 1px; " +
                "  background-color: " + dividerColor + "; " +
                "  margin: 16px 0; " +
                "}" +
                ".para-section { " +
                "  width: 100%; " +
                "  padding-bottom: 0; " +
                "}" +
                ".label-row { " +
                "  display: flex; " +
                "  align-items: center; " +
                "  margin-bottom: 8px; " +
                "}" +
                ".label { " +
                "  font-size: " + (fontSizePx * 72 / 100) + "px; " +
                "  font-weight: bold; " +
                "  white-space: nowrap; " +
                "}" +
                ".label-original { " +
                "  color: " + labelColor + ";" +
                "}" +
                ".label-annotation { " +
                "  color: " + annotationColor + ";" +
                "}" +
                ".label-divider { " +
                "  flex: 1; " +
                "  height: 1px; " +
                "  background-color: " + dividerColor + "; " +
                "  margin-left: 8px; " +
                "}" +
                "ruby { ruby-align: center; display: inline; }" +
                "rt { " +
                "  font-size: 0.6em; " +
                "  color: " + annotationColor + "; " +
                "  font-weight: normal; " +
                "}" +
                ".original-text { " +
                "  width: 100%; " +
                "  font-size: " + fontSizePx + "px; " +
                "  line-height: 2.2; " +
                "}" +
                ".annotation-text { " +
                "  font-size: " + (fontSizePx * 85 / 100) + "px; " +
                "  color: " + annotationColor + "; " +
                "  line-height: 1.8; " +
                "  margin-top: 4px; " +
                "  padding: 0; " +
                "}" +
                "</style></head><body>" + body + "</body></html>";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * 构建ruby标注内容（逐字拼音标注）
     * pinyin中每个token与原文逐字对齐（包括标点符号）
     */
    private static String buildRubyBody(String original, String pinyin) {
        if (TextUtils.isEmpty(original)) return "";

        if (TextUtils.isEmpty(pinyin)) {
            return escapeHtml(original);
        }

        String[] tokens = pinyin.trim().split("\\s+");
        StringBuilder sb = new StringBuilder(original.length() * 40);
        int tokenIdx = 0;

        for (int i = 0; i < original.length(); i++) {
            char ch = original.charAt(i);

            if (isHanCharacter(ch)) {
                // 汉字：使用ruby标签，上面是拼音下面是字
                String py = (tokenIdx < tokens.length) ? tokens[tokenIdx] : "";
                sb.append("<ruby>")
                  .append(escapeHtml(String.valueOf(ch)))
                  .append("<rt>").append(escapeHtml(py)).append("</rt>")
                  .append("</ruby>");
                tokenIdx++;
            } else {
                // 标点/空格：直接输出，同时消耗对应的拼音token
                sb.append(escapeHtml(String.valueOf(ch)));
                if (tokenIdx < tokens.length) {
                    tokenIdx++;
                }
            }
        }

        return sb.toString();
    }

    /**
     * 判断是否为CJK统一表意文字（汉字）
     */
    private static boolean isHanCharacter(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS;
    }
}
