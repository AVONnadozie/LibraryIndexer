
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Anuebunwa Victor
 */
public class ContentSearcher {

    private Directory directory;
    private Analyzer analyzer;
    private boolean indexChanged;
    private IndexSearcher isearcher;
    private DirectoryReader ireader;
    private SearchCategory currentSearchCategory;
    private SearchField currentSearchField;

    private final ArrayList<HashMap<IndexField, String>> results;

    /**
     * Constructs {@code ContentSearcher} in a separate thread<br/>
     * This creates the indexes and searcher for searching contents in the index
     * 
     * @param progressBar progress bar to show progress
     * @param label label to set progress messages on
     * @param closeAction action to execute before the {@code ContentSearcher} thread terminates
     * @throws Exception 
     */
    
    @Deprecated
    public ContentSearcher(JProgressBar progressBar, JLabel label, Runnable closeAction) throws Exception {
        results = new ArrayList<>();
        init();
        
        //Test indexer
        UnitTestClass.runTestIndxer(progressBar, label, closeAction);
        
    }
    
    /**
     * Constructs {@code ContentSearcher} for searching contents in the index<br/>
     * 
     * @throws Exception 
     */
    public ContentSearcher() throws Exception {
        results = new ArrayList<>();
        init();   
    }

    private void init() throws IOException, InterruptedException, ExecutionException {

        analyzer = new StandardAnalyzer();
        // Store the index on disk:
        directory = FSDirectory.open(new File(DBConnection.getIndexFolderLocation()));
        // To store an index in memory:
        //Directory directory = new RAMDirectory();
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

    /**
     * Searches for the query in the index and returns a list of results with
     * item at index 0 having most hits
     *
     * @param q query
     * @return result (0 - top hit)
     * @throws IOException
     * @throws ParseException
     */
    public List search(String q) throws IOException, ParseException {

        clearOldResults();

        if (q.isEmpty()) {
            return results;
        }

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

        //Restructure search query for better results
        if (!currentSearchCategory.equals(SearchCategory.ALL)) {
            q = currentSearchCategory.name() + " " + q;
        }

        String fields[] = new String[]{
            IndexField.TYPE.name(),
            IndexField.TITLE.name(),
            IndexField.AUTHOR.name(),
            IndexField.CONTENT.name(),
            IndexField.KEYWORDS.name()
        };

        BooleanClause.Occur[] occur = new BooleanClause.Occur[]{
            currentSearchCategory.equals(SearchCategory.ALL) ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST,
            currentSearchField.equals(SearchField.TITLE) ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD,
            currentSearchField.equals(SearchField.AUTHOR) ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD,
            currentSearchField.equals(SearchField.CONTENT) ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD,
            BooleanClause.Occur.SHOULD
        };

        Query query = MultiFieldQueryParser.parse(q, fields, occur, analyzer);
        //search
        ScoreDoc[] hits = isearcher.search(query, 1000).scoreDocs;

        // Iterate through the results:
        for (int i = 0; i < hits.length; i++) {
            Document hitDoc = isearcher.doc(hits[i].doc);
            if (!documentExists(hitDoc)) {
                HashMap<IndexField, String> item = new HashMap<>();
                for (IndexField aField : IndexField.values()) {
                    item.put(aField, hitDoc.get(aField.name()));
                }
                results.add(item);
            }
        }
//        ireader.close();
//        directory.close();

        return results;
    }

    private boolean documentExists(Document hitDoc) {
        return results.stream().anyMatch((item) -> (item.getOrDefault(IndexField.TITLE, "")
                .equalsIgnoreCase(hitDoc.get(IndexField.TITLE.name()))
                && item.getOrDefault(IndexField.LOCATION_IN_LIBRARY, "")
                .equalsIgnoreCase(hitDoc.get(IndexField.LOCATION_IN_LIBRARY.name()))));
    }

}
