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
package tech.metacontext.ec.prototype.composer.factory;

import static tech.metacontext.ec.prototype.composer.Parameters.*;
import tech.metacontext.ec.prototype.composer.enums.mats.Pitch;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class PitchSetFactory {

    public static final int ALLOWED = 1, NOT_ALLOWED = 0;
    public static final int DEFAULT_ENHARMONIC_ALLOWED = NOT_ALLOWED;
    public static final int DEFAULT_SHARP_ALLOWED = ALLOWED;

    private int minPitchNumber, maxPitchNumber;
    private int pitchNumber;
    /**
     * 是否容許混合升記號音符
     */
    private boolean sharpAllowed;
    /**
     * 同音異名是否視為同音
     */
    private boolean enharmonicAllowed;
    /**
     * 保留音
     */
    private final Set<Pitch> presetPitches = new HashSet<>();

    public PitchSetFactory() {

        this.minPitchNumber = MIN_PITCHSET_NUMBER.getInt();
        this.maxPitchNumber = MAX_PITCHSET_NUMBER.getInt();
        this.pitchNumber = DEFAULT_PITCH_NUMBER.getInt();
        this.sharpAllowed = (DEFAULT_SHARP_ALLOWED == ALLOWED);
        this.enharmonicAllowed = (DEFAULT_ENHARMONIC_ALLOWED == ALLOWED);
        this.presetPitches.clear();
    }

    public void randomize() {

        this.pitchNumber = new Random().nextInt(
                this.maxPitchNumber - this.minPitchNumber + 1) + this.minPitchNumber;
        this.sharpAllowed = (DEFAULT_SHARP_ALLOWED == ALLOWED);
        this.enharmonicAllowed = (DEFAULT_ENHARMONIC_ALLOWED == ALLOWED);
        this.presetPitches.clear();
    }

    public List<Pitch> generate() {

        List<Pitch> ps = new ArrayList<>();
        //若不允許升記號則將preset中的升記號以降記號取代
        if (!this.sharpAllowed && !presetPitches.isEmpty()) {
            this.setPresetPitches(
                    this.presetPitches.stream()
                            .map(pitch -> Pitch.values()[pitch.ordinalEnharmonic()])
                            .collect(Collectors.toSet()));
        }
        ps.addAll(Stream.of(Pitch.values())
                .limit(this.sharpAllowed ? 17 : 12)
                .map(p -> new AbstractMap.SimpleEntry<>(this.presetPitches.contains(p) ? 0.0 : Math.random(), p))
                .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey()))
                .map(e -> (this.enharmonicAllowed) ? e.getValue().ordinal() : e.getValue().ordinalEnharmonic())
                .distinct()
                .map(i -> Pitch.values()[i])
                .limit(new Random().nextInt(this.maxPitchNumber - this.minPitchNumber + 1) + this.minPitchNumber)
                .collect(Collectors.toList())
        );
        return ps;
    }

    /*
     * Default setters and getters.
     */
    public boolean isSharpAllowed() {
        return sharpAllowed;
    }

    public void setSharpAllowed(boolean sharpAllowed) {
        this.sharpAllowed = sharpAllowed;
    }

    public boolean isEnharmonicAllowed() {
        return enharmonicAllowed;
    }

    public void setEnharmonicAllowed(boolean enharmonicAllowed) {
        this.enharmonicAllowed = enharmonicAllowed;
    }

    public Set<Pitch> getPreset() {
        return presetPitches;
    }

    public void setPresetPitches(Set<Pitch> preset) {
        this.presetPitches.clear();
        this.presetPitches.addAll(preset);
    }

    public int getMinPitchNumber() {
        return minPitchNumber;
    }

    public void setMinPitchNumber(int min) {
        this.minPitchNumber = min;
    }

    public int getMaxPitchNumber() {
        return maxPitchNumber;
    }

    public void setMaxPitchNumber(int max) {
        this.maxPitchNumber = max;
    }

    public int getPitchNumber() {
        return pitchNumber;
    }

    public void setPitchNumber(int number) {
        this.pitchNumber = number;
    }

}
