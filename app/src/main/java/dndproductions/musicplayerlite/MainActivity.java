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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import dndproductions.musicplayerlite.MusicService.MusicBinder;

/**
 * Music player app that initially retrieves the user's songs from their music library, and then
 * provides playback functionality.
 */
public class MainActivity extends AppCompatActivity {

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

        // The following code either executes for versions older than M, or until the user
        // accepts the in-app permission for the next sessions.
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Instantiates the Intent if it doesn't exist yet, binds to it, and then starts it.
        if (mPlayIntent == null) {
            mPlayIntent = new Intent(this, MusicService.class);
            bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
            startService(mPlayIntent);
        }
    }

    // Displays a permission dialog when requested for devices M and above.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
            } else { // User denies the permission.
                Toast.makeText(this, "Please grant the permissions for Music Player 2.0 and come" +
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
                Song song = mSongList.get(position);
                Log.d(LOG_TAG, song.getTitle());
                Log.d(LOG_TAG, song.getArtist());
            }
        });
    }

    // Connects to the service to bind the interaction between the Service class and the Activity.
    private ServiceConnection mMusicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;

            // Gets service.
            mMusicService = binder.getService();

            // Passes the song list.
            mMusicService.setList(mSongList);

            // Sets the boolean flag accordingly.
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicBound = false;
        }
    };

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
}
