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
package tech.metacontext.ec.prototype.abs;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 * @param <E>
 */
public abstract class Population<E extends Individual> implements Serializable {

    private static final long serialVersionUID = 0L;

    private final transient Logger _logger;
    private final UUID id;
    private List<E> population;
    private final transient List<List<E>> archive;
    private int genCount;

    public Population() {

        this(UUID.randomUUID());
    }

    public Population(String id) {

        this(UUID.fromString(id));

    }

    public Population(UUID id) {

        this.id = id;
        _logger = Logger.getLogger(getId());
        this.population = new ArrayList<>();
        this.archive = new ArrayList<>();
        this.genCount = 0;
    }

    /**
     * Make population evolve.
     *
     */
    abstract public void evolve();

    abstract public void draw(int type);

    /**
     * Randomly select an Individual from subset selected with a criteria and a
     * threshold.
     *
     * @param criteria to select a subset from population.
     * @param threshold in percentage. For eg., 0.9 stands for that selected
     * score must be higher than 90% population.
     * @return the selected individual.
     */
    abstract public E select(Predicate<E> criteria, double threshold);

    public int getPopulationSize() {

        return population.size();
    }

    public int genCountIncrement() {

        return ++this.genCount;
    }

    public void archive(List<E> p) {

        this.archive.add(p.stream()
                .map(this::copyInstance)
                .collect(Collectors.toList()));
    }

    public void archive(Factory<E> factory) {

        this.archive.add(this.population.stream()
                .map(factory::forArchiving)
                .collect(Collectors.toList()));
        _logger.log(Level.INFO,
                "{0} Individuals archived as Generation {1}.",
                new Object[]{
                    this.getArchive().get(this.getGenCount()).size(),
                    this.getGenCount()});
    }

    public void archive(Path folder, List<? extends Individual> population) {

        _logger.log(Level.INFO, "Archiving, folder = {0}", folder.toString());
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                _logger.log(Level.SEVERE, "Error when creating folder {0}", folder.toString());
            }
        }
        population.stream()
                .forEach(idv -> {
                    var f = folder.resolve(idv.getId() + ".ser");
                    try (var fileOut = new FileOutputStream(f.toFile());
                            var out = new ObjectOutputStream(fileOut);) {
                        out.writeObject(idv);
                        _logger.log(Level.INFO, "Serialized data is saved in {0}", f.toString());
                    } catch (Exception e) {
                        _logger.log(Level.SEVERE, "Error when serializing {0}", f.toString());
                    }
                });
    }

    public void readArchive(Path folder) {

        _logger.log(Level.INFO, "Reading archive, folder = {0}", folder.toString());
        System.out.println("Reading archive, folder = " + folder.toString());
        if (Files.exists(folder) && Files.isDirectory(folder)) {
            this.archive.clear();
            try {
                Files.walk(folder, 1)
                        .filter(Population::isInteger)
                        .sorted((f1, f2)
                                -> Integer.valueOf(f1.toFile().getName())
                                .compareTo(Integer.valueOf(f2.toFile().getName())))
                        .peek(f -> System.out.println("Reading generation " + f.getFileName()))
                        .forEach(this::readIndividual);
            } catch (IOException ex) {
                _logger.log(Level.SEVERE,
                        "Error when reading Archive, folder = {0}", folder);
            }
        }
    }

    public static boolean isInteger(Path path) {

        try {
            Integer.parseInt(path.toFile().getName());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public void readIndividual(Path location) {

        _logger.log(Level.INFO, "Reading Individual. location = {0}", location.toString());
        List<E> generation = new ArrayList<>();
        try {
            Files.walk(location, 1)
                    .filter(l -> l.toString().endsWith(".ser"))
                    .peek(path -> _logger.log(Level.INFO, "Reading object: {0}", path))
                    .forEach(path -> {
                        try (var fis = new FileInputStream(path.toFile());
                                var ois = new ObjectInputStream(fis);) {
                            E i = (E) ois.readObject();
                            generation.add(i);
                        } catch (Exception ex) {
                            _logger.log(Level.SEVERE,
                                    "Error when reading Individual at {0}", location);
                        }
                    });
        } catch (IOException ex) {
            Logger.getLogger(Population.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.archive.add(generation);
    }

    public E copyInstance(E e) {

        try {
            return (E) e.getClass().getDeclaredConstructor(e.getClass()).newInstance(e);
        } catch (Exception ex) {
            Logger.getLogger(Population.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String getId() {

        return id.toString();
    }

    public String getId_prefix() {

        return "[" + this.getId().substring(0, 13) + "]";
    }

    /*
     * Default setters and getters.
     */
    public List<E> getPopulation() {
        return population;
    }

    public void setPopulation(List<E> population) {
        this.population = population;
    }

    public int getGenCount() {
        return genCount;
    }

    public void setGenCount(int genCount) {
        this.genCount = genCount;
    }

    public List<List<E>> getArchive() {
        return this.archive;
    }

    public Logger getLogger() {
        return this._logger;
    }
}
