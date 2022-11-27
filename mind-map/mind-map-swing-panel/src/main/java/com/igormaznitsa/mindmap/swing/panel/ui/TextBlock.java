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

package com.igormaznitsa.mindmap.swing.panel.ui;

import static com.igormaznitsa.mindmap.model.ModelUtils.breakToLines;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.swing.text.JTextComponent;

public final class TextBlock implements Cloneable {

  private final Rectangle2D bounds = new Rectangle2D.Double(0.0d, 0.0d, 0.0d, 0.0d);
  private String text;
  private Line[] lines;
  private Font font;
  private double maxLineAscent;
  private TextAlign textAlign;

  public TextBlock(final TextBlock orig) {
    this.text = orig.text;
    this.lines = orig.lines.clone();
    this.font = orig.font;
    this.maxLineAscent = orig.maxLineAscent;
    this.bounds.setRect(orig.getBounds());
    this.textAlign = orig.textAlign;
  }

  public TextBlock(final String text, final TextAlign justify) {
    updateText(requireNonNull(text));
    this.textAlign = requireNonNull(justify);
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return new TextBlock(this);
  }

  public void updateText(final String text) {
    this.text = text == null ? "" : text;
    invalidate();
  }

  public void fillByTextAndFont(final JTextComponent compo) {
    compo.setFont(this.font);
    compo.setText(this.text);
  }

  public Rectangle2D getBounds() {
    return this.bounds;
  }

  public TextAlign getTextAlign() {
    return this.textAlign;
  }

  public void setTextAlign(final TextAlign textAlign) {
    this.textAlign = textAlign == null ? TextAlign.CENTER : textAlign;
    invalidate();
  }

  public void invalidate() {
    this.lines = null;
  }

  public void setCoordOffset(final double x, final double y) {
    this.bounds.setFrame(x, y, this.bounds.getWidth(), this.bounds.getHeight());
  }

  public void updateSize(final MMGraphics gfx, final MindMapPanelConfig cfg) {
    this.font =
        cfg.getFont().deriveFont(AffineTransform.getScaleInstance(cfg.getScale(), cfg.getScale()));
    gfx.setFont(font);

    this.maxLineAscent = gfx.getFontMaxAscent();

    double maxWidth = 0.0d;
    double maxHeight = 0.0d;

    final String[] brokenText = breakToLines(this.text);

    this.lines = new Line[brokenText.length];

    int index = 0;
    for (final String s : brokenText) {
      final Rectangle2D lineBounds = gfx.getStringBounds(s);
      maxWidth = Math.max(lineBounds.getWidth(), maxWidth);
      maxHeight += lineBounds.getHeight();
      this.lines[index++] = new Line(s, lineBounds);
    }
    this.bounds.setRect(0.0d, 0.0d, maxWidth, maxHeight);
  }

  public void paint(final MMGraphics gfx, final Color color) {
    if (this.font != null && this.lines != null) {
      double posy = this.bounds.getY() + this.maxLineAscent;
      gfx.setFont(this.font);
      for (final Line l : this.lines) {
        final double drawX;
        switch (this.textAlign) {
          case LEFT: {
            drawX = this.bounds.getX();
          }
          break;
          case CENTER: {
            drawX = this.bounds.getX() + (this.bounds.getWidth() - l.bounds.getWidth()) / 2;
          }
          break;
          case RIGHT: {
            drawX = this.bounds.getX() + (this.bounds.getWidth() - l.bounds.getWidth());
          }
          break;
          default:
            throw new Error("unexpected situation #283794");
        }

        gfx.drawString(l.line, (int) Math.round(drawX), (int) Math.round(posy), color);
        posy += l.bounds.getHeight();
      }
    }
  }

  private static final class Line {

    private final Rectangle2D bounds;
    private final String line;

    private Line(final String line, final Rectangle2D bounds) {
      this.bounds = bounds;
      this.line = line;
    }
  }

}
