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
package tech.metacontext.ec.prototype.composer.factory;

import tech.metacontext.ec.prototype.abs.Factory;
import tech.metacontext.ec.prototype.composer.model.SketchNode;
import tech.metacontext.ec.prototype.composer.enums.MaterialType;
import tech.metacontext.ec.prototype.composer.enums.mats.Intensity;
import tech.metacontext.ec.prototype.composer.materials.*;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class SketchNodeFactory implements Factory<SketchNode> {

    public static void main(String[] args) {

        var instance = SketchNodeFactory.getInstance();
        System.out.println(instance.newInstance((MusicMaterial mm) -> {
            mm.setDivision(2);
            if (mm instanceof Dynamics) {
                ((Dynamics) mm).setLowestIntensity(Intensity.ppp);
                ((Dynamics) mm).setHighestIntensity(Intensity.fff);
            }
        }));
    }

    private static SketchNodeFactory instance;

    private SketchNodeFactory() {
    }

    public static SketchNodeFactory getInstance() {
        if (Objects.isNull(instance)) {
            instance = new SketchNodeFactory();
        }
        return instance;
    }

    @Override
    public SketchNode forArchiving(SketchNode origin) {

        SketchNode node = new SketchNode(origin.getId());
        node.setMats(origin.getMats().entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> e.getValue().duplicate())));
        return node;
    }

    public SketchNode forMutation(SketchNode origin) {

        SketchNode node = new SketchNode();
        node.setMats(origin.getMats().entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> e.getValue().duplicate())));
        return node;
    }

    public SketchNode newRandomInstance() {

        SketchNode newInstance = new SketchNode();
        newInstance.setMats(Stream.of(MaterialType.values())
                .collect(Collectors.toMap(
                        t -> t,
                        t -> t.getInstance().random())));
        return newInstance;
    }

    public SketchNode newInstance() {

        SketchNode newInstance = new SketchNode();
        newInstance.setMats(Stream.of(MaterialType.values())
                .collect(Collectors.toMap(
                        t -> t,
                        t -> t.getInstance())));
        return newInstance;
    }

    public SketchNode newInstance(Consumer<MusicMaterial> init) {

        SketchNode newInstance = new SketchNode();
        newInstance.setMats(Stream.of(MaterialType.values())
                .collect(Collectors.toMap(mt -> mt,
                        mt -> mt.getInstance(init))));
        return newInstance;
    }

}
