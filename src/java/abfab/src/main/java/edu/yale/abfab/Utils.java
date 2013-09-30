package edu.yale.abfab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

	private Utils() {

	}

	/* Adapted from http://codereview.stackexchange.com/a/8267 */
	public static <T> Set<Set<T>> getNTuplePermutations(Collection<T> input,
			int tupleSize) {
		List<Collection<T>> copies = new ArrayList<>();
		for (int i = 0; i < tupleSize; i++) {
			copies.add(input);
		}

		return getSetPermutations(copies);
	}

	private static <T> Set<Set<T>> getSetPermutations(
			final Collection<? extends Collection<T>> input) {
		if (input == null) {
			throw new IllegalArgumentException("Input not provided!");
		}
		final List<Set<T>> saved = new ArrayList<>();
		for (Collection<T> c : input) {
			Set<T> s = new HashSet<>(c);
			c.remove(null);
			if (c.size() >= 1) {
				saved.add(s);
			} else {
				throw new IllegalArgumentException(
						"Input includes null/empty collection");
			}
		}

		return permute(new HashSet<T>(), saved);
	}

	private static <T> Set<Set<T>> permute(final Set<T> initial,
			final List<Set<T>> itemSets) {
		if (itemSets.isEmpty()) {
			return Collections.singleton(initial);
		}

		final Set<T> items = itemSets.get(0);
		final List<Set<T>> remaining = itemSets.subList(1, itemSets.size());
		final int computedSetSize = initial.size() * items.size()
				* remaining.size();
		final Set<Set<T>> computed = new HashSet<>(computedSetSize, 1);

		for (T item : items) {
			if (!initial.contains(item)) {
				Set<T> permutation = new HashSet<>(initial);
				permutation.add(item);
				computed.addAll(permute(permutation, remaining));
			}
		}

		return computed;
	}
}
