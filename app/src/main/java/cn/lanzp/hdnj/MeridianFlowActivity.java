package cn.lanzp.hdnj;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Calendar;

import cn.lanzp.hdnj.model.MeridianData;
import cn.lanzp.hdnj.view.MeridianClockView;

/**
 * 子午流注详情页面
 *
 * 上半部分：子午流注时钟（MeridianClockView 自绘时钟）
 * 下半部分：当前选中时辰的详情卡片
 *  - 默认显示当前时辰
 *  - 点击时钟切换详情
 *
 * 🦞4号 Android实现  |  🦞🎨5号设计配色  |  🦞🌿3号数据审核
 */
public class MeridianFlowActivity extends AppCompatActivity {

    private MeridianClockView meridianClock;
    private MeridianData[] allData;

    // 详情卡片控件
    private TextView tvShichenTitle;
    private TextView tvTimeRange;
    private TextView tvFlowInfo;
    private TextView tvAcupoint;
    private TextView tvElement;
    private TextView tvMeridian;
    private TextView tvHealthTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 夜间模式支持
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(prefs.getBoolean("night_mode", false) ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meridian_flow);

        // 初始化数据
        allData = MeridianData.getAllData();

        // 初始化控件
        initViews();

        // 回退按钮
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        // 设置时钟点击回调
        meridianClock.setOnShichenClickListener(this::onShichenClick);

        // 默认显示当前时辰
        showCurrentShichenDetail();
    }

    @Override
    protected void onResume() {
        super.onResume();
        meridianClock.startClock();
        showCurrentShichenDetail();
    }

    @Override
    protected void onPause() {
        super.onPause();
        meridianClock.stopClock();
    }

    /**
     * 初始化页面控件
     */
    private void initViews() {
        meridianClock = findViewById(R.id.meridianClock);

        tvShichenTitle = findViewById(R.id.tvShichenTitle);
        tvTimeRange = findViewById(R.id.tvTimeRange);
        tvFlowInfo = findViewById(R.id.tvFlowInfo);
        tvAcupoint = findViewById(R.id.tvAcupoint);
        tvElement = findViewById(R.id.tvElement);
        tvMeridian = findViewById(R.id.tvMeridian);
        tvHealthTip = findViewById(R.id.tvHealthTip);
    }

    /**
     * 计算并显示当前时辰的详情
     */
    private void showCurrentShichenDetail() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int index;
        if (hour == 23 || hour == 0) {
            index = 0; // 子时
        } else {
            index = (hour + 1) / 2;
        }
        updateDetail(index);
    }

    /**
     * 时辰点击回调
     */
    private void onShichenClick(int index, String shichenName) {
        if (index >= 0 && index < allData.length) {
            updateDetail(index);
        }
    }

    /**
     * 更新详情卡片显示
     */
    private void updateDetail(int index) {
        if (index < 0 || index >= allData.length) return;

        MeridianData data = allData[index];

        tvShichenTitle.setText(data.getShichen() + "时");
        tvTimeRange.setText(data.getTimeRange());
        tvFlowInfo.setText(data.getFlowInfo());
        tvAcupoint.setText(data.getAcupoint());
        tvElement.setText(data.getElement());
        tvMeridian.setText(data.getMeridian());
        tvHealthTip.setText(data.getHealthTip());
    }
}
