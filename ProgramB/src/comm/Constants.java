package comm;

public class Constants
{
	public static final byte MOTOR_A = 0;
	public static final byte MOTOR_B = 1;
	public static final byte MOTOR_C = 2;
	public static final byte MOTOR_ALL = (byte)0xFF;

	public static final byte SENSOR_1 = 0;
	public static final byte SENSOR_2 = 1;
	public static final byte SENSOR_3 = 2;
	public static final byte SENSOR_4 = 3;
	
	public static final byte SENSOR_TOUCH = 1;
	public static final byte SENSOR_LIGHT_ACTIVE = 5;
	public static final byte SENSOR_LIGHT_INACTIVE = 6;
	public static final byte SENSOR_SOUND_DB = 7;
	public static final byte SENSOR_SOUND_DBA = 8;
	public static final byte SENSOR_LOWSPEED_9V = 0xB;
	
	public static final byte SMODE_RAW = 0;
	public static final byte SMODE_BOOL = 0x20;
	public static final byte SMODE_SWITCH = 0x40;
	public static final byte SMODE_PERIODIC = 0x60;
	public static final byte SMODE_PERCENT = (byte)0x80;
	
	public static final byte CMD_GETINPUTVALUES = 0x07;
	public static final byte CMD_LSREAD = 0x10;
	public static final byte CMD_LSGETSTATUS = 0x0E;
	
	private Constants(){}
}
