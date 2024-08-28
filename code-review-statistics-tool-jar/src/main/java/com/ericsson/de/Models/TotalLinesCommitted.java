package com.ericsson.de.Models;

public class TotalLinesCommitted
{
	private String name;
	private int lines;
	
	public TotalLinesCommitted(String name, int lines) 
	{
		super();
		this.name = name;
		this.lines = lines;
	}

	public String getName() 
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getLines()
	{
		return lines;
	}

	public void setLines(int lines)
	{
		this.lines = lines;
	}
}
