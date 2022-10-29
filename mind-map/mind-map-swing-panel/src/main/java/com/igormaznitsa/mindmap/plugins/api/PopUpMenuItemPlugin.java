/*
 * Copyright 2015-2018 Igor Maznitsa.
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

package com.igormaznitsa.mindmap.plugins.api;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.PopUpSection;
import javax.swing.JMenuItem;

/**
 * Interface describing a plug-in to be shown in the mind map panel pop-up menu
 * as an item.
 *
 * @since 1.2
 */
public interface PopUpMenuItemPlugin extends MindMapPlugin {

  JMenuItem makeMenuItem(PluginContext context, Topic activeTopic);

  PopUpSection getSection();

  boolean needsTopicUnderMouse();

  boolean needsSelectedTopics();

  boolean isEnabled(PluginContext context, Topic activeTopic);

  boolean isCompatibleWithFullScreenMode();
}
