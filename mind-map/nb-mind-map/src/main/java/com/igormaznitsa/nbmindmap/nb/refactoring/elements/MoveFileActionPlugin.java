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
package com.igormaznitsa.nbmindmap.nb.refactoring.elements;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.nbmindmap.nb.refactoring.MindMapLink;
import com.igormaznitsa.nbmindmap.nb.refactoring.gui.AbstractMMDRefactoringUI;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

public class MoveFileActionPlugin extends AbstractPlugin<MoveRefactoring> {

  public MoveFileActionPlugin(final MoveRefactoring refactoring) {
    super(refactoring);
  }

  protected static String replaceNameInPath(int pathItemIndexFromEnd, final String path, final String newName) {
    final String normalizedSeparators = FilenameUtils.separatorsToUnix(path);
    int start = normalizedSeparators.length();
    while (start >= 0 && pathItemIndexFromEnd >= 0) {
      start = normalizedSeparators.lastIndexOf('/', start - 1);
      pathItemIndexFromEnd--;
    }

    String result = path;

    if (start >= 0) {
      final int indexEnd = normalizedSeparators.indexOf('/', start + 1);
      if (indexEnd <= 0) {
        result = path.substring(0, start + 1) + newName;
      }
      else {
        result = path.substring(0, start + 1) + newName + path.substring(indexEnd);
      }
    }
    return result;
  }

  @Override
  protected Problem processFile(final Project project, final int level, final File projectFolder, final FileObject fileObject) {
    final MMapURI fileAsURI;
    try {
      fileAsURI = MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null);
    }catch (URISyntaxException ex){
      return new Problem(true, BUNDLE.getString("MoveFileActionPlugin.malformedURI"));
    }

    final Lookup targetLookup = this.refactoring.getTarget();
    if (targetLookup == null) {
      return new Problem(true, BUNDLE.getString("MoveFileActionPlugin.cantFindLookup"));
    }

    final URL targetURL = targetLookup.lookup(URL.class);

    if (targetURL != null) {
      try {
        URI baseURI = targetURL.toURI();
        if (baseURI.isAbsolute()) {
          final URI projectURI = Utilities.toURI(projectFolder);
          baseURI = projectURI.relativize(baseURI);
        }

        final MMapURI newFileAsURI = MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null).replaceBaseInPath(true, baseURI, level);

        for (final FileObject mmap : allMapsInProject(project)) {
          try {
            if (doesMindMapContainFileLink(project, mmap, fileAsURI)) {
              final MoveElement element = new MoveElement(new MindMapLink(mmap), projectFolder, MMapURI.makeFromFilePath(projectFolder, fileObject.getPath(), null));
              element.setTarget(newFileAsURI);
              addElement(element);
            }
          }
          catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
            return new Problem(true, BUNDLE.getString("Refactoring.CantProcessMindMap"));
          }
        }
      }
      catch (URISyntaxException ex) {
        LOGGER.error("Can't make new file uri for " + fileObject.getPath(), ex); //NOI18N
        return new Problem(true, BUNDLE.getString("MoveFileActionPlugin.cantMakeURIForFile")); //NOI18N
      }
      return null;
    }
    else {
      return new Problem(true, BUNDLE.getString("MoveFileActionPlugin.cantFindURL"));
    }
  }

  @Override
  public Problem fastCheckParameters() {
    return this.checkParameters();
  }

  @Override
  public Problem checkParameters() {
    if (this.refactoring.getRefactoringSource().lookup(AbstractMMDRefactoringUI.class) != null) {
      final Lookup targetLookup = this.refactoring.getTarget();
      if (targetLookup == null) {
        return new Problem(true, BUNDLE.getString("MoveFileActionPlugin.cantFindLookup"));
      }

      final URL url = targetLookup.lookup(URL.class);

      if (url == null) {
        return new Problem(true, BUNDLE.getString("MoveFileActionPlugin.cantFindURL"));
      }

      final FileObject obj = URLMapper.findFileObject(url);
      if (obj == null) {
        return new Problem(true, BUNDLE.getString("MoveFileActionPlugin.cantFindTargetFolder"));
      }
      if (!obj.isFolder()) {
        return new Problem(true, BUNDLE.getString("MoveFileActionPlugin.targetIsNotFolder"));
      }
    }
    return null;
  }

}
