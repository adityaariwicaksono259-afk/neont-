package app.neont.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private List<StoryModel> storyList;
    private Context context;

    public StoryAdapter(Context context, List<StoryModel> storyList) {
        this.context = context;
        this.storyList = storyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryModel story = storyList.get(position);
        holder.tvStoryName.setText(story.getName() != null ? story.getName() : "User");
        // Placeholder: nanti bisa load image dari URL (Firebase Storage/Bot)
    }

    @Override
    public int getItemCount() { return storyList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoryName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoryName = itemView.findViewById(R.id.tvStoryName);
        }
    }
}
