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

import tech.metacontext.ec.prototype.composer.model.*;
import tech.metacontext.ec.prototype.composer.enums.mats.*;
import tech.metacontext.ec.prototype.composer.enums.*;
import tech.metacontext.ec.prototype.composer.materials.*;
import static tech.metacontext.ec.prototype.composer.Settings.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.DoubleAdder;
import static java.util.function.Predicate.not;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class GoldenSectionClimax extends Style {

    public static void main(String[] args) throws Exception {

        var gsc = new GoldenSectionClimax(UnaccompaniedCello.getRange());
        var composer = new Composer(100, ComposerAim.Phrase, LogState.DISABLED,
                0.95, 0.85,
                new UnaccompaniedCello(),
                gsc);
        DoubleSummaryStatistics summary;
        do {
            composer.sketch().evolve();
            summary = composer.getPopulation().stream()
                    .peek(c -> c.getRenderedChecked(null))
                    .mapToDouble(c -> c.getScore(gsc))
                    .summaryStatistics();
            System.out.printf("%.5f ~ %.5f\n", summary.getMin(), summary.getMax());
        } while (composer.getPopulation().stream().anyMatch(not(composer.getAim()::isCompleted))
                || summary.getMax() < 0.95 && composer.getConservatory().isEmpty());
        (composer.getConservatory().isEmpty()
                ? composer.getPopulation()
                : composer.getConservatory().keySet())
                .stream()
                .peek(c -> System.out.println(Composer.simpleScoreOutput(c)))
                .peek(gsc::updateClimaxIndexes)
                .forEach(c -> {
                    System.out.println("base = " + gsc.base);
                    var note_list = c.getRendered();
                    var sum = IntStream.range(0, c.getSize())
                            .mapToDouble(i
                                    -> Math.abs(gsc.climaxIndexes.get(i) - gsc.standards.get(i)) * gsc.standards.get(i))
                            .sum();
                    System.out.println("sum = " + sum);
                    System.out.println("rate = " + ((gsc.base - sum) / gsc.base));
                    IntStream.range(0, note_list.size())
                            .peek(i -> System.out.printf("%.2f -> ", gsc.getStandard(c, i)))
                            .mapToObj(gsc.getClimaxIndexes()::get)
                            .forEach(System.out::println);
                });
    }

    public static final double RATIO = 1.6180339887498948482;

    public final NoteRange lowest, highest;
    private List<Double> climaxIndexes;
    private List<Double> standards;
    private double peak;
    private double base;

    public GoldenSectionClimax(Collection<NoteRange> ranges) {

        TreeSet<NoteRange> sortedRanges = new TreeSet<>(ranges);
        this.lowest = sortedRanges.first();
        this.highest = sortedRanges.last();
    }

    /**
     * Golden Section Style is not about single SketchNodes.
     *
     * @param sketchNode
     * @return Always true.
     */
    @Override
    public boolean qualifySketchNode(SketchNode sketchNode) {

        return true;
    }

    @Override
    public double rateComposition(Composition composition) {

        this.updateClimaxIndexes(composition);
        double sum = IntStream.range(0, composition.getSize())
                .mapToDouble(i
                        -> Math.abs(climaxIndexes.get(i) - this.standards.get(i)))
                .sum();
        return (base - sum) / base;
    }

    public void updateClimaxIndexes(Composition composition) {

        this.climaxIndexes = composition
                .getRenderedChecked("GoldenSectionClimax::rateComposition")
                .stream()
                .map(this::climaxIndex)
                .collect(Collectors.toList());
        this.peak = climaxIndexes.stream()
                .max(Comparator.naturalOrder())
                .orElse(0.0);
        this.standards = IntStream.range(0, composition.getSize())
                .mapToDouble(i -> this.getStandard(composition, i))
                .boxed()
                .collect(Collectors.toList());
        this.base = this.standards.stream().collect(Collectors.summingDouble(d -> d));
    }

    public double getStandard(Composition composition, int i) {

        if (i < 0 || i > composition.getSize() - 1) {
            return 0.0;
        }
        long peakNodeIndex = Math.round((composition.getSize() - 1) / RATIO);
        return (i < peakNodeIndex)
                ? (i + 1) * peak / (peakNodeIndex + 1)
                : (composition.getSize() - i) * peak
                / (composition.getSize() - peakNodeIndex);
    }

    public double climaxIndex(SketchNode node) {

        DoubleAdder index = new DoubleAdder();
        node.getMats().forEach((mt, mm) -> {
            double mti = 0.0;
            switch (mt) {
                case DYNAMICS:
                    mti = ((Dynamics) mm).getAvgIntensityIndex(mat
                            -> Intensity.getIntensityIndex(mat, ((Dynamics) mm).getLowestIntensity(),
                                    ((Dynamics) mm).getHighestIntensity()));
                    break;
                case NOTE_RANGES:
                    mti = ((NoteRanges) mm).getAvgIntensityIndex(mat
                            -> NoteRanges.getIntensityIndex(mat, lowest, highest));
                    break;
                case PITCH_SETS:
                    mti = ((PitchSets) mm).getIntensityIndex();
                    break;
                case RHYTHMIC_POINTS:
                    var rp = (RhythmicPoints) mm;
                    mti = rp.getAvgIntensityIndex(mat
                            -> 1.0 * (mat - rp.getMin()) / (rp.getMax() - rp.getMin()));
                    break;
                default:
            }
            assert (mti >= 0.0 && mti <= 1.0) :
                    "mti not in range: " + mt + " = " + mti + "\n" + node;
            index.add(mti);
        });
        return index.doubleValue() / node.getMats().size();
    }

    public int compareToPeak(Composition o1, Composition o2) {

        this.updateClimaxIndexes(o1);
        double o1Peak = this.getPeak();
        this.updateClimaxIndexes(o2);
        double o2Peak = this.getPeak();
        return Double.compare(o1Peak, o2Peak);
    }

    @Override
    public <M extends MusicMaterial> void matInitializer(M m) {
    }

    /*
     * Default setters and getters.
     */
    public List<Double> getClimaxIndexes() {
        return climaxIndexes;
    }

    public void setClimaxIndexes(List<Double> climaxIndexes) {
        this.climaxIndexes = climaxIndexes;
    }

    public List<Double> getStandards() {
        return standards;
    }

    public void setStandards(List<Double> standards) {
        this.standards = standards;
    }

    public double getPeak() {
        return peak;
    }

    public void setPeak(double peak) {
        this.peak = peak;
    }

}
