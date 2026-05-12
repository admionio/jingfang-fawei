package cn.lanzp.hdnj.reader;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
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

        // 添加篇目标题头
        if (chapter != null) {
            View titleHeader = getLayoutInflater().inflate(R.layout.item_paragraph, llContent, false);
            TextView tvOriginal = titleHeader.findViewById(R.id.tvOriginal);
            TextView tvPinyin = titleHeader.findViewById(R.id.tvPinyin);
            TextView tvTranslation = titleHeader.findViewById(R.id.tvTranslation);
            View divider = titleHeader.findViewById(R.id.divider);

            // 隐藏拼音和翻译区域
            tvPinyin.setVisibility(View.GONE);
            tvTranslation.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);

            // 设置篇目标题
            tvOriginal.setText(chapter.getTitle());
            tvOriginal.setTextSize(22f);
            tvOriginal.setTypeface(null, android.graphics.Typeface.BOLD);
            tvOriginal.setPadding(0, 24, 0, 16);
            if (isNightMode) {
                tvOriginal.setTextColor(getColor(R.color.nightTextPrimary));
                titleHeader.setBackgroundColor(getColor(R.color.nightSurface));
            } else {
                tvOriginal.setTextColor(getColor(R.color.textPrimary));
                titleHeader.setBackgroundColor(getColor(R.color.backgroundCard));
            }

            llContent.addView(titleHeader);

            // 添加一个分隔线
            View sepDivider = new View(this);
            LinearLayout.LayoutParams sepLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 1);
            sepLp.setMargins(0, 0, 0, 16);
            sepDivider.setLayoutParams(sepLp);
            sepDivider.setBackgroundColor(isNightMode ? getColor(R.color.nightDivider) : getColor(R.color.divider));
            llContent.addView(sepDivider);
        }

        // 无内容时显示提示
        if (paragraphs.isEmpty()) {
            TextView emptyHint = new TextView(this);
            emptyHint.setText("本章数据待完善");
            emptyHint.setTextSize(16f);
            emptyHint.setTextColor(getColor(R.color.textSecondary));
            emptyHint.setGravity(android.view.Gravity.CENTER);
            emptyHint.setPadding(0, 32, 0, 0);
            llContent.addView(emptyHint);
            scrollView.scrollTo(0, 0);
            return;
        }

        int targetChildIndex = -1;
        // 实际段落从llContent的第2个子View开始（0=标题头，1=分隔线）
        final int contentStartIndex = (chapter != null) ? 2 : 0;

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
                targetChildIndex = contentStartIndex + i;
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);
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

    private float touchStartX = 0;
    private boolean isSwiping = false;

    private void setupSwipeGesture() {
        scrollView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    isSwiping = false;
                    break;
                case MotionEvent.ACTION_MOVE: {
                    float diffX = event.getX() - touchStartX;
                    float absDiffX = Math.abs(diffX);
                    float diffY = Math.abs(event.getY() - touchStartX);

                    // 水平移动 > 垂直移动 且 水平移动超过阈值，进入翻页模式
                    if (absDiffX > diffY && absDiffX > 20) {
                        if (!isSwiping) {
                            isSwiping = true;
                            // 禁用ScrollView的垂直滚动
                            scrollView.requestDisallowInterceptTouchEvent(true);
                        }
                        // 手指跟随：让内容跟着手指移动
                        float translateX = diffX * 0.6f;
                        scrollView.setTranslationX(translateX);

                        // 缩放效果：根据滑动距离
                        float progress = Math.min(absDiffX / scrollView.getWidth(), 1f);
                        float scale = 1f - progress * 0.15f;
                        scrollView.setScaleX(scale);
                        scrollView.setScaleY(scale);
                        return true;
                    }
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    if (isSwiping) {
                        float diffX = event.getX() - touchStartX;
                        float absDiffX = Math.abs(diffX);
                        float threshold = scrollView.getWidth() * 0.30f;

                        // 判断是否触发翻页（>30%屏幕宽度）
                        if (absDiffX > threshold) {
                            // 触发翻页
                            int direction = (diffX > 0) ? -1 : 1;

                            // 检查是否可以翻页
                            int newIndex = currentChapterIndex + direction;
                            if (newIndex < 0 || newIndex >= allChapterIds.size()) {
                                // 不可翻页，回弹
                                bounceBackScrollView();
                                return true;
                            }

                            // 补全翻页动画
                            completePageTurn(direction);
                        } else {
                            // 不足30%，回弹
                            bounceBackScrollView();
                        }

                        isSwiping = false;
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        return true;
                    }
                    break;
                }
            }
            return false;
        });
        scrollView.setLongClickable(true);
    }

    private void bounceBackScrollView() {
        scrollView.animate()
                .translationX(0f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void completePageTurn(int direction) {
        float targetX = (direction > 0) ? -scrollView.getWidth() : scrollView.getWidth();

        scrollView.animate()
                .translationX(targetX)
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // 切换内容
                    int newIndex = currentChapterIndex + direction;
                    if (newIndex < 0 || newIndex >= allChapterIds.size()) return;

                    long newChapterId = allChapterIds.get(newIndex);
                    chapterId = newChapterId;
                    currentChapterIndex = newIndex;

                    String volume = chapter != null ? chapter.getVolume() : "素问";
                    Chapter newChapter = dbHelper.getChapterById(newChapterId);
                    chapter = newChapter;
                    String title = newChapter != null ? newChapter.getDisplayName() : "";

                    tvChapterTitle.setText(title);
                    paragraphs = dbHelper.getParagraphsByChapter(newChapterId);
                    updateFavoriteIcon();
                    updateNavigationButtons();

                    // 重置状态
                    scrollView.setTranslationX(0f);
                    scrollView.setScaleX(1f);
                    scrollView.setScaleY(1f);
                    renderContent();

                    // 新内容从相反方向滑入
                    float startX = (direction > 0) ? 0.4f * scrollView.getWidth() : -0.4f * scrollView.getWidth();
                    scrollView.setTranslationX(startX);
                    scrollView.setScaleX(0.85f);
                    scrollView.setScaleY(0.85f);

                    scrollView.animate()
                            .translationX(0f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(250)
                            .setInterpolator(new DecelerateInterpolator())
                            .start();
                })
                .start();
    }

    private void navigateChapter(int direction) {
        int newIndex = currentChapterIndex + direction;
        if (newIndex < 0 || newIndex >= allChapterIds.size()) return;

        final long newChapterId = allChapterIds.get(newIndex);
        final int oldIndex = currentChapterIndex;

        // 动画：当前内容滑出
        float exitX = (direction > 0) ? -scrollView.getWidth() : scrollView.getWidth();
        float exitScale = 0.85f;
        PropertyValuesHolder transX = PropertyValuesHolder.ofFloat("translationX", 0f, exitX);
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, exitScale);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, exitScale);
        ObjectAnimator exitAnim = ObjectAnimator.ofPropertyValuesHolder(scrollView, transX, scaleX, scaleY);
        exitAnim.setDuration(200);
        exitAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        exitAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 切换内容
                chapterId = newChapterId;
                currentChapterIndex = newIndex;

                String volume = chapter != null ? chapter.getVolume() : "素问";
                Chapter newChapter = dbHelper.getChapterById(newChapterId);
                chapter = newChapter;
                String title = newChapter != null ? newChapter.getDisplayName() : "";

                tvChapterTitle.setText(title);
                paragraphs = dbHelper.getParagraphsByChapter(newChapterId);
                updateFavoriteIcon();
                updateNavigationButtons();

                scrollView.setTranslationX(0f);
                scrollView.setScaleX(1f);
                scrollView.setScaleY(1f);
                renderContent();

                // 新内容滑入
                float startX = (direction > 0) ? 0.5f * scrollView.getWidth() : -0.5f * scrollView.getWidth();
                scrollView.setTranslationX(startX);
                scrollView.setScaleX(0.85f);
                scrollView.setScaleY(0.85f);

                PropertyValuesHolder inTransX = PropertyValuesHolder.ofFloat("translationX", startX, 0f);
                PropertyValuesHolder inScaleX = PropertyValuesHolder.ofFloat("scaleX", 0.85f, 1f);
                PropertyValuesHolder inScaleY = PropertyValuesHolder.ofFloat("scaleY", 0.85f, 1f);
                ObjectAnimator enterAnim = ObjectAnimator.ofPropertyValuesHolder(scrollView, inTransX, inScaleX, inScaleY);
                enterAnim.setDuration(250);
                enterAnim.setInterpolator(new DecelerateInterpolator());
                enterAnim.start();
            }
        });
        exitAnim.start();
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
