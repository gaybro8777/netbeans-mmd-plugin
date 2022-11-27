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
package com.igormaznitsa.nbmindmap.nb.explorer;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.Action;

import org.openide.actions.FileSystemAction;
import org.openide.actions.PropertiesAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

import com.igormaznitsa.meta.annotation.MayContainNull;

public abstract class AbstractMMFilter extends FilterNode {

  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractMMFilter.class);

  protected static final Action[] EMPTY_ACTION = new Action[0];

  protected Action[] actions;

  public AbstractMMFilter(@Nonnull final Node original) {
    super(original);
  }

  public AbstractMMFilter(@Nonnull final Node original, @Nonnull final org.openide.nodes.Children children) {
    super(original, children);
  }

  public AbstractMMFilter(@Nonnull final Node original, @Nonnull final org.openide.nodes.Children children, @Nonnull final Lookup lookup) {
    super(original, children, lookup);
  }

  @Override
  @Nonnull
  @MayContainNull
  public Action[] getActions(final boolean context) {
    if (!context) {
      if (actions == null) {
        // Copy actions and leave out the PropertiesAction and FileSystemAction.                
        Action superActions[] = super.getActions(context);
        List<Action> actionList = new ArrayList<Action>(superActions.length);

        for (int i = 0; i < superActions.length; i++) {
          if ((i <= superActions.length - 2) && superActions[i] == null && (superActions[i + 1] instanceof PropertiesAction)) {
            i++;
            continue;
          }
          else if (superActions[i] instanceof PropertiesAction) {
            continue;
          }
          else if (superActions[i] instanceof FileSystemAction) {
            actionList.add(null); // insert separator and new action
            actionList.addAll((List<Action>) org.openide.util.Utilities.actionsForPath("Projects/package/Actions"));
          }

          actionList.add(superActions[i]);
        }

        actions = new Action[actionList.size()];
        actionList.toArray(actions);
      }
      return actions;
    }
    else {
      return super.getActions(context);
    }
  }
}
