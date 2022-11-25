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
package com.igormaznitsa.sciareto.ui.tree;

import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.ui.Icons;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.PictureViewer;
import java.awt.Component;
import java.awt.Image;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.util.Locale;

public final class TreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = -6283018126496160094L;

  private static final Image PROJECT_BADGE = UiUtils.loadIcon("project_badge.png"); //NOI18N
  private static final Image KF_BADGE = UiUtils.loadIcon("mmdbadge.png"); //NOI18N
  private static final Image READONLY_BADGE = UiUtils.loadIcon("ro.png"); //NOI18N
  private static final Icon ICON_IMAGE_RO;

  public static final Icon ICON_IMAGE = new ImageIcon(UiUtils.loadIcon("image16.png")); //NOI18N
  public static final Icon DEFAULT_FOLDER_CLOSED = new ImageIcon(UiUtils.loadIcon("folder16.gif")); //NOI18N
  public static final Icon DEFAULT_FOLDER_OPENED = new ImageIcon(UiUtils.loadIcon("folderOpen16.gif")); //NOI18N
  public static final Icon DEFAULT_FILE = new ImageIcon(UiUtils.loadIcon("document_empty16.png")); //NOI18N
  public static final Icon PLANTUML_FILE = new ImageIcon(UiUtils.loadIcon("document_plantuml16.png")); //NOI18N
  public static final Icon KSTPL_FILE = new ImageIcon(UiUtils.loadIcon("document_kstpl16.png")); //NOI18N

  static {
    ICON_IMAGE_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) ICON_IMAGE).getImage(), READONLY_BADGE));
  }

  private Icon PROJECT_CLOSED;
  private Icon PROJECT_OPENED;

  private Icon PROJECT_CLOSED_RO;
  private Icon PROJECT_OPENED_RO;

  private Icon LEAF;
  private Icon LEAF_RO;

  private Icon FOLDER_CLOSED;
  private Icon FOLDER_CLOSED_RO;

  private Icon FOLDER_KF_CLOSED;
  private Icon FOLDER_KF_CLOSED_RO;

  private Icon FOLDER_OPENED;
  private Icon FOLDER_OPENED_RO;

  private Icon FOLDER_KF_OPENED;
  private Icon FOLDER_KF_OPENED_RO;

  private Icon LEAF_MINDMAP;
  private Icon LEAF_MINDMAP_RO;

  private Icon LEAF_EMPTY;
  private Icon LEAF_EMPTY_RO;

  private Icon LEAF_PLANTUML;
  private Icon LEAF_PLANTUML_RO;

  private Icon LEAF_KSTPL;
  private Icon LEAF_KSTPL_RO;

  public TreeCellRenderer() {
    super();
  }

  private void ensureIcons(@Nonnull final JTree tree) {
    if (PROJECT_CLOSED == null) {
      PROJECT_CLOSED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.closedIcon"), DEFAULT_FOLDER_CLOSED)), PROJECT_BADGE)); //NOI18N
      PROJECT_CLOSED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) PROJECT_CLOSED).getImage(), READONLY_BADGE));
    }

    if (PROJECT_OPENED == null) {
      PROJECT_OPENED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.openIcon"), DEFAULT_FOLDER_OPENED)), PROJECT_BADGE)); //NOI18N
      PROJECT_OPENED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) PROJECT_OPENED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_CLOSED == null) {
      FOLDER_CLOSED = new ImageIcon(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.closedIcon"), DEFAULT_FOLDER_CLOSED))); //NOI18N
      FOLDER_CLOSED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_CLOSED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_OPENED == null) {
      FOLDER_OPENED = new ImageIcon(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.openIcon"), DEFAULT_FOLDER_OPENED))); //NOI18N
      FOLDER_OPENED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_OPENED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_KF_CLOSED == null) {
      FOLDER_KF_CLOSED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.closedIcon"), DEFAULT_FOLDER_CLOSED)), KF_BADGE)); //NOI18N
      FOLDER_KF_CLOSED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_KF_CLOSED).getImage(), READONLY_BADGE));
    }

    if (FOLDER_KF_OPENED == null) {
      FOLDER_KF_OPENED = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.openIcon"), DEFAULT_FOLDER_OPENED)), KF_BADGE)); //NOI18N
      FOLDER_KF_OPENED_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) FOLDER_KF_OPENED).getImage(), READONLY_BADGE));
    }

    if (LEAF == null) {
      LEAF = new ImageIcon(UiUtils.iconToImage(tree, GetUtils.ensureNonNull(UIManager.getIcon("Tree.leafIcon"), DEFAULT_FILE))); //NOI18N
      LEAF_RO = new ImageIcon(UiUtils.makeBadgedRightTop(((ImageIcon) LEAF).getImage(), READONLY_BADGE));
    }

    if (LEAF_MINDMAP == null) {
      LEAF_MINDMAP = Icons.DOCUMENT.getIcon();
      LEAF_MINDMAP_RO = new ImageIcon(UiUtils.makeBadgedRightTop(Icons.DOCUMENT.getIcon().getImage(), READONLY_BADGE));
    }

    if (LEAF_EMPTY == null) {
      LEAF_EMPTY = new ImageIcon(UiUtils.iconToImage(tree, DEFAULT_FILE)); //NOI18N
      LEAF_EMPTY_RO = new ImageIcon(UiUtils.makeBadgedRightTop(UiUtils.iconToImage(tree, DEFAULT_FILE), READONLY_BADGE));
    }

    if (LEAF_PLANTUML == null) {
      LEAF_PLANTUML = new ImageIcon(UiUtils.iconToImage(tree, PLANTUML_FILE)); //NOI18N
      LEAF_PLANTUML_RO = new ImageIcon(UiUtils.makeBadgedRightTop(UiUtils.iconToImage(tree, PLANTUML_FILE), READONLY_BADGE));
    }

    if (LEAF_KSTPL == null) {
      LEAF_KSTPL = new ImageIcon(UiUtils.iconToImage(tree, KSTPL_FILE)); //NOI18N
      LEAF_KSTPL_RO = new ImageIcon(UiUtils.makeBadgedRightTop(UiUtils.iconToImage(tree, KSTPL_FILE), READONLY_BADGE));
    }
  }

  @Override
  @Nonnull
  public Component getTreeCellRendererComponent(@Nonnull final JTree tree, @Nullable final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
    ensureIcons(tree);
    final JLabel result = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    if (value != null) {
      if (value instanceof NodeFileOrFolder) {
        final NodeFileOrFolder node = (NodeFileOrFolder) value;
        result.setEnabled(true);
        if (node.isLoading()) {
          result.setIcon(ProjectLoadingIconAnimationController.LOADING);
        } else if (node.hasNoAccess()) {
          result.setEnabled(false);
          result.setDisabledIcon(LEAF_RO);
        } else if (node instanceof NodeProject) {
          if (node.isReadOnly()) {
            result.setIcon(expanded ? PROJECT_OPENED_RO : PROJECT_CLOSED_RO);
          } else {
            result.setIcon(expanded ? PROJECT_OPENED : PROJECT_CLOSED);
          }
        } else if (node.isLeaf()) {
          final String ext = FilenameUtils.getExtension(node.toString()).toLowerCase(Locale.ENGLISH);
          if (Utils.isPlantUmlFileExtension(ext)) {
            result.setIcon(node.isReadOnly() ? LEAF_PLANTUML_RO : LEAF_PLANTUML);
          } else if (ext.equals("kstpl")) { //NOI18N
            result.setIcon(node.isReadOnly() ? LEAF_KSTPL_RO : LEAF_KSTPL);
          } else if (ext.equals("mmd")) { //NOI18N
            result.setIcon(node.isReadOnly() ? LEAF_MINDMAP_RO : LEAF_MINDMAP);
          } else if (PictureViewer.SUPPORTED_FORMATS.contains(ext)) {
            result.setIcon(node.isReadOnly() ? ICON_IMAGE_RO : ICON_IMAGE);
          } else {
            result.setIcon(node.isReadOnly() ? LEAF_EMPTY_RO : LEAF_EMPTY);
          }
        } else if (node.isProjectKnowledgeFolder()) {
          result.setText("Knowledge");
          if (node.isReadOnly()) {
            result.setIcon(expanded ? FOLDER_KF_OPENED_RO : FOLDER_KF_CLOSED_RO);
          } else {
            result.setIcon(expanded ? FOLDER_KF_OPENED : FOLDER_KF_CLOSED);
          }
        } else if (node.isReadOnly()) {
          result.setIcon(expanded ? FOLDER_OPENED_RO : FOLDER_CLOSED_RO);
        } else {
          result.setIcon(expanded ? FOLDER_OPENED : FOLDER_CLOSED);
        }
      }
    }
    return result;
  }
}
