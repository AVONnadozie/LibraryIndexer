
/**
 *
 * @author Anuebunwa Victor
 */
public class Main {

    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting">        
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName())
                    .log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form when ready*/
        java.awt.EventQueue.invokeLater(() -> {
            MainWindow window = MainWindow.getInstance();
            Utility.centreOnScreen(window);
            window.setVisible(true);
        });

    }

    public static void closeApp(){
        MainWindow.getInstance().setVisible(false);
        
        System.exit(0);
    }
    
}
