package cn.lanzp.jffw.database;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import cn.lanzp.jffw.model.Chapter;
import cn.lanzp.jffw.model.Paragraph;

/**
 * 从assets中的JSON文件导入数据到SQLite数据库
 * JSON结构: {"books": [{"name":"...", "author":"...", "description":"...", "chapters": [...]}]}
 */
public class DataImporter {
    private static final String PREFS_NAME = "data_import";
    private static final String KEY_IMPORTED = "imported";

    public static boolean isDataImported(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_IMPORTED, false);
    }

    public static void markDataImported(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_IMPORTED, true)
                .apply();
    }

    public static boolean importData(Context context, DbHelper dbHelper) {
        try {
            dbHelper.clearChapters();
            dbHelper.clearParagraphs();

            // 导入伤寒发微
            importVolume(context, dbHelper, "shanghan.json", "伤寒发微");
            // 导入金匮发微
            importVolume(context, dbHelper, "jinkui.json", "金匮发微");

            markDataImported(context);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void importVolume(Context context, DbHelper dbHelper, String fileName, String volume) throws Exception {
        InputStream is = context.getAssets().open(fileName);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        String json = new String(buffer, StandardCharsets.UTF_8);

        JSONObject root = new JSONObject(json);
        JSONArray books = root.getJSONArray("books");
        JSONObject book = books.getJSONObject(0); // 每文件只有一本书

        JSONArray chapters = book.getJSONArray("chapters");
        int sortOrder = 0;

        for (int i = 0; i < chapters.length(); i++) {
            JSONObject chapterObj = chapters.getJSONObject(i);
            int chapterNo = chapterObj.getInt("chapter_no");
            String title = chapterObj.getString("title");
            String chapterTag = chapterObj.optString("chapter_tag", "");

            Chapter ch = new Chapter();
            ch.setVolume(volume);
            ch.setChapterNo(chapterNo);
            ch.setTitle(title);
            ch.setChapterTag(chapterTag);
            ch.setSortOrder(sortOrder++);
            long chapterId = dbHelper.insertChapter(ch);

            // 导入段落
            JSONArray paragraphs = chapterObj.optJSONArray("paragraphs");
            if (paragraphs != null) {
                for (int j = 0; j < paragraphs.length(); j++) {
                    JSONObject paraObj = paragraphs.getJSONObject(j);
                    Paragraph p = new Paragraph();
                    p.setChapterId(chapterId);
                    p.setParagraphNo(paraObj.getInt("paragraph_no"));
                    p.setOriginalText(paraObj.optString("original_text", ""));
                    p.setPinyinText(paraObj.optString("pinyin_text", ""));
                    p.setAnnotation(paraObj.optString("annotation", ""));
                    p.setSortOrder(j);
                    dbHelper.insertParagraph(p);
                }
            }
        }
    }
}
