package com.example.application.musicplayer.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.example.application.musicplayer.Fragments.CloudSongFragment;
import com.example.application.musicplayer.Fragments.DeviceAlbumFragment;
import com.example.application.musicplayer.Model.CreateNotification;
import com.example.application.musicplayer.Service.OnClearFromRecentService;
import com.example.application.musicplayer.User.UserData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.application.musicplayer.Adapter.ViewPagerAdapter;
import com.example.application.musicplayer.DB.FavoritesOperations;
import com.example.application.musicplayer.Fragments.DeviceSongFragment;
import com.example.application.musicplayer.Fragments.CurrentSongFragment;
import com.example.application.musicplayer.Fragments.FavSongFragment;
import com.example.application.musicplayer.Fragments.DeviceArtistFragment;
import com.example.application.musicplayer.Model.SongsList;
import com.example.application.musicplayer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DeviceSongFragment.createDataParse, FavSongFragment.createDataParsed, CurrentSongFragment.createDataParsed, CloudSongFragment.createDataParse, DeviceArtistFragment.createDataParse, DeviceAlbumFragment.createDataParse {
    public static ArrayList<SongsList> currentOfflineList = new ArrayList<>();
    private Menu menu;
//    public static boolean isLoggedIn = false;
    private FirebaseUser user;
    private DatabaseReference reference;
    private ImageButton imgBtnPlayPause, imgbtnReplay, imgbtnShuffle, imgBtnPrev, imgBtnNext, imgBtnSetting, imgBtnMode;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private SeekBar seekbarController;
    private DrawerLayout mDrawerLayout;
    private TextView tvCurrentTime, tvTotalTime;
    public static int playMode = 1;
    private int playButton = R.drawable.play_icon;
    private ArrayList<SongsList> songList;
    private ArrayList<SongsList> defaultSongList = new ArrayList<>();
    private int currentPosition;
    private String searchText = "";
    private SongsList currSong;
    private CreateNotification notification;

    private boolean checkFlag = false, repeatFlag = false, shuffleFlag = false,  playContinueFlag = false, favFlag = true, playlistFlag = false;
    private final int MY_PERMISSION_REQUEST = 100;
    private int allSongLength;

    MediaPlayer mediaPlayer;
    Handler handler;
    Runnable runnable;
    NotificationManager notificationManager;
    Intent openNotification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        Log.e("ABC", "Started");
        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        openNotification = new Intent(getBaseContext(), OnClearFromRecentService.class);
        startService(openNotification);
        init();
        grantedPermission();
        notification = new CreateNotification(this);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");
            Log.v("pos1", "action: " + action);
            if (action == null){
                return;
            }
            switch (action) {
                case CreateNotification.ACTION_PREV:
                    playPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    playPause();
                    break;
                case CreateNotification.ACTION_NEXT:
                    playNext();
                    break;
                case CreateNotification.ACTION_SEEK:
                    long pos = intent.getExtras().getLong("pos");
                    Log.v("pos1", pos + "");
                    mediaPlayer.seekTo((int) pos);
                    tvCurrentTime.setText(getTimeFormatted(pos));
                    seekbarController.setProgress((int) pos);
                    break;
            }
        }
    };


    /**
     * Initialising the views
     */

    private void init() {


        imgBtnPrev = findViewById(R.id.img_btn_previous);
        imgBtnNext = findViewById(R.id.img_btn_next);
        //imgbtnReplay = findViewById(R.id.img_btn_replay);
        imgBtnMode = findViewById(R.id.img_btn_mode);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        FloatingActionButton refreshSongs = findViewById(R.id.btn_refresh);
        seekbarController = findViewById(R.id.seekbar_controller);
        viewPager = findViewById(R.id.songs_viewpager);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        imgBtnPlayPause = findViewById(R.id.img_btn_play);
        Toolbar toolbar = findViewById(R.id.toolbar);
        handler = new Handler();
        mediaPlayer = new MediaPlayer();

        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        imgBtnNext.setOnClickListener(this);
        imgBtnPrev.setOnClickListener(this);
        //imgbtnReplay.setOnClickListener(this);
        refreshSongs.setOnClickListener(this);
        imgBtnPlayPause.setOnClickListener(this);
        imgBtnMode.setOnClickListener(this);
        Menu nav_menu = navigationView.getMenu();
        MenuItem user_item = nav_menu.findItem(R.id.username);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            nav_menu.findItem(R.id.nav_login).setVisible(false);
//            user = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference("user");
            reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserData userData = snapshot.getValue(UserData.class);
                    if (userData != null) {

                        user_item.setTitle("User: " + userData.getName());
                        user_item.setVisible(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            nav_menu.findItem(R.id.nav_login).setVisible(true);
            nav_menu.findItem(R.id.nav_logout).setVisible(false);
            user_item.setVisible(false);
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_about:
                        about();
                        break;
                    case R.id.nav_login:
                        startActivity(new Intent(MainActivity.this, LogIn.class));
                        break;
                    case R.id.nav_logout:
                        log_out();
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Function to ask user to grant the permission.
     */

    private void log_out() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu nav_menu = navigationView.getMenu();
        MenuItem user_item = nav_menu.findItem(R.id.username);
        user_item.setVisible(false);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Log out?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setMessage("Are you sure you want to log out?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                isLoggedIn = false;
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "LOGOUT SUCCESS!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LogIn.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }
    private void grantedPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        } else {
            setPagerLayout(1);
        }
    }

    /**
     * Checking if the permission is granted or not
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                        setPagerLayout(1);
                    } else {
                        Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission1", Snackbar.LENGTH_LONG);
                        snackbar.show();
                        finish();
                    }
                }
        }
    }

    /**
     * Setting up the tab layout with the viewpager in it.
     */

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Show Notification";
            String description = "This allows you to show notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setPagerLayout(int item) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getContentResolver());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(item);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    /**
     * Function to show the dialog for about us.
     */
    private void about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about))
                .setMessage(getString(R.string.about_text))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                queryText();
                setPagerLayout(0);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_favorites:
                if (checkFlag)
                    if (mediaPlayer != null) {
                        if (favFlag) {
                            Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                            item.setIcon(R.drawable.ic_favorite_filled);
                            SongsList favList = new SongsList(songList.get(currentPosition).getTitle(),
                                    songList.get(currentPosition).getSubTitle(), songList.get(currentPosition).getLink());
                            FavoritesOperations favoritesOperations = new FavoritesOperations(this);
                            favoritesOperations.addSongFav(favList);
                            setPagerLayout(0);
                            favFlag = false;
                        } else {
                            item.setIcon(R.drawable.favorite_icon);
                            favFlag = true;
                        }
                    }
                return true;
        }

        return super.onOptionsItemSelected(item);

    }



    /**
     * Function to handle the click events.
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_btn_play:
                playPause();
                break;
            case R.id.img_btn_previous:
                playPrevious();
                break;
            case R.id.img_btn_next:
                playNext();
                break;
            case R.id.btn_refresh:
                Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show();
                setPagerLayout(0);
                break;
//            case R.id.img_btn_replay:
//
//                if (repeatFlag) {
//                    Toast.makeText(this, "Replaying Removed..", Toast.LENGTH_SHORT).show();
//                    mediaPlayer.setLooping(false);
//                    repeatFlag = false;
//                } else {
//                    Toast.makeText(this, "Replaying Added..", Toast.LENGTH_SHORT).show();
//                    mediaPlayer.setLooping(true);
//                    repeatFlag = true;
//                }
//                break;
//            case R.id.img_btn_shuffle:
//                if (shuffleFlag) {
//                    Toast.makeText(this, "Shuffling Removed..", Toast.LENGTH_SHORT).show();
//                    songList = defaultSongList;
//                } else {
//                    Toast.makeText(this, "Shuffling Added..", Toast.LENGTH_SHORT).show();
//                    defaultSongList = songList;
//                    Collections.shuffle(songList);
//
//                }
//                break;
//            case R.id.img_btn_setting:
//                if (!playContinueFlag) {
//                    playContinueFlag = true;
//                    Toast.makeText(this, "Loop Added", Toast.LENGTH_SHORT).show();
//                } else {
//                    playContinueFlag = false;
//                    Toast.makeText(this, "Loop Removed", Toast.LENGTH_SHORT).show();
//                }
//                break;
            case R.id.img_btn_mode:
                changeMode();
        }
    }

    private void playPause() {
        if (checkFlag) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playButton = R.drawable.play_icon;
                Log.v("curPos", currentPosition + " \\\\ " + songList);
                notification.createNotification(songList.get(currentPosition), playButton
                        , mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), 0);
            } else if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                playButton = R.drawable.pause_icon;
                Log.v("curPos", currentPosition + "");
                notification.createNotification(songList.get(currentPosition), playButton
                        , mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), 1F);
                playCycle();
            }
        } else {
            Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
        }
        imgBtnPlayPause.setImageResource(playButton);
    }
    private void changeMode() {
        if (playMode == 1) {
            imgBtnMode.setImageResource(R.drawable.repeat_one_icon);
            playMode = 2;
        }
        else if (playMode == 2) {
            imgBtnMode.setImageResource(R.drawable.shuffle_icon);
            playMode = 3;
            setPagerLayout(5);
            if (currentOfflineList != null) Collections.shuffle(currentOfflineList);
        }
        else if (playMode == 3) {
            playMode = 1;
            imgBtnMode.setImageResource(R.drawable.repeat_icon);
        }
    }
    private void playNext() {
        if (checkFlag) {
            if (currentPosition + 1 < songList.size()) {
                attachMusic(songList.get(currentPosition + 1).getTitle(), songList.get(currentPosition + 1).getLink());
                currentPosition += 1;
            } else {
                if (playMode == 1) {
                    attachMusic(songList.get(0).getTitle(), songList.get(0).getLink());
                    currentPosition = 0;
                }
                else {
                    Toast.makeText(this, "Playlist Ended", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
        }
        playButton = R.drawable.pause_icon;
        notification.createNotification(songList.get(currentPosition), playButton
                , mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), 1F);
    }

    private void playPrevious() {
        if (checkFlag) {
            if (mediaPlayer.getCurrentPosition() > 10) {
                if (currentPosition - 1 > -1) {
                    attachMusic(songList.get(currentPosition - 1).getTitle(), songList.get(currentPosition - 1).getLink());
                    currentPosition = currentPosition - 1;
                } else {
                    if (playMode == 1) {
                        attachMusic(songList.get(songList.size() - 1).getTitle(), songList.get(songList.size() - 1).getLink());
                        currentPosition = songList.size() - 1;
                    }
                    else {
                        attachMusic(songList.get(currentPosition).getTitle(), songList.get(currentPosition).getLink());
                    }
                }
            } else {
                attachMusic(songList.get(currentPosition).getTitle(), songList.get(currentPosition).getLink());
            }
        } else {
            Toast.makeText(this, "Select a Song . .", Toast.LENGTH_SHORT).show();
        }
        playButton = R.drawable.pause_icon;
        notification.createNotification(songList.get(currentPosition), playButton
                , mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), 1F);
    }

    /**
     * Function to attach the song to the music player
     *
     * @param name
     * @param path
     */

    private void attachMusic(String name, String path) {
        imgBtnPlayPause.setImageResource(R.drawable.play_icon);
        setTitle(name);
        menu.getItem(1).setIcon(R.drawable.favorite_icon);
        favFlag = true;

        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            setControls();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgBtnPlayPause.setImageResource(R.drawable.play_icon);
                if (playContinueFlag) {
                    if (currentPosition + 1 < songList.size()) {
                        attachMusic(songList.get(currentPosition + 1).getTitle(), songList.get(currentPosition + 1).getLink());
                        currentPosition += 1;
                        notification.createNotification(songList.get(currentPosition), playButton
                                , mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), 0);
                    } else {
                        Toast.makeText(MainActivity.this, "PlayList Ended", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Function to set the controls according to the song
     */

    private void setControls() {
        seekbarController.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        playCycle();
        checkFlag = true;
        if (mediaPlayer.isPlaying()) {
            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
            tvTotalTime.setText(getTimeFormatted(mediaPlayer.getDuration()));
        }

        seekbarController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(getTimeFormatted(progress));
                    if (mediaPlayer.isPlaying()){
                        notification.createNotification(songList.get(currentPosition),
                                playButton, mediaPlayer.getDuration(), progress, 1F);
                    }else {
                        notification.createNotification(songList.get(currentPosition),
                                playButton, mediaPlayer.getDuration(), progress, 0);
                    }
                }
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (playMode == 2) {
                            attachMusic(songList.get(currentPosition).getTitle(), songList.get(currentPosition).getLink());
                        }
                        else {
                            playNext();
                        }
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Function to play the song using a thread
     */
    private void playCycle() {
        try {
            seekbarController.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrentTime.setText(getTimeFormatted(mediaPlayer.getCurrentPosition()));
            if (mediaPlayer.isPlaying()) {
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        playCycle();

                    }
                };
                handler.postDelayed(runnable, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getTimeFormatted(long milliSeconds) {
        String finalTimerString = "";
        String secondsString;

        //Converting total duration into time
        int hours = (int) (milliSeconds / 3600000);
        int minutes = (int) (milliSeconds % 3600000) / 60000;
        int seconds = (int) ((milliSeconds % 3600000) % 60000 / 1000);

        // Adding hours if any
        if (hours > 0)
            finalTimerString = hours + ":";

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // Return timer String;
        return finalTimerString;
    }

    /**
     * Function Overrided to receive the data from the fragment
     *
     * @param name
     * @param path
     */

    @Override
    public void onDataPass(String name, String path) {
        Toast.makeText(this, name, Toast.LENGTH_LONG).show();
        attachMusic(name, path);
    }

    @Override
    public void getLength(int length) {
        this.allSongLength = length;
    }

    @Override
    public void fullSongList(ArrayList<SongsList> songList, int position) {
        this.songList = songList;
        this.currentPosition = position;
        this.playlistFlag = songList.size() == allSongLength;
        Log.v("mainSong", "songlist size " + songList.size() + " " + allSongLength);
        this.playContinueFlag = !playlistFlag;
        Log.v("mainSong", "playContinueFlag " + playContinueFlag);
        notification.createNotification(songList.get(currentPosition), R.drawable.pause_icon
                , mediaPlayer.getDuration(), mediaPlayer.getCurrentPosition(), 1F);
    }

    @Override
    public String queryText() {
        return searchText.toLowerCase();
    }

    @Override
    public SongsList getSong() {
//        currentPosition = -1;
        return currSong;
    }

    @Override
    public boolean getPlaylistFlag() {
        return playlistFlag;
    }

    @Override
    public void currentSong(SongsList songsList) {
        this.currSong = songsList;
    }

    @Override
    public int getPosition() {
        return currentPosition;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
//        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        notificationManager.cancel(CreateNotification.id);
        stopService(openNotification);
        unregisterReceiver(broadcastReceiver);
        mediaPlayer.release();
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }
}
