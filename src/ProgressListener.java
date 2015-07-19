
import java.util.EventListener;


/**
 *
 * @author Victor Anuebunwa
 */
public interface ProgressListener extends EventListener{
    
    /**
     * Invoked when processing is about to start
     * @param message optional message. This can be a note on the operation about to be performed.
     */
    public void onStart(String message);
    
    /**
     * Invoked when a progress is made
     * @param message
     * @param progress 
     */
    public void update(String message, int progress);
    
    /**
     * Invoked when processing stops
     * @param message optional message. This can be a note on why the operation stopped.
     */
    public void onStop(String message);
    
}
