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

package com.igormaznitsa.mindmap.model;

import static com.igormaznitsa.mindmap.model.MiscUtils.ensureNotNull;
import static java.util.Objects.requireNonNull;

import com.igormaznitsa.mindmap.model.logger.Logger;
import com.igormaznitsa.mindmap.model.logger.LoggerFactory;
import com.igormaznitsa.mindmap.model.parser.MindMapLexer;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;

public final class MindMap implements Serializable, Constants, Iterable<Topic> {

  public static final String FORMAT_VERSION = "1.1"; //NOI18N
  private static final long serialVersionUID = 5929181596778047354L;
  private static final Logger LOGGER = LoggerFactory.getLogger(MindMap.class);
  private static final Pattern PATTERN_ATTRIBUTES = Pattern.compile("^\\s*\\>\\s(.+)$"); //NOI18N
  private static final Pattern PATTERN_ATTRIBUTE =
      Pattern.compile("[,]?\\s*([\\S]+?)\\s*=\\s*(\\`+)(.*?)\\2"); //NOI18N
  private static final String GENERATOR_VERSION_NAME = "__version__"; //NOI18N
  private final transient Lock locker = new ReentrantLock();
  private final Map<String, String> attributes =
          new TreeMap<>(ModelUtils.STRING_COMPARATOR);
  private final transient List<MindMapModelEventListener> modelEventListeners =
          new CopyOnWriteArrayList<>();

  private Topic root;

  public MindMap(final boolean makeRoot) {
    if (makeRoot) {
      this.root = new Topic(this, null, "");
    }
  }

  public MindMap(final MindMap map) {
    this.attributes.putAll(map.attributes);
    final Topic rootTopic = map.getRoot();
    this.root = rootTopic == null ? null : rootTopic.makeCopy(this, null);
  }

  public MindMap(final Reader reader) throws IOException {
    final String text = IOUtils.toString(requireNonNull(reader));

    final MindMapLexer lexer = new MindMapLexer();
    lexer.start(text, 0, text.length(), MindMapLexer.TokenType.HEAD_LINE);

    Topic rootTopic = null;

    boolean process = true;

    while (process) {
      final int oldLexerPosition = lexer.getCurrentPosition().getOffset();
      lexer.advance();
      final boolean lexerPositionWasNotChanged =
          oldLexerPosition == lexer.getCurrentPosition().getOffset();

      final MindMapLexer.TokenType token = lexer.getTokenType();
      if (token == null || lexerPositionWasNotChanged) {
        throw new IllegalArgumentException("Wrong format of mind map, end of header is not found");
      }
      switch (token) {
        case HEAD_LINE:
          continue;
        case ATTRIBUTE: {
          fillMapByAttributes(lexer.getTokenText(), this.attributes);
        }
        break;
        case HEAD_DELIMITER: {
          process = false;
          rootTopic = Topic.parse(this, lexer);
        }
        break;
        default:
          break;
      }
    }

    this.root = rootTopic;
    this.attributes.put(GENERATOR_VERSION_NAME, FORMAT_VERSION);
  }

  static boolean fillMapByAttributes(final String line,
                                     final Map<String, String> map) {
    final Matcher attrmatcher = PATTERN_ATTRIBUTES.matcher(line);
    if (attrmatcher.find()) {
      final Matcher attrParser = PATTERN_ATTRIBUTE.matcher(attrmatcher.group(1));
      while (attrParser.find()) {
        map.put(attrParser.group(1), attrParser.group(3));
      }
      return true;
    }
    return false;
  }

  static String allAttributesAsString(final Map<String, String> map) throws IOException {
    final StringBuilder buffer = new StringBuilder();

    final List<String> attrNames = new ArrayList<>(map.keySet());
    Collections.sort(attrNames);

    boolean nonfirst = false;
    for (final String k : attrNames) {
      final String value = map.get(k);
      if (nonfirst) {
        buffer.append(',');
      } else {
        nonfirst = true;
      }
      buffer.append(k).append('=').append(ModelUtils.makeMDCodeBlock(value));
    }

    return buffer.toString();
  }

  public void clear() {
    setRoot(null, true);
  }

  public Topic findNext(final File baseFolder, final Topic start,
                        final Pattern pattern, final boolean findInTopicText,
                        final Set<Extra.ExtraType> extrasToFind) {
    return this.findNext(baseFolder, start, pattern, findInTopicText, extrasToFind, null);
  }

  public Topic findNext(
      final File baseFolder,
      final Topic start,
      final Pattern pattern,
      final boolean findInTopicText,
      final Set<Extra.ExtraType> extrasToFind,
      final Set<TopicFinder> topicFinders
  ) {
    if (start != null && start.getMap() != this) {
      throw new IllegalArgumentException("Topic doesn't belong to the mind map");
    }

    final boolean findPluginNote = (extrasToFind != null && !extrasToFind.isEmpty())
        && (topicFinders != null && !topicFinders.isEmpty())
        && extrasToFind.contains(Extra.ExtraType.NOTE);
    final boolean findPluginFile = (extrasToFind != null && !extrasToFind.isEmpty())
        && (topicFinders != null && !topicFinders.isEmpty())
        && extrasToFind.contains(Extra.ExtraType.FILE);

    Topic result = null;

    this.locker.lock();
    try {
      boolean startFound = start == null;
      for (final Topic t : this) {
        if (startFound) {
          if (t.containsPattern(baseFolder, pattern, findInTopicText, extrasToFind)) {
            result = t;
          } else if (topicFinders != null) {
            for (TopicFinder f : topicFinders) {
              if (f.doesTopicContentMatches(t, baseFolder, pattern, extrasToFind)) {
                result = t;
                break;
              }
            }
          }
          if (result != null) {
            break;
          }
        } else if (t == start) {
          startFound = true;
        }
      }
    } finally {
      this.locker.unlock();
    }

    return result;
  }

  public Topic findPrev(final File baseFolder, final Topic start,
                        final Pattern pattern, final boolean findInTopicText,
                        final Set<Extra.ExtraType> extrasToFind) {
    return this.findPrev(baseFolder, start, pattern, findInTopicText, extrasToFind, null);
  }

  public Topic findPrev(
      final File baseFolder,
      final Topic start,
      final Pattern pattern,
      final boolean findInTopicText,
      final Set<Extra.ExtraType> extrasToFind,
      final Set<TopicFinder> topicFinders
  ) {
    if (start != null && start.getMap() != this) {
      throw new IllegalArgumentException("Topic doesn't belong to the mind map");
    }

    Topic result = null;

    this.locker.lock();
    try {
      final List<Topic> plain = this.makePlainList();
      int startIndex = start == null ? plain.size() : plain.indexOf(start);
      if (startIndex < 0) {
        throw new IllegalArgumentException(
            "It looks like that topic doesn't belong to the mind map");
      }
      if (startIndex > 0) {
        while (startIndex > 0 && result == null) {
          final Topic candidate = plain.get(--startIndex);
          if (candidate.containsPattern(baseFolder, pattern, findInTopicText, extrasToFind)) {
            result = candidate;
          } else if (topicFinders != null) {
            for (TopicFinder f : topicFinders) {
              if (f.doesTopicContentMatches(candidate, baseFolder, pattern, extrasToFind)) {
                result = candidate;
                break;
              }
            }
          }
        }
      }
    } finally {
      this.locker.unlock();
    }

    return result;
  }

  public void setRoot(final Topic newRoot, final boolean makeNotification) {
    this.locker.lock();
    try {
      if (newRoot != null) {
        if (newRoot.getMap() != this) {
          throw new IllegalStateException("Base map must be the same");
        }
      }
      this.root = newRoot;
      if (makeNotification) {
        fireModelChanged();
      }
    } finally {
      this.locker.unlock();
    }
  }

  @Override
  public Iterator<Topic> iterator() {
    final Topic theroot = this.root;

    return new Iterator<Topic>() {
      Topic topicroot = theroot;
      Iterator<Topic> children;

      @Override
      public void remove() {
        this.children.remove();
      }

      @Override
      public boolean hasNext() {
        return this.topicroot != null || (this.children != null && this.children.hasNext());
      }

      @Override
      public Topic next() {
        final Topic result;
        if (this.topicroot != null) {
          result = this.topicroot;
          this.topicroot = null;
          this.children = result.iterator();
        } else if (this.children != null) {
          result = this.children.next();
        } else {
          throw new NoSuchElementException();
        }
        return result;
      }
    };
  }

  public boolean isEmpty() {
    this.locker.lock();
    try {
      return this.root == null;
    } finally {
      this.locker.unlock();
    }
  }

  private void fireModelChanged() {
    final Topic rootTopic = this.root;
    final MindMapModelEvent evt =
        new MindMapModelEvent(this, rootTopic == null ? null : rootTopic.getPath());
    for (final MindMapModelEventListener l : this.modelEventListeners) {
      l.onMindMapStructureChanged(evt);
    }
  }

  private void fireTopicChanged(final Topic topic) {
    final MindMapModelEvent evt =
        new MindMapModelEvent(this, topic == null ? null : topic.getPath());
    for (final MindMapModelEventListener l : this.modelEventListeners) {
      l.onMindMapNodesChanged(evt);
    }
  }

  public String getAttribute(final String name) {
    return this.attributes.get(name);
  }

  public void setAttribute(final String name, final String value) {
    this.locker.lock();
    try {
      if (value == null) {
        this.attributes.remove(name);
      } else {
        this.attributes.put(name, value);
      }
    } finally {
      this.locker.unlock();
    }
  }

  public void resetPayload() {
    this.locker.lock();
    try {
      if (this.root != null) {
        resetPayload(this.root);
      }
    } finally {
      this.locker.unlock();
    }
  }

  private void resetPayload(final Topic t) {
    if (t != null) {
      t.setPayload(null);
      for (final Topic m : t.getChildren()) {
        resetPayload(m);
      }
    }
  }

  public Topic findForPositionPath(final int[] positions) {
    if (positions == null || positions.length == 0) {
      return null;
    }
    if (positions.length == 1) {
      return this.root;
    }

    Topic result = this.root;
    int index = 1;
    while (result != null && index < positions.length) {
      final int elementPosition = positions[index++];
      if (elementPosition < 0 || result.getChildren().size() <= elementPosition) {
        result = null;
        break;
      }
      result = result.getChildren().get(elementPosition);
    }
    return result;
  }

  public List<Topic> removeNonExistingTopics(
      final List<Topic> origList) {
    final List<Topic> result = new ArrayList<>();
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        for (final Topic t : origList) {
          if (rootTopic.containTopic(t)) {
            result.add(t);
          }
        }
      } finally {
        this.locker.unlock();
      }
    }
    return result;
  }

  public Topic getRoot() {
    this.locker.lock();
    try {
      return this.root;
    } finally {
      this.locker.unlock();
    }
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("MindMap[");
    String delim = "";
    for (final Topic t : this) {
      builder.append(delim);
      builder.append(t);
      delim = ",";
    }
    builder.append(']');
    return builder.toString();
  }

  public String packToString() {
    final StringWriter writer;
    this.locker.lock();
    try {
      writer = new StringWriter(16384);
      try {
        write(writer);
      } catch (IOException ex) {
        throw new Error("Unexpected exception", ex);
      }
    } finally {
      locker.unlock();
    }
    return writer.toString();
  }

  public <T extends Writer> T write(final T out) throws IOException {
    this.locker.lock();
    try {
      out.append("Mind Map generated by NB MindMap plugin").append(NEXT_PARAGRAPH); //NOI18N
      this.attributes.put(GENERATOR_VERSION_NAME, FORMAT_VERSION);
      if (!this.attributes.isEmpty()) {
        out.append("> ").append(MindMap.allAttributesAsString(this.attributes))
            .append(NEXT_LINE); //NOI18N
      }
      out.append("---").append(NEXT_LINE); //NOI18N
      final Topic rootTopic = this.root;
      if (rootTopic != null) {
        rootTopic.write(out);
      }
    } finally {
      this.locker.unlock();
    }
    return out;
  }

  public void lock() {
    this.locker.lock();
  }

  public void unlock() {
    this.locker.unlock();
  }

  public Topic cloneTopic(final Topic topic, final boolean cloneFullTree) {
    this.locker.lock();
    try {
      if (topic == null || topic == this.root) {
        return null;
      }

      final Topic clonedtopic = topic.makeCopy(this, topic.getParent());
      if (!cloneFullTree) {
        clonedtopic.removeAllChildren();
      }

      clonedtopic.removeAttributeFromSubtree(ExtraTopic.TOPIC_UID_ATTR);

      fireModelChanged();

      return clonedtopic;
    } finally {
      this.locker.unlock();
    }
  }

  public boolean removeTopic(final Topic topic) {
    this.locker.lock();
    try {
      final boolean result;
      final Topic rootTopic = this.root;
      if (rootTopic == null) {
        result = false;
      } else if (this.root == topic) {
        rootTopic.setText(""); //NOI18N
        rootTopic.removeExtras();
        rootTopic.setPayload(null);
        rootTopic.removeAllChildren();
        result = true;
      } else {
        rootTopic.removeTopic(topic);
        result = rootTopic.removeAllLinksTo(topic);
      }
      if (result) {
        fireModelChanged();
      }

      return result;
    } finally {
      this.locker.unlock();
    }
  }

  public Topic findTopicForLink(final ExtraTopic link) {
    Topic result = null;
    if (link != null) {
      final Topic rootTopic = this.root;
      if (rootTopic != null) {
        this.locker.lock();
        try {
          result = rootTopic.findForAttribute(ExtraTopic.TOPIC_UID_ATTR, link.getValue());
        } finally {
          this.locker.unlock();
        }
      }
    }
    return result;
  }

  public List<Topic> findAllTopicsForExtraType(final Extra.ExtraType type) {
    final List<Topic> result = new ArrayList<>();
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        _findAllTopicsForExtraType(rootTopic, type, result);
      } finally {
        this.locker.unlock();
      }
    }
    return result;
  }

  private void _findAllTopicsForExtraType(final Topic topic,
                                          final Extra.ExtraType type,
                                          final List<Topic> result) {
    if (topic.getExtras().containsKey(type)) {
      result.add(topic);
    }
    for (final Topic c : topic.getChildren()) {
      _findAllTopicsForExtraType(c, type, result);
    }
  }

  public Topic getChild(final Topic parent, final int index) {
    return parent.getChildren().get(index);
  }

  public int getChildCount(Topic parent) {
    return parent.getChildren().size();
  }

  public boolean isLeaf(final Topic node) {
    return !node.hasChildren();
  }

  public void valueForPathChanged(final Topic[] path,
                                  final String newValue) {
    if (path.length > 0) {
      final Topic target = path[path.length - 1];
      target.setText(ensureNotNull(newValue, ""));
      fireTopicChanged(target);
    }
  }

  public int getIndexOfChild(final Topic parent, final Topic child) {
    return parent.getChildren().indexOf(child);
  }

  public void addMindMapModelEventListener(final MindMapModelEventListener l) {
    this.modelEventListeners.add(l);
  }

  public void removeMindMapModelEventListener(final MindMapModelEventListener l) {
    this.modelEventListeners.remove(l);
  }

  public boolean doesContainFileLink(final File baseFolder, final MMapURI file) {
    boolean result = false;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        return rootTopic.doesContainFileLink(baseFolder, file);
      } finally {
        this.locker.unlock();
      }
    }
    return result;
  }

  public boolean deleteAllLinksToFile(final File baseFolder, final MMapURI file) {
    boolean changed = false;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        changed = rootTopic.deleteLinkToFileIfPresented(baseFolder, file);
      } finally {
        this.locker.unlock();
      }
      if (changed) {
        fireModelChanged();
      }
    }
    return changed;
  }

  public boolean replaceAllLinksToFile(final File baseFolder,
                                       final MMapURI oldFile,
                                       final MMapURI newFile) {
    boolean changed = false;
    final Topic rootTopic = this.root;
    if (rootTopic != null) {
      this.locker.lock();
      try {
        changed = rootTopic.replaceLinkToFileIfPresented(baseFolder, oldFile, newFile);
      } finally {
        this.locker.unlock();
      }
      if (changed) {
        fireModelChanged();
      }
    }
    return changed;
  }

  public List<Topic> makePlainList() {
    this.locker.lock();
    try {
      final List<Topic> result = new ArrayList<>();
      for (final Topic t : this) {
        result.add(t);
      }
      return result;
    } finally {
      this.locker.unlock();
    }
  }

}
