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

package com.igormaznitsa.sciareto.preferences;


import com.igormaznitsa.sciareto.ui.editors.PlantUmlSecurityProfile;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;

public final class PrefUtils {
  public static final String ENV_PLANTUML_LIMIT_SIZE = "PLANTUML_LIMIT_SIZE";
  public static final String ENV_PLANTUML_SECURITY_PROFILE = "PLANTUML_SECURITY_PROFILE";

  public static final String PROPERTY_PLANTUML_SECURITY_PROFILE = "plantuml.security.profile";
  public static final String PROPERTY_PLANTUML_DOT_PATH = "plantuml.dotpath";

  private PrefUtils() {

  }

  public static boolean isShowHiddenFilesAndFolders() {
    return PreferencesManager.getInstance().getPreferences().getBoolean("showHiddenFiles", true);
  }

  public static void setPlantUmlSecurityProfileAsSystemProperty() {
    final PlantUmlSecurityProfile currentProfile
        = PlantUmlSecurityProfile.findForText(
        PreferencesManager.getInstance().getPreferences()
            .get(PROPERTY_PLANTUML_SECURITY_PROFILE, null),
        PlantUmlSecurityProfile.LEGACY);
    System.setProperty(ENV_PLANTUML_SECURITY_PROFILE, currentProfile.name());
  }

  @Nullable
  public static String getPlantUmlDotPath() {
    final String result =
        PreferencesManager.getInstance().getPreferences().get(PROPERTY_PLANTUML_DOT_PATH, null);
    return (result == null || result.trim().isEmpty()) ? null : result;
  }

  @Nonnull
  public static String font2str(@Nonnull final Font font) {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(font.getFontName()).append('|').append(font.getStyle()).append('|')
        .append(font.getSize());
    return buffer.toString();
  }

  @Nullable
  public static Font str2font(@Nullable final String text, @Nullable final Font defaultFont) {
    if (text == null) {
      return defaultFont;
    }
    final String[] fields = text.split("\\|"); //NOI18N
    if (fields.length != 3) {
      return defaultFont;
    }
    try {
      return new Font(fields[0], Integer.parseInt(fields[1]), Integer.parseInt(fields[2]));
    } catch (NumberFormatException ex) {
      return defaultFont;
    }
  }
}
