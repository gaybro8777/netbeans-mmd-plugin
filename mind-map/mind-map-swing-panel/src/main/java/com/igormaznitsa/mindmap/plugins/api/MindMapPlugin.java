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

package com.igormaznitsa.mindmap.plugins.api;

import com.igormaznitsa.commons.version.Version;
import com.igormaznitsa.mindmap.plugins.MindMapPluginRegistry;

/**
 * The Main interface for any plug-in to be provided for mind map panel.
 *
 * @see MindMapPluginRegistry
 * @since 1.2
 */
public interface MindMapPlugin extends Comparable<MindMapPlugin> {
  /**
   * Recommended start order for custom user plug-ins.
   */
  int CUSTOM_PLUGIN_START = 1000;

  /**
   * Version of the API.
   */
  Version API = new Version("1.6.0");

  /**
   * Order of the plug-in among another plug-ins.
   *
   * @return the order.
   */
  int getOrder();
}
