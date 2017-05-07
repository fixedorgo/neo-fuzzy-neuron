package com.fixedorgo.neuron;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TriangularMembershipFunctionTest {

    @Test
    public void functionOutputTest() {
        MembershipFunction function = new TriangularMembershipFunction(0, 2, 4);

        // Integer inputs
        assertThat(function.apply(0)).isEqualTo(0);
        assertThat(function.apply(1)).isEqualTo(0.5);
        assertThat(function.apply(2)).isEqualTo(1);
        assertThat(function.apply(3)).isEqualTo(0.5);
        assertThat(function.apply(4)).isEqualTo(0);
        assertThat(function.apply(5)).isEqualTo(0);
        assertThat(function.apply(-1)).isEqualTo(0);

        // Decimal inputs
        assertThat(function.apply(0.25)).isEqualTo(0.125);
        assertThat(function.apply(1.75)).isEqualTo(0.875);
        assertThat(function.apply(2.25)).isEqualTo(0.875);
        assertThat(function.apply(3.75)).isEqualTo(0.125);
    }

    @Test
    public void functionOutputLeftBoundTest() {
        MembershipFunction function = new TriangularMembershipFunction(5, 5, 10);

        // Integer inputs
        assertThat(function.apply(4)).isEqualTo(0);
        assertThat(function.apply(5)).isEqualTo(1);
        assertThat(function.apply(7)).isEqualTo(0.6);
        assertThat(function.apply(10)).isEqualTo(0);
        assertThat(function.apply(11)).isEqualTo(0);

        // Decimal inputs
        assertThat(function.apply(4.99)).isEqualTo(0);
        assertThat(function.apply(5.01)).isEqualTo(0.998);
        assertThat(function.apply(9.99)).isEqualTo(0.0019999999999999575);
        assertThat(function.apply(10.01)).isEqualTo(0);
    }

    @Test
    public void functionOutputRightBoundTest() {
        MembershipFunction function = new TriangularMembershipFunction(5, 10, 10);

        // Integer inputs
        assertThat(function.apply(4)).isEqualTo(0);
        assertThat(function.apply(5)).isEqualTo(0);
        assertThat(function.apply(7)).isEqualTo(0.4);
        assertThat(function.apply(10)).isEqualTo(1);
        assertThat(function.apply(11)).isEqualTo(0);

        // Decimal inputs
        assertThat(function.apply(4.99)).isEqualTo(0);
        assertThat(function.apply(5.01)).isEqualTo(0.0019999999999999575);
        assertThat(function.apply(9.99)).isEqualTo(0.998);
        assertThat(function.apply(10.01)).isEqualTo(0);
    }

    @Test(expected = IllegalStateException.class)
    public void functionInvalidLeftBoundTest() {
        new TriangularMembershipFunction(10, 9, 11);
    }

    @Test(expected = IllegalStateException.class)
    public void functionInvalidRightBoundTest() {
        new TriangularMembershipFunction(8, 9, 7);
    }

    @Test
    public void functionEqualityTest() {
        MembershipFunction firstFunction = new TriangularMembershipFunction(0, 2, 4);
        MembershipFunction secondFunction = new TriangularMembershipFunction(0, 2, 4);
        MembershipFunction thirdFunction = new TriangularMembershipFunction(1, 3, 5);
        assertThat(firstFunction).isEqualTo(secondFunction).isNotEqualTo(thirdFunction);
    }

    @Test
    public void functionHashCodeTest() {
        MembershipFunction firstFunction = new TriangularMembershipFunction(0, 2, 4);
        MembershipFunction secondFunction = new TriangularMembershipFunction(0, 2, 4);
        MembershipFunction thirdFunction = new TriangularMembershipFunction(1, 3, 5);
        assertThat(firstFunction.hashCode())
                .isEqualTo(secondFunction.hashCode())
                .isNotEqualTo(thirdFunction.hashCode());
    }

    @Test
    public void functionToStringTest() {
        MembershipFunction function = new TriangularMembershipFunction(0, 2, 4);
        assertThat(function.toString()).isNotEmpty().isEqualTo("(0.0, 2.0, 4.0)");
    }

}
