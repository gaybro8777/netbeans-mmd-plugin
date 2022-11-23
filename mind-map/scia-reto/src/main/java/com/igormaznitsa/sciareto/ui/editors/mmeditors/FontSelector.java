/* 
 * Copyright (C) 2018 Igor Maznitsa.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package com.igormaznitsa.sciareto.ui.editors.mmeditors;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

public final class FontSelector extends javax.swing.JPanel implements ActionListener {

  private static final long serialVersionUID = -9217845584936911985L;

  public FontSelector(final Font initial) {
    initComponents();
    final ComboBoxModel<String> modelName = new DefaultComboBoxModel<>(getAllFontFamilies());
    this.comboBoxName.setModel(modelName);
    this.comboBoxName.setSelectedItem(initial.getName());

    final ComboBoxModel<String> modelStyle = new DefaultComboBoxModel<>(new String[]{"Plain", "Bold", "Italic", "Bold+Italic"}); //NOI18N
    this.comboBoxStyle.setModel(modelStyle);

    this.textArea.setWrapStyleWord(true);
    this.textArea.setLineWrap(true);
    this.textArea.setText("Sed ut perspiciatis unde omnis iste natus error. Sit voluptatem accusantium doloremque laudantium. Totam rem aperiam, eaque ipsa quae ab illo."); //NOI18N
    this.textArea.setEditable(false);

    selectForStyle(initial.getStyle());

    final List<Integer> sizes = new ArrayList<>();
    for (int i = 3; i < 72; i++) {
      sizes.add(i);
    }
    final ComboBoxModel<Integer> modelSize = new DefaultComboBoxModel<>(sizes.toArray(new Integer[]{sizes.size()}));
    this.comboBoxSize.setModel(modelSize);

    this.comboBoxSize.setSelectedItem(initial.getSize());

    this.textArea.setFont(getValue());

    this.comboBoxName.addActionListener(this);
    this.comboBoxStyle.addActionListener(this);
    this.comboBoxSize.addActionListener(this);

    final Dimension size = new Dimension(500, 300);

    this.textArea.setMinimumSize(size);
    this.textArea.setPreferredSize(size);
  }

  private String [] getAllFontFamilies(){
    return GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
  }
  
  @Override
  public void actionPerformed(ActionEvent e) {
    this.textArea.setFont(getValue());
  }

  private void selectForStyle(final int style) {
    switch (style) {
      case Font.PLAIN:
        this.comboBoxStyle.setSelectedIndex(0);
        break;
      case Font.BOLD:
        this.comboBoxStyle.setSelectedIndex(1);
        break;
      case Font.ITALIC:
        this.comboBoxStyle.setSelectedIndex(2);
        break;
      default:
        this.comboBoxStyle.setSelectedIndex(3);
        break;
    }
  }

  private int getFontStyle() {
    switch (this.comboBoxStyle.getSelectedIndex()) {
      case 0:
        return Font.PLAIN;
      case 1:
        return Font.BOLD;
      case 2:
        return Font.ITALIC;
      default:
        return Font.BOLD | Font.ITALIC;
    }
  }

  public Font getValue() {
    final String family = (String) this.comboBoxName.getSelectedItem();
    final int style = getFontStyle();
    final int size = (Integer) this.comboBoxSize.getSelectedItem();
    return new Font(family, style, size);
  }
  
  
  /**
   * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
   * Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        comboBoxName = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        comboBoxStyle = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        comboBoxSize = new javax.swing.JComboBox<>();

        setLayout(new java.awt.GridBagLayout());

        textArea.setColumns(20);
        textArea.setRows(5);
        jScrollPane1.setViewportView(textArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 404;
        gridBagConstraints.ipady = 224;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("panelFontSelector.labelName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 80;
        jPanel1.add(comboBoxName, gridBagConstraints);

        jLabel2.setText(bundle.getString("panelFontSelector.labelStyle")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 80;
        jPanel1.add(comboBoxStyle, gridBagConstraints);

        jLabel3.setText(bundle.getString("panelFontSelector.labelSize")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 80;
        jPanel1.add(comboBoxSize, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 8, 0);
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> comboBoxName;
    private javax.swing.JComboBox<Integer> comboBoxSize;
    private javax.swing.JComboBox<String> comboBoxStyle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
}
