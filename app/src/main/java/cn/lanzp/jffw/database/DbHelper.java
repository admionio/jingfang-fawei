package cn.lanzp.jffw.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.lanzp.jffw.model.Chapter;
import cn.lanzp.jffw.model.Favorite;
import cn.lanzp.jffw.model.Paragraph;

public class DbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "jingfang_fawei.db";
    private static final int DB_VERSION = 1;

    private static DbHelper instance;

    private static final String CREATE_CHAPTERS =
            "CREATE TABLE IF NOT EXISTS chapters (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "volume TEXT NOT NULL," +
            "chapter_no INTEGER NOT NULL," +
            "title TEXT NOT NULL," +
            "chapter_tag TEXT DEFAULT ''," +
            "sort_order INTEGER NOT NULL DEFAULT 0)";

    private static final String CREATE_PARAGRAPHS =
            "CREATE TABLE IF NOT EXISTS paragraphs (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "chapter_id INTEGER NOT NULL," +
            "paragraph_no INTEGER NOT NULL," +
            "original_text TEXT," +
            "annotation TEXT," +
            "sort_order INTEGER DEFAULT 0," +
            "FOREIGN KEY (chapter_id) REFERENCES chapters(id))";

    private static final String CREATE_FAVORITES =
            "CREATE TABLE IF NOT EXISTS favorites (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "chapter_id INTEGER NOT NULL UNIQUE," +
            "created_at TEXT NOT NULL," +
            "FOREIGN KEY (chapter_id) REFERENCES chapters(id))";

    private static final String CREATE_BOOKMARKS =
            "CREATE TABLE IF NOT EXISTS bookmarks (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "chapter_id INTEGER NOT NULL," +
            "paragraph_no INTEGER NOT NULL," +
            "excerpt TEXT," +
            "created_at TEXT NOT NULL," +
            "FOREIGN KEY (chapter_id) REFERENCES chapters(id))";

    private DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static synchronized DbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CHAPTERS);
        db.execSQL(CREATE_PARAGRAPHS);
        db.execSQL(CREATE_FAVORITES);
        db.execSQL(CREATE_BOOKMARKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bookmarks");
        db.execSQL("DROP TABLE IF EXISTS favorites");
        db.execSQL("DROP TABLE IF EXISTS paragraphs");
        db.execSQL("DROP TABLE IF EXISTS chapters");
        onCreate(db);
    }

    // ==================== Chapters ====================

    public long insertChapter(Chapter chapter) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO chapters (volume, chapter_no, title, chapter_tag, sort_order) VALUES (?, ?, ?, ?, ?)",
                new Object[]{chapter.getVolume(), chapter.getChapterNo(),
                        chapter.getTitle(), chapter.getChapterTag(), chapter.getSortOrder()});
        Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);
        long id = c.moveToFirst() ? c.getLong(0) : -1;
        c.close();
        return id;
    }

    public void clearChapters() {
        getWritableDatabase().execSQL("DELETE FROM chapters");
    }

    public List<Chapter> getChaptersByVolume(String volume) {
        List<Chapter> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT c.id, c.volume, c.chapter_no, c.title, c.chapter_tag, c.sort_order, " +
                "(CASE WHEN f.id IS NOT NULL THEN 1 ELSE 0 END) as is_fav " +
                "FROM chapters c LEFT JOIN favorites f ON c.id = f.chapter_id " +
                "WHERE c.volume = ? ORDER BY c.sort_order",
                new String[]{volume});
        while (c.moveToNext()) {
            Chapter ch = new Chapter();
            ch.setId(c.getLong(0));
            ch.setVolume(c.getString(1));
            ch.setChapterNo(c.getInt(2));
            ch.setTitle(c.getString(3));
            ch.setChapterTag(c.getString(4));
            ch.setSortOrder(c.getInt(5));
            ch.setFavorite(c.getInt(6) == 1);
            list.add(ch);
        }
        c.close();
        return list;
    }

    public List<Chapter> getAllChapters() {
        List<Chapter> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT c.id, c.volume, c.chapter_no, c.title, c.chapter_tag, c.sort_order, " +
                "(CASE WHEN f.id IS NOT NULL THEN 1 ELSE 0 END) as is_fav " +
                "FROM chapters c LEFT JOIN favorites f ON c.id = f.chapter_id " +
                "WHERE c.volume = '伤寒发微' ORDER BY c.sort_order", null);
        while (c.moveToNext()) {
            Chapter ch = new Chapter();
            ch.setId(c.getLong(0));
            ch.setVolume(c.getString(1));
            ch.setChapterNo(c.getInt(2));
            ch.setTitle(c.getString(3));
            ch.setChapterTag(c.getString(4));
            ch.setSortOrder(c.getInt(5));
            ch.setFavorite(c.getInt(6) == 1);
            list.add(ch);
        }
        c.close();

        c = db.rawQuery(
                "SELECT c.id, c.volume, c.chapter_no, c.title, c.chapter_tag, c.sort_order, " +
                "(CASE WHEN f.id IS NOT NULL THEN 1 ELSE 0 END) as is_fav " +
                "FROM chapters c LEFT JOIN favorites f ON c.id = f.chapter_id " +
                "WHERE c.volume = '金匮发微' ORDER BY c.sort_order", null);
        while (c.moveToNext()) {
            Chapter ch = new Chapter();
            ch.setId(c.getLong(0));
            ch.setVolume(c.getString(1));
            ch.setChapterNo(c.getInt(2));
            ch.setTitle(c.getString(3));
            ch.setChapterTag(c.getString(4));
            ch.setSortOrder(c.getInt(5));
            ch.setFavorite(c.getInt(6) == 1);
            list.add(ch);
        }
        c.close();
        return list;
    }

    public int getChapterCount(String volume) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM chapters WHERE volume = ?", new String[]{volume});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public Chapter getChapterById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM chapters WHERE id = ?", new String[]{String.valueOf(id)});
        Chapter ch = null;
        if (c.moveToFirst()) {
            ch = new Chapter();
            ch.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            ch.setVolume(c.getString(c.getColumnIndexOrThrow("volume")));
            ch.setChapterNo(c.getInt(c.getColumnIndexOrThrow("chapter_no")));
            ch.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
            ch.setChapterTag(c.getString(c.getColumnIndexOrThrow("chapter_tag")));
            ch.setSortOrder(c.getInt(c.getColumnIndexOrThrow("sort_order")));
        }
        c.close();
        return ch;
    }

    // ==================== Paragraphs ====================

    public void insertParagraph(Paragraph p) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO paragraphs (chapter_id, paragraph_no, original_text, annotation, sort_order) VALUES (?, ?, ?, ?, ?)",
                new Object[]{p.getChapterId(), p.getParagraphNo(), p.getOriginalText(),
                        p.getAnnotation(), p.getSortOrder()});
    }

    public void clearParagraphs() {
        getWritableDatabase().execSQL("DELETE FROM paragraphs");
    }

    public List<Paragraph> getParagraphsByChapter(long chapterId) {
        List<Paragraph> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM paragraphs WHERE chapter_id = ? ORDER BY sort_order",
                new String[]{String.valueOf(chapterId)});
        while (c.moveToNext()) {
            Paragraph p = new Paragraph();
            p.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            p.setChapterId(c.getLong(c.getColumnIndexOrThrow("chapter_id")));
            p.setParagraphNo(c.getInt(c.getColumnIndexOrThrow("paragraph_no")));
            p.setOriginalText(c.getString(c.getColumnIndexOrThrow("original_text")));
            p.setAnnotation(c.getString(c.getColumnIndexOrThrow("annotation")));
            p.setSortOrder(c.getInt(c.getColumnIndexOrThrow("sort_order")));
            list.add(p);
        }
        c.close();
        return list;
    }

    // ==================== Search ====================

    public static class SearchResult {
        public long chapterId;
        public String volume;
        public String chapterTitle;
        public String chapterTitleClean;
        public String matchedText;
        public String matchedField; // "original" or "annotation"
        public int paragraphNo;

        public String getDisplayText() {
            String excerpt = matchedText.length() > 60 ? matchedText.substring(0, 60) + "…" : matchedText;
            return "(" + volume + ") " + chapterTitle + "\n" + excerpt;
        }
    }

    public List<SearchResult> search(String keyword) {
        List<SearchResult> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String like = "%" + keyword + "%";

        Cursor c = db.rawQuery(
                "SELECT c.id, c.volume, c.title, c.chapter_tag, p.paragraph_no, p.original_text, p.annotation " +
                "FROM paragraphs p JOIN chapters c ON p.chapter_id = c.id " +
                "WHERE p.original_text LIKE ? OR p.annotation LIKE ? " +
                "ORDER BY c.sort_order, p.sort_order LIMIT 100",
                new String[]{like, like});

        while (c.moveToNext()) {
            SearchResult sr = new SearchResult();
            sr.chapterId = c.getLong(0);
            sr.volume = c.getString(1);
            sr.chapterTitle = c.getString(2);
            sr.chapterTitleClean = c.getString(2);
            sr.paragraphNo = c.getInt(4);

            String original = c.getString(5);
            String annotation = c.getString(6);

            if (original != null && original.contains(keyword)) {
                sr.matchedText = original;
                sr.matchedField = "original";
            } else if (annotation != null && annotation.contains(keyword)) {
                sr.matchedText = annotation;
                sr.matchedField = "annotation";
            } else {
                continue;
            }
            results.add(sr);
        }
        c.close();

        return results;
    }

    // ==================== Favorites ====================

    public boolean toggleFavorite(long chapterId) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM favorites WHERE chapter_id = ?", new String[]{String.valueOf(chapterId)});
        if (c.moveToFirst()) {
            db.execSQL("DELETE FROM favorites WHERE chapter_id = ?", new Object[]{chapterId});
            c.close();
            return false;
        }
        c.close();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        db.execSQL("INSERT INTO favorites (chapter_id, created_at) VALUES (?, ?)", new Object[]{chapterId, now});
        return true;
    }

    public boolean isFavorite(long chapterId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM favorites WHERE chapter_id = ?", new String[]{String.valueOf(chapterId)});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public List<Favorite> getFavorites() {
        List<Favorite> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT f.id, f.chapter_id, c.title, c.chapter_tag, c.volume, f.created_at " +
                "FROM favorites f JOIN chapters c ON f.chapter_id = c.id " +
                "ORDER BY f.created_at DESC", null);
        while (c.moveToNext()) {
            Favorite fav = new Favorite();
            fav.setId(c.getLong(0));
            fav.setChapterId(c.getLong(1));
            fav.setChapterTitle(c.getString(2));
            fav.setVolume(c.getString(4));
            fav.setCreatedAt(c.getString(5));
            list.add(fav);
        }
        c.close();
        return list;
    }

    public void removeFavorite(long chapterId) {
        getWritableDatabase().execSQL("DELETE FROM favorites WHERE chapter_id = ?", new Object[]{chapterId});
    }
}
