package cn.lanzp.hdnj;

import android.content.Intent;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.lanzp.hdnj.adapter.BookmarkAdapter;
import cn.lanzp.hdnj.database.DbHelper;
import cn.lanzp.hdnj.model.Bookmark;

public class BookmarksActivity extends AppCompatActivity {

    private DbHelper dbHelper;
    private RecyclerView rvBookmarks;
    private TextView tvEmpty;
    private BookmarkAdapter adapter;
    private List<Bookmark> bookmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        dbHelper = DbHelper.getInstance(this);

        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> finish());

        rvBookmarks = findViewById(R.id.rvBookmarks);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvBookmarks.setLayoutManager(new LinearLayoutManager(this));

        loadBookmarks();
        setupSwipeToDelete();
    }

    private void loadBookmarks() {
        bookmarks = dbHelper.getBookmarks();
        if (bookmarks.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvBookmarks.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvBookmarks.setVisibility(View.VISIBLE);
        }

        adapter = new BookmarkAdapter(this, bookmarks, new BookmarkAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Bookmark bm) {
                Intent intent = new Intent(BookmarksActivity.this,
                        cn.lanzp.hdnj.reader.ReaderActivity.class);
                intent.putExtra("chapter_id", bm.getChapterId());
                intent.putExtra("chapter_title", bm.getChapterTitle());
                // 有 paragraph_no 信息，可以跳转到段落
                startActivity(intent);
            }

            @Override
            public void onItemDelete(Bookmark bm) {
                dbHelper.removeBookmark(bm.getId());
                loadBookmarks();
            }
        });
        rvBookmarks.setAdapter(adapter);
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
                if (position >= 0 && position < bookmarks.size()) {
                    Bookmark bm = bookmarks.get(position);
                    dbHelper.removeBookmark(bm.getId());
                    adapter.removeItem(position);
                    if (bookmarks.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvBookmarks.setVisibility(View.GONE);
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

        new ItemTouchHelper(callback).attachToRecyclerView(rvBookmarks);
    }
}
