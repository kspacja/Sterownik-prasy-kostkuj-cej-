package comm;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Parser
{
	private static class Constants
	{
		public static final byte MOTOR_A = 0;
		public static final byte MOTOR_B = 1;
		public static final byte MOTOR_C = 2;
		public static final byte MOTOR_ALL = (byte)0xFF;

		public static final byte SENSOR_TOUCH = 1;
		
		public static final byte SMODE_RAW = 0;
		public static final byte SMODE_BOOL = 0x20;
		public static final byte SMODE_SWITCH = 0x40;
		public static final byte SMODE_PERIODIC = 0x60;
		public static final byte SMODE_PERCENT = (byte)0x80;
	}

	// Zwraca tablicę bajtów, które będzie mogła być wysłana do robota
	public static byte[] parse(String request) throws ParserException
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
			int tacholimit = 0;

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
								", should be 'forward', 'backward', 'stop'");
					}
					else if(arg[0].equalsIgnoreCase("power"))
					{
						pow = Byte.parseByte(arg[1]);
						if(pow < 0 || pow > 100)
							throw new ParserException("Power must be in range [0,100]");
					}
					else if(arg[0].equalsIgnoreCase("degrees"))
						tacholimit = Integer.parseInt(arg[1]);
					else if(arg[0].equalsIgnoreCase("rotations"))
						tacholimit = 360 * Integer.parseInt(arg[1]);
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

			bb.put((byte)0);
			bb.put((byte)4);
			bb.put(abc);
			bb.put((byte)(pow * powmod));
			bb.put((byte)3); // MOTORON + BRAKE
			bb.put((byte)0);
			bb.put((byte)0);
			bb.put((byte)0x20); //TODO Teraz tylko running, potem dodać inne tryby
			bb.putInt(tacholimit);

			return bb.array();
		}
		else if(args[0].equalsIgnoreCase("get-sensor"))
		{
			try
			{
				byte sen = Byte.parseByte(args[1]);
				if(sen < 0 || sen > 3)
					throw new ParserException("Sensor port must be in range [0-3]");
				return new byte[] {0, 7, sen};
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
				byte sen = Byte.parseByte(args[1]);
				if(sen < 0 || sen > 3)
					throw new ParserException("Sensor port must be in range [0-3]");
				return new byte[] {0, 8, sen};
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
				byte sen = Byte.parseByte(args[1]);
				if(sen < 0 || sen > 3)
					throw new ParserException("Sensor port must be in range [0-3]");
				
				byte type = 0;
				byte mode = 0;
				
				for(int i=2; i<args.length; ++i)
				{
					String[] arg = args[i].split("=");
					
					if(arg[0].equalsIgnoreCase("type"))
					{
						if(arg[1].equalsIgnoreCase("touch"))
							type = Constants.SENSOR_TOUCH;
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
						throw new ParserException("Unknown argument for get-sensor " + arg[0] +
							"should be 'type', 'mode'");
				}
				
				return new byte[] {0, 5, sen, type, mode};
			}
			catch(NumberFormatException e)
			{
				throw new ParserException("Expected numeric argument as sensor port", e);
			}
		}
		else
			throw new ParserException("Unknown command: "+args[0]);
	}
}