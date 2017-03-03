package ticketingsystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Route {
	private ExecutorService executorService;
	
	private AtomicInteger atomicInteger = new AtomicInteger(0);
	private int routeid;
	private int coachnum;
	private int seatnum;
	private int stationnum;
	
	private List<Set<Integer>> idleSeat = new ArrayList<Set<Integer>>();
	private List<ReentrantReadWriteLock> lockList = new ArrayList<ReentrantReadWriteLock>();
	
	public Route(ExecutorService executorService, int routeid, int coachnum, int seatnum, int stationnum){
		this.executorService = executorService;
		this.routeid = routeid;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		
		for(int i = 0; i < stationnum; i++){
			Set<Integer> seats = new HashSet<Integer>();
			
			for(int j = 0; j < coachnum * seatnum; j++){				
				seats.add(j);
			}
			
			idleSeat.add(seats);
			lockList.add(new ReentrantReadWriteLock());
		}
	}
	
	public Ticket buyTicket(String passengers, int route, int departure, int arrival) throws InterruptedException, ExecutionException{
		FutureTask<Ticket> futureTask = new FutureTask<Ticket>(new GetSeatTask(passengers, departure, arrival));
		executorService.submit(futureTask);
		
		return futureTask.get();
	}
	
	public int inquiry(int route, int departure, int arrival) throws InterruptedException, ExecutionException{
		FutureTask<Integer> futureTask = new FutureTask<Integer>(new QuerySeatTask(departure, arrival));
		executorService.submit(futureTask);
		
		return futureTask.get();
	}
	
	public boolean refundTicket(Ticket ticket) throws InterruptedException, ExecutionException{
		FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new RefundTask(ticket));
		executorService.submit(futureTask);
		
		return futureTask.get();
	}
	
	private class GetSeatTask implements Callable<Ticket> {
		private String passenger;
		private int from;
		private int to;
		
		public GetSeatTask(String passenger, int departure, int arrival){
			this.passenger = passenger;
			this.from = departure - 1;
			this.to = arrival - 1;
		}

		@Override
		public Ticket call() throws Exception {
			try {
				for(int i = from; i <= to; i++){
					lockList.get(i).writeLock().lock();
				}
				
				Set<Integer> tmpSeats = new HashSet<Integer>();
				tmpSeats.addAll(idleSeat.get(from));
				
				for(int i = from + 1; i <= to; i++){
					tmpSeats.retainAll(idleSeat.get(i));
				}
				
				if(tmpSeats.size() == 0){
					return null;
				}			
				
				long mod = (long)(1e10);
				
				long threadid = Thread.currentThread().getId() % mod;
				long tid  = (System.currentTimeMillis() * mod + threadid) * 10000 + atomicInteger.incrementAndGet();
				
				int seat = tmpSeats.iterator().next();	
				int[] tic = toSeat(seat);
				for(int i = from; i <= to; i++){
					idleSeat.get(i).remove(seat);
				}
				
				
				return new Ticket(Math.abs(tid), passenger, routeid, tic[0], tic[1], from + 1, to + 1);
				
			} finally {
				for(int i = from; i <= to; i++){
					lockList.get(i).writeLock().unlock();
				}
			}
		}		
	}
	
	private class QuerySeatTask implements Callable<Integer>{
		private int from;
		private int to;

		public QuerySeatTask(int departure, int arrival) {
			this.from = departure - 1;
			this.to = arrival - 1;
		}
		
		@Override
		public Integer call() throws Exception {
			try {
				for(int i = from; i <= to; i++){
					lockList.get(i).readLock().lock();
				}
				
				Set<Integer> tmpSeats = new HashSet<Integer>();
				tmpSeats.addAll(idleSeat.get(from));
				
				for(int i = from + 1; i <= to; i++){
					tmpSeats.retainAll(idleSeat.get(i));
				}
				
				return tmpSeats.size();
				
			} finally {
				for(int i = from; i <= to; i++){
					lockList.get(i).readLock().unlock();
				}
			}
		}
		
	}
	
	public class RefundTask implements Callable<Boolean>{
		private Ticket ticket;
		private int from;
		private int to;
		
		public RefundTask(Ticket ticket){
			this.ticket = ticket;
			this.from = ticket.getDeparture() - 1;
			this.to = ticket.getArrival() - 1;
		}

		@Override
		public Boolean call() throws Exception {
			if(from < 0 || from >= stationnum ||
					to < 0 || to >= stationnum ||
					from >= to || 
					ticket.getCoach() <= 0 || ticket.getCoach() > coachnum ||
					ticket.getSeat() <= 0 || ticket.getSeat() > seatnum){
				return false;
			}
			
			try {
				for(int i = from; i <= to; i++){
					lockList.get(i).writeLock().lock();
				}
				
				Integer seat = toNum(ticket.getCoach(), ticket.getSeat());
				
				for(int i = from; i <= to; i++){
					if(idleSeat.get(i).contains(seat)){
						return false;
					}
				}
				
				for(int i = from; i <= to; i++){
					idleSeat.get(i).add(seat);
				}
				return true;				
			} finally {
				for(int i = from; i <= to; i++){
					lockList.get(i).writeLock().unlock();
				}
			}
		}
	}
	
	private int[] toSeat(int num){
		int coaNum = num / seatnum + 1;
		int seNum = num % seatnum + 1;
		
		return new int[]{coaNum, seNum};
	}
	
	private Integer toNum(int coaNum, int seNum){
		return (coaNum - 1) * seatnum + seNum - 1;
	}

}
