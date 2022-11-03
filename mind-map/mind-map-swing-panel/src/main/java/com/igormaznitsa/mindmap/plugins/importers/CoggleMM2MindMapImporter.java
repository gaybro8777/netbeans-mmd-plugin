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

package com.igormaznitsa.mindmap.plugins.importers;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractImporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.attributes.images.ImageVisualAttributePlugin;
import com.igormaznitsa.mindmap.swing.panel.StandardTopicAttribute;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.ui.AbstractCollapsableElement;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class CoggleMM2MindMapImporter extends AbstractImporter {

  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_IMPORT_COGGLE2MM);

  private static final Logger LOGGER = LoggerFactory.getLogger(CoggleMM2MindMapImporter.class);
  private static final Pattern MD_IMAGE_LINK =
      Pattern.compile("\\!\\[(.*?)\\]\\((.*?)\\)", Pattern.MULTILINE | Pattern.UNICODE_CASE);
  private static final Pattern MD_URL_LINK =
      Pattern.compile("(?<!\\!)\\[(.*?)\\]\\((.*?)\\)", Pattern.MULTILINE | Pattern.UNICODE_CASE);

  private static String loadImageForURLAndEncode(final String imageUrl) {
    String result = null;

    final Image loadedImage;
    try {
      loadedImage = ImageIO.read(new URL(imageUrl));
    } catch (final Exception ex) {
      LOGGER.error("Can't load image for URL : " + imageUrl, ex);
      return null;
    }

    if (loadedImage != null) {
      try {
        result = Utils.rescaleImageAndEncodeAsBase64(loadedImage, -1);
      } catch (final Exception ex) {
        LOGGER.error("Can't decode image", ex);
      }
    }

    return result;
  }

  private static String loadFirstSuccessfulImage(
      final List<String> urls) {
    String result = null;
    for (final String url : urls) {
      result = loadImageForURLAndEncode(url);
      if (result != null) {
        break;
      }
    }
    return result;
  }

  private static MMapURI getFirstSuccessfulURL(
      final List<String> urls) {
    MMapURI result = null;
    for (final String url : urls) {
      try {
        result = new MMapURI(url);
      } catch (final Exception ex) {
        LOGGER.error("Can't recognize URI : " + url, ex);
      }
      if (result != null) {
        break;
      }
    }
    return result;
  }

  @Override
  public MindMap doImport(final PluginContext context) throws Exception {
    final File file = this.selectFileForExtension(context,
        Texts.getString("MMDImporters.CoggleMM2MindMap.openDialogTitle"), null, "mm",
        "Coggle MM files (.MM)", Texts.getString("MMDImporters.ApproveImport"));

    if (file == null) {
      return null;
    }

    return doImportFile(file);
  }

  MindMap doImportFile(final File file)
      throws SAXException, IOException, ParserConfigurationException {

    final Document document;
    try (final FileInputStream in = new FileInputStream(file)) {
      document = Utils.loadXmlDocument(in, "UTF-8", true);
    }

    final MindMap result = new MindMap(true);
    requireNonNull(result.getRoot()).setText("Empty");

    final Element root = document.getDocumentElement();
    if ("map".equals(root.getTagName())) {
      final List<Element> nodes = Utils.findDirectChildrenForName(root, "node");
      if (!nodes.isEmpty()) {
        parseTopic(result, null, result.getRoot(), nodes.get(0));
      }
    } else {
      throw new IllegalArgumentException("File is not Coggle mind map");
    }
    return result;
  }

  private List<String> extractImageURLs(final String mdText,
                                        final StringBuilder resultText) {
    final List<String> result = new ArrayList<>();
    final Matcher matcher = MD_IMAGE_LINK.matcher(mdText);
    int lastFoundEnd = 0;
    while (matcher.find()) {
      final String text = matcher.group(1);
      result.add(matcher.group(2));
      resultText.append(mdText, lastFoundEnd, matcher.start()).append(text);
      lastFoundEnd = matcher.end();
    }

    if (lastFoundEnd < mdText.length()) {
      resultText.append(mdText, lastFoundEnd, mdText.length());
    }

    return result;
  }

  private List<String> extractURLs(final String mdText,
                                   final StringBuilder resultText) {
    final List<String> result = new ArrayList<>();
    final Matcher matcher = MD_URL_LINK.matcher(mdText);
    int lastFoundEnd = 0;
    while (matcher.find()) {
      final String text = matcher.group(1);
      result.add(matcher.group(2));
      resultText.append(mdText, lastFoundEnd, matcher.start()).append(text);
      lastFoundEnd = matcher.end();
    }

    if (lastFoundEnd < mdText.length()) {
      resultText.append(mdText, lastFoundEnd, mdText.length());
    }

    return result;
  }

  private void parseTopic(final MindMap map, final Topic parent,
                          final Topic preGeneratedOne, final Element element) {
    final Topic topicToProcess;
    if (preGeneratedOne == null) {
      topicToProcess = requireNonNull(parent).makeChild("", null);
    } else {
      topicToProcess = preGeneratedOne;
    }

    final StringBuilder resultTextBuffer = new StringBuilder();
    final List<String> foundImageURLs =
        extractImageURLs(element.getAttribute("TEXT"), resultTextBuffer);
    String nodeText = resultTextBuffer.toString();
    resultTextBuffer.setLength(0);

    final List<String> foundLinkURLs = extractURLs(nodeText, resultTextBuffer);
    final MMapURI succesfullDecodedUrl = getFirstSuccessfulURL(foundLinkURLs);

    nodeText = resultTextBuffer.toString();

    final String encodedImage = loadFirstSuccessfulImage(foundImageURLs);
    if (encodedImage != null) {
      topicToProcess.putAttribute(ImageVisualAttributePlugin.ATTR_KEY, encodedImage);
    }

    if (succesfullDecodedUrl != null) {
      topicToProcess.setExtra(new ExtraLink(succesfullDecodedUrl));
    }

    final StringBuilder note = new StringBuilder();

    if (!foundLinkURLs.isEmpty() && (succesfullDecodedUrl == null || foundLinkURLs.size() > 1)) {
      if (note.length() > 0) {
        note.append("\n\n");
      }
      note.append("Detected URLs\n---------------");
      for (final String u : foundLinkURLs) {
        note.append('\n').append(u);
      }
    }

    if (!foundImageURLs.isEmpty() && (encodedImage == null || foundImageURLs.size() > 1)) {
      if (note.length() > 0) {
        note.append("\n\n");
      }
      note.append("Detected image links\n---------------");
      for (final String u : foundImageURLs) {
        note.append('\n').append(u);
      }
    }

    final String text = nodeText.replace("\r", "");
    final String position = element.getAttribute("POSITION");
    final String folded = element.getAttribute("FOLDED");

    Color edgeColor = null;
    for (final Element e : Utils.findDirectChildrenForName(element, "edge")) {
      try {
        edgeColor = Utils.html2color(e.getAttribute("COLOR"), false);
      } catch (final Exception ex) {
        LOGGER.error("Can't parse color value", ex);
      }
    }

    topicToProcess.setText(text);

    if (parent != null && parent.isRoot() && "left".equalsIgnoreCase(position)) {
      AbstractCollapsableElement.makeTopicLeftSided(topicToProcess, true);
    }

    if ("true".equalsIgnoreCase(folded)) {
      MindMapUtils.setCollapsed(topicToProcess, true);
    }

    if (edgeColor != null) {
      topicToProcess.putAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(),
          Utils.color2html(edgeColor, false));
      topicToProcess.putAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(),
          Utils.color2html(Utils.makeContrastColor(edgeColor), false));
    }

    if (note.length() > 0) {
      topicToProcess.setExtra(new ExtraNote(note.toString()));
    }

    for (final Element c : Utils.findDirectChildrenForName(element, "node")) {
      parseTopic(map, topicToProcess, null, c);
    }
  }

  @Override
  public String getMnemonic() {
    return "cogglemm";
  }

  @Override
  public String getName(final PluginContext context) {
    return Texts.getString("MMDImporters.CoggleMM2MindMap.Name");
  }

  @Override
  public String getReference(final PluginContext context) {
    return Texts.getString("MMDImporters.CoggleMM2MindMap.Reference");
  }

  @Override
  public Icon getIcon(final PluginContext context) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 5;
  }

  @Override
  public boolean isCompatibleWithFullScreenMode() {
    return false;
  }
}
