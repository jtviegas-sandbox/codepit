package org.aprestos.labs.lang.java8.lambdas;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

public class Streams {

	@Test
	public void creational() {

		List<BigDecimal> bds = Stream.iterate(BigDecimal.ONE, n -> n.add(BigDecimal.ONE)).limit(12)
				.collect(Collectors.toList());

		Stream.generate(RandomUtils::nextInt).limit(12).forEach(System.out::println);

		assertEquals(false, Arrays.asList("jonathan", "maura", "mario", "richard", "nordis", "manpul", "firefighter")
				.stream().allMatch(s -> s.startsWith("f")));

		IntStream.rangeClosed(10, 134).filter(i -> (i % 5 == 0)).boxed().forEach(System.out::println);

		List<Integer> intObjs = IntStream.of(1, 2, 3, 4, 5, 6, 7, 8).mapToObj(Integer::valueOf)
				.collect(Collectors.toList());

		assertEquals(
				IntStream.of(1, 2, 3, 4, 5, 6, 7, 8).collect(ArrayList<Integer>::new, List::add, List::addAll).size(),
				8);

		int[] vals = IntStream.of(1, 2, 3, 4, 5, 6, 7, 8).toArray();

		List<LocalDate> dates = Stream.iterate(LocalDate.now(), d -> d.plusDays(1l)).limit(10)
				.collect(Collectors.toList());
		List<Double> ds = Stream.generate(Math::random).limit(10).collect(Collectors.toList());

		// we have to use boxed(), or mapToObj otherwise it does not compile
		List<Long> longs = LongStream.rangeClosed(10, 15).boxed().collect(Collectors.toList());
		List<Long> longs2 = LongStream.rangeClosed(10, 15).mapToObj(Long::valueOf).collect(Collectors.toList());

		int[] intArray = IntStream.of(3, 4, 5, 6, 2).toArray();

	}

	@Test
	public void behavioural() {

		OptionalInt max = Arrays.asList("jonathan", "maura", "mario", "richard", "nordis", "manpul", "firefighter")
				.stream().mapToInt(String::length).max();

		OptionalInt sum = Arrays.asList("jonathan", "maura", "mario", "richard", "nordis", "manpul", "firefighter")
				.stream().mapToInt(String::length).reduce((x, y) -> x + y);

	}

	@Test
	public void behavioural2() {

		Stream.concat(Stream.of(0), Stream.of(1, 2, 3, 4, 5, 6, 7, 8)).forEach(System.out::println);
		Stream.of(1, 2, 3, 4, 5, 6, 7, 8).map(o -> Stream.of(0, o)).flatMap(x -> x).forEach(System.out::println);
	}

}
