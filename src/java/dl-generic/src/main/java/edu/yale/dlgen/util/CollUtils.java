package edu.yale.dlgen.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.yale.dlgen.DLEntity;

public class CollUtils {

	public static <T extends DLEntity<?>, E> Set<E> cast(Collection<T> c) {
		Set<E> tset = new HashSet<>();
		for (T o : c) {
			E ast = (E) o.get();
			tset.add(ast);
		}
		return tset;
	}

	public static <T extends DLEntity, E> Set<T> wrap(Iterable<E> c, Class<T> z) {
		Set<T> tset = new HashSet<>();
		for (Object o : c) {
			try {
				T inst = z.newInstance();
				inst.set(o);
				tset.add(inst);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return tset;
	}
}
