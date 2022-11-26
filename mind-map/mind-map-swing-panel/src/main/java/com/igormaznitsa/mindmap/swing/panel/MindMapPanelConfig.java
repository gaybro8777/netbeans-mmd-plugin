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

package com.igormaznitsa.mindmap.swing.panel;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.MiscUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.KeyShortcut;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;
import javax.swing.KeyStroke;
import org.apache.commons.lang3.SystemUtils;

public final class MindMapPanelConfig implements Serializable {

  public static final String KEY_ADD_CHILD_AND_START_EDIT = "addChildAndStartEdit";
  public static final String KEY_ADD_SIBLING_AND_START_EDIT = "addSiblingAndStartEdit";
  public static final String KEY_FOCUS_ROOT_OR_START_EDIT = "focusToRootOrStartEdit";
  public static final String KEY_CANCEL_EDIT = "cancelEdit";
  public static final String KEY_TOPIC_FOLD = "topicFold";
  public static final String KEY_TOPIC_FOLD_ALL = "topicFoldAll";
  public static final String KEY_TOPIC_UNFOLD = "topicUnfold";
  public static final String KEY_TOPIC_UNFOLD_ALL = "topicUnfoldAll";
  public static final String KEY_FOCUS_MOVE_UP = "moveFocusUp";
  public static final String KEY_FOCUS_MOVE_DOWN = "moveFocusDown";
  public static final String KEY_FOCUS_MOVE_LEFT = "moveFocusLeft";
  public static final String KEY_FOCUS_MOVE_RIGHT = "moveFocusRight";
  public static final String KEY_FOCUS_MOVE_UP_ADD_FOCUSED = "moveFocusUpAddFocused";
  public static final String KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED = "moveFocusDownAddFocused";
  public static final String KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED = "moveFocusLeftAddFocused";
  public static final String KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED = "moveFocusRightAddFocused";
  public static final String KEY_DELETE_TOPIC = "deleteSelectedTopic";
  public static final String KEY_TOPIC_TEXT_NEXT_LINE = "nextLineInTopicText";
  public static final String KEY_ZOOM_IN = "zoomIn";
  public static final String KEY_ZOOM_OUT = "zoomOut";
  public static final String KEY_ZOOM_RESET = "zoomReset";
  public static final String KEY_SHOW_POPUP = "showPopupMenu";

  private static final long serialVersionUID = -4273687011484460064L;
  private transient final List<WeakReference<MindMapConfigListener>> listeners =
      new ArrayList<>();
  private transient final Map<String, KeyShortcut> mapShortCut = new HashMap<>();
  private int collapsatorSize = 16;
  private int textMargins = 10;
  private int otherLevelVerticalInset = 16;
  private int otherLevelHorizontalInset = 32;
  private int firstLevelVerticalInset = 32;
  private int firstLevelHorizontalInset = 48;
  private int paperMargins = 20;
  private int selectLineGap = 5;
  private int horizontalBlockGap = 5;
  private int scaleModifiers = KeyEvent.CTRL_MASK;
  private boolean drawBackground = true;
  private Color paperColor = new Color(0x617B94);
  private Color gridColor = paperColor.darker();
  private boolean showGrid = true;
  private int gridStep = 32;
  private Color rootBackgroundColor = new Color(0x031A31);
  private Color firstLevelBackgroundColor = new Color(0xB1BFCC);
  private Color otherLevelBackgroundColor = new Color(0xFDFDFD);
  private Color rootTextColor = Color.WHITE;
  private Color firstLevelTextColor = Color.BLACK;
  private Color otherLevelTextColor = Color.BLACK;
  private Color elementBorderColor = Color.BLACK;
  private Color connectorColor = Color.WHITE;
  private Color shadowColor = new Color(0x30000000, true);
  private Color collapsatorBorderColor = Color.DARK_GRAY;
  private Color collapsatorBackgroundColor = Color.WHITE;
  private Color selectLineColor = Color.ORANGE;
  private Color jumpLinkColor = Color.CYAN;
  private float shadowOffset = 5.0f;
  private float elementBorderWidth = 1.0f;
  private float collapsatorBorderWidth = 1.0f;
  private float connectorWidth = 1.5f;
  private float selectLineWidth = 3.0f;
  private float jumpLinkWidth = 1.5f;
  private boolean smartTextPaste = false;
  private Font font;
  private double scale = 1.0d;
  private boolean dropShadow = true;
  private RenderQuality renderQuality = Utils.getDefaultRenderQialityForOs();
  private transient volatile boolean notificationEnabled = true;
  public MindMapPanelConfig(final MindMapPanelConfig cfg, final boolean copyListeners) {
    this();
    this.makeFullCopyOf(cfg, copyListeners, false);
  }

  public MindMapPanelConfig() {
    this.font = findDefaultFont(Font.BOLD, 18);
    if (SystemUtils.IS_OS_MAC) {
      // key map for MAC
      this.mapShortCut.put(KEY_ADD_CHILD_AND_START_EDIT,
          new KeyShortcut(KEY_ADD_CHILD_AND_START_EDIT, KeyEvent.VK_TAB, 0));
      this.mapShortCut.put(KEY_ADD_SIBLING_AND_START_EDIT,
          new KeyShortcut(KEY_ADD_SIBLING_AND_START_EDIT, KeyEvent.VK_ENTER, 0));
      this.mapShortCut
          .put(KEY_CANCEL_EDIT, new KeyShortcut(KEY_CANCEL_EDIT, KeyEvent.VK_ESCAPE, 0));
      this.mapShortCut.put(KEY_TOPIC_FOLD, new KeyShortcut(KEY_TOPIC_FOLD, KeyEvent.VK_MINUS, 0));
      this.mapShortCut.put(KEY_TOPIC_FOLD_ALL,
          new KeyShortcut(KEY_TOPIC_FOLD_ALL, KeyEvent.VK_MINUS, KeyEvent.ALT_MASK));
      this.mapShortCut
          .put(KEY_TOPIC_UNFOLD, new KeyShortcut(KEY_TOPIC_UNFOLD, KeyEvent.VK_EQUALS, 0));
      this.mapShortCut.put(KEY_TOPIC_UNFOLD_ALL,
          new KeyShortcut(KEY_TOPIC_UNFOLD_ALL, KeyEvent.VK_EQUALS, KeyEvent.ALT_MASK));
      this.mapShortCut.put(KEY_FOCUS_ROOT_OR_START_EDIT,
          new KeyShortcut(KEY_FOCUS_ROOT_OR_START_EDIT, KeyEvent.VK_SPACE, KeyEvent.ALT_MASK));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_DOWN, new KeyShortcut(KEY_FOCUS_MOVE_DOWN, KeyEvent.VK_DOWN, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_UP, new KeyShortcut(KEY_FOCUS_MOVE_UP, KeyEvent.VK_UP, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_LEFT, new KeyShortcut(KEY_FOCUS_MOVE_LEFT, KeyEvent.VK_LEFT, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_RIGHT, new KeyShortcut(KEY_FOCUS_MOVE_RIGHT, KeyEvent.VK_RIGHT, 0));
      this.mapShortCut.put(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED, KeyEvent.VK_DOWN, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_UP_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_UP_ADD_FOCUSED, KeyEvent.VK_UP, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED, KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED, KeyEvent.VK_RIGHT,
              KeyEvent.SHIFT_MASK));
      this.mapShortCut
          .put(KEY_DELETE_TOPIC, new KeyShortcut(KEY_DELETE_TOPIC, KeyEvent.VK_DELETE, 0));
      this.mapShortCut.put(KEY_TOPIC_TEXT_NEXT_LINE,
          new KeyShortcut(KEY_TOPIC_TEXT_NEXT_LINE, KeyEvent.VK_ENTER, KeyEvent.SHIFT_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_IN, new KeyShortcut(KEY_ZOOM_IN, KeyEvent.VK_EQUALS, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_OUT, new KeyShortcut(KEY_ZOOM_OUT, KeyEvent.VK_MINUS, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_RESET, new KeyShortcut(KEY_ZOOM_RESET, KeyEvent.VK_0, KeyEvent.CTRL_MASK));
      this.mapShortCut.put(KEY_SHOW_POPUP, new KeyShortcut(KEY_SHOW_POPUP, KeyEvent.VK_SPACE,
          KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK));
    } else {
      // key map for Linux and Windows
      this.mapShortCut.put(KEY_ADD_CHILD_AND_START_EDIT,
          new KeyShortcut(KEY_ADD_CHILD_AND_START_EDIT, KeyEvent.VK_TAB, 0));
      this.mapShortCut.put(KEY_ADD_SIBLING_AND_START_EDIT,
          new KeyShortcut(KEY_ADD_SIBLING_AND_START_EDIT, KeyEvent.VK_ENTER, 0));
      this.mapShortCut
          .put(KEY_CANCEL_EDIT, new KeyShortcut(KEY_CANCEL_EDIT, KeyEvent.VK_ESCAPE, 0));
      this.mapShortCut.put(KEY_TOPIC_FOLD, new KeyShortcut(KEY_TOPIC_FOLD, KeyEvent.VK_MINUS, 0));
      this.mapShortCut.put(KEY_TOPIC_FOLD_ALL,
          new KeyShortcut(KEY_TOPIC_FOLD_ALL, KeyEvent.VK_MINUS, KeyEvent.ALT_MASK));
      this.mapShortCut
          .put(KEY_TOPIC_UNFOLD, new KeyShortcut(KEY_TOPIC_UNFOLD, KeyEvent.VK_EQUALS, 0));
      this.mapShortCut.put(KEY_TOPIC_UNFOLD_ALL,
          new KeyShortcut(KEY_TOPIC_UNFOLD_ALL, KeyEvent.VK_EQUALS, KeyEvent.ALT_MASK));
      this.mapShortCut.put(KEY_FOCUS_ROOT_OR_START_EDIT,
          new KeyShortcut(KEY_FOCUS_ROOT_OR_START_EDIT, KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_DOWN, new KeyShortcut(KEY_FOCUS_MOVE_DOWN, KeyEvent.VK_DOWN, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_UP, new KeyShortcut(KEY_FOCUS_MOVE_UP, KeyEvent.VK_UP, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_LEFT, new KeyShortcut(KEY_FOCUS_MOVE_LEFT, KeyEvent.VK_LEFT, 0));
      this.mapShortCut
          .put(KEY_FOCUS_MOVE_RIGHT, new KeyShortcut(KEY_FOCUS_MOVE_RIGHT, KeyEvent.VK_RIGHT, 0));
      this.mapShortCut.put(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED, KeyEvent.VK_DOWN, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_UP_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_UP_ADD_FOCUSED, KeyEvent.VK_UP, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED, KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK));
      this.mapShortCut.put(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED,
          new KeyShortcut(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED, KeyEvent.VK_RIGHT,
              KeyEvent.SHIFT_MASK));
      this.mapShortCut
          .put(KEY_DELETE_TOPIC, new KeyShortcut(KEY_DELETE_TOPIC, KeyEvent.VK_DELETE, 0));
      this.mapShortCut.put(KEY_TOPIC_TEXT_NEXT_LINE,
          new KeyShortcut(KEY_TOPIC_TEXT_NEXT_LINE, KeyEvent.VK_ENTER, KeyEvent.SHIFT_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_IN, new KeyShortcut(KEY_ZOOM_IN, KeyEvent.VK_EQUALS, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_OUT, new KeyShortcut(KEY_ZOOM_OUT, KeyEvent.VK_MINUS, KeyEvent.CTRL_MASK));
      this.mapShortCut
          .put(KEY_ZOOM_RESET, new KeyShortcut(KEY_ZOOM_RESET, KeyEvent.VK_0, KeyEvent.CTRL_MASK));
      this.mapShortCut.put(KEY_SHOW_POPUP, new KeyShortcut(KEY_SHOW_POPUP, KeyEvent.VK_SPACE,
          KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK));
    }
  }

  public static Font findDefaultFont(final int style, final int size) {
    return Stream.of(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
        .filter(x -> x.startsWith("Fira Code"))
        .map(x -> new Font("Fira Code", style, size))
        .findFirst()
        .orElse(new Font(Font.SERIF, style, size));
  }

  public boolean isKeyEvent(final String id, final KeyEvent event,
                            final int modifiersMask) {
    final KeyShortcut shortCut = this.mapShortCut.get(id);
    return shortCut != null && shortCut.isEvent(event, modifiersMask);
  }

  public boolean isKeyEvent(final String id, final KeyEvent event) {
    return this.isKeyEvent(id, event, KeyShortcut.ALL_MODIFIERS_MASK);
  }

  public KeyShortcut getKeyShortCut(final String id) {
    return this.mapShortCut.get(id);
  }

  public void setKeyShortCut(final KeyShortcut shortCut) {
    this.mapShortCut.put(shortCut.getID(), shortCut);
  }

  @SettingsAccessor(name = "mapShortCut")
  public Map<String, KeyShortcut> getKeyShortcutMap() {
    return new HashMap<>(this.mapShortCut);
  }

  @SettingsAccessor(name = "mapShortCut")
  public void setKeyShortcutMap(final Map<String, KeyShortcut> map) {
    this.mapShortCut.clear();
    this.mapShortCut.putAll(map);
  }

  public boolean hasDifferenceInParameters(final MindMapPanelConfig etalon) {
    for (final Field f : MindMapPanelConfig.class.getDeclaredFields()) {
      if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
        continue;
      }
      try {
        final Object thisValue = f.get(this);
        final Object thatValue = f.get(etalon);

        if (thisValue == null && thatValue == null) {
          continue;
        }
        if (thisValue == null || !thisValue.equals(thatValue)) {
          return true;
        }

      } catch (IllegalAccessException ex) {
        throw new Error("IllegalAccessException [" + f.getName() + ']', ex);
      } catch (IllegalArgumentException ex) {
        throw new Error("IllegalArgumentException [" + f.getName() + ']', ex);
      }
    }

    final Map<String, KeyShortcut> thisShortcuts = this.mapShortCut;
    final Map<String, KeyShortcut> thatShortcuts = etalon.mapShortCut;

    if (thisShortcuts.size() != thatShortcuts.size()) {
      return true;
    }
    for (final Map.Entry<String, KeyShortcut> e : thisShortcuts.entrySet()) {
      if (!thatShortcuts.containsKey(e.getKey())) {
        return true;
      }
      if (!thatShortcuts.get(e.getKey()).equals(thisShortcuts.get(e.getKey()))) {
        return true;
      }
    }

    return false;
  }

  public Preferences saveTo(final Preferences prefs) {
    if (prefs != null) {
      final String prefix = MindMapPanelConfig.class.getSimpleName();

      for (final Field f : MindMapPanelConfig.class.getDeclaredFields()) {
        if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
          continue;
        }

        final Class<?> fieldClass = f.getType();
        final String fieldName = prefix + '.' + f.getName();

        try {
          if (fieldClass == boolean.class) {
            prefs.putBoolean(fieldName, f.getBoolean(this));
          } else if (fieldClass == int.class) {
            prefs.putInt(fieldName, f.getInt(this));
          } else if (fieldClass == float.class) {
            prefs.putFloat(fieldName, f.getFloat(this));
          } else if (fieldClass == double.class) {
            prefs.putDouble(fieldName, f.getDouble(this));
          } else if (fieldClass == Font.class) {
            final Font theFont = (Font) f.get(this);
            prefs.put(fieldName + ".name", theFont.getName());
            prefs.putInt(fieldName + ".size", theFont.getSize());
            prefs.putInt(fieldName + ".style", theFont.getStyle());
          } else if (fieldClass == Color.class) {
            prefs.putInt(fieldName, ((Color) f.get(this)).getRGB());
          } else if (fieldClass == String.class) {
            prefs.put(fieldName, (String) f.get(this));
          } else if (fieldClass == RenderQuality.class) {
            prefs.put(fieldName, ((RenderQuality) f.get(this)).name());
          } else {
            throw new Error("Unexpected field type " + fieldClass.getName());
          }
        } catch (IllegalAccessException ex) {
          throw new Error("IllegalAccessException [" + fieldClass.getName() + ']', ex);
        } catch (IllegalArgumentException ex) {
          throw new Error("IllegalArgumentException [" + fieldClass.getName() + ']', ex);
        }
      }

      for (final Map.Entry<String, KeyShortcut> e : this.mapShortCut.entrySet()) {
        prefs.put("mapShortCut." + e.getValue().getID(), e.getValue().packToString());
      }
    }
    return prefs;
  }

  public Preferences loadFrom(final Preferences prefs) {
    if (prefs != null) {
      final String prefix = MindMapPanelConfig.class.getSimpleName();

      final MindMapPanelConfig etalon = new MindMapPanelConfig();

      for (final Field f : MindMapPanelConfig.class.getDeclaredFields()) {
        if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT | Modifier.FINAL)) != 0) {
          continue;
        }

        final Class<?> fieldClass = f.getType();
        final String fieldName = prefix + '.' + f.getName();

        try {
          if (fieldClass == boolean.class) {
            f.setBoolean(this, prefs.getBoolean(fieldName, f.getBoolean(etalon)));
          } else if (fieldClass == int.class) {
            f.setInt(this, prefs.getInt(fieldName, f.getInt(etalon)));
          } else if (fieldClass == float.class) {
            f.setFloat(this, prefs.getFloat(fieldName, f.getFloat(etalon)));
          } else if (fieldClass == double.class) {
            f.setDouble(this, prefs.getDouble(fieldName, f.getDouble(etalon)));
          } else if (fieldClass == Font.class) {
            final Font etalonFont = etalon.getFont();

            final String fontName = prefs.get(fieldName + ".name", etalonFont.getName());
            final int fontSize = prefs.getInt(fieldName + ".size", etalonFont.getSize());
            final int fontStyle = prefs.getInt(fieldName + ".style", etalonFont.getStyle());

            f.set(this, new Font(fontName, fontStyle, fontSize));
          } else if (fieldClass == Color.class) {
            final int argb = prefs.getInt(fieldName, ((Color) f.get(etalon)).getRGB());
            f.set(this, new Color(argb, true));
          } else if (fieldClass == String.class) {
            f.set(this, prefs.get(fieldName, (String) f.get(etalon)));
          } else if (fieldClass == RenderQuality.class) {
            final String name = prefs.get(fieldName, ((RenderQuality) f.get(etalon)).name());
            f.set(this, RenderQuality.valueOf(name));
          } else {
            throw new Error("Unexpected field type " + fieldClass.getName());
          }
        } catch (IllegalAccessException ex) {
          throw new Error("IllegalAccessException [" + fieldClass.getName() + ']', ex);
        } catch (IllegalArgumentException ex) {
          throw new Error("IllegalArgumentException [" + fieldClass.getName() + ']', ex);
        }
      }
      this.mapShortCut.clear();
      this.mapShortCut.putAll(etalon.mapShortCut);
      try {
        for (final String k : prefs.keys()) {
          if (k.startsWith("mapShortCut.")) {
//            final int dotIndex = k.indexOf('.');
//            final String id = k.substring(dotIndex + 1);
            final String packedValue = prefs.get(k, "");
            if (packedValue.isEmpty()) {
              throw new Error("Unexpected situation, short cut value is empty [" + k + ']');
            }
            final KeyShortcut unpacked = new KeyShortcut(packedValue);
            this.mapShortCut.put(unpacked.getID(), unpacked);
          }
        }
      } catch (BackingStoreException ex) {
        throw new Error("Can't get list of keys from storage", ex);
      }
    }
    return prefs;
  }

  public void makeAtomicChange(final Runnable runnable) {
    this.notificationEnabled = false;
    try {
      runnable.run();
    } finally {
      this.notificationEnabled = true;
      notifyCfgListenersAboutChange();
    }
  }

  public float safeScaleFloatValue(final float value, final float minimal) {
    final float result = (float) (this.scale * (double) value);
    return Float.compare(result, minimal) >= 0 ? result : minimal;
  }

  public void makeFullCopyOf(final MindMapPanelConfig src, final boolean copyListeners,
                             final boolean makeNotification) {
    if (src != null) {
      for (final Field f : MindMapPanelConfig.class.getDeclaredFields()) {
        if (f.getName().equals("listeners")) {
          if (copyListeners) {
            this.listeners.clear();
            for (final WeakReference<MindMapConfigListener> weakContainer : src.listeners) {
              final MindMapConfigListener theListener = weakContainer.get();
              if (theListener != null) {
                this.listeners.add(new WeakReference<>(theListener));
              }
            }
          }
        } else if ((f.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0) {
          try {
            f.set(this, f.get(src));
          } catch (Exception ex) {
            throw new Error("Unexpected state during cloning field " + f, ex);
          }
        }
      }

      this.mapShortCut.clear();
      this.mapShortCut.putAll(src.mapShortCut);

      if (makeNotification) {
        this.notifyCfgListenersAboutChange();
      }
    }
  }

  public void addConfigurationListener(final MindMapConfigListener l) {
    this.listeners.add(new WeakReference<>(requireNonNull(l)));
  }

  public void removeConfigurationListener(final MindMapConfigListener l) {
    final Iterator<WeakReference<MindMapConfigListener>> iter = this.listeners.iterator();
    while (iter.hasNext()) {
      final WeakReference<MindMapConfigListener> wr = iter.next();
      final MindMapConfigListener c = wr.get();
      if (c == null || c == l) {
        iter.remove();
      }
    }
  }

  public boolean isShortcutConflict(final KeyStroke keyStroke) {
    boolean result = false;
    if (keyStroke != null) {
      for (final KeyShortcut s : this.mapShortCut.values()) {
        if (s.doesConflictWith(keyStroke)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  private void notifyCfgListenersAboutChange() {
    if (this.notificationEnabled) {
      for (final WeakReference<MindMapConfigListener> l : this.listeners) {
        final MindMapConfigListener c = l.get();
        if (c != null) {
          c.onConfigurationPropertyChanged(this);
        }
      }
    }
  }

  public boolean isKeyEventDetected(final KeyEvent event,
                                    final int effectiveModifiers,
                                    final String... shortCutIDs) {
    for (final String k : shortCutIDs) {
      final KeyShortcut shortCut = this.mapShortCut.get(k);
      if (shortCut != null && shortCut.isEvent(event, effectiveModifiers)) {
        return true;
      }
    }
    return false;
  }

  public boolean isKeyEventDetected(final KeyEvent event,
                                    final String... shortCutIDs) {
    for (final String k : shortCutIDs) {
      final KeyShortcut shortCut = this.mapShortCut.get(k);
      if (shortCut != null && shortCut.isEvent(event, KeyShortcut.ALL_MODIFIERS_MASK)) {
        return true;
      }
    }
    return false;
  }

  @SettingsAccessor(name = "horizontalBlockGap")
  public int getHorizontalBlockGap() {
    return this.horizontalBlockGap;
  }

  @SettingsAccessor(name = "horizontalBlockGap")
  public void setHorizontalBlockGap(final int gap) {
    this.horizontalBlockGap = gap;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "selectLineWidth")
  public float getSelectLineWidth() {
    return this.selectLineWidth;
  }

  @SettingsAccessor(name = "selectLineWidth")
  public void setSelectLineWidth(final float f) {
    this.selectLineWidth = f;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "jumpLinkWidth")
  public float getJumpLinkWidth() {
    return this.jumpLinkWidth;
  }

  @SettingsAccessor(name = "jumpLinkWidth")
  public void setJumpLinkWidth(final float f) {
    this.jumpLinkWidth = f;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "jumpLinkColor")
  public Color getJumpLinkColor() {
    return this.jumpLinkColor;
  }

  @SettingsAccessor(name = "jumpLinkColor")
  public void setJumpLinkColor(final Color color) {
    this.jumpLinkColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "scaleModifiers")
  public int getScaleModifiers() {
    return this.scaleModifiers;
  }

  @SettingsAccessor(name = "scaleModifiers")
  public void setScaleModifiers(final int value) {
    this.scaleModifiers = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "selectLineColor")
  public Color getSelectLineColor() {
    return this.selectLineColor;
  }

  @SettingsAccessor(name = "selectLineColor")
  public void setSelectLineColor(final Color color) {
    this.selectLineColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "paperMargins")
  public int getPaperMargins() {
    return this.paperMargins;
  }

  @SettingsAccessor(name = "paperMargins")
  public void setPaperMargins(final int size) {
    this.paperMargins = size;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "smartTextPaste")
  public boolean isSmartTextPaste() {
    return this.smartTextPaste;
  }

  @SettingsAccessor(name = "smartTextPaste")
  public void setSmartTextPaste(final boolean flag) {
    this.smartTextPaste = flag;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "drawBackground")
  public boolean isDrawBackground() {
    return this.drawBackground;
  }

  @SettingsAccessor(name = "drawBackground")
  public void setDrawBackground(final boolean flag) {
    this.drawBackground = flag;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "otherLevelVerticalInset")
  public int getOtherLevelVerticalInset() {
    return this.otherLevelVerticalInset;
  }

  @SettingsAccessor(name = "otherLevelVerticalInset")
  public void setOtherLevelVerticalInset(final int value) {
    this.otherLevelVerticalInset = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "otherLevelHorizontalInset")
  public int getOtherLevelHorizontalInset() {
    return this.otherLevelHorizontalInset;
  }

  @SettingsAccessor(name = "otherLevelHorizontalInset")
  public void setOtherLevelHorizontalInset(final int value) {
    this.otherLevelHorizontalInset = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "firstLevelVerticalInset")
  public int getFirstLevelVerticalInset() {
    return this.firstLevelVerticalInset;
  }

  @SettingsAccessor(name = "firstLevelVerticalInset")
  public void setFirstLevelVerticalInset(final int value) {
    this.firstLevelVerticalInset = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "firstLevelHorizontalInset")
  public int getFirstLevelHorizontalInset() {
    return this.firstLevelHorizontalInset;
  }

  @SettingsAccessor(name = "firstLevelHorizontalInset")
  public void setFirstLevelHorizontalInset(final int value) {
    this.firstLevelHorizontalInset = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "paperColor")
  public Color getPaperColor() {
    return this.paperColor;
  }

  @SettingsAccessor(name = "paperColor")
  public void setPaperColor(final Color color) {
    this.paperColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "gridColor")
  public Color getGridColor() {
    return this.gridColor;
  }

  @SettingsAccessor(name = "gridColor")
  public void setGridColor(final Color color) {
    this.gridColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "showGrid")
  public boolean isShowGrid() {
    return this.showGrid;
  }

  @SettingsAccessor(name = "showGrid")
  public void setShowGrid(final boolean flag) {
    this.showGrid = flag;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "gridStep")
  public int getGridStep() {
    return this.gridStep;
  }

  @SettingsAccessor(name = "gridStep")
  public void setGridStep(final int step) {
    this.gridStep = step;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "rootBackgroundColor")
  public Color getRootBackgroundColor() {
    return this.rootBackgroundColor;
  }

  @SettingsAccessor(name = "rootBackgroundColor")
  public void setRootBackgroundColor(final Color color) {
    this.rootBackgroundColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "firstLevelBackgroundColor")
  public Color getFirstLevelBackgroundColor() {
    return this.firstLevelBackgroundColor;
  }

  @SettingsAccessor(name = "firstLevelBackgroundColor")
  public void setFirstLevelBackgroundColor(final Color color) {
    this.firstLevelBackgroundColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "otherLevelBackgroundColor")
  public Color getOtherLevelBackgroundColor() {
    return this.otherLevelBackgroundColor;
  }

  @SettingsAccessor(name = "otherLevelBackgroundColor")
  public void setOtherLevelBackgroundColor(final Color color) {
    this.otherLevelBackgroundColor = color;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "rootTextColor")
  public Color getRootTextColor() {
    return this.rootTextColor;
  }

  @SettingsAccessor(name = "rootTextColor")
  public void setRootTextColor(final Color color) {
    this.rootTextColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "firstLevelTextColor")
  public Color getFirstLevelTextColor() {
    return this.firstLevelTextColor;
  }

  @SettingsAccessor(name = "firstLevelTextColor")
  public void setFirstLevelTextColor(final Color color) {
    this.firstLevelTextColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "otherLevelTextColor")
  public Color getOtherLevelTextColor() {
    return this.otherLevelTextColor;
  }

  @SettingsAccessor(name = "otherLevelTextColor")
  public void setOtherLevelTextColor(final Color color) {
    this.otherLevelTextColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "elementBorderColor")
  public Color getElementBorderColor() {
    return this.elementBorderColor;
  }

  @SettingsAccessor(name = "elementBorderColor")
  public void setElementBorderColor(final Color color) {
    this.elementBorderColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "connectorColor")
  public Color getConnectorColor() {
    return this.connectorColor;
  }

  @SettingsAccessor(name = "connectorColor")
  public void setConnectorColor(final Color color) {
    this.connectorColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "shadowColor")
  public Color getShadowColor() {
    return this.shadowColor;
  }

  @SettingsAccessor(name = "shadowColor")
  public void setShadowColor(final Color color) {
    this.shadowColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "collapsatorBorderColor")
  public Color getCollapsatorBorderColor() {
    return this.collapsatorBorderColor;
  }

  @SettingsAccessor(name = "collapsatorBorderColor")
  public void setCollapsatorBorderColor(final Color color) {
    this.collapsatorBorderColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "collapsatorBackgroundColor")
  public Color getCollapsatorBackgroundColor() {
    return this.collapsatorBackgroundColor;
  }

  @SettingsAccessor(name = "collapsatorBackgroundColor")
  public void setCollapsatorBackgroundColor(final Color color) {
    this.collapsatorBackgroundColor = requireNonNull(color);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "elementBorderWidth")
  public float getElementBorderWidth() {
    return this.elementBorderWidth;
  }

  @SettingsAccessor(name = "elementBorderWidth")
  public void setElementBorderWidth(final float value) {
    this.elementBorderWidth = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "collapsatorBorderWidth")
  public float getCollapsatorBorderWidth() {
    return this.collapsatorBorderWidth;
  }

  @SettingsAccessor(name = "collapsatorBorderWidth")
  public void setCollapsatorBorderWidth(final float width) {
    this.collapsatorBorderWidth = width;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "shadowOffset")
  public float getShadowOffset() {
    return this.shadowOffset;
  }

  @SettingsAccessor(name = "shadowOffset")
  public void setShadowOffset(final float value) {
    this.shadowOffset = value;
  }

  @SettingsAccessor(name = "connectorWidth")
  public float getConnectorWidth() {
    return this.connectorWidth;
  }

  @SettingsAccessor(name = "connectorWidth")
  public void setConnectorWidth(final float value) {
    this.connectorWidth = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "font")
  public Font getFont() {
    return this.font;
  }

  @SettingsAccessor(name = "font")
  public void setFont(final Font f) {
    this.font = requireNonNull(f);
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "scale")
  public double getScale() {
    return this.scale;
  }

  @SettingsAccessor(name = "scale")
  public void setScale(final double value) {
    this.scale = Math.max(0.01d, value);
    notifyCfgListenersAboutChange();
  }

  public void setScaleWithoutListenerNotification(final double value) {
    this.scale = Math.max(0.01d, value);
  }

  @SettingsAccessor(name = "dropShadow")
  public boolean isDropShadow() {
    return this.dropShadow;
  }

  @SettingsAccessor(name = "dropShadow")
  public void setDropShadow(final boolean value) {
    this.dropShadow = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "renderQuality")
  public RenderQuality getRenderQuality() {
    return this.renderQuality;
  }

  @SettingsAccessor(name = "renderQuality")
  public void setRenderQuality(final RenderQuality value) {
    this.renderQuality = MiscUtils.ensureNotNull(value, Utils.getDefaultRenderQialityForOs());
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "collapsatorSize")
  public int getCollapsatorSize() {
    return this.collapsatorSize;
  }

  @SettingsAccessor(name = "collapsatorSize")
  public void setCollapsatorSize(final int size) {
    this.collapsatorSize = size;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "textMargins")
  public int getTextMargins() {
    return this.textMargins;
  }

  @SettingsAccessor(name = "textMargins")
  public void setTextMargins(final int value) {
    this.textMargins = value;
    notifyCfgListenersAboutChange();
  }

  @SettingsAccessor(name = "selectLineGap")
  public int getSelectLineGap() {
    return this.selectLineGap;
  }

  @SettingsAccessor(name = "selectLineGap")
  public void setSelectLineGap(final int value) {
    this.selectLineGap = value;
    notifyCfgListenersAboutChange();
  }

}
