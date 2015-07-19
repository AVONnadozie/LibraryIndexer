
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Victor Anuebunwa
 */
public class Library {

    private static Library thisClass;
    private volatile ArrayList<Material> materials;
    private boolean busy;

    private Library() {
        materials = new ArrayList<>();
    }

    /**
     * Load materials from library locations<br/>
     * This might be a lengthy operation. therfore It is advisable no to invoke
     * this method on the Event dispatch thread.
     *
     * @return
     */
    public synchronized boolean init() {
        if (busy) {
            throw new IllegalStateException("Library is busy");
        }
        busy = true;
        loadLibrary();
//        materials.sort((Material o1, Material o2) -> o1.getTitle().compareTo(o2.getTitle()));
        busy = false;
        return true;
    }

    public ArrayList<Material> searchLibrary(String query) {
        if (busy || materials == null) {
            throw new IllegalStateException("Library is busy");
        }

        if (query.isEmpty()) {
            return materials;
        }

        ArrayList<Material> list = new ArrayList<>();
        String[] names;
        if (query.contains(" ")) {
            names = query.split("\\s");
        } else {
            names = new String[]{query};
        }
        materials.stream().forEach((material) -> {
            boolean found = false;
            for (String keyname : names) {
                //Search attributes for match
                keyname = keyname.toLowerCase();
                if (material.getTitle() != null) {
                    found = found || material.getTitle().toLowerCase().contains(keyname);
                }
                if (material.getAuthor() != null) {
                    found = found || material.getAuthor().toLowerCase().contains(keyname);
                }
                if (material.getISBN() != null) {
                    found = found || material.getISBN().toLowerCase().contains(keyname);
                }
            }
            if (found) {
                list.add(material);
            }
        });

        return list;
    }

    public boolean ready() {
        return !busy;
    }

    public static Library getInstance() {
        if (thisClass == null) {
            thisClass = new Library();
            thisClass.init();
        }
        return thisClass;
    }

    public ArrayList<Material> getMaterials() {
        return materials;
    }

    public Material getMaterial(URI path) {
        for (Material material : materials) {
            if (material.getPath().equals(path)) {
                return material;
            }
        }
        return null;
    }

    public void addMaterial(Material material) {
        materials.add(material);
    }

    /**
     * Synchronises local library with database library
     */
    private void loadLibrary() {
        try {
            materials = Database.getMaterials();
        } catch (InterruptedException | ExecutionException | NullPointerException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
