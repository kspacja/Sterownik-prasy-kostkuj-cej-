package comm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;


public class RobotMaster
{
	private static RobotMaster instance = new RobotMaster(); // Singleton
	private Thread robotTalker; // Wątek, który komunikuje się z robotem
	
	private LinkedList<byte[]> toRobot = new LinkedList<byte[]>();	
	private LinkedList<byte[]> fromRobot = new LinkedList<byte[]>();
	
	// Cztery sensory, pierwszy bajt drugiej tablicy to typ, a drugi to tryb
	private byte[][] sensorData = new byte[4][2];
	
	private RobotMaster()
	{
		robotTalker = new Thread(new Runnable(){
			public void run()
			{
				while(true)
				{
					while(!toRobot.isEmpty())
					{

					}

					try
					{
						wait();
					}
					catch(InterruptedException e)
					{
						break; // Kończę wątek
					}
				}
			}
		});
		
		robotTalker.start();
	}
	
	public static RobotMaster getInstance()
	{
		return instance;
	}
	
	public byte[] getSensorData(byte sensor)
	{
		return new byte[]{sensorData[sensor][0], sensorData[sensor][1]};
	}
	
	private synchronized void send(byte[] msg)
	{
		toRobot.add(msg);
		notify();
	}
	
	// Zwraca pierwszy element z listy otrzymanych, null jeśli nic nie ma
	public synchronized byte[] receive()
	{
		return fromRobot.isEmpty() ? null : fromRobot.pop();
	}
	
	public void motor(byte which, byte power, byte mode, byte reg, byte tratio,
		byte rstate, int tachoLimit)
	{
		ByteBuffer bb = ByteBuffer.wrap(new byte[11]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.put((byte)0);
		bb.put((byte)4);
		bb.put(which);
		bb.put(power);
		bb.put(mode);
		bb.put(reg);
		bb.put(tratio);
		bb.put(rstate);
		bb.putInt(tachoLimit);
		
		send(bb.array());
	}
	
	public void setSensor(byte which, byte type, byte mode)
	{
		sensorData[which][0] = type;
		sensorData[which][1] = mode;
		send(new byte[]{0, 5, which, type, mode});
	}
	
	public void getSensor(byte which)
	{
		send(new byte[]{0, 7, which});
	}
	
	public void resetSensorScaledValue(byte which)
	{
		send(new byte[]{0, 8, which});
	}
}
