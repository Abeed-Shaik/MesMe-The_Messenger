package com.abeed.mesme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abeed.mesme.Activities.ChatActivity;
import com.abeed.mesme.Activities.SelUsrDpActivity;
import com.abeed.mesme.Models.User;
import com.abeed.mesme.R;
import com.abeed.mesme.databinding.SelectUserSampleBinding;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class SelUserAdapter extends RecyclerView.Adapter<SelUserAdapter.UsersViewHolder> {

    Context context;
    ArrayList<User> users;
    FirebaseAuth auth;

    public SelUserAdapter(Context context, ArrayList<User> users) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.select_user_sample, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);

        if(holder.getAdapterPosition() == 0) {
            holder.binding.trendusr.setVisibility(View.VISIBLE);
        } else {
            holder.binding.trendusr.setVisibility(View.GONE);
        }

        if (user.getUid().equals("OUEqrnSmEVd9FuGprMKREEKoqX42")) {
            holder.binding.bltick.setVisibility(View.VISIBLE);
        } else {
            holder.binding.bltick.setVisibility(View.GONE);
        }

        auth = FirebaseAuth.getInstance();

        holder.binding.about.setText(user.getAbout());
        holder.binding.userName.setText(user.getName());

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.icuser)
                .into(holder.binding.profile);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("image", user.getProfileImage());
            intent.putExtra("uid", user.getUid());
            context.startActivity(intent);
        });
        holder.binding.profile.setOnClickListener(v -> {
            Intent intent = new Intent(context, SelUsrDpActivity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("profile", user.getProfileImage());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        SelectUserSampleBinding binding;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SelectUserSampleBinding.bind(itemView);
        }
    }
}
