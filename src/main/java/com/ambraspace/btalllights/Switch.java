package com.ambraspace.btalllights;

public class Switch implements Comparable<Switch>
{
	
	public static final int STATUS_UNKNOWN=-1;
	public static final int STATUS_OFF=0;
	public static final int STATUS_ON=1;
	
	private String name;
	private int a;
	private int pl;
	private int iface;
	private int status=STATUS_UNKNOWN;
	
	
	public Switch(String name, int iface, int a, int pl)
	{
		setName(name);
		setIface(iface);
		setA(a);
		setPl(pl);
	}

	
	public String getName()
	{
		return name;
	}
	
	
	private void setName(String name)
	{
		if (name==null)
		{
			throw new RuntimeException("Null name supplied!");
		}
		String finalName = name.trim();
		if (finalName.equals(""))
		{
			throw new RuntimeException("Empty name supplied!");
		}
		this.name = name;
	}

	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Switch))
		{
			return false;
		}
		Switch other = (Switch)obj;
		if (this.iface==other.iface && this.a==other.a && this.pl==other.pl)
		{
			return true;
		}
		return false;
	}


	@Override
	public int compareTo(Switch o)
	{
		if (this.iface!=o.iface)
		{
			return this.iface-o.iface;
		}
		else
		{
			if (this.a!=o.a)
			{
				return this.a-o.a;
			}
			else
			{
				return this.pl-o.pl;
			}
		}
	}


	
	public int getA()
	{
		return a;
	}


	
	private void setA(int a)
	{
		if (a<0 || a>10)
		{
			throw new RuntimeException("Invalid value for A.");
		}
		this.a = a;
	}


	
	public int getPl()
	{
		return pl;
	}


	
	private void setPl(int pl)
	{
		if (pl<0 || pl>15)
		{
			throw new RuntimeException("Invalid value for PL.");
		}
		this.pl = pl;
	}


	
	public int getIface()
	{
		return iface;
	}


	
	private void setIface(int iface)
	{
		if (iface<1 || iface>15)
		{
			throw new RuntimeException("Invalid value for Interface.");
		}
		this.iface = iface;
	}
	
	
	public int getStatus()
	{
		return status;
	}


	
	public void setStatus(int status)
	{
		if (status==STATUS_UNKNOWN || status == STATUS_OFF || status == STATUS_ON)
		{
			this.status = status;
		} else {
			throw new RuntimeException("Status not allowed!");
		}
	}


	@Override
	public String toString()
	{
		return String.format("(IF=%d, A=%d, PL=%d) %s", this.iface, this.a, this.pl, this.name);
	}
	
	
	public String statusRequest()
	{
		StringBuilder retVal = new StringBuilder();
		retVal.append("*#1*");
		if ((a>=0 && a<=9) && (pl>=1 && pl<=9))
		{
			retVal.append(""+a+pl);
		} else
		{
			retVal.append(String.format("%02d%02d", a, pl));
		}
		retVal.append("#4#");
		retVal.append(String.format("%02d", iface));
		retVal.append("##");
		return retVal.toString();
	}
	
	
}
