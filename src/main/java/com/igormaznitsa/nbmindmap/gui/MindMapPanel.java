/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.gui;

import com.igormaznitsa.nbmindmap.gui.mmview.*;
import com.igormaznitsa.nbmindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.model.MindMapTopic;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MindMapPanel extends JPanel {

  private static final long serialVersionUID = 7474413542713752159L;

  private volatile MindMap model;
  private volatile String errorText;

  private final Configuration config;

  private final List<MindMapListener> mindMapListeners = new CopyOnWriteArrayList<MindMapListener>();

  private final float SCALE_STEP = 0.5f;

  private static final Color COLOR_MOUSE_DRAG_SELECTION = new Color(0x80000000, true);

  private final JTextArea textEditor = new JTextArea();
  private final JPanel textEditorPanel = new JPanel(new BorderLayout(0, 0));
  private AbstractElement elementUnderEdit = null;

  private final List<MindMapTopic> selectedTopics = new ArrayList<MindMapTopic>();

  private MouseSelectedArea mouseDragSelection = null;

  public MindMapPanel() {
    super(null);
    this.config = new Configuration(this);

    this.textEditor.setMargin(new Insets(5, 5, 5, 5));
    this.textEditor.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(final KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          e.consume();
        }
      }

      @Override
      public void keyReleased(final KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_ENTER: {
            e.consume();
            if (((KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK) & e.getModifiersEx()) == 0) {
              endEdit(true);
            }
            else {
              textEditor.insert("\n", textEditor.getCaretPosition());
            }
          }
          break;
          case KeyEvent.VK_ESCAPE: {
            e.consume();
            endEdit(false);
          }
          break;
        }
      }
    });

    this.textEditor.getDocument().addDocumentListener(new DocumentListener() {

      private void updateEditorPanelSize(final Dimension newSize) {
        final Dimension editorPanelMinSize = textEditorPanel.getMinimumSize();
        final Dimension newDimension = new Dimension(Math.max(editorPanelMinSize.width, newSize.width), Math.max(editorPanelMinSize.height, newSize.height));
        textEditorPanel.setSize(newDimension);
        textEditorPanel.repaint();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        updateEditorPanelSize(textEditor.getPreferredSize());
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        updateEditorPanelSize(textEditor.getPreferredSize());
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        updateEditorPanelSize(textEditor.getPreferredSize());
      }
    });
    this.textEditorPanel.add(this.textEditor, BorderLayout.CENTER);

    super.setOpaque(true);

    final KeyAdapter keyAdapter = new KeyAdapter() {

      @Override
      public void keyReleased(final KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_TAB: {
            if (hasOnlyTopicSelected()) {
              makeNewChildAndStartEdit(selectedTopics.get(0), null);
            }
          }
          break;
          case KeyEvent.VK_DELETE: {
            deleteSelectedTopics();
          }
          break;
          case KeyEvent.VK_ENTER: {
            if (!hasActiveEditor() && hasOnlyTopicSelected()) {
              final MindMapTopic selectedTopic = selectedTopics.get(0);
              makeNewChildAndStartEdit(selectedTopic.getParent() == null ? selectedTopic : selectedTopic.getParent(), selectedTopic);
            }
          }
          break;
        }
      }
    };

    this.setFocusTraversalKeysEnabled(false);

    final MouseAdapter adapter = new MouseAdapter() {

      @Override
      public void mousePressed(final MouseEvent e) {
        mouseDragSelection = null;
        MindMap theMap = model;
        AbstractElement element = null;
        if (theMap != null) {
          element = findTopicUnderPoint(e.getPoint());
          if (element == null) {
            mouseDragSelection = new MouseSelectedArea(e.getPoint());
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (mouseDragSelection != null) {
          final List<MindMapTopic> covered = mouseDragSelection.getAllSelectedElements(model);
          if ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
            for (final MindMapTopic m : covered) {
              select(m, false);
            }
          }
          else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0) {
            for (final MindMapTopic m : covered) {
              select(m, true);
            }
          }
          else {
            selectedTopics.clear();
            for (final MindMapTopic m : covered) {
              select(m, false);
            }
          }

          mouseDragSelection = null;
          repaint();
        }
      }

      @Override
      public void mouseDragged(final MouseEvent e) {
        if (mouseDragSelection != null) {
          mouseDragSelection.update(e);
          repaint();
        }
      }

      @Override
      public void mouseWheelMoved(final MouseWheelEvent e) {
        if (e.isControlDown()) {
          endEdit(false);
          setScale(getScale() + (SCALE_STEP * -e.getWheelRotation()));
          invalidate();
          revalidate();
          repaint();
        }
      }

      @Override
      public void mouseClicked(final MouseEvent e) {
        MindMap theMap = model;
        AbstractElement element = null;
        if (theMap != null) {
          element = findTopicUnderPoint(e.getPoint());
        }

        if (e.isPopupTrigger()) {
          processPopUp(e.getPoint(), element);
        }
        else {
          final ElementPart part = element == null ? ElementPart.NONE : element.findPartForPoint(e.getPoint());
          if (part == ElementPart.COLLAPSATOR) {
            mouseDragSelection = null;
            removeSelection();
            ((AbstractCollapsableElement) element).setCollapse(!element.isCollapsed());
            invalidate();
            fireNotificationMindMapChanged();
            repaint();
          }
          else if (e.getClickCount() > 1) {
            mouseDragSelection = null;
            startEdit(element);
          }
          else {
            if (element != null) {
              mouseDragSelection = null;
              if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
                // only
                removeSelection();
                select(element.getModel(), false);
              }
              else {
                // group
                if (selectedTopics.isEmpty()) {
                  select(element.getModel(), false);
                }
                else {
                  select(element.getModel(), true);
                }
              }
            }
          }
        }
      }
    };

    addMouseWheelListener(adapter);
    addMouseListener(adapter);
    addMouseMotionListener(adapter);
    addKeyListener(keyAdapter);

    this.textEditorPanel.setVisible(false);
    this.add(this.textEditorPanel);
  }

  public void removeSelection() {
    this.selectedTopics.clear();
    repaint();
  }

  private boolean hasActiveEditor() {
    return this.elementUnderEdit != null;
  }

  private void makeNewChildAndStartEdit(final MindMapTopic parent, final MindMapTopic after) {
    if (parent != null) {
      this.selectedTopics.clear();
      final MindMapTopic newTopic = parent.makeChild("", after);
      select(newTopic, false);
      invalidate();
      revalidate();
      fireNotificationMindMapChanged();
      startEdit((AbstractElement) newTopic.getPayload());
    }
  }

  protected void fireNotificationMindMapChanged() {
    for (final MindMapListener l : mindMapListeners) {
      l.onMindMapModelChanged(this);
    }
  }

  protected void fireNotificationEnsureTopicVisibility(final MindMapTopic topic) {
    for (final MindMapListener l : mindMapListeners) {
      l.onEnsureVisibilityOfTopic(this, topic);
    }
  }

  protected void deleteSelectedTopics() {
    if (!this.selectedTopics.isEmpty()) {
      for (final MindMapTopic t : this.selectedTopics) {
        this.model.removeTopic(t);
      }
      removeSelection();
      invalidate();
      revalidate();
      fireNotificationMindMapChanged();
      repaint();
    }
  }

  public boolean hasOnlyTopicSelected() {
    return this.selectedTopics.size() == 1;
  }

  public void removeFromSelected(final MindMapTopic t) {
    if (this.selectedTopics.contains(t)) {
      this.selectedTopics.remove(t);
      repaint();
    }
  }

  public void select(final MindMapTopic t, final boolean removeIfPresented) {
    if (!this.selectedTopics.contains(t)) {
      this.selectedTopics.add(t);
      repaint();
    }
    else if (removeIfPresented) {
      removeFromSelected(t);
    }
  }

  public void endEdit(final boolean commit) {
    try {
      if (commit && this.elementUnderEdit != null) {
        final String text = this.textEditor.getText();
        this.elementUnderEdit.setText(this.textEditor.getText());
        repaint();
        fireNotificationMindMapChanged();
      }
    }
    finally {
      this.elementUnderEdit = null;
      this.textEditorPanel.setVisible(false);
      this.requestFocus();
    }
  }

  protected void startEdit(final AbstractElement element) {
    if (element == null) {
      this.elementUnderEdit = null;
      this.textEditorPanel.setVisible(false);
    }
    else {
      this.elementUnderEdit = element;
      final TextBlock painter = element.getTextBlock();
      element.fillByTextAndFont(this.textEditor);
      final Dimension textBlockSize = new Dimension((int) element.getBounds().getWidth(), (int) element.getBounds().getHeight());
      this.textEditorPanel.setBounds((int) element.getBounds().getX(), (int) element.getBounds().getY(), textBlockSize.width, textBlockSize.height);
      this.textEditor.setMinimumSize(textBlockSize);

      this.scrollRectToVisible(textEditorPanel.getBounds());
      this.fireNotificationEnsureTopicVisibility(this.elementUnderEdit.getModel());

      this.textEditorPanel.setVisible(true);
      this.textEditor.requestFocus();
    }
  }

  protected void processPopUp(final Point point, final AbstractElement element) {

  }

  public void addMindMapListener(final MindMapListener l) {
    Utils.assertNotNull("Listener must not be null", l);
    this.mindMapListeners.add(l);
  }

  public void removeMindMapListener(final MindMapListener l) {
    if (l != null) {
      this.mindMapListeners.remove(l);
    }
  }

  public void setModel(final MindMap model) {
    this.model = model;
    invalidate();
    revalidate();
    repaint();
  }

  @Override
  public boolean isFocusable() {
    return true;
  }

  public MindMap getModel() {
    return this.model;
  }

  public void setScale(final float zoom) {
    this.config.setScale(zoom);
  }

  public float getScale() {
    return this.config.getScale();
  }

  public void drawOnGraphicsForConfiguration(final Graphics2D g, final Configuration config, final MindMap map, final boolean drawSelection) {
    final Rectangle clipBounds = g.getClipBounds();

    if (config.isDrawBackground()) {
      g.setColor(config.getPaperColor());
      g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

      if (config.isShowGrid()) {
        final float scaledGridStep = config.getGridStep() * config.getScale();

        final float minX = clipBounds.x;
        final float minY = clipBounds.y;
        final float maxX = clipBounds.x + clipBounds.width;
        final float maxY = clipBounds.y + clipBounds.height;

        g.setColor(config.getGridColor());

        for (float x = 0.0f; x < maxX; x += scaledGridStep) {
          if (x < minX) {
            continue;
          }
          final int intx = Math.round(x);
          g.drawLine(intx, (int) minY, intx, (int) maxY);
        }

        for (float y = 0.0f; y < maxY; y += scaledGridStep) {
          if (y < minY) {
            continue;
          }
          final int inty = Math.round(y);
          g.drawLine((int) minX, inty, (int) maxX, inty);
        }
      }
    }

    drawTopics(g, config, map);
    if (drawSelection) {
      drawSelection(g, config);
    }
  }

  private void drawSelection(final Graphics2D g, final Configuration cfg) {
    if (!this.selectedTopics.isEmpty()) {
      g.setColor(cfg.getSelectLineColor());
      final Stroke dashed = new BasicStroke(cfg.getSelectLineWidth() * cfg.getScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{1 * cfg.getScale(), 4 * cfg.getScale()}, 0);
      g.setStroke(dashed);
      final double selectLineGap = cfg.getSelectLineGap() * cfg.getScale();
      final double dblLineGap = selectLineGap * 2.0d;

      for (final MindMapTopic s : this.selectedTopics) {
        final AbstractElement e = (AbstractElement) s.getPayload();
        if (e != null) {
          g.drawRect((int) Math.round(e.getBounds().getX() - selectLineGap), (int) Math.round(e.getBounds().getY() - selectLineGap), (int) Math.round(e.getBounds().getWidth() + dblLineGap), (int) Math.round(e.getBounds().getHeight() + dblLineGap));
        }
      }
    }
  }

  private void drawTopics(final Graphics2D g, final Configuration cfg, final MindMap map) {
    if (map != null) {
      final MindMapTopic root = map.getRoot();
      if (root != null) {
        if (root.getPayload() == null) {
          revalidate();
        }
        drawTopicTree(g, root, cfg);
      }
    }
  }

  private void drawTopicTree(final Graphics2D gfx, final MindMapTopic topic, final Configuration cfg) {
    paintTopic(gfx, topic, cfg);
    final AbstractElement w = (AbstractElement) topic.getPayload();
    if (w.isCollapsed()) {
      return;
    }
    for (final MindMapTopic t : topic.getChildren()) {
      drawTopicTree(gfx, t, cfg);
    }
  }

  private void paintTopic(final Graphics2D gfx, final MindMapTopic topic, final Configuration cfg) {
    ((AbstractElement) topic.getPayload()).doPaint(gfx, cfg);
  }

  private void revalidateTopicTree(final Graphics2D gfx, final Configuration cfg, final MindMapTopic topic, final int level) {
    AbstractElement widget = (AbstractElement) topic.getPayload();
    if (widget == null) {
      switch (level) {
        case 0:
          widget = new ElementRoot(topic);
          break;
        case 1:
          widget = new ElementLevelFirst(topic);
          break;
        default:
          widget = new ElementLevelOther(topic);
          break;
      }
      topic.setPayload(widget);
    }

    widget.updateElementBounds(gfx, cfg);
    for (final MindMapTopic t : topic.getChildren()) {
      revalidateTopicTree(gfx, cfg, t, level + 1);
    }
    widget.updateBlockSize(cfg);
  }

  protected void revalidateWholeTree(final Graphics2D gfx, final Configuration cfg, final MindMap model) {
    if (gfx.getFontMetrics() != null) {
      revalidateTopicTree(gfx, cfg, model.getRoot(), 0);
      realignElements(cfg, model);
    }
  }

  protected void realignElements(final Configuration cfg, final MindMap model) {
    if (model != null) {
      final MindMapTopic root = model.getRoot();
      if (root.getPayload() != null) {
        final ElementRoot rootWidget = (ElementRoot) root.getPayload();
        final Dimension2D blockSize = rootWidget.getBlockSize();

        final double scaledPageMargin = cfg.getPaperMargins() * cfg.getScale();

        final Dimension diagramSize = new Dimension((int) Math.round(blockSize.getWidth() + scaledPageMargin * 2), (int) Math.round(blockSize.getHeight() + scaledPageMargin * 2));

        setMinimumSize(diagramSize);
        setPreferredSize(diagramSize);

        final Dimension panelSize = getSize();

        final double xOff = Math.max(0.0d, panelSize.getWidth() - diagramSize.getWidth()) / 2;
        final double yOff = Math.max(0.0d, panelSize.getHeight() - diagramSize.getHeight()) / 2;

        final Dimension2D leftBlock = rootWidget.getLeftBlockSize();

        rootWidget.alignElementAndChildren(cfg, true, xOff + scaledPageMargin + leftBlock.getWidth(), yOff + scaledPageMargin + (blockSize.getHeight() - rootWidget.getBounds().getHeight()) / 2);

        for (final MindMapListener l : this.mindMapListeners) {
          l.onMindMapModelRealigned(this, diagramSize);
        }
      }
    }
  }

  @Override
  public void revalidate() {
    final Runnable runnable = new Runnable() {
      @Override
      public void run() {
        if (!isValid()) {
          final Graphics2D gfx = (Graphics2D) getGraphics();
          if (gfx != null) {
            revalidateWholeTree(gfx, config, model);
          }
        }
      }
    };
    if (SwingUtilities.isEventDispatchThread()) {
      runnable.run();
    }
    else {
      SwingUtilities.invokeLater(runnable);
    }
  }

  public void setErrorText(final String text) {
    this.errorText = text;
    repaint();
  }

  public String getErrorText() {
    return this.errorText;
  }

  @Override
  public boolean isValid() {
    return this.model == null || this.model.getRoot() == null ? true : this.model.getRoot().getPayload() != null;
  }

  @Override
  public boolean isValidateRoot() {
    return true;
  }

  @Override
  public void invalidate() {
    super.invalidate();
    if (this.model != null && this.model.getRoot() != null) {
      this.model.getRoot().setPayload(null);
    }
  }

  @Override
  public void paintComponent(final Graphics g) {
    final Graphics2D gfx = (Graphics2D) g;

    final String error = this.errorText;

    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    gfx.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    gfx.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    gfx.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
    if (error != null) {
      final Font font = new Font(Font.DIALOG, Font.BOLD, 24);
      final FontMetrics metrics = gfx.getFontMetrics(font);
      final Rectangle2D textBounds = metrics.getStringBounds(error, g);
      gfx.setFont(font);
      gfx.setColor(Color.DARK_GRAY);
      final Dimension size = getSize();
      gfx.fillRect(0, 0, size.width, size.height);
      final int x = (int) (size.width - textBounds.getWidth()) / 2;
      final int y = (int) (size.height - textBounds.getHeight()) / 2;
      gfx.setColor(Color.BLACK);
      gfx.drawString(error, x + 5, y + 5);
      gfx.setColor(Color.RED.brighter());
      gfx.drawString(error, x, y);
    }
    else {
      if (!isValid()) {
        revalidateWholeTree(gfx, this.config, this.model);
      }
      drawOnGraphicsForConfiguration(gfx, this.config, this.model, true);
    }

    paintChildren(g);

    if (this.mouseDragSelection != null) {
      gfx.setColor(COLOR_MOUSE_DRAG_SELECTION);
      gfx.fill(this.mouseDragSelection.asRectangle());
    }
  }

  public AbstractElement findSubtreeUnderPoint(final Point point) {
    AbstractElement result = null;
    if (this.model != null) {

    }
    return result;
  }

  public AbstractElement findTopicUnderPoint(final Point point) {
    AbstractElement result = null;
    if (this.model != null) {
      final MindMapTopic root = this.model.getRoot();
      if (root != null) {
        final AbstractElement rootWidget = (AbstractElement) root.getPayload();
        if (rootWidget != null) {
          result = rootWidget.findForPoint(point);
        }
      }
    }

    return result;
  }

  void onConfigurationChanged() {

  }

}
