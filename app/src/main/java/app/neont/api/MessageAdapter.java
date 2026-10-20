package app.neont.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<MessageModel> messageList;
    private String currentUserId;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    public MessageAdapter(List<MessageModel> messageList) {
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel msg = messageList.get(position);
        if (msg.getSenderId() != null && msg.getSenderId().equals(currentUserId)) return TYPE_SENT;
        else return TYPE_RECEIVED;
    }

    @NonNull
    @Override
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

        if (holder.viewType == TYPE_SENT) {
            String status = message.getStatus();
            if ("read".equals(status)) { holder.tvStatus.setText("✓✓"); holder.tvStatus.setTextColor(0xFF34B7F1); }
            else if ("delivered".equals(status)) { holder.tvStatus.setText("✓✓"); holder.tvStatus.setTextColor(0xFF888888); }
            else { holder.tvStatus.setText("✓"); holder.tvStatus.setTextColor(0xFF888888); }
        } else {
            // Tampilin nama pengirim (bisa lo ubah biar narik dari Firebase nanti)
            if (holder.tvSenderName != null) {
                holder.tvSenderName.setText("User"); 
            }
        }
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageTime, tvStatus, tvSenderName;
        int viewType;
        public ViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            if (viewType == TYPE_SENT) {
                tvStatus = itemView.findViewById(R.id.tvStatus);
            } else {
                tvSenderName = itemView.findViewById(R.id.tvSenderName);
            }
        }
    }
}
