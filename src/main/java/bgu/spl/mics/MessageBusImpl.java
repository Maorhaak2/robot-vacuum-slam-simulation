package bgu.spl.mics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import bgu.spl.mics.application.messages.CrashedBroadcast;
import bgu.spl.mics.application.messages.DetectObjectsEvent;
import bgu.spl.mics.application.messages.PoseEvent;
import bgu.spl.mics.application.messages.TerminatedBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrackedObjectsEvent;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {

	private final Map<MicroService, LinkedBlockingQueue<Message>> serviceQueues = new ConcurrentHashMap<>();
	private final Map<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> eventSubscribers = new ConcurrentHashMap<>();
	private final Map<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscribers = new ConcurrentHashMap<>();
	private final Map<Event<?>, Future<?>> futureMap = new ConcurrentHashMap<>();


	// Singleton
    private MessageBusImpl() {
		// Initialize the maps
		this.eventSubscribers.put(PoseEvent.class, new ConcurrentLinkedQueue<>());
		this.eventSubscribers.put(DetectObjectsEvent.class, new ConcurrentLinkedQueue<>());
		this.eventSubscribers.put(TrackedObjectsEvent.class, new ConcurrentLinkedQueue<>());
		this.broadcastSubscribers.put(TickBroadcast.class, new ConcurrentLinkedQueue<>());
		this.broadcastSubscribers.put(TerminatedBroadcast.class, new ConcurrentLinkedQueue<>());
		this.broadcastSubscribers.put(CrashedBroadcast.class, new ConcurrentLinkedQueue<>());
	}
	private static class Holder {
		private static final MessageBusImpl instance = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return Holder.instance;
	}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		/*eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		Queue<MicroService> subscribers = eventSubscribers.get(type);
		synchronized(subscribers) {
			if (!subscribers.contains(m)) {
				subscribers.add(m);
			}
		}*/
		eventSubscribers.computeIfAbsent(type, t->  new ConcurrentLinkedQueue<>()).add(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		/*broadcastSubscribers.putIfAbsent(type, new CopyOnWriteArrayList<>());
		List<MicroService> subscribers = broadcastSubscribers.get(type);
		synchronized(subscribers){
			if(!subscribers.contains(m)) {
				subscribers.add(m);
			}
		}*/
		broadcastSubscribers.computeIfAbsent(type, k -> new ConcurrentLinkedQueue<>()).add(m);
			
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = (Future<T>) futureMap.get(e);
		//?????????????????????????????/
		synchronized (e){
			if(future != null) {
				future.resolve(result);
				futureMap.remove(e);
			}
		}
		
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> l = broadcastSubscribers.get(b.getClass());
		//??????????????????????????
		synchronized(l){
			if(l!=null && !l.isEmpty()) {
				for(MicroService ms : l) {
					try {
					serviceQueues.get(ms).put(b); // Add to message queue
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		ConcurrentLinkedQueue<MicroService> q = eventSubscribers.get(e.getClass());
		
		synchronized(q) {
			if (q == null || q.isEmpty()) {
				return null;
			}
	
			MicroService ms = q.poll();
			if (ms != null) {
				q.add(ms);
				serviceQueues.get(ms).add(e);
			}
		}
	
		synchronized(e){
			Future<T> future = new Future<>();
			futureMap.put(e, future);
			return future;
		}
		
	}

	@Override
	public void register(MicroService m) {
		serviceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());

	}

	@Override
	public void unregister(MicroService m) {
		for (Queue<MicroService> subscribers : eventSubscribers.values()) {
			synchronized(subscribers){
				subscribers.remove(m);
			}
			
		}
		for (ConcurrentLinkedQueue<MicroService> subscribers : broadcastSubscribers.values()) {
			synchronized(subscribers){
				subscribers.remove(m);
			}
		}
		serviceQueues.remove(m);
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		LinkedBlockingQueue<Message> q = serviceQueues.get(m);
		if (q == null) {
			throw new IllegalStateException("Microservice is not registered");
		}
		return q.take();
	}

		// Getters for MessageBusImpl fields (for tests)
	public Map<MicroService, LinkedBlockingQueue<Message>> getMicroServiceQueues() {
		return serviceQueues;
	}

	public Map<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> getEventSubscribers() {
		return eventSubscribers;
	}

	public Map<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> getBroadcastSubscribers() {
		return broadcastSubscribers;
	}

	public Map<Event<?>, Future<?>> getFutureMap() {
		return futureMap;
	}

}
