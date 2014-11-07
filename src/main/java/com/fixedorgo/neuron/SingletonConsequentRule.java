/*
 * Copyright (C) 2014 Timur Zagorskiy
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

/**
 * Implication Rule that has singleton consequent. In general case, in each nonlinear
 * synapse, the following fuzzy inference with a singleton consequent is carried out:
 *
 *   <p>If x<sub>i</sub> is A<sub>ij</sub> then the output is w<sub>ij</sub>
 *
 * <p>where A<sub>ij</sub> is a fuzzy set whose membership function is m(.).
 * w<sub>ij</sub> is a singleton. The membership functions are arranged to cover each
 * fluctuation range of an input signal. All the initial values of weight are assigned
 * to be zero
 *
 * <p>Implements {@link ImplicationRule} interface
 *
 * @author Timur Zagorskiy
 * @since 0.1
 */
public class SingletonConsequentRule implements ImplicationRule {

    private final MembershipFunction function;

    private double weight;

    public SingletonConsequentRule(MembershipFunction function) {
        this.function = function;
    }

    @Override
    public MembershipFunction membershipFunction() {
        return function;
    }

    @Override
    public double evaluate(double x) {
        return function.apply(x) * weight;
    }

    @Override
    public void adjust(LearningFunction learningFunction) {
        weight += learningFunction.apply(function);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SingletonConsequentRule &&
                function.equals(SingletonConsequentRule.class.cast(obj).function);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + function.hashCode();
        long l = Double.doubleToLongBits(weight);
        result = 37 * result + (int) (l ^ (l >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return String.format("Rule: If 'x' is %s then 'y' is %s", function, weight);
    }

}
