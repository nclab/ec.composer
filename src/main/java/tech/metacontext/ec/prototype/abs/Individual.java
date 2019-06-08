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
package tech.metacontext.ec.prototype.abs;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 * @param <E>
 */
public abstract class Individual<E> implements Serializable {

    private final String id;
    private E eval;

    public Individual(String id) {

        this.id = id;
    }

    public Individual() {

        this(UUID.randomUUID().toString());
    }

    public String getId_prefix() {

        return "[" + id.substring(0, 13) + "]";
    }

    @Override
    public int hashCode() {

        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.id);
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
        final Individual other = (Individual) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {

        return this.getClass().getSimpleName() + " " + this.getId_prefix();
    }

    /*
     * Default setters and getters.
     */
    public String getId() {
        return id;
    }

    public E getEval() {
        return eval;
    }

    public void setEval(E eval) {
        this.eval = eval;
    }
}
