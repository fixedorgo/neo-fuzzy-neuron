/*
 * Copyright (C) 2014 Timur Zagorsky
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fixedorgo.neuron;

import com.google.common.collect.Sets;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;

/**
 * Nonlinear synapse as a part of Neo-Fuzzy-Neuron inference. The nonlinear synapse
 * is realized by a set of Fuzzy Implication Rules.
 *
 * @author Timur Zagorsky
 * @since 0.1
 */
public class Synapse {

    /**
     * Name of the {@link Synapse} to represent name of the input variable
     */
    protected final String name;

    /**
     * Set of Fuzzy {@link ImplicationRule}s
     */
    protected final Set<ImplicationRule> rules;

    protected Synapse(String name, Set<ImplicationRule> rules) {
        this.name = name;
        this.rules = rules;
    }

    /**
     * Main static factory method to get new {@link Synapse} Instance.
     * @param name of the {@link Synapse} or input variable name, must not be null.
     * @param rules that should be include into the Synapse inference, must not be null
     * @return new {@link Synapse} instance
     */
    public static Synapse synapse(String name, ImplicationRule... rules) {
        checkNotNull(name, "Synapse name must not be null");
        checkNotNull(rules, "Implication Rules must not be null");
        return new Synapse(name, Sets.newLinkedHashSet(asList(rules)));
    }

    /**
     * Calculation of {@link Synapse} output that is given by the weighted sum
     * of all {@link ImplicationRule}s.
     * @param input signal value
     * @return Synapse calculated output value
     */
    public double apply(double input) {
        double output = 0;
        for (ImplicationRule rule : rules)
            output += rule.evaluate(input);
        return output;
    }

    /**
     * Fuzzy Segment is a pair of {@link MembershipFunction}s that were activated by a input
     * signal and its membership degree is greater then 0. It's mainly needed for Neo-Fuzzy-Neuron
     * internal use (e.g. for optimal learning rate calculation)
     * @param input signal value
     * @return array with pair of membership degree values
     */
    public double[] fuzzySegment(double input) {
        double[] segment = new double[2];
        int index = 0;
        for (ImplicationRule rule : rules) {
            double output = rule.membershipFunction().apply(input);
            if (output > 0)
                segment[index++] = output;
        }
        return segment;
    }

    /**
     * Apply given {@link LearningFunction} to adjust parameters of {@link ImplicationRule}s
     * @param learningFunction that implement stepwise learning algorithm, must not be null
     */
    public void learnWith(LearningFunction learningFunction) {
        checkNotNull(learningFunction, "Learning Function must not be null");
        for (ImplicationRule rule : rules)
            rule.adjust(learningFunction);
    }

    /**
     * Static access to {@link SynapseBuilder} instance
     * @param synapseName that represents name of the input variable, must not be null
     * @return new instance of {@link SynapseBuilder}
     */
    public static SynapseBuilder synapse(String synapseName) {
        checkNotNull(synapseName, "Synapse name must not be null");
        return new SynapseBuilder(synapseName);
    }

    /**
     * A builder that provides fluent interface to create new {@link Synapse} instance.
     * Example: <pre>{@code
     *
     *     Synapse cats = synapse("cats").withRange(0, 20)
     *                         .withRulesCount(10)
     *                         .build();}</pre>
     *
     * <p>In general case [Takeshi Yamakawa, “A Neo Fuzzy Neuron and Its Applications to System
     * Identification and Prediction of the System Behavior", 1992] each membership function
     * in the antecedent is triangular, and assigned to be complementary with each other.
     * An input signal x<sub>i</sub> activates only two neighboring membership functions
     * simultaneously, and the sum of the grades of these membership functions always equals to 1.
     *
     * <p>Based on this assumption we can build Synapse using input range and
     * number of rules values only.
     */
    public static class SynapseBuilder {

        /**
         * Name of the {@link Synapse} (name of the variable)
         */
        protected String name;

        /**
         * Lower and upper range of the given variable
         */
        protected double lower, upper;

        /**
         * Count of {@link ImplicationRule}s. Default is '10'
         */
        protected int count = 10;

        protected SynapseBuilder(String name) {
            this.name = name;
        }

        /**
         * Set {@link Synapse}'s input signal range
         * @param lower variable range value
         * @param upper variable range value
         * @return same {@link SynapseBuilder} instance
         */
        public SynapseBuilder withRange(double lower, double upper) {
            this.lower = lower;
            this.upper = upper;
            return this;
        }

        /**
         * Set number of {@link ImplicationRule}s
         * @param count of Implication Rules, must be at least 1
         * @return same {@link SynapseBuilder} instance
         * @throws IllegalArgumentException in case of incorrect Rules count
         */
        public SynapseBuilder withRulesCount(int count) {
            if (count < 1)
                throw new IllegalArgumentException("Number of Rules must be at least 1");
            this.count = count;
            return this;
        }

        /**
         * Create {@link Synapse} according to specified input range and number of Rules
         * @return new {@link Synapse} instance according to specified data
         * @throws IllegalStateException if input signal range is specified incorrectly
         */
        public Synapse build() {
            if (lower >= upper) {
                throw new IllegalStateException(String.format("Input signal range [%s, %s] " +
                        "is incorrectly specified.", lower, upper));
            }
            Set<ImplicationRule> rules = Sets.newLinkedHashSet();
            double step = (upper - lower) / (count - 1);
            for (int i = 0; i < count; i++) {
                double b = lower + step * i;
                double a = max(lower, b - step);
                double c = min(upper, b + step);
                rules.add(new SingletonConsequentRule(new TriangularMembershipFunction(a, b, c)));
            }
            return new Synapse(name, rules);
        }

    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Synapse &&
                Sets.difference(rules, Synapse.class.cast(obj).rules).isEmpty();
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (ImplicationRule rule : rules)
            result = 37 * result + rule.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(String.format("Synapse: %s\n", name));
        for (ImplicationRule rule : rules)
            sb.append(String.format("\t%s\n", rule));
        return sb.toString();
    }

}
