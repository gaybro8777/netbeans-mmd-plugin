/*
 * Copyright (C) 2015-2022 Igor A. Maznitsa
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.igormaznitsa.sciareto.ui.platform;

import javax.annotation.Nonnull;

class PlatformDefault implements Platform {

  @Override
  public void init() {
    
  }

  @Override
  public boolean registerPlatformMenuEvent(@Nonnull final PlatformMenuEvent event, @Nonnull final PlatformMenuAction listener) {
    return false;
  }

  @Override
  @Nonnull
  public String getDefaultLFClassName() {
    return "javax.swing.plaf.nimbus.NimbusLookAndFeel"; //NOI18N
  }

  @Override
  public void dispose() {
    
  }

  @Override
  @Nonnull
  public String getName() {
    return "Default"; //NOI18N
  }
}
