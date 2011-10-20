package comm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.LinkedList;

public class SocketServer implements Runnable
{
	private DatagramSocket socket;
	private byte[] lenbuf = new byte[4];
	
	public SocketServer(int port) throws SocketException
	{
		socket = new DatagramSocket(port);
	}

	@Override
	public void run()
	{
		while(true)
		{
			DatagramPacket lenpacket = new DatagramPacket(lenbuf, lenbuf.length);
			
			// Oczekuje na otrzymanie pakietu z długością komendy
			try
			{
				socket.receive(lenpacket);
			}
			catch(IOException e)
			{
				System.err.println("!! Receiving a packet failed");
				e.printStackTrace();
				continue;
			}
			
			int len = (lenpacket.getData()[0] << 3) +
					(lenpacket.getData()[1] << 2) +
					(lenpacket.getData()[2] << 1) +
					lenpacket.getData()[3];
			
			// Oczekuje na otrzymanie właściwego pakietu
			byte[] buf = new byte[len];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			
			try
			{
				socket.receive(packet);
			}
			catch(IOException e)
			{
				System.err.println("!! Receiving a packet failed");
				e.printStackTrace();
				continue;
			}
			
			// Przetwarzanie i ewentualna odpowiedź
			byte[] response = processRequest(fromBytearray(packet.getData()));
			
			if(response != null)
			{
				try
				{
					DatagramPacket reply = new DatagramPacket(response, response.length,
						packet.getSocketAddress());
					socket.send(reply);
				}
				catch(IOException e)
				{
					System.err.println("!! Sending a packet failed");
					e.printStackTrace();
				}
			}
			
			// Zeruję bufor długości
			java.util.Arrays.fill(lenbuf, (byte)0);
		}
	}
	
	@Override
	public void finalize()
	{
		socket.close();
	}
	
	/**
	 * Metoda odpowiedzialna za reagowanie na żądania otrzymane przez socket
	 * @param reqstring pełny tekst żądania
	 * @return wiadomość zwrotna (null, jeśli jej nie ma)
	 */
	private byte[] processRequest(String req)
	{
		System.out.println(req);
		return null;
	}
	
	private static byte[] toBytearray(String str)
	{
		byte[] res = null;
		
		try
		{
			res = str.getBytes("utf8");
		}
		catch(UnsupportedEncodingException e)
		{
			System.err.println("!! The system doesn't support UTF-8... why?");
			System.exit(9);
		}
		
		return res;
	}
	
	private static String fromBytearray(byte[] arr)
	{
		String res = null;
		
		try
		{
			res = new String(arr, "utf8");
		}
		catch(UnsupportedEncodingException e)
		{
			System.err.println("!! The system doesn't support UTF-8... why?");
			System.exit(9);
		}
		
		return res;
	}
	
	public static void main(String[] args) throws SocketException
	{
		SocketServer s = new SocketServer(6666);
		s.run();
	}
}
