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
        holder.tvMessageText.setText(message.getText());

        // Format Jam
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.tvMessageTime.setText(sdf.format(new Date(message.getTimestamp())));

        // Layout Gravity: Kanan (Saya) atau Kiri (Teman)
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.tvMessageText.getLayoutParams();
        LinearLayout.LayoutParams timeParams = (LinearLayout.LayoutParams) holder.tvMessageTime.getLayoutParams();

        if (message.getSenderId().equals(currentUserId)) {
            params.gravity = Gravity.END;
            timeParams.gravity = Gravity.END;
            holder.tvMessageText.setBackgroundResource(R.drawable.nb_box_white); // Sementara sama, nanti kita bedain warna
            holder.tvMessageText.setTextColor(0xFF000000); // Warna teks hitam
        } else {
            params.gravity = Gravity.START;
            timeParams.gravity = Gravity.START;
            holder.tvMessageText.setBackgroundResource(R.drawable.nb_box_white);
            holder.tvMessageText.setTextColor(0xFF000000);
        }

        holder.tvMessageText.setLayoutParams(params);
        holder.tvMessageTime.setLayoutParams(timeParams);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }
    }
}
