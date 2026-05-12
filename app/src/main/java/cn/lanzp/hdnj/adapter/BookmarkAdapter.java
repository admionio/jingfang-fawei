package cn.lanzp.hdnj.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.lanzp.hdnj.R;
import cn.lanzp.hdnj.model.Bookmark;

public class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.ViewHolder> {

    private final Context context;
    private final List<Bookmark> bookmarks;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Bookmark bookmark);
        void onItemDelete(Bookmark bookmark);
    }

    public BookmarkAdapter(Context context, List<Bookmark> bookmarks, OnItemClickListener listener) {
        this.context = context;
        this.bookmarks = bookmarks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bookmark, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bookmark bm = bookmarks.get(position);
        holder.tvTitle.setText(bm.getChapterTitle());
        holder.tvExcerpt.setText(bm.getExcerpt());
        holder.tvTime.setText(bm.getCreatedAt());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(bm);
        });
    }

    @Override
    public int getItemCount() {
        return bookmarks.size();
    }

    public void removeItem(int position) {
        bookmarks.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvExcerpt;
        TextView tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBmTitle);
            tvExcerpt = itemView.findViewById(R.id.tvBmExcerpt);
            tvTime = itemView.findViewById(R.id.tvBmTime);
        }
    }
}
