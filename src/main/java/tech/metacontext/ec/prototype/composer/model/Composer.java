/*3
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

import java.awt.BasicStroke;
import tech.metacontext.ec.prototype.draw.LineChart_AWT;
import tech.metacontext.ec.prototype.draw.ScatterPlot_AWT;
import tech.metacontext.ec.prototype.draw.CombinedChart_AWT;
import tech.metacontext.ec.prototype.abs.Population;
import tech.metacontext.ec.prototype.composer.ex.ConservationFailedException;
import tech.metacontext.ec.prototype.composer.operations.MutationType;
import tech.metacontext.ec.prototype.composer.materials.MusicMaterial;
import tech.metacontext.ec.prototype.composer.styles.*;
import tech.metacontext.ec.prototype.composer.factory.*;
import tech.metacontext.ec.prototype.composer.enums.*;
import static tech.metacontext.ec.prototype.composer.operations.MutationType.*;
import static tech.metacontext.ec.prototype.composer.Settings.*;
import static tech.metacontext.ec.prototype.composer.Parameters.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.ScatterRenderer;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Composer extends Population<Composition> implements Serializable {

    public static void main(String[] args) throws Exception {

        var id = "2c585484-92a5-4974-9cf3-3e9254114713";
        var path = Path.of(SER_PATH, id, "Composer.ser");
        Composer composer;
        try (var fis = Files.newInputStream(path);
                var ois = new ObjectInputStream(fis)) {
            composer = (Composer) ois.readObject();
        }
        composer.readArchive();
        composer.draw(DRAWTYPE_COMBINEDCHART);
    }

    private static CompositionFactory compositionFactory;
    private static ConnectorFactory connectorfactory;
    private static SketchNodeFactory sketchNodeFactory;

    private ComposerAim aim;
    private List<Style> styles;
    private int size;
    private double threshold;
    private double conserve_score;
    private Consumer<MusicMaterial> init;

    private final Map<Composition, Integer> conservatory = new HashMap<>();

    public boolean ARCHIVE_TO_DISK = true;
    public static final int SELECT_FROM_ALL = 0, SELECT_ONLY_COMPLETED = 1;
    public static final int DRAWTYPE_SCATTERPLOT = 0,
            DRAWTYPE_AVERAGELINECHART = 1,
            DRAWTYPE_COMBINEDCHART = 2;

    /**
     * Constructor for loading data from serialized objects.
     *
     * @param id
     * @param size
     * @param aim
     * @param logState
     * @param styles
     * @throws java.lang.Exception
     */
    public Composer(String id, int size, ComposerAim aim, LogState logState,
            Style... styles) throws Exception {

        super(id);
        setup(size, aim, logState, styles);
        this.readArchive();
        this.setGenCount(this.getArchive().size());
    }

    /**
     * Constructor.
     *
     * @param size
     * @param logState: USE_EXISTING = 0, RENEW = 1, RENEW_TEST = 2;
     * @param aim
     * @param styles
     * @throws java.lang.Exception
     */
    public Composer(int size, ComposerAim aim, LogState logState, Style... styles)
            throws Exception {

        this(size, aim, logState,
                SELECTION_THRESHOLD.getDouble(),
                MIN_CONSERVE_SCORE.getDouble(),
                styles);
    }

    public Composer(int size, ComposerAim aim, LogState logState,
            double threshold, double conserve_score, Style... styles)
            throws Exception {

        setup(size, aim, logState, styles);
        getLogger().log(Level.INFO,
                "Initializing Composition Population...");
        this.setPopulation(Stream.generate(() -> compositionFactory.newInstance())
                .limit(size)
                .peek(c -> c.addDebugMsg("Initialization..."))
                .collect(Collectors.toList()));
        getLogger().log(Level.INFO,
                "Composer created: size = {0}, aim = {1}, styles = {2}",
                new Object[]{size, aim, this.styles.stream()
                            .map(style -> style.getClass().getSimpleName())
                            .collect(Collectors.joining(", "))});
        this.threshold = threshold;
        this.conserve_score = conserve_score;
    }

    private void setup(int size, ComposerAim aim, LogState logState, Style... styles)
            throws Exception {

        this.size = size;
        this.aim = aim;

        setFileHandler(logState, getLogger());
        getLogger().log(Level.INFO,
                "Initilizing Composer [{0}]", this.getId());
        this.styles = new ArrayList<>(Arrays.asList(styles));
        this.init = mm -> {
            for (Style style : styles) {
                style.matInitializer(mm);
            }
        };
        getLogger().log(Level.INFO,
                "Initializing ConnectorFactory...");
        Composer.connectorfactory = ConnectorFactory.getInstance();
        getLogger().log(Level.INFO,
                "Initializing CompositionFactory...");
        Composer.compositionFactory = CompositionFactory.getInstance(this);
        getLogger().log(Level.INFO,
                "Initializing SketchNodeFactory...");
        Composer.sketchNodeFactory = SketchNodeFactory.getInstance();
    }

    public void readArchive() {

        this.readArchive(Path.of(SER_PATH, this.getId()));
        this.getArchive().stream()
                .forEach(list
                        -> list.stream()
                        .peek(c -> c.setComposer(this))
                        .forEach(Composition::updateEval));
    }

    public void save() {

        var path = Path.of(SER_PATH, this.getId(), "Composer.ser");
        try (var os = Files.newOutputStream(path);
                var ois = new ObjectOutputStream(os)) {
            ois.writeObject(this);
        } catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, "Error when saving Composer to {0}", path);
        }
    }

    public Composer sketch() {

        if (ARCHIVE_TO_DISK) {
            archive(Path.of(SER_PATH, this.getId(), "" + this.getGenCount()), this.getPopulation());
        } else {
            this.archive(compositionFactory);
        }

        var num_elongated = this.getPopulation().stream()
                .parallel()
                .filter(this::toBeElongated)
                .peek(c -> getLogger().log(Level.INFO, "Composition {0} been elongated.", c.getId_prefix()))
                .sequential()
                .collect(Collectors.counting());
        getLogger().log(Level.INFO,
                "Composing, totally {0} Compositions been elongated.", num_elongated);

        int original = this.getSize();
        this.getPopulation().removeIf(this::conserve);
        if (original - this.getSize() > 0) {
            getLogger().log(Level.INFO,
                    "Composing, {0} Composition(s) conserved.",
                    original - this.getSize());
        }
        return this;
    }

    /**
     * Decide if a composition to be elongated. It's always true if the
     * composition has not been completed. Otherwise, it's decided by
     * ELONGATION_CHANCE.
     *
     * @param composition
     * @return true: to be elongated. false: not to be elongated.
     */
    private boolean toBeElongated(Composition c) {

        if (aim.isCompleted(c) && Math.random()
                >= Math.pow(CHANCE_ELONGATION_IF_COMPLETED.getDouble(),
                        c.getSize() - this.getAim().getAimSize() - 1)) {
            return false;
        }
        c.elongate();
        return true;
    }

    @Override
    public void evolve() {

        getLogger().log(Level.INFO,
                "Evolving from {0} parents.", this.getPopulationSize());
        var children = Stream.generate(this::getChild)
                .parallel()
                .filter(c -> !this.conserve(c))
                .limit(size)
                .sequential()
                .collect(Collectors.toList());
        getLogger().log(Level.INFO,
                "Evloving finished, gen = {0}, size = {1}, {2}",
                new Object[]{this.getGenCount(),
                    children.size(),
                    getSummary(children)});
        this.setPopulation(children);
        this.genCountIncrement();
    }

    public static String getSummary(List<Composition> list) {

        return list.stream()
                .collect(Collectors.groupingBy(Composition::getSize))
                .entrySet().stream()
                .map(e -> String.format("%2d x %2d", e.getKey(), e.getValue().size()))
                .collect(Collectors.joining(", "));
    }

    /**
     * Get child via mutation or crossover.
     *
     * @return the child produced.
     */
    public Composition getChild() {

        /*
            1.若p0 not completed則mutate -> children
            2.若completed則仍有一定機率走mutate -> children
            3.若則選出另一條p1 completed(不能是自己), crossover -> children
         */
        var p0 = select(SELECT_FROM_ALL, this.threshold);
        if (this.getAim().isCompleted(p0)
                && Math.random() < CHANCE_CROSSOVER_IF_COMPLETED.getDouble()) {
            var p1 = this.select(SELECT_ONLY_COMPLETED, this.threshold);
            if (!Objects.equals(p0, p1)) {
                return this.crossover(p0, p1);
            }
        }
        return this.mutate(p0);
    }

    public Composition mutate(Composition origin) {

        var mutant = compositionFactory.forMutation(origin);
        getLogger().log(Level.INFO,
                "Composition {0} being duplicated to {1} for mutation.",
                new Object[]{origin.getId_prefix(), mutant.getId_prefix()});
        int selected = new Random().nextInt(mutant.getSize() - 1);
        var type = MutationType.getRandom();
        switch (type) {
            case Alteration:
                mutant.getConnectors().set(selected,
                        connectorfactory.newConnector());
                break;
            case Insertion:
                if (!this.getAim().isCompleted(origin)) {
                    mutant.getConnectors().add(selected,
                            connectorfactory.newConnector());
                    break;
                }
                type = Deletion;
            case Deletion:
                mutant.getConnectors().remove(selected);
                break;
        }
        boolean reseeding = Math.random() < CHANCE_RESEEDING.getDouble();
        if (reseeding) {
            mutant.resetSeed(sketchNodeFactory.newInstance(init));
        }
        getLogger().log(Level.INFO,
                "Mutation, mutant: {0}, type: {1}, loci: {2}, reseed = {3}, length: {4} -> {5}",
                new Object[]{
                    mutant.getId_prefix(),
                    type, selected,
                    origin.getSize(),
                    reseeding,
                    mutant.getSize()});
        return mutant;
    }

    public Composition crossover(Composition p0, Composition p1) {

        int index = 1;
        var child = compositionFactory.forCrossover(
                p0.getConnectors().get(0),
                this.styles);

        getLogger().log(Level.INFO,
                "Composition {0} being transformed to {1} for crossover.",
                new Object[]{p0.getId_prefix(), child.getId_prefix()});
        String crossover_state = "X";
        do {
            var activated = new Random().nextBoolean()
                    ? ((p0.getSize() - 1 > index) ? p0 : p1)
                    : ((p1.getSize() - 1 > index) ? p1 : p0);
            child.addConnector(connectorfactory
                    .forMutation(activated.getConnectors().get(index)));
            crossover_state += (Objects.equals(activated, p0)) ? "X" : "Y";
        } while (++index < Math.max(p0.getSize() - 1, p1.getSize() - 1));
        getLogger().log(Level.INFO,
                "Crossover, [{0}, {1}] -> {2} = {3}", new Object[]{
                    p0.getId_prefix(),
                    p1.getId_prefix(),
                    child.getId_prefix(),
                    crossover_state});
        boolean reseeding = Math.random() < CHANCE_RESEEDING.getDouble();
        if (reseeding) {
            child.resetSeed(sketchNodeFactory.newInstance(init));
        }
        return child;
    }

    @Override
    public Composition select(Predicate<Composition> criteria, double threshold) {

        var subset = this.getPopulation().stream()
                .filter(criteria)
                .peek(Composition::updateEval)
                .sorted((c1, c2) -> (int) (this.getMinScore(c2) - this.getMinScore(c1)))
                .collect(Collectors.toList());
        if (subset.isEmpty() || threshold > 1.0 || threshold < 0.0) {
            return null;
        }
        int thresholdIndex = (int) ((subset.size() - 1) * threshold);
        double std = this.getMinScore(subset.get(thresholdIndex));
        var selectedSubset = subset.stream()
                .filter(c -> this.getMinScore(c) >= std)
                .collect(Collectors.toList());
        /*
        subsetsize = 100
        threshold = 7.5
        thresholdIndex = 100*7.5 = 75
        filtered subset size = 25
        subset.size - thresholdIndex = 25.
         */
        return selectedSubset.get(new Random().nextInt(selectedSubset.size()));
    }

    /**
     * Randomly select composition from population with specified state and
     * threshold.
     *
     * @param state SELECT_FROM_ALL = 0, SELECT_ONLY_COMPLETED = 1.
     * @param threshold in percentage. For eg., 0.9 stands for that selected
     * score must be higher than 90% population.
     * @return Selected composition.
     */
    public Composition select(int state, double threshold) {

        return select(c -> state == SELECT_FROM_ALL || this.getAim().isCompleted(c),
                threshold);
    }

    /**
     * Conserve qualified composition into conservatory.
     *
     * @param c composition under check to be conserved.
     * @return TRUE: if successfully conserved; FALSE: if not conserved.
     * @throws ConservationFailedException
     */
    public boolean conserve(Composition c) throws ConservationFailedException {

        if (!this.getAim().isCompleted(c)) {
            return false;
        }
        c.getRenderedChecked(this.getClass().getSimpleName() + "::conserve");
        c.addDebugMsg("under conservation check.");
        if (getMinScore(c) < conserve_score) {
            c.addDebugMsg("fail conservation check: " + simpleScoreOutput(c));
            return false;
        }
        c.addDebugMsg("pass conservation check: " + simpleScoreOutput(c));
        getLogger().log(Level.INFO, "Qualified Composition been located: {0}",
                simpleScoreOutput(c));
        getLogger().log(Level.INFO,
                "Composition {0} being duplicated for conservation.",
                c.getId_prefix());
        Composition dupe = compositionFactory.forArchiving(c);
        if (Objects.nonNull(this.conservatory.put(dupe, this.getGenCount()))) {
            getLogger().log(Level.WARNING,
                    "Conserving with an Id already existing in conservatory: {0}",
                    c.getId_prefix());
        }
        if (this.conservatory.containsKey(dupe)) {
            getLogger().log(Level.INFO,
                    "Composition {0} been conserved.",
                    c.getId_prefix());
        } else {
            throw new ConservationFailedException(
                    "id = " + dupe.getId_prefix() + ", gen = " + this.getGenCount());
        }
        return true;
    }

    @Override
    public void draw(int type) {

        getLogger().log(Level.INFO, "Drawing Composer {0}", this.getId());
        switch (type) {
            case 0:
                drawScatterPlot();
                break;
            case 1:
                drawAvgLineChart();
                break;
            case 2:
                drawCombinedChart();
                break;
        }
    }

    public void drawCombinedChart() {

        var chart = new CombinedChart_AWT("Composer " + this.getId());
        var list = new ArrayList<Composition>();
        var avgs = IntStream.range(0, this.getArchive().size())
                .boxed()
                .peek(i -> {
                    list.clear();
                    list.addAll(this.getArchive().get(i));
                    list.addAll(this.conservatory.entrySet().stream()
                            .filter(e -> e.getValue().equals(i))
                            .map(e -> e.getKey())
                            .collect(Collectors.toList()));
                })
                .collect(Collectors.toMap(
                        Function.identity(),
                        i -> list.stream()
                                /*...*/.mapToDouble(this::getMinScore)
                                /*...*/.filter(score -> score > 0.0)
                                /*...*/.average().orElse(0.0)));

        var xys = new HashMap<Integer, List<Double>>();
        var xyc = new HashMap<Integer, List<Double>>();
        IntStream.range(0, this.getArchive().size())
                .forEach(i -> {
                    xys.put(i, this.getArchive().get(i).stream()
                            .map(this::getMinScore)
                            .filter(score -> score > 0.0)
                            .collect(Collectors.toList()));
                    xyc.put(i, this.getConservatory().entrySet().stream()
                            .filter(e -> e.getValue() == i)
                            .map(e -> e.getKey())
                            .map(this::getMinScore)
                            .collect(Collectors.toList()));
                });
        double dotSize0 = 3.0;
        double dotSize1 = 4.0;
        double delta0 = dotSize0 / 2.0;
        double delta1 = dotSize1 / 2.0;
        var shape0 = new Ellipse2D.Double(-delta0, -delta0, dotSize0, dotSize0);
        var shape1 = new Ellipse2D.Double(-delta1, -delta1, dotSize1, dotSize1);
        var scatterRenderer0 = new ScatterRenderer();
        var scatterRenderer1 = new ScatterRenderer();
        scatterRenderer0.setSeriesPaint(0, Color.LIGHT_GRAY);
        scatterRenderer0.setSeriesShape(0, shape0);
        scatterRenderer1.setSeriesPaint(0, Color.RED);
        scatterRenderer1.setSeriesShape(0, shape1);
        chart.addRenderer(
                new String[]{"score", "conservatory"},
                new CategoryItemRenderer[]{scatterRenderer0, scatterRenderer1},
                xys, xyc);
        var lineAndShapeRenderer = new LineAndShapeRenderer();
        lineAndShapeRenderer.setDefaultShapesVisible(false);
        lineAndShapeRenderer.setSeriesPaint(0, Color.BLUE);
        lineAndShapeRenderer.setSeriesShapesVisible(0, false);
        lineAndShapeRenderer.setSeriesStroke(0, new BasicStroke(2.0f));
        chart.addRenderer(2, "average", lineAndShapeRenderer, avgs);
        chart.addMarker(this.conserve_score, Color.BLACK);
        chart.createChart("Evolutionary Computation", "Generation", "Score", 1600, 630, true);
        chart.showChartWindow();
    }

    public void drawAvgLineChart() {

        var chart = new LineChart_AWT("Composer " + this.getId());
        LineChart_AWT chartStat = new LineChart_AWT("Composer " + this.getId());
        IntStream.range(0, this.getArchive().size())
                .forEach(i -> {
                    List<Composition> list = this.getArchive().get(i);
                    List<Double> values = list.stream()
                            .map(this::getMinScore)
                            .filter(score -> score > 0.0)
                            .collect(Collectors.toList());
                    chart.addData(values, "average", "" + i);
                    chartStat.addStatData(values, "score", "" + i);
                });
        chart.createLineChart("Evolutionary Computation",
                "Generation", "Score",
                1600, 630, true);
        chart.showChartWindow();

        chartStat.createStatLineChart("Evolutionary Computation",
                "Generation", "Score",
                1600, 630, true);
        chartStat.showChartWindow();
    }

    public void drawScatterPlot() {

        var plot = new ScatterPlot_AWT("Composer " + this.getId());
        var popScores = IntStream.range(0, this.getArchive().size())
                .mapToObj(i
                        -> this.getArchive().get(i).stream()
                        .map(this::getMinScore).filter(score -> score > 0.0)
                        .map(score -> new SimpleEntry<>(i, score)))
                .flatMap(s -> s)
                .collect(Collectors.toList());
        plot.addSeries("Population", popScores);
        List<SimpleEntry<Integer, Double>> conserveScores = this.getConservatory().entrySet().stream()
                .map(e -> new SimpleEntry<>(e.getValue(), this.getMinScore(e.getKey())))
                .collect(Collectors.toList());
        plot.addSeries("Conservatory", conserveScores);
        plot.createScatterPlot("Evolutionary Computation",
                "Generation", "Score",
                1600, 630, true);
        plot.setSeriesDot(0, 3.0, Color.GRAY);
        plot.setSeriesDot(1, 3.0, Color.RED);
        plot.addHorizontalLine(this.conserve_score, Color.BLACK);
        plot.showPlotWindow();
    }
  
    public double getMinScore(Composition c) {

        return this.getAim().isCompleted(c)
                ? c.getEval().getScores().values().stream()
                        .mapToDouble(s -> s)
                        .min().getAsDouble()
                : 0.0;
    }

    public static String simpleScoreOutput(Composition... list) {

        StringBuilder report = new StringBuilder();
        Stream.of(list).forEach(composition -> report
                .append(composition.getId_prefix()).append(" ")
                .append(composition.getEval().getScores().entrySet().stream()
                        .map(e -> String.format("%s: %.3f", e.getKey(), e.getValue()))
                        .collect(Collectors.joining(" | "))));
        return report.toString();
    }

    public void persistAll() {

        this.getConservatory().keySet().stream()
                .forEach(Composition::persist);
    }

    public void addStyle(Style style) {

        this.styles.add(style);
    }

    /*
     * Default getters and setters.
     */
    public List<? extends Style> getStyles() {
        return this.styles;
    }

    public void setStyles(List<Style> styles) {
        this.styles = styles;
    }

    public ComposerAim getAim() {
        return aim;
    }

    public void setAim(ComposerAim aim) {
        this.aim = aim;
    }

    public Map<Composition, Integer> getConservatory() {
        return conservatory;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getConserve_score() {
        return conserve_score;
    }

    public void setConserve_score(double conserve_score) {
        this.conserve_score = conserve_score;
    }

    public Consumer<MusicMaterial> getInit() {
        return init;
    }

    public void setInit(Consumer<MusicMaterial> init) {
        this.init = init;
    }
}
