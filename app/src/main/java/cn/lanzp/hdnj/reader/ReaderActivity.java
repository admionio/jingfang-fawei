package cn.lanzp.hdnj.reader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.ArrayList;
import java.util.List;

import cn.lanzp.hdnj.R;
import cn.lanzp.hdnj.database.DbHelper;
import cn.lanzp.hdnj.model.Chapter;
import cn.lanzp.hdnj.model.Paragraph;

public class ReaderActivity extends AppCompatActivity {

    private static final String TAG = "ReaderActivity";

    private DbHelper dbHelper;
    private long chapterId;
    private Chapter chapter;
    private List<Paragraph> paragraphs = new ArrayList<>();
    private List<Long> allChapterIds = new ArrayList<>();
    private int currentChapterIndex = -1;

    private TextView tvChapterTitle;
    private ImageView ivFavorite, ivBack, ivSearch;
    private WebView wvArticle;
    private TextView tvPrev, tvNext, tvChapterPosition;

    private int fontSizeLevel = 2;
    private boolean isNightMode = false;
    private SharedPreferences prefs;
    private int targetParagraphNo = -1;

    // 搜索结果计数
    private int searchMatchCount = 0;
    private String currentSearchKeyword = "";

    // Font size mapping (px)
    private static final float[] FONT_SIZES_ORIGINAL = {14f, 16f, 18f, 20f, 22f};
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
    }

    private void applyTheme() {
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(sp.getBoolean("night_mode", false) ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    public void updateNightMode(boolean nightMode) {
        this.isNightMode = nightMode;
        renderContent();
    }

    private void initViews() {
        tvChapterTitle = findViewById(R.id.tvChapterTitle);
        ivFavorite = findViewById(R.id.ivFavorite);
        ivBack = findViewById(R.id.ivBack);
        ivSearch = findViewById(R.id.ivSearch);
        wvArticle = findViewById(R.id.wvArticle);
        tvPrev = findViewById(R.id.tvPrev);
        tvNext = findViewById(R.id.tvNext);
        tvChapterPosition = findViewById(R.id.tvChapterPosition);

        // 配置WebView
        WebSettings ws = wvArticle.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(false);
        ws.setBuiltInZoomControls(false);
        ws.setDisplayZoomControls(false);
        ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        wvArticle.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        wvArticle.setHorizontalScrollBarEnabled(false);
        wvArticle.setVerticalScrollBarEnabled(false);
        wvArticle.setOverScrollMode(View.OVER_SCROLL_NEVER);

        wvArticle.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        findViewById(R.id.tvFontSizeBtn).setOnClickListener(v -> showFontSizeDialog());
    }

    private void loadData(String chapterTitle) {
        chapter = dbHelper.getChapterById(chapterId);
        paragraphs = dbHelper.getParagraphsByChapter(chapterId);

        // 标记已读
        markChapterAsRead(chapterId);

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
            tvChapterTitle.setText(chapter.getTitle());
        }

        updateFavoriteIcon();
        updateNavigationButtons();
        renderContent();
    }

    private void loadChapterData(int newIndex) {
        if (newIndex < 0 || newIndex >= allChapterIds.size()) return;

        chapterId = allChapterIds.get(newIndex);
        currentChapterIndex = newIndex;

        Chapter newChapter = dbHelper.getChapterById(chapterId);
        chapter = newChapter;
        paragraphs = dbHelper.getParagraphsByChapter(chapterId);

        // 标记已读
        markChapterAsRead(chapterId);

        String title = newChapter != null ? newChapter.getTitle() : "";
        tvChapterTitle.setText(title);

        updateFavoriteIcon();
        updateNavigationButtons();

        Log.d(TAG, "loadChapterData: chapter=" + (newChapter != null ? newChapter.getTitle() : "null")
                + " paragraphs=" + paragraphs.size());

        renderContent();
    }

    private void renderContent() {
        Log.d(TAG, "renderContent: paragraphs=" + paragraphs.size());

        // 无内容时显示提示
        if (paragraphs.isEmpty()) {
            wvArticle.loadDataWithBaseURL(null,
                    "<html><body style='text-align:center;padding-top:32px;'>本章数据待完善</body></html>",
                    "text/html", "UTF-8", null);
            return;
        }

        String chapterTitle = (chapter != null) ? chapter.getTitle() : "";

        // 获取颜色值
        int textColorInt = isNightMode ? getColor(R.color.nightTextPrimary) : getColor(R.color.textPrimary);
        int pinyinColorInt = isNightMode ? getColor(R.color.nightTextPinyin) : getColor(R.color.textPinyin);
        int translationColorInt = isNightMode ? getColor(R.color.nightTextTranslation) : getColor(R.color.textTranslation);
        int labelColorInt = isNightMode ? getColor(R.color.nightLabelOriginal) : getColor(R.color.labelOriginal);
        int dividerColorInt = isNightMode ? getColor(R.color.nightDivider) : getColor(R.color.divider);
        int bgColorInt = isNightMode ? getColor(R.color.nightSurface) : getColor(R.color.backgroundCard);

        String textColorHex = colorToHex(textColorInt);
        String pinyinColorHex = colorToHex(pinyinColorInt);
        String translationColorHex = colorToHex(translationColorInt);
        String labelColorHex = colorToHex(labelColorInt);
        String dividerColorHex = colorToHex(dividerColorInt);
        String bgColorHex = colorToHex(bgColorInt);

        int fontSizePx = Math.round(FONT_SIZES_ORIGINAL[fontSizeLevel]);

        String html = PinyinHtmlBuilder.buildFullArticleHtml(
                chapterTitle, paragraphs,
                fontSizePx,
                textColorHex, pinyinColorHex,
                translationColorHex,
                labelColorHex,
                dividerColorHex,
                bgColorHex
        );

        wvArticle.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

        // 滚动到目标段落（定位跳转）
        if (targetParagraphNo >= 0) {
            final int paraNo = targetParagraphNo;
            wvArticle.postDelayed(() -> {
                wvArticle.evaluateJavascript(
                    "(function(){" +
                    "  var el = document.getElementById('para-" + paraNo + "');" +
                    "  if (el) { el.scrollIntoView(true); return 'ok'; }" +
                    "  return 'notfound';" +
                    "})()",
                    null
                );
            }, 300);
            targetParagraphNo = -1;
        }
    }

    // ================================================================
    // 当前页文字搜索功能
    // ================================================================

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout searchLayout = new LinearLayout(this);
        searchLayout.setOrientation(LinearLayout.VERTICAL);
        searchLayout.setPadding(32, 16, 32, 16);

        // 搜索输入框
        final EditText etSearch = new EditText(this);
        etSearch.setHint("输入搜索文字");
        etSearch.setText(currentSearchKeyword);
        etSearch.setTextSize(16);
        etSearch.setSingleLine(true);
        searchLayout.addView(etSearch);

        // 搜索结果计数
        final TextView tvSearchCount = new TextView(this);
        tvSearchCount.setTextSize(14);
        tvSearchCount.setPadding(0, 8, 0, 8);
        tvSearchCount.setVisibility(View.GONE);
        searchLayout.addView(tvSearchCount);

        // 按钮行
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setPadding(0, 8, 0, 0);

        final TextView btnPrev = new TextView(this);
        btnPrev.setText("◀ 上一个");
        btnPrev.setTextSize(15);
        btnPrev.setTextColor(getColor(R.color.colorPrimary));
        btnPrev.setPadding(16, 8, 16, 8);
        btnPrev.setClickable(true);
        btnPrev.setFocusable(true);
        btnPrev.setBackgroundResource(android.R.drawable.list_selector_background);

        final TextView btnNext = new TextView(this);
        btnNext.setText("下一个 ▶");
        btnNext.setTextSize(15);
        btnNext.setTextColor(getColor(R.color.colorPrimary));
        btnNext.setPadding(16, 8, 16, 8);
        btnNext.setClickable(true);
        btnNext.setFocusable(true);
        btnNext.setBackgroundResource(android.R.drawable.list_selector_background);

        btnLayout.addView(btnPrev);
        btnLayout.addView(btnNext);
        searchLayout.addView(btnLayout);

        builder.setView(searchLayout)
                .setTitle("搜索当前页面")
                .setPositiveButton("关闭", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // 搜索文本变化时执行WebView搜索
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                currentSearchKeyword = keyword;
                if (keyword.isEmpty()) {
                    wvArticle.clearMatches();
                    tvSearchCount.setVisibility(View.GONE);
                    searchMatchCount = 0;
                    return;
                }
                wvArticle.findAllAsync(keyword);
                wvArticle.postDelayed(() -> {
                    wvArticle.findNext(false);
                    wvArticle.post(() -> {
                        // 获取匹配数
                        wvArticle.evaluateJavascript(
                            "document.querySelectorAll('span[class=webview-highlight]').length",
                            value -> {
                                try {
                                    searchMatchCount = Integer.parseInt(value.replace("\"", ""));
                                    if (searchMatchCount > 0) {
                                        tvSearchCount.setText("找到 " + searchMatchCount + " 处匹配");
                                        tvSearchCount.setVisibility(View.VISIBLE);
                                    } else {
                                        tvSearchCount.setText("未找到匹配");
                                        tvSearchCount.setVisibility(View.VISIBLE);
                                    }
                                } catch (Exception e) {
                                    tvSearchCount.setVisibility(View.GONE);
                                }
                            }
                        );
                    });
                }, 500);
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (!currentSearchKeyword.isEmpty()) {
                wvArticle.findNext(true);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (!currentSearchKeyword.isEmpty()) {
                wvArticle.findNext(false);
            }
        });
    }

    // ================================================================
    // 辅助方法
    // ================================================================

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static String colorToHex(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
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

        ivSearch.setOnClickListener(v -> showSearchDialog());

        tvPrev.setOnClickListener(v -> {
            if (currentChapterIndex > 0) {
                loadChapterData(currentChapterIndex - 1);
            }
        });

        tvNext.setOnClickListener(v -> {
            if (currentChapterIndex < allChapterIds.size() - 1) {
                loadChapterData(currentChapterIndex + 1);
            }
        });
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
        renderContent();
    }

    /**
     * 标记篇章为已读，记录到 SharedPreferences
     */
    private void markChapterAsRead(long chapterId) {
        SharedPreferences readPrefs = getSharedPreferences("reading_progress", MODE_PRIVATE);
        readPrefs.edit().putBoolean("read_chapter_" + chapterId, true).apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
