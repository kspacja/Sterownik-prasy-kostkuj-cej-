package comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class BluetoothConnection
{
	private StreamConnection conn;
	private InputStream istream;
	private OutputStream ostream;
	
	public BluetoothConnection(String URL) throws IOException
	{
		conn = (StreamConnection) Connector.open(URL);
		istream = conn.openInputStream();
		ostream = conn.openOutputStream();
		
		// Wymuszam autoryzację, wysyłając pakiet keepalive
		//TODO Paweł chce to robić inaczej, więc to jest tymczasowe
		send(new byte[]{(byte)0x80, 0x0D});
		
		System.out.println("Connected to robot");
	}
	
	@Override
	public void finalize()
	{
		try
		{
			conn.close();
		}
		// I tak już kończymy, nie ma znaczenia co stanie się z tym połączeniem
		catch(IOException e){}
	}
	
	public void send(byte[] msg) throws IOException
	{
		System.out.println("sent to robot: " + Arrays.toString(msg));
		ostream.write(msg.length);
		ostream.write(0);
		ostream.write(msg);
		ostream.flush();
	}
	
	/**
	 * Metoda odczytuje i zwraca dokładnie jedną wiadomość od robota, nawet jeśli
	 * oczekujących w strumieniu jest więcej.
	 * @return wiadomość od robota, null jeśli nie ma żadnej oczekującej
	 * @throws IOException
	 */
	public byte[] receive() throws IOException
	{
		byte[] res = null;
		
		// Ponieważ znamy protokół, możemy założyć, że wszystkie wiadomości będą się do niego
		// stosować i nie musimy sprawdzać, ile dokładnie otrzymaliśmy bajtów
		if(istream.available() > 0)
		{
			byte[] lenbuf = new byte[2];
			istream.read(lenbuf);
			
			res = new byte[lenbuf[0]];
			istream.read(res);
		}
		
		System.out.println("got from robot: " + Arrays.toString(res));
		
		return res;
	}
	
	// Pozbywa się wszystkich danych, które zalegają w buforze
	public void clearInput() throws IOException
	{
		while(istream.available() > 0)
			istream.read();
	}
}
