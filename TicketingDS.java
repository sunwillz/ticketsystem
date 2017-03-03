package ticketingsystem;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author sunyangwei
 *
 */
public class TicketingDS implements TicketingSystem {
	
	/**
	 * 
	 */
	static final int thread = 16;//每个线程是一个票务代理 
	
	private ExecutorService executorService = Executors.newFixedThreadPool(thread);
	
	static BlockingQueue<Ticket> list = new LinkedBlockingQueue<>();

	private int routenum = 5;
	private int coachnum = 8;
	private int seatnum = 100;
	private int stationnum = 10;
	
	private Route[] routes = new Route[routenum];
	
	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum){
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		
		for(int i = 1; i <= routenum; i++){
			routes[i - 1] = new Route(executorService, i, coachnum, seatnum, stationnum);
		}
	}
		

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		if(route <= 0 || route > routenum || 
				departure <= 0 || departure >= stationnum ||
				arrival <= 1 || arrival > stationnum ||
				arrival <= departure){
			return null;
		}
		Ticket ret = null;
		try {
			ret = routes[route - 1].buyTicket(passenger, route, departure, arrival);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		list.add(ret);
		return ret;
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		if(route <= 0 || route > routenum || 
				departure <= 0 || departure >= stationnum ||
				arrival <= 1 || arrival > stationnum ||
				arrival <= departure){
			return 0;
		}
		int ret = 0;
		try {
			ret = routes[route - 1].inquiry(route, departure, arrival);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		int route = ticket.getRoute();
		if(route <= 0 || route > routenum){
			return false;
		}
		//是否有效票标志位
		boolean flag = false;
		for(Ticket t:list){
			Long a = ticket.getTid();
			Long b = t.getTid();
			if(a.equals(b)&&ticket.getPassenger().equals(t.getPassenger())&&ticket.getRoute()==t.getRoute()&&
					ticket.getDeparture()==t.getDeparture()&&ticket.getArrival()==t.getArrival()&&
					ticket.getCoach()==t.getCoach()&&ticket.getSeat()==t.getSeat())
			{	
				flag= true;
				list.remove(t);
			}	
		}
		boolean ret = false;
		if(flag){
			try {
				ret = routes[route - 1].refundTicket(ticket);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public void shutDown(){
		executorService.shutdown();;
	}
}
