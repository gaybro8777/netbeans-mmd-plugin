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

package com.igormaznitsa.ideamindmap.view;

import com.intellij.ProjectTopics;
import com.intellij.ide.CopyPasteUtil;
import com.intellij.ide.bookmarks.Bookmark;
import com.intellij.ide.bookmarks.BookmarksListener;
import com.intellij.ide.projectView.BaseProjectTreeBuilder;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewPsiTreeChangeListener;
import com.intellij.ide.projectView.impl.AbstractProjectTreeStructure;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.AbstractTreeUpdater;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.vcs.FileStatusListener;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.util.Alarm;
import com.intellij.util.SmartList;
import com.intellij.util.messages.MessageBusConnection;
import gnu.trove.THashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.datatransfer.Transferable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;

public class KnowledgeViewTreeBuilder extends BaseProjectTreeBuilder {

  public KnowledgeViewTreeBuilder(@Nonnull Project project,
                                  @Nonnull JTree tree,
                                  @Nonnull DefaultTreeModel treeModel,
                                  @Nullable Comparator<NodeDescriptor> comparator,
                                  @Nonnull KnowledgeViewPanelTreeStructure treeStructure) {
    super(project, tree, treeModel, treeStructure, comparator);

    final MessageBusConnection connection = project.getMessageBus().connect(this);

    connection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
      @Override
      public void rootsChanged(ModuleRootEvent event) {
        queueUpdate();
      }
    });

    connection.subscribe(BookmarksListener.TOPIC, new MyBookmarksListener());

    PsiManager.getInstance(project).addPsiTreeChangeListener(createPsiTreeChangeListener(project), this);
    FileStatusManager.getInstance(project).addFileStatusListener(new MyFileStatusListener(), this);
    CopyPasteManager.getInstance().addContentChangedListener(
            new CopyPasteManager.ContentChangedListener() {
              final AbstractTreeUpdater myUpdater = getUpdater();

              @Override
              public void contentChanged(final Transferable oldTransferable, final Transferable newTransferable) {
                this.updateByTransferable(oldTransferable);
                this.updateByTransferable(newTransferable);
              }

              private void updateByTransferable(final Transferable transferable) {
                PsiElement[] psiElements = CopyPasteUtil.getElementsInTransferable(transferable);
                for (PsiElement psiElement : psiElements) {
                  if (!psiElement.getProject().isDisposed()) {
                    this.myUpdater.addSubtreeToUpdateByElement(psiElement);
                  }
                }

              }
            });

    WolfTheProblemSolver.getInstance(project).addProblemListener(new MyProblemListener(), this);
    setCanYieldUpdate(true);
    initRootNode();
  }

  protected ProjectViewPsiTreeChangeListener createPsiTreeChangeListener(final Project project) {
    return new ProjectTreeBuilderPsiListener(project);
  }

  private PsiElement findPsi(@Nonnull VirtualFile vFile) {
    if (!vFile.isValid()) {
      return null;
    }
    PsiManager psiManager = PsiManager.getInstance(myProject);
    return vFile.isDirectory() ? psiManager.findDirectory(vFile) : psiManager.findFile(vFile);
  }

  private void updateNodesContaining(@Nonnull Collection<VirtualFile> filesToRefresh, @Nonnull DefaultMutableTreeNode rootNode) {
    if (!(rootNode.getUserObject() instanceof ProjectViewNode)) {
      return;
    }
    ProjectViewNode node = (ProjectViewNode) rootNode.getUserObject();
    Collection<VirtualFile> containingFiles = null;
    for (VirtualFile virtualFile : filesToRefresh) {
      if (!virtualFile.isValid()) {
        addSubtreeToUpdate(rootNode); // file must be deleted
        return;
      }
      if (node.contains(virtualFile)) {
        if (containingFiles == null) {
          containingFiles = new SmartList<>();
        }
        containingFiles.add(virtualFile);
      }
    }
    if (containingFiles != null) {
      updateNode(rootNode);
      final Enumeration<TreeNode> children = rootNode.children();
      while (children.hasMoreElements()) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
        updateNodesContaining(containingFiles, child);
      }
    }
  }

  protected class ProjectTreeBuilderPsiListener extends ProjectViewPsiTreeChangeListener {
    public ProjectTreeBuilderPsiListener(final Project project) {
      super(project);
    }

    @Override
    protected DefaultMutableTreeNode getRootNode() {
      return KnowledgeViewTreeBuilder.this.getRootNode();
    }

    @Override
    protected AbstractTreeUpdater getUpdater() {
      return KnowledgeViewTreeBuilder.this.getUpdater();
    }

    @Override
    protected boolean isFlattenPackages() {
      AbstractTreeStructure structure = getTreeStructure();
      return structure instanceof AbstractProjectTreeStructure && ((AbstractProjectTreeStructure) structure).isFlattenPackages();
    }
  }

  private final class MyBookmarksListener implements BookmarksListener {
    @Override
    public void bookmarkAdded(@Nonnull Bookmark b) {
      updateForFile(b.getFile());
    }

    @Override
    public void bookmarkRemoved(@Nonnull Bookmark b) {
      updateForFile(b.getFile());
    }

    @Override
    public void bookmarkChanged(@Nonnull Bookmark b) {
      updateForFile(b.getFile());
    }

    private void updateForFile(@Nonnull VirtualFile file) {
      PsiElement element = findPsi(file);
      if (element != null) {
        queueUpdateFrom(element, false);
      }
    }
  }

  private final class MyFileStatusListener implements FileStatusListener {
    @Override
    public void fileStatusesChanged() {
      queueUpdate(false);
    }

    @Override
    public void fileStatusChanged(@Nonnull VirtualFile vFile) {
      queueUpdate(false);
    }
  }

  private class MyProblemListener extends WolfTheProblemSolver.ProblemListener {
    private final Alarm myUpdateProblemAlarm = new Alarm();
    private final Collection<VirtualFile> myFilesToRefresh = new THashSet<>();

    @Override
    public void problemsAppeared(@Nonnull VirtualFile file) {
      queueUpdate(file);
    }

    @Override
    public void problemsDisappeared(@Nonnull VirtualFile file) {
      queueUpdate(file);
    }

    private void queueUpdate(@Nonnull VirtualFile fileToRefresh) {
      synchronized (myFilesToRefresh) {
        if (myFilesToRefresh.add(fileToRefresh)) {
          myUpdateProblemAlarm.cancelAllRequests();
          myUpdateProblemAlarm.addRequest(() -> {
            if (!myProject.isOpen()) {
              return;
            }
            Set<VirtualFile> filesToRefresh;
            synchronized (myFilesToRefresh) {
              filesToRefresh = new THashSet<>(myFilesToRefresh);
            }
            final DefaultMutableTreeNode rootNode = getRootNode();
            if (rootNode != null) {
              updateNodesContaining(filesToRefresh, rootNode);
            }
            synchronized (myFilesToRefresh) {
              myFilesToRefresh.removeAll(filesToRefresh);
            }
          }, 200, ModalityState.NON_MODAL);
        }
      }
    }
  }
}
