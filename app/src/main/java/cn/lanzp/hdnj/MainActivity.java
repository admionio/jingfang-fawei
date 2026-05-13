package cn.lanzp.hdnj;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import cn.lanzp.hdnj.database.DataImporter;
import cn.lanzp.hdnj.database.DbHelper;

import cn.lanzp.hdnj.fragment.FavoritesFragment;
import cn.lanzp.hdnj.fragment.SettingsFragment;
import cn.lanzp.hdnj.fragment.HomeFragment;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 使用 AppCompatDelegate 控制日夜模式，使 values-night/ 资源自动生效
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean nightMode = prefs.getBoolean("night_mode", false);
        AppCompatDelegate.setDefaultNightMode(nightMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DbHelper dbHelper = DbHelper.getInstance(this);

        // 首次启动导入数据
        if (!DataImporter.isDataImported(this)) {
            DataImporter.importData(this, dbHelper);
        }

        bottomNav = findViewById(R.id.bottomNavigation);
        setupNavigation();

        // 默认显示首页
        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                switchFragment(new HomeFragment());
                return true;
            } else if (itemId == R.id.nav_favorites) {
                switchFragment(new FavoritesFragment());
                return true;
            } else if (itemId == R.id.nav_settings) {
                switchFragment(new SettingsFragment());
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment fragment) {
        if (fragment == currentFragment) return;
        currentFragment = fragment;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }
}
