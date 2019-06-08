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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import tech.metacontext.ec.prototype.composer.model.Composition;
import tech.metacontext.ec.prototype.composer.model.SketchNode;
import tech.metacontext.ec.prototype.composer.enums.mats.NoteRange;
import tech.metacontext.ec.prototype.composer.enums.MaterialType;
import tech.metacontext.ec.prototype.composer.factory.SketchNodeFactory;
import tech.metacontext.ec.prototype.composer.materials.MusicMaterial;
import tech.metacontext.ec.prototype.composer.materials.NoteRanges;
import tech.metacontext.ec.prototype.composer.materials.PitchSets;
import tech.metacontext.ec.prototype.composer.materials.RhythmicPoints;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class UnaccompaniedCello extends Style {

    /**
     * 音域
     */
    public static final Map<NoteRange, Double> RANGE = new HashMap<>();

    static {
        RANGE.put(NoteRange.C2, 1.0);
        RANGE.put(NoteRange.C3, 1.0);
        RANGE.put(NoteRange.C4, 1.0);
        RANGE.put(NoteRange.C5, 0.155);
        RANGE.put(NoteRange.C6, 0.027);
    }

    public static void main(String[] args) {
        var sketchnode_factory = SketchNodeFactory.getInstance();
        var instance = new UnaccompaniedCello();
        Stream.generate(() -> sketchnode_factory.newInstance(instance::matInitializer))
                .limit(50)
                .peek(node -> System.out.println(//node.getMat(MaterialType.NOTE_RANGES) + "\n" + 
                node.getMat(MaterialType.PITCH_SETS)
                + "\n" + node.getMat(MaterialType.RHYTHMIC_POINTS)))
                .map(instance::qualifySketchNode)
                .forEach(System.out::println);
    }

    @Override
    public boolean qualifySketchNode(SketchNode sketchNode) {

        boolean inrange = ((NoteRanges) sketchNode.getMat(MaterialType.NOTE_RANGES))
                .getMaterials()
                .stream()
                .allMatch(list -> list.stream().allMatch(RANGE::containsKey));
        boolean chance = inrange
                ? ((NoteRanges) sketchNode.getMat(MaterialType.NOTE_RANGES))
                        .getMaterials()
                        .stream()
                        .mapToDouble(list -> list.stream().mapToDouble(RANGE::get).average().getAsDouble())
                        .average().getAsDouble() > Math.random()
                : false;
        PitchSets ps = (PitchSets) sketchNode.getMat(MaterialType.PITCH_SETS);
        RhythmicPoints rp = (RhythmicPoints) sketchNode.getMat(MaterialType.RHYTHMIC_POINTS);

        if (ps.getDivision() == rp.getDivision()) {
            for (var i = 0; i < ps.getDivision(); i++) {
                if (ps.getMaterials().get(i).size() > rp.getMaterials().get(i) * 2) {
                    return false;
                }
                System.out.println("ps.getMaterials().get(i).size() > rp.getMaterials().get(i) * 2 == false");
            }
        }
        return inrange && chance;
    }

    @Override
    public double rateComposition(Composition composition) {

        if (composition.getRenderedChecked(this.getClass().getSimpleName() + "::rateComposition")
                .stream()
                .map(node -> ((NoteRanges) node.getMat(MaterialType.NOTE_RANGES)))
                .map(mm -> mm.getMaterials())
                .flatMap(lnrs -> lnrs.stream())
                .flatMap(nrs -> nrs.stream())
                .allMatch(RANGE::containsKey)) {
            return 1.0;
        }
        return 0.0;
    }

    public static Collection<NoteRange> getRange() {

        return RANGE.keySet();
    }

    @Override
    public <M extends MusicMaterial> void matInitializer(M mat) {

        if (mat instanceof NoteRanges) {
            ((NoteRanges) mat).setHighest(RANGE.keySet().stream().max(NoteRange::compareTo).get());
            ((NoteRanges) mat).setLowest(RANGE.keySet().stream().min(NoteRange::compareTo).get());
        }
    }
}
