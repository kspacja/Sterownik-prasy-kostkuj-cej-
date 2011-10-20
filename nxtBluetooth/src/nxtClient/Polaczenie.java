package nxtClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

public class Polaczenie {

	private String serviceURL;

	private LinkedList<byte[]> odebrane;
	private LinkedList<byte[]> doWyslania;
	
	Timer timer;

	InputStream is;
	OutputStream os;
	
	private static int byteToInt(byte b) {
		if (b<0)
			return b+255;
		else
			return b;
	}
	public static void drukuj(byte[] buffer) {
		if (buffer!=null) {
			for (int i=0;i<buffer.length;i++)
	        	System.out.print((Integer.toHexString(byteToInt(buffer[i]))+" ").toUpperCase() );
	        System.out.println();
		} else {
			System.out.println("NULL");
		}
	}

	public static void main(String[] args) throws InterruptedException {
		//adres do jarka
//		Polaczenie polaczenie = new Polaczenie("btspp://0016530D3A52:1;authenticate=false;encrypt=false;master=false", 500);
		//adres do ewy
		Polaczenie polaczenie = new Polaczenie("btspp://0016530BD2F6:1;authenticate=false;encrypt=false;master=false", 500);
		
		
		//silniki
		polaczenie.wyslij(new byte[] {0x00,0x04,0x00,90,0x01,0x00,0,0x20,0,0,0,0, 0});
		polaczenie.wyslij(new byte[] {0x00,0x04,0x00,40,0x01,0x00,0,0x20,0,0,0,0, 0});
		polaczenie.wyslij(new byte[] {0x00,0x04,0x00,90,0x01,0x00,0,0x00,0,0,0,0, 0});
//		polaczenie.wyslij(new byte[] {0x00,0x04,0x00,40,0x01,0x00,0,0x20,0,0,0x00,0, 0});
//		polaczenie.wyslij(new byte[] {0x00,0x04,0x00,0,0x01,0x00,0,0x00,0,0,0x00,0, 0});

		

		//dzwieki
		polaczenie.wyslij(new byte[] {0x00,0x03, 0, 1, 120, 3});
		polaczenie.wyslij(new byte[] {0x00,0x03, 0, 1, 120, 1});
		polaczenie.wyslij(new byte[] {0x00,0x03, 0, 1, 120, 3});

		Thread.sleep(2000);
		drukuj(polaczenie.odbierz());
		Thread.sleep(6000);
		//wylacz silnik
		polaczenie.wylacz();
	}
	
	// konstruktor(adres uslugi, odstêp wysy³ania milisekundy)
	public Polaczenie(String url, int interval) {
		doWyslania = new LinkedList<byte[]>();
		odebrane = new LinkedList<byte[]>();
		
		serviceURL = url;
		System.out.print("£¹czenie z urz¹dzeniem... ");

		try {
			StreamConnection connection = (StreamConnection) Connector.open(serviceURL);
			
			is = connection.openInputStream();
			os = connection.openOutputStream();
			
			System.out.println("Ok.");
			
			timer = new Timer();
			Zadanie zadanie = new Zadanie( ); 

			timer.scheduleAtFixedRate(zadanie, 0, interval);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void wyslij(byte[] buffer) {
		doWyslania.addLast(buffer);
	}
	
	public byte[] odbierz() {
		byte buffer[]= null;
		try {
			int ile;
			ile = is.available();
			if (ile>0) {
				buffer = new byte[2];
				is.read(buffer);
				buffer=new byte[buffer[0]];
				is.read(buffer);
				odebrane.addLast(buffer);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!odebrane.isEmpty())
			buffer = odebrane.pollFirst();
		return buffer;
	}
	
	public void wylacz() {
		timer.cancel();
	}
	
	
	class Zadanie extends TimerTask 
	{

		public void run( ) 
		{
			if (is != null) {
				byte buffer[];
				try {
	//				ZAPIS
					if (!doWyslania.isEmpty()) {
						buffer = doWyslania.pollFirst();
						os.write(buffer.length);
			            os.write(0x00);
			            os.write(buffer);
			            os.flush();
					}
	//				ODCZYT
					int ile = is.available();
					if (ile>0) {
						buffer = new byte[2];
			            is.read(buffer);
			            buffer=new byte[buffer[0]];
			            is.read(buffer);
			            odebrane.addLast(buffer);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} 
	} 

}







