package cn.lanzp.hdnj;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cn.lanzp.hdnj.database.DataImporter;
import cn.lanzp.hdnj.database.DbHelper;
import cn.lanzp.hdnj.model.Chapter;
import cn.lanzp.hdnj.model.Paragraph;

public class MainActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private TextView tvSuwenCount, tvLingshuCount;
    private TextView tvRecommendTitle, tvRecommendExcerpt;
    private Chapter randomChapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean nightMode = prefs.getBoolean("night_mode", false);
        if (nightMode) {
            setTheme(R.style.Theme_HuangdiNeijing); // Will use night theme
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = DbHelper.getInstance(this);

        // 首次启动导入数据
        if (!DataImporter.isDataImported(this)) {
            DataImporter.importData(this, dbHelper);
        }

        initViews();
        loadData();
        setupNavigation();
    }

    private void initViews() {
        tvSuwenCount = findViewById(R.id.tvSuwenCount);
        tvLingshuCount = findViewById(R.id.tvLingshuCount);
        tvRecommendTitle = findViewById(R.id.tvRecommendTitle);
        tvRecommendExcerpt = findViewById(R.id.tvRecommendExcerpt);
    }

    private void loadData() {
        // 加载篇数统计
        int suwenCount = dbHelper.getChapterCount("素问");
        int lingshuCount = dbHelper.getChapterCount("灵枢");
        tvSuwenCount.setText(getString(R.string.chapter_count, suwenCount));
        tvLingshuCount.setText(getString(R.string.chapter_count, lingshuCount));

        // 今日推荐（基于日期的随机）
        loadTodayRecommend();
    }

    private void loadTodayRecommend() {
        List<Chapter> allChapters = dbHelper.getAllChapters();
        if (allChapters.isEmpty()) return;

        // 过滤出有内容的篇章（至少有一段非空原文）
        List<Chapter> validChapters = new ArrayList<>();
        for (Chapter ch : allChapters) {
            List<Paragraph> paras = dbHelper.getParagraphsByChapter(ch.getId());
            if (!paras.isEmpty()) {
                String firstText = paras.get(0).getOriginalText();
                if (firstText != null && !firstText.trim().isEmpty()) {
                    validChapters.add(ch);
                }
            }
        }
        if (validChapters.isEmpty()) return;

        // 基于日期做伪随机，保证同一天推荐同一篇
        String dateStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int seed = Integer.parseInt(dateStr);
        Random rnd = new Random(seed);
        int index = rnd.nextInt(validChapters.size());
        randomChapter = validChapters.get(index);

        tvRecommendTitle.setText(randomChapter.getDisplayName());

        // 获取第一段作为节选
        List<Paragraph> paras =
                dbHelper.getParagraphsByChapter(randomChapter.getId());
        if (!paras.isEmpty()) {
            String text = paras.get(0).getOriginalText();
            if (text != null) {
                if (text.length() > 50) text = text.substring(0, 50) + "…";
                tvRecommendExcerpt.setText(text);
            }
        }
    }

    public void onSearchClick(View v) {
        startActivity(new Intent(this, SearchActivity.class));
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
    }

    public void onRecommendClick(View v) {
        if (randomChapter != null) {
            Intent intent = new Intent(this, cn.lanzp.hdnj.reader.ReaderActivity.class);
            intent.putExtra("chapter_id", randomChapter.getId());
            intent.putExtra("chapter_title", randomChapter.getDisplayName());
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
        }
    }

    public void onSuwenClick(View v) {
        Intent intent = new Intent(this, ChapterListActivity.class);
        intent.putExtra("volume", "素问");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void onLingshuClick(View v) {
        Intent intent = new Intent(this, ChapterListActivity.class);
        intent.putExtra("volume", "灵枢");
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_favorites) {
                startActivity(new Intent(this, FavoritesActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_bookmarks) {
                startActivity(new Intent(this, BookmarksActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }
            return false;
        });
    }
}
