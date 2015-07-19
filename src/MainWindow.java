
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.Timer;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 *
 * @author Anuebunwa Victor
 */
public final class MainWindow extends javax.swing.JFrame {

    private final Timer searchTimer;
    private Image img;
    private final ListItem[] listItem;
    private int lastIndex;
    private List<Material> result;

    private final int MAX_NO_OF_ITEMS = 5;

    private final Dimension lastWindowSize;
    private final Point point;

    private static MainWindow thisClass;
    private final User user;

    private MainWindow() {
        setUndecorated(true);
        point = new Point();
        //Load components
        try {
            img = ImageIO.read(getClass().getResource("resources/bg.jpg"));
            setIconImage(ImageIO.read(getClass().getResource("resources/research.png")));
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

        Database.addProgressListener(new ProgressListener() {

            @Override
            public void onStart(String message) {
                progressPanel.setVisible(true);
                progressBar.setValue(0);
                progressLabel.setText(message);
            }

            @Override
            public void update(String message, int progress) {
                progressBar.setValue(progress);
                progressLabel.setText(message);
            }

            @Override
            public void onStop(String message) {
                progressPanel.setVisible(false);
            }
        });

        //Sets search timer to execute in a period of 500 milliseconds
        searchTimer = new Timer(500, (ActionEvent e) -> {
            defaultSearch();
        });
        searchTimer.setRepeats(false);
        searchTimer.setDelay(1000);

        user = User.getInstance();

        //Show materials result
        result = Library.getInstance().getMaterials();
        lastIndex = -1;
        showResult(true);

        //Save default window size
        lastWindowSize = getSize();
    }

    /**
     * Enforces singleton property on this class
     *
     * @return object of {@code  MainWindow} class
     */
    public static MainWindow getInstance() {
        if (thisClass == null) {
            thisClass = new MainWindow();
        }
        return thisClass;
    }

    public void fitWindowToScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (fitToScreenToggleButton.isSelected()) {
            setSize(screenSize);
            setLocation(0, 0);
            fitToScreenToggleButton.setToolTipText("Restore");
        } else {
            setSize(lastWindowSize);
            Utility.centreOnScreen(this);

            fitToScreenToggleButton.setToolTipText("Fit to screen");
        }
    }

    private void bodyMousePressed(MouseEvent evt) {
        if (!evt.isMetaDown()) {
            point.x = evt.getX();
            point.y = evt.getY();
        }
    }

    private void bodyMouseDragged(MouseEvent evt) {
        if (!evt.isMetaDown()) {
            Point p = getLocation();
            setLocation(p.x + evt.getX() - this.point.x,
                    p.y + evt.getY() - this.point.y);
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
        if (isSearchFieldWaterMarkShowing()) {
            search("");
        } else {
            search(queryField.getText());
        }
    }

    /**
     * Searches for query in index
     *
     * @param query user's query
     */
    private void search(String query) {
        if (query == null) {
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            //Search for query
            result = user.query(query, getSelectedSearchCategory(), getSelectedSearchField());
            messageLabel.setText(String.format("last search took %.4f secs",
                    ((float) (System.currentTimeMillis() - startTime) / 1000)));
            //Display result
            lastIndex = -1;
            showResult(true);
        } catch (IOException | ParseException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public final void setMessage(String message) {
        messageLabel.setText(message);
    }

    /**
     * Gets the category selected by user
     *
     * @return
     */
    private SearchCategory getSelectedSearchCategory() {
        int index = categoryComboBox.getSelectedIndex();
        switch (index) {
            case 1:
                return SearchCategory.BOOKS;
            case 2:
                return SearchCategory.CDS;
            case 0:
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
        int index = fieldComboBox.getSelectedIndex();

        switch (index) {
            case 3:
                return SearchField.TITLE;
            case 2:
                return SearchField.AUTHOR;
            case 1:
                return SearchField.CONTENT;
            case 0: //Content
            default:
                return SearchField.ALL;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgPanel = new javax.swing.JPanel(){
            @Override
            public void paintComponent(Graphics g){
                super.paintComponent(g);
                g.drawImage(img, 0, 0, getParent().getWidth(), getParent().getHeight(), Color.white, null);
            }
        };
        searchPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel2 = new javax.swing.JLabel();
        categoryComboBox = new javax.swing.JComboBox();
        fieldComboBox = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        queryField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
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
        jButton2 = new javax.swing.JButton();
        minimizeButton = new javax.swing.JButton();
        fitToScreenToggleButton = new javax.swing.JToggleButton();
        jLabel4 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Library Indexer");

        bgPanel.setBackground(new java.awt.Color(255, 255, 255));
        bgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 153, 0), 3));
        bgPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                bgPanelMouseDragged(evt);
            }
        });
        bgPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                bgPanelMousePressed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI Light", 0, 11)); // NOI18N
        jLabel1.setText("Locate materials easier and faster");

        jLabel3.setText("Field:");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel2.setText("Category:");

        categoryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "EBOOKS", "CD/DVD" }));
        categoryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxesActionPerformed(evt);
            }
        });

        fieldComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All", "Content", "Author", "Title", "ISBN" }));
        fieldComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(categoryComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(fieldComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(categoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                        .addComponent(fieldComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        jButton1.setBackground(new java.awt.Color(204, 204, 204));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/search67.png"))); // NOI18N
        jButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jButton1.setContentAreaFilled(false);
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        queryField.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        queryField.setForeground(java.awt.Color.lightGray);
        queryField.setText("Search");
        queryField.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(queryField, javax.swing.GroupLayout.PREFERRED_SIZE, 635, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(queryField, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jLabel5.setFont(new java.awt.Font("Segoe UI Semibold", 0, 18)); // NOI18N
        jLabel5.setText("Library Indexer");

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, searchPanelLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        searchPanelLayout.setVerticalGroup(
            searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(searchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5)
                .addGap(1, 1, 1)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        messageLabel.setFont(new java.awt.Font("Segoe UI Semilight", 0, 11)); // NOI18N
        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        messageLabel.setText("last results took 0.0secs");

        progressLabel.setText("loading library...");

        progressBar.setStringPainted(true);

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
                .addGap(18, 322, Short.MAX_VALUE)
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
                .addComponent(paginationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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

        jButton2.setBackground(new java.awt.Color(204, 0, 0));
        jButton2.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("X");
        jButton2.setToolTipText("Close");
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setFocusPainted(false);
        jButton2.setOpaque(true);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        minimizeButton.setBackground(new java.awt.Color(0, 153, 255));
        minimizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/minimize_12.png"))); // NOI18N
        minimizeButton.setToolTipText("Minimize");
        minimizeButton.setBorder(null);
        minimizeButton.setBorderPainted(false);
        minimizeButton.setContentAreaFilled(false);
        minimizeButton.setFocusPainted(false);
        minimizeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                topButtonsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                topButtonsMouseExited(evt);
            }
        });
        minimizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimizeButtonActionPerformed(evt);
            }
        });

        fitToScreenToggleButton.setBackground(new java.awt.Color(0, 153, 255));
        fitToScreenToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/fitScreen.png"))); // NOI18N
        fitToScreenToggleButton.setToolTipText("Fit to Screen");
        fitToScreenToggleButton.setBorder(null);
        fitToScreenToggleButton.setBorderPainted(false);
        fitToScreenToggleButton.setContentAreaFilled(false);
        fitToScreenToggleButton.setFocusPainted(false);
        fitToScreenToggleButton.setRolloverEnabled(false);
        fitToScreenToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/restore.png"))); // NOI18N
        fitToScreenToggleButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                topButtonsMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                topButtonsMouseExited(evt);
            }
        });
        fitToScreenToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fitToScreenToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bgPanelLayout = new javax.swing.GroupLayout(bgPanel);
        bgPanel.setLayout(bgPanelLayout);
        bgPanelLayout.setHorizontalGroup(
            bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(footerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(bgPanelLayout.createSequentialGroup()
                .addContainerGap(54, Short.MAX_VALUE)
                .addGroup(bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(searchPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(controlButtonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scene, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(54, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, bgPanelLayout.createSequentialGroup()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(minimizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(fitToScreenToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jButton2)
                .addGap(5, 5, 5))
        );
        bgPanelLayout.setVerticalGroup(
            bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bgPanelLayout.createSequentialGroup()
                .addGroup(bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(minimizeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(fitToScreenToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
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
            .addComponent(bgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void minimizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeButtonActionPerformed
        // TODO add your handling code here:
        setState(JFrame.ICONIFIED);
    }//GEN-LAST:event_minimizeButtonActionPerformed

    private void fitToScreenToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fitToScreenToggleButtonActionPerformed
        // TODO add your handling code here:
        fitWindowToScreen();
    }//GEN-LAST:event_fitToScreenToggleButtonActionPerformed

    private void bgPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bgPanelMousePressed
        // TODO add your handling code here:
        bodyMousePressed(evt);
    }//GEN-LAST:event_bgPanelMousePressed

    private void bgPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bgPanelMouseDragged
        // TODO add your handling code here:
        bodyMouseDragged(evt);
    }//GEN-LAST:event_bgPanelMouseDragged

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        Main.closeApp();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void topButtonsMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_topButtonsMouseEntered
        // TODO add your handling code here:
        AbstractButton b = (AbstractButton) evt.getSource();
        b.setOpaque(true);
        b.repaint();
    }//GEN-LAST:event_topButtonsMouseEntered

    private void topButtonsMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_topButtonsMouseExited
        // TODO add your handling code here:
        AbstractButton b = (AbstractButton) evt.getSource();
        b.setOpaque(false);
        b.repaint();
    }//GEN-LAST:event_topButtonsMouseExited

    private void comboBoxesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxesActionPerformed
        // TODO add your handling code here:
        defaultSearch();
    }//GEN-LAST:event_comboBoxesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bgPanel;
    private javax.swing.JComboBox categoryComboBox;
    private javax.swing.JPanel controlButtonsPanel;
    private javax.swing.JComboBox fieldComboBox;
    private javax.swing.JToggleButton fitToScreenToggleButton;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JButton minimizeButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JLabel paginationLabel;
    private javax.swing.JButton prevButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel progressLabel;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JTextField queryField;
    private javax.swing.JLabel resultCountLabel;
    private javax.swing.JPanel scene;
    private javax.swing.JPanel searchPanel;
    // End of variables declaration//GEN-END:variables
}
