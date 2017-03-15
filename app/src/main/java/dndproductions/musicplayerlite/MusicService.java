package dndproductions.musicplayerlite;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.content.ContentUris;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;

import java.util.List;

/**
 * A subclass of {@link Service} that assists with executing music playback continuously even when
 * the app is minimized.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    // Log tag constant.
    private static final String LOG_TAG = MusicService.class.getSimpleName();

    // MediaPlayer field.
    private MediaPlayer mPlayer;

    // Song list field.
    private List<Song> mSongList;

    // Int field used for keeping track with the current position.
    private int mSongPosition;

    // Initialization used to assist with the binding process.
    private final IBinder mMusicBinder = new MusicBinder();

    @Override
    public void onCreate(){
        super.onCreate();

        // Initializations.
        mSongPosition = 0;
        initMusicPlayer();
    }

    /**
     * Initializing method for the MediaPlayer.
     */
    public void initMusicPlayer(){
        mPlayer = new MediaPlayer();

        // Allows playback to continue when the device becomes idle.
        mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

        // Sets the stream type to music.
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        // Sets the following to their respective listener.
        mPlayer.setOnPreparedListener(this); // When the MediaPlayer instance is prepared.
        mPlayer.setOnCompletionListener(this); // When a song has completed playback.
        mPlayer.setOnErrorListener(this); // When an error is thrown.
    }

    /**
     * Setter method for retrieving the song list from the Activity.
     *
     * @param songs is the list of songs.
     */
    public void setList(List<Song> songs){
        mSongList = songs;
    }

    /**
     * Assists with the interaction between the Activity and this Service class.
     */
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent){

        // Releases MediaPlayer resources when the Service is unbound (e.g. user exiting app).
        mPlayer.stop();
        mPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

    }
}
