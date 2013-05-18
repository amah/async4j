package org.async4j.foreach.parallel;

import java.util.concurrent.atomic.AtomicBoolean;

import org.async4j.Callback;
import org.async4j.Task;
import org.async4j.util.ElementHandler;


/**
 * Parallel for loop state machine that observer the producer and flow controller to determine
 * the end of the for loop task.
 * @author Amah AHITE
 *
 * @param <E>
 */
public class ParallelForEachSM<E>{
	private final FlowController<E> flowController;
	private final Callback<Void> parentK;
	private final ExceptionAggregator exceptionAggregator = new DefaultExceptionAggregator();
	private final AtomicBoolean ended = new AtomicBoolean();
	private volatile boolean error = false;
	private volatile boolean producerEnded = false;
	private final Task<E, Void> iterationTask;
	private final Callback<Void> producerCallback = new ProducerCallback();
	private final Callback<Void> iterationCallback = new IterationEndCallback();
	private final ElementHandler<E> elementHandler = new ProducerElementHandler();

	public ParallelForEachSM(Callback<Void> parentK, FlowControllerFactory fcf, Task<E, Void> iterationTask) {
		this.parentK = parentK;
		this.flowController = fcf.create(iterationCallback);
		this.iterationTask = iterationTask;
	}

	protected void mayCompleted(){
		if(producerEnded){
			if(! flowController.isRunning()){
				if(! ended.getAndSet(true)){
					if(! error){
						parentK.completed(null);
					}
					else{
						parentK.error(exceptionAggregator.getAggregated());
					}
				}
			}
		}
	}
	
	
	public Callback<Void> getProducerCallback() {
		return producerCallback;
	}

	public ElementHandler<E> getElementHandler() {
		return elementHandler;
	}


	protected class ProducerElementHandler implements ElementHandler<E>{
		
		public void handleElement(Callback<Void> k, E e) {
			try{
				if(error){
					k.error(AbortException.INSTANCE);
				}
				else{
					flowController.run(k, iterationTask, e);
				}
			}catch (Throwable t) {
				k.error(t);
			}
		}
	}
	protected class ProducerCallback implements Callback<Void>{
		public void completed(Void result) {
			producerEnded = true;
			mayCompleted();
		}

		public void error(Throwable e) {
			exceptionAggregator.handle(e);
			error = true;
			producerEnded = true;
			mayCompleted();
		}
	}
	
	
	protected class IterationEndCallback implements Callback<Void>{
		public void completed(Void v) {
				mayCompleted();
		}

		public void error(Throwable e) {
			exceptionAggregator.handle(e);
			error = true;
			mayCompleted();
		}
	}
}
