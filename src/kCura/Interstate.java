package kCura;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Interstate {
	private static final String PREFIX = "I-";
	
	private int fNumber;
	private Set<City> fCities;
	
	public Interstate(String name) {
		try {
			if (name == null) {
				throw new NullPointerException("name");
			}
			if (!name.startsWith(PREFIX)) {
				throw new InvalidParameterException("\"" + name + "\" does not start with the prefix \"" + PREFIX + "\"");
			}
			fNumber = Integer.parseInt(name.substring(PREFIX.length()));
		} catch (Throwable t) {
			throw new InvalidParameterException("\"" + name + "\" is not a valid interstate");
		}
		fCities = new HashSet<City>();
	}
	
	@Override
	public int hashCode() {
		return fNumber;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Interstate) {
			return ((Interstate)obj).fNumber == fNumber;
		} else if (obj instanceof String) {
			return toString().equals(obj);
		}
		return super.equals(obj);
	}
	
	public int getNumber() {
		return fNumber;
	}
	
	protected boolean addCity(City city) {
		return fCities.add(city);
	}
	
	public Collection<City> getCities() {
		return fCities;
	}
	
	@Override
	public String toString() {
		return PREFIX + fNumber;
	}
}
