import java.util.ArrayList;
import java.util.List;
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

	public String ip;
	public int port;
	public List<Peer> activePeersList = new ArrayList<Peer>();
	public List<UUID> fromUIID = new ArrayList<UUID>();

	public void bootstrap() {
		activePeersList.addAll(Overlay.getBootstrapList());
		removeMyselfFromActiveList();
		System.out.format("%s:%d bootstrap done, load %d peers\n", ip, port, activePeersList.size());
		stampIp();

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

	private void ping(Peer toPeer) {
		Message msg = new Message(this,"ping",null);
		send(toPeer, msg);			
	}
	private void pong(Peer toPeer) {
		Payload payload = new Payload(this, 12, 12);
		Message msg = new Message(this,"pong", payload);
		send(toPeer, msg);			
	}

	private void flood(Message ping) {
		if( (_rejectPingTTLCounter + _rejectPingGUIIDCounter + _rejectPongTTLCounter + _rejectPongGUIIDCounter)>0){			
			System.out.format(ANSI_RED +"<msg ping reject> TTL <= 0: %d, same GUIID: %d\t<msg pong reject> TTL <= 0: %d, same GUIID: %d"+ ANSI_RESET, _rejectPingTTLCounter, _rejectPingGUIIDCounter, _rejectPongTTLCounter, _rejectPongGUIIDCounter);
			_rejectPingTTLCounter = 0;
			_rejectPingGUIIDCounter = 0;
			_rejectPongTTLCounter = 0;
		}
		_rejectPongGUIIDCounter = 0;
		int activePeersListLength = activePeersList.size();
		System.out.format("\n%s flood  %d neighbors\n",ip, activePeersListLength);
		for(int i = 0; i < activePeersListLength ; i++){
			if(ip != activePeersList.get(i).ip){
				send(activePeersList.get(i), ping);;								
			}
		}
	}

	private void send(Peer toPeer, Message msg) {
		toPeer.receive(this, msg);
	}
	public void receive(Peer fromPeer, Message msg) {
		switch(msg.function()){
		case "connect":
			Message responseMsg = new Message(this,"ok",null);
			discoverPeer(fromPeer);
			send(fromPeer, responseMsg);
			break;
		case "ok":
			System.out.format(ANSI_CYAN + "%s estabilish connection with %s\n" + ANSI_RESET, ip, fromPeer.ip);
			ping(fromPeer);
			break;
		case "ping": 
			boolean boolGuiid = fromUIID.contains(msg.guid());
			/**		System.out.format(ANSI_GREEN + "\n msg: ttl:%d  hop:%d\t" + ANSI_RESET, msg.TTL(), msg.hops());*/
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
			flood(msg);											

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
			flood(msg);

			break;
		default: break;
		}


	}

	private void discoverPeer(Peer peer) {
		/**System.out.format(ANSI_BLUE + "%s DISCOVER ENTER %s\t" + ANSI_RESET, ip, peer.ip);
		stampIp();*/
		boolean alive = peer.isAlive();
		if(activePeersList.contains(peer) == false && peer != this &&  alive == true){
			activePeersList.add(peer);
			System.out.format(ANSI_BLUE + "%s discover new peer %s\t" + ANSI_RESET, ip, peer.ip);
			connection(peer);
		}

	}
	
	public void destroy() {
		this.interrupt();
	}


	public void run() {
		for(int i = 0; i < activePeersList.size(); i++){
			connection(activePeersList.get(i));
		}
	}

}
