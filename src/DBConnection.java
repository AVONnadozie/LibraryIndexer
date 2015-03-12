
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 *
 * @author Anuebunwa Victor
 */
public class DBConnection {

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
    public static ResultSet getMaterials() throws InterruptedException, ExecutionException, NullPointerException {
        Future<ResultSet> result = Utility.getExecutorService().submit(() -> {
            Statement s = getConnection().createStatement();
            return s.executeQuery("select * from materials order by title DESC");
        });
        return result.get();
    }

    /**
     *
     * @param id material id
     * @return Index folder location
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static String getMaterialPreview(int id) throws InterruptedException, ExecutionException, NullPointerException {
        Future<String> result = Utility.getExecutorService().submit(() -> {
            Statement s = getConnection().createStatement();
            ResultSet rs = s.executeQuery("select preview from materials where id = '" + id + "'");
            String preview = "";
            if (rs.next()) {
                preview = rs.getString(1);
            }
            return preview;
        });
        return result.get();
    }

    /**
     *
     * @return Index folder location
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static String getIndexFolderLocation() throws InterruptedException, ExecutionException, NullPointerException {
        Future<String> result = Utility.getExecutorService().submit(() -> {
            Statement s = getConnection().createStatement();
            ResultSet rs = s.executeQuery("select value from settings where name = 'index_dir'");
            if (rs.next()) {
                return rs.getString(1);
            } else {
                throw new NullPointerException("No result from database");
            }
        });
        return result.get();
    }

    /**
     *
     * @return Library folder location
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static String getLibraryLocation() throws InterruptedException, ExecutionException, NullPointerException {
        Future<String> result = Utility.getExecutorService().submit(() -> {
            Statement s = getConnection().createStatement();
            ResultSet rs = s.executeQuery("select value from settings where name = 'lib_dir'");
            if (rs.next()) {
                return rs.getString(1);
            } else {
                throw new NullPointerException("No result from database");
            }
        });
        return result.get();
    }

}
