
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author Anuebunwa Victor
 */
public final class MainWindow extends javax.swing.JFrame {

    private final Timer searchTimer;
    private Image img;
    private ContentSearcher cs;
    private final ListItem[] listItem;
    private int lastIndex;
    private List<HashMap<IndexField, String>> result;

    private final int MAX_NO_OF_ITEMS = 5;

    public MainWindow() {
        //Load components
        try {
            img = ImageIO.read(getClass().getResource("resources/bg.jpg"));
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        initComponents();
        resultCountLabel.setText("");
        listItem = new ListItem[MAX_NO_OF_ITEMS]; //Maximum of 10 list items at a time
        for (int i = 0; i < MAX_NO_OF_ITEMS; i++) {
            listItem[i] = new ListItem();
            if (i == 0) {
                listItem[i].showNoResult();
            } else {
                listItem[i].setVisible(false);
            }
            scene.add(listItem[i]);
        }
        controlButtonsPanel.setVisible(false);

        //Sets search timer to execute in a period of 500 milliseconds 
        searchTimer = new Timer(500, (ActionEvent e) -> {
            defaultSearch();
        });
        searchTimer.setRepeats(false);
        searchTimer.setDelay(1000);

        //Load indexer and searcher
        try {
            setMessage("");

            cs = new ContentSearcher(progressBar, progressLabel, () -> {
                if (progressBar.getValue() < 100) {
                    setMessage("error");
                } else {
                    setMessage("ready");
                }
                setProgress(-1);                
            });

//            cs = new ContentSearcher();
            setProgress(-1);
        } catch (Exception ex) {
            Utility.writeLog(ex);
            setMessage("error");
        }
    }

    /**
     * Displays next or previous results relative to the last accessed index
     *
     * @param forward true if next results should be shown else false (previous)
     */
    private void showResult(boolean forward) {

        if (result.isEmpty()) {
            for (int i = 0; i < MAX_NO_OF_ITEMS; i++) {
                if (i == 0) {
                    listItem[i].showNoResult();
                    listItem[i].setVisible(true);
                } else {
                    listItem[i].setVisible(false);
                }
            }
            controlButtonsPanel.setVisible(false);
            resultCountLabel.setText("0 results");
            return;
        }

        if (forward) { //Next
            int remainingItems = (result.size() - (lastIndex + 1));
            if (remainingItems > 0) {
                //Display from lastIndex to index <= MAX_NO_OF_ITEMS
                int size = Math.min(MAX_NO_OF_ITEMS, remainingItems);
                for (int i = 0; i < size; i++) {
                    listItem[i].setContent(result.get(++lastIndex));
                    listItem[i].setVisible(true);
                }

                //Remove unused listItems
                if (remainingItems <= MAX_NO_OF_ITEMS) {
                    for (int i = remainingItems; i < MAX_NO_OF_ITEMS; i++) {
                        listItem[i].setVisible(false);
                    }
                }

            } else {
                //No way
            }
        } else { //Previous
            if ((lastIndex + 1) > MAX_NO_OF_ITEMS) {
                //Calculate number of steps back
                int currentlyVisibleItems = (lastIndex + 1) % MAX_NO_OF_ITEMS;
                currentlyVisibleItems = currentlyVisibleItems == 0 ? MAX_NO_OF_ITEMS : currentlyVisibleItems;

                //Previous items are >=  MAX_NO_OF_ITEMS
                lastIndex -= MAX_NO_OF_ITEMS + currentlyVisibleItems;
                for (int i = 0; i < MAX_NO_OF_ITEMS; i++) {
                    listItem[i].setContent(result.get(++lastIndex));
                    listItem[i].setVisible(true);
                }
            } else {
                //No way
            }
        }

        //Show number of pages
        int currentPage = (int) (lastIndex + 1) / MAX_NO_OF_ITEMS;
        int pages = (int) result.size() / MAX_NO_OF_ITEMS;
        paginationLabel.setText(String.format("Page %d of %d",
                (((lastIndex + 1) % MAX_NO_OF_ITEMS == 0) ? currentPage : currentPage + 1),
                ((result.size() % MAX_NO_OF_ITEMS == 0) ? pages : pages + 1)));
        //Show number of results
        resultCountLabel.setText(result.size() + " results");
        //If size of results is greater than number accessed indexes
        nextButton.setEnabled(result.size() > (lastIndex + 1));
        //If previously accessed indexes are more than MAX_NO_OF_ITEMS, enable prevButton
        prevButton.setEnabled((lastIndex + 1) > MAX_NO_OF_ITEMS);
        //Toggle visibility of control buttons panel
        controlButtonsPanel.setVisible(nextButton.isEnabled() || prevButton.isEnabled());
    }

    /**
     * Shows progress of tasks
     *
     * @param value
     */
    public void setProgress(int value) {
        if (value < 0 || value > 100) {
            progressPanel.setVisible(false);
            setProgressMessage("");
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(false);
        } else {
            progressBar.setIndeterminate(false);
            progressPanel.setVisible(true);
            progressBar.setValue(value);
            progressBar.setStringPainted(true);
        }
    }

    public void setProgressMessage(String message) {
        progressLabel.setText(message);
    }

    public final void setMessage(String message) {
        messageLabel.setText(message);
    }

    /**
     * Sets the time for next search
     */
    private void scheduleDefaultSearch() {
        if (searchTimer.isRunning()) {
            searchTimer.restart();
        } else {
            searchTimer.start();
        }
    }

    /**
     * Searches for typed query
     */
    private void defaultSearch() {
        if (!isSearchFieldWaterMarkShowing()) {
            search(queryField.getText());
        }
    }

    /**
     * Searches for query in index
     *
     * @param query user's query
     */
    private void search(String query) {
        if (query == null || query.isEmpty()) {
            return;
        }

        if (cs != null) {
            try {
                long startTime = System.currentTimeMillis();
                //Set the selected category by user
                cs.setSearchCategory(getSelectedSearchCategory());
                //set the selected field by user
                cs.setSearchField(getSelectedSearchField());
                //Search for query
                result = cs.search(query);
                messageLabel.setText(String.format("last search took %.2f secs",
                        (float) ((System.currentTimeMillis() - startTime) / 1000)));
                //Display result
                lastIndex = -1;
                showResult(true);
            } catch (IOException | ParseException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            setMessage("search engine not ready");
        }
    }

    /**
     * Gets the category selected by user
     *
     * @return
     */
    private SearchCategory getSelectedSearchCategory() {
        String actionCommand = categories.getSelection().getActionCommand();
        if (actionCommand == null) {
            return SearchCategory.ALL;
        }

        switch (actionCommand) {
            case "Books":
                return SearchCategory.BOOKS;
            case "CDs/DVDs":
                return SearchCategory.CDS;
            default: //All
                return SearchCategory.ALL;
        }
    }

    /**
     * Gets the search field selected by user
     *
     * @return
     */
    private SearchField getSelectedSearchField() {
        String actionCommand = searchField.getSelection().getActionCommand();
        if (actionCommand == null) {
            return SearchField.ALL;
        }

        switch (actionCommand) {
            case "Title":
                return SearchField.TITLE;
            case "Author":
                return SearchField.AUTHOR;
            case "Content":
                return SearchField.CONTENT;
            default: //Content
                return SearchField.ALL;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        categories = new javax.swing.ButtonGroup();
        searchField = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(img, 0, 0, getParent().getWidth(), getParent().getHeight(), Color.white, null);
            }
        };
        searchPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        queryField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        footerPanel = new javax.swing.JPanel();
        messageLabel = new javax.swing.JLabel();
        progressPanel = new javax.swing.JPanel();
        progressLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        resultCountLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        scene = new javax.swing.JPanel();
        controlButtonsPanel = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        paginationLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Library Indexer");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Segoe UI Light", 0, 30)); // NOI18N
        jLabel1.setText("Locate materials easier and faster");

        queryField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        queryField.setForeground(java.awt.Color.lightGray);
        queryField.setText("Search");
        queryField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                queryFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                queryFieldFocusLost(evt);
            }
        });
        queryField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryFieldActionPerformed(evt);
            }
        });
        queryField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                queryFieldKeyTyped(evt);
            }
        });

        jButton1.setText("Search");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel3.setText("Field:");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel2.setText("Category:");

        categories.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("All");

        categories.add(jRadioButton2);
        jRadioButton2.setText("Books");

        categories.add(jRadioButton3);
        jRadioButton3.setText("CDs/DVDs");

        searchField.add(jRadioButton6);
        jRadioButton6.setText("Content");

        searchField.add(jRadioButton5);
        jRadioButton5.setText("Author");

        searchField.add(jRadioButton4);
        jRadioButton4.setText("Title");

        searchField.add(jRadioButton7);
        jRadioButton7.setSelected(true);
        jRadioButton7.setText("All");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(36, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButton4)
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jRadioButton4)
                        .addComponent(jRadioButton5)
                        .addComponent(jRadioButton6)
                        .addComponent(jRadioButton7))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jRadioButton1)
                        .addComponent(jRadioButton2)
                        .addComponent(jRadioButton3))
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(searchPanelLayout.createSequentialGroup()
                        .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(queryField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(20, 20, 20))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(queryField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        messageLabel.setFont(new java.awt.Font("Segoe UI Semilight", 0, 11)); // NOI18N
        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        messageLabel.setText("last results took 0.0secs");

        progressLabel.setText("loading library...");

        progressBar.setIndeterminate(true);

        javax.swing.GroupLayout progressPanelLayout = new javax.swing.GroupLayout(progressPanel);
        progressPanel.setLayout(progressPanelLayout);
        progressPanelLayout.setHorizontalGroup(
            progressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(progressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(progressLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        progressPanelLayout.setVerticalGroup(
            progressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(progressPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(progressPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        resultCountLabel.setText("10 results");

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout footerPanelLayout = new javax.swing.GroupLayout(footerPanel);
        footerPanel.setLayout(footerPanelLayout);
        footerPanelLayout.setHorizontalGroup(
            footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(footerPanelLayout.createSequentialGroup()
                .addComponent(progressPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 291, Short.MAX_VALUE)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(resultCountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(messageLabel)
                .addGap(28, 28, 28))
        );
        footerPanelLayout.setVerticalGroup(
            footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(progressPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, footerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(footerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(messageLabel)
                        .addComponent(resultCountLabel))
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        scene.setMinimumSize(new java.awt.Dimension(647, 350));
        scene.setOpaque(false);
        scene.setPreferredSize(new java.awt.Dimension(647, 350));
        scene.setLayout(new javax.swing.BoxLayout(scene, javax.swing.BoxLayout.PAGE_AXIS));

        controlButtonsPanel.setOpaque(false);

        nextButton.setText("Next");
        nextButton.setOpaque(false);
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        prevButton.setText("Prev");
        prevButton.setOpaque(false);
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });

        paginationLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        paginationLabel.setText("Page 1 of 2");
        paginationLabel.setOpaque(true);

        javax.swing.GroupLayout controlButtonsPanelLayout = new javax.swing.GroupLayout(controlButtonsPanel);
        controlButtonsPanel.setLayout(controlButtonsPanelLayout);
        controlButtonsPanelLayout.setHorizontalGroup(
            controlButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlButtonsPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(paginationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(prevButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton)
                .addContainerGap())
        );
        controlButtonsPanelLayout.setVerticalGroup(
            controlButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, controlButtonsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(controlButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nextButton)
                    .addComponent(prevButton)
                    .addComponent(paginationLabel))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(footerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(39, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(controlButtonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scene, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(searchPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlButtonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scene, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(footerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void queryFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_queryFieldKeyTyped
        // TODO add your handling code here:
        scheduleDefaultSearch();
    }//GEN-LAST:event_queryFieldKeyTyped

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        defaultSearch();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void queryFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_queryFieldFocusGained
        // TODO add your handling code here:
        if (isSearchFieldWaterMarkShowing()) {
            queryField.setText("");
            queryField.setForeground(Color.black);
        }

    }//GEN-LAST:event_queryFieldFocusGained

    private boolean isSearchFieldWaterMarkShowing() {
        return queryField.getText().equals("Search")
                && queryField.getForeground().equals(Color.lightGray);
    }

    private void queryFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_queryFieldFocusLost
        // TODO add your handling code here:
        if (queryField.getText().isEmpty()) {
            queryField.setText("Search");
            queryField.setForeground(Color.lightGray);
        }
    }//GEN-LAST:event_queryFieldFocusLost

    private void queryFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryFieldActionPerformed
        // TODO add your handling code here:
        defaultSearch();
    }//GEN-LAST:event_queryFieldActionPerformed

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        // TODO add your handling code here:
        showResult(true);
    }//GEN-LAST:event_nextButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
        // TODO add your handling code here:
        showResult(false);
    }//GEN-LAST:event_prevButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup categories;
    private javax.swing.JPanel controlButtonsPanel;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel paginationLabel;
    private javax.swing.JButton prevButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JTextField queryField;
    private javax.swing.JLabel resultCountLabel;
    private javax.swing.JPanel scene;
    private javax.swing.ButtonGroup searchField;
    private javax.swing.JPanel searchPanel;
    // End of variables declaration//GEN-END:variables
}
