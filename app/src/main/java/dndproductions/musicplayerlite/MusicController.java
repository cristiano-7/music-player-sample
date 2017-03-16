package dndproductions.musicplayerlite;

import android.content.Context;
import android.widget.MediaController;

/**
 * A subclass of {@link MediaController} that presents a widget with song functionality including
 * play/pause, fast-forward/rewind, and etc. The widget also contains a seek bar, which updates as
 * the song plays and contains text indicating the duration of the song and the player's current
 * position.
 */
public class MusicController extends MediaController {

    /**
     * Creates a {@link MusicController} object.
     *
     * @param context is the Activity's context.
     */
    public MusicController(Context context){
        super(context);
    }

    /**
     * Overrides the following to stop the control bar from being hidden within three seconds.
     */
    @Override
    public void hide(){

    }
}
