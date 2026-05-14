package cn.lanzp.jffw.fragment;

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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import cn.lanzp.jffw.ChapterListActivity;
import cn.lanzp.jffw.R;
import cn.lanzp.jffw.SearchActivity;
import cn.lanzp.jffw.database.DbHelper;
import cn.lanzp.jffw.model.Chapter;
import cn.lanzp.jffw.model.Paragraph;
import cn.lanzp.jffw.reader.ReaderActivity;
import cn.lanzp.jffw.util.LunarCalendar;

public class HomeFragment extends Fragment {

    private DbHelper dbHelper;
    private TextView tvShanghanCount, tvJinkuiCount;
    private TextView tvShanghanProgress, tvJinkuiProgress;
    private TextView tvRecommendTitle, tvRecommendExcerpt, tvRecommendTag;
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
        tvShanghanCount = view.findViewById(R.id.tvSuwenCount);
        tvJinkuiCount = view.findViewById(R.id.tvLingshuCount);
        tvShanghanProgress = view.findViewById(R.id.tvSuwenProgress);
        tvJinkuiProgress = view.findViewById(R.id.tvLingshuProgress);
        tvRecommendTitle = view.findViewById(R.id.tvRecommendTitle);
        tvRecommendExcerpt = view.findViewById(R.id.tvRecommendExcerpt);
        tvRecommendTag = view.findViewById(R.id.tvRecommendTag);
        ivNightToggle = view.findViewById(R.id.ivNightToggle);

        loadStatistics();
        loadTodayRecommend();
        updateNightToggleIcon();

        // 搜索点击
        View searchCardView = (View) view.findViewById(R.id.tvSearchHint).getParent().getParent();
        searchCardView.setOnClickListener(v -> onSearchClick());
        view.findViewById(R.id.tvSearchHint).setOnClickListener(v -> onSearchClick());
        view.findViewById(R.id.cvTodayRecommend).setOnClickListener(v -> onRecommendClick());
        view.findViewById(R.id.cvShanghan).setOnClickListener(v -> onShanghanClick());
        view.findViewById(R.id.cvJinkui).setOnClickListener(v -> onJinkuiClick());

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
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
            String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
            int w = cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
            String solarDate = sdf2.format(now) + " " + weekDays[w];
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
        // 农历
        if (currentHour != cachedLastShichenHour) {
            cachedLastShichenHour = currentHour;

            LunarCalendar.LunarDate lunar = LunarCalendar.getLunarDate(now);
            Calendar cal2 = Calendar.getInstance();
            int huangdiYearNum = cal2.get(Calendar.YEAR) + 2698;
            String huangdiYearChinese = numberToChineseDigits(huangdiYearNum);
            cachedLunarDate = "黄帝纪元" + huangdiYearChinese + "年  " + lunar.ganzhiYear + "年 " + lunar.monthName + "月" + lunar.dayName;
            tvDateLunar.setText(cachedLunarDate);
        }

        // 时辰 - 仅显示时辰名称，不关联经络
        LunarCalendar.ShichenInfo shichen = LunarCalendar.getShichen(now);
        String shichenText = shichen.name + "时";
        if (!shichenText.equals(cachedShichenText)) {
            cachedShichenText = shichenText;
            tvShichen.setText(cachedShichenText);
            tvShichen.setTextColor(ContextCompat.getColor(requireContext(), R.color.homeShichen));
        }
    }

    private void loadStatistics() {
        int shanghanCount = dbHelper.getChapterCount("伤寒发微");
        int jinkuiCount = dbHelper.getChapterCount("金匮发微");
        tvShanghanCount.setText(getString(R.string.chapter_count, shanghanCount));
        tvJinkuiCount.setText(getString(R.string.chapter_count, jinkuiCount));

        SharedPreferences readPrefs = requireContext().getSharedPreferences("reading_progress", Context.MODE_PRIVATE);

        List<Chapter> shanghanChapters = dbHelper.getChaptersByVolume("伤寒发微");
        int shanghanReadCount = 0;
        for (Chapter ch : shanghanChapters) {
            if (readPrefs.getBoolean("read_chapter_" + ch.getId(), false)) {
                shanghanReadCount++;
            }
        }

        List<Chapter> jinkuiChapters = dbHelper.getChaptersByVolume("金匮发微");
        int jinkuiReadCount = 0;
        for (Chapter ch : jinkuiChapters) {
            if (readPrefs.getBoolean("read_chapter_" + ch.getId(), false)) {
                jinkuiReadCount++;
            }
        }

        tvShanghanProgress.setText("已阅读 " + shanghanReadCount + " / " + shanghanCount + " 篇");
        tvJinkuiProgress.setText("已阅读 " + jinkuiReadCount + " / " + jinkuiCount + " 篇");
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

        // 显示书籍标签
        String volume = randomChapter.getVolume();
        if ("伤寒发微".equals(volume)) {
            tvRecommendTag.setText("伤寒");
            tvRecommendTag.setVisibility(View.VISIBLE);
        } else if ("金匮发微".equals(volume)) {
            tvRecommendTag.setText("金匮");
            tvRecommendTag.setVisibility(View.VISIBLE);
        }

        List<Paragraph> paras = dbHelper.getParagraphsByChapter(randomChapter.getId());
        if (!paras.isEmpty()) {
            String text = paras.get(0).getOriginalText();
            if (text != null) {
                if (text.length() > 50) text = text.substring(0, 50) + "…";
                tvRecommendExcerpt.setText(text);
            }
        }
    }

    /**
     * 将数字逐位转为中文小写数字
     */
    private String numberToChineseDigits(int number) {
        String numStr = String.valueOf(number);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numStr.length(); i++) {
            char c = numStr.charAt(i);
            switch (c) {
                case '0': sb.append('零'); break;
                case '1': sb.append('一'); break;
                case '2': sb.append('二'); break;
                case '3': sb.append('三'); break;
                case '4': sb.append('四'); break;
                case '5': sb.append('五'); break;
                case '6': sb.append('六'); break;
                case '7': sb.append('七'); break;
                case '8': sb.append('八'); break;
                case '9': sb.append('九'); break;
                default: sb.append(c); break;
            }
        }
        return sb.toString();
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

    private void onShanghanClick() {
        Intent intent = new Intent(getActivity(), ChapterListActivity.class);
        intent.putExtra("volume", "伤寒发微");
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private void onJinkuiClick() {
        Intent intent = new Intent(getActivity(), ChapterListActivity.class);
        intent.putExtra("volume", "金匮发微");
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }
}
