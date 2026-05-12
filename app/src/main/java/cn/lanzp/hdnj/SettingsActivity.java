package cn.lanzp.hdnj;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SeekBar sbFontSize;
    private SwitchCompat switchNightMode;
    private RadioButton rbScrollMode, rbPageMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        sbFontSize = findViewById(R.id.sbFontSize);
        switchNightMode = findViewById(R.id.switchNightMode);
        rbScrollMode = findViewById(R.id.rbScrollMode);
        rbPageMode = findViewById(R.id.rbPageMode);

        // 加载已有设置
        sbFontSize.setProgress(prefs.getInt("font_size", 2));
        switchNightMode.setChecked(prefs.getBoolean("night_mode", false));
        boolean isScrollMode = prefs.getBoolean("scroll_mode", true);
        rbScrollMode.setChecked(isScrollMode);
        rbPageMode.setChecked(!isScrollMode);

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
            // 提示用户需要重启Activity才能生效
            switchNightMode.postDelayed(() -> {
                // 触发重建
                recreate();
            }, 300);
        });

        rbScrollMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                prefs.edit().putBoolean("scroll_mode", true).apply();
                rbPageMode.setChecked(false);
            }
        });

        rbPageMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                prefs.edit().putBoolean("scroll_mode", false).apply();
                rbScrollMode.setChecked(false);
            }
        });
    }
}
