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
package tech.metacontext.ec.prototype.composer.model;

import java.io.Serializable;
import java.util.HashMap;
import tech.metacontext.ec.prototype.composer.materials.MusicMaterial;
import tech.metacontext.ec.prototype.composer.enums.MaterialType;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tech.metacontext.ec.prototype.abs.Individual;
import tech.metacontext.ec.prototype.composer.factory.SketchNodeFactory;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class SketchNode extends Individual implements Serializable {

    public static void main(String[] args) {

        Stream.generate(SketchNodeFactory.getInstance()::newRandomInstance)
                .limit(50)
                .forEach(System.out::println);
    }

    private Map<MaterialType, ? extends MusicMaterial> musicMats;

    public SketchNode(String id) {

        super(id);
        this.musicMats = new HashMap<>();
    }

    public SketchNode() {

        this.musicMats = new HashMap<>();
    }

    public MusicMaterial getMat(MaterialType type) {

        return musicMats.get(type);
    }

    @Override
    public String toString() {
        return super.toString() + "{"
                + this.getMats().entrySet().stream()
                        .map(e -> String.format("%s(%d):%s",
                        /*..........*/ e.getKey(),
                        /*..........*/ e.getValue().getDivision(),
                        /*..........*/ e.getValue().getMaterials()))
                        .collect(Collectors.joining(", "))
                + "}";
    }

    /*
     * Default setters and getters
     */
    public Map<MaterialType, ? extends MusicMaterial> getMats() {
        return musicMats;
    }

    public void setMats(Map<MaterialType, ? extends MusicMaterial> mats) {
        this.musicMats = mats;
    }

}
