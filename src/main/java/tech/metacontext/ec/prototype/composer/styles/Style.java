/*
 * Copyright 2018 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
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

import java.io.Serializable;
import tech.metacontext.ec.prototype.composer.materials.MusicMaterial;
import tech.metacontext.ec.prototype.composer.model.Composition;
import tech.metacontext.ec.prototype.composer.model.SketchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public abstract class Style implements Serializable {

    /**
     * Qualify a newly produced SketchNode. This function should only be called
     * when evaluating a SketchNode newly generated to prevent potentially
     * unpredictable outcome, for example, those containing some random rating
     * factors. One should only retrieves scores from CompositionEval storage
     * afterwards.
     *
     * @param sketchNode
     * @return true if qualified, otherwise false.
     */
    public abstract boolean qualifySketchNode(SketchNode sketchNode);

    /**
     * Rate composition. This function should not be called other than by
     * Composition::updateScore to prevent potentially unwanted score changes,
     * for example, those containing some random rating factors. One should only
     * retrieves scores from CompositionEval storage.
     *
     * @param composition
     * @return double value score ranging from 0.0 to 1.0.
     */
    public abstract double rateComposition(Composition composition);

    /**
     *
     * @param <M>
     * @param m
     */
    abstract public <M extends MusicMaterial> void matInitializer(M m);

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

}
