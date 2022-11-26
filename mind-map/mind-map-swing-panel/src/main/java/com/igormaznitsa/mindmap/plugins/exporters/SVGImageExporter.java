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

package com.igormaznitsa.mindmap.plugins.exporters;

import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.calculateSizeOfMapInPixels;
import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.drawOnGraphicsForConfiguration;
import static com.igormaznitsa.mindmap.swing.panel.MindMapPanel.layoutFullDiagramWithCenteringToPaper;
import static java.lang.Long.toHexString;

import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.Topic;
import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.plugins.api.AbstractExporter;
import com.igormaznitsa.mindmap.plugins.api.PluginContext;
import com.igormaznitsa.mindmap.plugins.api.parameters.AbstractParameter;
import com.igormaznitsa.mindmap.plugins.api.parameters.BooleanParameter;
import com.igormaznitsa.mindmap.swing.i18n.MmdI18n;
import com.igormaznitsa.mindmap.swing.ide.IDEBridgeFactory;
import com.igormaznitsa.mindmap.swing.panel.MindMapPanelConfig;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.MMGraphics;
import com.igormaznitsa.mindmap.swing.panel.ui.gfx.StrokeType;
import com.igormaznitsa.mindmap.swing.panel.utils.MindMapUtils;
import com.igormaznitsa.mindmap.swing.panel.utils.RenderQuality;
import com.igormaznitsa.mindmap.swing.panel.utils.Utils;
import com.igormaznitsa.mindmap.swing.services.IconID;
import com.igormaznitsa.mindmap.swing.services.ImageIconServiceProvider;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.font.TextLayout;
import java.awt.geom.Dimension2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;

public class SVGImageExporter extends AbstractExporter {

  protected static final String FONT_CLASS_NAME = "mmdTitleFont";
  private static final Map<String, String[]> LOCAL_FONT_MAP = new HashMap<String, String[]>() {
    {
      put("dialog", new String[] {"sans-serif", "SansSerif"});
      put("dialoginput", new String[] {"monospace", "Monospace"});
      put("monospaced", new String[] {"monospace", "Monospace"});
      put("serif", new String[] {"serif", "Serif"});
      put("sansserif", new String[] {"sans-serif", "SansSerif"});
      put("symbol", new String[] {"'WingDings'", "WingDings"});
    }
  };
  private static final Logger LOGGER = LoggerFactory.getLogger(SVGImageExporter.class);
  private static final Icon ICO =
      ImageIconServiceProvider.findInstance().getIconForId(IconID.POPUP_EXPORT_SVG);
  private static final String NEXT_LINE = "\n";
  private static final String SVG_HEADER =
      "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" + NEXT_LINE + "<!-- " +
          "Generated by " +
          IDEBridgeFactory.findInstance().getIDEGeneratorId() + ' ' +
          IDEBridgeFactory.findInstance().getIDEVersion() +
          " (https://sciareto.org) -->" + NEXT_LINE +
          "<svg version=\"1.1\" baseProfile=\"tiny\" id=\"svg-root\" width=\"%d%%\" height=\"%d%%\" viewBox=\"0 0 %s %s\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">";
  private static final DecimalFormat DOUBLE;
  private static final String KEY_PARAMETER_UNFOLD_ALL = "mmd.exporter.svg.unfold.all";
  private static final String KEY_PARAMETER_DRAW_BACKGROUND = "mmd.exporter.svg.background.draw";

  public static final String LOOKUP_PARAM_REQ_FONT = "mmd.exporter.svg.font";
  public static final String LOOKUP_PARAM_RESP_WOFF_FONT_AS_ARRAY = "mmd.exporter.svg.font.woff";

  static {
    DOUBLE = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.US));
  }

  private static Optional<byte[]> findWoffFont(final Font font) {
    final Map<String,Object> map = new HashMap<>();
    map.put(LOOKUP_PARAM_REQ_FONT, font);
    final Map<String,Object> result = IDEBridgeFactory.findInstance().lookup(map);
    return Optional.ofNullable((byte[]) result.get(LOOKUP_PARAM_RESP_WOFF_FONT_AS_ARRAY));
  }

  private static String dbl2str(final double value) {
    return DOUBLE.format(value);
  }

  private static String fontFamilyToSVG(final Font font) {
    String fontFamilyStr = font.getFamily();
    final String[] logicalFontFamily = LOCAL_FONT_MAP.get(font.getName().toLowerCase());
    if (logicalFontFamily != null) {
      fontFamilyStr = logicalFontFamily[0];
    } else {
      fontFamilyStr = String.format("'%s'", fontFamilyStr);
    }
    return fontFamilyStr;
  }

  private static String font2style(final Font font) {
    final StringBuilder result = new StringBuilder();

    final String fontStyle = font.isItalic() ? "italic" : "normal";
    final String fontWeight = font.isBold() ? "bold" : "normal";
    final String fontSize = DOUBLE.format(font.getSize2D()) + "px";
    final String fontFamily = fontFamilyToSVG(font);

    result.append("font-family: ").append(fontFamily).append(';').append(NEXT_LINE);
    result.append("font-size: ").append(fontSize).append(';').append(NEXT_LINE);
    result.append("font-style: ").append(fontStyle).append(';').append(NEXT_LINE);
    result.append("font-weight: ").append(fontWeight).append(';').append(NEXT_LINE);

    findWoffFont(font).ifPresent(woff -> {
      result.append("src: url(\"data:application/font-woff;base64,").append(Utils.base64encode(woff)).append("\") format(woff);").append(NEXT_LINE);
    });

    return result.toString();
  }

  @Override
  public String getMnemonic() {
    return "svg";
  }

  @Override
  public Set<AbstractParameter<?>> makeDefaultParameters() {
    return new HashSet<AbstractParameter<?>>() {{
      add(new BooleanParameter(KEY_PARAMETER_UNFOLD_ALL,
          MmdI18n.getInstance().findBundle().getString("SvgExporter.optionUnfoldAll"),
          MmdI18n.getInstance().findBundle().getString("SvgExporter.optionUnfoldAll.comment"),
          true));
      add(new BooleanParameter(KEY_PARAMETER_DRAW_BACKGROUND,
          MmdI18n.getInstance().findBundle().getString("SvgExporter.optionDrawBackground"),
          MmdI18n.getInstance().findBundle().getString("SvgExporter.optionDrawBackground.comment"),
          true));
    }};
  }

  private String makeContent(final PluginContext context, final Set<AbstractParameter<?>> options) {
    final boolean flagExpandAllNodes = options.stream()
        .filter(x -> KEY_PARAMETER_UNFOLD_ALL.equals(x.getId()))
        .findFirst()
        .map(x -> ((BooleanParameter) x).getValue())
        .orElse(true);

    final boolean flagDrawBackground = options.stream()
        .filter(x -> KEY_PARAMETER_DRAW_BACKGROUND.equals(x.getId()))
        .findFirst()
        .map(x -> ((BooleanParameter) x).getValue())
        .orElse(true);


    final MindMap workMap = context.getPanel().getModel().makeCopy();
    workMap.clearAllPayloads();

    if (flagExpandAllNodes) {
      MindMapUtils.removeCollapseAttr(workMap);
    }

    final MindMapPanelConfig newConfig = new MindMapPanelConfig(context.getPanelConfig(), false);
    final String[] mappedFont =
        LOCAL_FONT_MAP.get(newConfig.getFont().getFamily().toLowerCase(Locale.ENGLISH));
    if (mappedFont != null) {
      final Font adaptedFont =
          new Font(mappedFont[1], newConfig.getFont().getStyle(), newConfig.getFont().getSize());
      newConfig.setFont(adaptedFont);
    }

    newConfig.setDrawBackground(flagDrawBackground);
    newConfig.setScale(1.0f);

    final Dimension2D blockSize =
        calculateSizeOfMapInPixels(workMap, null, newConfig, flagExpandAllNodes,
            RenderQuality.DEFAULT);
    if (blockSize == null) {
      return SVG_HEADER + "</svg>";
    }

    final StringBuilder buffer = new StringBuilder(16384);

    final ImageCache imageCache = new ImageCache();
    final BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
    final Graphics2D g = image.createGraphics();
    final MMGraphics gfx = new SVGMMGraphics(buffer, g, imageCache);
    gfx.setClip(0, 0, (int) Math.round(blockSize.getWidth()),
        (int) Math.round(blockSize.getHeight()));
    try {
      layoutFullDiagramWithCenteringToPaper(gfx, workMap, newConfig, blockSize);
      drawOnGraphicsForConfiguration(gfx, newConfig, workMap, false, null);
      buffer.insert(0, imageCache);
    } finally {
      gfx.dispose();
      imageCache.reset();
    }

    buffer.insert(0, NEXT_LINE);
    buffer.insert(0, prepareStylePart(newConfig));
    buffer.insert(0, NEXT_LINE);
    buffer.insert(0, String.format(SVG_HEADER, 100, 100, dbl2str(blockSize.getWidth()),
        dbl2str(blockSize.getHeight())));

    buffer.append("</svg>");

    return buffer.toString();
  }

  @Override
  public void doExportToClipboard(final PluginContext context,
                                  final Set<AbstractParameter<?>> options)
      throws IOException {
    final String text = makeContent(context, options);
    SwingUtilities.invokeLater(() -> {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      if (clipboard != null) {
        clipboard.setContents(new SvgClip(text), null);
      }
    });
  }

  @Override
  public void doExport(final PluginContext context, final Set<AbstractParameter<?>> options,
                       final OutputStream out) throws IOException {
    final String text = makeContent(context, options);

    File fileToSaveMap = null;
    OutputStream theOut = out;
    if (theOut == null) {
      fileToSaveMap = MindMapUtils.selectFileToSaveForFileFilter(
          context.getPanel(),
          context,
          this.getClass().getName(),
          MmdI18n.getInstance().findBundle().getString("SvgExporter.saveDialogTitle"), null,
          ".svg", MmdI18n.getInstance().findBundle().getString("SvgExporter.filterDescription"),
          MmdI18n.getInstance().findBundle().getString("SvgExporter.approveButtonText"));
      fileToSaveMap =
          MindMapUtils.checkFileAndExtension(context.getPanel(), fileToSaveMap, ".svg");
      theOut = fileToSaveMap == null ? null :
          new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
    }
    if (theOut != null) {
      try {
        IOUtils.write(text, theOut, "UTF-8");
      } finally {
        if (fileToSaveMap != null) {
          IOUtils.closeQuietly(theOut);
        }
      }
    }
  }

  private String prepareStylePart(final MindMapPanelConfig config) {
    return "<style type=\"text/css\">" + NEXT_LINE +
        '.' + FONT_CLASS_NAME + " {" + NEXT_LINE + font2style(config.getFont()) + "}" + NEXT_LINE +
        "</style>";
  }

  @Override
  public String getName(final PluginContext context, final Topic actionTopic) {
    return MmdI18n.getInstance().findBundle().getString("SvgExporter.exporterName");
  }

  @Override
  public String getReference(final PluginContext context, final Topic actionTopic) {
    return MmdI18n.getInstance().findBundle().getString("SvgExporter.exporterReference");
  }

  @Override
  public Icon getIcon(final PluginContext panel, final Topic actionTopic) {
    return ICO;
  }

  @Override
  public int getOrder() {
    return 5;
  }

  private static final class ImageCache {
    private final AtomicLong counter = new AtomicLong(1L);

    private final Map<RenderedImage, String> map = new HashMap<>();

    String add(final RenderedImage image) {
      if (this.map.containsKey(image)) {
        return this.map.get(image);
      } else {
        final String uid = "imageId-" + toHexString(this.counter.getAndIncrement()).toUpperCase(
            Locale.ENGLISH);
        this.map.put(image, uid);
        return uid;
      }
    }

    @Override
    public String toString() {
      final StringWriter writer = new StringWriter(4096);
      writer.write("<defs>");
      writer.write(NEXT_LINE);

      this.map.entrySet().stream()
          .sorted(Map.Entry.comparingByValue())
          .forEach(e -> {
            final ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream(4096);
            String pngBase64Encoded = "";
            try {
              if (ImageIO.write(e.getKey(), "png", imageBuffer)) {
                pngBase64Encoded = Utils.base64encode(imageBuffer.toByteArray());
              }
            } catch (IOException ex) {
              LOGGER.error("Can't render PNG image for internal IO error", ex);
              pngBase64Encoded = "http://cant_render_image_for_io_error.org";
            }
            writer.write(
                String.format(
                    " <image id=\"%s\" width=\"%d\" height=\"%d\" xlink:href=\"data:image/png;charset=utf-8;base64,%s\"/>",
                    e.getValue(),
                    e.getKey().getWidth(),
                    e.getKey().getHeight(),
                    pngBase64Encoded));
            writer.write(NEXT_LINE);
          });

      writer.write("</defs>");
      writer.write(NEXT_LINE);
      return writer.toString();
    }

    void reset() {
      this.map.clear();
    }
  }

  public static class SvgClip implements Transferable {

    private static final DataFlavor SVG_FLAVOR =
        new DataFlavor("image/svg+xml; class=java.io.InputStream", "Scalable Vector Graphic");
    final private String svgContent;

    private final DataFlavor[] supportedFlavors;

    public SvgClip(final String str) {
      this.supportedFlavors = new DataFlavor[] {
          SVG_FLAVOR,};

      this.svgContent = str;
      SystemFlavorMap systemFlavorMap = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
      DataFlavor dataFlavor = getSVGFlavor();
      systemFlavorMap.addUnencodedNativeForFlavor(dataFlavor, "image/svg+xml");
    }

    static DataFlavor getSVGFlavor() {
      return SvgClip.SVG_FLAVOR;
    }

    @Override
    public boolean isDataFlavorSupported(final DataFlavor flavor) {
      for (DataFlavor supported : this.supportedFlavors) {
        if (flavor.equals(supported)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
      return this.supportedFlavors;
    }

    @Override
    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
      if (isDataFlavorSupported(flavor) && flavor.equals(SVG_FLAVOR)) {
        return new ByteArrayInputStream(this.svgContent.getBytes(StandardCharsets.UTF_8));
      }
      throw new UnsupportedFlavorException(flavor);
    }
  }

  private static final class SVGMMGraphics implements MMGraphics {

    private static final DecimalFormat ALPHA = new DecimalFormat("#.##");
    private final StringBuilder buffer;
    private final Graphics2D context;
    private final ImageCache imageCache;
    private double translateX;
    private double translateY;
    private float strokeWidth = 1.0f;
    private StrokeType strokeType = StrokeType.SOLID;

    private SVGMMGraphics(
        final StringBuilder buffer,
        final Graphics2D context,
        final ImageCache imageCache
    ) {
      this.imageCache = imageCache;
      this.buffer = buffer;
      this.context = (Graphics2D) context.create();
    }

    private static String svgRgb(final Color color) {
      return "rgb(" + color.getRed() + ',' + color.getGreen() + ',' + color.getBlue() + ')';
    }

    private void printFillOpacity(final Color color) {
      if (color.getAlpha() < 255) {
        this.buffer.append(" fill-opacity=\"").append(ALPHA.format(color.getAlpha() / 255.0f))
            .append("\" ");
      }
    }

    private void printFontData() {
      this.buffer.append("class=\"" + FONT_CLASS_NAME + '\"');
    }

    private void printStrokeData(final Color color) {
      this.buffer.append(" stroke=\"").append(svgRgb(color))
          .append("\" stroke-width=\"").append(dbl2str(this.strokeWidth)).append("\"");

      switch (this.strokeType) {
        case SOLID:
          this.buffer.append(" stroke-linecap=\"round\"");
          break;
        case DASHES:
          this.buffer.append(" stroke-linecap=\"butt\" stroke-dasharray=\"")
              .append(dbl2str(this.strokeWidth * 3.0f)).append(',')
              .append(dbl2str(this.strokeWidth)).append("\"");
          break;
        case DOTS:
          this.buffer.append(" stroke-linecap=\"butt\" stroke-dasharray=\"")
              .append(dbl2str(this.strokeWidth)).append(',')
              .append(dbl2str(this.strokeWidth * 2.0f)).append("\"");
          break;
      }
    }

    @Override
    public float getFontMaxAscent() {
      return this.context.getFontMetrics().getMaxAscent();
    }

    @Override
    public Rectangle2D getStringBounds(final String str) {
      if (str.isEmpty()) {
        return this.context.getFontMetrics().getStringBounds("", this.context);
      } else {
        final TextLayout textLayout =
            new TextLayout(str, this.context.getFont(), this.context.getFontRenderContext());
        return new Rectangle2D.Float(0, -textLayout.getAscent(), textLayout.getAdvance(),
            textLayout.getAscent() + textLayout.getDescent() + textLayout.getLeading());
      }
    }

    @Override
    public void setClip(final int x, final int y, final int w, final int h) {
      this.context.setClip(x, y, w, h);
    }

    @Override
    public MMGraphics copy() {
      final SVGMMGraphics result = new SVGMMGraphics(this.buffer, this.context, this.imageCache);
      result.translateX = this.translateX;
      result.translateY = this.translateY;
      result.strokeType = this.strokeType;
      result.strokeWidth = this.strokeWidth;
      return result;
    }

    @Override
    public void dispose() {
      this.context.dispose();
    }

    @Override
    public void translate(final double x, final double y) {
      this.translateX += x;
      this.translateY += y;
      this.context.translate(x, y);
    }

    @Override
    public Rectangle getClipBounds() {
      return this.context.getClipBounds();
    }

    @Override
    public void setStroke(final float width, final StrokeType type) {
      if (type != this.strokeType || Float.compare(this.strokeWidth, width) != 0) {
        this.strokeType = type;
        this.strokeWidth = width;
        if (Float.compare(this.strokeWidth, width) != 0) {
          this.strokeWidth = width;

          final Stroke stroke;

          switch (type) {
            case SOLID:
              stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
              break;
            case DASHES:
              stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
                  new float[] {width * 2.0f, width}, 0.0f);
              break;
            case DOTS:
              stroke = new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
                  new float[] {width, width * 2.0f}, 0.0f);
              break;
            default:
              throw new Error("Unexpected stroke type : " + type);
          }
          this.context.setStroke(stroke);
        }
      }
    }

    @Override
    public void drawLine(final int startX, final int startY, final int endX, final int endY,
                         final Color color) {
      this.buffer.append("<line x1=\"").append(dbl2str(startX + this.translateX))
          .append("\" y1=\"").append(dbl2str(startY + this.translateY))
          .append("\" x2=\"").append(dbl2str(endX + this.translateX))
          .append("\" y2=\"").append(dbl2str(endY + this.translateY)).append("\" ");
      if (color != null) {
        printStrokeData(color);
        printFillOpacity(color);
      }
      this.buffer.append("/>").append(NEXT_LINE);
    }

    @Override
    public void drawString(final String text, final int x, final int y, final Color color) {
      this.buffer.append("<text x=\"").append(dbl2str(this.translateX + x)).append("\" y=\"")
          .append(dbl2str(this.translateY + y)).append('\"');
      if (color != null) {
        this.buffer.append(" fill=\"").append(svgRgb(color)).append("\"");
        printFillOpacity(color);
      }
      this.buffer.append(' ');
      printFontData();
      this.buffer.append('>').append(StringEscapeUtils.escapeXml10(text)).append("</text>")
          .append(NEXT_LINE);
    }

    @Override
    public void drawRect(final int x, final int y, final int width, final int height,
                         final Color border, final Color fill) {
      this.buffer.append("<rect x=\"").append(dbl2str(this.translateX + x))
          .append("\" y=\"").append(dbl2str(translateY + y))
          .append("\" width=\"").append(dbl2str(width))
          .append("\" height=\"").append(dbl2str(height))
          .append("\" ");
      if (border != null) {
        printStrokeData(border);
      }

      if (fill == null) {
        this.buffer.append(" fill=\"none\"");
      } else {
        this.buffer.append(" fill=\"").append(svgRgb(fill)).append("\"");
        printFillOpacity(fill);
      }

      this.buffer.append("/>").append(NEXT_LINE);
    }

    @Override
    public void draw(final Shape shape, final Color border, final Color fill) {
      if (shape instanceof RoundRectangle2D) {
        final RoundRectangle2D rect = (RoundRectangle2D) shape;

        this.buffer.append("<rect x=\"").append(dbl2str(this.translateX + rect.getX()))
            .append("\" y=\"").append(dbl2str(translateY + rect.getY()))
            .append("\" width=\"").append(dbl2str(rect.getWidth()))
            .append("\" height=\"").append(dbl2str(rect.getHeight()))
            .append("\" rx=\"").append(dbl2str(rect.getArcWidth() / 2.0d))
            .append("\" ry=\"").append(dbl2str(rect.getArcHeight() / 2.0d))
            .append("\" ");

      } else if (shape instanceof Rectangle2D) {

        final Rectangle2D rect = (Rectangle2D) shape;
        this.buffer.append("<rect x=\"").append(dbl2str(this.translateX + rect.getX()))
            .append("\" y=\"").append(dbl2str(translateY + rect.getY()))
            .append("\" width=\"").append(dbl2str(rect.getWidth()))
            .append("\" height=\"").append(dbl2str(rect.getHeight()))
            .append("\" ");

      } else if (shape instanceof Path2D) {
        final Path2D path = (Path2D) shape;
        final double[] data = new double[6];

        this.buffer.append("<path d=\"");

        boolean nofirst = false;

        for (final PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
          if (nofirst) {
            this.buffer.append(' ');
          }
          switch (pi.currentSegment(data)) {
            case PathIterator.SEG_MOVETO: {
              this.buffer.append("M ").append(dbl2str(this.translateX + data[0])).append(' ')
                  .append(dbl2str(this.translateY + data[1]));
            }
            break;
            case PathIterator.SEG_LINETO: {
              this.buffer.append("L ").append(dbl2str(this.translateX + data[0])).append(' ')
                  .append(dbl2str(this.translateY + data[1]));
            }
            break;
            case PathIterator.SEG_CUBICTO: {
              this.buffer.append("C ")
                  .append(dbl2str(this.translateX + data[0])).append(' ')
                  .append(dbl2str(this.translateY + data[1])).append(',')
                  .append(dbl2str(this.translateX + data[2])).append(' ')
                  .append(dbl2str(this.translateY + data[3])).append(',')
                  .append(dbl2str(this.translateX + data[4])).append(' ')
                  .append(dbl2str(this.translateY + data[5]));
            }
            break;
            case PathIterator.SEG_QUADTO: {
              this.buffer.append("Q ")
                  .append(dbl2str(this.translateX + data[0])).append(' ')
                  .append(dbl2str(this.translateY + data[1])).append(',')
                  .append(dbl2str(this.translateX + data[2])).append(' ')
                  .append(dbl2str(this.translateY + data[3]));
            }
            break;
            case PathIterator.SEG_CLOSE: {
              this.buffer.append("Z");
            }
            break;
            default:
              LOGGER.warn("Unexpected path segment type");
          }
          nofirst = true;
        }
        this.buffer.append("\" ");
      } else {
        LOGGER.warn("Detected unexpected shape : " + shape.getClass().getName());
      }

      if (border != null) {
        printStrokeData(border);
      }

      if (fill == null) {
        this.buffer.append(" fill=\"none\"");
      } else {
        this.buffer.append(" fill=\"").append(svgRgb(fill)).append("\"");
        printFillOpacity(fill);
      }

      this.buffer.append("/>").append(NEXT_LINE);
    }

    @Override
    public void drawCurve(final double startX, final double startY, final double endX,
                          final double endY, final Color color) {
      this.buffer.append("<path d=\"M").append(dbl2str(startX + this.translateX)).append(',')
          .append(startY + this.translateY)
          .append(" C").append(dbl2str(startX))
          .append(',').append(dbl2str(endY))
          .append(' ').append(dbl2str(startX))
          .append(',').append(dbl2str(endY))
          .append(' ').append(dbl2str(endX))
          .append(',').append(dbl2str(endY))
          .append("\" fill=\"none\"");

      if (color != null) {
        printStrokeData(color);
      }
      this.buffer.append(" />").append(NEXT_LINE);
    }

    @Override
    public void drawOval(final int x, final int y, final int w, final int h, final Color border,
                         final Color fill) {
      final double rx = (double) w / 2.0d;
      final double ry = (double) h / 2.0d;
      final double cx = (double) x + this.translateX + rx;
      final double cy = (double) y + this.translateY + ry;

      this.buffer.append("<ellipse cx=\"").append(dbl2str(cx))
          .append("\" cy=\"").append(dbl2str(cy))
          .append("\" rx=\"").append(dbl2str(rx))
          .append("\" ry=\"").append(dbl2str(ry))
          .append("\" ");

      if (border != null) {
        printStrokeData(border);
      }

      if (fill == null) {
        this.buffer.append(" fill=\"none\"");
      } else {
        this.buffer.append(" fill=\"").append(svgRgb(fill)).append("\"");
        printFillOpacity(fill);
      }

      this.buffer.append("/>").append(NEXT_LINE);
    }

    @Override
    public void drawImage(final Image image, final int x, final int y) {
      if (image != null) {
        if (image instanceof RenderedImage) {
          final RenderedImage renderedImage = (RenderedImage) image;
          final String imageUid = this.imageCache.add(renderedImage);
          this.buffer.append("<use href=\"#").append(imageUid).append("\" xlink:href=\"#")
              .append(imageUid).append("\" ")
              .append("x=\"").append(dbl2str(this.translateX + x)).append("\" ")
              .append("y=\"").append(dbl2str(this.translateY + y)).append("\"/>")
              .append(NEXT_LINE);
        } else {
          LOGGER.warn(
              "Can't place image because it is not rendered one : " + image.getClass().getName());
        }
      }
    }

    @Override
    public void setFont(final Font font) {
      this.context.setFont(font);
    }

  }
}
