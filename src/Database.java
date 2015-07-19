
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 *
 * @author Anuebunwa Victor
 */
public class Database {

    private static final ArrayList<ProgressListener> listeners = new ArrayList<>();
    private static Connection con;

    private static Connection getConnection() throws SQLException {
        if (con == null) {
            con = DriverManager.getConnection("jdbc:mysql://localhost/library", "root", "");
        }
        return con;
    }

    /**
     *
     * @return Index folder location
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static ArrayList<Material> getMaterials() throws InterruptedException, ExecutionException, NullPointerException {
        Future<ArrayList<Material>> result = Utility.getExecutorService().submit(() -> {
            Statement s = getConnection().createStatement();
            ResultSet rs = s.executeQuery("select * from materials "
                    + "where is_excluded = 0 and in_library = 1 order by title ASC");
            ArrayList<Material> materials = new ArrayList<>();
            while (rs.next()) {
                //Create material
                try {
                    Material m = new Material(new URI(rs.getString("path")));
                    m.setAuthor(rs.getString("author"));
                    m.setDateAdded(rs.getTimestamp("date_added"));
                    m.setPreview(rs.getString("preview"));
                    m.setTitle(rs.getString("title"));
                    m.setKeywords(rs.getString("keywords").split(","));
                    MaterialType type = MaterialType.valueOf(rs.getString("type"));
                    m.setType(type);
                    m.setExcluded(rs.getBoolean("is_excluded"));
                    m.setLastModificationTime(rs.getTimestamp("last_modification_time"));
                    materials.add(m);
                } catch (SQLException | URISyntaxException e) {
                    Utility.writeLog(e);
                }
            }
            return materials;
        });
        return result.get();
    }

    /**
     *
     * @param path
     * @return Index folder location
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static String getMaterialPreview(String path) throws InterruptedException, ExecutionException, NullPointerException {
        Future<String> result = Utility.getExecutorService().submit(() -> {
            Statement s = getConnection().createStatement();
            ResultSet rs = s.executeQuery("select preview from materials where path = '" + path + "'");
            String preview = "";
            if (rs.next()) {
                preview = rs.getString(1);
            }
            return preview;
        });
        return result.get();
    }

    public static CharArraySet getStopWords() throws InterruptedException, ExecutionException {
        Future<CharArraySet> result = Utility.getExecutorService().submit(() -> {
            Statement s = getConnection().createStatement();
            ResultSet rs = s.executeQuery("select word from stopwords");
            rs.last();
            int rowSize = rs.getRow();
            CharArraySet array = new CharArraySet(rowSize, true);
            rs.beforeFirst();
            while (rs.next()) {
                array.add(rs.getString(1));
            }
            return array;
        });
        return result.get();
    }

    /**
     * Read and save index files from database
     *
     * @throws SQLException
     * @throws IOException
     */
    public static void readIndexes() throws SQLException, IOException {
        listeners.stream().forEach((listener) -> {
            listener.onStart("updating index files...");
        });

        Statement s = getConnection().createStatement();
        ResultSet rs = s.executeQuery("select * from index_files");
        rs.last();

        int allFiles = rs.getRow();
        int readFiles = 0;

        if (allFiles > 0) {
            //Clear files
            File file = new File(Utility.getIndexFolderLocation());
            File[] listFiles = file.listFiles();
            for (File listFile : listFiles) {
                listFile.delete();
            }
        }

        rs.beforeFirst();
        while (rs.next()) {
            File file = new File(Utility.getIndexFolderLocation(), rs.getString("file_name"));
            //Get modification dates
            long systemFileModDate = file.exists() ? file.lastModified() : 0L;
            long dbFileModDate = rs.getTimestamp("last_modified").getTime();
            //If database file is a newer version
            if (dbFileModDate > systemFileModDate) {
                //Open streams for input and output
                try (InputStream is = rs.getBlob("content").getBinaryStream();
                        FileOutputStream fos = new FileOutputStream(file)) {
                    int len;
                    byte data[] = new byte[1024];
                    while ((len = is.read(data)) != -1) {
                        fos.write(data, 0, len);
                    }
                    fos.flush();
                }
            }

            //Update progress
            ++readFiles;
            int progress = (int) (((double) readFiles / allFiles) * 100);
            if (progress >= 80) {
                listeners.stream().forEach((listener) -> {
                    listener.update("just a little more...", progress);
                });
            } else if (progress >= 50) {
                listeners.stream().forEach((listener) -> {
                    listener.update("half way there...", progress);
                });
            }

        }
        listeners.stream().forEach((listener) -> {
            listener.onStop("Done");
        });
    }

    public static boolean addProgressListener(ProgressListener listener) {
        return listeners.add(listener);
    }

    public static boolean removeProgressListener(ProgressListener listener) {
        return listeners.remove(listener);
    }
}
