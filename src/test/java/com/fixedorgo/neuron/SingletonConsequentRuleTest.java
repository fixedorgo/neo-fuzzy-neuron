package com.fixedorgo.neuron;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SingletonConsequentRuleTest {

    @Test
    public void ruleEvaluationTest() {
        MembershipFunction function = new TriangularMembershipFunction(1, 3, 5);
        ImplicationRule rule = new SingletonConsequentRule(function);
        assertThat(rule.evaluate(3)).isEqualTo(0);
    }

    @Test
    public void ruleFunctionTest() {
        MembershipFunction function = new TriangularMembershipFunction(1, 3, 5);
        ImplicationRule rule = new SingletonConsequentRule(function);
        assertThat(rule.membershipFunction()).isSameAs(function);
    }

    @Test(expected = NullPointerException.class)
    public void ruleNullMembershipFunctionTest() {
        ImplicationRule rule = new SingletonConsequentRule(null);
        rule.evaluate(10);
    }

    @Test
    public void ruleNullMembershipFunctionReturnTest() {
        ImplicationRule rule = new SingletonConsequentRule(null);
        assertThat(rule.membershipFunction()).isNull();
    }

    @Test(expected = NullPointerException.class)
    public void ruleNullLearningFunctionTest() {
        MembershipFunction function = new TriangularMembershipFunction(1, 3, 5);
        ImplicationRule rule = new SingletonConsequentRule(function);
        rule.adjust(null);
    }

    @Test
    public void ruleEqualityTest() {
        ImplicationRule firstRule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 3, 5));
        ImplicationRule secondRule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 3, 5));
        ImplicationRule thirdRule = new SingletonConsequentRule(new TriangularMembershipFunction(2, 6, 10));
        assertThat(firstRule).isEqualTo(secondRule).isNotEqualTo(thirdRule);
    }

    @Test
    public void ruleHashCodeTest() {
        ImplicationRule firstRule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 3, 5));
        ImplicationRule secondRule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 3, 5));
        ImplicationRule thirdRule = new SingletonConsequentRule(new TriangularMembershipFunction(2, 6, 10));
        assertThat(firstRule.hashCode()).isEqualTo(secondRule.hashCode()).isNotEqualTo(thirdRule.hashCode());
    }

    @Test
    public void ruleToStringTest() {
        ImplicationRule rule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 3, 5));
        assertThat(rule.toString()).isEqualTo("Rule: If 'x' is (1.0, 3.0, 5.0) then 'y' is 0.0");
    }

    @Test
    public void ruleLearningTest() {
        double input = 2;
        double desiredOutput = 15;
        double learningRate = 4;

        ImplicationRule rule = new SingletonConsequentRule(new TriangularMembershipFunction(1, 3, 5));
        assertThat(rule.evaluate(input)).isEqualTo(0);

        rule.adjust(withData(input, rule.evaluate(input), desiredOutput, learningRate));
        assertThat(rule.evaluate(input)).isEqualTo(desiredOutput);
    }

    private LearningFunction withData(final double input, final double output, final double trainingData, final double learningRate) {
        return new LearningFunction() {
            @Override
            public double apply(MembershipFunction membershipFunction) {
                return -learningRate * (output - trainingData) * membershipFunction.apply(input);
            }
        };
    }

}
