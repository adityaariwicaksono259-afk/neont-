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
import java.util.List;

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
        holder.tvName.setText(user.getName());
        
        // Logika Preview Pesan Terakhir
        String roomId;
        if ("GLOBAL".equals(user.getUid())) {
            roomId = "GLOBAL";
        } else {
            if (myUid.compareTo(user.getUid()) < 0) {
                roomId = myUid + "_" + user.getUid();
            } else {
                roomId = user.getUid() + "_" + myUid;
            }
        }

        mDb.child("chats").child(roomId).orderByChild("timestamp").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        MessageModel lastMsg = data.getValue(MessageModel.class);
                        if (lastMsg != null) {
                            holder.tvLastMessage.setText(lastMsg.getText());
                        }
                    }
                } else {
                    holder.tvLastMessage.setText("Klik untuk mulai chat");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                holder.tvLastMessage.setText("Klik untuk mulai chat");
            }
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
        ImageView avatar;
        TextView tvName, tvLastMessage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}
