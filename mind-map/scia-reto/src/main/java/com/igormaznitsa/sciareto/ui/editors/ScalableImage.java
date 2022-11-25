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

package com.igormaznitsa.sciareto.ui.editors;

import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.sciareto.preferences.PreferencesManager;
import com.igormaznitsa.sciareto.ui.ScaleStatusIndicator;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

final class ScalableImage extends JComponent implements ScaleStatusIndicator.Scalable {

  private static final long serialVersionUID = 6804581090800919466L;
  private static final float SCALE_STEP = 0.05f;
  private BufferedImage image;
  private float scale = 1.0f;

  public static final int IMG_UNIT_INCREMENT = 16;
  public static final int IMG_BLOCK_INCREMENT = IMG_UNIT_INCREMENT * 8;

  private Point dragOrigin;

  private static final float MIN_SCALE = 0.2f;
  private static final float MAX_SCALE = 10.0f;

  private final java.util.List<ActionListener> scalableListeners = new CopyOnWriteArrayList<>();

  private MindMapPanelConfig config;

  public ScalableImage(@Nonnull final MindMapPanelConfig config) {
    super();
    this.config = config;
    final ScalableImage theInstance = this;

    final MouseAdapter adapter = new MouseAdapter() {
      @Override
      public void mouseWheelMoved(@Nonnull final MouseWheelEvent e) {
        if (!e.isConsumed() && ((e.getModifiers() & config.getScaleModifiers()) == config.getScaleModifiers())) {
          e.consume();
          final float oldScale = scale;
          scale = Math.max(MIN_SCALE, Math.min(scale + (SCALE_STEP * -e.getWheelRotation()), MAX_SCALE));

          final JViewport viewport = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, theInstance);

          final Dimension size = getPreferredSize();

          if (viewport != null) {
            final Dimension extentSize = viewport.getExtentSize();

            final Rectangle viewPos = viewport.getViewRect();
            if (extentSize.width < size.width || extentSize.height < size.height) {
              final Point mousePoint = e.getPoint();
              final int dx = mousePoint.x - viewPos.x;
              final int dy = mousePoint.y - viewPos.y;

              final double scaleRelation = scale / oldScale;

              final int newMouseX = (int) (Math.round(mousePoint.x * scaleRelation));
              final int newMouseY = (int) (Math.round(mousePoint.y * scaleRelation));

              viewPos.x = Math.max(0, newMouseX - dx);
              viewPos.y = Math.max(0, newMouseY - dy);
              viewport.setView(ScalableImage.this);

              scrollRectToVisible(viewPos);
            } else {
              viewPos.x = 0;
              viewPos.y = 0;
              scrollRectToVisible(viewPos);
            }

            final Container scrollPane = viewport.getParent();
            if (scrollPane != null) {
              scrollPane.revalidate();
              scrollPane.repaint();
            }
          } else {
            revalidate();
            repaint();
          }

          fireScaleListeners();
        }
        if (!e.isConsumed()) {
          sendEventToParent(e);
        }
      }

      @Override
      public void mouseClicked(@Nonnull final MouseEvent e) {
        sendEventToParent(e);
      }

      @Override
      public void mousePressed(@Nonnull final MouseEvent e) {
        if (!e.isConsumed()) {
          e.consume();
          dragOrigin = e.getPoint();
        }
        sendEventToParent(e);
      }

      @Override
      public void mouseReleased(@Nonnull final MouseEvent e) {
        if (!e.isConsumed()) {
          e.consume();
          dragOrigin = null;
        }
        sendEventToParent(e);
      }

      @Override
      public void mouseEntered(@Nonnull final MouseEvent e) {
        sendEventToParent(e);
      }

      @Override
      public void mouseExited(@Nonnull final MouseEvent e) {
        sendEventToParent(e);
      }

      @Override
      public void mouseDragged(@Nonnull final MouseEvent e) {
        if (!e.isConsumed() && dragOrigin != null) {
          e.consume();
          final JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, theInstance);
          if (viewPort != null) {
            int deltaX = dragOrigin.x - e.getX();
            int deltaY = dragOrigin.y - e.getY();

            final Rectangle view = viewPort.getViewRect();
            view.x += deltaX;
            view.y += deltaY;
            scrollRectToVisible(view);

            viewPort.revalidate();
            viewPort.repaint();
          }
        }
        if (!e.isConsumed()) {
          sendEventToParent(e);
        }
      }

      @Override
      public void mouseMoved(@Nonnull final MouseEvent e) {
        sendEventToParent(e);
      }
    };
    this.addMouseWheelListener(adapter);
    this.addMouseListener(adapter);
    this.addMouseMotionListener(adapter);
  }

  private void fireScaleListeners() {
    final ActionEvent event = new ActionEvent(ScalableImage.this, 0, "scaled");
    for (final ActionListener a : scalableListeners) {
      a.actionPerformed(event);
    }
  }

  private void sendEventToParent(@Nonnull final MouseEvent e) {
    final Container parent = this.getParent();
    if (parent != null) {
      parent.dispatchEvent(e);
    }
  }

  public void updateConfig(@Nonnull final MindMapPanelConfig config) {
    this.config = config;
    this.repaint();
  }

  @Override
  public void doZoomIn() {
    this.setScale(this.scale + SCALE_STEP);
  }

  @Override
  public void doZoomOut() {
    this.setScale(this.scale - SCALE_STEP);
  }

  @Override
  public void doZoomReset() {
    this.setScale(1.0f);
  }

  @Nonnull
  public MindMapPanelConfig getConfig() {
    return this.config;
  }

  @Override
  @Nonnull
  public Dimension getMinimumSize() {
    return getPreferredSize();
  }

  @Override
  @Nonnull
  public Dimension getMaximumSize() {
    return getPreferredSize();
  }

  @Override
  @Nonnull
  public Dimension getPreferredSize() {
    if (image == null) {
      return new Dimension(16, 16);
    } else {
      return new Dimension(Math.round(this.image.getWidth() * this.scale), Math.round(this.image.getHeight() * this.scale));
    }
  }

  @Override
  public float getScale() {
    return this.scale;
  }

  @Override
  public void paintComponent(@Nonnull final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;
    this.config.getRenderQuality().prepare(gfx);

    final Rectangle bounds = this.getBounds();
    if (this.image == null) {
      gfx.setColor(Color.BLACK);
      gfx.fillRect(0, 0, bounds.width, bounds.height);
      gfx.setColor(Color.RED);
      final String text = "Can't load image, check the log!";
      gfx.drawString(text, (bounds.width - gfx.getFontMetrics().stringWidth(text)) / 2, (bounds.height - gfx.getFontMetrics().getMaxAscent()) / 2);
    } else {
      final Dimension size = getPreferredSize();
      gfx.drawImage(this.image, Math.max(0, (bounds.width - size.width) / 2), Math.max(0, (bounds.height - size.height) / 2), size.width, size.height, null);
    }
  }

  @Nullable
  public BufferedImage getImage() {
    return this.image;
  }

  public void setImage(@Nullable final BufferedImage image, final boolean resetZoom) {
    this.image = image;
    if (resetZoom) {
      setScale(1.0f);
    }
    revalidate();
    repaint();
  }

  @Override
  public void setScale(final float scale) {
    this.scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    revalidate();
    repaint();
    fireScaleListeners();
  }

  @Override
  public void addScaleListener(@Nonnull final ActionListener scaleListener) {
    this.scalableListeners.add(scaleListener);
  }

  @Override
  public void removeScaleListener(@Nonnull final ActionListener scaleListener) {
    this.scalableListeners.remove(scaleListener);
  }

}
