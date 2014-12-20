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
 * Representation of the Fuzzy Implication Rule as a part
 * of Fuzzy Inference in the Neo-Fuzzy-Neuron's Synapse
 *
 * @author Timur Zagorsky
 * @since 0.1
 */
public interface ImplicationRule {

    /**
     * Provides access to the Membership Function defined as a
     * Rule antecedent. Generally for internal use
     * @return Rule's antecedent Membership Function
     */
    MembershipFunction membershipFunction();

    /**
     * Evaluation of Rule output according to input signal applied to the
     * antecedent Membership Function and consequent value (usually singleton)
     * @param x value of Rule input in the variable dimension
     * @return evaluated value of Rule fuzzy inference
     */
    double evaluate(double x);

    /**
     * Adjustment of the consequent value according to provided
     * stepwise learning algorithm via Learning Function implementation.
     * In general case [Takeshi Yamakawa, “A New Effective Learning algorithm
     * for a Neo Fuzzy Neuron Model, 1992], learning is simple renewal of the
     * consequent singleton weight
     * @param learningFunction to apply Membership Function value
     */
    void adjust(LearningFunction learningFunction);

}
