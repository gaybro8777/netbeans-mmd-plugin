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

package com.igormaznitsa.ideamindmap.utils;

import com.igormaznitsa.ideamindmap.editor.MindMapDocumentEditor;
import com.igormaznitsa.ideamindmap.view.KnowledgeViewPane;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.ProjectViewPane;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.NavigatableFileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import javax.annotation.Nonnull;

public enum SelectIn {
  IDE,
  SYSTEM,
  ;

  private static final Logger LOGGER = LoggerFactory.getLogger(SelectIn.class);

  private static void projectFocusTo(final Project project, final VirtualFile file) {
    final ProjectView view = ProjectView.getInstance(project);

    String viewToActivate = ProjectViewPane.ID;

    if (KnowledgeViewPane.ID.equals(view.getCurrentViewId())) {
      final Module theModule = ModuleUtil.findModuleForFile(file, project);
      if (theModule == null) {
        viewToActivate = null;
      } else {
        final VirtualFile knowledgeFolder = IdeaUtils.findKnowledgeFolderForModule(theModule, false);
        if (knowledgeFolder != null && VfsUtil.isAncestor(knowledgeFolder, file, true)) {
          viewToActivate = KnowledgeViewPane.ID;
        }
      }
    }

    if (viewToActivate != null) {
      view.changeView(viewToActivate);
    }

    final ToolWindow toolwindow = ToolWindowManager.getInstance(project).getToolWindow(ToolWindowId.PROJECT_VIEW);
    if (toolwindow != null) {
      toolwindow.activate(() -> view.select(null, file, true));
    }
  }

  public void open(@Nonnull final MindMapDocumentEditor source, @Nonnull final VirtualFile file, final int line) {
    final ProjectManager manager = ProjectManager.getInstance();
    switch (this) {
      case IDE: {
        if (file.isDirectory()) {
          if (IdeaUtils.isInProjectContentRoot(source.getProject(), file)) {
            projectFocusTo(source.getProject(), file);
          } else {
            try {
              manager.loadAndOpenProject(file.getCanonicalPath());
            } catch (Exception ex) {
              LOGGER.error("Can't open folder as project [" + file + ']', ex);
              IdeaUtils.openInSystemViewer(source.getDialogProvider(), file);
            }
          }
        } else {
          projectFocusTo(source.getProject(), file);
          final FileEditor[] editors = FileEditorManager.getInstance(source.getProject()).openFile(file, true);

          if (line > 0) {
            for (final FileEditor e : editors) {
              if (e instanceof NavigatableFileEditor) {
                final TextEditor navigatedEditor = (TextEditor) e;
                final Editor editor = navigatedEditor.getEditor();
                if (editor != null && editor.getDocument().getLineCount() > line) {
                  editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(line - 1, 0));
                  editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                }
              }
            }
          }
        }
      }
      break;
      case SYSTEM: {
        IdeaUtils.openInSystemViewer(source.getDialogProvider(), file);
      }
      break;
    }
  }
}
