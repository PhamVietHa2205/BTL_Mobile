package com.example.application.musicplayer.Model;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.example.application.musicplayer.Activity.MainActivity;
import com.example.application.musicplayer.Model.SongsList;
import com.example.application.musicplayer.R;
import com.example.application.musicplayer.Service.NotificationActionService;

public class CreateNotification {
    public static final String CHANNEL_ID = "CHANNEL_1";
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREV = "PREVIOUS";
    public static final String ACTION_PLAY = "PLAY";

    public static final String ACTION_SEEK = "SEEK";
    public static final int id = 1;
    private NotificationCompat.Builder notification;
    private final MediaSessionCompat mediaSession;
    private final PendingIntent pendingIntentPrevious;
    private final PendingIntent pendingIntentPlay;
    private final PendingIntent pendingIntentNext;
    private final PendingIntent contentIntent;
    private PendingIntent pendingIntentSeek;
    private final Intent intentSeekTo;
    private final Context context;
    private final NotificationManagerCompat managerCompat;

    public CreateNotification(Context context){
        this.context = context;
        this.mediaSession = new MediaSessionCompat(context, "tag");
        this.mediaSession.setActive(true);
        this.managerCompat = NotificationManagerCompat.from(context);
        Intent notificationIntent = new Intent(context, MainActivity.class);
        contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        intentSeekTo = new Intent(context, NotificationActionService.class).setAction(ACTION_SEEK);
//        pendingIntentSeek = PendingIntent.getBroadcast(context, 0, intentSeekTo, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intentPrevious = new Intent(context, NotificationActionService.class).setAction(ACTION_PREV);
        pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intentPlay = new Intent(context, NotificationActionService.class).setAction(ACTION_PLAY);
        pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intentNext = new Intent(context, NotificationActionService.class).setAction(ACTION_NEXT);
        pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                updatePlaybackState(pos);
                intentSeekTo.putExtra("pos", pos);
                pendingIntentSeek = PendingIntent.getBroadcast(context, 0,
                        intentSeekTo, PendingIntent.FLAG_UPDATE_CURRENT);
                try {
                    pendingIntentSeek.send();
//                    pendingIntentSeek.notify();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void createNotification(SongsList track, int playButton, long duration, long position, float speed) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), track.getThumbnail());
            mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, track.getTitle())
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, track.getSubTitle())
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
                    .putBitmap(MediaMetadata.METADATA_KEY_ART, icon)
                    .build());
            mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, position, speed)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build());
            notification = new NotificationCompat.Builder(context, CHANNEL_ID).
                    setSmallIcon(R.drawable.play_icon).
                    setContentTitle(track.getTitle()).
                    setContentText(track.getSubTitle()).
                    setContentIntent(contentIntent).
                    setLargeIcon(icon).
                    setOnlyAlertOnce(true).
                    setShowWhen(false).
                    addAction(R.drawable.previous_icon, "Previous", pendingIntentPrevious).
                    addAction(playButton, "Pause", pendingIntentPlay).
                    addAction(R.drawable.next_icon, "Next", pendingIntentNext).
                    setStyle(new MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(mediaSession.getSessionToken())).
                    setPriority(NotificationCompat.PRIORITY_MAX);
            managerCompat.notify(id, notification.build());
        }
    }

    private void updatePlaybackState(long pos){
        float speed = mediaSession.getController().getPlaybackState().getPlaybackSpeed();
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, pos, speed)
                .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                .build());
    }
}

