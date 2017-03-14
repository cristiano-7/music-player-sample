package dndproductions.musicplayerlite;

/**
 * Class used to model the data for a single audio file.
 */
public class Song {

    // Fields used as data for storing for each track.
    private long id;
    private String title;
    private String artist;

    /**
     * Creates a {@link Song} object.
     *
     * @param songID is the ID of the song.
     * @param songTitle is the title of the song.
     * @param songArtist is the artist of the song.
     */
    public Song(long songID, String songTitle, String songArtist) {
        id = songID;
        title = songTitle;
        artist = songArtist;
    }

    // Getter methods.
    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    /**
     * Converts a {@link Song} object to a string.
     */
    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }

    /**
     * Compares two objects - one of them being a {@link Song} object.
     *
     * @param o is the other object being compared with.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;
        return artist != null ? artist.equals(song.artist) : song.artist == null;

    }
}
