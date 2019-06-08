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

import tech.metacontext.ec.prototype.abs.Factory;
import tech.metacontext.ec.prototype.composer.Settings;
import tech.metacontext.ec.prototype.composer.model.*;
import tech.metacontext.ec.prototype.composer.styles.*;
import tech.metacontext.ec.prototype.composer.enums.ComposerAim;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CompositionFactory implements Factory<Composition> {

    private static final ConnectorFactory connectorFactory = ConnectorFactory.getInstance();
    private static final SketchNodeFactory sketchNodeFactory = SketchNodeFactory.getInstance();
    private static final Map<String, CompositionFactory> instances = new HashMap<>();
    private final Composer composer;

    public static void main(String[] args) throws Exception {

        var composer = new Composer(50, ComposerAim.Phrase, Settings.LogState.DISABLED,
                new UnaccompaniedCello(),
                new GoldenSectionClimax(UnaccompaniedCello.getRange()));
        var instance = CompositionFactory.getInstance(composer);
        var composition = instance.newInstance();
        do {
            composer.sketch().evolve();
            composition.elongate();
        } while (!composer.getAim().isCompleted(composition));
        composition.getRenderedChecked(null);
        System.out.println(composer.getAim().isCompleted(composition));
        System.out.println(composition);
        System.out.println("-".repeat(100));
        System.out.println(instance.forArchiving(composition));
    }

    private CompositionFactory(Composer composer) {

        this.composer = composer;
    }

    public static CompositionFactory getInstance(Composer composer) {

        if (Objects.isNull(instances.get(composer.getId()))) {
            instances.put(composer.getId(), new CompositionFactory(composer));
        }
        return instances.get(composer.getId());
    }

    public Composition newInstance() {

        Composition newInstance = new Composition(composer);
        Connector conn = connectorFactory.newConnectorWithSeed(composer.getInit());
        newInstance.addConnector(conn);
        newInstance.setSeed(conn.getPrevious());
        return newInstance;
    }

    /**
     * Create duplicated Composition instance for archiving.
     *
     * @param origin
     * @return
     */
    @Override
    public Composition forArchiving(Composition origin) {

        origin.addDebugMsg("forArchiving: "
                + origin.getId_prefix() + " been checked/rendered.");
        origin.getRenderedChecked(this.getClass().getSimpleName() + "::forArchiving");
        Composition dupe = new Composition(this.composer, origin.getId());
        dupe.getRendered().addAll(origin.getRendered());
        dupe.getConnectors().addAll(origin.getConnectors().stream()
                .map(connectorFactory::forArchiving)
                .collect(Collectors.toList()));
        dupe.resetSeed(dupe.getConnectors().get(0).getPrevious());
        dupe.getRenderedChecked("Composition::resetSeed");
        dupe.updateEval();
        assert origin.getEval().equals(dupe.getEval()) :
                "unequal: " + origin.getEval() + " vs. " + dupe.getEval();
        return dupe;
    }

    /**
     * Create Composition instance for mutation, with all connector duplicated
     * with their previous nodes.
     *
     * @param origin
     * @return Composition instance ready for mutation.
     */
    public Composition forMutation(Composition origin) {

        origin.addDebugMsg("forMutation: "
                + origin.getId_prefix() + "been checked/rendered.");
        Composition dupe = new Composition(this.composer);
        dupe.getConnectors().addAll(origin.getConnectors().stream()
                .map(connectorFactory::forMutation)
                .collect(Collectors.toList()));
        dupe.resetSeed(sketchNodeFactory.forMutation(origin.getSeed()));
        return dupe;
    }

    /**
     * Create Composition instance for crossover, with a preset first Connector
     * and its previous node as seed.
     *
     * @param conn
     * @param styles
     * @return Composition instance ready for crossover.
     */
    public Composition forCrossover(Connector conn,
            Collection<? extends Style> styles) {

        Composition newInstance = new Composition(this.composer);
        Connector dupe = connectorFactory.forMutation(conn);
        newInstance.addConnector(dupe);
        newInstance.resetSeed(dupe.getPrevious());
        return newInstance;
    }

}
