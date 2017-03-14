package dndproductions.musicplayerlite;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

/**
 * Adapter that's used for displaying the songs to the ListView via MainActivity.
 */
public class SongAdapter extends ArrayAdapter<Song> {

    /**
     * Provides a view for an AdapterView (ListView, GridView, and etc.).
     *
     * @param context is an Activity context.
     * @param songList is a song list.
     */
    public SongAdapter(Context context, List<Song> songList){
        super(context, 0, songList);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, and etc.).
     *
     * @param position is the position in the list of data that should be displayed in the
     *                 list item view.
     * @param convertView is the recycled view to populate.
     * @param parent is the parent ViewGroup that is used for inflation.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Initializes the following to hold child views later on.
        ViewHolder holder;

        // Checks if the existing view is being reused, otherwise inflates the view.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent,
                    false);

            // Initializes the rest of the child views for the sake of not looking them up
            // repeatedly.
            holder = new ViewHolder();
            holder.song = (TextView) listItemView.findViewById(R.id.song_title);
            holder.artist = (TextView) listItemView.findViewById(R.id.song_artist);

            // Associates the holder with the view for later lookup.
            listItemView.setTag(holder);
        } else {

            // Otherwise, the view already exists so retrieve it.
            holder = (ViewHolder) listItemView.getTag();
        }

        // Retrieves each song in the array with the position/index parameter.
        Song currentSong = getItem(position);

        // Sets the song's details as texts, accordingly.
        if (currentSong != null) {
            holder.song.setText(currentSong.getTitle());
            holder.artist.setText(currentSong.getArtist());
        }

        return listItemView;
    }

    // ViewHolder class used to hold the set of views.
    private static class ViewHolder {
        TextView song;
        TextView artist;
    }
}
