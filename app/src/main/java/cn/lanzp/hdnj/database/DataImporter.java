package cn.lanzp.hdnj.database;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import cn.lanzp.hdnj.model.Chapter;
import cn.lanzp.hdnj.model.Paragraph;

/**
 * 从assets中的JSON文件导入数据到SQLite数据库
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

            // 导入素问
            importVolume(context, dbHelper, "suwen.json", "素问");
            // 导入灵枢
            importVolume(context, dbHelper, "lingshu.json", "灵枢");

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

        JSONArray chapters = new JSONArray(json);
        int sortOrder = 0;

        for (int i = 0; i < chapters.length(); i++) {
            JSONObject chapterObj = chapters.getJSONObject(i);
            int id = chapterObj.getInt("id");
            String chapter = chapterObj.getString("chapter");
            String title = chapterObj.getString("title");

            Chapter ch = new Chapter();
            ch.setVolume(volume);
            ch.setChapterNo(i + 1);
            ch.setTitle(title);
            ch.setChapterTag(chapter);
            ch.setSortOrder(sortOrder++);
            long chapterId = dbHelper.insertChapter(ch);

            // 导入段落
            JSONArray content = chapterObj.optJSONArray("content");
            if (content != null) {
                for (int j = 0; j < content.length(); j++) {
                    JSONObject paraObj = content.getJSONObject(j);
                    Paragraph p = new Paragraph();
                    p.setChapterId(chapterId);
                    p.setParagraphNo(paraObj.getInt("paragraph"));
                    p.setOriginalText(paraObj.optString("original", ""));
                    p.setPinyinText(paraObj.optString("pinyin", ""));
                    p.setTranslation(paraObj.optString("translation", ""));
                    p.setSortOrder(j);
                    dbHelper.insertParagraph(p);
                }
            }
        }
    }
}
