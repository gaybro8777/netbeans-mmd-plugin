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

package com.igormaznitsa.mindmap.annotations;

import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Color constants to be used for generated MMD topics.
 *
 * @see MmdTopic#colorFill()
 * @see MmdTopic#colorText()
 * @see MmdTopic#colorBorder()
 */
public enum MmdColor {

  /**
   * Default color, means that application should use its default element color.
   */
  Default(),
  /**
   * AliceBlue(0xF0, 0xF8, 0xFF).
   */
  AliceBlue(0xF0, 0xF8, 0xFF),
  /**
   * AntiqueWhite(0xFA, 0xEB, 0xD7).
   */
  AntiqueWhite(0xFA, 0xEB, 0xD7),
  /**
   * Aqua(0x00, 0xFF, 0xFF).
   */
  Aqua(0x00, 0xFF, 0xFF),
  /**
   * AquaMarine(0x7F, 0xFF, 0xD4).
   */
  AquaMarine(0x7F, 0xFF, 0xD4),
  /**
   * Azure(0xF0, 0xFF, 0xFF).
   */
  Azure(0xF0, 0xFF, 0xFF),
  /**
   * Beige(0xF5, 0xF5, 0xDC).
   */
  Beige(0xF5, 0xF5, 0xDC),
  /**
   * Bisque(0xFF, 0xE4, 0xC4).
   */
  Bisque(0xFF, 0xE4, 0xC4),
  /**
   * Black(0x00, 0x00, 0x00).
   */
  Black(0x00, 0x00, 0x00),
  /**
   * BlanchedAlmond(0xFF, 0xEB, 0xCD).
   */
  BlanchedAlmond(0xFF, 0xEB, 0xCD),
  /**
   * Blue(0x00, 0x00, 0xFF).
   */
  Blue(0x00, 0x00, 0xFF),
  /**
   * BlueViolet(0x8A, 0x2B, 0xE2).
   */
  BlueViolet(0x8A, 0x2B, 0xE2),
  /**
   * Brown(0xA5, 0x2A, 0x2A).
   */
  Brown(0xA5, 0x2A, 0x2A),
  /**
   * BurlyWood(0xDE, 0xB8, 0x87).
   */
  BurlyWood(0xDE, 0xB8, 0x87),
  /**
   * CadetBlue(0x5F, 0x9E, 0xA0).
   */
  CadetBlue(0x5F, 0x9E, 0xA0),
  /**
   * Chartreuse(0x7F, 0xFF, 0x00).
   */
  Chartreuse(0x7F, 0xFF, 0x00),
  /**
   * Chocolate(0xD2, 0x69, 0x1E).
   */
  Chocolate(0xD2, 0x69, 0x1E),
  /**
   * Coral(0xFF, 0x7F, 0x50).
   */
  Coral(0xFF, 0x7F, 0x50),
  /**
   * CornFlowerBlue(0x64, 0x95, 0xED).
   */
  CornFlowerBlue(0x64, 0x95, 0xED),
  /**
   * CornSilk(0xFF, 0xF8, 0xDC).
   */
  CornSilk(0xFF, 0xF8, 0xDC),
  /**
   * Crimson(0xDC, 0x14, 0x3C).
   */
  Crimson(0xDC, 0x14, 0x3C),
  /**
   * Cyan(0x00, 0xFF, 0xFF).
   */
  Cyan(0x00, 0xFF, 0xFF),
  /**
   * DarkBlue(0x00, 0x00, 0x8B).
   */
  DarkBlue(0x00, 0x00, 0x8B),
  /**
   * DarkCyan(0x00, 0x8B, 0x8B).
   */
  DarkCyan(0x00, 0x8B, 0x8B),
  /**
   * DarkGoldenRod(0xB8, 0x86, 0x0B).
   */
  DarkGoldenRod(0xB8, 0x86, 0x0B),
  /**
   * DarkGray(0xA9, 0xA9, 0xA9).
   */
  DarkGray(0xA9, 0xA9, 0xA9),
  /**
   * DarkGreen(0x00, 0x64, 0x00).
   */
  DarkGreen(0x00, 0x64, 0x00),
  /**
   * DarkKhaki(0xBD, 0xB7, 0x6B).
   */
  DarkKhaki(0xBD, 0xB7, 0x6B),
  /**
   * DarkMagenta(0x8B, 0x00, 0x8B).
   */
  DarkMagenta(0x8B, 0x00, 0x8B),
  /**
   * DarkOliveGreen(0x55, 0x6B, 0x2F).
   */
  DarkOliveGreen(0x55, 0x6B, 0x2F),
  /**
   * DarkOrange(0xFF, 0x8C, 0x00).
   */
  DarkOrange(0xFF, 0x8C, 0x00),
  /**
   * DarkOrchid(0x99, 0x32, 0xCC).
   */
  DarkOrchid(0x99, 0x32, 0xCC),
  /**
   * DarkRed(0x8B, 0x00, 0x00).
   */
  DarkRed(0x8B, 0x00, 0x00),
  /**
   * DarkSalmon(0xE9, 0x96, 0x7A).
   */
  DarkSalmon(0xE9, 0x96, 0x7A),
  /**
   * DarkSeaGreen(0x8F, 0xBC, 0x8F).
   */
  DarkSeaGreen(0x8F, 0xBC, 0x8F),
  /**
   * DarkSlateBlue(0x48, 0x3D, 0x8B).
   */
  DarkSlateBlue(0x48, 0x3D, 0x8B),
  /**
   * DarkSlateGray(0x2F, 0x4F, 0x4F).
   */
  DarkSlateGray(0x2F, 0x4F, 0x4F),
  /**
   * DarkTurquoise(0x00, 0xCE, 0xD1).
   */
  DarkTurquoise(0x00, 0xCE, 0xD1),
  /**
   * DarkViolet(0x94, 0x00, 0xD3).
   */
  DarkViolet(0x94, 0x00, 0xD3),
  /**
   * DeepPink(0xFF, 0x14, 0x93).
   */
  DeepPink(0xFF, 0x14, 0x93),
  /**
   * DeepSkyBlue(0x00, 0xBF, 0xFF).
   */
  DeepSkyBlue(0x00, 0xBF, 0xFF),
  /**
   * DimGray(0x69, 0x69, 0x69).
   */
  DimGray(0x69, 0x69, 0x69),
  /**
   * DodgerBlue(0x1E, 0x90, 0xFF).
   */
  DodgerBlue(0x1E, 0x90, 0xFF),
  /**
   * FireBrick(0xB2, 0x22, 0x22).
   */
  FireBrick(0xB2, 0x22, 0x22),
  /**
   * FloralWhite(0xFF, 0xFA, 0xF0).
   */
  FloralWhite(0xFF, 0xFA, 0xF0),
  /**
   * ForestGreen(0x22, 0x8B, 0x22).
   */
  ForestGreen(0x22, 0x8B, 0x22),
  /**
   * Fuchsia(0xFF, 0x00, 0xFF).
   */
  Fuchsia(0xFF, 0x00, 0xFF),
  /**
   * Gainsboro(0xDC, 0xDC, 0xDC).
   */
  Gainsboro(0xDC, 0xDC, 0xDC),
  /**
   * GhostWhite(0xF8, 0xF8, 0xFF).
   */
  GhostWhite(0xF8, 0xF8, 0xFF),
  /**
   * Gold(0xFF, 0xD7, 0x00).
   */
  Gold(0xFF, 0xD7, 0x00),
  /**
   * GoldenRod(0xDA, 0xA5, 0x20).
   */
  GoldenRod(0xDA, 0xA5, 0x20),
  /**
   * Gray(0x80, 0x80, 0x80).
   */
  Gray(0x80, 0x80, 0x80),
  /**
   * Green(0x00, 0x80, 0x00).
   */
  Green(0x00, 0x80, 0x00),
  /**
   * GreenYellow(0xAD, 0xFF, 0x2F).
   */
  GreenYellow(0xAD, 0xFF, 0x2F),
  /**
   * HoneyDew(0xF0, 0xFF, 0xF0).
   */
  HoneyDew(0xF0, 0xFF, 0xF0),
  /**
   * HotPink(0xFF, 0x69, 0xB4).
   */
  HotPink(0xFF, 0x69, 0xB4),
  /**
   * IndianRed(0xCD, 0x5C, 0x5C).
   */
  IndianRed(0xCD, 0x5C, 0x5C),
  /**
   * Indigo(0x4B, 0x00, 0x82).
   */
  Indigo(0x4B, 0x00, 0x82),
  /**
   * Ivory(0xFF, 0xFF, 0xF0).
   */
  Ivory(0xFF, 0xFF, 0xF0),
  /**
   * Khaki(0xF0, 0xE6, 0x8C).
   */
  Khaki(0xF0, 0xE6, 0x8C),
  /**
   * Lavender(0xE6, 0xE6, 0xFA).
   */
  Lavender(0xE6, 0xE6, 0xFA),
  /**
   * LavenderBlush(0xFF, 0xF0, 0xF5).
   */
  LavenderBlush(0xFF, 0xF0, 0xF5),
  /**
   * LawnGreen(0x7C, 0xFC, 0x00).
   */
  LawnGreen(0x7C, 0xFC, 0x00),
  /**
   * LemonChiffon(0xFF, 0xFA, 0xCD).
   */
  LemonChiffon(0xFF, 0xFA, 0xCD),
  /**
   * LightBlue(0xAD, 0xD8, 0xE6).
   */
  LightBlue(0xAD, 0xD8, 0xE6),
  /**
   * LightCoral(0xF0, 0x80, 0x80).
   */
  LightCoral(0xF0, 0x80, 0x80),
  /**
   * LightCyan(0xE0, 0xFF, 0xFF).
   */
  LightCyan(0xE0, 0xFF, 0xFF),
  /**
   * LightGoldenRodYellow(0xFA, 0xFA, 0xD2).
   */
  LightGoldenRodYellow(0xFA, 0xFA, 0xD2),
  /**
   * LightGreen(0x90, 0xEE, 0x90).
   */
  LightGreen(0x90, 0xEE, 0x90),
  /**
   * LightGray(0xD3, 0xD3, 0xD3).
   */
  LightGray(0xD3, 0xD3, 0xD3),
  /**
   * LightPink(0xFF, 0xB6, 0xC1).
   */
  LightPink(0xFF, 0xB6, 0xC1),
  /**
   * LightSalmon(0xFF, 0xA0, 0x7A).
   */
  LightSalmon(0xFF, 0xA0, 0x7A),
  /**
   * LightSeaGreen(0x20, 0xB2, 0xAA).
   */
  LightSeaGreen(0x20, 0xB2, 0xAA),
  /**
   * LightSkyBlue(0x87, 0xCE, 0xFA).
   */
  LightSkyBlue(0x87, 0xCE, 0xFA),
  /**
   * LightSlateGray(0x77, 0x88, 0x99).
   */
  LightSlateGray(0x77, 0x88, 0x99),
  /**
   * LightSteelBlue(0xB0, 0xC4, 0xDE).
   */
  LightSteelBlue(0xB0, 0xC4, 0xDE),
  /**
   * LightYellow(0xFF, 0xFF, 0xE0).
   */
  LightYellow(0xFF, 0xFF, 0xE0),
  /**
   * Lime(0x00, 0xFF, 0x00).
   */
  Lime(0x00, 0xFF, 0x00),
  /**
   * LimeGreen(0x32, 0xCD, 0x32).
   */
  LimeGreen(0x32, 0xCD, 0x32),
  /**
   * Linen(0xFA, 0xF0, 0xE6).
   */
  Linen(0xFA, 0xF0, 0xE6),
  /**
   * Magenta(0xFF, 0x00, 0xFF).
   */
  Magenta(0xFF, 0x00, 0xFF),
  /**
   * Maroon(0x80, 0x00, 0x00).
   */
  Maroon(0x80, 0x00, 0x00),
  /**
   * MediumAquaMarine(0x66, 0xCD, 0xAA).
   */
  MediumAquaMarine(0x66, 0xCD, 0xAA),
  /**
   * MediumBlue(0x00, 0x00, 0xCD).
   */
  MediumBlue(0x00, 0x00, 0xCD),
  /**
   * MediumOrchid(0xBA, 0x55, 0xD3).
   */
  MediumOrchid(0xBA, 0x55, 0xD3),
  /**
   * MediumPurple(0x93, 0x70, 0xDB).
   */
  MediumPurple(0x93, 0x70, 0xDB),
  /**
   * MediumSeaGreen(0x3C, 0xB3, 0x71).
   */
  MediumSeaGreen(0x3C, 0xB3, 0x71),
  /**
   * MediumSlateBlue(0x7B, 0x68, 0xEE).
   */
  MediumSlateBlue(0x7B, 0x68, 0xEE),
  /**
   * MediumSpringGreen(0x00, 0xFA, 0x9A).
   */
  MediumSpringGreen(0x00, 0xFA, 0x9A),
  /**
   * MediumTurquoise(0x48, 0xD1, 0xCC).
   */
  MediumTurquoise(0x48, 0xD1, 0xCC),
  /**
   * MediumVioletRed(0xC7, 0x15, 0x85).
   */
  MediumVioletRed(0xC7, 0x15, 0x85),
  /**
   * MidnightBlue(0x19, 0x19, 0x70).
   */
  MidnightBlue(0x19, 0x19, 0x70),
  /**
   * MintCream(0xF5, 0xFF, 0xFA).
   */
  MintCream(0xF5, 0xFF, 0xFA),
  /**
   * MistyRose(0xFF, 0xE4, 0xE1).
   */
  MistyRose(0xFF, 0xE4, 0xE1),
  /**
   * Moccasin(0xFF, 0xE4, 0xB5).
   */
  Moccasin(0xFF, 0xE4, 0xB5),
  /**
   * NavajoWhite(0xFF, 0xDE, 0xAD).
   */
  NavajoWhite(0xFF, 0xDE, 0xAD),
  /**
   * Navy(0x00, 0x00, 0x80).
   */
  Navy(0x00, 0x00, 0x80),
  /**
   * OldLace(0xFD, 0xF5, 0xE6).
   */
  OldLace(0xFD, 0xF5, 0xE6),
  /**
   * Olive(0x80, 0x80, 0x00).
   */
  Olive(0x80, 0x80, 0x00),
  /**
   * OliveDrab(0x6B, 0x8E, 0x23).
   */
  OliveDrab(0x6B, 0x8E, 0x23),
  /**
   * Orange(0xFF, 0xA5, 0x00).
   */
  Orange(0xFF, 0xA5, 0x00),
  /**
   * OrangeRed(0xFF, 0x45, 0x00).
   */
  OrangeRed(0xFF, 0x45, 0x00),
  /**
   * Orchid(0xDA, 0x70, 0xD6).
   */
  Orchid(0xDA, 0x70, 0xD6),
  /**
   * PaleGoldenRod(0xEE, 0xE8, 0xAA).
   */
  PaleGoldenRod(0xEE, 0xE8, 0xAA),
  /**
   * PaleGreen(0x98, 0xFB, 0x98).
   */
  PaleGreen(0x98, 0xFB, 0x98),
  /**
   * PaleTurquoise(0xAF, 0xEE, 0xEE).
   */
  PaleTurquoise(0xAF, 0xEE, 0xEE),
  /**
   * PaleVioletRed(0xDB, 0x70, 0x93).
   */
  PaleVioletRed(0xDB, 0x70, 0x93),
  /**
   * PapayaWhip(0xFF, 0xEF, 0xD5).
   */
  PapayaWhip(0xFF, 0xEF, 0xD5),
  /**
   * PeachPuff(0xFF, 0xDA, 0xB9).
   */
  PeachPuff(0xFF, 0xDA, 0xB9),
  /**
   * Peru(0xCD, 0x85, 0x3F).
   */
  Peru(0xCD, 0x85, 0x3F),
  /**
   * Pink(0xFF, 0xC0, 0xCB).
   */
  Pink(0xFF, 0xC0, 0xCB),
  /**
   * Plum(0xDD, 0xA0, 0xDD).
   */
  Plum(0xDD, 0xA0, 0xDD),
  /**
   * PowderBlue(0xB0, 0xE0, 0xE6).
   */
  PowderBlue(0xB0, 0xE0, 0xE6),
  /**
   * Purple(0x80, 0x00, 0x80).
   */
  Purple(0x80, 0x00, 0x80),
  /**
   * Red(0xFF, 0x00, 0x00).
   */
  Red(0xFF, 0x00, 0x00),
  /**
   * RosyBrown(0xBC, 0x8F, 0x8F).
   */
  RosyBrown(0xBC, 0x8F, 0x8F),
  /**
   * RoyalBlue(0x41, 0x69, 0xE1).
   */
  RoyalBlue(0x41, 0x69, 0xE1),
  /**
   * SaddleBrown(0x8B, 0x45, 0x13).
   */
  SaddleBrown(0x8B, 0x45, 0x13),
  /**
   * Salmon(0xFA, 0x80, 0x72).
   */
  Salmon(0xFA, 0x80, 0x72),
  /**
   * SandyBrown(0xF4, 0xA4, 0x60).
   */
  SandyBrown(0xF4, 0xA4, 0x60),
  /**
   * SeaGreen(0x2E, 0x8B, 0x57).
   */
  SeaGreen(0x2E, 0x8B, 0x57),
  /**
   * SeaShell(0xFF, 0xF5, 0xEE).
   */
  SeaShell(0xFF, 0xF5, 0xEE),
  /**
   * Sienna(0xA0, 0x52, 0x2D).
   */
  Sienna(0xA0, 0x52, 0x2D),
  /**
   * Silver(0xC0, 0xC0, 0xC0).
   */
  Silver(0xC0, 0xC0, 0xC0),
  /**
   * SkyBlue(0x87, 0xCE, 0xEB).
   */
  SkyBlue(0x87, 0xCE, 0xEB),
  /**
   * SlateBlue(0x6A, 0x5A, 0xCD).
   */
  SlateBlue(0x6A, 0x5A, 0xCD),
  /**
   * SlateGray(0x70, 0x80, 0x90).
   */
  SlateGray(0x70, 0x80, 0x90),
  /**
   * Snow(0xFF, 0xFA, 0xFA).
   */
  Snow(0xFF, 0xFA, 0xFA),
  /**
   * SpringGreen(0x00, 0xFF, 0x7F).
   */
  SpringGreen(0x00, 0xFF, 0x7F),
  /**
   * SteelBlue(0x46, 0x82, 0xB4).
   */
  SteelBlue(0x46, 0x82, 0xB4),
  /**
   * Tan(0xD2, 0xB4, 0x8C).
   */
  Tan(0xD2, 0xB4, 0x8C),
  /**
   * Teal(0x00, 0x80, 0x80).
   */
  Teal(0x00, 0x80, 0x80),
  /**
   * Thistle(0xD8, 0xBF, 0xD8).
   */
  Thistle(0xD8, 0xBF, 0xD8),
  /**
   * Tomato(0xFF, 0x63, 0x47).
   */
  Tomato(0xFF, 0x63, 0x47),
  /**
   * Turquoise(0x40, 0xE0, 0xD0).
   */
  Turquoise(0x40, 0xE0, 0xD0),
  /**
   * Violet(0xEE, 0x82, 0xEE).
   */
  Violet(0xEE, 0x82, 0xEE),
  /**
   * Wheat(0xF5, 0xDE, 0xB3).
   */
  Wheat(0xF5, 0xDE, 0xB3),
  /**
   * White(0xFF, 0xFF, 0xFF).
   */
  White(0xFF, 0xFF, 0xFF),
  /**
   * WhiteSmoke(0xF5, 0xF5, 0xF5).
   */
  WhiteSmoke(0xF5, 0xF5, 0xF5),
  /**
   * Yellow(0xFF, 0xFF, 0x00).
   */
  Yellow(0xFF, 0xFF, 0x00),
  /**
   * YellowGreen(0x9A, 0xCD, 0x32).
   */
  YellowGreen(0x9A, 0xCD, 0x32);

  private final String htmlColor;
  private final int argb;

  MmdColor() {
    this.htmlColor = "#FFFFFF";
    this.argb = 0x00FFFFFF;
  }

  MmdColor(final int red, final int green, final int blue) {
    final int rawRgb = (red << 16) | (green << 8) | blue;

    String hexColor = Integer.toHexString(rawRgb).toUpperCase(Locale.ENGLISH);
    this.argb = rawRgb | 0xFF000000;
    hexColor = "000000".substring(0, 6 - hexColor.length()) + hexColor;
    this.htmlColor = '#' + hexColor;
  }

  /**
   * Find value for value in html color format, '#RRGGBB'
   *
   * @param htmlColor html color value, can be null
   * @return found value or {@link #Default} if not found
   */
  public static MmdColor findForHtmlColor(final String htmlColor) {
    if (htmlColor == null || htmlColor.isEmpty()) {
      return Default;
    }
    for (final MmdColor color : MmdColor.values()) {
      if (color.htmlColor.equalsIgnoreCase(htmlColor)) {
        return color;
      }
    }
    throw new NoSuchElementException("There is no color enum value for " + htmlColor);
  }

  /**
   * Get ARGB value in format AARRGGBB.
   *
   * @return value of argb color
   */
  public int getArgb() {
    return this.argb;
  }

  /**
   * Get HTML color in format #RRGGBB
   *
   * @return value in html color format
   */
  public String getHtmlColor() {
    return this.htmlColor;
  }
}
