package com.abeed.mesme.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abeed.mesme.Activities.ChatActivity;
import com.abeed.mesme.Activities.UsrDpActivity;
import com.abeed.mesme.Models.Message;
import com.abeed.mesme.Models.User;
import com.abeed.mesme.R;
import com.abeed.mesme.databinding.RemoveUserBinding;
import com.abeed.mesme.databinding.RowConversationBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    FirebaseUser fuser;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    Context context;
    ArrayList<User> users;

    public UsersAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    public void setFilteredList(ArrayList<User> filteredList) {
        this.users = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);

        if (user.getUid().equals("OUEqrnSmEVd9FuGprMKREEKoqX42")) {
            holder.binding.usrtick.setVisibility(View.VISIBLE);
        } else {
            holder.binding.usrtick.setVisibility(View.GONE);
        }

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();

        holder.binding.userName.setText(user.getName());

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.icuser)
                .into(holder.binding.profile);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("image", user.getProfileImage());
            intent.putExtra("uid", user.getUid());
            intent.putExtra("about", user.getAbout());
            intent.putExtra("token", user.getToken());
            context.startActivity(intent);
        });

        holder.binding.profile.setOnClickListener(v -> {
            Intent intent = new Intent(context, UsrDpActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("profile", user.getProfileImage());
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            @SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.remove_user, null);
            RemoveUserBinding binding = RemoveUserBinding.bind(view);
            AlertDialog dialog = new AlertDialog.Builder(context)
                    .setTitle("Remove User?")
                    .setMessage("Even the user is removed from your Chats list, the chat is not deleted.")
                    .setView(binding.getRoot())
                    .create();
            binding.remove1.setOnClickListener(v1 -> FirebaseDatabase.getInstance().getReference()
                    .child("chatslist")
                    .child(Objects.requireNonNull(senderId))
                    .child(user.getUid()).removeValue().addOnSuccessListener(unused -> dialog.dismiss()).addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Error : Unable to remove user", Toast.LENGTH_SHORT).show();
                    }));

            binding.cancel1.setOnClickListener(v12 -> dialog.dismiss());
            dialog.show();

            return false;
        });

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference().child("chats").child(senderRoom).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {

                                Message message = snapshot1.getValue(Message.class);
                                String isn = Objects.requireNonNull(message).getIsseen().toString();

                                if ((!isn.equals("true")) && (fuser.getUid().equals(message.getRecieverId()))) {
                                    holder.binding.newmsgbbl.setVisibility(View.VISIBLE);
                                } else {
                                    holder.binding.newmsgbbl.setVisibility(View.GONE);
                                }

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
