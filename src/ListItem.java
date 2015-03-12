
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.SwingConstants;

/**
 *
 * @author Anuebunwa Victor
 */
public class ListItem extends javax.swing.JPanel {

    private HashMap<IndexField, String> result;

    public ListItem() {
        initComponents();
    }

//    @Override
//    public void paint(Graphics g) {
//        Graphics2D g2 = (Graphics2D) g.create();
//        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
//        super.paint(g2);
//        g2.dispose();
//    }

    /**
     * Setup or reinitialises components for viewing this content
     * @param material 
     */
    public void setContent(HashMap<IndexField, String> material) {
        this.result = material;
        authorLabel.setVisible(true);
        typeLabel.setVisible(true);
        jLabel2.setVisible(true);
        jLabel4.setVisible(true);
        readMaterialButton.setVisible(true);
        
        viewMaterialButton.setVisible(true);
//        viewMaterialButton.setVisible(false); //Disabled for now
        
        jSeparator1.setVisible(true);
        titleLabel.setHorizontalAlignment(SwingConstants.LEADING);

        String title = material.get(IndexField.TITLE);
        titleLabel.setText(title.length() > 100 ? title.substring(0, 100) + "..." : title);
        authorLabel.setText(material.getOrDefault(IndexField.AUTHOR, ""));
        typeLabel.setText(material.get(IndexField.TYPE));
        if (material.getOrDefault(IndexField.PATH, "").isEmpty()
                || !(new File(material.get(IndexField.PATH)).exists())) {
            readMaterialButton.setVisible(false);
        } else {
            if (material.get(IndexField.TYPE).equals(MaterialType.EBOOK.name())) {
                readMaterialButton.setText("Read");
                readMaterialButton.setVisible(true);
            } else if (material.get(IndexField.TYPE).equals(MaterialType.CD.name())) {
                readMaterialButton.setText("Open");
                readMaterialButton.setVisible(true);
            } else {
                readMaterialButton.setVisible(false);
            }
        }
    }

    /**
     * Setup components to show no result
     */
    public void showNoResult() {
        titleLabel.setText("Nothing Found");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        authorLabel.setVisible(false);
        typeLabel.setVisible(false);
        jLabel2.setVisible(false);
        jLabel4.setVisible(false);
        readMaterialButton.setVisible(false);
        viewMaterialButton.setVisible(false);
        jSeparator1.setVisible(false);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        viewMaterialButton = new javax.swing.JButton();
        authorLabel = new javax.swing.JLabel();
        typeLabel = new javax.swing.JLabel();
        readMaterialButton = new javax.swing.JButton();
        titleLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel4.setText("Type:");

        viewMaterialButton.setText("View");
        viewMaterialButton.setOpaque(false);
        viewMaterialButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMaterialButtonActionPerformed(evt);
            }
        });

        authorLabel.setText("<Author>");

        typeLabel.setText("<Type>");

        readMaterialButton.setText("Read");
        readMaterialButton.setOpaque(false);
        readMaterialButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readMaterialButtonActionPerformed(evt);
            }
        });

        titleLabel.setFont(new java.awt.Font("Segoe UI Semilight", 0, 16)); // NOI18N
        titleLabel.setText("<Title>");

        jLabel2.setText("Author:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1)
                    .addComponent(titleLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(authorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(typeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(viewMaterialButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(readMaterialButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(authorLabel)
                    .addComponent(jLabel4)
                    .addComponent(typeLabel)
                    .addComponent(readMaterialButton)
                    .addComponent(viewMaterialButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void readMaterialButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readMaterialButtonActionPerformed
        // TODO add your handling code here:
        if (Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(new File(result.get(IndexField.PATH)));
            } catch (IOException ex) {
                Utility.writeLog(ex);
            }
        }
    }//GEN-LAST:event_readMaterialButtonActionPerformed

    private void viewMaterialButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewMaterialButtonActionPerformed
        // TODO add your handling code here:
        MaterialInfoDialog.showDialog(result);
    }//GEN-LAST:event_viewMaterialButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel authorLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton readMaterialButton;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JButton viewMaterialButton;
    // End of variables declaration//GEN-END:variables
}
