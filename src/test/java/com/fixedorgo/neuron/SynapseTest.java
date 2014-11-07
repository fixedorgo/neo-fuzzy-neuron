package com.fixedorgo.neuron;

import com.fixedorgo.neuron.Synapse.SynapseBuilder;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Set;

import static com.fixedorgo.neuron.Synapse.synapse;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class SynapseTest {

    @Test
    public void synapseFactoryMethodTest() {
        ImplicationRule rule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        assertThat(synapse("Dogs", rule)).isExactlyInstanceOf(Synapse.class);
    }

    @Test(expected = NullPointerException.class)
    public void synapseFactoryMethodNullSynapseNameTest() {
        ImplicationRule rule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        synapse(null, rule);
    }

    @Test(expected = NullPointerException.class)
    public void synapseFactoryMethodNullRulesSetTest() {
        ImplicationRule[] rules = null;
        synapse("Dogs", rules);
    }

    @Test
    public void synapseEvaluationTest() {
        ImplicationRule rule1 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule2 = new SingletonConsequentRule(new TriangularMembershipFunction(2, 3, 4));
        ImplicationRule rule3 = new SingletonConsequentRule(new TriangularMembershipFunction(3, 4, 5));
        Synapse synapse = new Synapse("Dogs", newLinkedHashSet(asList(rule1, rule2, rule3)));
        assertThat(synapse.apply(5)).isEqualTo(0);
        assertThat(synapse.apply(23.1)).isEqualTo(0);
        assertThat(synapse.apply(-134723.456)).isEqualTo(0);
    }

    @Test
    public void synapseFuzzySegmentTest() {
        ImplicationRule rule1 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule2 = new SingletonConsequentRule(new TriangularMembershipFunction(2, 3, 4));
        ImplicationRule rule3 = new SingletonConsequentRule(new TriangularMembershipFunction(3, 4, 5));
        Synapse synapse = new Synapse("Cats", newLinkedHashSet(asList(rule1, rule2, rule3)));
        assertThat(synapse.fuzzySegment(2)).containsExactly(1, 0);
        assertThat(synapse.fuzzySegment(2.5)).containsExactly(0.5, 0.5);
        assertThat(synapse.fuzzySegment(2.75)).containsExactly(0.25, 0.75);
        assertThat(synapse.fuzzySegment(3)).containsExactly(1, 0);
        assertThat(synapse.fuzzySegment(6.4)).containsExactly(0, 0);
    }

    @Test
    public void synapseLearningTest() {
        double[] input = {2, 3, 4.5, -1, 6.2};
        double[] desiredOutput = {30, 37, 55, 0, 3};
        double learningRate = 1.5;

        ImplicationRule rule1 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule2 = new SingletonConsequentRule(new TriangularMembershipFunction(2, 3, 4));
        ImplicationRule rule3 = new SingletonConsequentRule(new TriangularMembershipFunction(3, 4, 5));
        Synapse synapse = new Synapse("Ducks", newLinkedHashSet(asList(rule1, rule2, rule3)));

        for (int i = 0; i < input.length; i++)
            synapse.learnWith(function(input[i], synapse.apply(input[i]), desiredOutput[i], learningRate));

        double[] outputAfterLearning = {45, 55.5, 20.625, 0, 0};

        for (int i = 0; i < input.length; i++)
            assertThat(synapse.apply(input[i])).isEqualTo(outputAfterLearning[i]);
    }

    @Test(expected = NullPointerException.class)
    public void synapseNullLearningFunctionTest() {
        ImplicationRule rule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        synapse("Dogs", rule).learnWith(null);
    }

    @Test
    public void synapseDefaultBuilderInstanceTest() {
        SynapseBuilder builder = synapse("Passengers");
        assertThat(builder).isExactlyInstanceOf(SynapseBuilder.class);
        assertThat(builder.name).isEqualTo("Passengers");
        assertThat(builder.lower).isEqualTo(builder.upper).isEqualTo(0);
        assertThat(builder.count).isEqualTo(10);
    }

    @Test(expected = NullPointerException.class)
    public void synapseBuilderWithNullSynapseNameTest() {
        synapse(null);
    }

    @Test
    public void synapseBuilderSetRangeTest() {
        SynapseBuilder builder = synapse("Sand").withRange(50.789, 123.567);
        assertThat(builder.lower).isEqualTo(50.789);
        assertThat(builder.upper).isEqualTo(123.567);
    }

    @Test(expected = IllegalStateException.class)
    public void synapseBuildingWithDefaultInputSignalRangeTest() {
        synapse("Pets").build();
    }

    @Test(expected = IllegalStateException.class)
    public void synapseBuilderIncorrectInputSignalRangeTest() {
        synapse("Water").withRange(10.6, 9.1).build();
    }

    @Test
    public void synapseBuilderSetRulesCountTest() {
        SynapseBuilder builder = synapse("Dogs").withRulesCount(20);
        assertThat(builder.count).isEqualTo(20);
    }

    @Test(expected = IllegalArgumentException.class)
    public void synapseBuilderIncorrectNumberOfRulesTest() {
        synapse("Pets").withRulesCount(0);
    }

    @Test
    public void synapseBuildingNewInstanceTest() {
        Synapse synapse = synapse("Liquid")
                .withRange(-10, 10)
                .withRulesCount(5)
                .build();

        assertThat(synapse.name).isEqualTo("Liquid");
        assertThat(synapse.rules).hasSize(5);

        Set<MembershipFunction> functions = Sets.newLinkedHashSet();
        for (ImplicationRule rule : synapse.rules) {
            assertThat(rule).isExactlyInstanceOf(SingletonConsequentRule.class);
            assertThat(rule.membershipFunction()).isExactlyInstanceOf(TriangularMembershipFunction.class);
            functions.add(rule.membershipFunction());
        }

        assertThat(functions).containsExactly(
                new TriangularMembershipFunction(-10.0, -10.0, -5.0),
                new TriangularMembershipFunction(-10.0, -5.0, 0.0),
                new TriangularMembershipFunction(-5.0, 0.0, 5.0),
                new TriangularMembershipFunction(0.0, 5.0, 10.0),
                new TriangularMembershipFunction(5.0, 10.0, 10.0));
    }

    @Test
    public void synapseEqualityTest() {
        ImplicationRule rule1 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule2 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule3 = new SingletonConsequentRule(new TriangularMembershipFunction(2, 3, 4));
        Synapse synapse1 = new Synapse("Cats", newLinkedHashSet(asList(rule1)));
        Synapse synapse2 = new Synapse("Dogs", newLinkedHashSet(asList(rule2)));
        Synapse synapse3 = new Synapse("Ducks", newLinkedHashSet(asList(rule3)));
        assertThat(synapse1).isEqualTo(synapse2).isNotEqualTo(synapse3);
    }

    @Test
    public void synapseToStringTest() {
        ImplicationRule rule1 = new SingletonConsequentRule(new TriangularMembershipFunction(1, 2, 3));
        ImplicationRule rule2 = new SingletonConsequentRule(new TriangularMembershipFunction(2, 3, 4));
        Synapse synapse = new Synapse("Cats", newLinkedHashSet(asList(rule1, rule2)));
        assertThat(synapse.toString()).isEqualTo("Synapse: Cats\n" +
                "\tRule: If 'x' is (1.0, 2.0, 3.0) then 'y' is 0.0\n" +
                "\tRule: If 'x' is (2.0, 3.0, 4.0) then 'y' is 0.0\n");
    }

    private LearningFunction function(final double input, final double output, final double trainingData, final double learningRate) {
        return new LearningFunction() {
            @Override
            public double apply(MembershipFunction membershipFunction) {
                return -learningRate * (output - trainingData) * membershipFunction.apply(input);
            }
        };
    }

}
