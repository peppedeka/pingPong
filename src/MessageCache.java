import java.util.ArrayList;
import java.util.List;

public class MessageCache {
	private List<Message> _messages = new ArrayList<Message>();
	private List<Peer> _fromPeers = new ArrayList<Peer>();
	private List<Peer> _toPeers = new ArrayList<Peer>();
	
	
	MessageCache(){	}

	public void add(Message msg, Peer fromPeer, Peer toPeer) {
		_messages.add(msg);
		_fromPeers.add(fromPeer);
		_toPeers.add(toPeer);
	}
	
	public void removeByMessage(Message msg){
		int index = getIndexByMessage(msg);
		_fromPeers.remove(index);
		_toPeers.remove(index);
		_messages.remove(index);
	}
	
	public int getIndexByMessage(Message msg){
		return _messages.indexOf(msg);
	}

	public Peer getNextPeer(Peer fromPeer) {
		int index = _toPeers.indexOf(fromPeer);
		return  _fromPeers.get(index);
	}
	
}
