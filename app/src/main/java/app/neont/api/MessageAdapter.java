package app.neont.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<MessageModel> messageList;
    private String currentUserId;
    private OnMessageLongClickListener longClickListener;
    private OnReactionClickListener reactionClickListener;
    private static final int TYPE_SENT = 1, TYPE_RECEIVED = 2;

    public interface OnMessageLongClickListener { void onMessageLongClick(MessageModel message); }
    public interface OnReactionClickListener { void onReactionClick(MessageModel message, String emoji); }

    public MessageAdapter(List<MessageModel> messageList, OnMessageLongClickListener longClickListener, OnReactionClickListener reactionClickListener) {
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
        this.longClickListener = longClickListener;
        this.reactionClickListener = reactionClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel msg = messageList.get(position);
        if (msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) return TYPE_SENT;
        else return TYPE_RECEIVED;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_SENT) ? R.layout.item_message_sent : R.layout.item_message_received;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        holder.tvMessageText.setText(message.getText() != null ? message.getText() : "");

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvMessageTime.setText(sdf.format(new Date(message.getTimestamp())));

        // Reply (Quote)
        if (message.getReplyToMsgId() != null && holder.layoutQuote != null) {
            holder.layoutQuote.setVisibility(LinearLayout.VISIBLE);
            if (holder.tvQuoteText != null) holder.tvQuoteText.setText("Membalas pesan");
        } else if (holder.layoutQuote != null) {
            holder.layoutQuote.setVisibility(LinearLayout.GONE);
        }

        // Reaction (Toggle)
        if (holder.layoutReaction != null) {
            if (message.getReactions() != null && !message.getReactions().isEmpty()) {
                holder.layoutReaction.setVisibility(LinearLayout.VISIBLE);
                StringBuilder emojiText = new StringBuilder();
                for (Map.Entry<String, Map<String, Boolean>> entry : message.getReactions().entrySet()) {
                    emojiText.append(entry.getKey()).append(entry.getValue().size()).append(" ");
                }
                if (holder.tvReaction != null) holder.tvReaction.setText(emojiText.toString());
                
                // Klik reaction untuk toggle
                holder.layoutReaction.setOnClickListener(v -> {
                    if (reactionClickListener != null) {
                        // Ambil emoji pertama untuk di-toggle
                        String firstEmoji = message.getReactions().keySet().iterator().next();
                        reactionClickListener.onReactionClick(message, firstEmoji);
                    }
                });
            } else {
                holder.layoutReaction.setVisibility(LinearLayout.GONE);
            }
        }

        if (holder.viewType == TYPE_SENT) {
            String status = message.getStatus();
            if ("read".equals(status)) { holder.tvStatus.setText("✓✓"); holder.tvStatus.setTextColor(0xFF007AFF); }
            else if ("delivered".equals(status)) { holder.tvStatus.setText("✓✓"); holder.tvStatus.setTextColor(0xFF8E8E93); }
            else { holder.tvStatus.setText("✓"); holder.tvStatus.setTextColor(0xFF8E8E93); }
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onMessageLongClick(message);
            return true;
        });
    }

    @Override public int getItemCount() { return messageList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageTime, tvStatus, tvQuoteText, tvReaction;
        LinearLayout layoutQuote, layoutReaction;
        int viewType;
        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            if (viewType == TYPE_SENT) tvStatus = itemView.findViewById(R.id.tvStatus);
            layoutQuote = itemView.findViewById(R.id.layoutQuote);
            tvQuoteText = itemView.findViewById(R.id.tvQuoteText);
            layoutReaction = itemView.findViewById(R.id.layoutReaction);
            tvReaction = itemView.findViewById(R.id.tvReaction);
        }
    }
}
