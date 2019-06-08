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

import java.util.Random;
import static tech.metacontext.ec.prototype.composer.Parameters.*;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum TransformType {

    Repetition(CHANCE_REPETITION.getDouble()),
    MoveForward(CHANCE_MOVEFORWARD.getDouble()),
    MoveBackward(CHANCE_MOVEBACKWARD.getDouble()),
    Retrograde(CHANCE_RETROGRADE.getDouble()),
    Disconnected(CHANCE_DISCONNECTED.getDouble());

    double weighting;

    private TransformType(double weighting) {

        this.weighting = weighting;
    }

    public static TransformType getRandom() {

        return new Random().ints(0, values().length)
                .mapToObj(i -> values()[i])
                .filter(tt -> Math.random() < tt.weighting)
                .findFirst().get();
    }

}
