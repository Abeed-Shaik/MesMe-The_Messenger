package com.abeed.mesme.Activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.abeed.mesme.Adapters.SelUserAdapter;
import com.abeed.mesme.Models.User;
import com.abeed.mesme.R;
import com.abeed.mesme.databinding.ActivitySelectContactBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class SelectContactActivity extends AppCompatActivity {

    ActivitySelectContactBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    SelUserAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.status_blue));
        binding = ActivitySelectContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setTitle("Select User");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0099FF")));

                database = FirebaseDatabase.getInstance();
                users = new ArrayList<>();

                usersAdapter = new SelUserAdapter(this, users);
                binding.recyclerView.setAdapter(usersAdapter);

                database.getReference().child("users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            users.add(user);
                            Collections.sort(users, (o1, o2) -> o2.getMsgs().compareTo(o1.getMsgs()));
                        }
                        usersAdapter.notifyDataSetChanged();
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
            }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.selectuser, menu);
        MenuItem menuItem = menu.findItem(R.id.searchuser);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.clearFocus();
        searchView.setQueryHint("Search User");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filterList(newText);

                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void filterList(String text) {
        ArrayList<User> filteredList = new ArrayList<>();
        for (User user : users){
            if(user.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            } else if (user.getPhoneNumber().contains(text.toLowerCase())) {
                filteredList.add(user);
            }
        }
        if (filteredList.isEmpty()){
            Toast.makeText(SelectContactActivity.this, "User not Found", Toast.LENGTH_SHORT).show();
        } else {
            usersAdapter.setFilteredList(filteredList);
        }
    }

}