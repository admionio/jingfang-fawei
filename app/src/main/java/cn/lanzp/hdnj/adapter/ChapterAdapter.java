package cn.lanzp.hdnj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.lanzp.hdnj.R;
import cn.lanzp.hdnj.model.Chapter;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ViewHolder> {

    private final Context context;
    private final List<Chapter> chapters;
    private final OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(Chapter chapter);
        void onFavoriteClick(Chapter chapter, int position);
    }

    public ChapterAdapter(Context context, List<Chapter> chapters, OnChapterClickListener listener) {
        this.context = context;
        this.chapters = chapters;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        holder.tvIndex.setText(String.valueOf(chapter.getChapterNo()));
        holder.tvTitle.setText(chapter.getTitle());

        // 已读标记
        holder.tvReadStatus.setVisibility(chapter.isRead() ? View.VISIBLE : View.GONE);

        holder.ivFavorite.setImageResource(
                chapter.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChapterClick(chapter);
        });

        holder.ivFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(chapter, position);
        });
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public void updateItem(int position, Chapter chapter) {
        chapters.set(position, chapter);
        notifyItemChanged(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIndex;
        TextView tvTitle;
        TextView tvReadStatus;
        ImageView ivFavorite;

        ViewHolder(View itemView) {
            super(itemView);
            tvIndex = itemView.findViewById(R.id.tvChapterIndex);
            tvTitle = itemView.findViewById(R.id.tvChapterTitle);
            tvReadStatus = itemView.findViewById(R.id.tvReadStatus);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}
