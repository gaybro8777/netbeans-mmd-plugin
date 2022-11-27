/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.nbmindmap.nb.swing;

import static com.igormaznitsa.mindmap.ide.commons.Misc.string2pattern;
import com.igormaznitsa.nbmindmap.nb.editor.MMDGraphEditor;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class FindTextPanel extends javax.swing.JPanel implements FindTextScopeProvider {

  private static final long serialVersionUID = -2286996344502363552L;

  private static final int TEXT_FIELD_WIDTH = 300;

  private static boolean stateCaseSensetive = false;
  private static boolean stateInTopicText = true;
  private static boolean stateInNote = true;
  private static boolean stateInFile = true;
  private static boolean stateInURI = true;

  private final MMDGraphEditor baseEditor;

  public FindTextPanel(@Nonnull final MMDGraphEditor editor, @Nullable final String text) {
    initComponents();
    this.setVisible(false);

    this.baseEditor = editor;

    this.toggleButtonCaseSensitive.setSelected(stateCaseSensetive);
    this.toggleButtonTopicText.setSelected(stateInTopicText);
    this.toggleButtonFile.setSelected(stateInFile);
    this.toggleButtonNote.setSelected(stateInNote);
    this.toggleButtonURI.setSelected(stateInURI);

    final ActionListener stateListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        stateCaseSensetive = toggleButtonCaseSensitive.isSelected();
        stateInTopicText = toggleButtonTopicText.isSelected();
        stateInFile = toggleButtonFile.isSelected();
        stateInNote = toggleButtonNote.isSelected();
        stateInURI = toggleButtonURI.isSelected();
      }
    };

    this.toggleButtonCaseSensitive.addActionListener(stateListener);
    this.toggleButtonFile.addActionListener(stateListener);
    this.toggleButtonNote.addActionListener(stateListener);
    this.toggleButtonTopicText.addActionListener(stateListener);
    this.toggleButtonURI.addActionListener(stateListener);

    this.textFieldSearchText.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, this.textFieldSearchText.getPreferredSize().height));
    this.textFieldSearchText.setMinimumSize(new Dimension(TEXT_FIELD_WIDTH, this.textFieldSearchText.getMinimumSize().height));
    this.textFieldSearchText.setMaximumSize(new Dimension(TEXT_FIELD_WIDTH, this.textFieldSearchText.getMaximumSize().height));

    this.textFieldSearchText.setText(text == null ? "" : text); //NOI18N

    this.textFieldSearchText.setFocusTraversalPolicy(new FocusTraversalPolicy() {
      @Override
      @Nonnull
      public Component getComponentAfter(@Nonnull final Container aContainer, @Nonnull final Component aComponent) {
        return textFieldSearchText;
      }

      @Override
      @Nonnull
      public Component getComponentBefore(@Nonnull final Container aContainer, @Nonnull final Component aComponent) {
        return textFieldSearchText;
      }

      @Override
      @Nonnull
      public Component getFirstComponent(@Nonnull final Container aContainer) {
        return textFieldSearchText;
      }

      @Override
      @Nonnull
      public Component getLastComponent(@Nonnull final Container aContainer) {
        return textFieldSearchText;
      }

      @Override
      @Nonnull
      public Component getDefaultComponent(@Nonnull final Container aContainer) {
        return textFieldSearchText;
      }
    });
  }

  @Override
  public void requestFocus() {
    this.textFieldSearchText.requestFocus();
  }

  @Override
  public boolean toSearchIn(@Nonnull final SearchTextScope scope) {
    switch (scope) {
      case CASE_INSENSETIVE:
        return !this.toggleButtonCaseSensitive.isSelected();
      case IN_TOPIC_NOTES:
        return this.toggleButtonNote.isSelected();
      case IN_TOPIC_TEXT:
        return this.toggleButtonTopicText.isSelected();
      case IN_TOPIC_FILES:
        return this.toggleButtonFile.isSelected();
      case IN_TOPIC_URI:
        return this.toggleButtonURI.isSelected();
      default:
        return false;
    }
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

    labelTitle = new javax.swing.JLabel();
    textFieldSearchText = new javax.swing.JTextField();
    buttonPrev = new javax.swing.JButton();
    buttonNext = new javax.swing.JButton();
    labelClose = new javax.swing.JLabel();
    filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
    toggleButtonCaseSensitive = new javax.swing.JToggleButton();
    panelButtonsForMap = new javax.swing.JPanel();
    toggleButtonTopicText = new javax.swing.JToggleButton();
    toggleButtonNote = new javax.swing.JToggleButton();
    toggleButtonFile = new javax.swing.JToggleButton();
    toggleButtonURI = new javax.swing.JToggleButton();

    setLayout(new java.awt.GridBagLayout());

    labelTitle.setText("Find:");
    labelTitle.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 10.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 16, 0, 8);
    add(labelTitle, gridBagConstraints);

    textFieldSearchText.setFocusTraversalPolicyProvider(true);
    textFieldSearchText.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent evt) {
        textFieldSearchTextKeyPressed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
    add(textFieldSearchText, gridBagConstraints);

    buttonPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/find/resultset_previous.png"))); // NOI18N
    buttonPrev.setToolTipText("Find previous (SHFT+ENTER)");
    buttonPrev.setFocusable(false);
    buttonPrev.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonPrevActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 10.0;
    add(buttonPrev, gridBagConstraints);

    buttonNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/find/resultset_next.png"))); // NOI18N
    buttonNext.setToolTipText("Find next (ENTER)");
    buttonNext.setFocusable(false);
    buttonNext.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        buttonNextActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 10.0;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
    add(buttonNext, gridBagConstraints);

    labelClose.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    labelClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/find/nimbusCloseFrame.png"))); // NOI18N
    labelClose.setToolTipText("Close search form (ESC)");
    labelClose.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    labelClose.setFocusable(false);
    labelClose.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        labelCloseMouseClicked(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 7;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
    add(labelClose, gridBagConstraints);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 6;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 100000.0;
    add(filler1, gridBagConstraints);

    toggleButtonCaseSensitive.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/find/case16.png"))); // NOI18N
    toggleButtonCaseSensitive.setToolTipText("Case sensitive mode");
    toggleButtonCaseSensitive.setFocusable(false);
    toggleButtonCaseSensitive.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        toggleButtonCaseSensitiveActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 4;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
    add(toggleButtonCaseSensitive, gridBagConstraints);

    panelButtonsForMap.setLayout(new java.awt.GridBagLayout());

    toggleButtonTopicText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/find/text16.png"))); // NOI18N
    toggleButtonTopicText.setToolTipText("Find in topic title");
    toggleButtonTopicText.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    panelButtonsForMap.add(toggleButtonTopicText, gridBagConstraints);

    toggleButtonNote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/find/note16.png"))); // NOI18N
    toggleButtonNote.setToolTipText("Find in topic notes");
    toggleButtonNote.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    panelButtonsForMap.add(toggleButtonNote, gridBagConstraints);

    toggleButtonFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/find/disk16.png"))); // NOI18N
    toggleButtonFile.setToolTipText("Find in file links");
    toggleButtonFile.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    panelButtonsForMap.add(toggleButtonFile, gridBagConstraints);

    toggleButtonURI.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/find/url16.png"))); // NOI18N
    toggleButtonURI.setToolTipText("Find in URI links");
    toggleButtonURI.setFocusable(false);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    panelButtonsForMap.add(toggleButtonURI, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 5;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    add(panelButtonsForMap, gridBagConstraints);
  }// </editor-fold>//GEN-END:initComponents

  private void textFieldSearchTextKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldSearchTextKeyPressed
    switch (evt.getKeyCode()) {
      case KeyEvent.VK_ESCAPE: {
        this.setVisible(false);
        this.baseEditor.requestActive();
        evt.consume();
      }
      break;
      case KeyEvent.VK_ENTER: {
        if (evt.isShiftDown()) {
          findPrev();
        } else {
          findNext();
        }
        evt.consume();
      }
      break;
    }
  }//GEN-LAST:event_textFieldSearchTextKeyPressed

  private void findNext() {
    final String text = this.textFieldSearchText.getText();
    if (!text.isEmpty()) {
      this.baseEditor.findNext(string2pattern(text, this.toggleButtonCaseSensitive.isSelected() ? Pattern.UNICODE_CASE : (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)), this);
    }
  }

  private void findPrev() {
    final String text = this.textFieldSearchText.getText();
    if (!text.isEmpty()) {
      this.baseEditor.findPrev(string2pattern(text, this.toggleButtonCaseSensitive.isSelected() ? Pattern.UNICODE_CASE : (Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE)), this);
    }
  }

  public boolean activate() {
    boolean activated = false;
    if (!this.isVisible()) {
      activated = true;
      this.textFieldSearchText.setText("");
      this.setVisible(true);
    }
    this.textFieldSearchText.requestFocus();
    return activated;
  }

  public void deactivate() {
    if (this.isVisible()) {
      this.setVisible(false);
    }
    this.baseEditor.requestActive();
  }

  private void labelCloseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelCloseMouseClicked
    this.setVisible(false);
  }//GEN-LAST:event_labelCloseMouseClicked

  private void buttonPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPrevActionPerformed
    findPrev();
  }//GEN-LAST:event_buttonPrevActionPerformed

  private void buttonNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonNextActionPerformed
    findNext();
  }//GEN-LAST:event_buttonNextActionPerformed

  private void toggleButtonCaseSensitiveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleButtonCaseSensitiveActionPerformed
    stateCaseSensetive = this.toggleButtonCaseSensitive.isSelected();
  }//GEN-LAST:event_toggleButtonCaseSensitiveActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton buttonNext;
  private javax.swing.JButton buttonPrev;
  private javax.swing.Box.Filler filler1;
  private javax.swing.JLabel labelClose;
  private javax.swing.JLabel labelTitle;
  private javax.swing.JPanel panelButtonsForMap;
  private javax.swing.JTextField textFieldSearchText;
  private javax.swing.JToggleButton toggleButtonCaseSensitive;
  private javax.swing.JToggleButton toggleButtonFile;
  private javax.swing.JToggleButton toggleButtonNote;
  private javax.swing.JToggleButton toggleButtonTopicText;
  private javax.swing.JToggleButton toggleButtonURI;
  // End of variables declaration//GEN-END:variables

}
