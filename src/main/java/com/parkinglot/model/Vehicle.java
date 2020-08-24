package com.parkinglot.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author Balasaheb
 *
 */
public abstract class Vehicle implements Externalizable
{
	private String	registrationNo	= null;
	private String	color			= null;	
	private LocalDateTime parkingtime = LocalDateTime.now(); 
	
	public Vehicle(String registrationNo, String color)
	{
		this.registrationNo = registrationNo;
		this.color = color;
		this.parkingtime = LocalDateTime.now();
	}
	
	@Override
	public String toString()
	{
		return "[registrationNo=" + registrationNo + ", color=" + color + " , parkingtime=" + parkingtime + "]";
	}
	
	/**
	 * @return the registrationNo
	 */
	public String getRegistrationNo()
	{
		return registrationNo;
	}
			
	public LocalDateTime getParkingtime() {
		return parkingtime;
	}

	/**
	 * @param registrationNo
	 *            the registrationNo to set
	 */
	public void setRegistrationNo(String registrationNo)
	{
		this.registrationNo = registrationNo;
	}
	
	/**
	 * @return the color
	 */
	public String getColor()
	{
		return color;
	}
	
	/**
	 * @param color
	 *            the color to set
	 */
	public void setColor(String color)
	{
		this.color = color;
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(getRegistrationNo());
		out.writeObject(getColor());
		out.writeObject(parkingtime);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		setRegistrationNo((String) in.readObject());
		setColor((String) in.readObject());
	}
}
