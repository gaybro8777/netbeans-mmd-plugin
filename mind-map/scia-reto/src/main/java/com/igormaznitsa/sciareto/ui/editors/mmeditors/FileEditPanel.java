/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.igormaznitsa.sciareto.ui.editors.mmeditors;

import com.igormaznitsa.mindmap.ide.commons.FilePathWithLine;
import com.igormaznitsa.sciareto.ui.UiUtils;
import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JFileChooser;
import com.igormaznitsa.mindmap.ide.commons.SwingUtils;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import com.igormaznitsa.mindmap.swing.panel.utils.Focuser;
import com.igormaznitsa.sciareto.ui.SrI18n;

public final class FileEditPanel extends javax.swing.JPanel {

  public static final class DataContainer {

    private final FilePathWithLine filePathWithLine;
    private final boolean showWithSystemTool;

    public DataContainer(@Nullable final String path, final boolean showWithSystemTool) {
      this.filePathWithLine = new FilePathWithLine(path);
      this.showWithSystemTool = showWithSystemTool;
    }

    @Nonnull
    public FilePathWithLine getFilePathWithLine() {
      return this.filePathWithLine;
    }

    public boolean isShowWithSystemTool() {
      return this.showWithSystemTool;
    }

    public boolean isEmptyOrOnlySpaces() {
      return this.filePathWithLine.isEmptyOrOnlySpaces();
    }

    public boolean isValid() {
      try {
        return this.filePathWithLine.isEmptyOrOnlySpaces() ? true : new File(this.filePathWithLine.getPath()).exists();
      } catch (Exception ex) {
        return false;
      }
    }

  }

  private static final long serialVersionUID = -6683682013891751388L;
  private final File projectFolder;

  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  public FileEditPanel(@Nullable final File projectFolder, @Nullable final DataContainer initialData) {
    initComponents();
    this.projectFolder = projectFolder;
    this.textFieldFilePath.setText(initialData.getFilePathWithLine().toString()); //NOI18N
    this.checkBoxShowFileInSystem.setSelected(initialData == null ? false : initialData.isShowWithSystemTool());
    this.textFieldFilePath.setComponentPopupMenu(SwingUtils.addTextActions(UIComponentFactoryProvider.findInstance().makePopupMenu()));
    new Focuser(this.textFieldFilePath);
  }

  @Nonnull
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

        labelBrowseCurrentLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/file_link.png"))); // NOI18N
        labelBrowseCurrentLink.setToolTipText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("panelFileEdit.clickIcon.tooltip")); // NOI18N
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

        buttonChooseFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/file_manager.png"))); // NOI18N
        buttonChooseFile.setToolTipText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("panelFileEdit.tooltipSelectFile")); // NOI18N
        buttonChooseFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonChooseFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(buttonChooseFile, gridBagConstraints);

        buttonReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cross16.png"))); // NOI18N
        buttonReset.setToolTipText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("panelFileEdit.tooltipClearValue")); // NOI18N
        buttonReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResetActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(buttonReset, gridBagConstraints);

        optionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        checkBoxShowFileInSystem.setText(com.igormaznitsa.sciareto.ui.SrI18n.getInstance().findBundle().getString("panelFileEdit.checkboxOpenInSystemBrowser")); // NOI18N
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
      UiUtils.openInSystemViewer(file);
    }
  }//GEN-LAST:event_labelBrowseCurrentLinkMouseClicked

  private void buttonChooseFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonChooseFileActionPerformed
    final File theFile = new File(this.textFieldFilePath.getText().trim());
    final File parent = theFile.getParentFile();

    final JFileChooser chooser = new JFileChooser(parent == null ? this.projectFolder : parent);
    if (theFile.isFile()) {
      chooser.setSelectedFile(theFile);
    }
    chooser.setApproveButtonText(SrI18n.getInstance().findBundle().getString("FileEditPanel.fileChooser.approve"));
    chooser.setDialogTitle(SrI18n.getInstance().findBundle().getString("FileEditPanel.fileChooser.title"));
    chooser.setMultiSelectionEnabled(false);
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      final File selected = chooser.getSelectedFile();
      this.textFieldFilePath.setText(selected.getAbsolutePath());
    }
  }//GEN-LAST:event_buttonChooseFileActionPerformed

  private void buttonResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResetActionPerformed
    this.textFieldFilePath.setText(""); //NOI18N
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
