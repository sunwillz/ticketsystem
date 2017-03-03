package ticketingsystem;

class Ticket{
	long tid = 0;//车票编号
	String passenger;//乘客名字
	int route;//列车车次
	int coach;//车厢号
	int seat;//座位号
	int departure;//出发站编号
	int arrival;//到达站编号
	
	public Ticket(){}
	public Ticket(long tid,String passenger,int route,int coach,int seat,int departure,int arrival){
		this.tid = tid;
		this.passenger = passenger;
		this.route = route;
		this.coach = coach;
		this.seat = seat;
		this.departure = departure;
		this.arrival = arrival;
	}
	
	public long getTid() {
		return tid;
	}
	public void setTid(long tid) {
		this.tid = tid;
	}
	public String getPassenger() {
		return passenger;
	}
	public void setPassenger(String passenger) {
		this.passenger = passenger;
	}
	public int getRoute() {
		return route;
	}
	public void setRoute(int route) {
		this.route = route;
	}
	public int getCoach() {
		return coach;
	}
	public void setCoach(int coach) {
		this.coach = coach;
	}
	public int getSeat() {
		return seat;
	}
	public void setSeat(int seat) {
		this.seat = seat;
	}
	public int getDeparture() {
		return departure;
	}
	public void setDeparture(int departure) {
		this.departure = departure;
	}
	public int getArrival() {
		return arrival;
	}
	public void setArrival(int arrival) {
		this.arrival = arrival;
	}
	
	@Override
	public String toString(){
		return "[车票id:"+tid+",乘客姓名:"+passenger+",车次:"+route+",座位号:"+coach+"车厢"+seat+"号,"+"出发站:"+departure+",到达站:"+arrival+"]";
	}
	
}

public interface TicketingSystem {
	
	/**
	 * 购票方法，即乘客passenger购买route车次从departure站到arrival站的车票一张
	 * 若购票成功，返回有效的ticket对象，若失败（即无余票），返回无效的ticket对象（即return null）
	 * @param passenger
	 * @param route
	 * @param departure
	 * @param arrival
	 * @return
	 */
	Ticket buyTicket(String passenger,int route,int departure, int arrival);
	
	
	/**
	 * 查询余票方法，即查询route车次从departure到arrival的余票数
	 * @param route
	 * @param departure
	 * @param arrival
	 * @return
	 */
	int inquiry(int route,int departure,int arrival);
	
	/**
	 * 退票方法，对有效的ticket对象返回true，对无效的ticket对象返回false
	 * @param ticket
	 * @return
	 */
	boolean refundTicket(Ticket ticket);
}