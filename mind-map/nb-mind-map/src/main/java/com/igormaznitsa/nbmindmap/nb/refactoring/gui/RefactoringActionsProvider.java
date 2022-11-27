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
package com.igormaznitsa.nbmindmap.nb.refactoring.gui;

import com.igormaznitsa.nbmindmap.nb.refactoring.RefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ActionsImplementationProvider.class, position = 400)
public class RefactoringActionsProvider extends ActionsImplementationProvider {

  @Override
  public boolean canRename(final Lookup lookup) {
    return RefactoringUtils.hasOnlyMMDNodes(lookup);
  }

  @Override
  public boolean canFindUsages(final Lookup lookup) {
    return RefactoringUtils.hasOnlyMMDNodes(lookup);
  }

  @Override
  public boolean canDelete(final Lookup lookup) {
    return RefactoringUtils.hasOnlyMMDNodes(lookup);
  }

  @Override
  public boolean canMove(final Lookup lookup) {
    return RefactoringUtils.hasOnlyMMDNodes(lookup);
  }

  @Override
  public void doMove(final Lookup lookup) {
    final FileObject [] fileObjects = RefactoringUtils.getMMDs(lookup);
    UI.openRefactoringUI(new MoveUI(lookup, fileObjects));
  }
  
  @Override
  public void doFindUsages(final Lookup lookup) {
    final FileObject fileObject = RefactoringUtils.getMMD(lookup);
    UI.openRefactoringUI(new WhereUsedRefactoringUI(lookup, fileObject));
  }
  
  @Override
  public void doDelete(final Lookup lookup) {
    final FileObject [] files = RefactoringUtils.getMMDs(lookup);
    UI.openRefactoringUI(new SafeDeleteUI(lookup, files));
  }
  
  @Override
  public void doRename(final Lookup lookup) {
    final FileObject file = RefactoringUtils.getMMD(lookup);
    UI.openRefactoringUI(new RenameUI(lookup, file));
  }
  
}
