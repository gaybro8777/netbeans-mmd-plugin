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
package com.igormaznitsa.nbmindmap.nb.refactoring.gui;

import java.awt.Component;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

public class RenamePanel extends javax.swing.JPanel implements CustomRefactoringPanel {

  private static final long serialVersionUID = -5231764322175432594L;

  private final AtomicBoolean initialized = new AtomicBoolean();

  private final Lookup lookup;
  private final ChangeListener parent;
  private final String name;

  public RenamePanel(final String name, final Lookup lookup, final ChangeListener parent) {
    initComponents();
    this.lookup = lookup;
    this.parent = parent;
    this.name = name;
  }

  @Override
  public void initialize() {
    if (this.initialized.compareAndSet(false, true)) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          textFieldName.setText(name);

          textFieldName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
              parent.stateChanged(null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
              parent.stateChanged(null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
              parent.stateChanged(null);
            }
          });

          if (!panelScope.initialize(lookup, new AtomicBoolean())) {
            labelScopeName.setVisible(false);
            panelScope.setVisible(false);
          }
          else {
            labelScopeName.setVisible(true);
            panelScope.setVisible(true);
          }
        }
      });
    }
  }

  public String getNewName() {
    return this.textFieldName.getText().trim();
  }

  @Override
  public Component getComponent() {
    return this;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    labelFieldTitle = new javax.swing.JLabel();
    textFieldName = new javax.swing.JTextField();
    labelScopeName = new javax.swing.JLabel();
    panelScope = new org.netbeans.modules.refactoring.spi.ui.ScopePanel(RenamePanel.class.getCanonicalName().replace('.', '-'),NbPreferences.forModule(RenamePanel.class),"renameFile.scope");

    labelFieldTitle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/igormaznitsa/nbmindmap/icons/logo/logo16.png"))); // NOI18N
    java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle"); // NOI18N
    org.openide.awt.Mnemonics.setLocalizedText(labelFieldTitle, bundle.getString("RenamePanel.labelFieldTitle.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(labelScopeName, bundle.getString("RenamePanel.labelScopeName.text")); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(labelFieldTitle)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(textFieldName))
          .addGroup(layout.createSequentialGroup()
            .addComponent(labelScopeName)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelScope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(labelFieldTitle)
          .addComponent(textFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
          .addComponent(labelScopeName)
          .addComponent(panelScope, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(38, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel labelFieldTitle;
  private javax.swing.JLabel labelScopeName;
  private org.netbeans.modules.refactoring.spi.ui.ScopePanel panelScope;
  private javax.swing.JTextField textFieldName;
  // End of variables declaration//GEN-END:variables
}
