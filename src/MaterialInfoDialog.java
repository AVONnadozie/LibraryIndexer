
import java.awt.Desktop;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

/**
 *
 * @author Victor Anuebunwa
 */
public class MaterialInfoDialog extends javax.swing.JDialog {

    private String path;

    public MaterialInfoDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

//    public void paint(Graphics g) {
//        Graphics2D g2 = (Graphics2D) g.create();
//        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
//        super.paint(g2);
//        g2.dispose();
//    }
    public static void showDialog(Frame parent, Material material) {
        MaterialInfoDialog dialog = new MaterialInfoDialog(MainWindow.getInstance(), true);
        Utility.centreOnParent(parent, dialog);
        dialog.setInfo(material);
        dialog.setVisible(true);
    }

    private void setInfo(Material material) {
        int maxWindowtextWidth = 70;
        try {
            String preview = Database.getMaterialPreview(material.getPath().toString());
            if (preview.isEmpty()) {
                previewTextArea.setText("No Preview Available.");
            } else {
                previewTextArea.setText(preview);
            }
        } catch (InterruptedException | ExecutionException | NumberFormatException | NullPointerException ex) {
            previewTextArea.setText("No Preview Available.");
            Utility.writeLog(ex);
        }

        for (IndexFields value : IndexFields.values()) {
            switch (value) {
                case CONTENT:
                    break;
                case TITLE:
                    StringBuilder title = new StringBuilder(material.getTitle());
                    if (title.length() > maxWindowtextWidth) {
                        title.insert(maxWindowtextWidth, "<br>");
                    }
                    titleLabel.setText("<html>" + title);
                    break;
                case PATH:
                    path = material.getPath().toString();
                     {
                        try {
                            if (!(new File(new URI(path)).isFile())) {
                                openMaterialButton.setVisible(false);
                            }
                        } catch (URISyntaxException ex) {
                            Utility.writeLog(ex);
                        }
                    }
                    break;
                case AUTHOR:
                    StringBuilder author = new StringBuilder(material.getAuthor());
                    if (author.length() > maxWindowtextWidth) {
                        author.insert(maxWindowtextWidth, "<br>");
                    }
                    authorLabel.setText("<html>" + author);
                    break;
                case KEYWORDS:
                    break;
                case CREATION_DATE:
                    break;
                case TYPE:
                    MaterialType type = material.getType();
                    switch (type) {
                        case EBOOK:
                            typeLabel.setText(type.name() + " | ISBN: " + material.getISBN());
                            break;
                        case CD:
                            typeLabel.setText(type.name());
                            break;
                        default:
                            throw new AssertionError(type.name());
                    }
                    break;
                case ISBN:
                    break;
                default:
                    throw new AssertionError(value.name());
            }
        }

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        openMaterialButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        previewTextArea = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        authorLabel = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        typeLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Info.");
        setResizable(false);

        jPanel1.setPreferredSize(new java.awt.Dimension(420, 442));

        openMaterialButton.setText("Open");
        openMaterialButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMaterialButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("Title");

        titleLabel.setText("<Title in html>");

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        previewTextArea.setEditable(false);
        previewTextArea.setColumns(20);
        previewTextArea.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        previewTextArea.setLineWrap(true);
        previewTextArea.setRows(5);
        previewTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(previewTextArea);

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("Preview");

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel4.setText("Author");

        authorLabel.setText("<Author in html>");

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText("Type of Material:");

        typeLabel.setText("<type>");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                    .addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(authorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(typeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(0, 420, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(openMaterialButton))
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(authorLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(typeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(openMaterialButton)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openMaterialButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMaterialButtonActionPerformed
        // TODO add your handling code here:
        if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (IOException ex) {
                Utility.writeLog(ex);
            }
        }
    }//GEN-LAST:event_openMaterialButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel authorLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton openMaterialButton;
    private javax.swing.JTextArea previewTextArea;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
}
