
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.attribute.UserPrincipal;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/*
 * Copyright (C) 2015 Victor Anuebunwa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 * @author Victor Anuebunwa
 */
public class UnitTestClass {

    public static void main(String args[]) {

//        boolean match = ".Mp4".matches("[\\s\\S]*" + Utility.getAllowedMaterialsExtensions());
//        System.out.println(match);
//        try {            
//            Connection con = DriverManager.getConnection("jdbc:mysql://localhost/library", "root", "");
//            con.setAutoCommit(false);
//            PreparedStatement statement = con.prepareStatement("insert into stopwords set word = ?", Statement.NO_GENERATED_KEYS);
//            BufferedReader fileReader = new BufferedReader(
//                    new FileReader(new File("C:/Users/Admin/Desktop/stopwordsen.txt")));
//            while (fileReader.ready()) {
//                String line = fileReader.readLine();
//                statement.setString(1,line); 
//                statement.execute();
//            }
//            con.commit();
//            
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(UnitTestClass.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException | SQLException ex) {
//            Logger.getLogger(UnitTestClass.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public static void runTestIndxer(JProgressBar progressBar, JLabel label, Runnable closeAction) {
        Runnable runnable = new Runnable() {
            private double filesCount;
            private double indexFiles;

            public void run() {
                try {
                    File indexDir = new File(DBConnection.getIndexFolderLocation());
//                    CharArraySet set = new CharArraySet(, true);

                    StandardAnalyzer analysis = new StandardAnalyzer(
                            new FileReader(new File("C:/Users/Admin/Desktop/stopwordsen.txt")));

//                    StandardAnalyzer analysis = new StandardAnalyzer(
//                            new FileReader(new File("C:/Users/Admin/Desktop/stopwordsen.txt")));
                    // Store the index on disk:
                    FSDirectory dir = FSDirectory.open(indexDir);
                    // To store an index in memory:
                    //Directory directory = new RAMDirectory();
                    IndexWriter idx = new IndexWriter(dir, new IndexWriterConfig(Version.LATEST, analysis));

                    File files = new File(DBConnection.getLibraryLocation());

                    if (indexDir.list().length <= 1) { //Excluding FSDirectory lock file
                        filesCount = count(files);
                        System.out.println("Processing " + filesCount + " files...");

                        if (progressBar != null && label != null) {
                            progressBar.setValue(0);
                            label.setText("loading library...");
                        }

                        long startTime = addFiles(files, idx);
                        idx.close();
                        System.out.println("Processing took " + ((System.currentTimeMillis() - startTime) / 1000) + " secs");
                    } else {
                        progressBar.setValue(100);
                    }

                } catch (IOException | SAXException | TikaException | InterruptedException | ExecutionException | NullPointerException ex) {
                    Logger.getLogger(UnitTestClass.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (closeAction != null) {
                    closeAction.run();
                }
            }

            private int count(File file) {
                int total = 0;
                if (file.isDirectory()) {
                    File[] listFiles = file.listFiles((File pathname) -> {
                        return pathname.isDirectory() || pathname.getName()
                                .matches("[\\s\\S]*" + Utility.getAllowedMaterialsExtensions());
                    });
                    for (File listFile : listFiles) {
                        total += count(listFile);
                    }
                } else if (file.isFile()) {
                    total += 1;
                }
                return total;
            }

            private long addFiles(File files, IndexWriter idx) throws IOException, SAXException, TikaException {

                File[] listFiles = files.listFiles((File pathname) -> {
                    return pathname.isDirectory() || pathname.getName()
                            .matches("[\\s\\S]*" + Utility.getAllowedMaterialsExtensions());
                });

                long startTime = System.currentTimeMillis();
                FieldType type = new FieldType();
                type.setTokenized(true);
                type.setIndexed(true);
                type.setStored(true);

                for (File f : listFiles) {
                    if (f.isFile()) {
                        System.out.println("> " + f.getName());
                        // **** Tika specific-stuff.  Otherwise this is like the basic Lucene Indexer example.
                        ContentHandler contenthandler = new BodyContentHandler(Integer.MAX_VALUE);
                        Metadata metadata = new Metadata();
                        ParseContext context = new ParseContext();
                        Parser parser = new AutoDetectParser();
                        metadata.set(Metadata.RESOURCE_NAME_KEY, f.getName());
                        // OOXMLParser parser = new OOXMLParser();

                        FileInputStream is = new FileInputStream(f);
                        parser.parse(is, contenthandler, metadata, context);
                        // **** End Tika-specific

                        Document doc = new Document();
                        for (IndexField value : IndexField.values()) {
                            switch (value) {
                                case CONTENT:
                                    doc.add(new TextField(IndexField.CONTENT.name(), new StringReader(contenthandler.toString())));
                                    break;
                                case TITLE:
                                    doc.add(new Field(IndexField.TITLE.name(), f.getName(), type));
                                    break;
                                case PATH:
                                    doc.add(new StringField(IndexField.PATH.name(), f.getPath(), Field.Store.YES));
                                    break;
                                case AUTHOR:
                                    UserPrincipal owner = Files.getOwner(f.toPath());
                                    doc.add(new Field(IndexField.AUTHOR.name(), owner.getName(), type));
                                    break;
                                case KEYWORDS:
                                    doc.add(new Field(IndexField.KEYWORDS.name(), f.toString(), type));
                                    break;
                                case LOCATION_IN_LIBRARY:
                                    doc.add(new StringField(IndexField.LOCATION_IN_LIBRARY.name(), f.getCanonicalPath(), Field.Store.YES));
                                    break;
                                case CREATION_DATE:
                                    doc.add(new LongField(IndexField.CREATION_DATE.name(),
                                            Files.getLastModifiedTime(f.toPath()).toMillis(),
                                            Field.Store.YES));
                                    break;
                                case TYPE:
                                    doc.add(new StringField(IndexField.TYPE.name(), MaterialType.EBOOK.name(), Field.Store.YES));
                                    break;
                                case ID:
                                    ++indexFiles;
                                    doc.add(new StringField(IndexField.ID.name(), indexFiles + "", Field.Store.YES));
                                    break;
                                default:
                                    throw new AssertionError(value.name());
                            }
                        }
                        idx.addDocument(doc);

                        if (progressBar != null && label != null) {
                            int progress = (int) (indexFiles / filesCount * 100);
                            progressBar.setValue(progress);
                            if (progress >= 80) {
                                label.setText("just a little more...");
                            } else if (progress >= 50) {
                                label.setText("half way there...");
                            }
                        }

                    } else {
                        System.out.println("> [Folder] " + f.getName());
                        addFiles(f, idx);
                    }

                }

                return startTime;
            }
        };
        Utility.getExecutorService().execute(runnable);
    }

}
