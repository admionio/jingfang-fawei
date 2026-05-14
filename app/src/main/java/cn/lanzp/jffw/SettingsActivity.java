package cn.lanzp.jffw;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import cn.lanzp.jffw.BuildConfig;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SeekBar sbFontSize;
    private SwitchCompat switchNightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 使用 AppCompatDelegate 控制日夜模式
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(sp.getBoolean("night_mode", false) ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        // 动态显示版本号
        TextView tvAboutVersion = findViewById(R.id.tvAboutVersion);
        tvAboutVersion.setText("版本 " + BuildConfig.VERSION_NAME);

        sbFontSize = findViewById(R.id.sbFontSize);
        switchNightMode = findViewById(R.id.switchNightMode);

        // 加载已有设置
        sbFontSize.setProgress(prefs.getInt("font_size", 2));
        switchNightMode.setChecked(prefs.getBoolean("night_mode", false));

        // 保存设置
        sbFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                prefs.edit().putInt("font_size", seekBar.getProgress()).apply();
            }
        });

        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("night_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            switchNightMode.postDelayed(() -> {
                recreate();
            }, 300);
        });
    }
}
