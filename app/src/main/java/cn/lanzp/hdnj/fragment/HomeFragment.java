package cn.lanzp.hdnj.fragment;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cn.lanzp.hdnj.ChapterListActivity;
import cn.lanzp.hdnj.R;
import cn.lanzp.hdnj.SearchActivity;
import cn.lanzp.hdnj.database.DbHelper;
import cn.lanzp.hdnj.model.Chapter;
import cn.lanzp.hdnj.model.Paragraph;
import cn.lanzp.hdnj.reader.ReaderActivity;
import cn.lanzp.hdnj.util.LunarCalendar;

public class HomeFragment extends Fragment {

    private DbHelper dbHelper;
    private TextView tvSuwenCount, tvLingshuCount;
    private TextView tvSuwenProgress, tvLingshuProgress;
    private TextView tvRecommendTitle, tvRecommendExcerpt;
    private TextView tvClock, tvDateSolar, tvDateLunar, tvShichen;
    private ImageView ivNightToggle;
    private Chapter randomChapter;

    // 时钟刷新
    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            updateTimeDisplay();
            clockHandler.postDelayed(this, 1000);
        }
    };

    // 日期和时辰缓存
    private String cachedSolarDate = "";
    private String cachedLunarDate = "";
    private String cachedShichenText = "";
    private int cachedLastMinute = -1;
    private int cachedLastShichenHour = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = DbHelper.getInstance(requireContext());

        tvClock = view.findViewById(R.id.tvClock);
        tvDateSolar = view.findViewById(R.id.tvDateSolar);
        tvDateLunar = view.findViewById(R.id.tvDateLunar);
        tvShichen = view.findViewById(R.id.tvShichen);
        tvSuwenCount = view.findViewById(R.id.tvSuwenCount);
        tvLingshuCount = view.findViewById(R.id.tvLingshuCount);
        tvSuwenProgress = view.findViewById(R.id.tvSuwenProgress);
        tvLingshuProgress = view.findViewById(R.id.tvLingshuProgress);
        tvRecommendTitle = view.findViewById(R.id.tvRecommendTitle);
        tvRecommendExcerpt = view.findViewById(R.id.tvRecommendExcerpt);
        ivNightToggle = view.findViewById(R.id.ivNightToggle);

        loadStatistics();
        loadTodayRecommend();
        updateNightToggleIcon();

        // 搜索点击
        View searchCardView = (View) view.findViewById(R.id.tvSearchHint).getParent().getParent();
        searchCardView.setOnClickListener(v -> onSearchClick());
        view.findViewById(R.id.tvSearchHint).setOnClickListener(v -> onSearchClick());
        view.findViewById(R.id.cvTodayRecommend).setOnClickListener(v -> onRecommendClick());
        view.findViewById(R.id.cvSuwen).setOnClickListener(v -> onSuwenClick());
        view.findViewById(R.id.cvLingshu).setOnClickListener(v -> onLingshuClick());

        // 夜间模式快速切换
        ivNightToggle.setOnClickListener(v -> toggleNightMode());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
        startClock();
        updateNightToggleIcon();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopClock();
    }

    private void startClock() {
        updateTimeDisplay();
        clockHandler.postDelayed(clockRunnable, 1000);
    }

    private void stopClock() {
        clockHandler.removeCallbacks(clockRunnable);
    }

    /**
     * 更新夜间模式切换图标
     */
    private void updateNightToggleIcon() {
        if (ivNightToggle != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
            boolean nightMode = prefs.getBoolean("night_mode", false);
            ivNightToggle.setImageResource(nightMode ? R.drawable.ic_sun : R.drawable.ic_moon);
        }
    }

    /**
     * 切换夜间模式
     */
    private void toggleNightMode() {
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean currentNightMode = prefs.getBoolean("night_mode", false);
        boolean newNightMode = !currentNightMode;

        prefs.edit().putBoolean("night_mode", newNightMode).apply();
        AppCompatDelegate.setDefaultNightMode(newNightMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // 重建 Activity 使主题生效
        requireActivity().recreate();
    }

    /**
     * 更新时间显示（每秒调用）
     */
    private void updateTimeDisplay() {
        Date now = new Date();

        // 第1行：实时时钟（每秒刷新）
        SimpleDateFormat clockFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);
        tvClock.setText(clockFormat.format(now));

        // 第2行：公历日期（分钟变化时刷新）
        Calendar cal = Calendar.getInstance();
        int currentMinute = cal.get(Calendar.MINUTE);
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);

        if (currentMinute != cachedLastMinute) {
            cachedLastMinute = currentMinute;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日  EE", Locale.CHINA);
            String solarDate = sdf.format(now);
            // 将星期简写转为完整星期名
            solarDate = solarDate.replace("星期", "星期")
                    .replace("Mon", "一").replace("Tue", "二").replace("Wed", "三")
                    .replace("Thu", "四").replace("Fri", "五").replace("Sat", "六")
                    .replace("Sun", "日");
            // 处理 locale "EE" 返回中文格式时可能直接是"星期三"
            if (solarDate.contains("星期") && solarDate.length() > 6) {
                // 已经正确，不需要转换
            } else {
                // 用SimpleDateFormat+中文本地化
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
                String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
                int w = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
                solarDate = sdf2.format(now) + " " + weekDays[w];
            }
            cachedSolarDate = solarDate;
            tvDateSolar.setText(cachedSolarDate);

            // 检查时辰是否变化
            updateLunarAndShichen(now, currentHour);
        }
    }

    /**
     * 更新农历和时辰信息
     */
    private void updateLunarAndShichen(Date now, int currentHour) {
        // 农历（跨天或小时变化时刷新）
        if (currentHour != cachedLastShichenHour) {
            cachedLastShichenHour = currentHour;

            LunarCalendar.LunarDate lunar = LunarCalendar.getLunarDate(now);
            cachedLunarDate = "农历 " + lunar.ganzhiYear + "年 " + lunar.monthName + "月" + lunar.dayName;
            tvDateLunar.setText(cachedLunarDate);
        }

        // 时辰
        LunarCalendar.ShichenInfo shichen = LunarCalendar.getShichen(now);
        String shichenText = shichen.name + "时（" + shichen.range + "）";
        if (!shichenText.equals(cachedShichenText)) {
            cachedShichenText = shichenText;
            tvShichen.setText(cachedShichenText);
        }
    }

    private void loadStatistics() {
        int suwenCount = dbHelper.getChapterCount("素问");
        int lingshuCount = dbHelper.getChapterCount("灵枢");
        tvSuwenCount.setText(getString(R.string.chapter_count, suwenCount));
        tvLingshuCount.setText(getString(R.string.chapter_count, lingshuCount));

        // 从 SharedPreferences 读取已读标记
        SharedPreferences readPrefs = requireContext().getSharedPreferences("reading_progress", Context.MODE_PRIVATE);

        List<Chapter> suwenChapters = dbHelper.getChaptersByVolume("素问");
        int suwenReadCount = 0;
        for (Chapter ch : suwenChapters) {
            if (readPrefs.getBoolean("read_chapter_" + ch.getId(), false)) {
                suwenReadCount++;
            }
        }

        List<Chapter> lingshuChapters = dbHelper.getChaptersByVolume("灵枢");
        int lingshuReadCount = 0;
        for (Chapter ch : lingshuChapters) {
            if (readPrefs.getBoolean("read_chapter_" + ch.getId(), false)) {
                lingshuReadCount++;
            }
        }

        tvSuwenProgress.setText("已阅读 " + suwenReadCount + " / " + suwenCount + " 篇");
        tvLingshuProgress.setText("已阅读 " + lingshuReadCount + " / " + lingshuCount + " 篇");
    }

    private void loadTodayRecommend() {
        List<Chapter> allChapters = dbHelper.getAllChapters();
        if (allChapters.isEmpty()) return;

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

        String dateStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        int seed = Integer.parseInt(dateStr);
        Random rnd = new Random(seed);
        int index = rnd.nextInt(validChapters.size());
        randomChapter = validChapters.get(index);

        tvRecommendTitle.setText(randomChapter.getTitle());

        List<Paragraph> paras = dbHelper.getParagraphsByChapter(randomChapter.getId());
        if (!paras.isEmpty()) {
            String text = paras.get(0).getOriginalText();
            if (text != null) {
                if (text.length() > 50) text = text.substring(0, 50) + "…";
                tvRecommendExcerpt.setText(text);
            }
        }
    }

    private void onSearchClick() {
        startActivity(new Intent(getActivity(), SearchActivity.class));
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
        }
    }

    private void onRecommendClick() {
        if (randomChapter != null) {
            Intent intent = new Intent(getActivity(), ReaderActivity.class);
            intent.putExtra("chapter_id", randomChapter.getId());
            intent.putExtra("chapter_title", randomChapter.getTitle());
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
            }
        }
    }

    private void onSuwenClick() {
        Intent intent = new Intent(getActivity(), ChapterListActivity.class);
        intent.putExtra("volume", "素问");
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private void onLingshuClick() {
        Intent intent = new Intent(getActivity(), ChapterListActivity.class);
        intent.putExtra("volume", "灵枢");
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}
