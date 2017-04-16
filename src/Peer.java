import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class Peer extends Thread {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	private int _rejectPingTTLCounter = 0;
	private int _rejectPingGUIIDCounter = 0;
	private int _rejectPongTTLCounter = 0;
	private int _rejectPongGUIIDCounter = 0;

	private int _init=0;
	private Timer timer = new Timer();

	public String ip;
	public int port;
	public List<Peer> activePeersList = new ArrayList<Peer>();
	public List<UUID> fromUIID = new ArrayList<UUID>();
	public MessageCache pingCache = new MessageCache();

	public void bootstrap() {
		activePeersList.addAll(Overlay.getBootstrapList());
		removeMyselfFromActiveList();
		System.out.format("%s:%d bootstrap done, load %d peers\n", ip, port, activePeersList.size());
		/*stampIp();*/

	}

	public void removeMyselfFromActiveList() {
		int index= activePeersList.indexOf(this);
		if(index >= 0){
			System.out.format("\nremove myself %s from neghbour", ip);
			activePeersList.remove(index);
		}
	}

	public void stampIp() {
		System.out.format("%s active: \t",ip);
		for(int i = 0; i < activePeersList.size() ; i++){
			System.out.format("%s\t", activePeersList.get(i).ip);					
		}
		System.out.format("\n");
	}


	public Peer() {
		ip = Network.generateIPAddress();
		port = Network.generatePort();

		System.out.format("peer created with ip %s on port %d\t", ip, port);
		bootstrap();
	}

	private void connection(Peer toPeer){
		Message msg = new Message(this,"connect",null);
		send(toPeer, msg);
	}

	private void ping(Peer fromPeer, Peer toPeer) {
		Message msg = new Message(this,"ping",null);
		pingCache.add(msg, fromPeer, toPeer);
		send(toPeer, msg);			
	}
	private void ping(Message ping, Peer fromPeer, Peer toPeer) {
		pingCache.add(ping, fromPeer, toPeer);
		send(toPeer, ping);			
	}
	private void pong(Peer toPeer) {
		Payload payload = new Payload(this, 12, 12);
		Message msg = new Message(this,"pong", payload);
		send(toPeer, msg);			
	}
	private void pong(Peer toPeer, Message msg) {
		send(toPeer, msg);			
	}

	private void flood(Peer fromPeer, Message ping) {
		if( (_rejectPingTTLCounter + _rejectPingGUIIDCounter + _rejectPongTTLCounter + _rejectPongGUIIDCounter)>0){			
			System.out.format(ANSI_RED +"%s: <msg ping reject> TTL <= 0: %d, same GUIID: %d\t<msg pong reject> TTL <= 0: %d, same GUIID: %d"+ ANSI_RESET,ip, _rejectPingTTLCounter, _rejectPingGUIIDCounter, _rejectPongTTLCounter, _rejectPongGUIIDCounter);
			_rejectPingTTLCounter = 0;
			_rejectPingGUIIDCounter = 0;
			_rejectPongTTLCounter = 0;
			_rejectPongGUIIDCounter = 0;
		}
		int activePeersListLength = activePeersList.size();
		System.out.format("\n%s flood  %d neighbors\n",ip, activePeersListLength + Overlay.getBootstrapList().size());
		for(int i = 0; i < activePeersListLength ; i++){
			boolean bool1 =ip != activePeersList.get(i).ip;
			boolean bool2 = fromPeer.ip !=activePeersList.get(i).ip;
			/*System.out.format(ANSI_GREEN +"%d,%s != %s == %B && %s != %s == %B \n"+ ANSI_RESET,i,ip,activePeersList.get(i).ip,bool1,fromPeer.ip, activePeersList.get(i).ip,bool2);*/
			if(bool1 && bool2){
				/*System.out.format(ANSI_GREEN +"now: %s , from: %s to: %s"+ ANSI_RESET,ip, fromPeer.ip, activePeersList.get(i).ip);*/
				ping(ping,fromPeer, activePeersList.get(i));
			}
		}
	}

	private void send(Peer toPeer, Message msg) {
		toPeer.receive(this, msg);
	}
	public void receive(Peer fromPeer, Message msg) {
		discoverPeer(fromPeer);
		switch(msg.function()){
		case "connect":
			Message responseMsg = new Message(this,"ok",null);
			discoverPeer(fromPeer);
			send(fromPeer, responseMsg);
			break;
		case "ok":
			System.out.format(ANSI_CYAN + "%s estabilish connection with %s\n" + ANSI_RESET, ip, fromPeer.ip);
			ping(this,fromPeer);
			break;
		case "ping": 
			boolean boolGuiid = fromUIID.contains(msg.guid());
			/*		System.out.format(ANSI_GREEN + "\n msg: ttl:%d  hop:%d\t" + ANSI_RESET, msg.TTL(), msg.hops());*/
			if(msg.TTL()<=0){
				_rejectPingTTLCounter++;
				break;
			}
			if(boolGuiid == true){
				_rejectPingGUIIDCounter++;
				break;
			}

			msg.decrementTTL();
			msg.incrementHops();
			System.out.format(ANSI_GREEN + "ping: origin:%s  ---> ...  %s ---> %s TTL:%d hops:%d\n" + ANSI_RESET,  msg.fromPeer.ip, fromPeer.ip, ip,msg.TTL(),msg.hops());
			fromUIID.add(msg.guid());
			pong(fromPeer);
			new Thread(() -> {
				flood(fromPeer, msg);	
			}).start();
			break;
		case "pong":
			/**	System.out.format(ANSI_YELLOW + "\n msg: ttl:%d  hop:%d\t" + ANSI_RESET, msg.TTL(), msg.hops());*/
			boolean boolGuiidP = fromUIID.contains(msg.guid());

			if(msg.TTL()<=0){
				_rejectPongTTLCounter++;
				break;
			}
			if(boolGuiidP == true){
				_rejectPongGUIIDCounter++;
				break;
			}

			msg.decrementTTL();
			msg.incrementHops();
			System.out.format(ANSI_YELLOW + "pong: origin:%s  ---> ...  %s ---> %s TTL:%d hops:%d\n" + ANSI_RESET,  msg.fromPeer.ip, fromPeer.ip, ip,msg.TTL(),msg.hops());

			fromUIID.add(msg.guid());
			discoverPeer(msg.payload().peer());
			if(msg.fromPeer != this){
				new Thread(() -> {
					pong(pingCache.getNextPeer(fromPeer), msg);				
				}).start();
			}


			break;
		default: break;
		}


	}

	private void discoverPeer(Peer peer) {
		boolean alive = peer.currentThread().isAlive();
		boolean containPeer = activePeersList.contains(peer);
		boolean peerNotThis = peer != this;
		/*System.out.format(ANSI_BLUE + "%s DISCOVER ENTER %s\t" + ANSI_RESET, ip, peer.ip);
		System.out.format(ANSI_BLUE + "containPeer==%B peerNotThis==%B alive==%B\t" + ANSI_RESET, containPeer, peerNotThis, alive);*/
		if( containPeer == false && peerNotThis == true &&  alive == true){
			activePeersList.add(peer);
			System.out.format(ANSI_BLUE + "%s discover new peer %s\t" + ANSI_RESET, ip, peer.ip);
			stampIp();
			connection(peer);
		}

	}

	private void MyTimer(Peer peer) {
		TimerTask task;
		task = new TimerTask () {
			@Override
			public void run() { 
				System.out.format(ANSI_PURPLE +"%s: RELAY PING  %d neighbors\n"+ANSI_RESET,ip,activePeersList.size());
				for(int i = 0; i < activePeersList.size(); i++){
					peer.ping(peer,activePeersList.get(i));
				}
			}
		};
		timer.schedule(task, 0, 1000);

	}

	public void run() {			
		for(int i = 0; i < activePeersList.size(); i++){
			connection(activePeersList.get(i));
		}
		MyTimer(this);



	}

}
