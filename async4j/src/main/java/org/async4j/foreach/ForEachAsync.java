/*******************************************************************************
 * Copyright 2013 Async4j Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.async4j.foreach;

import org.async4j.Callback;
import org.async4j.Callback2;
import org.async4j.FunctionAsync;
import org.async4j.streams.EnumeratorAsync;

/**
 * 
 * @author Amah AHITE
 * 
 * @param <E>
 *            loop iterator element type
 */
public class ForEachAsync<E> implements FunctionAsync<EnumeratorAsync<E>, Void> {
	private final FunctionAsync<E, Void> iterationTask;

	public ForEachAsync(FunctionAsync<E, Void> iterationTask) {
		this.iterationTask = iterationTask;
	}

	public void apply(Callback<? super Void> k, EnumeratorAsync<E> enumerator) {
		try {
			enumerator.next(new NextCallback<E>(k, enumerator, iterationTask));
		} catch (Throwable e) {
			k.error(e);
		}
	}

	public static class NextCallback<E> implements Callback2<Boolean, E> {
		private final Callback<? super Void> parent;
		private final Callback<Void> iterationCallback;
		private final FunctionAsync<E, Void> iterationTask;

		public NextCallback(Callback<? super Void> parent, EnumeratorAsync<E> enumerator, FunctionAsync<E, Void> iterationTask) {
			this.parent = parent;
			this.iterationTask = iterationTask;
			this.iterationCallback = new IterationCallback<E>(parent, this, enumerator);
		}

		public void completed(Boolean found, E e) {
			try {
				if (found) {
					iterationTask.apply(iterationCallback, e);
				} else {
					parent.completed(null);
				}
			} catch (Throwable ex) {
				parent.error(ex);
			}
		}

		public void error(Throwable e) {
			parent.error(e);
		}

	}

	public static class IterationCallback<E> implements Callback<Void> {
		private final Callback<? super Void> parent;
		private final Callback2<Boolean, E> nextCallback;
		private final EnumeratorAsync<E> enumeratorAsync;

		public IterationCallback(Callback<? super Void> parent, Callback2<Boolean, E> nextCallback, EnumeratorAsync<E> enumerator) {
			this.parent = parent;
			this.nextCallback = nextCallback;
			this.enumeratorAsync = enumerator;
		}

		public void completed(Void result) {
			try {
				enumeratorAsync.next(nextCallback);
			} catch (Throwable e) {
				parent.error(e);
			}
		}

		public void error(Throwable e) {
			parent.error(e);
		}
	}

}
