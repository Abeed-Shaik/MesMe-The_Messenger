package com.abeed.mesme.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abeed.mesme.Activities.ChatActivity;
import com.abeed.mesme.Models.Message;
import com.abeed.mesme.R;
import com.abeed.mesme.databinding.DeleteDialogBinding;
import com.abeed.mesme.databinding.ItemRecieveBinding;
import com.abeed.mesme.databinding.ItemSendBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Message> messages;
    final int ITEM_SEND = 1;
    final int ITEM_RECIEVE = 2;
    String senderRoom;
    String recieverRoom;
    FirebaseRemoteConfig remoteConfig;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String recieverRoom) {
        remoteConfig = FirebaseRemoteConfig.getInstance();
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.recieverRoom = recieverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_send, parent, false);
            return new SendViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_recieve, parent, false);
            return new RecieverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (Objects.equals(FirebaseAuth.getInstance().getUid(), message.getSenderId())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECIEVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder.getClass() == SendViewHolder.class) {
            SendViewHolder viewHolder = (SendViewHolder) holder;
            if (message.getQuotePos() == -1) {
                viewHolder.binding.linearLayoutqt.setVisibility(View.GONE);
                viewHolder.binding.message.setText(message.getMessage());
                long time = message.getTimestamp();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy - hh:mm a");
                viewHolder.binding.messagetm.setText(dateFormat.format(new Date(time)));
            } else {
                viewHolder.binding.linearLayoutqt.setVisibility(View.VISIBLE);
                viewHolder.binding.messageqtname.setText(message.getQoutename());
                viewHolder.binding.messageqt.setText(message.getQoute());
                viewHolder.binding.message.setText(message.getMessage());
                long time = message.getTimestamp();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy - hh:mm a");
                viewHolder.binding.messagetm.setText(dateFormat.format(new Date(time)));

            }

            viewHolder.binding.linearLayoutqt.setOnClickListener(v -> {
                int pos1 = (int) message.getQuoteMsgPos();
                if (pos1 != -1 ) {
                    Objects.requireNonNull(((ChatActivity) context).binding.recyclerView.getLayoutManager()).scrollToPosition(pos1);
                } else {
                    Toast.makeText(context, "Message doesn't exist", Toast.LENGTH_SHORT).show();
                }
            });

            viewHolder.itemView.setOnLongClickListener(v -> {
                @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
                DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setView(binding.getRoot())
                        .create();
                binding.everyone.setOnClickListener(v1 -> {
                    message.setMessage("🚫This message was deleted");
                    message.setQuotePos(-1);
                    message.setQuoteMsgPos((long) -1);

                    FirebaseDatabase.getInstance().getReference()
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(message.getMessageId()).setValue(message);

                    FirebaseDatabase.getInstance().getReference()
                            .child("chats")
                            .child(recieverRoom)
                            .child("messages")
                            .child(message.getMessageId()).setValue(message);
                    dialog.dismiss();
                });

                binding.delete.setOnClickListener(v12 -> {
                    FirebaseDatabase.getInstance().getReference()
                            .child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(message.getMessageId()).setValue(null);
                    dialog.dismiss();
                });

                binding.cancel.setOnClickListener(v13 -> dialog.dismiss());

                dialog.show();

                return false;
            });

            if (message.getIsseen()) {
                viewHolder.binding.heartSent.setVisibility(View.GONE);
                viewHolder.binding.heartSeen.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.heartSent.setVisibility(View.VISIBLE);
                viewHolder.binding.heartSeen.setVisibility(View.GONE);
            }

        } else {
            RecieverViewHolder viewHolder = (RecieverViewHolder) holder;

            if (message.getQuotePos() == -1) {
                viewHolder.binding.linearLayoutqt.setVisibility(View.GONE);
                viewHolder.binding.message.setText(message.getMessage());
                long time = message.getTimestamp();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy - hh:mm a");
                viewHolder.binding.messagetm.setText(dateFormat.format(new Date(time)));
            } else {
                viewHolder.binding.linearLayoutqt.setVisibility(View.VISIBLE);
                viewHolder.binding.messageqtname.setText(message.getQoutename());
                viewHolder.binding.messageqt.setText(message.getQoute());
                viewHolder.binding.message.setText(message.getMessage());
                long time = message.getTimestamp();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy - hh:mm a");
                viewHolder.binding.messagetm.setText(dateFormat.format(new Date(time)));

            }

            viewHolder.binding.linearLayoutqt.setOnClickListener(v -> {
                int pos1 = (int) message.getQuoteMsgPos();
                if (pos1 != -1 ) {
                    Objects.requireNonNull(((ChatActivity) context).binding.recyclerView.getLayoutManager()).scrollToPosition(pos1);
                } else {
                    Toast.makeText(context, "Message doesn't exist", Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SendViewHolder extends RecyclerView.ViewHolder {

        ItemSendBinding binding;

        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSendBinding.bind(itemView);
        }
    }

    public static class RecieverViewHolder extends RecyclerView.ViewHolder {

        ItemRecieveBinding binding;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemRecieveBinding.bind(itemView);
        }
    }

}