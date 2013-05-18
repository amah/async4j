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
package org.async4j.foreach.parallel;

import org.async4j.Callback;
import org.async4j.Task;
import org.async4j.util.Producer;

/**
 * Parallel for construct
 * @author Amah AHITE
 *
 * @param <E> Element 
 */
public class ParallelForEach<E> implements Task<Producer<E>, Void> {
	private final Task<E, Void> iterationTask;
	private final FlowControllerFactory fcf;
	
	public ParallelForEach(FlowControllerFactory fcf, Task<E, Void> iterationTask) {
		this.fcf = fcf;
		this.iterationTask = iterationTask;
	}

	public void run(Callback<Void> k, Producer<E> producer) {
		try{
			ParallelForEachSM<E> sm = new ParallelForEachSM<E>(k, fcf, iterationTask);
			producer.produce(sm.getProducerCallback(), sm.getElementHandler());
		}catch (Throwable e) {
			k.error(e);
		}
	}

}
