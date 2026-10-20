package app.neont.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHolder> {
    private List<ChannelModel> channelList;
    private Context context;

    public ChannelAdapter(Context context, List<ChannelModel> channelList) {
        this.context = context;
        this.channelList = channelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChannelModel channel = channelList.get(position);
        holder.tvChannelName.setText(channel.getName());
        holder.tvChannelFollowers.setText(channel.getFollowers() + " Pengikut");
    }

    @Override
    public int getItemCount() { return channelList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvChannelName, tvChannelFollowers;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChannelName = itemView.findViewById(R.id.tvChannelName);
            tvChannelFollowers = itemView.findViewById(R.id.tvChannelFollowers);
        }
    }
}
