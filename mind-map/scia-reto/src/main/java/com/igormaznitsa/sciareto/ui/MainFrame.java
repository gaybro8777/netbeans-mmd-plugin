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
package com.igormaznitsa.sciareto.ui;

import static com.igormaznitsa.sciareto.ui.UiUtils.assertSwingThread;


import com.igormaznitsa.meta.annotation.MayContainNull;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.meta.annotation.ReturnsOriginal;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.SciaRetoStarter;
import com.igormaznitsa.sciareto.preferences.FileHistoryManager;
import com.igormaznitsa.sciareto.preferences.PrefUtils;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.preferences.PreferencesPanel;
import com.igormaznitsa.sciareto.preferences.SystemFileExtensionManager;
import com.igormaznitsa.sciareto.ui.editors.AbstractEditor;
import com.igormaznitsa.sciareto.ui.editors.AbstractPlUmlEditor;
import com.igormaznitsa.sciareto.ui.editors.DotScriptEditor;
import com.igormaznitsa.sciareto.ui.editors.EditorContentType;
import com.igormaznitsa.sciareto.ui.editors.KsTplTextEditor;
import com.igormaznitsa.sciareto.ui.editors.MMDEditor;
import com.igormaznitsa.sciareto.ui.editors.PictureViewer;
import com.igormaznitsa.sciareto.ui.editors.PlantUmlTextEditor;
import com.igormaznitsa.sciareto.ui.editors.SourceTextEditor;
import com.igormaznitsa.sciareto.ui.editors.TextFileBackuper;
import com.igormaznitsa.sciareto.ui.misc.AboutPanel;
import com.igormaznitsa.sciareto.ui.misc.DonateButton;
import com.igormaznitsa.sciareto.ui.misc.FileLinkGraphPanel;
import com.igormaznitsa.sciareto.ui.misc.GoToFilePanel;
import com.igormaznitsa.sciareto.ui.misc.SplitPaneExt;
import com.igormaznitsa.sciareto.ui.platform.PlatformMenuAction;
import com.igormaznitsa.sciareto.ui.platform.PlatformMenuEvent;
import com.igormaznitsa.sciareto.ui.platform.PlatformProvider;
import com.igormaznitsa.sciareto.ui.tabs.EditorTabPane;
import com.igormaznitsa.sciareto.ui.tabs.TabProvider;
import com.igormaznitsa.sciareto.ui.tabs.TabTitle;
import com.igormaznitsa.sciareto.ui.tree.ExplorerTree;
import com.igormaznitsa.sciareto.ui.tree.NodeFileOrFolder;
import com.igormaznitsa.sciareto.ui.tree.NodeProject;
import com.igormaznitsa.sciareto.ui.tree.NodeProjectGroup;
import com.igormaznitsa.sciareto.ui.tree.ProjectLoadingIconAnimationController;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

import org.apache.commons.io.FilenameUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public final class MainFrame extends javax.swing.JFrame implements Context, PlatformMenuAction {

  private static final long serialVersionUID = 3798040833406256900L;

  public static final Logger LOGGER = LoggerFactory.getLogger(MainFrame.class);

  private static final boolean DELETE_MOVING_FILE_TO_TRASH = true;

  private final EditorTabPane tabPane;
  private final ExplorerTree explorerTree;

  private final boolean stateless;

  private final AtomicReference<Runnable> taskToEndFullScreen = new AtomicReference<>();

  private final JPanel stackPanel;
  private final JPanel mainPanel;

  private final AtomicReference<FindTextPanel> currentFindTextPanel = new AtomicReference<>();

  private final JSplitPane mainSplitPane;

  private int lastDividerLocation;

  public static final Scheduler REACTOR_SCHEDULER = Schedulers
      .newBoundedElastic(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE,
          "sr-reactor", 15, true);

  public MainFrame(@Nullable final GraphicsConfiguration gfc, @Nonnull @MustNotContainNull final String... args) throws IOException {
    super(gfc);
    initComponents();

    if (PlatformProvider.getPlatform().registerPlatformMenuEvent(com.igormaznitsa.sciareto.ui.platform.PlatformMenuEvent.ABOUT, this)) {
      this.menuHelp.setVisible(false);
    }

    if (PlatformProvider.getPlatform().registerPlatformMenuEvent(com.igormaznitsa.sciareto.ui.platform.PlatformMenuEvent.PREFERENCES, this)) {
      this.menuPreferences.setVisible(false);
    }

    if (PlatformProvider.getPlatform().registerPlatformMenuEvent(com.igormaznitsa.sciareto.ui.platform.PlatformMenuEvent.QUIT, this)) {
      this.separatorExitSection.setVisible(false);
      this.menuExit.setVisible(false);
    }

    PlatformProvider.getPlatform().registerPlatformMenuEvent(PlatformMenuEvent.REOPEN_APPLICATION, this);

    this.stackPanel = new JPanel();
    this.stackPanel.setFocusable(false);
    this.stackPanel.setOpaque(false);
    this.stackPanel.setBorder(BorderFactory.createEmptyBorder(32, 32, 16, 32));
    this.stackPanel.setLayout(new BoxLayout(this.stackPanel, BoxLayout.Y_AXIS));

    final JPanel glassPanel = (JPanel) this.getGlassPane();
    glassPanel.setOpaque(false);

    this.setGlassPane(glassPanel);

    glassPanel.setLayout(new BorderLayout(8, 8));
    glassPanel.add(Box.createGlue(), BorderLayout.CENTER);

    final JPanel ppanel = new JPanel(new BorderLayout(0, 0));
    ppanel.setFocusable(false);
    ppanel.setOpaque(false);
    ppanel.setCursor(null);
    ppanel.add(this.stackPanel, BorderLayout.SOUTH);

    glassPanel.add(ppanel, BorderLayout.EAST);

    this.stackPanel.add(Box.createGlue());

    glassPanel.setVisible(false);

    this.setTitle(SciaRetoStarter.APP_TITLE); //NOI18N
    this.setIconImage(SciaRetoStarter.APP_ICON); //NOI18N

    this.stateless = args.length > 0;

    final MainFrame theInstance = this;

    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(@Nonnull final WindowEvent e) {
        if (doClosing()) {
          try {
            for (final TabTitle t : tabPane) {
              t.getProvider().getEditor().deleteBackup();
            }
          } finally {
            try {
              dispose();
            } finally {
              TextFileBackuper.getInstance().finish();
            }
          }
        }
      }
    });

    this.tabPane = new EditorTabPane(this);

    this.explorerTree = new ExplorerTree(this);

    this.mainSplitPane = new SplitPaneExt(JSplitPane.HORIZONTAL_SPLIT);
    this.mainSplitPane.setOneTouchExpandable(true);
    this.mainSplitPane.setDividerLocation(250);
    this.mainSplitPane.setResizeWeight(0.0d);
    this.mainSplitPane.setLeftComponent(this.explorerTree);

    this.tabPane.addMaxMinEditorListener((@Nonnull final ActionEvent e) -> {
      changeVisibilityStateOfTreePanel();
    });

    this.mainPanel = new JPanel(new BorderLayout(0, 0));
    this.mainPanel.add(this.tabPane, BorderLayout.CENTER);

    this.mainSplitPane.setRightComponent(this.mainPanel);

    add(this.mainSplitPane, BorderLayout.CENTER);

    this.menuOpenRecentProject.addMenuListener(new MenuListener() {
      @Override
      public void menuSelected(MenuEvent e) {
        final File[] lastOpenedProjects = FileHistoryManager.getInstance().getLastOpenedProjects();
        if (lastOpenedProjects.length > 0) {
          for (final File folder : lastOpenedProjects) {
            final JMenuItem item = new JMenuItem(folder.getName());
            item.setToolTipText(folder.getAbsolutePath());
            item.addActionListener((ActionEvent e1) -> {
              openProject(folder, false);
            });
            menuOpenRecentProject.add(item);
          }
        }
      }

      @Override
      public void menuDeselected(MenuEvent e) {
        menuOpenRecentProject.removeAll();
      }

      @Override
      public void menuCanceled(MenuEvent e) {
      }
    });

    this.menuOpenRecentFile.addMenuListener(new MenuListener() {
      @Override
      public void menuSelected(MenuEvent e) {
        final File[] lastOpenedFiles = FileHistoryManager.getInstance().getLastOpenedFiles();
        if (lastOpenedFiles.length > 0) {
          for (final File file : lastOpenedFiles) {
            final JMenuItem item = new JMenuItem(file.getName());
            item.setToolTipText(file.getAbsolutePath());
            item.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                openFileAsTab(file, -1);
              }
            });
            menuOpenRecentFile.add(item);
          }
        }
      }

      @Override
      public void menuDeselected(MenuEvent e) {
        menuOpenRecentFile.removeAll();
      }

      @Override
      public void menuCanceled(MenuEvent e) {
      }
    });

    if (!this.stateless) {
      restoreState();
    } else {
      boolean openedProject = false;
      for (final String filePath : args) {
        final File file = new File(filePath);
        if (file.isDirectory()) {
          openedProject = true;
          openProject(file, true);
        } else if (file.isFile()) {
          openFileAsTab(file, -1);
        }
      }
      if (!openedProject) {
        SwingUtilities.invokeLater(() -> {
          this.mainSplitPane.setDividerLocation(0);
        });
      }
    }

    final LookAndFeel current = UIManager.getLookAndFeel();
    final ButtonGroup lfGroup = new ButtonGroup();
    final String currentLFClassName = current.getClass().getName();

    final Consumer<UIManager.LookAndFeelInfo> lfRegistrator = info -> {
      final JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(info.getName());
      lfGroup.add(menuItem);
      if (currentLFClassName.equals(info.getClassName())) {
        menuItem.setSelected(true);
      }
      menuItem.addActionListener((@Nonnull final ActionEvent e) -> {
        try {

          UIManager.setLookAndFeel(info.getClassName());
          SwingUtilities.updateComponentTreeUI(theInstance);
          PreferencesManager.getInstance().getPreferences()
              .put(SciaRetoStarter.PROPERTY_LOOKANDFEEL, info.getClassName());
          PreferencesManager.getInstance().flush();
        } catch (Exception ex) {
          LOGGER.error("Can't change LF", ex); //NOI18N
        }
      });
      this.menuLookAndFeel.add(menuItem);
    };

    final List<UIManager.LookAndFeelInfo> baseLookAndFeels = findBaseLookAndFeels();
    baseLookAndFeels.forEach(lfRegistrator);
    this.menuLookAndFeel.add(new JSeparator());

    Stream.of(UIManager.getInstalledLookAndFeels())
        .filter(x -> !baseLookAndFeels.contains(x))
        .sorted(Comparator.comparing(UIManager.LookAndFeelInfo::getName))
        .forEach(lfRegistrator);
    
    fillScaleUiMenu();
    
    if (SystemUtils.isMac()) {
      this.menuOpenProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())); // ?

      this.menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuSaveAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));

      this.menuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

      this.menuGoToFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
      this.menuUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuFindText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

      this.menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

      this.menuEditShowTreeContextMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
      this.menuFullScreen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.CTRL_MASK));

    } else {

      this.menuOpenProject.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
      this.menuOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuSaveAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
      this.menuExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));

      this.menuGoToFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
      this.menuRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
      this.menuUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuFindText.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

      this.menuEditCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuEditCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
      this.menuEditPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

      this.menuEditShowTreeContextMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
      this.menuFullScreen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0));
    }

    SwingUtilities.invokeLater(() -> {
      if (tabPane.getTabCount() > 0) {
        tabPane.setSelectedIndex(0);
        processTabChanged(tabPane.getCurrentTitle());
      } else {
        processTabChanged(null);
      }
      tabPane.setNotifyForTabChanged(true);
    });

    enableAllMenuItems();

    this.explorerTree.focusToFirstElement();
  }

  private void fillScaleUiMenu() {
    final String selectedScale = UiUtils.loadUiScaleFactor();

    final ButtonGroup scaleButtonGroup = new ButtonGroup();
    Stream.of("None", "1","1.5","2","2.5","3","3.5","4","4.5","5")
            .forEach(scale -> {
                final boolean none = scale.equalsIgnoreCase("none");
                final boolean selected;
                if (selectedScale == null) {
                    selected = none;
                } else {
                    selected = scale.equals(selectedScale);
                }
                final JRadioButtonMenuItem scaleMenuItem = new JRadioButtonMenuItem(none ? scale : "x"+scale, selected);
                scaleMenuItem.addItemListener(x -> {
                    if (x.getStateChange() == ItemEvent.SELECTED) {
                        UiUtils.saveUiScaleFactor(none ? null : scale);
                        LOGGER.info("Set UI scaling factor: " + scale);
                        JOptionPane.showMessageDialog(SciaRetoStarter.getApplicationFrame(), "UI scaling requires application restart!", "Restart required", JOptionPane.WARNING_MESSAGE);
                    }
                });
                scaleButtonGroup.add(scaleMenuItem);
                this.menuViewUIScale.add(scaleMenuItem);
            });
  }
  
  private void ensureTreePanelVisible() {
    final int divider = this.mainSplitPane.getDividerLocation();
    if (divider <= 3) {
      this.mainSplitPane.setDividerLocation(lastDividerLocation);
    }
  }

  private void changeVisibilityStateOfTreePanel() {
    final int divider = this.mainSplitPane.getDividerLocation();
    if (divider > 3) {
      this.lastDividerLocation = divider;
      this.mainSplitPane.setDividerLocation(0);
    } else {
      this.mainSplitPane.setDividerLocation(lastDividerLocation);
    }
  }

  private void updateMenuItemsForProvider(@Nullable final TabProvider provider) {
    if (provider == null) {
      this.menuRedo.setEnabled(false);
      this.menuUndo.setEnabled(false);
      this.menuSave.setEnabled(false);
      this.menuSaveAs.setEnabled(false);
      this.menuEditCopy.setEnabled(false);
      this.menuEditPaste.setEnabled(false);
      this.menuEditCut.setEnabled(false);
      this.menuFindText.setEnabled(false);
    } else {
      if (provider.doesSupportCutCopyPaste()) {
        this.menuEditCopy.setEnabled(provider.isCopyAllowed());
        this.menuEditPaste.setEnabled(provider.isPasteAllowed());
        this.menuEditCut.setEnabled(provider.isCutAllowed());
      } else {
        this.menuEditCopy.setEnabled(false);
        this.menuEditPaste.setEnabled(false);
        this.menuEditCut.setEnabled(false);
      }

      this.menuFindText.setEnabled(provider.doesSupportPatternSearch());

      this.menuRedo.setEnabled(provider.isRedo());
      this.menuUndo.setEnabled(provider.isUndo());

      if (provider.isSaveable()) {
        this.menuSave.setEnabled(provider.getTabTitle().isChanged());
        this.menuSaveAs.setEnabled(true);
      } else {
        this.menuSave.setEnabled(false);
        this.menuSaveAs.setEnabled(false);
      }
    }
  }

  public static void showExceptionDialog(@Nonnull final Exception ex) {
    MainFrame.LOGGER.error("Error", ex);
    Utils.safeSwingBlockingCall(() -> {
      JOptionPane.showMessageDialog(SciaRetoStarter.getApplicationFrame(), "Error during loading : " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    });
  }

  public void processTabChanged(@Nullable final TabTitle title) {
    this.menuSaveAll.setEnabled(this.tabPane.hasEditableAndChangedDocument());

    if (title != null && !this.tabPane.isEmpty() && title.getProvider().doesSupportPatternSearch()) {
      this.menuFindText.setEnabled(true);
      final FindTextPanel findTextPanel = this.currentFindTextPanel.get();
      if (findTextPanel != null) {
        findTextPanel.updateUI(title);
      }
    } else {
      this.menuFindText.setEnabled(false);
    }
    hideFindTextPane();

    enableAllMenuItems();

    if (title != null) {
      title.visited();
    }
  }

  @Override
  public void notifyUpdateRedoUndo() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        processTabChanged(tabPane.getCurrentTitle());
      }
    });
  }

  public JPanel getStackPanel() {
    return this.stackPanel;
  }

  @Override
  public boolean doPlatformMenuAction(@Nonnull final PlatformMenuEvent event, @Nullable @MayContainNull final Object... args) {
    boolean handled = false;
    switch (event) {
      case ABOUT: {
        this.menuAboutActionPerformed(new ActionEvent(this, 0, "about")); //NOI18N
        handled = true;
      }
      break;
      case QUIT: {
        handled = doClosing();
        if (handled) {
          dispose();
        }
      }
      break;
      case REOPEN_APPLICATION: {
        this.setVisible(true);
        handled = true;
      }
      break;
      case PREFERENCES: {
        editPreferences();
        handled = true;
      }
      break;
    }
    return handled;
  }

  private boolean doClosing() {
    endFullScreenIfActive();

    boolean hasUnsaved = false;
    for (final TabTitle t : tabPane) {
      hasUnsaved |= t.isChanged();
    }

    if (hasUnsaved) {
      if (!DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(this, "Detected non-saved documents", "Detected unsaved documents! Close application?")) {
        return false;
      }
    }

    if (!this.stateless) {
      saveState();
    }

    this.getCurrentGroup().cancelLoading();
    REACTOR_SCHEDULER.dispose();

    return true;
  }

  @Override
  public void notifyFileRenamed(@Nullable @MustNotContainNull final List<File> affectedFiles, @Nonnull final File oldFile, @Nonnull final File newFile) {
    this.tabPane.replaceFileLink(oldFile, newFile);
    if (affectedFiles != null) {
      for (final TabTitle t : this.tabPane) {
        final File tabFile = t.getAssociatedFile();
        if (tabFile != null && affectedFiles.contains(tabFile)) {
          t.reload(true);
        }
      }
    }
  }

  @Override
  public boolean safeCloseEditorsForFile(@Nonnull final File file) {
    boolean changed = false;

    final List<TabTitle> list = new ArrayList<>();
    for (final TabTitle t : this.tabPane) {
      if (t.belongFolderOrSame(file)) {
        list.add(t);
        changed |= t.isChanged();
      }
    }

    if (changed && !DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(this, "Confirmation", "Some changed file will be affected! To close them?")) {
      return false;
    }

    closeTab(list.toArray(new TabTitle[list.size()]));

    return true;
  }

  private void restoreState() {
    try {
      final File[] activeProjects = FileHistoryManager.getInstance().getActiveProjects();
      for (final File f : activeProjects) {
        if (f.isDirectory()) {
          openProject(f, true);
        }
      }
      final File[] activeFiles = FileHistoryManager.getInstance().getActiveFiles();
      for (final File f : activeFiles) {
        if (f.isFile()) {
          openFileAsTab(f, -1);
        }
      }

      for (int i = 0; i < this.tabPane.getTabCount(); i++) {
        final TabTitle tab = (TabTitle) this.tabPane.getTabComponentAt(i);
        if (tab.getType() == EditorContentType.PLANTUML || tab.getType() == EditorContentType.KSTPL) {
          SwingUtilities.invokeLater(() -> {
            ((AbstractPlUmlEditor) tab.getProvider()).hideTextPanel();
          });
        }
      }
    } catch (IOException ex) {
      LOGGER.error("Can't restore state", ex); //NOI18N
    }
  }

  private void saveState() {
    try {
      final List<File> files = new ArrayList<>();
      for (final NodeFileOrFolder p : this.explorerTree.getCurrentGroup()) {
        final File f = ((NodeProject) p).getFolder();
        if (f.isDirectory()) {
          files.add(f);
        }
      }
      FileHistoryManager.getInstance().saveActiveProjects(files.toArray(new File[files.size()]));
      files.clear();

      for (final TabTitle p : this.tabPane) {
        final File f = p.getAssociatedFile();
        if (f != null && f.isFile()) {
          files.add(f);
        }
      }
      FileHistoryManager.getInstance().saveActiveFiles(files.toArray(new File[files.size()]));
    } catch (IOException ex) {
      LOGGER.error("Can't save state", ex); //NOI18N
    }
  }

  @Override
  public boolean openFileAsTab(@Nonnull final File file, final int line) {
    boolean result = false;
    if (file.isFile()) {
      if (this.tabPane.focusToFile(file, line)) {
        result = true;
      } else {
        final String nameInLowerCase = file.getName().toLowerCase(Locale.ENGLISH);
        final String ext = FilenameUtils.getExtension(nameInLowerCase);
        if (ext.equals("mmd")) { //NOI18N
          try {
            final MMDEditor panel = new MMDEditor(this, file);
            this.tabPane.createTab(panel);
            result = true;
            centerRootTopicIfFocusedMMD();
          } catch (IOException ex) {
            LOGGER.error("Can't load mind map", ex); //NOI18N
          }
        } else if (PictureViewer.SUPPORTED_FORMATS.contains(ext)) {
          try {
            final PictureViewer panel = new PictureViewer(this, file);
            this.tabPane.createTab(panel);
            result = true;
          } catch (IOException ex) {
            LOGGER.error("Can't load file as image", ex); //NOI18N
          }
        } else if (SourceTextEditor.SUPPORTED_EXTENSIONS.contains(ext) || SourceTextEditor.SUPPORTED_EXTENSIONS.contains("*" + nameInLowerCase)) {
          try {
            final SourceTextEditor panel = new SourceTextEditor(this, file, line, false);
            this.tabPane.createTab(panel);
            result = true;
          } catch (IOException ex) {
            LOGGER.error("Can't load file as sources", ex); //NOI18N
          } finally {
            processTabChange();
          }
        } else if (PlantUmlTextEditor.SUPPORTED_EXTENSIONS.contains(ext)) {
          try {
            final PlantUmlTextEditor panel = new PlantUmlTextEditor(this, file);
            this.tabPane.createTab(panel);
            result = true;
          } catch (IOException ex) {
            LOGGER.error("Can't load file as plant uml text", ex); //NOI18N
          } finally {
            processTabChange();
          }
        } else if (KsTplTextEditor.SUPPORTED_EXTENSIONS.contains(ext)) {
          try {
            final KsTplTextEditor panel = new KsTplTextEditor(this, file);
            this.tabPane.createTab(panel);
            result = true;
          } catch (IOException ex) {
            LOGGER.error("Can't load file as KStream topology text", ex); //NOI18N
          } finally {
            processTabChange();
          }
        } else if (DotScriptEditor.SUPPORTED_EXTENSIONS.contains(ext)) {
          try {
            final DotScriptEditor panel = new DotScriptEditor(this, file);
            this.tabPane.createTab(panel);
            result = true;
          } catch (IOException ex) {
            LOGGER.error("Can't load file as DOT script text", ex); //NOI18N
          } finally {
            processTabChange();
          }
        } else {
          if (SystemFileExtensionManager.getInstance().isSystemFileExtension(FilenameUtils.getExtension(file.getName()))) {
            LOGGER.info("Exension of file " + file.getName() + " among extensions to be opened in system browser");
            result = false;
          } else {
            if (file.length() >= (2L * 1024L * 1024L) && !DialogProviderManager.getInstance().getDialogProvider().msgConfirmYesNo(SciaRetoStarter.getApplicationFrame(), "Very big file", "It is a very big file! Are you sure to open it?")) {
              return true;
            }

            try {
              final SourceTextEditor panel = new SourceTextEditor(this, file, line, true);
              this.tabPane.createTab(panel);
              result = true;
            } catch (IOException ex) {
              LOGGER.error("Can't load file as text", ex); //NOI18N
            } finally {
              processTabChange();
            }
          }
        }
      }
    }
    if (result) {
      try {
        FileHistoryManager.getInstance().registerOpenedFile(file);
      } catch (IOException x) {
        LOGGER.error("Can't register last opened file", x); //NOI18N
      } finally {
        this.tabPane.focusToFile(file, -1);
      }
    }
    return result;
  }

  @Override
  public boolean focusInTree(@Nonnull final TabTitle title) {
    boolean result = false;
    final File file = title.getAssociatedFile();
    if (file != null) {
      result = this.explorerTree.focusToFileItem(file);
    }
    return result;
  }

  @Override
  public boolean focusInTree(@Nonnull final File file) {
    return this.explorerTree.focusToFileItem(file);
  }

  @Override
  public void closeTab(@Nonnull final TabTitle... titles) {
    for (final TabTitle t : titles) {
      this.tabPane.removeTab(t);
    }
    processTabChange();
  }

  private void processTabChange() {
    this.menuFindText.setEnabled(!this.tabPane.isEmpty() && this.tabPane.getCurrentTitle().getProvider().doesSupportPatternSearch());

    if (this.tabPane.isEmpty() || !this.tabPane.getCurrentTitle().getProvider().doesSupportPatternSearch()) {
      hideFindTextPane();
    }

    if (this.tabPane.getTabCount() == 0) {
      this.ensureTreePanelVisible();
    }
  }

  @Override
  public void showFindTextPane(@Nullable final String text) {
    final TabTitle current = getFocusedTab();
    if (current != null && this.tabPane.getCurrentTitle().getProvider().doesSupportPatternSearch()) {

      FindTextPanel panel = this.currentFindTextPanel.get();
      if (panel == null) {
        panel = new FindTextPanel(this, text);
        panel.updateUI(current);
      }
      this.currentFindTextPanel.set(panel);

      final Container currentParent = panel.getParent();
      if (currentParent != null) {
        currentParent.remove(panel);
        currentParent.revalidate();
        currentParent.repaint();
      }

      final boolean mmdEditor = current.getProvider().getEditor().getEditorContentType() == EditorContentType.MINDMAP;
      
      panel.setEnableSearchFile(mmdEditor);
      panel.setEnableSearchNote(mmdEditor);
      panel.setEnableSearchURI(mmdEditor);
      panel.setEnableSearchTopicText(mmdEditor);
      
      if (!this.tabPane.getCurrentTitle().getProvider().showSearchPane(panel)) {
        this.mainPanel.add(panel, BorderLayout.SOUTH);
      }

      this.mainPanel.revalidate();
      this.mainPanel.repaint();

      panel.requestFocus();
    }
  }

  @Override
  public void hideFindTextPane() {
    final FindTextPanel panel = this.currentFindTextPanel.getAndSet(null);
    if (panel != null) {
      final Container parent = panel.getParent();
      if (parent != null) {
        parent.remove(panel);
        parent.revalidate();
        parent.repaint();
      }
    }
  }

  @Override
  @Nullable
  public NodeProject findProjectForFile(@Nonnull final File file) {
    return this.explorerTree.getCurrentGroup().findProjectForFile(file);
  }

  @Override
  public void notifyReloadConfig() {
    for (final TabTitle t : this.tabPane) {
      final AbstractEditor editor = t.getProvider().getEditor();
        editor.doUpdateConfiguration();
    }
  }

  @Override
  public void onCloseProject(@Nonnull final NodeProject project) {
    final File projectFolder = project.getFolder();
    for (final TabTitle t : this.tabPane) {
      if (t.belongFolderOrSame(projectFolder)) {
        t.doSafeClose();
      }
    }
  }

  @Override
  public boolean deleteTreeNode(@Nonnull final NodeFileOrFolder node) {
    final File file = node.makeFileForNode();

    if (file != null && file.exists()) {
      final List<TabTitle> tabsToClose = this.tabPane.findListOfRelatedTabs(file);
      boolean hasUnsaved = false;

      for (final TabTitle t : tabsToClose) {
        hasUnsaved |= t.isChanged();
      }

      if (hasUnsaved && !DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel(this, "Confirmation", "Are you sure to delete changed unsaved file?")) {
        return false;
      }

      closeTab(tabsToClose.toArray(new TabTitle[tabsToClose.size()]));

      final NodeProject project = findProjectForFile(file);

      List<File> affectedFiles = project == null ? Collections.EMPTY_LIST : project.findAffectedFiles(file);

      final Iterator<File> iterator = affectedFiles.iterator();
      final Path removingFile = file.toPath();
      while (iterator.hasNext()) {
        if (iterator.next().toPath().startsWith(removingFile)) {
          iterator.remove();
        }
      }

      if (!affectedFiles.isEmpty()) {
        affectedFiles = UiUtils.showSelectAffectedFiles(affectedFiles);
        if (affectedFiles == null) {
          return false;
        }
      }

      boolean ok = false;
      if (file.isDirectory()) {
        if (SystemUtils.deleteFile(file, DELETE_MOVING_FILE_TO_TRASH)) {
          ok = true;
        } else {
          DialogProviderManager.getInstance().getDialogProvider().msgError(this, "Can't delete directory, see the log!");
        }
      } else {
        ok = SystemUtils.deleteFile(file, DELETE_MOVING_FILE_TO_TRASH);
        if (!ok) {
          DialogProviderManager.getInstance().getDialogProvider().msgError(this, "Can't delete file!");
        }
      }

      if (ok) {
        explorerTree.deleteNode(node);
      }

      if (!affectedFiles.isEmpty() && project != null) {
        final List<File> changedFiles = project.deleteAllLinksToFile(affectedFiles, file);
        if (!changedFiles.isEmpty()) {
          for (final TabTitle t : tabPane) {
            final File associated = t.getAssociatedFile();
            if (associated != null && changedFiles.contains(associated)) {
              t.reload(true);
            }
          }
        }
      }

      return ok;
    }
    return false;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainMenu = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        menuNewProject = new javax.swing.JMenuItem();
        menuNewFile = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        menuOpenProject = new javax.swing.JMenuItem();
        menuOpenRecentProject = new javax.swing.JMenu();
        menuOpenFile = new javax.swing.JMenuItem();
        menuOpenRecentFile = new javax.swing.JMenu();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        menuSave = new javax.swing.JMenuItem();
        menuSaveAs = new javax.swing.JMenuItem();
        menuSaveAll = new javax.swing.JMenuItem();
        separatorExitSection = new javax.swing.JPopupMenu.Separator();
        menuExit = new javax.swing.JMenuItem();
        menuEdit = new javax.swing.JMenu();
        menuEditShowTreeContextMenu = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JPopupMenu.Separator();
        menuUndo = new javax.swing.JMenuItem();
        menuRedo = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuEditCut = new javax.swing.JMenuItem();
        menuEditCopy = new javax.swing.JMenuItem();
        menuEditPaste = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        menuFindText = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        menuPreferences = new javax.swing.JMenuItem();
        menuView = new javax.swing.JMenu();
        menuViewUIScale = new javax.swing.JMenu();
        menuFullScreen = new javax.swing.JMenuItem();
        menuLookAndFeel = new javax.swing.JMenu();
        menuViewZoom = new javax.swing.JMenu();
        menuViewZoomIn = new javax.swing.JMenuItem();
        menuViewZoomOut = new javax.swing.JMenuItem();
        menuViewZoomReset = new javax.swing.JMenuItem();
        menuNavigate = new javax.swing.JMenu();
        menuGoToFile = new javax.swing.JMenuItem();
        menuNavigateLinksGraph = new javax.swing.JMenuItem();
        menuHelp = new javax.swing.JMenu();
        menuHelpHelp = new javax.swing.JMenuItem();
        menuHelpPLantUmpManual = new javax.swing.JMenuItem();
        menuAbout = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        menuMakeDonation = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationByPlatform(true);

        menuFile.setMnemonic('f');
        menuFile.setText("File");
        menuFile.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
                menuFileMenuCanceled(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
                menuFileMenuDeselected(evt);
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuFileMenuSelected(evt);
            }
        });

        menuNewProject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/box_closed.png"))); // NOI18N
        menuNewProject.setMnemonic('w');
        menuNewProject.setText("New Project");
        menuNewProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNewProjectActionPerformed(evt);
            }
        });
        menuFile.add(menuNewProject);

        menuNewFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/document16.png"))); // NOI18N
        menuNewFile.setText("New File");
        menuNewFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNewFileActionPerformed(evt);
            }
        });
        menuFile.add(menuNewFile);
        menuFile.add(jSeparator2);

        menuOpenProject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/open_folder.png"))); // NOI18N
        menuOpenProject.setMnemonic('e');
        menuOpenProject.setText("Open Project");
        menuOpenProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenProjectActionPerformed(evt);
            }
        });
        menuFile.add(menuOpenProject);

        menuOpenRecentProject.setMnemonic('j');
        menuOpenRecentProject.setText("Open Recent Project");
        menuFile.add(menuOpenRecentProject);

        menuOpenFile.setMnemonic('o');
        menuOpenFile.setText("Open File");
        menuOpenFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenFileActionPerformed(evt);
            }
        });
        menuFile.add(menuOpenFile);

        menuOpenRecentFile.setMnemonic('f');
        menuOpenRecentFile.setText("Open Recent File");
        menuFile.add(menuOpenRecentFile);
        menuFile.add(jSeparator3);

        menuSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/diskette.png"))); // NOI18N
        menuSave.setMnemonic('s');
        menuSave.setText("Save");
        menuSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveActionPerformed(evt);
            }
        });
        menuFile.add(menuSave);

        menuSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/file_save_as.png"))); // NOI18N
        menuSaveAs.setMnemonic('v');
        menuSaveAs.setText("Save As");
        menuSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveAsActionPerformed(evt);
            }
        });
        menuFile.add(menuSaveAs);

        menuSaveAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/disk_multiple.png"))); // NOI18N
        menuSaveAll.setMnemonic('a');
        menuSaveAll.setText("Save All");
        menuSaveAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuSaveAllActionPerformed(evt);
            }
        });
        menuFile.add(menuSaveAll);
        menuFile.add(separatorExitSection);

        menuExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/door_in.png"))); // NOI18N
        menuExit.setMnemonic('x');
        menuExit.setText("Exit");
        menuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuExitActionPerformed(evt);
            }
        });
        menuFile.add(menuExit);

        mainMenu.add(menuFile);

        menuEdit.setMnemonic('e');
        menuEdit.setText("Edit");
        menuEdit.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
                menuEditMenuCanceled(evt);
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
                menuEditMenuDeselected(evt);
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuEditMenuSelected(evt);
            }
        });

        menuEditShowTreeContextMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tree_list16.png"))); // NOI18N
        menuEditShowTreeContextMenu.setMnemonic('t');
        menuEditShowTreeContextMenu.setText("Show Tree context menu");
        menuEditShowTreeContextMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditShowTreeContextMenuActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditShowTreeContextMenu);
        menuEdit.add(jSeparator7);

        menuUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/undo.png"))); // NOI18N
        menuUndo.setMnemonic('u');
        menuUndo.setText("Undo");
        menuUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuUndoActionPerformed(evt);
            }
        });
        menuEdit.add(menuUndo);

        menuRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/redo.png"))); // NOI18N
        menuRedo.setMnemonic('r');
        menuRedo.setText("Redo");
        menuRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuRedoActionPerformed(evt);
            }
        });
        menuEdit.add(menuRedo);
        menuEdit.add(jSeparator1);

        menuEditCut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/cut16.png"))); // NOI18N
        menuEditCut.setMnemonic('t');
        menuEditCut.setText("Cut");
        menuEditCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditCutActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditCut);

        menuEditCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/page_copy16.png"))); // NOI18N
        menuEditCopy.setMnemonic('y');
        menuEditCopy.setText("Copy"); // NOI18N
        menuEditCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditCopyActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditCopy);

        menuEditPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/paste_plain16.png"))); // NOI18N
        menuEditPaste.setMnemonic('p');
        menuEditPaste.setText("Paste"); // NOI18N
        menuEditPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditPasteActionPerformed(evt);
            }
        });
        menuEdit.add(menuEditPaste);
        menuEdit.add(jSeparator6);

        menuFindText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/find16.png"))); // NOI18N
        menuFindText.setMnemonic('n');
        menuFindText.setText("Find text");
        menuFindText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFindTextActionPerformed(evt);
            }
        });
        menuEdit.add(menuFindText);
        menuEdit.add(jSeparator5);

        menuPreferences.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/setting_tools.png"))); // NOI18N
        menuPreferences.setMnemonic('e');
        menuPreferences.setText("Preferences");
        menuPreferences.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuPreferencesActionPerformed(evt);
            }
        });
        menuEdit.add(menuPreferences);

        mainMenu.add(menuEdit);

        menuView.setMnemonic('v');
        menuView.setText("View");
        menuView.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuViewMenuSelected(evt);
            }
        });

        menuViewUIScale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/scale_image.png"))); // NOI18N
        menuViewUIScale.setText("UI scale");
        menuView.add(menuViewUIScale);

        menuFullScreen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/monitor.png"))); // NOI18N
        menuFullScreen.setText("Full screen");
        menuFullScreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuFullScreenActionPerformed(evt);
            }
        });
        menuView.add(menuFullScreen);

        menuLookAndFeel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/application.png"))); // NOI18N
        menuLookAndFeel.setText("Look and Feel");
        menuView.add(menuLookAndFeel);

        menuViewZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/zoom.png"))); // NOI18N
        menuViewZoom.setText("Zoom");

        menuViewZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/zoom_in.png"))); // NOI18N
        menuViewZoomIn.setText("In");
        menuViewZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuViewZoomInActionPerformed(evt);
            }
        });
        menuViewZoom.add(menuViewZoomIn);

        menuViewZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/zoom_out.png"))); // NOI18N
        menuViewZoomOut.setText("Out");
        menuViewZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuViewZoomOutActionPerformed(evt);
            }
        });
        menuViewZoom.add(menuViewZoomOut);

        menuViewZoomReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/zoom_actual.png"))); // NOI18N
        menuViewZoomReset.setText("Reset");
        menuViewZoomReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuViewZoomResetActionPerformed(evt);
            }
        });
        menuViewZoom.add(menuViewZoomReset);

        menuView.add(menuViewZoom);

        mainMenu.add(menuView);

        menuNavigate.setMnemonic('n');
        menuNavigate.setText("Navigate");
        menuNavigate.addMenuListener(new javax.swing.event.MenuListener() {
            public void menuCanceled(javax.swing.event.MenuEvent evt) {
            }
            public void menuDeselected(javax.swing.event.MenuEvent evt) {
            }
            public void menuSelected(javax.swing.event.MenuEvent evt) {
                menuNavigateMenuSelected(evt);
            }
        });

        menuGoToFile.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/rocket.png"))); // NOI18N
        menuGoToFile.setMnemonic('f');
        menuGoToFile.setText("Go to File");
        menuGoToFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuGoToFileActionPerformed(evt);
            }
        });
        menuNavigate.add(menuGoToFile);

        menuNavigateLinksGraph.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/graph16.png"))); // NOI18N
        menuNavigateLinksGraph.setText("Build File links graph");
        menuNavigateLinksGraph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNavigateLinksGraphActionPerformed(evt);
            }
        });
        menuNavigate.add(menuNavigateLinksGraph);

        mainMenu.add(menuNavigate);

        menuHelp.setMnemonic('h');
        menuHelp.setText("Help");

        menuHelpHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/help.png"))); // NOI18N
        menuHelpHelp.setText("Help");
        menuHelpHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuHelpHelpActionPerformed(evt);
            }
        });
        menuHelp.add(menuHelpHelp);

        menuHelpPLantUmpManual.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/factory.png"))); // NOI18N
        menuHelpPLantUmpManual.setText("PlantUML manual");
        menuHelpPLantUmpManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuHelpPLantUmpManualActionPerformed(evt);
            }
        });
        menuHelp.add(menuHelpPLantUmpManual);

        menuAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/menu_icons/information.png"))); // NOI18N
        menuAbout.setMnemonic('a');
        menuAbout.setText("About");
        menuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuAboutActionPerformed(evt);
            }
        });
        menuHelp.add(menuAbout);
        menuHelp.add(jSeparator4);

        menuMakeDonation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/coins_in_hand16.png"))); // NOI18N
        menuMakeDonation.setMnemonic('m');
        menuMakeDonation.setText("Make donation");
        menuMakeDonation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMakeDonationActionPerformed(evt);
            }
        });
        menuHelp.add(menuMakeDonation);

        mainMenu.add(menuHelp);

        setJMenuBar(mainMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void menuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuAboutActionPerformed
    JOptionPane.showMessageDialog(SciaRetoStarter.getApplicationFrame(), new AboutPanel(), "About", JOptionPane.PLAIN_MESSAGE);
  }//GEN-LAST:event_menuAboutActionPerformed

  private void menuOpenProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenProjectActionPerformed
    final JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileView(new FileView() {
      private Icon KNOWLEDGE_FOLDER_ICO = null;
      private Icon OTHER_FOLDER_ICO = null;

      @Override
      public Icon getIcon(final File f) {
        final Icon result;
        if (f.isDirectory()) {
          final File knowledge = new File(f, Context.KNOWLEDGE_FOLDER);
          if (knowledge.isDirectory()) {
            if (KNOWLEDGE_FOLDER_ICO == null) {
              Icon superIcon = super.getIcon(f);
              if (superIcon == null || superIcon instanceof ImageIcon) {
                superIcon = superIcon == null ? UIManager.getIcon("FileView.directoryIcon") : superIcon;
                KNOWLEDGE_FOLDER_ICO = new ImageIcon(UiUtils.makeBadgedRightBottom(UiUtils.iconToImage(fileChooser, superIcon), Icons.MMDBADGE.getIcon().getImage()));
                result = KNOWLEDGE_FOLDER_ICO;
              } else {
                result = superIcon;
              }
            } else {
              result = KNOWLEDGE_FOLDER_ICO;
            }
          } else {
            if (OTHER_FOLDER_ICO == null) {
              Icon superIcon = super.getIcon(f);
              if (superIcon == null || superIcon instanceof ImageIcon) {
                OTHER_FOLDER_ICO = superIcon == null ? UIManager.getIcon("FileView.directoryIcon") : superIcon;
                result = OTHER_FOLDER_ICO;
              } else {
                result = superIcon;
              }
            } else {
              result = OTHER_FOLDER_ICO;
            }
          }
          return result;
        } else if (f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".mmd")) { //NOI18N
          return Icons.DOCUMENT.getIcon();
        } else {
          return super.getIcon(f);
        }
      }
    });
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fileChooser.setMultiSelectionEnabled(false);
    fileChooser.setDialogTitle("Open project folder");

    if (fileChooser.showOpenDialog(SciaRetoStarter.getApplicationFrame()) == JFileChooser.APPROVE_OPTION) {
      final File choosenFile = fileChooser.getSelectedFile();
      if (!focusInTree(choosenFile)) {
        openProject(fileChooser.getSelectedFile(), false);
      }
    }
  }//GEN-LAST:event_menuOpenProjectActionPerformed

  @Nonnull
  @ReturnsOriginal
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public NodeProject asyncReloadProject(@Nonnull final NodeProject project, @Nullable final Runnable... invokeLater) {
    assertSwingThread();

    LOGGER.info("Starting asyncronous loading of " + project.toString());

    project.initLoading(Mono.just(project)
            .map(proj -> {
              SwingUtilities.invokeLater(() -> ProjectLoadingIconAnimationController.getInstance().registerLoadingProject(this.explorerTree.getProjectTree(), proj));
              return proj;
            })
            .flatMap(proj -> proj.readSubtree(PrefUtils.isShowHiddenFilesAndFolders()))
            .doOnError(error -> {
              LOGGER.error("Can't open project", error);
              SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(rootPane, "Can't open project : " + error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
              });
            })
            .doOnTerminate(() -> {
              ProjectLoadingIconAnimationController.getInstance().unregisterLoadingProject(project);
              for (final Runnable r : invokeLater) {
                SwingUtilities.invokeLater(r);
              }
            })
            .subscribeOn(REACTOR_SCHEDULER)
            .subscribe());
    return project;
  }

  @Override
  public boolean openProject(@Nonnull final File folder, final boolean enforceSeparatedProject) {
    boolean result = false;
    if (folder.isDirectory()) {
      final NodeProject alreadyOpened = findProjectForFile(folder);
      if (alreadyOpened == null || enforceSeparatedProject) {
        final boolean unfoldFirstProject = this.explorerTree.getCurrentGroup().getChildCount() == 0;

        final NodeProject node;
        try {
          node = asyncReloadProject(this.explorerTree.getCurrentGroup().addProjectFolder(folder), () -> {
              SwingUtilities.invokeLater(()->{
                  if (unfoldFirstProject) {
                      final NodeProject opened = (NodeProject)this.explorerTree.getCurrentGroup().getChildAt(0);
                      this.explorerTree.unfoldProject(opened);
                      this.explorerTree.focusToFirstElement();
                  }                  
              });
          });
        } catch (final IOException ex) {
          JOptionPane.showMessageDialog(this.rootPane, "Can't open project : " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
          LOGGER.error("Can't open project", ex);
          return false;
        }

        try {
          FileHistoryManager.getInstance().registerOpenedProject(folder);
        } catch (IOException ex) {
          LOGGER.error("Can't register last opened project", ex); //NOI18N
        }
      } else {
        this.focusInTree(folder);
      }
      result = true;
    } else {
      LOGGER.error("Can't find folder : " + folder); //NOI18N
      DialogProviderManager.getInstance().getDialogProvider().msgError(this, "Can't find project folder!");
    }
    return result;
  }

  @Override
  public void editPreferences() {
    final PreferencesPanel preferencesPanel = new PreferencesPanel(this);
    preferencesPanel.load(PreferencesManager.getInstance().getPreferences());
    preferencesPanel.doLayout();
    final JScrollPane preferenceScrollPane = new JScrollPane(preferencesPanel);
    preferenceScrollPane.doLayout();
    final Dimension prefSize = new Dimension(preferenceScrollPane.getPreferredSize().width, preferenceScrollPane.getPreferredSize().height);
    
    final int possibleScrollBarSpace = 48;
    prefSize.width += possibleScrollBarSpace;
    prefSize.height += possibleScrollBarSpace;
    final Rectangle graphicsBounds = this.getGraphicsConfiguration().getBounds();

    preferenceScrollPane.setPreferredSize(new Dimension(
        Math.min(prefSize.width, (graphicsBounds.width << 1) / 3), 
        Math.min(prefSize.height, (graphicsBounds.height << 1) / 3)
    ));

    if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(this, "Preferences", preferenceScrollPane)) {
      preferencesPanel.save();
      for (final TabTitle t : this.tabPane) {
        t.getProvider().updateConfiguration();
      }
    }
  }

  private void menuSaveAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveAllActionPerformed
    for (final TabTitle t : this.tabPane) {
      try {
        t.save();
      } catch (IOException ex) {
        LOGGER.error("Can't save file", ex); //NOI18N
        DialogProviderManager.getInstance().getDialogProvider().msgError(this, "Can't save document, may be it is read-only! See log!");
      }
    }
  }//GEN-LAST:event_menuSaveAllActionPerformed

  private void menuPreferencesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuPreferencesActionPerformed
    editPreferences();
  }//GEN-LAST:event_menuPreferencesActionPerformed

  private void menuOpenFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenFileActionPerformed
    final File file = DialogProviderManager.getInstance()
            .getDialogProvider()
            .msgOpenFileDialog(null, null, "open-file", "Open file", null, true, new FileFilter[]{
      MMDEditor.MMD_FILE_FILTER,
      PlantUmlTextEditor.SRC_FILE_FILTER,
      KsTplTextEditor.SRC_FILE_FILTER,
      SourceTextEditor.SRC_FILE_FILTER
    }, "Open");
    if (file != null) {
      if (openFileAsTab(file, -1)) {
        try {
          FileHistoryManager.getInstance().registerOpenedProject(file);
        } catch (IOException ex) {
          LOGGER.error("Can't register last opened file", ex); //NOI18N
        }
      }
    }
  }//GEN-LAST:event_menuOpenFileActionPerformed

  @Override
  public boolean centerRootTopicIfFocusedMMD() {
    boolean result = false;
    final TabTitle title = this.getFocusedTab();
    if (title != null && title.getProvider().getEditor().getEditorContentType() == EditorContentType.MINDMAP) {
      SwingUtilities.invokeLater(() -> {
        ((MMDEditor) title.getProvider().getEditor()).rootToCentre();
      });

      result = true;
    }
    return result;
  }

  private void menuSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveActionPerformed
    final int index = this.tabPane.getSelectedIndex();
    if (index >= 0) {
      try {
        ((TabTitle) this.tabPane.getTabComponentAt(index)).save();
      } catch (IOException ex) {
        LOGGER.error("Can't save file", ex); //NOI18N
        DialogProviderManager.getInstance().getDialogProvider().msgError(this, "Can't save document, may be it is read-only! See log!");
      }
    }
  }//GEN-LAST:event_menuSaveActionPerformed

  private void menuSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuSaveAsActionPerformed
    final int index = this.tabPane.getSelectedIndex();
    if (index >= 0) {
      try {
        ((TabTitle) this.tabPane.getTabComponentAt(index)).saveAs();
      } catch (IOException ex) {
        LOGGER.error("Can't save file", ex); //NOI18N
        DialogProviderManager.getInstance().getDialogProvider().msgError(this, "Can't save document, may be it is read-only! See log!");
      }
    }
  }//GEN-LAST:event_menuSaveAsActionPerformed

  private void menuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuExitActionPerformed
    if (doClosing()) {
      dispose();
    }
  }//GEN-LAST:event_menuExitActionPerformed

  private boolean createKnowledgeFolder(@Nonnull final File folder) {
    boolean result = false;
    if (PreferencesManager.getInstance().getPreferences().getBoolean(PreferencesPanel.PREFERENCE_KEY_KNOWLEDGEFOLDER_ALLOWED, false)) {
      final File knowledgeFolder = new File(folder, Context.KNOWLEDGE_FOLDER);
      if (knowledgeFolder.mkdirs()) {
        result = true;
      } else {
        LOGGER.warn("Can't create folder : " + Context.KNOWLEDGE_FOLDER); //NOI18N
      }
    }
    return result;
  }

  private boolean prepareAndOpenProjectFolder(@Nonnull final File folder) {
    boolean result = false;
    createKnowledgeFolder(folder);
    if (openProject(folder, true)) {
      result = true;
      this.focusInTree(folder);
    }
    return result;
  }

  private void menuNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewProjectActionPerformed
    final JFileChooser folderChooser = new JFileChooser();
    folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    folderChooser.setDialogTitle("Create project folder");
    folderChooser.setDialogType(JFileChooser.SAVE_DIALOG);
    folderChooser.setApproveButtonText("Create");
    folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    if (folderChooser.showSaveDialog(SciaRetoStarter.getApplicationFrame()) == JFileChooser.APPROVE_OPTION) {
      final File file = folderChooser.getSelectedFile();
      if (file.isDirectory()) {
        final String [] files = file.list();
        if (files == null) {
          LOGGER.error("Can't create folder : " + file); //NOI18N
          DialogProviderManager.getInstance().getDialogProvider().msgError(this, "Error during folder process: " + file);
          return;
        }
        if (files.length > 0) {
          DialogProviderManager.getInstance().getDialogProvider().msgError(this, "File '" + file.getName() + "' already exists and non-empty!");
        } else {
          prepareAndOpenProjectFolder(file);
        }
      } else if (file.mkdirs()) {
        prepareAndOpenProjectFolder(file);
      } else {
        LOGGER.error("Can't create folder : " + file); //NOI18N
        DialogProviderManager.getInstance().getDialogProvider().msgError(this, "Can't create folder: " + file);
      }
    }
  }//GEN-LAST:event_menuNewProjectActionPerformed

  private void menuFullScreenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFullScreenActionPerformed

    final AbstractEditor selectedEditor = this.tabPane.getCurrentEditor();
    if (selectedEditor != null) {
      final GraphicsConfiguration gconfig = this.getGraphicsConfiguration();
      if (gconfig != null) {
        final GraphicsDevice device = gconfig.getDevice();
        if (device.isFullScreenSupported()) {
          if (device.getFullScreenWindow() == null) {
            final JLabel label = new JLabel("Opened in full screen");
            final int tabIndex = this.tabPane.getSelectedIndex();
            this.tabPane.setComponentAt(tabIndex, label);
            final JWindow window = new JWindow(SciaRetoStarter.getApplicationFrame());
            window.setAlwaysOnTop(true);
            window.setAutoRequestFocus(true);
            window.setContentPane(selectedEditor.getContainerToShow());

            endFullScreenIfActive();

            final KeyEventDispatcher fullScreenEscCatcher = (@Nonnull final KeyEvent e) -> {
              if (e.getID() == KeyEvent.KEY_PRESSED && (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_F11)) {
                endFullScreenIfActive();
                return true;
              }
              return false;
            };

            if (this.taskToEndFullScreen.compareAndSet(null, (Runnable) () -> {
              try {
                window.dispose();
              } finally {
                tabPane.setComponentAt(tabIndex, selectedEditor.getContainerToShow());
                device.setFullScreenWindow(null);
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(fullScreenEscCatcher);
              }
            })) {
              try {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(fullScreenEscCatcher);
                device.setFullScreenWindow(window);
              } catch (Exception ex) {
                LOGGER.error("Can't turn on full screen", ex); //NOI18N
                endFullScreenIfActive();
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(fullScreenEscCatcher);
              }
            } else {
              LOGGER.error("Unexpected state, processor is not null!"); //NOI18N
            }
          } else {
            LOGGER.warn("Attempt to full screen device which already in full screen!"); //NOI18N
          }
        } else {
          LOGGER.warn("Device doesn's support full screen"); //NOI18N
          DialogProviderManager.getInstance().getDialogProvider().msgWarn(this, "The Device doesn't support full-screen mode!");
        }
      } else {
        LOGGER.warn("Can't find graphics config for the frame"); //NOI18N
      }
    }
  }//GEN-LAST:event_menuFullScreenActionPerformed

  private void menuMakeDonationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMakeDonationActionPerformed
    new DonateButton().doClick();
  }//GEN-LAST:event_menuMakeDonationActionPerformed

  private void menuGoToFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuGoToFileActionPerformed
    final GoToFilePanel panel = new GoToFilePanel(this.explorerTree, JOptionPane.OK_OPTION);
    if (DialogProviderManager.getInstance().getDialogProvider().msgOkCancel(null, "Go To File", panel)) {
      final NodeFileOrFolder selected = panel.getSelected();
      if (selected != null) {
        final File file = selected.makeFileForNode();
        if (file != null) {
          this.focusInTree(file);
          ensureTreePanelVisible();
          SwingUtilities.invokeLater(() -> {
            explorerTree.requestFocus();
          });
        }
      }
    }
  }//GEN-LAST:event_menuGoToFileActionPerformed

  private void menuUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuUndoActionPerformed
    final TabTitle title = this.getFocusedTab();
    if (title != null) {
      this.menuUndo.setEnabled(title.getProvider().undo());
      this.menuRedo.setEnabled(title.getProvider().isRedo());
    }
  }//GEN-LAST:event_menuUndoActionPerformed

  private void menuRedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuRedoActionPerformed
    final TabTitle title = this.getFocusedTab();
    if (title != null) {
      this.menuRedo.setEnabled(title.getProvider().redo());
      this.menuUndo.setEnabled(title.getProvider().isUndo());
    }
  }//GEN-LAST:event_menuRedoActionPerformed

  private void menuFindTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFindTextActionPerformed
    showFindTextPane(""); //NOI18N
  }//GEN-LAST:event_menuFindTextActionPerformed

  private void menuEditMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuEditMenuSelected
    final TabTitle title = this.getFocusedTab();
    updateMenuItemsForProvider(title == null ? null : title.getProvider());
    this.menuEditShowTreeContextMenu.setEnabled(this.explorerTree.hasSelectedItem());
  }//GEN-LAST:event_menuEditMenuSelected

  private void menuEditMenuCanceled(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuEditMenuCanceled
    enableAllMenuItems();
  }//GEN-LAST:event_menuEditMenuCanceled

  private void menuEditMenuDeselected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuEditMenuDeselected
    enableAllMenuItems();
  }//GEN-LAST:event_menuEditMenuDeselected

  private void menuFileMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuFileMenuSelected
    final TabTitle title = this.getFocusedTab();
    updateMenuItemsForProvider(title == null ? null : title.getProvider());
  }//GEN-LAST:event_menuFileMenuSelected

  private void menuFileMenuCanceled(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuFileMenuCanceled
    enableAllMenuItems();
  }//GEN-LAST:event_menuFileMenuCanceled

  private void menuFileMenuDeselected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuFileMenuDeselected
    enableAllMenuItems();
  }//GEN-LAST:event_menuFileMenuDeselected

  private void menuEditCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditCopyActionPerformed
    final TabTitle title = this.getFocusedTab();
    if (title != null && title.getProvider().doesSupportCutCopyPaste()) {
      title.getProvider().doCopy();
    }
    updateMenuItemsForProvider(title == null ? null : title.getProvider());
  }//GEN-LAST:event_menuEditCopyActionPerformed

  private void menuEditPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditPasteActionPerformed
    final TabTitle title = this.getFocusedTab();
    if (title != null && title.getProvider().doesSupportCutCopyPaste()) {
      title.getProvider().doPaste();
    }
    updateMenuItemsForProvider(title == null ? null : title.getProvider());
  }//GEN-LAST:event_menuEditPasteActionPerformed

  private void menuEditCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditCutActionPerformed
    final TabTitle title = this.getFocusedTab();
    if (title != null && title.getProvider().doesSupportCutCopyPaste()) {
      title.getProvider().doCut();
    }
    updateMenuItemsForProvider(title == null ? null : title.getProvider());
  }//GEN-LAST:event_menuEditCutActionPerformed

  @Override
  public boolean showGraphMindMapFileLinksDialog(@Nullable final File projectFolder, @Nullable final File initialMindMapFile, final boolean openIfSelected) {
    Utils.assertSwingDispatchThread();

    boolean result = false;
    if (projectFolder != null || initialMindMapFile != null) {
      File projectFolderToUse = projectFolder;

      if (projectFolderToUse == null) {
        final NodeProject foundProject = findProjectForFile(initialMindMapFile);
        if (foundProject != null) {
          projectFolderToUse = foundProject.getFolder();
        }
      }

      final FileLinkGraphPanel graph = new FileLinkGraphPanel(projectFolderToUse, initialMindMapFile);

      JOptionPane.showMessageDialog(this, graph, "Graph of Mind Map file links", JOptionPane.PLAIN_MESSAGE);
      final FileLinkGraphPanel.FileVertex selected = graph.getSelectedFile();

      final File fileToOpen = selected == null ? null : selected.getFile();
      if (openIfSelected && fileToOpen != null) {
        result = true;
        boolean cantFind = true;
        if (fileToOpen.exists()) {
          if (fileToOpen.isFile()) {
            if (openFileAsTab(fileToOpen, -1)) {
              cantFind = false;
            }
          } else if (fileToOpen.isDirectory()) {
            focusInTree(fileToOpen);
            cantFind = false;
          }
        }
        if (cantFind) {
          DialogProviderManager.getInstance().getDialogProvider().msgWarn(this, "Can't open file \'" + fileToOpen.getAbsolutePath() + "\'!");
          result = false;
        }
      }
    }
    return result;
  }


  private void menuNavigateLinksGraphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNavigateLinksGraphActionPerformed
    final File file = this.getFocusedTab().getAssociatedFile();
    final NodeProject project = findProjectForFile(file);
    showGraphMindMapFileLinksDialog(project == null ? null : project.getFolder(), file, true);
  }//GEN-LAST:event_menuNavigateLinksGraphActionPerformed

  private void menuNavigateMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuNavigateMenuSelected
    final TabTitle title = getFocusedTab();
    this.menuNavigateLinksGraph.setEnabled(title != null && title.getProvider().getEditor().getEditorContentType() == EditorContentType.MINDMAP);
  }//GEN-LAST:event_menuNavigateMenuSelected

  private void menuEditShowTreeContextMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditShowTreeContextMenuActionPerformed
    this.explorerTree.showPopUpForSelectedItem();
  }//GEN-LAST:event_menuEditShowTreeContextMenuActionPerformed

  private void menuNewFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewFileActionPerformed
    File currentFolder = this.explorerTree.getCurrentFocusedFolder();
    if (currentFolder == null) {
      currentFolder = this.explorerTree.findFirstProjectFolder();
    }

    final JFileChooser fileChooser = new JFileChooser(currentFolder);
    fileChooser.setDialogTitle("New file");

    class Filter extends FileFilter {

      final String extension;
      final String description;

      Filter(@Nonnull final String extension, @Nonnull final String description) {
        this.extension = '.' + extension;
        this.description = description;
      }

      @Override
      public boolean accept(@Nonnull final File f) {
        return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(this.extension);
      }

      @Override
      @Nonnull
      public String getDescription() {
        return this.description;
      }
    }

    fileChooser.setAcceptAllFileFilterUsed(false);
    final Filter filerMindMap = new Filter("mmd", "Mind Map file (*.mmd)");
    final Filter filerText = new Filter("txt", "Text file (*.txt)");
    final Filter filerPuml = new Filter("puml", "PantUML file (*.puml)");
    final Filter filerKstpl = new Filter("kstpl", "KStreams topology (*.kstpl)");
    final Filter filerDot = new Filter("gv", "DOT script (*.gv)");

    fileChooser.addChoosableFileFilter(filerMindMap);
    fileChooser.addChoosableFileFilter(filerText);
    fileChooser.addChoosableFileFilter(filerPuml);
    fileChooser.addChoosableFileFilter(filerKstpl);
    fileChooser.addChoosableFileFilter(filerDot);

    fileChooser.setFileFilter(filerMindMap);

    if (fileChooser.showDialog(this, "Create") == JFileChooser.APPROVE_OPTION) {
      final Filter choosenFilter = (Filter) fileChooser.getFileFilter();
      File selectedFile = fileChooser.getSelectedFile();

      if (!selectedFile.getName().contains(".")) {
        selectedFile = new File(selectedFile.getParent(), selectedFile.getName() + choosenFilter.extension);
      }

      final String text;

      if (choosenFilter == filerMindMap) {
        final MindMap model = new MindMap(true);
        model.setAttribute("showJumps", "true"); //NOI18N
        final Topic root = model.getRoot();
        if (root != null) {
          root.setText("Root"); //NOI18N
        }

        try {
          text = model.write(new StringWriter()).toString();
        } catch (IOException ex) {
          throw new Error("Unexpected error", ex);
        }
      } else if (choosenFilter == filerPuml) {
        text = PlantUmlTextEditor.NEW_TEMPLATE;
      } else if (choosenFilter == filerKstpl) {
        text = KsTplTextEditor.NEW_TEMPLATE;
      } else if (choosenFilter == filerDot) {
        text = DotScriptEditor.NEW_TEMPLATE;
      } else {
        text = "";
      }

      try {
        org.apache.commons.io.FileUtils.write(selectedFile, text, "UTF-8");
        this.explorerTree.addFileIfPossible(selectedFile, true);
      } catch (IOException ex) {
        LOGGER.error("Can't create file : " + selectedFile);
        JOptionPane.showMessageDialog(this, "Can't create file", "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }//GEN-LAST:event_menuNewFileActionPerformed

  private void menuHelpPLantUmpManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuHelpPLantUmpManualActionPerformed
    UiUtils.openLocalResourceInDesktop("help/PlantUML_Language_Reference_Guide_en.pdf");
  }//GEN-LAST:event_menuHelpPLantUmpManualActionPerformed

  private void menuHelpHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuHelpHelpActionPerformed
    UiUtils.openLocalResourceInDesktop("help/index.html");
  }//GEN-LAST:event_menuHelpHelpActionPerformed

  private void menuViewMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menuViewMenuSelected
    final AbstractEditor editor = this.tabPane.getCurrentEditor();
    this.menuViewZoom.setEnabled(editor != null && editor.isZoomable());
  }//GEN-LAST:event_menuViewMenuSelected

  private void menuViewZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuViewZoomInActionPerformed
    final AbstractEditor editor = this.tabPane.getCurrentEditor();
    if (editor != null && editor.isZoomable()) editor.doZoomIn();
  }//GEN-LAST:event_menuViewZoomInActionPerformed

  private void menuViewZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuViewZoomOutActionPerformed
    final AbstractEditor editor = this.tabPane.getCurrentEditor();
    if (editor != null && editor.isZoomable()) {
      editor.doZoomOut();
    }
  }//GEN-LAST:event_menuViewZoomOutActionPerformed

  private void menuViewZoomResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuViewZoomResetActionPerformed
    final AbstractEditor editor = this.tabPane.getCurrentEditor();
    if (editor != null && editor.isZoomable()) {
      editor.doZoomReset();
    }
  }//GEN-LAST:event_menuViewZoomResetActionPerformed

  private void enableMenu(final JMenu menu) {
    menu.setEnabled(true);
    for (final Component c : menu.getMenuComponents()) {
      if (c instanceof JMenu) {
        enableMenu((JMenu) c);
      } else if (c instanceof JMenuItem) {
        ((JMenuItem) c).setEnabled(true);
      }
    }
  }

  private void enableAllMenuItems() {
    for (int i = 0; i < this.mainMenu.getMenuCount(); i++) {
      enableMenu(this.mainMenu.getMenu(i));
    }
  }

  public boolean isFullScreenActive() {
    return this.taskToEndFullScreen.get() != null;
  }

  public void endFullScreenIfActive() {
    final Runnable runnable = this.taskToEndFullScreen.getAndSet(null);
    if (runnable != null) {
      final AbstractEditor editor = this.tabPane.getCurrentEditor();
      if (editor != null && editor instanceof MMDEditor) {
        final MMDEditor mmdeditor = (MMDEditor) editor;
        mmdeditor.getMindMapPanel().endEdit(true);
      }
      Utils.safeSwingCall(runnable);
    }
  }

  @Override
  public boolean hasUnsavedDocument() {
    return this.tabPane.hasEditableAndChangedDocument();
  }

  @Override
  @Nullable
  public TabTitle getFocusedTab() {
    return this.tabPane.getCurrentTitle();
  }

  @Override
  @Nonnull
  public NodeProjectGroup getCurrentGroup() {
    return this.explorerTree.getCurrentGroup();
  }

  @Nullable
  @Override
  public File createMindMapFile(@Nullable final File folder) {
    final JFileChooser chooser = new JFileChooser(folder);
    chooser.setDialogTitle("Create new Mind Map");
    chooser.setFileFilter(MMDEditor.MMD_FILE_FILTER);
    chooser.setMultiSelectionEnabled(false);
    chooser.setApproveButtonText("Create");

    File result = null;

    if (chooser.showSaveDialog(SciaRetoStarter.getApplicationFrame()) == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();
      if (!file.getName().endsWith(".mmd")) { //NOI18N
        file = new File(file.getAbsolutePath() + ".mmd"); //NOI18N
      }

      if (file.exists()) {
        DialogProviderManager.getInstance().getDialogProvider().msgError(this, "File '" + file + "' already exists!");
      } else {
        try {
          final MindMap mindMap = new MindMap(true);
          final String text = mindMap.write(new StringWriter()).toString();
          SystemUtils.saveUTFText(file, text);
          result = file;
        } catch (IOException ex) {
          DialogProviderManager.getInstance().getDialogProvider().msgError(this, "Can't save mind map into file '" + file.getName() + "'");
        }
      }
    }
    return result;
  }

  @Nonnull
  private static List<UIManager.LookAndFeelInfo> findBaseLookAndFeels() {
    return Stream.of(UIManager.getInstalledLookAndFeels())
        .filter(x ->
            x.getClassName().startsWith("com.sun.")
                || x.getClassName().startsWith("com.apple.")
                || x.getClassName().startsWith("java.")
                || x.getClassName().startsWith("javax.")
        )
        .sorted(Comparator.comparing(UIManager.LookAndFeelInfo::getName))
        .collect(Collectors.toList());
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JPopupMenu.Separator jSeparator7;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JMenuItem menuAbout;
    private javax.swing.JMenu menuEdit;
    private javax.swing.JMenuItem menuEditCopy;
    private javax.swing.JMenuItem menuEditCut;
    private javax.swing.JMenuItem menuEditPaste;
    private javax.swing.JMenuItem menuEditShowTreeContextMenu;
    private javax.swing.JMenuItem menuExit;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem menuFindText;
    private javax.swing.JMenuItem menuFullScreen;
    private javax.swing.JMenuItem menuGoToFile;
    private javax.swing.JMenu menuHelp;
    private javax.swing.JMenuItem menuHelpHelp;
    private javax.swing.JMenuItem menuHelpPLantUmpManual;
    private javax.swing.JMenu menuLookAndFeel;
    private javax.swing.JMenuItem menuMakeDonation;
    private javax.swing.JMenu menuNavigate;
    private javax.swing.JMenuItem menuNavigateLinksGraph;
    private javax.swing.JMenuItem menuNewFile;
    private javax.swing.JMenuItem menuNewProject;
    private javax.swing.JMenuItem menuOpenFile;
    private javax.swing.JMenuItem menuOpenProject;
    private javax.swing.JMenu menuOpenRecentFile;
    private javax.swing.JMenu menuOpenRecentProject;
    private javax.swing.JMenuItem menuPreferences;
    private javax.swing.JMenuItem menuRedo;
    private javax.swing.JMenuItem menuSave;
    private javax.swing.JMenuItem menuSaveAll;
    private javax.swing.JMenuItem menuSaveAs;
    private javax.swing.JMenuItem menuUndo;
    private javax.swing.JMenu menuView;
    private javax.swing.JMenu menuViewUIScale;
    private javax.swing.JMenu menuViewZoom;
    private javax.swing.JMenuItem menuViewZoomIn;
    private javax.swing.JMenuItem menuViewZoomOut;
    private javax.swing.JMenuItem menuViewZoomReset;
    private javax.swing.JPopupMenu.Separator separatorExitSection;
    // End of variables declaration//GEN-END:variables
}
