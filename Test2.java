package ticketingsystem;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sunyangwei
 *
 */
public class Test2 {
	
	static boolean printflag = false;//是否将操作结果写入文件,false表示不写入文件，只输出到控制台，true表示写入文件
	static int thread_cnt = 100;//并发的线程数，模拟多个用户同时买票、查询、退票行为。
	
	private static ExecutorService  executorService = Executors.newFixedThreadPool(thread_cnt);
	
	private final static int routenum = 5 ;//车次总数
	private final static int coachnum = 8;//列车车厢数
	private final static int seatnum = 100;//座位数
	private final static int stationnum = 10;//车次经停站的数量
	
	private static TicketingDS ticketingDS = new TicketingDS(routenum,coachnum,seatnum,stationnum);
	//统计售票情况
	private static AtomicInteger counter = new AtomicInteger(0);
	
	public static class BuyTicket implements Runnable {
		@Override
		public void run() {
			Random random = new Random(System.currentTimeMillis());
			//随机生成乘客姓名字符串
			String name = "" + (char)('a' + random.nextInt(26))+(char)('a' + random.nextInt(26)) + (char)('a' + random.nextInt(26));
			
			int route = random.nextInt(routenum) + 1;
			int departure = random.nextInt(stationnum - 1) + 1;
			int arrival = random.nextInt(stationnum - departure) + 1 + departure;
			
			Ticket ticket = ticketingDS.buyTicket(name, route, departure, arrival);
			if(printflag)
				System.out.println("[" + (counter.incrementAndGet()) + ":出售" + ticket + "]");
			
		}
	}
	
	public static class RefundTicket implements Runnable {
		@Override
		public void run() {
				boolean res  = false;
				Ticket ticket = TicketingDS.list.peek();
				if(ticket != null){
					res = ticketingDS.refundTicket(ticket);
				}
				if(printflag)
					System.out.println("退票:[" + ticket + "  " + res + "]");
		}
	}
	
	public static class QueryTicket implements Runnable {

		@Override
		public void run() {
			Random random = new Random(System.currentTimeMillis());
			int route = random.nextInt(routenum) + 1;
			int departure = random.nextInt(stationnum - 1) + 1;
			int arrival = random.nextInt(stationnum - departure) + 1 + departure;
			
			int res = ticketingDS.inquiry(route, departure, arrival);
			if(printflag)
				System.out.println("查询:[" + route + "车次 \t出发站：" + departure + "\t到达站：" + arrival + "\t余票：" + res + "]");
		}
		
	}

	public static void main(String[] args){
		//重定向 for debug
		if(printflag){
			PrintStream ps = null;
			try {
				ps = new PrintStream(new FileOutputStream(TicketingDS.thread
						+ ".txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			System.setOut(ps);
		}
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < 3000; i++){
			executorService.execute(new QueryTicket());	
			executorService.execute(new BuyTicket());
		}
		for (int i = 0; i < 1000; i++) {
			executorService.execute(new QueryTicket());	
			executorService.execute(new RefundTicket());
		}
		for (int i = 0; i < 2000; i++) {
			executorService.execute(new QueryTicket());
		}
		
		executorService.shutdown();
		while(true){
			if(executorService.isTerminated()){
				long end = System.currentTimeMillis();
				long time = (end-start);
				float per = (float)(end-start)/10000;
				System.out.println("线程(票务代理)数为:"+TicketingDS.thread);
				System.out.println("总运行时间："+time+"毫秒");
				System.out.println("每个方法运行时间："+per+"毫秒");
				System.out.println("吞吐率："+(float)1/per*1000);
				System.exit(0);
			}
		}
		
	}
}
