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

import com.fixedorgo.neuron.Synapse.SynapseBuilder;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.pow;

/**
 * A main Neo-Fuzzy-Neuron class that allow to create and use Neuron with
 * unlimited number of Synapses. But please make sure you have applied {@link Input}s
 * with correct dimension that should correspond to {@link Synapse} number. It's
 * highly recommended to use {@link NeuronBuilder} and
 * {@link com.fixedorgo.neuron.NeoFuzzyNeuron.Input.InputBuilder}
 *
 * @author Timur Zagorsky
 * @since 0.1
 */
public class NeoFuzzyNeuron {

    /**
     * Map of {@link Synapse}s that represent input variables.
     */
    protected final Map<String, Synapse> synapses;

    protected NeoFuzzyNeuron(Map<String, Synapse> synapses) {
        this.synapses = synapses;
    }

    /**
     * Main static factory method to get new {@link NeoFuzzyNeuron} instance
     * @param synapses that represents input variables, must not be null
     * @return new {@link NeoFuzzyNeuron} instance with provided {@link Synapse}s
     */
    public static NeoFuzzyNeuron neuron(Synapse... synapses) {
        checkNotNull(synapses, "Synapses must not be null");
        Map<String, Synapse> synapseMap = Maps.newHashMap();
        for (Synapse synapse : synapses)
            synapseMap.put(synapse.name.toLowerCase(), synapse);
        return new NeoFuzzyNeuron(synapseMap);
    }

    /**
     * Calculation of Neo-Fuzzy-Neuron output. Neuron has a nonlinear synaptic transfer
     * characteristic so the input signals through the nonlinear synapses are applied
     * and summed up. Order of the Inputs is not important.
     * @param inputs values of Neo-Fuzzy-Neuron input signals, must not be null
     * @return Neo-Fuzzy-Neuron output value
     */
    public double calculate(Input[] inputs) {
        checkInputDimension(inputs);
        double output = 0;
        for (Input input : inputs) {
            output += synapseFor(input).apply(input.value);
        }
        return output;
    }

    /**
     * Use Stepwise Learning Algorithm in the "passive mode" when only input/output data
     * are available. Actual Neuron output is calculated with standard
     * {@link #calculate(com.fixedorgo.neuron.NeoFuzzyNeuron.Input[])}
     * @param inputs values of Neo-Fuzzy-Neuron input signals, must not be null
     * @param trainingData that represents desired output value
     */
    public void learn(Input[] inputs, double trainingData) {
        learn(inputs, calculate(inputs), trainingData);
    }

    /**
     * Use Stepwise Learning Algorithm with special case of learning rate calculation.
     * The {@link #optimalLearningRate(com.fixedorgo.neuron.NeoFuzzyNeuron.Input[])} to
     * calculate optimal learning rate
     * @param inputs values of Neo-Fuzzy-Neuron input signals, must not be null
     * @param output actual output of the Neo-Fuzzy-Neuron
     * @param trainingData that represents desired output value
     */
    public void learn(Input[] inputs, double output, double trainingData) {
        learn(inputs, output, trainingData, optimalLearningRate(inputs));
    }

    /**
     * Stepwise Learning Algorithm. The gradient descent scheme is employed here
     * to reduce the error E through the adjustment of weight w<sub>ij</sub>. Then
     * the renewal of weight is given by:
     *
     * <p>   dw<sub>ij</sub> = - Learning Rate * Error * Membership Degree
     *
     * @param inputs values of Neo-Fuzzy-Neuron input signals, must not be null
     * @param output actual output of the Neo-Fuzzy-Neuron
     * @param trainingData that represents desired output value
     * @param learningRate that is given by some ratio and always greater than 0
     */
    public void learn(Input[] inputs, final double output, final double trainingData, final double learningRate) {
        checkInputDimension(inputs);
        for (final Input input : inputs) {
            synapseFor(input).learnWith(new LearningFunction() {
                @Override
                public double apply(MembershipFunction membershipFunction) {
                    return -learningRate * (output - trainingData) * membershipFunction.apply(input.value);
                }
            });
        }
    }

    /**
     * Calculates optimal Neo-Fuzzy-Neuron learning rate according to [Walmir Caminhas,
     * "A Fast Learning Algorithm for Neofuzzy Networks", 1998]. For internal use, at least now.
     * @param inputs array of values to Neo-Fuzzy-Neuron, must not be null
     * @return learning rate calculated value
     */
    protected double optimalLearningRate(Input[] inputs) {
        checkInputDimension(inputs);
        double sumOfSegments = 0;
        for (Input input : inputs) {
            for (double membershipDegree : synapseFor(input).fuzzySegment(input.value)) {
                sumOfSegments += pow(membershipDegree, 2);
            }
        }
        return 1 / sumOfSegments;
    }

    /**
     * Input data for a given Synapse of the Neo-Fuzzy-Neuron. Should be used with
     * default {@link NeoFuzzyNeuron} implementation due to ability of dynamic
     * {@link NeoFuzzyNeuron} creation.
     */
    public static class Input {

        protected final String synapseName;

        protected final double value;

        /**
         * To prevent direct Input instantiation
         */
        protected Input(String synapseName, double value) {
            this.synapseName = synapseName;
            this.value = value;
        }

        /**
         * Static factory method to get new {@link Input} instance
         * @param synapseName to which Input should be applied, must not be null
         * @param value of the input signal
         * @return new {@link Input} instance
         */
        public static Input input(String synapseName, double value) {
            checkNotNull(synapseName, "Synapse name must not be null");
            return new Input(synapseName, value);
        }

        /**
         * Static access to {@link InputBuilder} instance
         * @param synapseName for first created {@link Synapse}
         * @return new instance of {@link InputBuilder}
         */
        public static InputBuilder input(String synapseName) {
            return new InputBuilder(synapseName);
        }

        /**
         * A builder that provides fluent interface to create new {@link Input} instance.
         */
        public static class InputBuilder {

            protected Set<Input> inputs = Sets.newHashSet();

            protected String synapseName;

            /**
             * To prevent direct Builder instantiation
             */
            protected InputBuilder(String synapseName) {
                input(synapseName);
            }

            /**
             * Set name of the target {@link Synapse}
             * @param synapseName name of the target Synapse, must not be null
             * @return same {@link InputBuilder} instance
             */
            public InputBuilder input(String synapseName) {
                checkNotNull(synapseName, "Synapse name must not be null");
                this.synapseName = synapseName;
                return this;
            }

            /**
             * Adds specified value for a new {@link Input} instance and sets it
             * to Inputs collection
             * @param value input signal value
             * @return same {@link InputBuilder} instance
             */
            public InputBuilder as(double value) {
                checkNotNull(synapseName, "You must specify Synapse name first via InputBuilder.input() method");
                inputs.add(new Input(synapseName, value));
                synapseName = null; // to prevent call of this method twice
                return this;
            }

            /**
             * Alternative way to use {@link #input(String)} method
             * @param synapseName name of the target Synapse, must not be null
             * @return same {@link InputBuilder} instance
             */
            public InputBuilder and(String synapseName) {
                return input(synapseName);
            }

            /**
             * Just for fluent needs
             * @return same {@link InputBuilder} instance
             */
            public InputBuilder and() {
                return this;
            }

            /**
             * Builds array of {@link Input} instances according to specified data
             * @return array of new {@link Input} instances
             * @throws IllegalStateException if Input values were not set at all
             */
            public Input[] build() {
                if (inputs.isEmpty()) {
                    throw new IllegalStateException("You have to specify at least one Input value. " +
                            "Use InputBuilder.as() method");
                }
                return inputs.toArray(new Input[inputs.size()]);
            }

        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Input &&
                    synapseName.equalsIgnoreCase(Input.class.cast(obj).synapseName) &&
                    value == Input.class.cast(obj).value;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + synapseName.hashCode();
            long l = Double.doubleToLongBits(value);
            result = 37 * result + (int) (l ^ (l >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return String.format("(%s: %s)", synapseName, value);
        }

    }

    /**
     * Static access to {@link NeuronBuilder} instance
     * @return new instance of {@link NeuronBuilder}
     */
    public static NeuronBuilder neuron() {
        return new NeuronBuilder();
    }

    /**
     * A builder for creating {@link NeoFuzzyNeuron} instances. There are two ways to use
     * Bulder API:
     *
     * <p>1. Use Builder with internal {@link SynapseBuilder} out of the box. Example:
     * <pre> {@code
     *
     *     NeoFuzzyNeuron catsDinner = neuron().withVariable("cats")
     *                        .hasRange(0, 20)
     *                        .hasRulesCount(10)
     *                        .and().withVariable("fishes")
     *                        .hasRange(0, 40)
     *                        .hasRulesCount(15)
     *                        .build();}</pre>
     *
     * <p>2. Or another approach is to use {@link SynapseBuilder} separately. Example:
     * <pre> {@code
     *
     *     NeoFuzzyNeuron passengers = neuron().withVariable(synapse("passengers")
     *                        .withRange(0, 40)
     *                        .withRulesCount(20)
     *                        .build()).build();}</pre>
     *
     * Both approaches are equivalent. Please see {@link SynapseBuilder} for details.
     */
    public static class NeuronBuilder {

        protected Map<String, Synapse> synapses = Maps.newHashMap();

        /**
         * To prevent direct Builder instantiation
         */
        protected NeuronBuilder() {
        }

        /**
         * Uses {@link SynapseBuilder} to construct new {@link Synapse} via {@link SynapseBuilderWrapper}
         * @param synapseName for new {@link Synapse}, must not be null
         * @return new {@link SynapseBuilderWrapper} instance
         * @throws IllegalArgumentException if such Synapse with same name already created
         */
        public SynapseBuilderWrapper withVariable(String synapseName) {
            checkNotNull(synapseName, "Synapse name must not be null");
            if (synapses.containsKey(synapseName.toLowerCase()))
                throw new IllegalArgumentException(String.format("Synapse with name [%s] is already defined", synapseName));
            return new SynapseBuilderWrapper(new SynapseBuilder(synapseName));
        }

        /**
         * Uses {@link Synapse} instance directly to construct new {@link Synapse}
         * @param synapse instance that is fully constructed and ready to go, must not be null
         * @return same {@link NeuronBuilder} instance
         */
        public NeuronBuilder withVariable(Synapse synapse) {
            checkNotNull(synapse, "Synapse instance must not be null");
            synapses.put(synapse.name.toLowerCase(), synapse);
            return this;
        }

        /**
         * Just for fluent needs
         * @return same {@link NeuronBuilder} instance
         */
        public NeuronBuilder and() {
            return this;
        }

        /**
         * Builds new {@link NeoFuzzyNeuron} instances according to specified data
         * @return new {@link NeoFuzzyNeuron} instance
         * @throws IllegalStateException if there is no Synapses at all
         */
        public NeoFuzzyNeuron build() {
            if (synapses.isEmpty()) {
                throw new IllegalStateException("You have to specify at least one Synapse. " +
                        "See NeuronBuilder.withVariable() method");
            }
            return new NeoFuzzyNeuron(synapses);
        }

        /**
         * Simple wrapper for {@link SynapseBuilder} instance. Nothing special
         */
        public class SynapseBuilderWrapper {

            private SynapseBuilder synapseBuilder;

            protected SynapseBuilderWrapper(SynapseBuilder synapseBuilder) {
                this.synapseBuilder = synapseBuilder;
            }

            public SynapseBuilderWrapper hasRange(double lower, double upper) {
                synapseBuilder.withRange(lower, upper);
                return this;
            }

            public NeuronBuilder hasRulesCount(int count) {
                withVariable(synapseBuilder.withRulesCount(count).build());
                return NeuronBuilder.this;
            }

        }

    }

    /**
     * Quick access to {@link Synapse} for a given {@link Input}. For internal use only
     * @param input for Neo-Fuzzy-Neuron, must not be null
     * @return {@link Synapse} that corresponds to the {@link Input}
     * @throws NullPointerException in case of null {@link Input}
     * @throws SynapseNameNotFoundException if no such Synapse is defined
     */
    protected Synapse synapseFor(Input input) {
        checkNotNull(input, "Input must not be null");
        String synapseName = input.synapseName.toLowerCase();
        if (!synapses.containsKey(synapseName))
            throw new SynapseNameNotFoundException(synapseName);
        return synapses.get(synapseName);
    }

    /**
     * Checks that {@link Input}[] has correct dimension according to number of {@link Synapse}s.
     * For internal use only
     * @param inputs of teh Neo-Fuzzy-Neuron, must not be null
     * @return the same {@link Input}[] array instance after checking
     * @throws NullPointerException in case of null {@link Input}[]
     * @throws NeuronInputDimensionException if input dimension is not corresponded
     */
    protected Input[] checkInputDimension(Input[] inputs) {
        checkNotNull(inputs, "Input[] must not be null");
        if (inputs.length != synapses.size())
            throw new NeuronInputDimensionException(inputs.length, synapses.size());
        return inputs;
    }

    protected static class SynapseNameNotFoundException extends RuntimeException {
        public SynapseNameNotFoundException(String synapseName) {
            super(String.format("Synapse with name [%s] was not found", synapseName));
        }
    }

    protected static class NeuronInputDimensionException extends RuntimeException {
        public NeuronInputDimensionException(int inputDimension, int numberOfSynapses) {
            super(String.format("Current Input dimension [%s] does not correspond " +
                    "to synapse number [%s]", inputDimension, numberOfSynapses));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NeoFuzzyNeuron &&
                Maps.difference(synapses, NeoFuzzyNeuron.class.cast(obj).synapses).areEqual();
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (String key : synapses.keySet())
            result = 37 * result + key.hashCode();
        for (Synapse synapse : synapses.values())
            result = 37 * result + synapse.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Neo-Fuzzy-Neuron:\n");
        for (Synapse synapse : synapses.values())
            sb.append(synapse);
        return sb.toString();
    }

}
