package cn.lanzp.hdnj.reader;

import android.text.TextUtils;

import java.util.List;

import cn.lanzp.hdnj.model.Paragraph;

/**
 * 生成拼音逐字标注的HTML字符串（使用ruby标签）
 * 适用于WebView渲染，拼音标注在每个汉字头顶
 */
public class PinyinHtmlBuilder {

    private PinyinHtmlBuilder() {}

    /**
     * 构建包含逐字拼音标注的完整HTML
     *
     * @param original    原文
     * @param pinyin      拼音（空格分隔，每个汉字/标点对应一个token）
     * @param fontSizePx  正文字体大小（CSS px）
     * @param textColor   正文文字颜色（CSS hex, 如 "#E0E0E0"）
     * @param pinyinColor 拼音文字颜色（CSS hex）
     * @return 完整HTML字符串
     */
    public static String buildPinyinHtml(String original, String pinyin,
                                          int fontSizePx,
                                          String textColor, String pinyinColor) {
        String rubyBody = buildRubyBody(original, pinyin);
        return buildPage(rubyBody, fontSizePx, textColor, pinyinColor);
    }

    /**
     * 构建包含逐字拼音标注及翻译的完整HTML
     */
    public static String buildPinyinWithTranslationHtml(String original, String pinyin,
                                                         String translation,
                                                         int fontSizePx,
                                                         String textColor, String pinyinColor,
                                                         String translationColor) {
        String rubyBody = buildRubyBody(original, pinyin);
        String translationHtml = escapeHtml(translation);
        return buildPageWithTranslation(rubyBody, translationHtml,
                fontSizePx, textColor, pinyinColor, translationColor);
    }

    /**
     * 构建整篇文章的HTML（所有段落合并在一个WebView中）
     * 完全替代逐段WebView的方案，解决长段落显示不全和滚动卡顿问题
     *
     * @param chapterTitle            篇目标题
     * @param paragraphs              段落列表
     * @param fontSizePx              正文字体大小（CSS px）
     * @param textColor               正文文字颜色（CSS hex）
     * @param pinyinColor             拼音文字颜色（CSS hex）
     * @param translationColor        翻译文字颜色（CSS hex）
     * @param labelColor              原文/翻译标签颜色（CSS hex）
     * @param dividerColor            分隔线颜色（CSS hex）
     * @param backgroundColor         背景颜色（CSS hex 或 transparent）
     * @return 完整HTML字符串
     */
    public static String buildFullArticleHtml(String chapterTitle,
                                               List<Paragraph> paragraphs,
                                               int fontSizePx,
                                               String textColor, String pinyinColor,
                                               String translationColor,
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
            String pinyin = p.getPinyinText();
            String translation = p.getTranslation();
            int paraNo = p.getParagraphNo();

            if (original == null) original = "";
            if (pinyin == null) pinyin = "";

            // 原文段落（带anchor用于js定位）
            body.append("<div class='para-section' id='para-").append(paraNo).append("'>");

            // 📜 原文标签
            body.append("<div class='label-row'>")
                .append("<span class='label'>📜 原文</span>")
                .append("<span class='label-divider'></span>")
                .append("</div>");

            // 原文内容（带ruby拼音标注）
            body.append("<div class='original-text'>")
                .append(buildRubyBody(original, pinyin))
                .append("</div>");

            // 翻译
            if (!TextUtils.isEmpty(translation)) {
                body.append("<div class='label-row' style='margin-top:12px;'>")
                    .append("<span class='label'>📖 现代文翻译</span>")
                    .append("<span class='label-divider'></span>")
                    .append("</div>");
                body.append("<div class='translation-text'>")
                    .append(escapeHtml(translation))
                    .append("</div>");
            }

            body.append("</div>");

            // 段落间分隔
            if (i < paragraphs.size() - 1) {
                body.append("<hr class='para-separator'>");
            }
        }

        return buildFullPage(body.toString(), fontSizePx, textColor, pinyinColor,
                translationColor, labelColor, dividerColor, backgroundColor);
    }

    private static String buildFullPage(String body, int fontSizePx,
                                         String textColor, String pinyinColor,
                                         String translationColor,
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
                "ruby { ruby-align: center; display: inline; }" +
                "rt { " +
                "  font-size: 0.6em; " +
                "  color: " + pinyinColor + "; " +
                "  font-weight: normal; " +
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
                "  color: " + labelColor + "; " +
                "  white-space: nowrap; " +
                "}" +
                ".label-divider { " +
                "  flex: 1; " +
                "  height: 1px; " +
                "  background-color: " + dividerColor + "; " +
                "  margin-left: 8px; " +
                "}" +
                ".original-text { " +
                "  width: 100%; " +
                "  font-size: " + fontSizePx + "px; " +
                "  line-height: 2.2; " +
                "}" +
                ".translation-text { " +
                "  font-size: " + (fontSizePx * 85 / 100) + "px; " +
                "  color: " + translationColor + "; " +
                "  line-height: 1.6; " +
                "  margin-top: 4px; " +
                "  padding: 0; " +
                "}" +
                "</style></head><body>" + body + "</body></html>";
    }

    private static String buildPage(String body, int fontSizePx,
                                     String textColor, String pinyinColor) {
        return "<!DOCTYPE html><html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0'>" +
                "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "html, body { " +
                "  width: 100%; " +
                "  min-width: 100%; " +
                "  overflow-x: hidden; " +
                "  overflow-y: hidden; " +
                "}" +
                "body { " +
                "  font-size: " + fontSizePx + "px; " +
                "  line-height: 2.0; " +
                "  color: " + textColor + "; " +
                "  background: transparent; " +
                "  word-wrap: break-word; " +
                "  word-break: break-word; " +
                "  white-space: normal; " +
                "  padding-left: 20px; padding-right: 20px; padding-top: 12px; padding-bottom: 0; " +
                "  display: block; " +
                "}" +
                "ruby { ruby-align: center; display: inline; }" +
                "rt { " +
                "  font-size: 0.6em; " +
                "  color: " + pinyinColor + "; " +
                "  font-weight: normal; " +
                "}" +
                "</style></head><body>" + body + "</body></html>";
    }

    private static String buildPageWithTranslation(String body, String translationHtml,
                                                    int fontSizePx,
                                                    String textColor, String pinyinColor,
                                                    String translationColor) {
        return "<!DOCTYPE html><html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0'>" +
                "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "html, body { " +
                "  width: 100%; " +
                "  min-width: 100%; " +
                "  overflow-x: hidden; " +
                "  overflow-y: hidden; " +
                "}" +
                "body { " +
                "  font-size: " + fontSizePx + "px; " +
                "  line-height: 2.0; " +
                "  color: " + textColor + "; " +
                "  background: transparent; " +
                "  word-wrap: break-word; " +
                "  word-break: break-word; " +
                "  white-space: normal; " +
                "  padding-left: 20px; padding-right: 20px; padding-top: 12px; padding-bottom: 0; " +
                "  display: block; " +
                "}" +
                "ruby { ruby-align: center; display: inline; }" +
                "rt { " +
                "  font-size: 0.6em; " +
                "  color: " + pinyinColor + "; " +
                "  font-weight: normal; " +
                "}" +
                ".translation { " +
                "  margin-top: 12px; " +
                "  padding-top: 8px; " +
                "  font-size: " + (fontSizePx * 85 / 100) + "px; " +
                "  color: " + translationColor + "; " +
                "  line-height: 1.6; " +
                "}" +
                "</style></head><body>" +
                body +
                "<div class='translation'>" + translationHtml + "</div>" +
                "</body></html>";
    }

    /**
     * 构建ruby标注内容
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
                // 标点/空格：直接输出，同时消耗对应的拼音token（标点本身就是token）
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
    public static boolean isHanCharacter(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS;
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
