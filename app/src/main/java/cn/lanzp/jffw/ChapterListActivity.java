package cn.lanzp.jffw;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.lanzp.jffw.adapter.ChapterAdapter;
import cn.lanzp.jffw.database.DbHelper;
import cn.lanzp.jffw.model.Chapter;

public class ChapterListActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private RecyclerView rvChapters;
    private ChapterAdapter adapter;
    private List<Chapter> chapters;
    private String volume;
    private SharedPreferences readPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(prefs.getBoolean("night_mode", false) ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        volume = getIntent().getStringExtra("volume");
        if (volume == null) volume = "伤寒发微";

        dbHelper = DbHelper.getInstance(this);
        readPrefs = getSharedPreferences("reading_progress", MODE_PRIVATE);

        TextView tvTitle = findViewById(R.id.tvToolbarTitle);
        tvTitle.setText(volume);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        rvChapters = findViewById(R.id.rvChapters);
        rvChapters.setLayoutManager(new LinearLayoutManager(this));

        loadChapters();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
    }

    private void loadChapters() {
        chapters = dbHelper.getChaptersByVolume(volume);
        // 填充已读状态
        for (Chapter chapter : chapters) {
            chapter.setRead(readPrefs.getBoolean("read_chapter_" + chapter.getId(), false));
        }
        adapter = new ChapterAdapter(this, chapters, new ChapterAdapter.OnChapterClickListener() {
            @Override
            public void onChapterClick(Chapter chapter) {
                Intent intent = new Intent(ChapterListActivity.this,
                        cn.lanzp.jffw.reader.ReaderActivity.class);
                intent.putExtra("chapter_id", chapter.getId());
                intent.putExtra("chapter_title", chapter.getTitle());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
            }

            @Override
            public void onFavoriteClick(Chapter chapter, int position) {
                boolean added = dbHelper.toggleFavorite(chapter.getId());
                chapter.setFavorite(added);
                adapter.updateItem(position, chapter);
            }
        });
        rvChapters.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新数据（可能在其他页面修改了收藏状态）
        loadChapters();
    }
}
