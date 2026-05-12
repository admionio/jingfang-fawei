package cn.lanzp.hdnj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.lanzp.hdnj.adapter.ChapterAdapter;
import cn.lanzp.hdnj.database.DbHelper;
import cn.lanzp.hdnj.model.Chapter;

public class ChapterListActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private RecyclerView rvChapters;
    private ChapterAdapter adapter;
    private List<Chapter> chapters;
    private String volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        volume = getIntent().getStringExtra("volume");
        if (volume == null) volume = "素问";

        dbHelper = DbHelper.getInstance(this);

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
        adapter = new ChapterAdapter(this, chapters, new ChapterAdapter.OnChapterClickListener() {
            @Override
            public void onChapterClick(Chapter chapter) {
                Intent intent = new Intent(ChapterListActivity.this,
                        cn.lanzp.hdnj.reader.ReaderActivity.class);
                intent.putExtra("chapter_id", chapter.getId());
                intent.putExtra("chapter_title", chapter.getDisplayName());
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
