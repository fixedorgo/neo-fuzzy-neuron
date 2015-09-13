package com.fixedorgo.neuron;

import com.fixedorgo.neuron.NeoFuzzyNeuron.Input;
import com.fixedorgo.neuron.NeoFuzzyNeuron.Input.InputBuilder;
import com.fixedorgo.neuron.NeoFuzzyNeuron.NeuronBuilder;
import com.fixedorgo.neuron.NeoFuzzyNeuron.NeuronInputDimensionException;
import com.fixedorgo.neuron.NeoFuzzyNeuron.SynapseNameNotFoundException;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashSet;

import static com.fixedorgo.neuron.NeoFuzzyNeuron.Input.input;
import static com.fixedorgo.neuron.NeoFuzzyNeuron.neuron;
import static com.fixedorgo.neuron.Synapse.synapse;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class NeoFuzzyNeuronTest {

    @Test
    public void neuronFactoryMethodTest() {
        ImplicationRule rule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        Synapse synapse = new Synapse("Pets", new LinkedHashSet<>(asList(rule)));
        assertThat(neuron(synapse)).isExactlyInstanceOf(NeoFuzzyNeuron.class);
    }

    @Test(expected = NullPointerException.class)
    public void neuronFactoryMethodNullSynapsesTest() {
        Synapse[] synapses = null;
        neuron(synapses);
    }

    @Test
    public void neuronInputFactoryMethodTest() {
        Input input = input("Frodo", 123);
        assertThat(input).isExactlyInstanceOf(Input.class);
        assertThat(input.synapseName).isEqualTo("Frodo");
        assertThat(input.value).isEqualTo(123);
    }

    @Test(expected = NullPointerException.class)
    public void neuronInputNullSynapseNameTest() {
        input(null, 123);
    }

    @Test
    public void neuronInputBuilderFactoryMethodTest() {
        InputBuilder builder = input("Frodo");
        assertThat(builder).isExactlyInstanceOf(InputBuilder.class);
        assertThat(builder.synapseName).isEqualTo("Frodo");
        assertThat(builder.inputs).hasSize(0);
    }

    @Test(expected = NullPointerException.class)
    public void neuronInputBuilderNullSynapseNameTest() {
        input(null);
    }

    @Test
    public void neuronInputBuilderAddToListTest() {
        InputBuilder builder = input("Frodo").as(1);
        assertThat(builder.inputs).hasSize(1).containsExactly(new Input("Frodo", 1));
        assertThat(builder.synapseName).isNull();

        builder.and("Gandalf");
        assertThat(builder.inputs).hasSize(1).containsExactly(new Input("Frodo", 1));
        assertThat(builder.synapseName).isEqualTo("Gandalf");

        builder.as(1);
        assertThat(builder.inputs).hasSize(2).containsExactly(new Input("Gandalf", 1), new Input("Frodo", 1));
        assertThat(builder.synapseName).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void neuronInputBuilderCallMethodTwiceTest() {
        input("Frodo").as(1).as(2);
    }

    @Test(expected = IllegalStateException.class)
    public void neuronInputBuilderIllegalStateTest() {
        input("Frodo").build();
    }

    @Test
    public void neuronInputBuilderTest() {
        Input[] inputs = input("Aragorn").as(1)
                .and("Gandalf").as(1)
                .and().input("Rohirrims").as(100)
                .build();
        assertThat(inputs).hasSize(3).containsExactly(
                new Input("Gandalf", 1),
                new Input("Aragorn", 1),
                new Input("Rohirrims", 100));
    }

    @Test
    public void neuronCalculationTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        Synapse water = synapse("Water").withRange(0, 1000).build();
        Input[] inputs = {new Input("Sand", 25.34), new Input("Water", 76.5)};
        assertThat(neuron(sand, water).calculate(inputs)).isEqualTo(0);
    }

    @Test(expected = NeuronInputDimensionException.class)
    public void neuronIllegalInputDimensionTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        Input[] inputs = {new Input("Sand", 25.34), new Input("Water", 76.5)};
        neuron(sand).calculate(inputs);
    }

    @Test(expected = SynapseNameNotFoundException.class)
    public void neuronIllegalSynapseNameTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        Synapse water = synapse("Water").withRange(0, 1000).build();
        Input[] inputs = {new Input("Rocks", 25.34), new Input("Water", 76.5)};
        neuron(sand, water).calculate(inputs);
    }

    @Test(expected = NullPointerException.class)
    public void neuronNullInputTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        neuron(sand).calculate(null);
    }

    @Test
    public void neuronInputEqualityTest() {
        assertThat(new Input("Pets", 123)).isEqualTo(new Input("Pets", 123))
                .isNotEqualTo(new Input("Birds", 123))
                .isNotEqualTo(new Input("Pets", 345));
    }

    @Test
    public void neuronInputToStringTest() {
        assertThat(new Input("Pets", 123).toString()).isEqualTo("(Pets: 123.0)");
    }

    @Test
    public void neuronLearningTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        Synapse water = synapse("Water").withRange(0, 1000).build();
        NeoFuzzyNeuron neuron = neuron(sand, water);
        {
            Input[] input = {new Input("Sand", 25.34), new Input("Water", 76.5)};
            double desiredOutput = 178.56;
            neuron.learn(input, desiredOutput);
            assertThat(neuron.calculate(input)).isEqualTo(178.55999999999997);
        }
        {
            Input[] input = {new Input("Sand", 46.4), new Input("Water", 23.1)};
            double output = neuron.calculate(input);
            double desiredOutput = 68.9;
            neuron.learn(input, output, desiredOutput);
            assertThat(neuron.calculate(input)).isEqualTo(desiredOutput);
        }
        {
            Input[] input = {new Input("Sand", 4.8), new Input("Water", 343.67)};
            double output = neuron.calculate(input);
            double desiredOutput = 768.9;
            double learningRate = 0.75;
            neuron.learn(input, output, desiredOutput, learningRate);
            assertThat(neuron.calculate(input)).isEqualTo(773.0312007810151);
        }
    }

    @Test(expected = NullPointerException.class)
    public void neuronLearningWithNullInputTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        Synapse water = synapse("Water").withRange(0, 1000).build();
        neuron(sand, water).learn(null, 123, 345, 765);
    }

    @Test(expected = NeuronInputDimensionException.class)
    public void neuronLearningWithIllegalInputDimensionTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        Synapse water = synapse("Water").withRange(0, 1000).build();
        Input[] input = {new Input("Sand", 4.8)};
        neuron(sand, water).learn(input, 123, 345, 765);
    }

    @Test(expected = SynapseNameNotFoundException.class)
    public void neuronLearningWithIllegalSynapseNameTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        Synapse water = synapse("Water").withRange(0, 1000).build();
        Input[] input = {new Input("Rocks", 4.8), new Input("Water", 343.67)};
        neuron(sand, water).learn(input, 123, 345, 765);
    }

    @Test
    public void neuronOptimalLearningRateTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        Synapse water = synapse("Water").withRange(0, 1000).build();
        NeoFuzzyNeuron neuron = neuron(sand, water);
        Input[] input = {new Input("Sand", 4.8), new Input("Water", 343.67)};
        assertThat(neuron.optimalLearningRate(input)).isEqualTo(0.7459918815920613);
    }

    @Test
    public void neuronBuilderDefaultInstanceTest() {
        NeuronBuilder builder = neuron();
        assertThat(builder).isExactlyInstanceOf(NeuronBuilder.class);
        assertThat(builder.synapses).isEmpty();
    }

    @Test
    public void neuronBuilderSetSynapseTest() {
        Synapse synapse = synapse("Sand").withRange(0, 100).build();
        NeuronBuilder builder = neuron().withVariable(synapse);
        assertThat(builder.synapses).hasSize(1).containsOnlyKeys("sand").containsValue(synapse);
        builder.withVariable(synapse);
        assertThat(builder.synapses).hasSize(1).containsOnlyKeys("sand").containsValue(synapse);
    }

    @Test(expected = NullPointerException.class)
    public void neuronBuilderSetNullSynapseTest() {
        Synapse synapse = null;
        neuron().withVariable(synapse);
    }

    @Test
    public void neuronBuilderSetSynapseNameTest() {
        Synapse synapse = synapse("Sand").withRange(0, 100).build();
        NeuronBuilder builder = neuron().withVariable("Sand")
                .hasRange(0, 100)
                .hasRulesCount(10);
        assertThat(builder.synapses).hasSize(1).containsOnlyKeys("sand").containsValue(synapse);
    }

    @Test(expected = IllegalArgumentException.class)
    public void neuronBuilderSetSameSynapseNameTest() {
        neuron().withVariable("Sand")
                .hasRange(0, 100)
                .hasRulesCount(10)
                .withVariable("Sand");
    }

    @Test(expected = NullPointerException.class)
    public void neuronBuilderSetNullSynapseNameTest() {
        String synapseName = null;
        neuron().withVariable(synapseName);
    }

    @Test
    public void neuronBuilderTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        Synapse water = synapse("Water").withRange(0, 1000).build();
        {
            NeoFuzzyNeuron neuron = neuron()
                    .withVariable(sand)
                    .withVariable(water)
                    .build();
            assertThat(neuron.synapses).hasSize(2)
                    .containsOnlyKeys("sand", "water")
                    .containsValue(sand)
                    .containsValue(water);
        }
        {
            NeoFuzzyNeuron neuron = neuron().withVariable("Sand")
                    .hasRange(0, 100)
                    .hasRulesCount(10)
                    .and()
                    .withVariable("Water")
                    .hasRange(0, 1000)
                    .hasRulesCount(10)
                    .build();
            assertThat(neuron.synapses).hasSize(2)
                    .containsOnlyKeys("sand", "water")
                    .containsValue(sand)
                    .containsValue(water);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void neuronBuilderEmptyBuildTest() {
        neuron().build();
    }

    @Test
    public void neuronInputDimensionCheckTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        NeoFuzzyNeuron neuron = neuron(sand);
        Input[] input = {new Input("Sand", 4.8)};
        assertThat(neuron.checkInputDimension(input)).isSameAs(input);
    }

    @Test(expected = NeuronInputDimensionException.class)
    public void neuronInputDimensionCheckIllegalArgumentTest() {
        Synapse sand = synapse("Sand").withRange(0, 100).build();
        NeoFuzzyNeuron neuron = neuron(sand);
        Input[] input = {new Input("Sand", 4.8), new Input("Sand", 4.8)};
        neuron.checkInputDimension(input);
    }

    @Test
    public void neuronSynapseNameCheckTest() {
        Synapse sand = synapse("sand").withRange(0, 100).build();
        NeoFuzzyNeuron neuron = neuron(sand);
        Input input = new Input("SAND", 4.8);
        assertThat(neuron.synapseFor(input)).isSameAs(sand);
    }

    @Test(expected = NullPointerException.class)
    public void neuronNullSynapseNameCheckTest() {
        Synapse sand = null;
        neuron(sand);
    }

    @Test(expected = SynapseNameNotFoundException.class)
    public void neuronIllegalSynapseNameCheckTest() {
        Synapse sand = synapse("sand").withRange(0, 100).build();
        NeoFuzzyNeuron neuron = neuron(sand);
        Input input = new Input("Water", 4.8);
        neuron.synapseFor(input);
    }

    @Test
    public void neuronEqualityTest() {
        ImplicationRule rule1 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule2 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule3 = new SingletonConsequentRule(new TriangularMembershipFunction(2, 3, 4));
        final Synapse synapse1 = new Synapse("Cats", new LinkedHashSet<>(asList(rule1)));
        final Synapse synapse2 = new Synapse("Cats", new LinkedHashSet<>(asList(rule2)));
        final Synapse synapse3 = new Synapse("Ducks", new LinkedHashSet<>(asList(rule3)));
        NeoFuzzyNeuron neuron1 = new NeoFuzzyNeuron(new HashMap<String, Synapse>() {{
            put("Cats", synapse1);
        }});
        NeoFuzzyNeuron neuron2 = new NeoFuzzyNeuron(new HashMap<String, Synapse>() {{
            put("Cats", synapse2);
        }});
        NeoFuzzyNeuron neuron3 = new NeoFuzzyNeuron(new HashMap<String, Synapse>() {{
            put("Ducks", synapse3);
        }});
        assertThat(neuron1).isEqualTo(neuron2).isNotEqualTo(neuron3);
    }

    @Test
    public void neuronHashCodeTest() {
        ImplicationRule rule1 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule2 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule3 = new SingletonConsequentRule(new TriangularMembershipFunction(2, 3, 4));
        final Synapse synapse1 = new Synapse("Cats", new LinkedHashSet<>(asList(rule1)));
        final Synapse synapse2 = new Synapse("Cats", new LinkedHashSet<>(asList(rule2)));
        final Synapse synapse3 = new Synapse("Ducks", new LinkedHashSet<>(asList(rule3)));
        NeoFuzzyNeuron neuron1 = new NeoFuzzyNeuron(new HashMap<String, Synapse>() {{
            put("Cats", synapse1);
        }});
        NeoFuzzyNeuron neuron2 = new NeoFuzzyNeuron(new HashMap<String, Synapse>() {{
            put("Cats", synapse2);
        }});
        NeoFuzzyNeuron neuron3 = new NeoFuzzyNeuron(new HashMap<String, Synapse>() {{
            put("Ducks", synapse3);
        }});
        assertThat(neuron1.hashCode()).isEqualTo(neuron2.hashCode()).isNotEqualTo(neuron3.hashCode());
    }

    @Test
    public void neuronToStringTest() {
        ImplicationRule rule1 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule2 = new SingletonConsequentRule(new TriangularMembershipFunction(2, 3, 4));
        final Synapse synapse = new Synapse("Cats", new LinkedHashSet<>(asList(rule1, rule2)));
        NeoFuzzyNeuron neuron = new NeoFuzzyNeuron(new HashMap<String, Synapse>() {{
            put("Cats", synapse);
        }});
        assertThat(neuron.toString()).isEqualTo("Neo-Fuzzy-Neuron:\nSynapse: Cats\n" +
                "\tRule: If 'x' is (1.0, 2.0, 3.0) then 'y' is 0.0\n" +
                "\tRule: If 'x' is (2.0, 3.0, 4.0) then 'y' is 0.0\n");
    }

}
