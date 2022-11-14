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
package com.igormaznitsa.sciareto.ui.tree;

import static com.igormaznitsa.sciareto.SciaRetoStarter.IDE_VERSION;

import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.common.utils.Assertions;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.StandardMmdAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.preferences.PrefUtils;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;
import com.igormaznitsa.sciareto.ui.FindFilesForTextPanel;
import com.igormaznitsa.sciareto.ui.FindUsagesPanel;
import com.igormaznitsa.sciareto.ui.MainFrame;
import com.igormaznitsa.sciareto.ui.UiUtils;
import com.igormaznitsa.sciareto.ui.editors.DotScriptEditor;
import com.igormaznitsa.sciareto.ui.editors.EditorContentType;
import com.igormaznitsa.sciareto.ui.editors.KsTplTextEditor;
import com.igormaznitsa.sciareto.ui.editors.MMDEditor;
import com.igormaznitsa.sciareto.ui.editors.PlantUmlTextEditor;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public final class ExplorerTree extends JScrollPane {

  private static final long serialVersionUID = 3894835807758698784L;
  private static final Logger LOGGER = LoggerFactory.getLogger(ExplorerTree.class);

  private final DnDTree projectTree;
  private final Context context;

  private static final Icon CROSS_ICON = new ImageIcon(UiUtils.loadIcon("nimbusCloseFrame.png"));

  public ExplorerTree(@Nonnull final Context context) throws IOException {
    super();
    this.projectTree = new DnDTree();
    this.context = context;
    this.projectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.projectTree.setDropMode(DropMode.ON);
    
    this.projectTree.setEditable(true);

    ToolTipManager.sharedInstance().registerComponent(this.projectTree);

    this.projectTree.setCellRenderer(new TreeCellRenderer());
    this.projectTree.setModel(new NodeProjectGroup(context, ".")); //NOI18N
    this.projectTree.setRootVisible(false);
    this.setViewportView(this.projectTree);

    this.projectTree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(@Nonnull final KeyEvent e) {
        if (!e.isConsumed() && e.getModifiers() == 0 && e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
          final TreePath selectedPath = projectTree.getSelectionPath();
          if (selectedPath != null) {
            final NodeFileOrFolder node = (NodeFileOrFolder) selectedPath.getLastPathComponent();
            if (node != null) {
              if (!node.isLoading() && node.isLeaf()) {
                final File file = node.makeFileForNode();
                if (file != null && !context.openFileAsTab(file, -1)) {
                  UiUtils.openInSystemViewer(file);
                }
              } else {
                projectTree.expandPath(selectedPath);
              }
            }
          }
        }
      }

    });

    this.projectTree.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        if (e.getClickCount() > 1) {
          final int selRow = projectTree.getRowForLocation(e.getX(), e.getY());
          final TreePath selPath = projectTree.getPathForLocation(e.getX(), e.getY());
          if (selRow >= 0) {
            final NodeFileOrFolder node = (NodeFileOrFolder) selPath.getLastPathComponent();
            if (node != null && !node.isLoading() && node.isLeaf()) {
              final File file = node.makeFileForNode();
              if (file != null) {
                if (context.openFileAsTab(file, -1)) {
                  SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                      context.centerRootTopicIfFocusedMMD();
                    }
                  });
                } else {
                  UiUtils.openInSystemViewer(file);
                }
              }
            }
          }
        }
      }

      @Override
      public void mouseReleased(@Nonnull final MouseEvent e) {
        if (e.isPopupTrigger()) {
          processPopup(e);
        }
      }

      @Override
      public void mousePressed(@Nonnull final MouseEvent e) {
        if (e.isPopupTrigger()) {
          processPopup(e);
        }
      }

      private void processPopup(@Nonnull final MouseEvent e) {
        final TreePath selPath = projectTree.getPathForLocation(e.getX(), e.getY());
        if (selPath != null) {
          projectTree.setSelectionPath(selPath);
          final Object last = selPath.getLastPathComponent();
          if (last instanceof NodeFileOrFolder) {
            final NodeFileOrFolder nof = (NodeFileOrFolder) last;
            if (nof.isLoading()) {
              makePopupMenuForLoading(nof).show(e.getComponent(), e.getX(), e.getY());
            } else if (!nof.hasNoAccess()) {
              makePopupMenu(nof).show(e.getComponent(), e.getX(), e.getY());
            }
          }
        }
      }

    });
  }

  @Nonnull
  public JTree getProjectTree() {
    return this.projectTree;
  }

  public boolean hasSelectedItem() {
    return this.projectTree.getSelectionPath() != null;
  }

  @Nullable
  public File getCurrentFocusedFolder() {
    final TreePath path = this.projectTree.getSelectionPath();
    File result = null;
    if (path != null) {
      NodeFileOrFolder selected = (NodeFileOrFolder) path.getLastPathComponent();
      do {
        if (!selected.isLeaf() && !selected.hasNoAccess()) {
          result = selected.makeFileForNode();
          break;
        } else {
          selected = selected.getNodeParent();
        }
      } while (selected != null);
    }
    return result;
  }

  public void showPopUpForSelectedItem() {
    final TreePath path = this.projectTree.getSelectionPath();
    if (path != null) {
      final NodeFileOrFolder component = (NodeFileOrFolder) path.getLastPathComponent();
      final Rectangle rect = this.projectTree.getRowBounds(this.projectTree.getRowForPath(path));
      final JPopupMenu popupMenu = component.isLoading() ? makePopupMenuForLoading(component) : makePopupMenu(component);
      popupMenu.show(this.projectTree, rect.x + rect.width / 2, rect.y + rect.height / 2);
    }
  }

  @Override
  public void requestFocus() {
    this.projectTree.requestFocus();
  }

  @Nonnull
  @MustNotContainNull
  public List<NodeFileOrFolder> findForNamePattern(@Nullable final Pattern namePattern) {
    return getCurrentGroup().findForNamePattern(namePattern);
  }

  @Nonnull
  @MustNotContainNull
  public List<NodeFileOrFolder> findNodesForFile(@Nonnull final File file) {
    return getCurrentGroup().findRelatedNodes(file, new ArrayList<NodeFileOrFolder>());
  }

  public void сloseProject(@Nonnull final NodeProject tree) {
    ((NodeProjectGroup) this.projectTree.getModel()).removeProject(tree);
    this.context.onCloseProject(tree);
  }

  public void focusToFirstElement() {
    this.projectTree.focusToFirstElement();
  }

  public boolean focusToFileItem(@Nonnull final File file) {
    boolean found = false;
    final NodeProjectGroup group = getCurrentGroup();
    final TreePath pathToFile = group.findPathToFile(file);
    if (pathToFile != null) {
      found = true;
      this.projectTree.setSelectionPath(pathToFile);
      this.projectTree.scrollPathToVisible(pathToFile);
    }
    return found;
  }

  public void unfoldProject(@Nonnull final NodeProject node) {
    Utils.safeSwingCall(new Runnable() {
      @Override
      public void run() {
        projectTree.expandPath(new TreePath(new Object[]{getCurrentGroup(), node}));
      }
    });
  }

  @Nonnull
  private JPopupMenu makePopupMenuForLoading(@Nonnull final NodeFileOrFolder node) {
    final JPopupMenu result = new JPopupMenu();

    if (node instanceof NodeProject) {
      final JMenuItem stopAndClose = new JMenuItem("Cancel and remove", CROSS_ICON);
      stopAndClose.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          ((NodeProject) node).cancelLoading();
          сloseProject((NodeProject) node);
        }
      });

      result.add(stopAndClose);
    }

    return result;
  }

  @Nonnull
  private JPopupMenu makePopupMenu(@Nonnull final NodeFileOrFolder node) {
    final JPopupMenu result = new JPopupMenu();

    if (!node.isLeaf()) {
      final JMenu makeNew = new JMenu("New...");

      JMenuItem item = new JMenuItem("Folder");
      item.addActionListener((@Nonnull final ActionEvent e) -> {
        addChildTo(node, null);
      });
      makeNew.add(item);

      item = new JMenuItem("Mind map");
      item.addActionListener((@Nonnull final ActionEvent e) -> {
        addChildTo(node, "mmd"); //NOI18N
      });
      makeNew.add(item);

      item = new JMenuItem("Text");
      item.addActionListener((@Nonnull final ActionEvent e) -> {
        addChildTo(node, "txt"); //NOI18N
      });
      makeNew.add(item);

      item = new JMenuItem("PlantUML");
      item.addActionListener((@Nonnull final ActionEvent e) -> {
        addChildTo(node, "puml"); //NOI18N
      });
      makeNew.add(item);

      item = new JMenuItem("KStreams topology");
      item.addActionListener((@Nonnull final ActionEvent e) -> {
        addChildTo(node, "kstpl"); //NOI18N
      });
      makeNew.add(item);

      item = new JMenuItem("DOT script");
      item.addActionListener((@Nonnull final ActionEvent e) -> {
        addChildTo(node, "gv"); //NOI18N
      });
      makeNew.add(item);

      result.add(makeNew);
    }

    if (!node.isProjectKnowledgeFolder()) {
      final JMenuItem rename = new JMenuItem("Rename");
      rename.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          projectTree.startEditingAtPath(node.makeTreePath());
        }
      });
      result.add(rename);

      if (node.isLeaf()) {
        final JMenuItem doClone = new JMenuItem("Clone");
        doClone.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(@Nonnull final ActionEvent e) {
            try {
              final File file = projectTree.cloneFile(node.makeTreePath());
              if (file != null) {
                addFileIfPossible(file, false);
              }
            } catch (IOException ex) {
              DialogProviderManager.getInstance().getDialogProvider().msgError(SciaRetoStarter.getApplicationFrame(), "Can't make copy '" + node + '\'');
            }
          }
        });
        result.add(doClone);
      }
    }

    if (node instanceof NodeProject) {
      final JMenuItem close = new JMenuItem("Close");
      close.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          if (DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(SciaRetoStarter.getApplicationFrame(), "Close '" + node + '\'', "Do you really want close '" + node + "\'?\nIt will be just removed from the tree.")) {
            сloseProject((NodeProject) node);
          }
        }
      });
      result.add(close);

      final JMenuItem refresh = new JMenuItem("Reload");
      refresh.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          getCurrentGroup().startProjectFolderRefresh((NodeProject) node);
        }
      });
      result.add(refresh);
    }

    final JMenuItem delete = new JMenuItem("Delete");
    delete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        if (DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo(null, "Delete", "Do you really want to delete \"" + node + "\"?")) {
          context.deleteTreeNode(node);
        }
      }
    });
    result.add(delete);

    final JMenuItem openInSystem = new JMenuItem("Open in System");
    openInSystem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {
        final File file = node.makeFileForNode();
        if (file != null && file.exists()) {
          UiUtils.openInSystemViewer(file);
        }
      }
    });
    result.add(openInSystem);

    if (node instanceof NodeProject) {
      final NodeProject theProject = (NodeProject) node;
      if (!theProject.hasKnowledgeFolder()) {
        final File knowledgeFolder = new File(theProject.getFolder(), Context.KNOWLEDGE_FOLDER);
        final JMenuItem addKnowledgeFolder = new JMenuItem("Create Knowledge folder");
        addKnowledgeFolder.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(@Nonnull final ActionEvent e) {
            if (knowledgeFolder.mkdirs()) {
              getCurrentGroup().startProjectFolderRefresh(theProject, new Runnable() {
                @Override
                public void run() {
                  context.focusInTree(knowledgeFolder);
                }
              });
            } else {
              LOGGER.error("Can't create knowledge folder : " + knowledgeFolder); //NOI18N
            }
          }
        });
        result.add(addKnowledgeFolder);
      }
    }

    final String BUILD_GRAPH_ITEM = "Build file links graph";

    if (node instanceof NodeProject) {
      final JMenuItem buildMindMapGraph = new JMenuItem(BUILD_GRAPH_ITEM);
      buildMindMapGraph.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          context.showGraphMindMapFileLinksDialog(((NodeProject) node).getFolder(), null, true);
        }
      });
      result.add(buildMindMapGraph);
    } else if (node.isLeaf() && node.isMindMapFile()) {
      final JMenuItem buildMindMapGraph = new JMenuItem(BUILD_GRAPH_ITEM);
      buildMindMapGraph.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          final NodeProject project = node.findProject();
          context.showGraphMindMapFileLinksDialog(project == null ? null : project.getFolder(), node.makeFileForNode(), true);
        }
      });
      result.add(buildMindMapGraph);
    }

    final List<JMenuItem> optional = new ArrayList<>();

    final JMenuItem menuSearchUsage = new JMenuItem("Find in maps");
    menuSearchUsage.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(@Nonnull final ActionEvent e) {

        if (context.hasUnsavedDocument() && !DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(null, "Detected unsaved documents", "Unsaved content will not be processed!")) {
          return;
        }

        final FindUsagesPanel panel = new FindUsagesPanel(context, node, false);
        if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(null, "Find usages in all opened projects", panel)) {
          final NodeFileOrFolder selected = panel.getSelected();
          panel.dispose();
          if (selected != null) {
            final File file = selected.makeFileForNode();
            if (file != null) {
              context.focusInTree(file);
              SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                  requestFocus();
                }
              });
            }
          }
        } else {
          panel.dispose();
        }
      }
    });
    optional.add(menuSearchUsage);

    if (!node.isLeaf()) {
      final JMenuItem menuSearchFilesForText = new JMenuItem("Find files for text");
      menuSearchFilesForText.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          final FindFilesForTextPanel panel = new FindFilesForTextPanel(context, node, JOptionPane.OK_OPTION);

          if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(null, "Find files for text in '" + node.name + '\'', panel)) {
            final NodeFileOrFolder selected = panel.getSelected();
            panel.dispose();
            if (selected != null) {
              final File file = selected.makeFileForNode();
              if (file != null) {
                context.focusInTree(file);
                SwingUtilities.invokeLater(new Runnable() {
                  @Override
                  public void run() {
                    requestFocus();
                  }
                });
              }
            }
          } else {
            panel.dispose();
          }
        }
      });

      optional.add(menuSearchFilesForText);
    }

    final TabTitle editingTab = this.context.getFocusedTab();
    if (editingTab != null && editingTab.getType() == EditorContentType.MINDMAP) {
      final JMenuItem addIntoMap = new JMenuItem("Add File as topic");
      addIntoMap.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          addTreeAsTopic(context.findProjectForFile(editingTab.getAssociatedFile()), node, ((MMDEditor) editingTab.getProvider().getEditor()));
        }
      });
      optional.add(addIntoMap);
    }

    if (!optional.isEmpty()) {
      result.add(new JSeparator());
      for (final JMenuItem i : optional) {
        result.add(i);
      }
    }

    return result;
  }

  private void addTreeAsTopic(@Nullable final NodeProject project, @Nonnull final NodeFileOrFolder node, @Nonnull final MMDEditor editor) {

    final File projectFolder = project == null ? null : project.getFolder();

    if (project != null && (node.findProject() != project && !DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(null, "Different projects", "Opened Map file from another project. File paths will not be relative ones."))) {
      return;
    }

    final List<Topic> targetTopics = new ArrayList<>(Arrays.asList(editor.getMindMapPanel().getSelectedTopics()));

    if (targetTopics.size() > 1) {
      if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(null, "Multiple selection detected", "New children will be generated for all focused topics.")) {
        return;
      }
    } else {
      if (targetTopics.isEmpty() && editor.getMindMapPanel().getModel().getRoot() != null) {
        if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(null, "No selected parent", "There is not selected topic. The Root will be used as the parent.")) {
          return;
        }
        targetTopics.add(editor.getMindMapPanel().getModel().getRoot());
      }
    }

    editor.getMindMapPanel().executeModelJobs(new MindMapPanel.ModelJob() {
      @Nonnull
      private Topic recursiveGenerateTopics(@Nullable final File projectFolder, @Nonnull final MindMap model, @Nullable final Topic parent, @Nonnull final NodeFileOrFolder node) {
        final ExtraFile fileLink = new ExtraFile(new MMapURI(projectFolder, node.makeFileForNode(), null));
        final Topic theTopic;
        if (parent == null) {
          theTopic = new Topic(model, null, node.toString(), fileLink);
        } else {
          theTopic = parent.makeChild(node.toString(), null);
          theTopic.setExtra(fileLink);
        }

        if (!node.isLeaf()) {
          final Enumeration<NodeFileOrFolder> children = node.children();
          while (children.hasMoreElements()) {
            recursiveGenerateTopics(projectFolder, model, theTopic, children.nextElement());
          }
        }

        return theTopic;
      }

      @Override
      public boolean doChangeModel(@Nonnull final MindMap model) {

        Topic createdTopic = null;
        if (targetTopics.isEmpty()) {
          createdTopic = recursiveGenerateTopics(projectFolder, model, null, node);
        } else {
          boolean first = true;
          for (final Topic t : targetTopics) {
            final Topic generated = recursiveGenerateTopics(projectFolder, model, t, node);
            if (first) {
              createdTopic = generated;
            }
            first = false;
          }
        }

        if (editor.getMindMapPanel().getSelectedTopics().length == 0 && createdTopic != null) {
          final Topic forfocus = createdTopic;
          SwingUtilities.invokeLater(() -> {
              editor.getMindMapPanel().focusTo(forfocus);
          });
        }

        return true;
      }
    });
  }

  private void addChildTo(@Nonnull final NodeFileOrFolder folder, @Nullable final String extension) {
    String fileName = JOptionPane.showInputDialog(SciaRetoStarter.getApplicationFrame(), extension == null ? "Folder name" : "File name", extension == null ? "New folder" : "New " + extension.toUpperCase(Locale.ENGLISH) + " file", JOptionPane.QUESTION_MESSAGE);
    if (fileName != null) {
      fileName = fileName.trim();
      if (NodeProjectGroup.FILE_NAME.matcher(fileName).matches()) {
        if (extension != null) {
          final String providedExtension = FilenameUtils.getExtension(fileName);
          if (!extension.equalsIgnoreCase(providedExtension)) {
            if ("txt".equals(extension)) {
              fileName += providedExtension.isEmpty() ? '.' + extension : "";
            } else {
              fileName += '.' + extension;
            }
          }
        }
        final File file = new File(folder.makeFileForNode(), fileName);
        if (file.exists()) {
          DialogProviderManager.getInstance().getDialogProvider().msgError(null, "File '" + fileName + "' already exists!");
          return;
        }

        boolean ok = false;

        if (extension == null) {
          if (!file.mkdirs()) {
            LOGGER.error("Can't create folder"); //NOI18N
            DialogProviderManager.getInstance().getDialogProvider().msgError(null, "Can't create folder '" + fileName + "'!");
          } else {
            ok = true;
          }
        } else {
          switch (extension) {
            case "mmd": { //NOI18N
              final MindMap model = new MindMap(true);
              model.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_SHOW_JUMPS, "true"); //NOI18N
              model.putAttribute(StandardMmdAttributes.MMD_ATTRIBUTE_GENERATOR_ID, IDEBridgeFactory.findInstance()
                  .getIDEGeneratorId());
              final Topic root = model.getRoot();
              if (root != null) {
                root.setText("Root"); //NOI18N
              }
              try {
                FileUtils.write(file, model.write(new StringWriter()).toString(), StandardCharsets.UTF_8); //NOI18N
                ok = true;
              } catch (IOException ex) {
                LOGGER.error("Can't create MMD file", ex); //NOI18N
                DialogProviderManager.getInstance().getDialogProvider().msgError(null, "Can't create mind map '" + fileName + "'!");
              }
            }
            break;
            case "puml": { //NOI18N
              try {
                FileUtils.write(file, PlantUmlTextEditor.NEW_TEMPLATE, "UTF-8"); //NOI18N
                ok = true;
              } catch (IOException ex) {
                LOGGER.error("Can't create PUML file", ex); //NOI18N
                DialogProviderManager.getInstance().getDialogProvider().msgError(null, "Can't create puml file '" + fileName + "'!");
              }
            }
            break;
            case "kstpl": { //NOI18N
              try {
                FileUtils.write(file, KsTplTextEditor.NEW_TEMPLATE, "UTF-8"); //NOI18N
                ok = true;
              } catch (IOException ex) {
                LOGGER.error("Can't create KSTPL file", ex); //NOI18N
                DialogProviderManager.getInstance().getDialogProvider().msgError(null, "Can't create KSTPL file '" + fileName + "'!");
              }
            }
            break;
            case "txt": { //NOI18N
              try {
                FileUtils.write(file, "", "UTF-8"); //NOI18N
                ok = true;
              } catch (IOException ex) {
                LOGGER.error("Can't create TXT file", ex); //NOI18N
                DialogProviderManager.getInstance().getDialogProvider().msgError(null, "Can't create txt file '" + fileName + "'!");
              }
            }
            break;
            case "gv": { //NOI18N
              try {
                FileUtils.write(file, DotScriptEditor.NEW_TEMPLATE, "UTF-8"); //NOI18N
                ok = true;
              } catch (IOException ex) {
                LOGGER.error("Can't create TXT file", ex); //NOI18N
                DialogProviderManager.getInstance().getDialogProvider().msgError(null, "Can't create txt file '" + fileName + "'!");
              }
            }
            break;
            default:
              throw new Error("Unexpected extension : " + extension); //NOI18N
          }
        }

        if (ok) {
          try {
            addFileIfPossible(file, true);
          } catch (IOException ex) {
            DialogProviderManager.getInstance().getDialogProvider().msgError(SciaRetoStarter.getApplicationFrame(), "Error:" + ex.getMessage());
          }
        }
      } else {
        DialogProviderManager.getInstance().getDialogProvider().msgError(SciaRetoStarter.getApplicationFrame(), "Illegal file name!");
      }
    }
  }

  public boolean deleteNode(@Nonnull final NodeFileOrFolder node) {
    return getCurrentGroup().deleteNode(node, true);
  }

  @Nonnull
  public NodeProjectGroup getCurrentGroup() {
    return (NodeProjectGroup) this.projectTree.getModel();
  }

  public void setModel(@Nonnull final NodeProjectGroup model, final boolean expandFirst) {
    this.projectTree.setModel(Assertions.assertNotNull(model));
    if (expandFirst && model.getChildCount() > 0) {
      this.projectTree.expandPath(new TreePath(new Object[]{model, model.getChildAt(0)}));
    }
  }

  public void addFileIfPossible(@Nonnull final File file, final boolean openAndFocusInTree) throws IOException {
    if (file.exists()) {
      TreePath parentFolderPath = this.projectTree.findTreePathToFolderContains(file);

      if (parentFolderPath == null) {
        parentFolderPath = this.projectTree.tryCreatePathInTreeToFile(file);
      }

      if (parentFolderPath != null) {
        final NodeFileOrFolder folder = (NodeFileOrFolder) parentFolderPath.getLastPathComponent();
        try {
          getCurrentGroup().addChild(folder, PrefUtils.isShowHiddenFilesAndFolders(), file);
          if (openAndFocusInTree) {
            context.openFileAsTab(file, -1);
            context.focusInTree(file);
          }
        } catch (IOException ex) {
          MainFrame.showExceptionDialog(ex);
        }
      }
    }
  }

  @Nullable
  public File findFirstProjectFolder() {
    final NodeProjectGroup group = (NodeProjectGroup) this.projectTree.getModel();
    File result = null;
    if (group.getChildCount() > 0) {
      result = ((NodeFileOrFolder) group.getChild(group, 0)).makeFileForNode();
    }
    return result;
  }

}
