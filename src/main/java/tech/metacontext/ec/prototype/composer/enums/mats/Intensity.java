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
package tech.metacontext.ec.prototype.composer.enums.mats;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum Intensity {

    pppp, ppp, pp, p, mp, mf, f, ff, fff, ffff;

    public static double getIntensityIndex(Intensity intensity) {

        return 1.0 * intensity.ordinal() / (values().length - 1);
    }

    public static double getIntensityIndex(Intensity intensity,
            Intensity lowest, Intensity highest) {

        if (intensity.ordinal() > highest.ordinal()) {
            return 1.0;
        }
        if (intensity.ordinal() < lowest.ordinal()) {
            return 0.0;
        }
        return 1.0 * (intensity.ordinal() - lowest.ordinal()) / (highest.ordinal() - lowest.ordinal());
    }

    public static Intensity valueOf(int ordinal) {

        return Intensity.values()[ordinal];
    }

}
