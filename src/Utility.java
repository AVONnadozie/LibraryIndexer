
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
     * Provide regular expressions that match allowed eBook formats
     *
     * @return
     */
    public static String getAllowedEBookExtentions() {
        return "((.[Dd][Oo][Cc][Xx]?)|(.[Pp][Dd][Ff])|(.[Tt][Xx][Tt])|(.[Pp]{2}[Tt][Xx]?))";
    }

    /**
     * Provide regular expressions that match allowed music and video formats
     *
     * @return
     */
    public static String getAllowedCDExtentions() {
        return "((.[Mm][Pp][34])|(.[Ff][Ll][Vv])|(.[Aa][Vv][Ii]))";
    }

    /**
     * Provide regular expressions that match all allowed formats for library
     * materials
     *
     * @return
     */
    public static String getAllowedMaterialsExtensions() {
        return "(" + getAllowedEBookExtentions() + "|" + getAllowedCDExtentions() + ")";
    }
}
