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
import tech.metacontext.ec.prototype.composer.model.Composition;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum ComposerAim implements Serializable {
    Phrase(8),
    Section(16),
    Movement(32),
    MultiMovement(-1);

    private final int aimSize;

    ComposerAim(int aimSize) {

        this.aimSize = aimSize;
    }

    public boolean isCompleted(Composition composition) {

        return composition.getConnectors().size() >= this.aimSize;
    }

    /*
     * Default getter.
     */
    public int getAimSize() {
        return aimSize;
    }
}
