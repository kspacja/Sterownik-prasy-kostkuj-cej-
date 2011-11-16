package comm;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

public class RobotMaster implements Runnable
{
	private Thread robotTalker; // Wątek, który komunikuje się z robotem
	private SocketConnection ss;
	private BluetoothConnection bt;
	
	private LinkedList<byte[]> toRobot = new LinkedList<byte[]>();
	// Informuje, że wiadomość na tej samej pozycji w toRobot MUSI otrzymać odpowiedź
	private LinkedList<Boolean> expectAns = new LinkedList<Boolean>();
	
	// Cztery sensory, pierwszy bajt drugiej tablicy to typ, a drugi to tryb
	private byte[][] sensorData = new byte[4][2];
	
	public RobotMaster(SocketConnection socket, BluetoothConnection bluetooth)
	{
		ss = socket;
		bt = bluetooth;
		robotTalker = new Thread(this);
		robotTalker.start();
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			// Czekaj na żądanie z socketu
			try
			{
				synchronized(ss)
				{
					ss.wait();
				}
			}
			catch(InterruptedException e)
			{
				return; // Kończę wątek
			}
			
			// Parsuj otrzymaną wiadomość
			while(ss.isAvailable())
			{
				try
				{
					try
					{
						parseCommand(ss.receiveString());
						//ss.reply("OK Command");
					}
					catch(ParserException e)
					{
						// Wyślij zwrotną informację, że komenda była niezrozumiała
						ss.reply("Command Error: " + e.getMessage());
						e.printStackTrace();
					}
					
				}
				catch(IOException e)
				{
					// W tym momencie niewiele da się zrobić
					System.err.println("!? Failed to send an UDP reply packet");
					e.printStackTrace();
				}
				
				// Teraz w kolejce do robota są komendy
				while(!toRobot.isEmpty())
				{
					try
					{
						bt.send(toRobot.removeFirst());
						
						Thread.sleep(200);

						byte[] ans = bt.receive();
						boolean mustAns = expectAns.removeFirst();
						

						try
						{
							if(ans != null)
								if(ans[2] == 0)
								{
									if(mustAns)
										ss.reply(parseReply(ans));
//									else
//										ss.reply("OK Robot");
								}
								else
									ss.reply("Robot Error: " + ans[2]);
							else if(mustAns)
								ss.reply("Robot does not respond");
						}
						catch(IOException e)
						{
							// W tym momencie niewiele da się zrobić
							System.err.println("!? Failed to send an UDP reply packet");
							e.printStackTrace();
						}
					}
					catch(IOException e)
					{
						System.err.println("!! Failed to communicate with the robot");
						e.printStackTrace();
					}
					catch(InterruptedException e)
					{
						return; // Kończę wątek
					}
				}
			}
		}
	}
	
	private void send(byte[] msg, boolean ans)
	{
		toRobot.add(msg);
		expectAns.add(ans);
	}
	
	private void motor(byte which, byte power, byte mode, byte reg, byte tratio,
		byte rstate, short tachoLimit)
	{
		ByteBuffer bb = ByteBuffer.wrap(new byte[13]);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.put((byte)0);
		bb.put((byte)4);
		bb.put(which);
		bb.put(power);
		bb.put(mode);
		bb.put(reg);
		bb.put(tratio);
		bb.put(rstate);
		bb.putShort(tachoLimit);
		
		send(bb.array(), false);
	}
	
	private void setSensor(byte which, byte type, byte mode)
	{
		sensorData[which][0] = type;
		sensorData[which][1] = mode;
		send(new byte[]{0, 5, which, type, mode}, false);
	}
	
	private void getSensor(byte which)
	{
		send(new byte[]{0, 7, which}, true);
	}
	
	private void resetMotor(byte which, boolean abs)
	{
		send(new byte[]{0, 0x0A, which, (byte)(abs ? 1 : 0)}, false);
	}
	
	private void resetSensorScaledValue(byte which)
	{
		send(new byte[]{0, 8, which}, false);
	}
	
	private void lsread(byte which)
	{
		send(new byte[]{0, 0x10, which}, true);
	}
	
	private void lswrite(byte which, byte[] towrite, byte expected)
	{
		byte[] res = new byte[9 + towrite.length];
		res[1] = 0xF;
		res[2] = which;
		res[3] = (byte)towrite.length;
		res[4] = expected;
		
		for(int i=0; i<towrite.length; ++i)
			res[5 + i] = towrite[i];
		// Reszta sama się wypełni zerami
		
		send(res, false);
	}
	
	private void parseCommand(String request) throws ParserException
	{
		String[] args = request.split(" ");

		// Najpierw spradzamy, co to za polecenie
		if(args[0].equalsIgnoreCase("motor"))
		{
			ByteBuffer bb = ByteBuffer.wrap(new byte[11]);
			bb.order(ByteOrder.LITTLE_ENDIAN);

			// Który silnik
			byte abc;
			if(args[1].equalsIgnoreCase("A"))
				abc = Constants.MOTOR_A;
			else if(args[1].equalsIgnoreCase("B"))
				abc = Constants.MOTOR_B;
			else if(args[1].equalsIgnoreCase("C"))
				abc = Constants.MOTOR_C;
			else if(args[1].equalsIgnoreCase("ALL"))
				abc = Constants.MOTOR_ALL;
			else
				throw new ParserException("Unknown motor label (should be 'A', 'B', 'C' or 'ALL')");

			// Tworzę zmienne dla wywołania setoutputstate
			byte pow = 0;
			byte powmod = 0; // 1 lub -1 lub 0 - zależne od direction
			short tacholimit = 0;

			for(int i=2; i<args.length; ++i)
			{
				String[] arg = args[i].split("=");

				try
				{
					if(arg[0].equalsIgnoreCase("direction"))
					{
						if(arg[1].equalsIgnoreCase("forward"))
							powmod = 1;
						else if(arg[1].equalsIgnoreCase("backwards"))
							powmod = -1;
						else if(arg[1].equalsIgnoreCase("stop"))
							powmod = 0;
						else
							throw new ParserException("Unknown direction" + arg[1] +
								", should be 'forward', 'backwards', 'stop'");
					}
					else if(arg[0].equalsIgnoreCase("power"))
					{
						pow = Byte.parseByte(arg[1]);
						if(pow < 0 || pow > 100)
							throw new ParserException("Power must be in range [0,100]");
					}
					else if(arg[0].equalsIgnoreCase("degrees"))
						// Tu parsuję integery, ponieważ java nie ma typów unsigned
						tacholimit = (short)Integer.parseInt(arg[1]);
					else if(arg[0].equalsIgnoreCase("rotations"))
						tacholimit = (short)(360 * Integer.parseInt(arg[1]));
					else if(arg[0].equalsIgnoreCase("unlimited"))
						tacholimit = 0;
					else
						throw new ParserException("Unkown motor argument " + arg[0] +
							", should be 'direction', 'forward', 'backwards', 'stop'" +
							", 'power', 'degrees', 'rotations', 'unlimited'");
				}
				catch(NumberFormatException e)
				{
					throw new ParserException("Expected numeric argument for" + arg[0], e);
				}
			}

			motor(abc, (byte)(pow*powmod), (byte)3, (byte)0, (byte)0,
				(byte)0x20, tacholimit);
			// 3 - MOTORON + BRAKE
			//TODO 0x20 - Teraz tylko running, potem dodać inne tryby
		}
		else if(args[0].equalsIgnoreCase("get-sensor"))
		{
			try
			{
				byte sen = (byte)(Byte.parseByte(args[1]) - 1);
				if(sen < 0 || sen > 3)
					throw new ParserException("Sensor port must be in range [1-4]");
				
				// Jeśli sensor jest w tym trybie, to odczytuję za pomocą lsread
				// Na razie (i być może w ogóle) obsługuję tylko jeden sensor, więc...
				if(sensorData[sen][0] == Constants.SENSOR_LOWSPEED_9V)
				{
					//TODO Czy trzeba robić jakiś timeout?
					lswrite(sen, new byte[]{0x2, 0x42}, (byte)1);
//					Thread.sleep(200);
					lsread(sen);
				}
				else
					getSensor(sen);
			}
			catch(NumberFormatException e)
			{
				throw new ParserException("Expected numeric argument as sensor port", e);
			}
		}
		else if(args[0].equalsIgnoreCase("reset-sensor-scaled"))
		{
			try
			{
				byte sen = (byte)(Byte.parseByte(args[1]) - 1);
				if(sen < 0 || sen > 3)
					throw new ParserException("Sensor port must be in range [1-4]");
				
				resetSensorScaledValue(sen);
			}
			catch(NumberFormatException e)
			{
				throw new ParserException("Expected numeric argument as sensor port", e);
			}
		}
		else if(args[0].equalsIgnoreCase("set-sensor"))
		{
			try
			{
				byte sen = (byte)(Byte.parseByte(args[1]) - 1);
				if(sen < 0 || sen > 3)
					throw new ParserException("Sensor port must be in range [1-4]");
				
				byte type = 0;
				byte mode = 0;
				
				for(int i=2; i<args.length; ++i)
				{
					String[] arg = args[i].split("=");
					
					if(arg[0].equalsIgnoreCase("type"))
					{
						if(arg[1].equalsIgnoreCase("touch"))
						{
							type = Constants.SENSOR_TOUCH;
							mode = Constants.SMODE_BOOL;
						}
						else if(arg[1].equalsIgnoreCase("sound_db"))
						{
							type = Constants.SENSOR_SOUND_DB;
							mode = Constants.SMODE_PERCENT;
						}
						else if(arg[1].equalsIgnoreCase("sound_dba") ||
								arg[1].equalsIgnoreCase("sound"))
						{
							type = Constants.SENSOR_SOUND_DBA;
							mode = Constants.SMODE_PERCENT;
						}
						else if(arg[1].equalsIgnoreCase("light_reflected"))
						{
							type = Constants.SENSOR_LIGHT_ACTIVE;
							mode = Constants.SMODE_PERCENT;
						}
						else if(arg[1].equalsIgnoreCase("light_ambient"))
						{
							type = Constants.SENSOR_LIGHT_INACTIVE;
							mode = Constants.SMODE_PERCENT;
						}
						else if(arg[1].equalsIgnoreCase("color"))
						{
							// Tu ignoruję inne opcje niż typ
							// dla sensora koloru nie mają one sensu
							type = Constants.SENSOR_COLOR_FULL;
							mode = Constants.SMODE_RAW;
							break;
						}
						else if(arg[1].equalsIgnoreCase("color_as_light_red"))
						{
							type = Constants.SENSOR_COLOR_RED;
							mode = Constants.SMODE_PERCENT;
						}
						else if(arg[1].equalsIgnoreCase("color_as_light_green"))
						{
							type = Constants.SENSOR_COLOR_GREEN;
							mode = Constants.SMODE_PERCENT;
						}
						else if(arg[1].equalsIgnoreCase("color_as_light_blue"))
						{
							type = Constants.SENSOR_COLOR_BLUE;
							mode = Constants.SMODE_PERCENT;
						}
						else if(arg[1].equalsIgnoreCase("color_as_light_ambient"))
						{
							type = Constants.SENSOR_COLOR_NONE;
							mode = Constants.SMODE_PERCENT;
						}
						else if(arg[1].equalsIgnoreCase("ultrasonic"))
						{
							// Teraz trzeba się zachować całkowicie inaczej od reszty
							// Nie uwzględniam reszty argumentów
							type = Constants.SENSOR_LOWSPEED_9V;
							mode = Constants.SMODE_RAW;
							break;
						}
						else
							throw new ParserException("Unknown sensor type " + arg[1] +
								", should be 'touch'");
					}
					else if(arg[0].equalsIgnoreCase("mode"))
					{
						if(arg[1].equalsIgnoreCase("raw"))
							mode = Constants.SMODE_RAW;
						else if(arg[1].equalsIgnoreCase("bool"))
							mode = Constants.SMODE_BOOL;
						else if(arg[1].equalsIgnoreCase("switch"))
							mode = Constants.SMODE_SWITCH;
						else if(arg[1].equalsIgnoreCase("periodic"))
							mode = Constants.SMODE_PERIODIC;
						else if(arg[1].equalsIgnoreCase("percent"))
							mode = Constants.SMODE_PERCENT;
						else
							throw new ParserException("Unknown mode " + arg[1] +
								", should be 'raw', 'bool', 'switch', 'periodic', 'percent'");
					}
					else
						throw new ParserException("Unknown argument for set-sensor " + arg[0] +
							"should be 'type', 'mode'");
				}
				
				setSensor(sen, type, mode);
			}
			catch(NumberFormatException e)
			{
				throw new ParserException("Expected numeric argument as sensor port", e);
			}
		}
		else if(args[0].equalsIgnoreCase("reset-motor-position"))
		{
			byte abc;
			if(args[1].equalsIgnoreCase("A"))
				abc = Constants.MOTOR_A;
			else if(args[1].equalsIgnoreCase("B"))
				abc = Constants.MOTOR_B;
			else if(args[1].equalsIgnoreCase("C"))
				abc = Constants.MOTOR_C;
			else if(args[1].equalsIgnoreCase("ALL"))
				abc = Constants.MOTOR_ALL;
			else
				throw new ParserException("Unknown motor label (should be 'A', 'B', 'C' or 'ALL')");
			
			resetMotor(abc, args.length >= 2 && args[2].equalsIgnoreCase("absolute"));
		}
		else
			throw new ParserException("Unknown command: "+args[0]);
	}
	
	private String parseReply(byte[] reply)
	{
		String res = null;
		
		// W tym momencie zakładam, że pakiet jest ok
		
		// Switch na bajt [1] - typ pakietu
		switch(reply[1])
		{
			case Constants.CMD_GETINPUTVALUES:
				if(reply[4] == 0) // bajt valid
					res = "Error: Sensor readings are invalid";
				else
					// switch na tryb sensora
					// od tego zależy, czy zwraca normalized czy scaled value
					switch(reply[7])
					{
						case Constants.SMODE_RAW:
							if(reply[6] == Constants.SENSOR_COLOR_FULL)
								switch(reply[12])
								{
									case Constants.COLOR_RED:
										res = "RED";
										break;
									case Constants.COLOR_BLACK:
										res = "BLACK";
										break;
									case Constants.COLOR_BLUE:
										res = "BLUE";
										break;
									case Constants.COLOR_GREEN:
										res = "GREEN";
										break;
									case Constants.COLOR_WHITE:
										res = "WHITE";
										break;
									case Constants.COLOR_YELLOW:
										res = "YELLOW";
										break;
								}
							else
								//FIXME Czy to powinien być long? Bo java nie chce dać unsigned
								res = Integer.toString((reply[11] << 8) + reply[10]);
							break;

						case Constants.SMODE_BOOL:
						case Constants.SMODE_SWITCH:
						case Constants.SMODE_PERIODIC:
						case Constants.SMODE_PERCENT:
							res = Integer.toString((reply[13] << 8) + reply[12]);
					}
				break;
			
			case Constants.CMD_LSREAD:
				if(reply[3] > 0) // bytes read
					//TODO Na razie pod sensor, dać możliwość rozszerzenia
					res = Byte.toString(reply[4]);
				else
					res = "No bytes read";
				break;
				
			case Constants.CMD_LSGETSTATUS:
				res = Byte.toString(reply[3]);
				break;
		}
		
		return res.toString();
	}
	
	//TODO To jest tylko procedura testowa, prawdziwy main będzie inny!
	public static void main(String[] args) throws SocketException, IOException
	{
		RobotMaster master = new RobotMaster(new SocketConnection(6666), new BluetoothConnection(
//				"btspp://0016530BD2F6:1;authenticate=false;encrypt=false;master=false"));
				"btspp://0016530D3A52:1;authenticate=false;encrypt=false;master=false"));
	}
}
