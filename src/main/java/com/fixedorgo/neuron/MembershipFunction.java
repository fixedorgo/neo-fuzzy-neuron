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
 * Interface for fuzzy Membership Function. Determines an membership degree based
 * on an input signal value.
 *
 * <p>The only reason not to use classic Guava {@link com.google.common.base.Function} interface
 * is desire to return double value instead of autoboxed Double.
 *
 * @author Timur Zagorsky
 * @since 0.1
 */
public interface MembershipFunction {

    /**
     * Returns membership degree of applying this function to {@code x}.
     * @param x represents input signal value
     * @return membership degree according to input signal
     */
    double apply(double x);

}
