/*
 * Copyright (C) 2015-2023 Igor A. Maznitsa
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

package com.igormaznitsa.mindmap.plugins.exporters;

import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_BORDER_COLOR;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_FILL_COLOR;
import static com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute.ATTR_TEXT_COLOR;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.StandardTopicAttributes;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.ui.TextAlign;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.MiscIcons;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;

public class PUMLExporter extends AbstractExporter {

  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_PUML);

  private static final String EOL = "\n";

  private Color extractColor(final Topic topic, final StandardTopicAttribute color,
                             final Color defaultColor) {
    final String attributeValue = topic.getAttribute(color.getText());
    if (attributeValue == null) {
      return defaultColor;
    } else {
      return Utils.html2color(attributeValue, false);
    }
  }

  private String float2str(final float value) {
    return DecimalFormat.getInstance(Locale.ENGLISH).format(value);
  }

  private String makeContent(final PluginContext pluginContext) {
    final MindMapPanel panel = pluginContext.getPanel();
    final MindMapPanelConfig config = panel.getConfiguration();
    final MindMap map = panel.getModel();

    final List<StyleItem> styles = new ArrayList<>();
    final Set<String> emoticons = new HashSet<>();
    final Map<Topic, String> images = new IdentityHashMap<>();

    int styleCounter = 0;
    for (final Topic t : map) {
      final Color textColor;
      final Color borderColor;
      final Color backColor;
      final TextAlign textAlign = TextAlign.findForName(
          t.getAttribute(StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_TITLE_ALIGN));

      final int level = t.getPath().length;
      if (level == 1) {
        borderColor = extractColor(t, ATTR_BORDER_COLOR, config.getElementBorderColor());
        textColor = extractColor(t, ATTR_TEXT_COLOR, config.getRootTextColor());
        backColor = extractColor(t, ATTR_FILL_COLOR, config.getRootBackgroundColor());
      } else if (level == 2) {
        borderColor = extractColor(t, ATTR_BORDER_COLOR, config.getElementBorderColor());
        textColor = extractColor(t, ATTR_TEXT_COLOR, config.getFirstLevelTextColor());
        backColor = extractColor(t, ATTR_FILL_COLOR, config.getFirstLevelBackgroundColor());
      } else {
        borderColor = extractColor(t, ATTR_BORDER_COLOR, config.getElementBorderColor());
        textColor = extractColor(t, ATTR_TEXT_COLOR, config.getOtherLevelTextColor());
        backColor = extractColor(t, ATTR_FILL_COLOR, config.getOtherLevelBackgroundColor());
      }

      StyleItem styleItem =
          styles.stream().filter(x -> x.match(textColor, backColor, borderColor, textAlign))
              .findFirst().orElse(null);
      if (styleItem == null) {
        styleCounter++;
        final boolean defaultBorderColor = config.getElementBorderColor().equals(borderColor);
        styles.add(
            new StyleItem("style" + styleCounter, textColor, backColor, borderColor, textAlign,
                !defaultBorderColor));
      }

      final String emoticon = t.getAttribute(StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_EMOTICON);
      if (emoticon != null) {
        emoticons.add(emoticon);
      }
      final String image = t.getAttribute(StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_IMAGE_DATA);
      if (image != null) {
        images.put(t, image);
      }
    }

    final StringBuilder buffer = new StringBuilder();
    buffer.append("@startmindmap").append(EOL);

    buffer.append("skinparam {").append(EOL)
        .append("  shadowing ").append(config.isDropShadow())
        .append(EOL)
        .append("  BackgroundColor ").append(Utils.color2html(config.getPaperColor(), false))
        .append(EOL)
        .append("  ArrowColor ").append(Utils.color2html(config.getConnectorColor(), false))
        .append(EOL)
        .append("  ArrowThickness ").append(float2str(config.getConnectorWidth())).append(EOL)
        .append("  RoundCorner 0").append(EOL)
        .append("  NodeBorderThickness ").append(float2str(config.getConnectorWidth())).append(EOL)
        .append("  NodeBorderColor ")
        .append(Utils.color2html(config.getElementBorderColor(), false)).append(EOL)
        .append('}').append(EOL);

    buffer.append("<style>").append(EOL);
    styles.forEach(x -> x.writeStyle(buffer));
    buffer.append(EOL).append("</style>").append(EOL);

    emoticons.forEach(e -> {
      final RenderedImage image = (RenderedImage) MiscIcons.findForName(e);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
        if (!ImageIO.write(Objects.requireNonNull(image), "png", out)) {
          throw new IllegalStateException("Can't find PNG encoder");
        }
        final String base64 = Base64.getEncoder().encodeToString(out.toByteArray());
        buffer.append("!$emoticon_").append(e).append("=\"<img data:image/png;base64,")
            .append(base64).append(">\"").append(EOL);
      } catch (Exception ex) {
        LOGGER.error("Can't encode emoticon: " + e, ex);
      }
    });

    final Map<Topic, String> mapImageId = new IdentityHashMap<>();
    final AtomicInteger imageCounter = new AtomicInteger(0);
    images.forEach((t, i) -> {
      final String id = "$image_" + imageCounter.incrementAndGet();
      buffer.append('!').append(id).append("=\"<img data:image/png;base64,")
          .append(i).append(">\"").append(EOL);
      mapImageId.put(t, id);
    });
    images.clear();

    map.forEach(x -> writeTopic(buffer, x, mapImageId, config, styles));

    buffer.append("@endmindmap");

    return buffer.toString();
  }

  private void writeTopic(
      final StringBuilder buffer, final Topic topic,
      final Map<Topic, String> imageMap,
      final MindMapPanelConfig config,
      final List<StyleItem> styles
  ) {
    final int level = topic.getPath().length;
    final char prefix;
    if (level > 1) {
      final Topic firstLevel = topic.getPath()[1];
      prefix =
          "true".equals(firstLevel.getAttribute(StandardTopicAttribute.ATTR_LEFTSIDE.getText())) ?
              '-' : '+';
    } else {
      prefix = '+';
    }

    final String emoticon =
        topic.getAttribute(StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_EMOTICON);
    final String image = imageMap.get(topic);

    final Color borderColor;
    final Color textColor;
    final Color backColor;
    if (level == 1) {
      borderColor = extractColor(topic, ATTR_BORDER_COLOR, config.getElementBorderColor());
      textColor = extractColor(topic, ATTR_TEXT_COLOR, config.getRootTextColor());
      backColor = extractColor(topic, ATTR_FILL_COLOR, config.getRootBackgroundColor());
    } else if (level == 2) {
      borderColor = extractColor(topic, ATTR_BORDER_COLOR, config.getElementBorderColor());
      textColor = extractColor(topic, ATTR_TEXT_COLOR, config.getFirstLevelTextColor());
      backColor = extractColor(topic, ATTR_FILL_COLOR, config.getFirstLevelBackgroundColor());
    } else {
      borderColor = extractColor(topic, ATTR_BORDER_COLOR, config.getElementBorderColor());
      textColor = extractColor(topic, ATTR_TEXT_COLOR, config.getOtherLevelTextColor());
      backColor = extractColor(topic, ATTR_FILL_COLOR, config.getOtherLevelBackgroundColor());
    }
    final TextAlign textAlign = TextAlign.findForName(
        topic.getAttribute(StandardTopicAttributes.MMD_TOPIC_ATTRIBUTE_TITLE_ALIGN));

    final StyleItem styleItem =
        styles.stream().filter(x -> x.match(textColor, backColor, borderColor, textAlign))
            .findFirst()
            .orElseThrow(() -> new Error(
                "Impossible situation, can't find any style for topic record"));

    IntStream.range(0, level).forEach(x -> buffer.append(prefix));
    if (emoticon != null) {
      buffer.append(" $emoticon_").append(emoticon).append(' ');
    }
    if (image != null) {
      buffer.append(' ').append(image).append(' ');
    }
    buffer.append(' ').append(escapePlantUml(topic.getText())).append(" <<")
        .append(styleItem.getUid())
        .append(">>").append(EOL);
  }

  private String escapePlantUml(final String text) {
    final StringBuilder result = new StringBuilder();
    for (final char c : text.toCharArray()) {
      switch (c) {
        case '{':
          result.append("&#123;");
          break;
        case '|':
          result.append("&#124;");
          break;
        case '}':
          result.append("&#125;");
          break;
        case '<':
          result.append("&#60;");
          break;
        case '>':
          result.append("&#62;");
          break;
        case '$':
          result.append("&#36;");
          break;
        case '#':
          result.append("&#35;");
          break;
        case '&':
          result.append("&#38;");
          break;
        case '~':
          result.append("&#126;");
          break;
        case '\n':
          result.append("\\n");
          break;
        case '\t':
          result.append("\\t");
          break;
        case '\r':
          result.append("\\r");
          break;
        default: {
          result.append(c);
        }
        break;
      }
    }
    return result.toString();
  }

  @Override
  public void doExportToClipboard(final PluginContext context,
                                  final Set<AbstractParameter<?>> options)
      throws IOException {
    final String text = makeContent(context);
    SwingUtilities.invokeLater(() -> {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      if (clipboard != null) {
        clipboard.setContents(new StringSelection(text), null);
      }
    });
  }

  @Override
  public void doExport(final PluginContext context, final Set<AbstractParameter<?>> options,
                       final OutputStream out) throws IOException {
    final String text = makeContent(context);

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          this.getResourceBundle().getString("PumlExporter.saveDialogTitle"),
          null,
          ".puml",
          this.getResourceBundle().getString("PumlExporter.filterDescription"),
          this.getResourceBundle().getString("PumlExporter.approveButtonText"));
      fileToSaveMap =
          MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".puml");
      theOut = fileToSaveMap == null ? null :
          new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(text, theOut, StandardCharsets.UTF_8);
      } finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  @Override
  public String getMnemonic() {
    return "puml";
  }

  @Override
  public String getName(final PluginContext context, final Topic actionTopic) {
    return this.getResourceBundle().getString("PumlExporter.exporterName");
  }

  @Override
  public String getReference(final PluginContext context, final Topic actionTopic) {
    return this.getResourceBundle().getString("PumlExporter.exporterReference");
  }

  @Override
  public Icon getIcon(final PluginContext context, final Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 3;
  }

  private static final class StyleItem {

    private final Color textColor;
    private final Color backColor;
    private final Color borderColor;

    private final TextAlign textAlign;
    private final String uid;

    private final boolean writeBorderColor;

    private StyleItem(final String uid, final Color textColor, final Color backColor,
                      final Color borderColor, final TextAlign textAlign,
                      final boolean writeBorderColor) {
      this.uid = uid;
      this.textColor = textColor;
      this.backColor = backColor;
      this.borderColor = borderColor;
      this.textAlign = textAlign;
      this.writeBorderColor = writeBorderColor;
    }

    public String getUid() {
      return this.uid;
    }

    public void writeStyle(final StringBuilder builder) {
      builder.append(".").append(this.uid).append(" {").append(EOL);
      builder.append("  BackgroundColor ").append(Utils.color2html(this.backColor, false))
          .append(EOL);
      builder.append("  FontColor ").append(Utils.color2html(this.textColor, false)).append(EOL);

      if (this.writeBorderColor) {
        builder.append("  LineColor ").append(Utils.color2html(this.borderColor, false))
            .append(EOL);
      }

      builder.append("  HorizontalAlignment ");
      switch (this.textAlign) {
        case CENTER:
          builder.append("center");
          break;
        case LEFT:
          builder.append("left");
          break;
        case RIGHT:
          builder.append("right");
          break;
      }
      builder.append(EOL);
      builder.append("}").append(EOL);
    }

    public boolean match(final Color textColor, final Color backColor, final Color borderColor,
                         final TextAlign textAlign) {
      return Objects.equals(this.textColor, textColor)
          && Objects.equals(this.backColor, backColor)
          && Objects.equals(this.borderColor, borderColor)
          && this.textAlign == textAlign;
    }
  }
}
