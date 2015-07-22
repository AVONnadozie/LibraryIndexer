
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Anuebunwa Victor
 */
public class Searcher {

    private Directory directory;
    private Analyzer analyzer;
    private boolean indexChanged;
    private IndexSearcher isearcher;
    private DirectoryReader ireader;
    private SearchCategory currentSearchCategory;
    private SearchField currentSearchField;

    private final ArrayList<Material> results;

    /**
     * Constructs {@code ContentSearcher} in a separate thread<br/>
     * This creates the indexes and searcher for searching contents in the index
     *
     * thread terminates
     */
    public Searcher() {
        results = new ArrayList<>();

        Runnable runnable = () -> {
            try {
                Database.readIndexes();
                MainWindow.getInstance().setMessage("ready"); //Indicate error
            } catch (SQLException | IOException ex) {
                MainWindow.getInstance().setMessage("error"); //Indicate error
                Utility.writeLog(ex);
            }
            try {
                init();
            } catch (IOException | InterruptedException | ExecutionException ex) {
                Utility.writeLog(ex);
            }

        };
        Utility.getExecutorService().execute(runnable);
    }

    private void init() throws IOException, InterruptedException, ExecutionException {

        analyzer = new EnglishAnalyzer(Database.getStopWords());
        // Store the index on disk:
        directory = FSDirectory.open(new File(Utility.getIndexFolderLocation()));
    }

    public void setSearchCategory(SearchCategory selectedSearchType) {
        currentSearchCategory = selectedSearchType;
    }

    public void setSearchField(SearchField selectedSearchField) {
        currentSearchField = selectedSearchField;
    }

    private void clearOldResults() {
        results.clear();
    }

    public List<Material> search(String q) throws IOException, ParseException {

        List<Material> searchResults;
        boolean useIndexes = currentSearchField.equals(SearchField.ALL)
                || currentSearchField.equals(SearchField.CONTENT);
        if (q.isEmpty() || !useIndexes) {
            searchResults = new ArrayList<>(Library.getInstance().getMaterials());
        } else {
            searchResults = new ArrayList<>(searchIndexes(q));
        }

        ArrayList<Material> exceptions = new ArrayList<>();

        if (!currentSearchCategory.equals(SearchCategory.ALL)) {
            searchResults.stream().forEach((material) -> {
                boolean found;
                if (material != null) {
                    MaterialType type = material.getType();
                    //Search attributes for match
                    switch (currentSearchCategory) {
                        case ALL:
                            found = true;
                            break;
                        case CDS:
                            found = type.equals(MaterialType.CD);
                            break;
                        case BOOKS:
                            found = type.equals(MaterialType.EBOOK);
                            break;
                        default:
                            throw new AssertionError(currentSearchCategory.name());
                    }
                } else {
                    found = true; //Null materials will be removed later
                }

                if (!found) {
                    exceptions.add(material);
                }
            });
        }

        if (!useIndexes) {
            String[] names = q.split("\\s");
            searchResults.stream().forEach((material) -> {
                if (material != null) {
                    boolean found = false;
                    loop:
                    for (String keyname : names) {
                        //Search attributes for match
                        keyname = keyname.toLowerCase();
                        switch (currentSearchField) {
                            case AUTHOR:
                                if (material.getAuthor() != null
                                        && material.getAuthor().toLowerCase().contains(keyname)) {
                                    found = true;
                                    break loop;
                                }
                                break;
                            case TITLE:
                                if (material.getTitle() != null
                                        && material.getTitle().toLowerCase().contains(keyname)) {
                                    found = true;
                                    break loop;
                                }
                                break;
                            case ISBN:
                                if (material.getISBN() != null
                                        && material.getISBN().toLowerCase().contains(keyname)) {
                                    found = true;
                                    break loop;
                                }
                                break;
                            default:
                                throw new AssertionError(currentSearchField.name());
                        }
                    }
                    if (!found) {
                        exceptions.add(material);
                    }
                }

            });
        }

        searchResults.removeIf((material) -> (material == null));

        searchResults.removeAll(exceptions);

        return searchResults;
    }

    /**
     * Searches for the query in the index and returns a list of results with
     * item at index 0 having most hits
     *
     * @param q query
     * @return result (0 - top hit)
     * @throws IOException
     * @throws ParseException
     */
    private List<Material> searchIndexes(String q) throws IOException, ParseException {
        clearOldResults();

        // Now search the index:
        if (ireader == null) {
            ireader = DirectoryReader.open(directory);
            isearcher = new IndexSearcher(ireader, Utility.getExecutorService());
            indexChanged = false;
        } else if (indexChanged) {
            ireader = DirectoryReader.openIfChanged(ireader);
            isearcher = new IndexSearcher(ireader, Utility.getExecutorService());
            indexChanged = false;
        }

        QueryParser parser = new QueryParser(IndexFields.CONTENT.name(), analyzer);
        Query query = parser.parse(q);
        //search
        ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;
        // Iterate through the results:
        for (ScoreDoc hit : hits) {
            Document hitDoc = isearcher.doc(hit.doc);
            if (!documentExists(hitDoc)) {
                try {
                    String path = hitDoc.get(IndexFields.PATH.name());
                    if (path != null) {
                        Material material = Library.getInstance().getMaterial(new URI(path));
                        if (material != null) {
                            results.add(material);
                        }
                    }
                } catch (URISyntaxException ex) {
                    Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return results;
    }

    private boolean documentExists(Document hitDoc) {
        return results.stream().anyMatch((item) -> (item.getPath().toString()
                .equalsIgnoreCase(hitDoc.get(IndexFields.PATH.name()))));
    }

}
