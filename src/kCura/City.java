package kCura;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class City {
	private static final String CITY_STATE_DIVIDER = ", ";
	
	private int fPopulation;
	private String fCityName;
	private String fStateName;
	private Set<Interstate> fInterstates;
	
	public City(int population, String cityName, String stateName) {
		if (cityName == null) {
			throw new NullPointerException("cityName");
		}
		if (stateName == null) {
			throw new NullPointerException("stateName");
		}
		fPopulation = population;
		fCityName = cityName;
		fStateName = stateName;
		fInterstates = new TreeSet<Interstate>(new Comparator<Interstate>() {
			@Override
			public int compare(Interstate paramT1, Interstate paramT2) {
				// Sort by smallest interstate first
				return paramT1.getNumber() - paramT2.getNumber();
			}
		});
	}
	
	@Override
	public int hashCode() {
		return (fCityName + CITY_STATE_DIVIDER + fStateName).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof City) {
			return fCityName.equalsIgnoreCase(((City)obj).fCityName) && fStateName.equalsIgnoreCase(((City)obj).fStateName);
		} else if (obj instanceof String) {
			return toString().equals(obj);
		}
		return super.equals(obj);
	}
	
	public int getPopulation() {
		return fPopulation;
	}
	
	public boolean addInterstate(Interstate interstate) {
		if (fInterstates.add(interstate)) {
			return interstate.addCity(this);
		}
		return false;
	}
	
	public Collection<Interstate> getInterstates() {
		return fInterstates;
	}
	
	public String getCityName() {
		return fCityName;
	}
	
	public String getStateName() {
		return fStateName;
	}
	
	@Override
	public String toString() {
		return fCityName + CITY_STATE_DIVIDER + fStateName;
	}
}
