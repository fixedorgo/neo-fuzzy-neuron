/*
 * Copyright (C) 2015 Timur Zagorsky
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
 * Interface for implementation of Neo-Fuzzy-Neuron stepwise learning algorithm.
 * To adjust consequent the only variable we need is membership value of each
 * particular Implication Rule m(x).
 *
 * @author Timur Zagorsky
 * @since 0.1
 */
public interface LearningFunction {

    /**
     * Returns the renewal of weight value by applying Rule's {@code membershipFunction}.
     * @param membershipFunction of Implication Rule to apply input signal value
     * @return the renewal of consequent weight value (not new weight value)
     */
    double apply(MembershipFunction membershipFunction);

}
