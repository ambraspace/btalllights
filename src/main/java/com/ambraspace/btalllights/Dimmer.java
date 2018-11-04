package com.ambraspace.btalllights;


public class Dimmer extends Switch
{
	
	public static final int INTENSITY_UNKNOWN=-1;
	
	private int intensity=INTENSITY_UNKNOWN;

	public Dimmer(String name, int iface, int a, int pl)
	{
		super(name, iface, a, pl);
	}

	
	public int getIntensity()
	{
		return intensity;
	}

	
	public void setIntensity(int intensity)
	{
		if (intensity==INTENSITY_UNKNOWN || (intensity>=0 && intensity<=10))
		{
			this.intensity = intensity;
		} else
		{
			throw new RuntimeException("Intensity not allowed!");
		}
		
	}


	@Override
	public String whatCommand(int what)
	{
		if (what!=0 && what!=1 && what!=30 && what!=31)
		{
			throw new RuntimeException("What value not allowed!");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("*1*" + what + "*");
		if ((getA()>=0 && getA()<=9) && (getPl()>=0 && getPl()<=9))
		{
			sb.append("" + getA() + getPl());
		} else 
		{
			sb.append(String.format("%02d%02d", getA(), getPl()));
		}
		sb.append("#4#" + String.format("%02d", getIface()) + "##");
		return sb.toString();
	}
	
	
	
	
}
