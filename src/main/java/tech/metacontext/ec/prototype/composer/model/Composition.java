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

import tech.metacontext.ec.prototype.composer.styles.Style;
import tech.metacontext.ec.prototype.abs.*;
import tech.metacontext.ec.prototype.composer.*;
import tech.metacontext.ec.prototype.composer.factory.*;
import static tech.metacontext.ec.prototype.composer.Settings.*;
import static tech.metacontext.ec.prototype.composer.Parameters.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class Composition extends Individual<CompositionEval> implements Serializable {

    public static void main(String[] args) throws Exception {
        Main main = new Main(100, 1, 100, LogState.TEST);
        Composer composer = main.getComposer();
        Composition p0, p1;
        do {
            p0 = composer.select(Composer.SELECT_ONLY_COMPLETED, SELECTION_THRESHOLD.getDouble());
            p1 = composer.select(Composer.SELECT_ONLY_COMPLETED, SELECTION_THRESHOLD.getDouble());
        } while (Objects.equals(p0, p1));
        Composition child, dupe;
        int counter = 0;
        CompositionFactory cf = CompositionFactory.getInstance(composer);
        do {
            System.out.print(".");
            child = composer.crossover(p0, p1);
            child.getRenderedChecked("Composition::main");
            dupe = cf.forArchiving(child);
            if (counter++ % 50 == 0) {
                System.out.println();
            }
        } while (!dupe.ifReRenderRequired());
    }

    private static ConnectorFactory connectorFactory = ConnectorFactory.getInstance();
    private static SketchNodeFactory sketchNodeFactory = SketchNodeFactory.getInstance();
    private LinkedList<Connector> connectors;
    private LinkedList<SketchNode> rendered;
    private SketchNode seed;
    private transient Composer composer;

    /**
     * Constructor with id specified.
     *
     * @param composer
     * @param id
     */
    public Composition(Composer composer, String id) {

        super(id);
        setup(composer);
    }

    /**
     * Constructor without id specified.
     *
     * @param composer
     * @param styles
     */
    public Composition(Composer composer) {

        setup(composer);
    }

    public void setup(Composer composer) {

        this.composer = composer;
        this.rendered = new LinkedList<>();
        this.connectors = new LinkedList<>();
        this.setEval(new CompositionEval(composer.getStyles()));
        //for debugging
        this.debug = new ArrayList<>();
        this.addDebugMsg("Initilization completed.");
    }

    public void elongate() {

        this.addConnector(connectorFactory.newConnector());
    }

    public void addConnector(Connector connector) {

        this.connectors.add(connector);
    }

    public List<SketchNode> render() {

        rendered.clear();
        if (Objects.isNull(seed) || Math.random() < CHANCE_RESEEDING.getDouble()) {
            resetSeed(sketchNodeFactory.newInstance(this.composer.getInit()));
        }
        rendered.add(seed);
        var previous = new Wrapper<>(seed);
        /*
        1. conn.setPrevious(previous.get())
        2. previous.set(conn.transform())
        3. return conn.getNext()
         */
        rendered.addAll(this.getConnectors().stream()
                .peek(conn -> conn.setPrevious(previous.get()))
                .map(Connector::transform)
                .map(previous::set)
                .collect(Collectors.toList())
        );
        return rendered;
    }

    public List<SketchNode> getRenderedChecked(String request) {

        composer.getLogger().log(Level.INFO,
                "{0}: getRenderedChecked, request from {1}",
                new Object[]{this.getId_prefix(), request});
        if (this.ifReRenderRequired()) {
            this.render();
            updateEval();
        }
        return this.rendered;
    }

    public boolean ifReRenderRequired() {

        if (this.rendered.isEmpty()) {
            composer.getLogger().log(Level.INFO,
                    "Not rendered yet, rendering required for Composition {0}.",
                    this.getId_prefix());
            return true;
        }
        if (!Objects.equals(this.connectors.getFirst().getPrevious(), this.seed)) {
            composer.getLogger().log(Level.INFO,
                    "Seed mismatched, rerendering required for Composition {0}.",
                    this.getId_prefix());
            return true;
        }
        if (this.rendered.size() != this.getSize()) {
            composer.getLogger().log(Level.INFO,
                    "Size mismatched: {0} to {1}, rerendering required for Composition {2}.", new Object[]{
                        this.rendered.size(),
                        this.getSize(),
                        this.getId_prefix()});
            return true;
        }
        if (this.connectors.stream().anyMatch(conn
                -> Objects.isNull(conn.getPrevious()) || Objects.isNull(conn.getNext()))) {
            composer.getLogger().log(Level.INFO,
                    "Connector without connected SketchNode found, rerendering required for Composition {0}.",
                    this.getId_prefix());
            return true;
        }
        OptionalInt mismatchIndex = IntStream.range(1, this.getSize())
                .filter(i
                        -> !Objects.equals(
                        this.connectors.get(i - 1).getNext(),
                        this.rendered.get(i)))
                .findFirst();
        if (mismatchIndex.isPresent()) {
            composer.getLogger().log(Level.INFO,
                    "Mismatched SketchNodes at {0}, rerendering required for Composition {1}.",
                    new Object[]{
                        mismatchIndex.getAsInt(),
                        this.getId_prefix()});
            return true;
        }
        composer.getLogger().log(Level.FINE,
                "Rendered list remained consistant, no rerendering required for {0}.",
                this.getId_prefix());
        return false;
    }

    public void updateEval() {

        this.getEval().getStyles().stream()
                .forEach(this::updateScore);
    }

    public void updateScore(Style style) {

        this.getEval().getScores()
                .put(style, style.rateComposition(this));
    }

    public Double getScore(Style style) {

        return this.getEval().getScores().get(style);
    }

    /**
     * Estimated size of rendered SketchNode from the size of connectors.
     *
     * @return
     */
    public int getSize() {

        return this.getConnectors().size() + 1;
    }

    public Path persist() {

        var destination = new File("src/main/resources/composition",
                composer.getId() + "_" + this.getId_prefix() + ".txt")
                .toPath();
        try (var out = Files.newBufferedWriter(
                destination, StandardCharsets.UTF_8)) {
            out.write(this.toString());
            out.flush();
        } catch (IOException ex) {
            composer.getLogger().log(Level.SEVERE, "Failed to persist {0}. {1}", new Object[]{
                this.getId_prefix(), ex.getMessage()});
        }
        composer.getLogger().log(Level.INFO, "{0} has been persisted to {1}", new Object[]{
            this.getId_prefix(),
            destination.getFileName()});
        return destination;
    }

    public void resetSeed(SketchNode seed) {

        if (Objects.equals(this.seed, seed) && this.connectors.getFirst().getPrevious().equals(seed)
                && this.rendered.size() == this.getSize()) {
            return;
        }
        this.seed = seed;
        this.connectors.getFirst().setPrevious(seed);
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Composition other = (Composition) obj;
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public String toString() {
        String result
                = String.format("%s(size = %d, Composer = [%s])\n"
                        + "%s\n"
                        + "Seed: %s\n",
                        super.toString(), this.getSize(), composer.getId(),
                        Composer.simpleScoreOutput(this),
                        this.getSeed())
                + this.getConnectors().stream()
                        .peek(c -> {
                            if (Objects.isNull(c.getPrevious())) {
                                composer.getLogger().log(Level.WARNING,
                                        "Null SketchNode found in {0}.getPrevious().", c.getId_prefix());
                            }
                            if (Objects.isNull(c.getNext())) {
                                composer.getLogger().log(Level.WARNING,
                                        "Null SketchNode found in {0}.getNext().", c.getId_prefix());
                            }
                        })
                        .filter(c -> Objects.nonNull(c.getNext()))
                        .map(Connector::toStringNext)
                        .collect(Collectors.joining("\n"));
        return result;
    }

    /*
     * Default setters and getters
     */
    public LinkedList<Connector> getConnectors() {
        return connectors;
    }

    public SketchNode getSeed() {
        return seed;
    }

    public void setSeed(SketchNode seed) {
        this.seed = seed;
    }

    public List<SketchNode> getRendered() {
        return this.rendered;
    }

    /*
     * For debugging.
     */
    private List<String> debug;

    public void addDebugMsg(String msg) {
        debug.add(msg);
    }

    public List<String> getDebug() {
        return debug;
    }

    public Composer getComposer() {
        return composer;
    }

    public void setComposer(Composer composer) {
        this.composer = composer;
    }
}
