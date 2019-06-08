/*
 * Copyright 2018 Jonathan.
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
package tech.metacontext.ec.prototype.composer.enums;

import java.io.Serializable;
import java.util.function.Consumer;
import tech.metacontext.ec.prototype.composer.ex.InstantiationFailedException;
import tech.metacontext.ec.prototype.composer.materials.*;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum MaterialType implements Serializable{

    DYNAMICS(Dynamics.class),
    PITCH_SETS(PitchSets.class),
    RHYTHMIC_POINTS(RhythmicPoints.class),
    NOTE_RANGES(NoteRanges.class);

    Class<? extends MusicMaterial> clazz;

    MaterialType(Class<? extends MusicMaterial> clazz) {
        this.clazz = clazz;
    }

    public MusicMaterial getInstance() {

        try {
            return this.clazz.getDeclaredConstructor()
                    .newInstance();
        } catch (Exception ex) {
            System.out.println(this);
            throw new InstantiationFailedException(ex.getMessage());
        }
    }

    public <M extends MusicMaterial> M getInstance(Consumer<M> init) {

        try {
            M instance = (M) this.clazz.getDeclaredConstructor()
                    .newInstance();
            init.accept(instance);
            return (M) instance.generate();
        } catch (Exception ex) {
            System.out.println(this);
            throw new InstantiationFailedException(ex.getMessage());
        }
    }
}
