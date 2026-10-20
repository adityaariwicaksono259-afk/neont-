package app.neont.api;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class PembaruanActivity extends AppCompatActivity {

    private RecyclerView rvStories, rvChannels;
    private StoryAdapter storyAdapter;
    private ChannelAdapter channelAdapter;
    private List<StoryModel> storyList;
    private List<ChannelModel> channelList;
    private DatabaseReference mDb;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pembaruan);

        rvStories = findViewById(R.id.rvStories);
        rvChannels = findViewById(R.id.rvChannels);
        mDb = FirebaseDatabase.getInstance().getReference();
        myUid = FirebaseAuth.getInstance().getUid();

        storyList = new ArrayList<>();
        storyAdapter = new StoryAdapter(this, storyList);
        rvStories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvStories.setAdapter(storyAdapter);

        channelList = new ArrayList<>();
        channelAdapter = new ChannelAdapter(this, channelList);
        rvChannels.setLayoutManager(new LinearLayoutManager(this));
        rvChannels.setAdapter(channelAdapter);

        // Tombol Bikin Story (Klik Avatar "Story saya")
        findViewById(R.id.layoutStorySaya).setOnClickListener(v -> bikinStory());

        loadStories();
        loadChannels();
    }

    private void bikinStory() {
        String[] privacyOptions = {"Semua Orang (Publik)", "Kontak Saya", "Teman Dekat", "Kontak Terpilih"};
        new AlertDialog.Builder(this)
            .setTitle("Siapa yang bisa lihat story ini?")
            .setItems(privacyOptions, (dialog, which) -> {
                String privacy = "everyone";
                if (which == 1) privacy = "contacts";
                else if (which == 2) privacy = "close_friends";
                else if (which == 3) privacy = "selected";

                // Simpan Story ke Firebase (Sementara teks dulu)
                String storyId = mDb.child("stories").child(myUid).push().getKey();
                java.util.Map<String, Object> storyData = new java.util.HashMap<>();
                storyData.put("name", "Status saya");
                storyData.put("timestamp", System.currentTimeMillis());
                storyData.put("privacy", privacy);
                
                if (storyId != null) {
                    mDb.child("stories").child(myUid).child(storyId).setValue(storyData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Story Diposting! (" + privacy + ")", Toast.LENGTH_SHORT).show());
                }
            })
            .show();
    }

    private void loadStories() {
        mDb.child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                storyList.clear();
                long now = System.currentTimeMillis();
                
                // Ambil list kontak kita dulu (buat ngecek privasi "contacts")
                mDb.child("contacts").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot contactsSnap) {
                        List<String> myContacts = new ArrayList<>();
                        for (DataSnapshot c : contactsSnap.getChildren()) myContacts.add(c.getKey());

                        for (DataSnapshot userStoryNode : snapshot.getChildren()) {
                            String uid = userStoryNode.getKey();
                            for (DataSnapshot story : userStoryNode.getChildren()) {
                                Long timestamp = story.child("timestamp").getValue(Long.class);
                                String privacy = story.child("privacy").getValue(String.class);
                                String name = story.child("name").getValue(String.class);
                                
                                // Cek Expired 24 Jam
                                if (timestamp != null && (now - timestamp) < (24 * 60 * 60 * 1000)) {
                                    // LOGIKA FILTER PRIVASI
                                    boolean canView = false;
                                    if (uid.equals(myUid)) canView = true; // Story sendiri selalu keliatan
                                    else if ("everyone".equals(privacy)) canView = true;
                                    else if ("contacts".equals(privacy) && myContacts.contains(uid)) canView = true;
                                    else if ("selected".equals(privacy)) {
                                        // Nanti kita cek list allowedUsers, sementara false dulu
                                        canView = false; 
                                    }

                                    if (canView) {
                                        storyList.add(new StoryModel(uid, name, null, timestamp, privacy, null));
                                    }
                                }
                            }
                        }
                        storyAdapter.notifyDataSetChanged();
                    }
                    @Override public void onCancelled(DatabaseError error) {}
                });
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadChannels() {
        mDb.child("channels").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                channelList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String id = data.getKey();
                    String name = data.child("name").getValue(String.class);
                    String desc = data.child("description").getValue(String.class);
                    Integer followers = data.child("followers").getValue(Integer.class);
                    channelList.add(new ChannelModel(id, name, desc, followers != null ? followers : 0));
                }
                channelAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(DatabaseError error) {}
        });
    }
}
