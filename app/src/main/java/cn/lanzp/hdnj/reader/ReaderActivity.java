package cn.lanzp.hdnj.reader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import cn.lanzp.hdnj.R;
import cn.lanzp.hdnj.database.DbHelper;
import cn.lanzp.hdnj.model.Chapter;
import cn.lanzp.hdnj.model.Paragraph;

public class ReaderActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private long chapterId;
    private Chapter chapter;
    private List<Paragraph> paragraphs = new ArrayList<>();
    private List<Long> allChapterIds = new ArrayList<>();
    private int currentChapterIndex = -1;

    private TextView tvChapterTitle;
    private ImageView ivFavorite, ivBack;
    private LinearLayout llContent;
    private TextView tvPrev, tvNext, tvChapterPosition;
    private ScrollView scrollView;

    private int fontSizeLevel = 2;
    private boolean isNightMode = false;
    private SharedPreferences prefs;
    private int targetParagraphNo = -1;

    // Font size mapping
    private static final float[] FONT_SIZES_ORIGINAL = {14f, 16f, 18f, 20f, 22f};
    private static final float[] FONT_SIZES_PINYIN = {11f, 12f, 13f, 14f, 15f};
    private static final float[] FONT_SIZES_TRANSLATION = {12f, 14f, 15f, 17f, 18f};

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        dbHelper = DbHelper.getInstance(this);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        fontSizeLevel = prefs.getInt("font_size", 2);
        isNightMode = prefs.getBoolean("night_mode", false);

        chapterId = getIntent().getLongExtra("chapter_id", -1);
        String chapterTitle = getIntent().getStringExtra("chapter_title");
        targetParagraphNo = getIntent().getIntExtra("paragraph_no", -1);

        initViews();
        loadData(chapterTitle);
        setupListeners();
        setupSwipeGesture();
    }

    private void applyTheme() {
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        if (sp.getBoolean("night_mode", false)) {
            setTheme(R.style.Theme_HuangdiNeijing); // night theme
        }
    }

    private void initViews() {
        tvChapterTitle = findViewById(R.id.tvChapterTitle);
        ivFavorite = findViewById(R.id.ivFavorite);
        ivBack = findViewById(R.id.ivBack);
        llContent = findViewById(R.id.llContent);
        tvPrev = findViewById(R.id.tvPrev);
        tvNext = findViewById(R.id.tvNext);
        tvChapterPosition = findViewById(R.id.tvChapterPosition);
        scrollView = findViewById(R.id.scrollView);

        findViewById(R.id.tvFontSizeBtn).setOnClickListener(v -> showFontSizeDialog());
    }

    private void loadData(String chapterTitle) {
        chapter = dbHelper.getChapterById(chapterId);
        paragraphs = dbHelper.getParagraphsByChapter(chapterId);

        // 加载所有chapter ID用于上下篇导航
        String volume = chapter != null ? chapter.getVolume() : "素问";
        List<Chapter> allChapters = dbHelper.getChaptersByVolume(volume);
        allChapterIds.clear();
        for (int i = 0; i < allChapters.size(); i++) {
            allChapterIds.add(allChapters.get(i).getId());
            if (allChapters.get(i).getId() == chapterId) {
                currentChapterIndex = i;
            }
        }

        if (chapterTitle != null) {
            tvChapterTitle.setText(chapterTitle);
        } else if (chapter != null) {
            tvChapterTitle.setText(chapter.getDisplayName());
        }

        updateFavoriteIcon();
        updateNavigationButtons();
        renderContent();
    }

    private void renderContent() {
        llContent.removeAllViews();

        // 无内容时显示提示
        if (paragraphs.isEmpty()) {
            TextView emptyHint = new TextView(this);
            emptyHint.setText("本章数据待完善");
            emptyHint.setTextSize(16f);
            emptyHint.setTextColor(getColor(R.color.textSecondary));
            emptyHint.setGravity(android.view.Gravity.CENTER);
            emptyHint.setPadding(0, 120, 0, 0);
            llContent.addView(emptyHint);
            scrollView.scrollTo(0, 0);
            return;
        }

        int targetChildIndex = -1;

        for (int i = 0; i < paragraphs.size(); i++) {
            Paragraph p = paragraphs.get(i);
            View itemView = getLayoutInflater().inflate(R.layout.item_paragraph, llContent, false);

            TextView tvPinyin = itemView.findViewById(R.id.tvPinyin);
            TextView tvOriginal = itemView.findViewById(R.id.tvOriginal);
            TextView tvTranslation = itemView.findViewById(R.id.tvTranslation);
            View divider = itemView.findViewById(R.id.divider);

            // 设置文字
            String pinyin = p.getPinyinText();
            if (pinyin != null && !pinyin.isEmpty()) {
                tvPinyin.setText(pinyin);
                tvPinyin.setVisibility(View.VISIBLE);
            } else {
                tvPinyin.setVisibility(View.GONE);
            }

            tvOriginal.setText(p.getOriginalText());
            tvTranslation.setText(p.getTranslation());

            // 设置字体大小
            tvPinyin.setTextSize(FONT_SIZES_PINYIN[fontSizeLevel]);
            tvOriginal.setTextSize(FONT_SIZES_ORIGINAL[fontSizeLevel]);
            tvTranslation.setTextSize(FONT_SIZES_TRANSLATION[fontSizeLevel]);

            // 夜间模式
            if (isNightMode) {
                tvPinyin.setTextColor(getColor(R.color.nightTextPinyin));
                tvOriginal.setTextColor(getColor(R.color.nightTextPrimary));
                tvTranslation.setTextColor(getColor(R.color.nightTextTranslation));
                divider.setBackgroundColor(getColor(R.color.nightDivider));
                itemView.setBackgroundColor(getColor(R.color.nightSurface));
            } else {
                tvPinyin.setTextColor(getColor(R.color.textPinyin));
                tvOriginal.setTextColor(getColor(R.color.textPrimary));
                tvTranslation.setTextColor(getColor(R.color.textTranslation));
                divider.setBackgroundColor(getColor(R.color.divider));
            }

            llContent.addView(itemView);

            // 记录目标段落的child index
            if (targetParagraphNo >= 0 && p.getParagraphNo() == targetParagraphNo) {
                targetChildIndex = i;
            }
        }

        // 滚动到目标段落（书签定位）
        final int indexToScroll = targetChildIndex;
        if (targetParagraphNo >= 0 && indexToScroll >= 0) {
            scrollView.post(() -> {
                // 等待布局完成后计算位置
                int y = 0;
                for (int i = 0; i < indexToScroll && i < llContent.getChildCount(); i++) {
                    y += llContent.getChildAt(i).getHeight();
                }
                scrollView.scrollTo(0, y);
            });
        } else {
            // 默认滚动到顶部
            scrollView.scrollTo(0, 0);
        }
    }

    private void updateFavoriteIcon() {
        boolean isFav = dbHelper.isFavorite(chapterId);
        ivFavorite.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        ivFavorite.setTag(isFav);
    }

    private void updateNavigationButtons() {
        if (allChapterIds.size() <= 1) {
            tvPrev.setVisibility(View.GONE);
            tvNext.setVisibility(View.GONE);
            tvChapterPosition.setVisibility(View.GONE);
            return;
        }

        tvPrev.setVisibility(View.VISIBLE);
        tvNext.setVisibility(View.VISIBLE);
        tvChapterPosition.setVisibility(View.VISIBLE);

        tvPrev.setEnabled(currentChapterIndex > 0);
        tvNext.setEnabled(currentChapterIndex < allChapterIds.size() - 1);
        tvPrev.setAlpha(currentChapterIndex > 0 ? 1f : 0.3f);
        tvNext.setAlpha(currentChapterIndex < allChapterIds.size() - 1 ? 1f : 0.3f);

        tvChapterPosition.setText((currentChapterIndex + 1) + "/" + allChapterIds.size());
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        ivFavorite.setOnClickListener(v -> {
            boolean added = dbHelper.toggleFavorite(chapterId);
            updateFavoriteIcon();
            String msg = added ? "已收藏" : "已取消收藏";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.ivBookmark).setOnClickListener(v -> addBookmark());

        tvPrev.setOnClickListener(v -> navigateChapter(-1));
        tvNext.setOnClickListener(v -> navigateChapter(1));
    }

    private void setupSwipeGesture() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                // 只处理水平方向，垂直方向忽略
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD
                            && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // 右滑 → 上一篇
                            navigateChapter(-1);
                        } else {
                            // 左滑 → 下一篇
                            navigateChapter(1);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        scrollView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // 不消费事件，让 ScrollView 继续处理垂直滚动
        });
        scrollView.setLongClickable(true);
    }

    private void navigateChapter(int direction) {
        int newIndex = currentChapterIndex + direction;
        if (newIndex < 0 || newIndex >= allChapterIds.size()) return;

        long newChapterId = allChapterIds.get(newIndex);
        chapterId = newChapterId;
        currentChapterIndex = newIndex;

        String volume = chapter != null ? chapter.getVolume() : "素问";
        Chapter newChapter = dbHelper.getChapterById(newChapterId);
        String title = newChapter != null ? newChapter.getDisplayName() : "";

        tvChapterTitle.setText(title);
        paragraphs = dbHelper.getParagraphsByChapter(newChapterId);
        updateFavoriteIcon();
        updateNavigationButtons();
        renderContent();
    }

    private void showFontSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_font_size, null);
        SeekBar seekBar = view.findViewById(R.id.sbFontSizeDialog);
        seekBar.setMax(4);
        seekBar.setProgress(fontSizeLevel);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fontSizeLevel = progress;
                applyFontSizeToContent();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        builder.setView(view)
                .setTitle("字体大小")
                .setPositiveButton("确定", (dialog, which) -> {
                    prefs.edit().putInt("font_size", fontSizeLevel).apply();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void applyFontSizeToContent() {
        for (int i = 0; i < llContent.getChildCount(); i++) {
            View item = llContent.getChildAt(i);
            if (item instanceof LinearLayout) {
                TextView tvPinyin = item.findViewById(R.id.tvPinyin);
                TextView tvOriginal = item.findViewById(R.id.tvOriginal);
                TextView tvTranslation = item.findViewById(R.id.tvTranslation);
                if (tvPinyin != null) tvPinyin.setTextSize(FONT_SIZES_PINYIN[fontSizeLevel]);
                if (tvOriginal != null) tvOriginal.setTextSize(FONT_SIZES_ORIGINAL[fontSizeLevel]);
                if (tvTranslation != null) tvTranslation.setTextSize(FONT_SIZES_TRANSLATION[fontSizeLevel]);
            }
        }
    }

    private void addBookmark() {
        // 获取当前可见段落
        int visibleParaNo = 1;
        if (!paragraphs.isEmpty()) {
            // 估算可见段：取第一个段落的编号
            visibleParaNo = paragraphs.get(0).getParagraphNo();
        }

        String excerpt = !paragraphs.isEmpty() ? paragraphs.get(0).getOriginalText() : "";
        long result = dbHelper.addBookmark(chapterId, visibleParaNo, excerpt);

        if (result < 0) {
            Toast.makeText(this, R.string.bookmark_exists, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.bookmark_added, Toast.LENGTH_SHORT).show();
        }
    }
}
