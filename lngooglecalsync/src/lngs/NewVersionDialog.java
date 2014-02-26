package lngs;

import java.awt.*;
import javax.swing.*;

public class NewVersionDialog extends javax.swing.JDialog {

    /** Creates new form NewVersionDialog */
    public NewVersionDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        SetAppVersion("");
        SetHelpFilename("");

        // Scroll to the top of the list of changes
        jTextPane_Changes.setCaretPosition(0);

        jButton_OK.requestFocus();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton_OK = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane_Changes = new javax.swing.JTextPane();
        jLabel_Heading1 = new javax.swing.JLabel();
        jLabel_HelpFile = new javax.swing.JLabel();
        jLabel_Heading3 = new javax.swing.JLabel();
        jLabel_Donate = new javax.swing.JLabel();
        jLabel_Heading2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New Version Announcement");
        setResizable(false);

        jButton_OK.setMnemonic('O');
        jButton_OK.setText("OK");
        jButton_OK.setSelected(true);
        jButton_OK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_OKActionPerformed(evt);
            }
        });

        jTextPane_Changes.setEditable(false);
        jTextPane_Changes.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jTextPane_Changes.setText("o Fix: Several changes were made to prevent events from being deleted/recreated over and over due to Description differences.\no Fix: For some users, all-day events were being created one-day early. A (second) change was made that might fix this problem.");
        jScrollPane2.setViewportView(jTextPane_Changes);

        jLabel_Heading1.setText("This version is being run for the first time in GUI mode.");

        jLabel_HelpFile.setText("<HTML><FONT color=\\\"#000099\\\"><U>Help File.</U></FONT></HTML>");
        jLabel_HelpFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_HelpFileMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel_HelpFileMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel_HelpFileMouseExited(evt);
            }
        });

        jLabel_Heading3.setText("Here are the changes from the History Log in the");

        jLabel_Donate.setText("<HTML><FONT color=\\\"#000099\\\"><U>Donate $5 (PayPal)</U></FONT></HTML>");
        jLabel_Donate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel_DonateMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jLabel_DonateMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jLabel_DonateMouseExited(evt);
            }
        });

        jLabel_Heading2.setText("<html>To upgrade:<br>1. Unzip the new files.<br>2. Copy your old lngsync.config file into the new-version directory.<br>3. If you modified the old lngsync.vbs or lngsync.sh files, then merge those changes into the new vbs/sh files.<br>4. Copy your old client_secret.json file into the new-version directory.</html>");
        jLabel_Heading2.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(330, 330, 330)
                                .addComponent(jButton_OK)
                                .addGap(55, 55, 55)
                                .addComponent(jLabel_Donate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel_Heading1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel_Heading3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel_HelpFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel_Heading2, javax.swing.GroupLayout.PREFERRED_SIZE, 666, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 735, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel_Heading1)
                    .addComponent(jLabel_Heading3)
                    .addComponent(jLabel_HelpFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_Heading2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton_OK)
                    .addComponent(jLabel_Donate, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_OKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_OKActionPerformed
        dispose();
}//GEN-LAST:event_jButton_OKActionPerformed

    private void jLabel_HelpFileMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_HelpFileMouseEntered
        if (!helpFilename.isEmpty()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }//GEN-LAST:event_jLabel_HelpFileMouseEntered

    private void jLabel_HelpFileMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_HelpFileMouseExited
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_jLabel_HelpFileMouseExited

    private void jLabel_HelpFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_HelpFileMouseClicked
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(helpFilename));
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "There was a problem opening the help file.", "Can't Open Help File", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jLabel_HelpFileMouseClicked

    private void jLabel_DonateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_DonateMouseClicked
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=JSNX6GEWA8JK8&lc=US&item_name=LNGS&item_number=LNGS&amount=5%2e00&currency_code=USD&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted"));
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "There was a problem opening the help file.", "Can't Open Donate Page", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jLabel_DonateMouseClicked

    private void jLabel_DonateMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_DonateMouseEntered
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_jLabel_DonateMouseEntered

    private void jLabel_DonateMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel_DonateMouseExited
        setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_jLabel_DonateMouseExited


    public void SetAppVersion(String appVersion)
    {
        this.appVersion = appVersion;
        
        String versionMsg = "This version";
        if (!appVersion.isEmpty()) {
            jLabel_Heading1.setText("Version " + appVersion + " is being run for the first time in GUI mode.");
        }
    }

    private String appVersion = "";

    public void SetHelpFilename(String helpFilename)
    {
        this.helpFilename = helpFilename;
    }

    private String helpFilename = "";

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_OK;
    private javax.swing.JLabel jLabel_Donate;
    private javax.swing.JLabel jLabel_Heading1;
    private javax.swing.JLabel jLabel_Heading2;
    private javax.swing.JLabel jLabel_Heading3;
    private javax.swing.JLabel jLabel_HelpFile;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextPane jTextPane_Changes;
    // End of variables declaration//GEN-END:variables

}
