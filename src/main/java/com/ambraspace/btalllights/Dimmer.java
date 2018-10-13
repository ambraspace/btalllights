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
	
}
