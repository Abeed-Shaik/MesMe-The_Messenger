package com.abeed.mesme.Activities;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.abeed.mesme.Adapters.UsersAdapter;
import com.abeed.mesme.Models.Chatslist;
import com.abeed.mesme.Models.User;
import com.abeed.mesme.R;
import com.abeed.mesme.databinding.ActivityMainBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseDatabase database;
    ArrayList<Chatslist> userlist;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;

    public static int UPDATE_CODE = 22;
    AppUpdateManager appUpdateManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.status_blue));
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(Color.parseColor("#0099FF")));

        inAppUp();

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(aBoolean -> {

            String backgroundImage = mFirebaseRemoteConfig.getString("backgroundImage");
            boolean isbackgroundImageEnabled = mFirebaseRemoteConfig.getBoolean("backgroundImageEnabled");
            if (isbackgroundImageEnabled) {
                Glide.with(MainActivity.this)
                        .load(backgroundImage)
                        .placeholder(R.drawable.chatscrbg)
                        .into(binding.backgroundImage);
            } else {
                return;
            }

            String toolbarColor = mFirebaseRemoteConfig.getString("toolbarColor");
            String toolbarImage = mFirebaseRemoteConfig.getString("toolbarImage");
            boolean istoolBarImageEnabled = mFirebaseRemoteConfig.getBoolean("toolBarImageEnabled");

            if (istoolBarImageEnabled) {
                Glide.with(MainActivity.this)
                        .load(toolbarImage)
                        .placeholder(R.drawable.toolbarholder)
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                Objects.requireNonNull(getSupportActionBar())
                                        .setBackgroundDrawable(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            } else {
                Objects.requireNonNull(getSupportActionBar())
                        .setBackgroundDrawable(new ColorDrawable(Color.parseColor(toolbarColor)));
            }
        });

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(token -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("token", token);
                    database.getReference()
                            .child("users")
                            .child(Objects.requireNonNull(auth.getCurrentUser()).getUid())
                            .updateChildren(map);
                });

        users = new ArrayList<>();

        usersAdapter = new UsersAdapter(this, users);
        binding.recyclerView.setAdapter(usersAdapter);

        binding.floatimgbtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SelectContactActivity.class);
            startActivity(intent);
        });

        userlist = new ArrayList<>();
        firebaseUser = auth.getCurrentUser();

        FirebaseDatabase.getInstance().getReference()
                .child("chatslist")
                .child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userlist.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    Chatslist chatslist = ds.getValue(Chatslist.class);
                    userlist.add(chatslist);
                }
                database.getReference().child("users").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            User user = snapshot1.getValue(User.class);
                            for (Chatslist chatslist : userlist) {
                                if (Objects.requireNonNull(user).getUid().equals(chatslist.getId())) {
                                    users.add(user);
                                }
                            }
                        }
                        usersAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void inAppUp() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> task = appUpdateManager.getAppUpdateInfo();
        task.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.FLEXIBLE,MainActivity.this, UPDATE_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    Log.d("update error", "onSuccess: "+ e);
                }
            }
        });
        appUpdateManager.registerListener(listener);
    }

    InstallStateUpdatedListener listener = installState -> {
        if(installState.installStatus() == InstallStatus.DOWNLOADED){
            popUp();
        }
    };

    private void popUp() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"App Update Almost Done.",Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Reload", v -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(Color.parseColor("#FF0000"));
        snackbar.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_CODE){
            if (resultCode != RESULT_OK){
                Log.e("error download", "onActivityResult: app download failed");
            }
        }
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
        getMenuInflater().inflate(R.menu.topmenu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
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
            Toast.makeText(MainActivity.this, "User not Found", Toast.LENGTH_SHORT).show();
        } else {
            usersAdapter.setFilteredList(filteredList);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.settings) {
            startActivity(new Intent(MainActivity.this, ProfileSettingActivity.class));
            return  true;
        }
        if (id == R.id.aboutapp) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        if (id == R.id.policy) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://asitro.blogspot.com/2022/10/mesme-privacy-policy.html"));
            startActivity(intent);
            return true;
        }
        if (id == R.id.share) {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT,"MesMe");
                intent.putExtra(Intent.EXTRA_TEXT,"https://play.google.com/store/apps/details?id="+getApplicationContext().getPackageName());
                startActivity(Intent.createChooser(intent,"Share With"));
            }catch (Exception e){
               Toast.makeText(this,"Unable to share this app.", Toast.LENGTH_SHORT).show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
