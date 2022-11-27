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

package com.igormaznitsa.ideamindmap.plugins;

import static com.igormaznitsa.meta.common.utils.Assertions.assertNotNull;


import com.igormaznitsa.ideamindmap.print.IdeaMMDPrintPanelAdaptor;
import com.igormaznitsa.ideamindmap.utils.AllIcons;
import com.igormaznitsa.ideamindmap.utils.IdeaUtils;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import com.igormaznitsa.mindmap.plugins.api.AbstractPopupMenuItem;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.print.MMDPrintPanel;
import com.igormaznitsa.mindmap.print.PrintableObject;
import com.intellij.openapi.project.Project;

import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JMenuItem;

public class PrinterPlugin extends AbstractPopupMenuItem {
  private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("i18n/Bundle");

  @Nullable
  @Override
  public JMenuItem makeMenuItem(@Nonnull final PluginContext context, @Nullable Topic activeTopic) {
    final JMenuItem printAction = UI_COMPO_FACTORY.makeMenuItem(BUNDLE.getString("MMDGraphEditor.makePopUp.miPrintPreview"), AllIcons.PopUp.PRINTER);
    final Project project = (Project) assertNotNull(context.getPanel().getClientProperty("mmd.editor.project"));
    printAction.addActionListener(e -> {
      final MMDPrintPanel panel = new MMDPrintPanel(context.getDialogProvider(), new IdeaMMDPrintPanelAdaptor(project), PrintableObject.newBuild().mmdpanel(context.getPanel()).build());
      IdeaUtils.plainMessageClose(project, "Print mind map", panel);
    });
    return printAction;
  }

  @Override
  public boolean isEnabled(@Nonnull PluginContext context, @Nullable Topic activeTopic) {
    return !context.getPanel().getModel().isEmpty();
  }

  @Nonnull
  @Override
  public PopUpSection getSection() {
    return PopUpSection.MISC;
  }

  @Override
  public boolean needsTopicUnderMouse() {
    return false;
  }

  @Override
  public boolean needsSelectedTopics() {
    return false;
  }

  @Override
  public int getOrder() {
    return CUSTOM_PLUGIN_START + 100;
  }
}
