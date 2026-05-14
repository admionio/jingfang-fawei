package cn.lanzp.jffw;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.lanzp.jffw.adapter.FavoriteAdapter;
import cn.lanzp.jffw.database.DbHelper;
import cn.lanzp.jffw.model.Favorite;

public class FavoritesActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private RecyclerView rvFavorites;
    private TextView tvEmpty;
    private FavoriteAdapter adapter;
    private List<Favorite> favorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        AppCompatDelegate.setDefaultNightMode(prefs.getBoolean("night_mode", false) ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        dbHelper = DbHelper.getInstance(this);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        rvFavorites = findViewById(R.id.rvFavorites);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));

        loadFavorites();
        setupSwipeToDelete();
    }

    private void loadFavorites() {
        favorites = dbHelper.getFavorites();
        if (favorites.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }

        adapter = new FavoriteAdapter(this, favorites, new FavoriteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Favorite fav) {
                Intent intent = new Intent(FavoritesActivity.this,
                        cn.lanzp.jffw.reader.ReaderActivity.class);
                intent.putExtra("chapter_id", fav.getChapterId());
                intent.putExtra("chapter_title", fav.getChapterTitle());
                startActivity(intent);
            }

            @Override
            public void onItemDelete(Favorite fav) {
                dbHelper.removeFavorite(fav.getChapterId());
                loadFavorites();
            }
        });
        rvFavorites.setAdapter(adapter);
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {
                int position = vh.getAdapterPosition();
                if (position >= 0 && position < favorites.size()) {
                    Favorite fav = favorites.get(position);
                    dbHelper.removeFavorite(fav.getChapterId());
                    adapter.removeItem(position);
                    if (favorites.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder vh,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = vh.itemView;
                    Paint p = new Paint();
                    p.setColor(Color.parseColor("#E53935"));
                    RectF bg = new RectF(
                            itemView.getRight() + dX,
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom());
                    c.drawRoundRect(bg, 8, 8, p);
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(rvFavorites);
    }
}
