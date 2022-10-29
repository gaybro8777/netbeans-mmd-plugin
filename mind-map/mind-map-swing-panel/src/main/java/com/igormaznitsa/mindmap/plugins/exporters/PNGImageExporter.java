/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not usne this file except in compliance with the License.
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

package com.igormaznitsa.mindmap.plugins.exporters;

import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.HasOptions;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanel;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.Texts;
import com.igormaznitsa.mindmap.swing.panel.utils.ImageSelection;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactory;
import com.igormaznitsa.mindmap.swing.services.UIComponentFactoryProvider;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;

public final class PNGImageExporter extends AbstractExporter {

  private static final Logger LOGGER = LoggerFactory.getLogger(PNGImageExporter.class);
  private static final UIComponentFactory UI_FACTORY = UIComponentFactoryProvider.findInstance();
  private static final Icon ICO = ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_PNG);
  private boolean flagExpandAllNodes = false;
  private boolean flagDrawBackground = true;


  public PNGImageExporter() {
    super();
  }

  @Override
  public JComponent makeOptions(final PluginContext context) {
    final Options options = new Options(flagExpandAllNodes, flagDrawBackground);

    final JPanel panel = UI_FACTORY.makePanelWithOptions(options);
    final JCheckBox checkBoxExpandAll = UI_FACTORY.makeCheckBox();
    checkBoxExpandAll.setSelected(flagExpandAllNodes);
    checkBoxExpandAll.setText(Texts.getString("PNGImageExporter.optionUnfoldAll"));
    checkBoxExpandAll.setActionCommand("unfold");

    final JCheckBox checkBoxDrawBackground = UI_FACTORY.makeCheckBox();
    checkBoxDrawBackground.setSelected(flagDrawBackground);
    checkBoxDrawBackground.setText(Texts.getString("PNGImageExporter.optionDrawBackground"));
    checkBoxDrawBackground.setActionCommand("back");

    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    panel.add(checkBoxExpandAll);
    panel.add(checkBoxDrawBackground);

    panel.setBorder(BorderFactory.createEmptyBorder(16, 32, 16, 32));

    final ActionListener actionListener = e -> {
      if (e.getSource() == checkBoxExpandAll) {
        options.setOption(Options.KEY_EXPAND_ALL, Boolean.toString(checkBoxExpandAll.isSelected()));
      }
      if (e.getSource() == checkBoxDrawBackground) {
        options.setOption(Options.KEY_DRAW_BACK,
            Boolean.toString(checkBoxDrawBackground.isSelected()));
      }
    };

    checkBoxExpandAll.addActionListener(actionListener);
    checkBoxDrawBackground.addActionListener(actionListener);

    return panel;
  }

  private BufferedImage makeImage(final PluginContext context, final JComponent options) {
    if (options instanceof HasOptions) {
      final HasOptions opts = (HasOptions) options;
      this.flagExpandAllNodes = Boolean.parseBoolean(opts.getOption(Options.KEY_EXPAND_ALL));
      this.flagDrawBackground = Boolean.parseBoolean(opts.getOption(Options.KEY_DRAW_BACK));
    } else {
      for (final Component compo : requireNonNull(options).getComponents()) {
        if (compo instanceof JCheckBox) {
          final JCheckBox cb = (JCheckBox) compo;
          if ("unfold".equalsIgnoreCase(cb.getActionCommand())) {
            this.flagExpandAllNodes = cb.isSelected();
          } else if ("back".equalsIgnoreCase(cb.getActionCommand())) {
            this.flagDrawBackground = cb.isSelected();
          }
        }
      }
    }

    final MindMapPanelConfig newConfig = new MindMapPanelConfig(context.getPanelConfig(), false);
    newConfig.setDrawBackground(this.flagDrawBackground);
    newConfig.setScale(1.0f);

    return MindMapPanel.renderMindMapAsImage(context.getPanel().getModel(), newConfig, flagExpandAllNodes, RenderQuality.QUALITY);
  }

  @Override
  public void doExportToClipboard(final PluginContext context, final JComponent options)
      throws IOException {
    final BufferedImage image = makeImage(context, options);
    if (image != null) {
      SwingUtilities.invokeLater(() -> {
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (clipboard != null) {
          clipboard.setContents(new ImageSelection(image), null);
        }
      });
    }
  }

  @Override
  public void doExport(final PluginContext context, final JComponent options,
                       final OutputStream out) throws IOException {
    final RenderedImage image = makeImage(context, options);

    if (image == null) {
      if (out == null) {
        LOGGER.error("Can't render map as image");
        context.getDialogProvider()
            .msgError(null, Texts.getString("PNGImageExporter.msgErrorDuringRendering"));
        return;
      } else {
        throw new IOException("Can't render image");
      }
    }

    final ByteArrayOutputStream buff = new ByteArrayOutputStream(128000);
    ImageIO.write(image, "png", buff);//NOI18N

    final byte[] imageData = buff.toByteArray();

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          Texts.getString("PNGImageExporter.saveDialogTitle"),
          null,
          ".png",
          Texts.getString("PNGImageExporter.filterDescription"),
          Texts.getString("PNGImageExporter.approveButtonText"));
      fileToSaveMap = MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".png");//NOI18N
      theOut = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(imageData, theOut);
      } finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  @Override
  public String getMnemonic() {
    return "png";
  }

  @Override
  public String getName(final PluginContext context, Topic actionTopic) {
    return Texts.getString("PNGImageExporter.exporterName");
  }

  @Override
  public String getReference(final PluginContext context, Topic actionTopic) {
    return Texts.getString("PNGImageExporter.exporterReference");
  }

  @Override
  public Icon getIcon(final PluginContext context, Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 4;
  }

  private static class Options implements HasOptions {

    private static final String KEY_EXPAND_ALL = "expand.all";
    private static final String KEY_DRAW_BACK = "draw.back";
    private boolean expandAll;
    private boolean drawBack;

    private Options(final boolean expandAllNodes, final boolean drawBackground) {
      this.expandAll = expandAllNodes;
      this.drawBack = drawBackground;
    }

    @Override
    public boolean doesSupportKey(final String key) {
      return KEY_DRAW_BACK.equals(key) || KEY_EXPAND_ALL.equals(key);
    }

    @Override
    public String[] getOptionKeys() {
      return new String[] {KEY_EXPAND_ALL, KEY_DRAW_BACK};
    }

    @Override
    public String getOptionKeyDescription(final String key) {
      if (KEY_DRAW_BACK.equals(key)) {
        return "Draw background";
      }
      if (KEY_EXPAND_ALL.equals(key)) {
        return "Unfold all topics";
      }
      return "";
    }

    @Override
    public void setOption(final String key, final String value) {
      if (KEY_DRAW_BACK.equals(key)) {
        this.drawBack = Boolean.parseBoolean(value);
      } else if (KEY_EXPAND_ALL.equals(key)) {
        this.expandAll = Boolean.parseBoolean(value);
      }
    }

    @Override
    public String getOption(final String key) {
      if (KEY_DRAW_BACK.equals(key)) {
        return Boolean.toString(this.drawBack);
      }
      if (KEY_EXPAND_ALL.equals(key)) {
        return Boolean.toString(this.expandAll);
      }
      return null;
    }

  }
}
