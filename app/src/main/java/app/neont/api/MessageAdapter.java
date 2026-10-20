package app.neont.api;

import android.view.Gravity;
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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<MessageModel> messageList;
    private String currentUserId;

    public MessageAdapter(List<MessageModel> messageList) {
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        
        String text = message.getText() != null ? message.getText() : "";
        holder.tvMessageText.setText(text);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvMessageTime.setText(sdf.format(new Date(message.getTimestamp())));

        LinearLayout.LayoutParams containerParams = (LinearLayout.LayoutParams) holder.bubbleContainer.getLayoutParams();
        String sender = message.getSenderId() != null ? message.getSenderId() : "";

        if (sender.equals(currentUserId)) {
            // Pesan Kita (Kanan, Hijau, Centang)
            containerParams.gravity = Gravity.END;
            holder.bubbleLayout.setBackgroundResource(R.drawable.bg_bubble_sent);
            holder.tvStatus.setVisibility(View.VISIBLE);
            
            String status = message.getStatus();
            if ("read".equals(status)) {
                holder.tvStatus.setText("✓✓");
                holder.tvStatus.setTextColor(0xFF34B7F1); // Biru
            } else if ("delivered".equals(status)) {
                holder.tvStatus.setText("✓✓");
                holder.tvStatus.setTextColor(0xFF888888); // Abu-abu
            } else {
                holder.tvStatus.setText("✓");
                holder.tvStatus.setTextColor(0xFF888888); // Abu-abu
            }
        } else {
            // Pesan Orang (Kiri, Putih)
            containerParams.gravity = Gravity.START;
            holder.bubbleLayout.setBackgroundResource(R.drawable.bg_bubble_received);
            holder.tvStatus.setVisibility(View.GONE);
        }
        holder.bubbleContainer.setLayoutParams(containerParams);
    }

    @Override
    public int getItemCount() { return messageList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageTime, tvStatus;
        LinearLayout bubbleContainer, bubbleLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            bubbleContainer = itemView.findViewById(R.id.bubbleContainer);
            bubbleLayout = itemView.findViewById(R.id.bubbleLayout);
        }
    }
}
