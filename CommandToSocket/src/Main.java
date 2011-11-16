import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
	        
	        DatagramPacket packet = new DatagramPacket(len, len.length, address, 6666);
	        socket.send(packet);
	        
	        buf = commandList.toString().getBytes();
	        
	        packet = new DatagramPacket(buf, buf.length, address, 6666);
	        socket.send(packet);
	        
	        // only wait for reply if the user specified how long
	        // we don't want to get them stuck forever
	     	if(waitTime > 0)
	     	{
	     		socket.setSoTimeout(waitTime);
	     		
	     		// There are three possible replies, in order:
	     		// Command parse status, robot status, reply (not always)
	     		// So we wait three times
	     		
	     		try
	     		{
	     			//for(int i=0; i<2; ++i)
	     			//{
	     			
	     			
	     			//FIXME This is too late
	     			// The command status slips through
	     			// We may try and disregard this,
	     			// it'll catch the answer anyway
	     			
	     			// We'll just catch one answer, never mind the rest

	     			// receive length
	     			packet = new DatagramPacket(len, len.length);
	     			socket.receive(packet);

	     			length = (packet.getData()[0] << 3) +
	     					(packet.getData()[1] << 2) +
	     					(packet.getData()[2] << 1) +
	     					packet.getData()[3];

	     			buf = new byte[length];

	     			// receive packet
	     			packet = new DatagramPacket(buf, buf.length);
	     			socket.receive(packet);

	     			String toPrint = new String(packet.getData());
	     			if(!toPrint.startsWith("OK"))
	     				System.out.println(toPrint);

	     			//TODO Recognize if the message will expect a reply
	     			// and skip the third wait if it does not
	     				
	     			//}
	     		}
	     		catch(SocketTimeoutException e)
	     		{
	     			//System.out.println("Timed out");
	     		}
	     	}
	        
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
