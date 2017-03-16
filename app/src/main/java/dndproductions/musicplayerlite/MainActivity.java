package dndproductions.musicplayerlite;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ListView;
import android.view.View;
import android.widget.Toast;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController.MediaPlayerControl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dndproductions.musicplayerlite.MusicService.MusicBinder;

/**
 * Music player app that initially retrieves the user's songs from their music library, and then
 * provides playback functionality.
 */
public class MainActivity extends AppCompatActivity implements MediaPlayerControl {

    // Log tag constant.
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // Constant used as a parameter to assist with the permission requesting process.
    private final int PERMISSION_CODE = 1;

    // Fields used to assist with a song list UI.
    private List<Song> mSongList;
    private ListView mSongView;

    // Fields used for binding the interaction between the Activity and the Service class - the
    // music will be played in the Service class, but be controlled from the Activity.
    private MusicService mMusicService;
    private Intent mPlayIntent;
    private boolean mMusicBound = false;

    // Field used for setting the controller up.
    private MusicController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Requests permission for devices with versions Marshmallow (M)/API 23 or above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_CODE);

                return;
            }
        }

        // The following code either executes for versions older than M, or until the user accepts
        // the in-app permission for the next sessions.
        init();

        // Invokes the iteration for adding songs.
        getSongList();

        // Sorts the data so that the song titles are presented alphabetically.
        Collections.sort(mSongList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        // Custom adapter instantiation that displays the songs via the ListView.
        SongAdapter songAdapter = new SongAdapter(this, mSongList);
        mSongView.setAdapter(songAdapter);

        // Invokes the controller setup.
        setController();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Instantiates the Intent if it doesn't exist yet, binds to it, and then starts it.
        if (mPlayIntent == null) {
            Log.d(LOG_TAG, "Binding and starting service");

            mPlayIntent = new Intent(this, MusicService.class);
            bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.song_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Performs the following for the respective item.
        switch (item.getItemId()) {
            case R.id.option_shuffle:
                mMusicService.setShuffle();
                break;
            case R.id.option_end: // TEMPORARY
                stopService(mPlayIntent);
                mMusicService = null;
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Displays a permission dialog when requested for devices M and above.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {

            // User accepts the permission(s).
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();

                // Invokes the iteration for adding songs.
                getSongList();

                // Sorts the data so that the song titles are presented alphabetically.
                Collections.sort(mSongList, new Comparator<Song>(){
                    public int compare(Song a, Song b){
                        return a.getTitle().compareTo(b.getTitle());
                    }
                });

                // Custom adapter instantiation that displays the songs via the ListView.
                SongAdapter songAdapter = new SongAdapter(this, mSongList);
                mSongView.setAdapter(songAdapter);

                // Manually passes the song list since the ServiceConnection instance was binded
                // before the song list was formed.
                mMusicService.setList(mSongList);

                // Invokes the controller setup.
                setController();
            } else { // User denies the permission.
                Toast.makeText(this, "Please grant the permissions for Music Player Lite and come" +
                        " back again soon!", Toast.LENGTH_SHORT).show();

                // Runs a thread for a slight delay prior to shutting down the app.
                Thread mthread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            sleep(1500);
                            System.exit(0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };

                mthread.start();
            }
        }
    }

    /**
     * Initializing/instantiating method.
     */
    private void init() {
        mSongList = new ArrayList<>();
        mSongView = (ListView) findViewById(R.id.song_list);

        // Sets each song with a functionality.
        mSongView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                // Sets the respective song in the Service, and then plays it.
                mMusicService.setSong(position);
                mMusicService.playSong();
            }
        });
    }

    // Connects to the service to bind the interaction between the Service class and the Activity.
    private ServiceConnection mMusicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "onServiceConnected()");

            MusicBinder binder = (MusicBinder) service;

            // Gets service.
            mMusicService = binder.getService();

            // Passes the song list.
            mMusicService.setList(mSongList);

            // Sets the boolean flag accordingly.
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "onServiceDisconnected()");

            mMusicBound = false;
        }
    };

    /**
     * Sets the controller up.
     */
    private void setController() {
        mController = new MusicController(this);

        // Addresses when the user presses the previous/next buttons.
        mController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });

        // Sets the controller to work on media playback in the app, with its anchor view referring
        // to the song list.
        mController.setMediaPlayer(this);
        mController.setAnchorView(findViewById(R.id.song_list));
        mController.setEnabled(true);
    }

    /**
     * Plays the next song via the Service class.
     */
    private void playNext(){
        mMusicService.playNext();
        mController.show(0);
    }

    /**
     * Plays the previous song via the Service class.
     */
    private void playPrevious(){
        mMusicService.playPrevious();
        mController.show(0);
    }

    // Helper method used for retrieving audio file information.
    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();

        // Retrieves the URI for external music files.
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // Queries the music files.
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        // Initially checks to see if the data is valid.
        if (musicCursor != null && musicCursor.moveToFirst()) {

            // Column indexes used for retrieval purposes.
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);

            // Iterates and adds new Song objects to the list, accordingly..
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                mSongList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    // The following are MediaPlayerControl interface methods.
    @Override
    public void start() {
        mMusicService.go();
    }

    @Override
    public void pause() {
        mMusicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (mMusicService != null && mMusicBound && mMusicService.isPlaying()) {
            return mMusicService.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (mMusicService != null && mMusicBound && mMusicService.isPlaying()) {
            return mMusicService.getPosition();
        } else {
            return 0;
        }
    }

    @Override
    public void seekTo(int position) {
        mMusicService.seek(position);
    }

    @Override
    public boolean isPlaying() {
        if (mMusicService != null && mMusicBound) return mMusicService.isPlaying();

        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
