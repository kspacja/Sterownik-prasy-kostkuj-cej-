package comm;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class SocketConnection implements Runnable
{
	DatagramSocket socket;
	// Wątek, który nasłuchuje wiadomości z socketa i umieszcza je w kolejce
	private Thread socketReader;
	// Adres zwrotny, czyli ostatni adres z którego przyszedł jakiś pakiet
	private SocketAddress returnAddress = null;
	private byte[] lenbuf = new byte[4];
	
	private LinkedList<byte[]> fromSocket = new LinkedList<byte[]>();
	
	public SocketConnection(int port) throws SocketException
	{
		socket = new DatagramSocket(port);
		socketReader = new Thread(this);
		socketReader.start();
	}
	
	@Override
	public void run()
	{
		while(!socket.isClosed())
		{
			DatagramPacket lenpacket = new DatagramPacket(lenbuf, lenbuf.length);
			
			// Oczekuje na otrzymanie pakietu z długością komendy
			try
			{
				socket.receive(lenpacket);
			}
			catch(IOException e)
			{
				if(!socket.isClosed())
				{
					System.err.println("!! Receiving a packet failed");
					e.printStackTrace();
					continue;
				}
				else
					break;
			}
			
			int len = (lenpacket.getData()[0] << (3*8)) |
					(lenpacket.getData()[1] << (2*8)) |
					(lenpacket.getData()[2] << 8) |
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
				if(!socket.isClosed())
				{
					System.err.println("!! Receiving a packet failed");
					e.printStackTrace();
					continue;
				}
				else
					break;
			}
			
			synchronized(this)
			{
				fromSocket.add(packet.getData());
				returnAddress = packet.getSocketAddress();
			}
			
			System.out.println("Otrzymano przez port UDP: " + fromBytearray(packet.getData()));
			
			// Zeruję bufor długości
			java.util.Arrays.fill(lenbuf, (byte)0);
			// Pobudza wątek czekający na żądania z socketu
			synchronized(this)
			{
				notify();
			}
		}
	}
	
	public boolean isAvailable()
	{
		return !fromSocket.isEmpty();
	}
	
	public synchronized byte[] receive()
	{
		return fromSocket.removeFirst();
	}
	
	public synchronized String receiveString()
	{
		return fromBytearray(fromSocket.removeFirst());
	}
	
	public synchronized void reply(byte[] msg) throws IOException
	{
		if(returnAddress == null)
			throw new IOException("There's no return address to reply to");
		
		// Wysyłam długość pakietu
		byte[] len = new byte[4];
		ByteBuffer wrapper = ByteBuffer.wrap(len);
		wrapper.putInt(msg.length);

		DatagramPacket ts = new DatagramPacket(len, len.length, returnAddress);
		socket.send(ts);
		
		// I właściwy pakiet
		ts = new DatagramPacket(msg, msg.length, returnAddress);
		socket.send(ts);
	}
	
	public synchronized void reply(String msg) throws IOException
	{
		System.out.println("Wysłano przez port UDP: " + msg);
		reply(toBytearray(msg));
	}
	
	@Override
	public void finalize()
	{
		socket.close();
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
}
