import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;


public class ServerThread extends Thread{

	DatagramSocket socket;
	
	public ServerThread(String name) throws IOException {
	    super(name);
	    this.socket = new DatagramSocket(666);	    
	}  
	
	@Override
	public void run() {
		boolean isRun = true;
		while(isRun){
			byte[] buf = new byte[4];
			
			try {
				
				//set length
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				this.socket.receive(packet);
				
				byte[] data = packet.getData();	
				ByteBuffer bb = ByteBuffer.wrap(data);
				bb.position(0);
				int length = bb.getInt();
				
				System.out.println("length = " + length);
				
				//get string
				buf = new byte[length];
				
				packet = new DatagramPacket(buf, buf.length);
				this.socket.receive(packet);
				
				String stringData = new String(packet.getData());
				
				System.out.println(stringData);
				
				isRun = stringData.compareTo("exit") != 0;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		socket.close();
		System.out.println("Turn off server");
	}
	
}
