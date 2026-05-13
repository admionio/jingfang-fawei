package cn.lanzp.hdnj.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import cn.lanzp.hdnj.BuildConfig;
import cn.lanzp.hdnj.R;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;
    private SeekBar sbFontSize;
    private SwitchCompat switchNightMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireContext().getSharedPreferences("settings", getContext().MODE_PRIVATE);

        // 动态显示版本号
        TextView tvAboutVersion = view.findViewById(R.id.tvAboutVersion);
        tvAboutVersion.setText("版本 " + BuildConfig.VERSION_NAME);

        sbFontSize = view.findViewById(R.id.sbFontSize);
        switchNightMode = view.findViewById(R.id.switchNightMode);

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
            // 触发重建
            requireActivity().recreate();
        });

        return view;
    }
}
