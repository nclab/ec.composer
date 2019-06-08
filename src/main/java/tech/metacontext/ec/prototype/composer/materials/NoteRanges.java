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
package tech.metacontext.ec.prototype.composer.materials;

import java.io.Serializable;
import java.util.List;
import tech.metacontext.ec.prototype.composer.enums.TransformType;
import tech.metacontext.ec.prototype.composer.enums.mats.*;
import static tech.metacontext.ec.prototype.composer.Parameters.*;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class NoteRanges extends MusicMaterial<List<NoteRange>> implements Serializable {

    private NoteRange lowestRange;
    private NoteRange highestRange;

    public NoteRanges() {
    }

    public NoteRanges(NoteRanges origin) {

        super(origin.getDivision(), origin.getMaterials());
        this.lowestRange = origin.lowestRange;
        this.highestRange = origin.highestRange;
    }

    @Override
    public NoteRanges duplicate() {

        return new NoteRanges(this);
    }

    @Override
    public NoteRanges reset() {

        this.setDivision(DEFAULT_DIVISION.getInt());
        this.lowestRange = NoteRange.valueOf(LOWEST_RANGE.getInt());
        this.highestRange = NoteRange.valueOf(HIGHEST_RANGE.getInt());
        return this;
    }

    @Override
    public NoteRanges generate() {

        int highest = this.highestRange.ordinal(), lowerest = this.lowestRange.ordinal();
        this.setMaterials(
                new Random().ints(this.getDivision(), lowerest, highest + 1)
                        .mapToObj(lowerBond
                                -> IntStream.rangeClosed(lowerBond, new Random().nextInt(highest - lowerBond + 1) + lowerBond)
                                .mapToObj(NoteRange::valueOf)
                                .collect(Collectors.toList()))
                        .collect(Collectors.toList())
        );
        return this;
    }

    @Override
    public NoteRanges random() {

        this.setDivision(new Random()
                .nextInt(MAX_DIVISION.getInt()
                        - MIN_DIVISION.getInt() + 1)
                + MIN_DIVISION.getInt());
        return generate();
    }

    @Override
    public NoteRanges transform(TransformType type) {

        switch (type) {
            case Repetition:
                return new NoteRanges(this);
            case Retrograde:
                return new NoteRanges(this).retrograde();
            case MoveForward:
                return new NoteRanges(this).moveForward();
            case MoveBackward:
                return new NoteRanges(this).moveBackward();
            case Disconnected:
                return new NoteRanges();
        }
        return null;
    }

    private NoteRanges retrograde() {

        this.setMaterials(IntStream.range(0, this.size())
                .mapToObj(i -> this.getMaterials().get(this.size() - i - 1))
                .collect(Collectors.toList()));
        return this;
    }

    private NoteRanges moveForward() {

        this.setMaterials(this.getMaterials().stream()
                .map(rlist -> rlist.stream().map(r -> r.forward(this.highestRange)).collect(Collectors.toList()))
                .collect(Collectors.toList()));
        return this;
    }

    private NoteRanges moveBackward() {

        this.setMaterials(this.getMaterials().stream()
                .map(rlist -> rlist.stream().map(r -> r.backward(this.lowestRange)).collect(Collectors.toList()))
                .collect(Collectors.toList()));
        return this;
    }

    public static double getIntensityIndex(List<NoteRange> rangeSet,
            NoteRange lowest, NoteRange highest) {

        int low = lowest.ordinal(),
                hi = highest.ordinal();
        return IntStream.rangeClosed(low, hi)
                .filter(i -> rangeSet.contains(NoteRange.valueOf(i)))
                .mapToDouble(i -> Math.pow(2, i - low))
                .sum() / getBase(hi - low);
    }

    public static int getBase(int coverage) {

        return IntStream.rangeClosed(0, coverage)
                .map(p -> (int) Math.pow(2, p))
                .sum();
    }

    @Override
    public String toString() {
        
        return this.getMaterials().toString();
    }

    /*
     * Default setters and getters.
     */
    public NoteRange getLowest() {
        return lowestRange;
    }

    public void setLowest(NoteRange lowest) {
        this.lowestRange = lowest;
    }

    public NoteRange getHighest() {
        return highestRange;
    }

    public void setHighest(NoteRange highest) {
        this.highestRange = highest;
    }

}
