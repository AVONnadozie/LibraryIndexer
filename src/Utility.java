
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class Utility {

    private static ExecutorService executor;

    public static ExecutorService getExecutorService() {
        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
        return executor;
    }

    public static void writeLog(Exception e) {
        Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, e);
    }

    public static void centreOnParent(Window parent, Window child) {
        if (parent == null) {
            centreOnScreen(child);
        } else {
            int x = parent.getLocationOnScreen().x + (parent.getWidth() - child.getWidth()) / 2;
            int y = parent.getLocationOnScreen().y + (parent.getHeight() - child.getHeight()) / 2;
            child.setLocation(x, y);
            child.setIconImages(parent.getIconImages());
        }
    }

    public static void centreOnScreen(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2.0D);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2.0D);
        frame.setLocation(x, y);
    }

    /**
     *
     * @return Index folder location
     */
    public static String getIndexFolderLocation() {
        return "indexer_files"; //root folder
    }
}
