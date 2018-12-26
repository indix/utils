package com.indix.gocd.utils.utils;

import org.junit.Test;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import static com.indix.gocd.utils.utils.Lists.*;
import static com.indix.gocd.utils.utils.Functions.VoidFunction;

public class ListsTest {
    @Test
    public void shouldRunForEachOnceForEveryElement() {
        final int[] sum = {0};
        List<Integer> list = Lists.of(1, 2, 3, 4, 5);
        foreach(list, new VoidFunction<Integer>() {
            @Override
            public void execute(Integer input) {
                sum[0] += (input * 2);
            }
        });

        assertThat(sum[0], is(30));
    }

    @Test
    public void shouldTransformEveryElementInTheListAndFlatten() {
        List<Integer> duplicateNumbers = flatMap(Lists.of(1, 2, 3, 4, 5), new Function<Integer, List<Integer>>() {
            @Override
            public List<Integer> apply(Integer input) {
                return Lists.of(input, input * 2);
            }
        });

        assertThat(duplicateNumbers, is(Lists.of(1, 2, 2, 4, 3, 6, 4, 8, 5, 10)));
    }
}
