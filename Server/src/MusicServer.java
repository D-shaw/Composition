import java.io.*;
import java.net.*;
import java.util.*;

public class MusicServer {

	// use ObjectOuputStream instead of PrintWriter because not String 
	// but two serialized objects are transferred.
	ArrayList<ObjectOutputStream> clientOutputStreams;
	HashMap<Socket, String> socketAndNameMap = new HashMap<Socket, String>();
	
	public static void main(String[] args) {
		new MusicServer().go();
	}
	
	public void go() {
		clientOutputStreams =  new ArrayList<ObjectOutputStream>();
		try {
			ServerSocket serverSock = new ServerSocket(4242);
			while (true) {
				// listen until new connection is made to this socket. and accept it.
				Socket clientSocket = serverSock.accept();
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
				
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				clientOutputStreams.add(out);
				
				System.out.println("got a connection");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public class ClientHandler implements Runnable {
		
		ObjectInputStream in;
		Socket clientSocket;
		
		public ClientHandler(Socket socket) {
			try {
				clientSocket = socket;
				in = new ObjectInputStream(clientSocket.getInputStream());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		public void run() {
			Object o1 = null;
			Object o2 = null;
			Object o3 = null;
			try {
				while ((o1 = in.readObject()) != null) {
					int state = (int) o1;
					if (state == 0) {
						o2 = in.readObject();
						String userName = (String) o2;
						System.out.println("New User " + userName);
						socketAndNameMap.put(clientSocket, userName);
						String message = "" + userName + " joined.";
						tellEveryone(o1, message);
					} else if (state == 1) {	// sent-in is a Midi and its message
						o2 = in.readObject();
						System.out.println("Receive Midi");
						o3 = in.readObject();
						tellEveryone(o1, o2, o3);
					} else if (state == 2) {	// sent-in is a Chat Message
						o2 = in.readObject();
						System.out.println("Receive Chat Message");
						tellEveryone(o1, o2);
					}
				}
			} catch (Exception ex) {
				// ex.printStackTrace();
				int state = 0;
				String userName = socketAndNameMap.remove(clientSocket);
				String message = "" + userName + " left.";
				tellEveryone(state, message);
			}
		}
	}
	
	// distribute Piano
	public void tellEveryone(Object o1, Object o2, Object o3) {
		Iterator it = clientOutputStreams.iterator();
		while (it.hasNext()) {
			try {
				ObjectOutputStream out = (ObjectOutputStream) it.next();
				out.writeObject(o1);
				out.writeObject(o2);
				out.writeObject(o3);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	// distribute Chat Message
	public void tellEveryone(Object o1, Object o2) {
		Iterator it = clientOutputStreams.iterator();
		while (it.hasNext()) {
			try {
				ObjectOutputStream out = (ObjectOutputStream) it.next();
				out.writeObject(o1);
				out.writeObject(o2);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
}
