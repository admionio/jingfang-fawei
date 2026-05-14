package cn.lanzp.jffw.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cn.lanzp.jffw.R;
import cn.lanzp.jffw.model.Favorite;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private final Context context;
    private final List<Favorite> favorites;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Favorite favorite);
        void onItemDelete(Favorite favorite);
    }

    public FavoriteAdapter(Context context, List<Favorite> favorites, OnItemClickListener listener) {
        this.context = context;
        this.favorites = favorites;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Favorite fav = favorites.get(position);
        holder.tvTitle.setText(fav.getChapterTitle());
        holder.tvVolume.setText(fav.getVolume());
        holder.tvTime.setText(fav.getCreatedAt());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(fav);
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public void removeItem(int position) {
        favorites.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvVolume;
        TextView tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvFavTitle);
            tvVolume = itemView.findViewById(R.id.tvFavVolume);
            tvTime = itemView.findViewById(R.id.tvFavTime);
        }
    }
}
