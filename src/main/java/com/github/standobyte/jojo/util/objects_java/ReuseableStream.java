package com.github.standobyte.jojo.util.objects_java;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.Lists;

/* 
 * i don't give a flying fuck about their low level rewrites anymore, 
 * if they delete a class i used on the old version i'll just copypaste it
 */
public class ReuseableStream<T> {
	private final List<T> cache = Lists.newArrayList();
	private final Spliterator<T> source;

	public ReuseableStream(Stream<T> p_i49816_1_) {
		this.source = p_i49816_1_.spliterator();
	}

	public Stream<T> getStream() {
		return StreamSupport.stream(new AbstractSpliterator<T>(Long.MAX_VALUE, 0) {
			private int index;

			public boolean tryAdvance(Consumer<? super T> p_tryAdvance_1_) {
				while(true) {
					if (this.index >= ReuseableStream.this.cache.size()) {
						if (ReuseableStream.this.source.tryAdvance(ReuseableStream.this.cache::add)) {
							continue;
						}

						return false;
					}

					p_tryAdvance_1_.accept(ReuseableStream.this.cache.get(this.index++));
					return true;
				}
			}
		}, false);
	}
}
