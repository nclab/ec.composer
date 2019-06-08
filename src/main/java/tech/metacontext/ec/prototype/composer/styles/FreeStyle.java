/*
 * Copyright 2018 Jonathan Chang.
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
package tech.metacontext.ec.prototype.composer.styles;

import tech.metacontext.ec.prototype.composer.materials.MusicMaterial;
import tech.metacontext.ec.prototype.composer.model.Composition;
import tech.metacontext.ec.prototype.composer.model.SketchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class FreeStyle extends Style {

    @Override
    public boolean qualifySketchNode(SketchNode sketchNode) {

        return true;
    }

    @Override
    public double rateComposition(Composition composition) {

        return 1.0;
    }

    public static boolean checker(SketchNode whatever) {

        return true;
    }

    @Override
    public <M extends MusicMaterial> void matInitializer(M m) {
    }
}
