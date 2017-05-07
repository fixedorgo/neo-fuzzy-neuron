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

import static java.util.Arrays.asList;

/**
 * Regular triangular fuzzy membership function that implements
 * {@link MembershipFunction} interface. Instance is immutable.
 *
 * @author Timur Zagorsky
 * @since 0.1
 */
public class TriangularMembershipFunction implements MembershipFunction {

    private final double a, b, c;

    public TriangularMembershipFunction(double a, double b, double c) {
        if (a > b || b > c) {
            throw new IllegalStateException(
                    String.format("Incorrect parameters specified. Should be (a <= b) " +
                            "and (b <= c), but was: [%s, %s, %s]", a, b, c));
        }
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public double apply(double x) {
        if (x >= a && x <= c) {
            if (x < b) {
                return (x - a) / (b - a);
            }
            if (x == b) {
                return 1;
            }
            if (x < c) {
                return (c - x) / (c - b);
            }
        }
        return 0;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TriangularMembershipFunction) {
            TriangularMembershipFunction function = TriangularMembershipFunction.class.cast(obj);
            return Double.compare(a, function.a) == 0 &&
                    Double.compare(b, function.b) == 0 &&
                    Double.compare(c, function.c) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (double d : asList(a, b, c)) {
            long l = Double.doubleToLongBits(d);
            int i = (int) (l ^ (l >>> 32));
            result = 37 * result + i;
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s)", a, b, c);
    }

}
