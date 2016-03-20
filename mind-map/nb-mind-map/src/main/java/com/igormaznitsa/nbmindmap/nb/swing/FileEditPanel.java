/*
 * Copyright 2015 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.nbmindmap.nb.swing;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.nbmindmap.utils.NbUtils;
import java.io.File;
import javax.swing.JFileChooser;

public final class FileEditPanel extends javax.swing.JPanel {

  public static final class DataContainer {

    private final String path;
    private final boolean showWithSystemTool;

    public DataContainer(final String path, final boolean showWithSystemTool) {
      this.path = path == null ? "" : path;
      this.showWithSystemTool = showWithSystemTool;
    }

    public String getPath() {
      return this.path;
    }

    public boolean isShowWithSystemTool() {
      return this.showWithSystemTool;
    }

    public boolean isEmpty() {
      return this.path.trim().isEmpty();
    }
    
    public boolean isValid () {
      try {
        return this.path.isEmpty() ? true : new File(this.path).exists();
      }
      catch (Exception ex) {
        return false;
      }
    }

  }

  private static final long serialVersionUID = -6683682013891751388L;
  private final File projectFolder;

  public FileEditPanel(final File projectFolder, final DataContainer initialData) {
    initComponents();
    this.projectFolder = projectFolder;
    this.textFieldFilePath.setText(initialData == null ? "" : initialData.getPath());
    this.checkBoxShowFileInSystem.setSelected(initialData == null ? false : initialData.isShowWithSystemTool());
  }

  public DataContainer getData() {
    return new DataContainer(this.textFieldFilePath.getText().trim(), this.checkBoxShowFileInSystem.isSelected());
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    labelBrowseCurrentLink = new javax.swing.JLabel();
    textFieldFilePath = new javax.swing.JTextField();
    buttonChooseFile = new javax.swing.JButton();
    buttonReset = new javax.swing.JButton();
    optionPanel = new javax.swing.JPanel();
    checkBoxShowFileInSystem = new javax.swing.JCheckBox();

    setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 1, 10));
    setLayout(new java.awt.GridBagLayout());

    labelBrowseCurrentLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/file_link.png"))); // NOI18N
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
    labelBrowseCurrentLink.setToolTipText(bundle.getString("FileEditPanel.labelBrowseCurrentLink.toolTipText")); // NOI18N
    labelBrowseCurrentLink.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    labelBrowseCurrentLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    labelBrowseCurrentLink.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelBrowseCurrentLinkMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 10;
    add(labelBrowseCurrentLink, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1000.0;
    add(textFieldFilePath, gridBagConstraints);

    buttonChooseFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/file_manager.png"))); // NOI18N
    buttonChooseFile.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonChooseFileActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(buttonChooseFile, gridBagConstraints);

    buttonReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/cross16.png"))); // NOI18N
    buttonReset.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonResetActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(buttonReset, gridBagConstraints);

    optionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

    org.openide.awt.Mnemonics.setLocalizedText(checkBoxShowFileInSystem, bundle.getString("FileEditPanel.checkBoxShowFileInSystem.text")); // NOI18N
    optionPanel.add(checkBoxShowFileInSystem);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipadx = 2;
    add(optionPanel, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void labelBrowseCurrentLinkMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelBrowseCurrentLinkMouseClicked
    if (evt.getClickCount() > 1) {
      final File file = new File(this.textFieldFilePath.getText().trim());
      NbUtils.openInSystemViewer(file);
    }
  }//GEN-LAST:event_labelBrowseCurrentLinkMouseClicked

  private void buttonChooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonChooseFileActionPerformed
    final File theFile = new File(this.textFieldFilePath.getText().trim());
    final File parent = theFile.getParentFile();

    final JFileChooser chooser = new JFileChooser(parent == null ? this.projectFolder : parent);
    if (theFile.isFile()) {
      chooser.setSelectedFile(theFile);
    }
    chooser.setApproveButtonText("Select");
    chooser.setDialogTitle("Select file");
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      final File selected = chooser.getSelectedFile();
      this.textFieldFilePath.setText(selected.getAbsolutePath());
    }
  }//GEN-LAST:event_buttonChooseFileActionPerformed

  private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
    this.textFieldFilePath.setText("");
  }//GEN-LAST:event_buttonResetActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonChooseFile;
  private javax.swing.JButton buttonReset;
  private javax.swing.JCheckBox checkBoxShowFileInSystem;
  private javax.swing.JLabel labelBrowseCurrentLink;
  private javax.swing.JPanel optionPanel;
  private javax.swing.JTextField textFieldFilePath;
  // End of variables declaration//GEN-END:variables
}
