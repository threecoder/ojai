/**
 * Copyright (c) 2015 MapR, Inc.
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
package org.ojai.tests.json;

import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;

import org.junit.Test;
import org.ojai.Value;
import org.ojai.json.impl.JsonValueBuilder;

public class TestValues {

  @Test
  public void testEmptyByteBufferSerialization() throws Exception {
    ByteBuffer hbb = ByteBuffer.allocate(0);
    Value hbbValue = JsonValueBuilder.initFrom(hbb);
    assertEquals("{\"$binary\":\"\"}", hbbValue.toString());

    ByteBuffer dbb = ByteBuffer.allocateDirect(0);
    Value dbbValue = JsonValueBuilder.initFrom(dbb);
    assertEquals("{\"$binary\":\"\"}", dbbValue.toString());
  }

}
