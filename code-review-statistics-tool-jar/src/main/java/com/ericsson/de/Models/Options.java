package com.ericsson.de.Models;

public class Options
{
	private String chrctr;
	private String full;
	private String capitalFull;
	private String number;
	
	private int value;

	public Options(String chrctr, String full, String capitalFull, String number, int value) 
	{
		super();
		this.chrctr = chrctr;
		this.full = full;
		this.capitalFull = capitalFull;
		this.number = number;
		this.value = value;
	}

	public String getChrctr() 
	{
		return chrctr;
	}

	public void setChrctr(String chrctr) 
	{
		this.chrctr = chrctr;
	}

	public String getFull() 
	{
		return full;
	}

	public void setFull(String full) 
	{
		this.full = full;
	}

	public String getCapitalFull()
	{
		return capitalFull;
	}

	public void setCapitalFull(String capitalFull)
	{
		this.capitalFull = capitalFull;
	}

	public String getNumber()
	{
		return number;
	}

	public void setNumber(String number) 
	{
		this.number = number;
	}

	public int getValue()
	{
		return value;
	}

	public void setValue(int value)
	{
		this.value = value;
	}
	
	
}
