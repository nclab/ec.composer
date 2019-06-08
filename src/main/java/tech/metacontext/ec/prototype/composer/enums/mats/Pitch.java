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
package tech.metacontext.ec.prototype.composer.enums.mats;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public enum Pitch {

    //0, 1, 2, 3,
    C("1"), D_flat("2b"), D("2"), E_flat("3b"),
    //4, 5, 6, 7, 
    E("3"), F("4"), G_flat("5b"), G("5"),
    //8, 9, 10, 11,
    A_flat("6b"), A("6"), B_flat("7b"), B("7"),
    //12, 13, 14, 15, 16 
    C_sharp("1#"), D_sharp("2#"), F_sharp("4#"), G_sharp("5#"), A_sharp("6#");

    String simple;

    Pitch(String simple) {

        this.simple = simple;
    }

    public int ordinalEnharmonic() {

        switch (this.ordinal()) {
            case 12:
                return 1;
            case 13:
                return 3;
            case 14:
                return 6;
            case 15:
                return 8;
            case 16:
                return 10;
            default:
                return this.ordinal();
        }
    }

    public int compareToPitch(Pitch o) {

        return this.ordinalEnharmonic() - o.ordinalEnharmonic();
    }

    public Pitch forward() {

        return Pitch.values()[(this.ordinalEnharmonic() + 1) % 12];
    }

    public Pitch backward() {

        return Pitch.values()[(this.ordinalEnharmonic() + 11) % 12];
    }
}
