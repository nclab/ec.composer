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

import tech.metacontext.ec.prototype.abs.Individual;
import tech.metacontext.ec.prototype.composer.factory.SketchNodeFactory;
import tech.metacontext.ec.prototype.composer.materials.MusicMaterial;
import tech.metacontext.ec.prototype.composer.enums.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Connector extends Individual {

    public static void main(String[] args) {
        var conn = new Connector();
        Stream.of(MaterialType.values()).forEach(mt
                -> conn.addTransformType(mt, TransformType.getRandom()));
        System.out.println(conn);
    }

    private static SketchNodeFactory sketchNodeFactory;

    private final Map<MaterialType, TransformType> transformTypes;
    private SketchNode previous;
    private SketchNode next;

    public Connector() {

        this.transformTypes = new HashMap<>();
        sketchNodeFactory = SketchNodeFactory.getInstance();

    }

    public Connector(String id) {

        super(id);
        this.transformTypes = new HashMap<>();
        sketchNodeFactory = SketchNodeFactory.getInstance();
    }

    public void addTransformType(MaterialType mt, TransformType tt) {

        this.transformTypes.put(mt, tt);
    }

    public SketchNode transform() {

        if (this.previous == null) {
            return null;
        }
        this.next = sketchNodeFactory.newInstance();

        Map<MaterialType, ? extends MusicMaterial> mats
                = this.getTransformTypes().entrySet().stream()
                        .map(e -> new SimpleEntry<>(e.getKey(), this.previous.getMat(e.getKey()).transform(e.getValue())))
                        .collect(Collectors.toMap(SimpleEntry::getKey,
                                SimpleEntry::getValue));
        this.next.setMats(mats);
        return this.next;
    }

    @Override
    public String toString() {

        return super.toString() + getTransformTypes();
    }

    public String toStringNext() {

        return super.toString() + getTransformTypes() + " "
                + ((next == null) ? "" : "\n => " + next);
    }

    /*
     * Default setters and getters
     */
    public SketchNode getPrevious() {
        return previous;
    }

    public void setPrevious(SketchNode previous) {
        this.previous = previous;
    }

    public SketchNode getNext() {
        return next;
    }

    public void setNext(SketchNode next) {
        this.next = next;
    }

    public Map<MaterialType, TransformType> getTransformTypes() {
        return transformTypes;
    }

}
