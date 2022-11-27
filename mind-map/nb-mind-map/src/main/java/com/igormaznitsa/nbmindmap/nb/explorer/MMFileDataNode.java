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

import com.igormaznitsa.nbmindmap.nb.editor.MMDDataObject;
import java.awt.Image;
import java.beans.BeanInfo;
import java.util.ResourceBundle;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

public class MMFileDataNode extends DataNode {

  protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com/igormaznitsa/nbmindmap/i18n/Bundle");

  private static final Image NODE_ICON16 = ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/logo/logo16.png"); //NOI18N
  private static final Image NODE_ICON32 = ImageUtilities.loadImage("com/igormaznitsa/nbmindmap/icons/logo/logo32.png"); //NOI18N

  public MMFileDataNode(final MMDDataObject obj, final Lookup lookup) {
    super(obj, Children.LEAF, lookup);
    setShortDescription(BUNDLE.getString("MMDDataNode.shortDescription"));
  }

  @Override
  public Image getIcon(final int type) {
    switch (type) {
      case BeanInfo.ICON_COLOR_32x32:
      case BeanInfo.ICON_MONO_32x32:
        return NODE_ICON32;
      default:
        return NODE_ICON16;
    }
  }

}
