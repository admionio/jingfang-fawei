package cn.lanzp.hdnj;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.lanzp.hdnj.database.DbHelper;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvResults;
    private TextView tvEmptyResult;
    private View layoutHistory;
    private LinearLayout layoutHistoryTags;
    private TextView tvClearHistory;
    private DbHelper dbHelper;
    private SearchResultAdapter adapter;
    private List<DbHelper.SearchResult> results = new ArrayList<>();

    private static final int MAX_HISTORY = 10;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = DbHelper.getInstance(this);
        prefs = getSharedPreferences("search_history", MODE_PRIVATE);

        etSearch = findViewById(R.id.etSearch);
        rvResults = findViewById(R.id.rvSearchResults);
        tvEmptyResult = findViewById(R.id.tvEmptyResult);
        layoutHistory = findViewById(R.id.layoutHistory);
        layoutHistoryTags = findViewById(R.id.layoutHistoryTags);
        tvClearHistory = findViewById(R.id.tvClearHistory);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter();
        rvResults.setAdapter(adapter);

        loadHistory();

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(etSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        // 回车搜索
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (s.toString().contains("\n")) {
                    performSearch(s.toString().trim().replace("\n", ""));
                }
            }
        });

        findViewById(R.id.ivSearchBtn).setOnClickListener(v -> 
            performSearch(etSearch.getText().toString().trim()));

        tvClearHistory.setOnClickListener(v -> clearHistory());
    }

    private void performSearch(String keyword) {
        if (keyword.isEmpty()) return;

        saveToHistory(keyword);
        results = dbHelper.search(keyword);
        adapter.notifyDataSetChanged();

        layoutHistory.setVisibility(View.GONE);
        if (results.isEmpty()) {
            rvResults.setVisibility(View.GONE);
            tvEmptyResult.setVisibility(View.VISIBLE);
        } else {
            rvResults.setVisibility(View.VISIBLE);
            tvEmptyResult.setVisibility(View.GONE);
        }
    }

    private void loadHistory() {
        Set<String> history = prefs.getStringSet("history", new HashSet<>());
        layoutHistoryTags.removeAllViews();
        for (String h : history) {
            TextView tag = new TextView(this);
            tag.setText(h);
            tag.setTextSize(14f);
            tag.setTextColor(getColor(R.color.colorPrimary));
            tag.setBackgroundResource(android.R.drawable.list_selector_background);
            tag.setPadding(16, 10, 16, 10);
            tag.setOnClickListener(v -> {
                etSearch.setText(h);
                etSearch.setSelection(h.length());
                performSearch(h);
            });
            layoutHistoryTags.addView(tag);
        }

        if (history.isEmpty()) {
            tvClearHistory.setVisibility(View.GONE);
        } else {
            tvClearHistory.setVisibility(View.VISIBLE);
        }
    }

    private void saveToHistory(String keyword) {
        Set<String> history = prefs.getStringSet("history", new HashSet<>());
        history.add(keyword);
        // 限制数量
        if (history.size() > MAX_HISTORY) {
            List<String> list = new ArrayList<>(history);
            history = new HashSet<>(list.subList(list.size() - MAX_HISTORY, list.size()));
        }
        prefs.edit().putStringSet("history", history).apply();
    }

    private void clearHistory() {
        prefs.edit().remove("history").apply();
        loadHistory();
    }

    private class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(SearchActivity.this)
                    .inflate(R.layout.item_search_result, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DbHelper.SearchResult sr = results.get(position);
            holder.tvSource.setText("(" + sr.volume + ") " + sr.chapterTitle);
            holder.tvExcerpt.setText(sr.matchedText);

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(SearchActivity.this,
                        cn.lanzp.hdnj.reader.ReaderActivity.class);
                intent.putExtra("chapter_id", sr.chapterId);
                intent.putExtra("chapter_title", sr.chapterTitle);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return results.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvSource, tvExcerpt;

            ViewHolder(View itemView) {
                super(itemView);
                tvSource = itemView.findViewById(R.id.tvResultSource);
                tvExcerpt = itemView.findViewById(R.id.tvResultExcerpt);
            }
        }
    }
}
