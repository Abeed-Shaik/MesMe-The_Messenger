package com.abeed.mesme.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abeed.mesme.Adapters.MessagesAdapter;
import com.abeed.mesme.Models.Message;
import com.abeed.mesme.Models.User;
import com.abeed.mesme.R;
import com.abeed.mesme.SwipeReply;
import com.abeed.mesme.databinding.ActivityChatBinding;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    public ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    SwipeReply swipeReplyController;

    ValueEventListener seenListener;
    ValueEventListener slistener;

    String senderRoom, recieverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;

    DatabaseReference reference;
    DatabaseReference ref;

    FirebaseUser fuser;
    private boolean notifShown = false;

    String senderUid;
    String recieverUid;

    int position;
    final int ITEM_SEND = 1;
    final int ITEM_RECIEVE = 2;
    Boolean isSwiped = false;
    int viewType;

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.chatmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clrchat) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear this Chat?")
                    .setMessage("Messages will only be removed from this device.")
                    .setPositiveButton("CLEAR CHAT", (dialog, which) -> FirebaseDatabase.getInstance().getReference().child("chats")
                            .child(senderRoom).setValue(null).addOnSuccessListener(unused -> {
                                dialog.dismiss();
                                Toast.makeText(ChatActivity.this, "Chat Cleared Successfully", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                dialog.dismiss();
                                Toast.makeText(ChatActivity.this, "Error in Clearing Chat, please try again", Toast.LENGTH_SHORT).show();
                            })).setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).show();

            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.status_blue));
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        messages = new ArrayList<>();

        String name = getIntent().getStringExtra("name");
        String profile = getIntent().getStringExtra("image");
        String token = getIntent().getStringExtra("token");

        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profile)
                .placeholder(R.drawable.icuser)
                .into(binding.profile);

        binding.imageView2.setOnClickListener(v -> finish());

        recieverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getUid();

        database.getReference().child("presence").child(recieverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.exists()) {
                   String status = snapshot.getValue(String.class);
                   if(!Objects.requireNonNull(status).isEmpty()) {
                       if(status.equals("Offline")){
                           binding.status.setVisibility(View.GONE);
                       } else {
                           binding.status.setText(status);
                           binding.status.setVisibility(View.VISIBLE);
                       }
                   }
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        senderRoom = senderUid + recieverUid;
        recieverRoom = recieverUid + senderUid;

        adapter = new MessagesAdapter(this, messages, senderRoom, recieverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);


        swipeReplyController = new SwipeReply(this,position -> {
            this.position = position;
            viewType = adapter.getItemViewType(position);

            Message message = messages.get(position);
            binding.messegeBox.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(binding.messegeBox, InputMethodManager.SHOW_IMPLICIT);
            binding.messageqt.setText(message.getMessage());
            binding.cardViewQt.setVisibility(View.VISIBLE);
            isSwiped = true;

            switch (viewType) {
                case ITEM_RECIEVE:
                    binding.messageqtname.setText(name);
                    break;
                case ITEM_SEND:
                    binding.messageqtname.setText(R.string.you);
                    break;
                default:
                    binding.messageqtname.setVisibility(View.GONE);
                    break;
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeReplyController);
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);

        database.getReference().child("chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            Objects.requireNonNull(message).setMessageId(snapshot1.getKey());
                            messages.add(message);
                            binding.recyclerView.scrollToPosition(adapter.getItemCount()-1);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        seenMessage(recieverUid);

        binding.cancelbtn.setOnClickListener(v -> {
        binding.cardViewQt.setVisibility(View.GONE);
        isSwiped = false;
        });

        binding.sendBtn.setOnClickListener(v -> {
            String messageTxt = binding.messegeBox.getText().toString();

            if (senderUid.equals(recieverUid)) {
                Toast.makeText(ChatActivity.this, "You can't Message Yourself..!!", Toast.LENGTH_SHORT).show();
            } else if (messageTxt.isEmpty()) {
                Toast.makeText(ChatActivity.this, "Please Type a Message", Toast.LENGTH_SHORT).show();
            } else if (viewType == ITEM_RECIEVE && isSwiped)  {
                viewType = adapter.getItemViewType(position);
                Message message = messages.get(position);
                binding.cardViewQt.setVisibility(View.GONE);
                isSwiped = false;
                binding.messageqtname.setText(name);

                Date date = new Date();
                Message msgrecvr = new Message(messageTxt, senderUid, recieverUid, date.getTime(), 1, (long) position, "You", message.getMessage());
                Message msgsendr = new Message(messageTxt, senderUid, recieverUid, date.getTime(), 1, (long) position, name, message.getMessage());
                binding.messegeBox.setText("");
                binding.recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                 String randomkey = database.getReference().push().getKey();
                        database.getReference().child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(Objects.requireNonNull(randomkey))
                                .setValue(msgsendr).addOnSuccessListener(aVoid -> database.getReference().child("chats")
                                        .child(recieverRoom)
                                        .child("messages")
                                        .child(randomkey)
                                        .setValue(msgrecvr).addOnSuccessListener(unused -> database.getReference().child("users").child(senderUid).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                User user = snapshot.getValue(User.class);
                                                String nm = Objects.requireNonNull(user).getName();
                                                if (!notifShown) {
                                                    sendNotification(nm, msgsendr.getMessage(), token);
                                                    notifShown = true;
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        })));

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(senderUid);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Long msgcount = Objects.requireNonNull(user).getMsgs();
                        Long cnt = msgcount + 1;
                        HashMap<String, Object> cntObj = new HashMap<>();
                        cntObj.put("msgs", cnt);
                        ref.updateChildren(cntObj);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("users").child(recieverUid);
                ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Long msgcount = Objects.requireNonNull(user).getMsgs();
                        Long cnt = msgcount + 1;
                        HashMap<String, Object> cntObj = new HashMap<>();
                        cntObj.put("msgs", cnt);
                        ref1.updateChildren(cntObj);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("chatslist")
                        .child(senderUid)
                        .child(recieverUid);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            reference.child("id").setValue(recieverUid);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                DatabaseReference dbref = FirebaseDatabase.getInstance().getReference()
                        .child("chatslist")
                        .child(recieverUid)
                        .child(senderUid);
                dbref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            dbref.child("id").setValue(senderUid);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                } else if (isSwiped) {
                viewType = adapter.getItemViewType(position);
                Message message = messages.get(position);
                binding.cardViewQt.setVisibility(View.GONE);
                    isSwiped = false;
                    binding.messageqtname.setText(R.string.you);

                    binding.messageqtname.setText(name);

                        Date date = new Date();
                        Message msgsendr = new Message(messageTxt, senderUid, recieverUid, date.getTime(), 1, position, "You", message.getMessage());
                        binding.messegeBox.setText("");
                        binding.recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                        String randomkey = database.getReference().push().getKey();

                        database.getReference().child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(Objects.requireNonNull(randomkey))
                                .setValue(msgsendr).addOnSuccessListener(aVoid -> database.getReference().child("users").child(senderUid).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        User user = snapshot.getValue(User.class);
                                        String nm = Objects.requireNonNull(user).getName();
                                        Message msgrecvr = new Message(messageTxt, senderUid, recieverUid, date.getTime(), 1, position, nm, message.getMessage());
                                        database.getReference().child("chats")
                                                .child(recieverRoom)
                                                .child("messages")
                                                .child(randomkey)
                                                .setValue(msgrecvr);
                                        if (!notifShown) {
                                            sendNotification(nm, msgrecvr.getMessage(), token);
                                            notifShown = true;
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                }));

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(senderUid);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Long msgcount = Objects.requireNonNull(user).getMsgs();
                        Long cnt = msgcount + 1;
                        HashMap<String, Object> cntObj = new HashMap<>();
                        cntObj.put("msgs", cnt);
                        ref.updateChildren(cntObj);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("users").child(recieverUid);
                ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Long msgcount = Objects.requireNonNull(user).getMsgs();
                        Long cnt = msgcount + 1;
                        HashMap<String, Object> cntObj = new HashMap<>();
                        cntObj.put("msgs", cnt);
                        ref1.updateChildren(cntObj);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("chatslist")
                        .child(senderUid)
                        .child(recieverUid);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            reference.child("id").setValue(recieverUid);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                DatabaseReference dbref = FirebaseDatabase.getInstance().getReference()
                        .child("chatslist")
                        .child(recieverUid)
                        .child(senderUid);
                dbref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            dbref.child("id").setValue(senderUid);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                } else {

                    Date date = new Date();
                    Message msg = new Message(messageTxt, senderUid, recieverUid, date.getTime(), -1, -1, "", "");
                    binding.messegeBox.setText("");
                    binding.recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                    String randomkey = database.getReference().push().getKey();

                    database.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(Objects.requireNonNull(randomkey))
                            .setValue(msg).addOnSuccessListener(aVoid -> database.getReference().child("chats")
                                    .child(recieverRoom)
                                    .child("messages")
                                    .child(randomkey)
                                    .setValue(msg).addOnSuccessListener(unused -> database.getReference().child("users").child(senderUid).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            User user = snapshot.getValue(User.class);
                                            String nm = Objects.requireNonNull(user).getName();
                                            if (!notifShown) {
                                                sendNotification(nm, msg.getMessage(), token);
                                                notifShown = true;
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    })));

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(senderUid);
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Long msgcount = Objects.requireNonNull(user).getMsgs();
                        Long cnt = msgcount + 1;
                        HashMap<String, Object> cntObj = new HashMap<>();
                        cntObj.put("msgs", cnt);
                        ref.updateChildren(cntObj);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("users").child(recieverUid);
                ref1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Long msgcount = Objects.requireNonNull(user).getMsgs();
                        Long cnt = msgcount + 1;
                        HashMap<String, Object> cntObj = new HashMap<>();
                        cntObj.put("msgs", cnt);
                        ref1.updateChildren(cntObj);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                        .child("chatslist")
                        .child(senderUid)
                        .child(recieverUid);
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            reference.child("id").setValue(recieverUid);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                DatabaseReference dbref = FirebaseDatabase.getInstance().getReference()
                        .child("chatslist")
                        .child(recieverUid)
                        .child(senderUid);
                dbref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!snapshot.exists()) {
                            dbref.child("id").setValue(senderUid);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

        });

        binding.linearLayoutqt.setOnClickListener(v -> binding.recyclerView.smoothScrollToPosition(position));

        final Handler handler = new Handler();
        binding.messegeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }
            final Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(senderUid).setValue("Online");
                }
            };
        });

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

    }

    void sendNotification(String name, String message, String token) {
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);
            JSONObject notificationData = new JSONObject();
            notificationData.put("notification", data);
            notificationData.put("to", token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData, response -> {
            }, error -> Toast.makeText(ChatActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show()) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> map = new HashMap<>();
                    String key = "Key=AAAAFmU4S-4:APA91bGEUwZoU5bmTVm_joBh4G-slgG3KnkhFeIR9IT2bCeiIxlyjLPv8pcSBsJ0VRXXVz6uiVWkNeASTKByCWtmJCMbTquwfohn5x1VkvlBpZaDT-eqIiAtNt8Yufp7bGdQkAPfseDu";
                    map.put("Content-Type", "application/json");
                    map.put("Authorization",key);

                    return map;
                }
            };

            queue.add(request);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void seenMessage(String recieverUid) {

      reference = FirebaseDatabase.getInstance().getReference().child("chats").child(recieverRoom).child("messages");

      fuser = FirebaseAuth.getInstance().getCurrentUser();

        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                        Message message = snapshot1.getValue(Message.class);

                        if (Objects.requireNonNull(message).getRecieverId().equals(senderUid) && message.getSenderId().equals(recieverUid)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isseen", true);
                            snapshot1.getRef().updateChildren(hashMap);
                        }
                    }
                    adapter.notifyItemChanged(2);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref = FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("messages");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        slistener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                        Message message = snapshot1.getValue(Message.class);

                        if (Objects.requireNonNull(message).getRecieverId().equals(senderUid) && message.getSenderId().equals(recieverUid)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isseen", true);
                            snapshot1.getRef().updateChildren(hashMap);
                        }

                    }
                    adapter.notifyItemChanged(2);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(Objects.requireNonNull(currentId)).setValue("Offline");
        reference.removeEventListener(seenListener);
        ref.removeEventListener(slistener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

}