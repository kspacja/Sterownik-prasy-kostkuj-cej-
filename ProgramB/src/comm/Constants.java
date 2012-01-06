package comm;

/**
 * Klasa zawiera stałe wartości używane w komendach dla robota
 */
public class Constants
{
	public static final byte MOTOR_A = 0;
	public static final byte MOTOR_B = 1;
	public static final byte MOTOR_C = 2;
	public static final byte MOTOR_ALL = (byte)0xFF;
	
	public static final byte MOTOR_MODE_ON = 0x01;
	public static final byte MOTOR_MODE_BRAKE = 0x02;
	public static final byte MOTOR_MODE_REG = 0x04;
	
	public static final byte MOTOR_REG_IDLE = 0;
	public static final byte MOTOR_REG_SPEED = 0x01;
	public static final byte MOTOR_REG_SYNC = 0x02;
	
	public static final byte MOTOR_RUNSTATE_IDLE = 0;
	public static final byte MOTOR_RUNSTATE_RAMPUP = 0x10;
	public static final byte MOTOR_RUNSTATE_RUNNING = 0x20;
	public static final byte MOTOR_RUNSTATE_RAMPDOWN = 0x40;
	
	public static final byte SENSOR_TOUCH = 1;
	public static final byte SENSOR_LIGHT_ACTIVE = 5;
	public static final byte SENSOR_LIGHT_INACTIVE = 6;
	public static final byte SENSOR_SOUND_DB = 7;
	public static final byte SENSOR_SOUND_DBA = 8;
	public static final byte SENSOR_LOWSPEED_9V = 0xB;
	public static final byte SENSOR_COLOR_FULL = 0xD;
	public static final byte SENSOR_COLOR_RED = 0xE;
	public static final byte SENSOR_COLOR_GREEN = 0xF;
	public static final byte SENSOR_COLOR_BLUE = 0x10;
	public static final byte SENSOR_COLOR_NONE = 0x11;
	
	public static final byte SMODE_RAW = 0;
	public static final byte SMODE_BOOL = 0x20;
	public static final byte SMODE_PULSE = 0x40;
	public static final byte SMODE_EDGE = 0x60;
	public static final byte SMODE_PERCENT = (byte)0x80;
	
	public static final byte CMD_GETINPUTVALUES = 0x07;
	public static final byte CMD_GETOUTPUTSTATE = 0x06;
	public static final byte CMD_LSREAD = 0x10;
	public static final byte CMD_LSGETSTATUS = 0x0E;
	
	public static final byte COLOR_BLACK = 1;
	public static final byte COLOR_BLUE = 2;
	public static final byte COLOR_GREEN = 3;
	public static final byte COLOR_YELLOW = 4;
	public static final byte COLOR_RED = 5;
	public static final byte COLOR_WHITE = 6;
	
	private Constants(){}
}
