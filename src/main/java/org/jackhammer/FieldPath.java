/**
 * Copyright (c) 2014 MapR, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jackhammer;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.jackhammer.FieldSegment.NameSegment;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Immutable class for representing a field path.
 */
public final class FieldPath implements Comparable<FieldPath>, Iterable<FieldSegment> {

  /**
   * Use this method to construct
   * @param fieldPath
   * @return An immutable instance of {@link FieldPath} parsed from the input string.
   */
  public static FieldPath parseFrom(String fieldPath) {
    if (fieldPath == null || fieldPath.isEmpty()) {
      return null;
    }

    FieldPath fp = null;
    if ((fp = fieldPathCache.getIfPresent(fieldPath)) == null) {
      try {
        CommonTokenStream tokens = new CommonTokenStream(
            new FieldPathLexer(new ANTLRInputStream(fieldPath)));
        FieldPathErrorListerner listener = new FieldPathErrorListerner();
        FieldPathParser parser = new FieldPathParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        fp = parser.parse().e;
        if (listener.isError()) {
          throw new IllegalArgumentException(
              "'" + fieldPath + "' is not a valid FieldPath: "
              + listener.getErrorMsg(), listener.getException());
        }
        fieldPathCache.put(fieldPath, fp);
      } catch (RecognitionException e) {
        throw new IllegalArgumentException(
            "Unable to parse '" + fieldPath + "' as a FieldPath.", e);
      }
    }

    return fp;
  }

  @Override
  public Iterator<FieldSegment> iterator() {
    return new FieldSegmentIterator();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FieldPath)) {
      return false;
    }

    FieldPath other = (FieldPath) obj;
    if (rootSegment == null) {
      return (other.rootSegment == null);
    }
    return rootSegment.equals(other.rootSegment);
  }

  @Override
  public int hashCode() {
    return ((rootSegment == null) ? 0 : rootSegment.hashCode());
  }

  @Override
  public String toString() {
    return asPathString();
  }

  @Override
  public int compareTo(FieldPath other) {
    return rootSegment.compareTo(other.getRootSegment());
  }

  public String asPathString() {
    return rootSegment.asPathString(true);
  }

  public String asPathString(boolean escape) {
    return rootSegment.asPathString(escape);
  }

  public FieldSegment getRootSegment() {
    return rootSegment;
  }

  /**
   * Internal cache which store
   */
  private static Cache<String, FieldPath> fieldPathCache =
      CacheBuilder.newBuilder().maximumSize(1000).build();

  final private NameSegment rootSegment;

  /**
   * Package default for parser
   * @param root
   */
  FieldPath(NameSegment root) {
    this.rootSegment = root;
  }

  final class FieldSegmentIterator implements Iterator<FieldSegment> {
    FieldSegment current = rootSegment;

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public FieldSegment next() {
      if (current == null) {
        throw new NoSuchElementException();
      }
      FieldSegment ret = current;
      current = current.getChild();
      return ret;
    }

    @Override
    public boolean hasNext() {
      return current != null;
    }
  }

  final static class FieldPathErrorListerner extends BaseErrorListener {
    private String errorMsg;
    private RecognitionException exception;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
        int charPositionInLine, String msg, RecognitionException e) {
      this.errorMsg = String.format("At line:%d:%d: %s", line, charPositionInLine, msg);
      this.exception = e;
    }

    public boolean isError() {
      return errorMsg != null;
    }

    public String getErrorMsg() {
      return errorMsg;
    }

    public RecognitionException getException() {
      return exception;
    }
  }

}
