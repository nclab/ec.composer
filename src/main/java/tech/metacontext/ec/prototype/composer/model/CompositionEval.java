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

import java.io.Serializable;
import tech.metacontext.ec.prototype.composer.styles.Style;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CompositionEval implements Serializable {

    private final Map<Style, Double> scores;

    public CompositionEval(Map<? extends Style, Double> scores) {

        this.scores = new HashMap<>(scores);
    }

    public CompositionEval(Collection<? extends Style> styles) {

        this(styles.stream().collect(Collectors.toMap(s -> s, s -> 0.0)));
    }

    public CompositionEval(CompositionEval eval) {

        this.scores = new HashMap<>();
        eval.getScores().forEach(scores::put);
    }

    public Set<? extends Style> getStyles() {

        return this.getScores().keySet();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (Style s : this.scores.keySet()) {
            hash = 23 * hash + s.hashCode();
            hash = 23 * hash + this.scores.get(s).hashCode();
        }
        return hash;
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
        final CompositionEval other = (CompositionEval) obj;
        if (this.scores.size() != other.scores.size()) {
            return false;
        }
        return this.scores.entrySet().stream()
                .allMatch(e -> other.scores.get(e.getKey()).equals(e.getValue()));
    }

    @Override
    public String toString() {

        return "CompositionEval{" + "scores=" + scores + '}';
    }

    /*
     * Default setters and getters
     */
    public Map<Style, Double> getScores() {
        return scores;
    }
}
