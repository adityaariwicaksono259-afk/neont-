package app.neont.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<UserModel> userList;
    private Context context;
    private OnUserClickListener listener;
    private String myUid;
    private DatabaseReference mDb;

    public interface OnUserClickListener {
        void onUserClick(UserModel user);
    }

    public ChatAdapter(Context context, List<UserModel> userList, OnUserClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
        this.myUid = FirebaseAuth.getInstance().getUid();
        this.mDb = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.tvName.setText(user.getName() != null ? user.getName() : "User");

        // LOGIKA TELEGRAM: Baca status online
        mDb.child("users").child(user.getUid()).child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if ("online".equals(status)) {
                    holder.tvPreview.setText("online");
                    holder.tvPreview.setTextColor(0xFF007AFF); // Biru
                } else {
                    holder.tvPreview.setText("terakhir dilihat baru-baru ini");
                    holder.tvPreview.setTextColor(0xFF8E8E93); // Abu-abu
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        // LOGIKA TELEGRAM: Baca pesan terakhir (preview)
        String roomId = (myUid.compareTo(user.getUid()) < 0) ? myUid + "_" + user.getUid() : user.getUid() + "_" + myUid;
        mDb.child("chats").child(roomId).child("messages").orderByChild("timestamp").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        String text = data.child("text").getValue(String.class);
                        String status = data.child("status").getValue(String.class);
                        Long timestamp = data.child("timestamp").getValue(Long.class);

                        holder.tvPreview.setText(text != null ? text : "Klik untuk mulai chat");

                        // LOGIKA TELEGRAM: Centang (sent/delivered/read)
                        if ("read".equals(status)) {
                            holder.tvCheck.setText("✓✓");
                            holder.tvCheck.setTextColor(0xFF007AFF); // Biru
                        } else if ("delivered".equals(status)) {
                            holder.tvCheck.setText("✓✓");
                            holder.tvCheck.setTextColor(0xFF8E8E93); // Abu-abu
                        } else {
                            holder.tvCheck.setText("✓");
                            holder.tvCheck.setTextColor(0xFF8E8E93); // Abu-abu
                        }

                        // LOGIKA TELEGRAM: Jam
                        if (timestamp != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            holder.tvTime.setText(sdf.format(new Date(timestamp)));
                        }
                    }
                } else {
                    holder.tvPreview.setText("Klik untuk mulai chat");
                }
            }
            @Override public void onCancelled(DatabaseError error) {}
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPreview, tvCheck, tvTime;
        ImageView avatar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPreview = itemView.findViewById(R.id.tvPreview);
            tvCheck = itemView.findViewById(R.id.tvCheck);
            tvTime = itemView.findViewById(R.id.tvTime);
            avatar = itemView.findViewById(R.id.avatar);
        }
    }
}
