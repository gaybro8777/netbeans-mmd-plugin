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

package com.igormaznitsa.mindmap.annoit.paths;

import com.igormaznitsa.mindmap.annotations.MmdFileRef;
import com.igormaznitsa.mindmap.annotations.MmdTopic;

@MmdFileRef(target = RootFile.class)
@MmdTopic(title = "class_3", path = "path")
public class Class3 {
    @MmdTopic(path = {"path", "class_3"}, title = "m1")
    public void mon1() {
    }

    @MmdTopic(path = {"path", "class_3"}, title = "m2")
    public void mon2() {
    }

}
