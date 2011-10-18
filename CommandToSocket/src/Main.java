import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		StringBuilder commandList = new StringBuilder();
		int waitTime = 0;
		
		for (int i = 0; i < args.length; i++){
			if (args[i].compareTo("-t") == 0 ){ //if wait time is setted
				i++; //go to next argument
				try {
					if (args.length > 1) waitTime = Integer.parseInt(args[i]);
				}
				catch(NumberFormatException e){
					System.err.println("Time of waiting has to be integer");
					System.exit(0); //in case of error exit app
				}
			}
			else commandList.append(args[i] + " "); //add command
		}
		
		if (commandList.length() < 1) { //if there is no command exit app
			System.err.println("No command to send");
			System.exit(0);
		}
		
		/*for (String command: commandList){
			System.out.println(command);
		}
		*/
		
		DatagramSocket socket;
		try {
			socket = new DatagramSocket();
			
		    // send command
	        byte[] buf = commandList.toString().getBytes();
	        
	        Integer length = buf.length;
	        
	        byte[] len = ByteBuffer.allocate(4).putInt(length).array();
	        
	        InetAddress address = InetAddress.getByName("localhost");
	        
	        DatagramPacket packet = new DatagramPacket(len, len.length, address, 666);
	        socket.send(packet);
	        
	        buf = commandList.toString().getBytes();
	        
	        packet = new DatagramPacket(buf, buf.length, address, 666);
	        socket.send(packet);
	        
	        socket.close();
	        
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
 
		
		
		//System.out.println("Wait time: " + waitTime);
		
		
		System.exit(0);
		
	}

}
