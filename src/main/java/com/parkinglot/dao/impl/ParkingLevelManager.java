package com.parkinglot.dao.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.parkinglot.constants.Constants;
import com.parkinglot.dao.ParkingLevelDataManager;
import com.parkinglot.model.Vehicle;
import com.parkinglot.model.stratergy.NearestFirstParkingStrategy;
import com.parkinglot.model.stratergy.ParkingStrategy;

/**
 * This class is a singleton class to manage the data of parking system
 * 
 * @author Balasaheb
 * @param <T>
 */
public class ParkingLevelManager<T extends Vehicle> implements ParkingLevelDataManager<T>
{
	// For Multilevel Parking lot - 0 -> Ground floor 1 -> First Floor etc
	private AtomicInteger	level			= new AtomicInteger(0);
	private AtomicInteger	capacity		= new AtomicInteger();
	private AtomicInteger	availability	= new AtomicInteger();
	// Allocation Strategy for parking
	private ParkingStrategy parkingStrategy;
	// this is per level - slot - vehicle
	private Map<Integer, Optional<T>> slotVehicleMap;
	
	@SuppressWarnings("rawtypes")
	private static ParkingLevelManager instance = null;
	
	@SuppressWarnings("unchecked")
	public static <T extends Vehicle> ParkingLevelManager<T> getInstance(int level, int capacity,
			ParkingStrategy parkingStrategy)
	{
		if (instance == null)
		{
			synchronized (ParkingLevelManager.class)
			{
				if (instance == null)
				{
					instance = new ParkingLevelManager<T>(level, capacity, parkingStrategy);
				}
			}
		}
		return instance;
	}
	
	private ParkingLevelManager(int level, int capacity, ParkingStrategy parkingStrategy)
	{
		this.level.set(level);
		this.capacity.set(capacity);
		this.availability.set(capacity);
		this.parkingStrategy = parkingStrategy;
		if (parkingStrategy == null)
			parkingStrategy = new NearestFirstParkingStrategy();
		slotVehicleMap = new ConcurrentHashMap<>();
		for (int i = 1; i <= capacity; i++)
		{
			slotVehicleMap.put(i, Optional.empty());
			parkingStrategy.add(i);
		}
	}
	
	@Override
	public int parkCar(T vehicle)
	{
		int availableSlot;
		if (availability.get() == 0)
		{
			return Constants.NOT_AVAILABLE;
		}
		else
		{
			availableSlot = parkingStrategy.getSlot();
			if (slotVehicleMap.containsValue(Optional.of(vehicle)))
				return Constants.VEHICLE_ALREADY_EXIST;
			
			slotVehicleMap.put(availableSlot, Optional.of(vehicle));
			availability.decrementAndGet();
			parkingStrategy.removeSlot(availableSlot);
		}
		return availableSlot;
	}
	
	@Override
	public boolean leaveCar(int slotNumber)
	{
		//System.out.println("slotVehicleMap===>"+slotVehicleMap.get(slotNumber).get().getParkingtime());
		//int hours  = slotVehicleMap.get(slotNumber).get().getParkingtime() - LocalDateTime.now();
		long minutesDiff = ChronoUnit.MINUTES.between(slotVehicleMap.get(slotNumber).get().getParkingtime(), LocalDateTime.now());
        long hoursDiff = Math.round(Math.ceil(minutesDiff/60.0));
        //System.out.println("hoursDiff====>"+hoursDiff);
		if (!slotVehicleMap.get(slotNumber).isPresent()) // Slot already empty
			return false;
		availability.incrementAndGet();
		parkingStrategy.add(slotNumber);
		slotVehicleMap.put(slotNumber, Optional.empty());
		return true;
	}
	
	@Override
	public List<String> getStatus()
	{
		List<String> statusList = new ArrayList<>();
		for (int i = 1; i <= capacity.get(); i++)
		{
			Optional<T> vehicle = slotVehicleMap.get(i);
			if (vehicle.isPresent())
			{
				statusList.add(i + "\t\t" + vehicle.get().getRegistrationNo() + "\t\t" + vehicle.get().getColor());
			}
		}
		return statusList;
	}
	
	public int getAvailableSlotsCount()
	{
		return availability.get();
	}
	
	@Override
	public List<String> getRegNumberForColor(String color)
	{
		List<String> statusList = new ArrayList<>();
		for (int i = 1; i <= capacity.get(); i++)
		{
			Optional<T> vehicle = slotVehicleMap.get(i);
			if (vehicle.isPresent() && color.equalsIgnoreCase(vehicle.get().getColor()))
			{
				statusList.add(vehicle.get().getRegistrationNo());
			}
		}
		return statusList;
	}
	
	@Override
	public List<Integer> getSlotNumbersFromColor(String colour)
	{
		List<Integer> slotList = new ArrayList<>();
		for (int i = 1; i <= capacity.get(); i++)
		{
			Optional<T> vehicle = slotVehicleMap.get(i);
			if (vehicle.isPresent() && colour.equalsIgnoreCase(vehicle.get().getColor()))
			{
				slotList.add(i);
			}
		}
		return slotList;
	}
	
	@Override
	public int getSlotNoFromRegistrationNo(String registrationNo)
	{
		int result = Constants.NOT_FOUND;
		for (int i = 1; i <= capacity.get(); i++)
		{
			Optional<T> vehicle = slotVehicleMap.get(i);
			if (vehicle.isPresent() && registrationNo.equalsIgnoreCase(vehicle.get().getRegistrationNo()))
			{
				result = i;
			}
		}
		return result;
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
	
	@Override
	public void doCleanUp()
	{
		this.level = new AtomicInteger();
		this.capacity = new AtomicInteger();
		this.availability = new AtomicInteger();
		this.parkingStrategy = null;
		slotVehicleMap = null;
		instance = null;
	}
}
