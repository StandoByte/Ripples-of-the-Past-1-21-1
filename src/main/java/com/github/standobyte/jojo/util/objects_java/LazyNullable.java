package com.github.standobyte.jojo.util.objects_java;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

/**
 * Like {@link net.neoforged.neoforge.common.util.Lazy}, but the supplier can also return null.
 * 
 * @param <T> The type of the value
 */
public final class LazyNullable<T> implements Supplier<T> {
	/**
	 * Constructs a lazy-initialized object.
	 *
	 * @param supplier The supplier for the value, to be called the first time the value is needed,
	 *                 or whenever the cache has been invalidated.
	 */
	public static <T> LazyNullable<T> of(Supplier<T> supplier) {
		return new LazyNullable<>(supplier);
	}

	/**
	 * Invalidates the cache, causing the supplier to be called again on the next access.
	 */
	public synchronized void invalidate() {
		this.isCached = false;
		this.cachedValue = null;
	}

	private final Supplier<T> delegate;
	@Nullable private volatile T cachedValue;
	private volatile boolean isCached = false;

	public LazyNullable(Supplier<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public T get() {
		T ret = cachedValue;
		
		if (!isCached) {
			synchronized (this) {
				if (!isCached) {
					cachedValue = delegate.get();
					isCached = true;
				}
				ret = cachedValue;
			}
		}

		return ret;
	}
}
