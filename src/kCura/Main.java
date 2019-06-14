package kCura;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class Main {
	private static final String TYPE_DELIMITER = "\\|";
	private static final String INTERSTATE_DELIMITER = ";";
	private static final String SEARCH_CITY = "Chicago";
	private static final String SEARCH_STATE =  "Illinois";
	
	private static CitiesAndInterstates parseFile(String location) throws IOException, ParseException {
		CitiesAndInterstates resultSet = new CitiesAndInterstates();
		BufferedReader reader = new BufferedReader(new FileReader(location));
		try {
			String line = reader.readLine();
			int lineCount = 0;
			while (line != null) {
				String[] lineParts = line.split(TYPE_DELIMITER);
				if (lineParts.length != 4) {
					throw new ParseException("File at location \"" + location + "\" has a nonparsable line \"" + line + "\" due to lack of line information", lineCount);
				}
				
				// Parse Population
				int population;
				try {
					population = Integer.parseInt(lineParts[0]);
				} catch (Throwable t) {
					throw new ParseException("File at location \"" + location + "\" has a nonparsable line \"" + line + "\" due to non-parseable population \"" + lineParts[0] + "\"", lineCount);
				}
				
				// Parse City
				City city;
				try {
					city = new City(population, lineParts[1].trim(), lineParts[2].trim());
				} catch (Throwable t) {
					throw new ParseException("File at location \"" + location + "\" has a nonparsable line \"" + line + "\" due to non-parseable city \"" + lineParts[1] + "\",  \"" + lineParts[2] + "\"", lineCount);
				}
				if (resultSet.cities.put(city, city) != null) {
					throw new ParseException("File at location \"" + location + "\" has a nonparsable line \"" + line + "\" due to repeat city \"" + lineParts[1] + "\",  \"" + lineParts[2] + "\"", lineCount);
				}
				
				// Check for searched city
				if (city.getCityName().equalsIgnoreCase(SEARCH_CITY) && city.getStateName().equalsIgnoreCase(SEARCH_STATE)) {
					resultSet.searchCity = city;
				}				
				
				// Parse Interstates
				for (String interstateName : lineParts[3].split(INTERSTATE_DELIMITER)) {
					Interstate interstate;
					try {
						interstate = new Interstate(interstateName.trim());
						if (!resultSet.interstates.containsKey(interstate)) {
							resultSet.interstates.put(interstate, interstate);
						} else {
							interstate = resultSet.interstates.get(interstate);
						}
					} catch (Throwable t) {
						throw new ParseException("File at location \"" + location + "\" has a nonparsable line \"" + line + "\" due to non-parseable interstate \"" + interstateName + "\"", lineCount);
					}
					if (!city.addInterstate(interstate)) {
						throw new ParseException("File at location \"" + location + "\" has a nonparsable line \"" + line + "\" due to repeat interstate \"" + interstateName + "\" for city \"" + lineParts[1] + "\",  \"" + lineParts[2] + "\"", lineCount);
					}
				}				
				
				line = reader.readLine();
				lineCount++;
			}
		} finally {
			reader.close();
		}
		
		return resultSet;
	}
	
	public static void writeCitiesByPopulation(Collection<City> cities, BufferedWriter writer) throws IOException {
		Integer lastPopulation = null;
		
		for (City city : cities) {
			// Write population if new
			if (lastPopulation != (Integer)city.getPopulation()) {
				writer.write(Integer.toString(city.getPopulation()));
				writer.newLine();
				writer.newLine();
				lastPopulation = city.getPopulation();
			}
			
			// Write city
			writer.write(city.getCityName());
			writer.write(", ");
			writer.write(city.getStateName());
			writer.newLine();
			
			// Write interstates
			writer.write("Interstates: ");
			Iterator<Interstate> interstates = city.getInterstates().iterator();
			while (interstates.hasNext()) {
				Interstate next = interstates.next();
				writer.write(next.toString());
				if (interstates.hasNext()) {
					writer.write(", ");
				}
			}
			writer.newLine();
			writer.newLine();
		}
	}
	
	public static void writeInterstatesByCity(Collection<Interstate> interstates, BufferedWriter writer) throws IOException {
		for (Interstate interstate : interstates) {
			writer.write(interstate.toString());
			writer.write(" ");
			writer.write(Integer.toString(interstate.getCities().size()));
			writer.newLine();
		}
	}
	
	public static void writeDegreesFromCity(Collection<City> cities, City searchCity, BufferedWriter writer) throws IOException {
		Set<Degree> degrees = new TreeSet<Degree>(new Comparator<Degree>() {

			@Override
			public int compare(Degree paramT1, Degree paramT2) {
				if (paramT1.degree != paramT2.degree) {
					return paramT2.degree - paramT1.degree;
				} else if (!paramT1.city.getCityName().equalsIgnoreCase(paramT2.city.getCityName())) {
					return paramT1.city.getCityName().compareToIgnoreCase(paramT2.city.getCityName());
				} else if (!paramT1.city.getStateName().equalsIgnoreCase(paramT2.city.getStateName())) {
					return paramT1.city.getStateName().compareToIgnoreCase(paramT2.city.getStateName());
				}
				return 0;
			}
			
		});
		HashSet<City> foundCities = new HashSet<City>();
		Map<Interstate, Integer> interstatesToTake = new LinkedHashMap<Interstate, Integer>();
		Set<Interstate> takenInterstates = new HashSet<Interstate>();
		Set<City> remainingCities = new HashSet<City>(cities);
		if (remainingCities.remove(searchCity)) {
			foundCities.add(searchCity);
			degrees.add(new Degree(searchCity, 0));
			
			for (Interstate interstate : searchCity.getInterstates()) {
				if (!interstatesToTake.containsKey(interstate)) {
					interstatesToTake.put(interstate, 1);
				}
			}
			
			// Find degrees
			while (!interstatesToTake.isEmpty() && !remainingCities.isEmpty()) {
				Entry<Interstate, Integer> interstate = interstatesToTake.entrySet().iterator().next();
				interstatesToTake.remove(interstate.getKey());
				takenInterstates.add(interstate.getKey());
				
				for (City city : interstate.getKey().getCities()) {
					// Add city
					if (!foundCities.contains(city)) {
						remainingCities.remove(city);
						foundCities.add(city);
						degrees.add(new Degree(city, interstate.getValue()));
						
						// Add next set of interstates
						for (Interstate nextInterstate : city.getInterstates()) {
							if (!takenInterstates.contains(nextInterstate) && !interstatesToTake.containsKey(nextInterstate)) {
								interstatesToTake.put(nextInterstate, interstate.getValue() + 1);
							}
						}
					}
				}
			}
			
			// Write degrees
			for (Degree degree : degrees) {
				writer.write(Integer.toString(degree.degree));
				writer.write(" ");
				writer.write(degree.city.getCityName());
				writer.write(", ");
				writer.write(degree.city.getStateName());
				writer.newLine();
			}
			
			// Write not found cities
			for (City city : remainingCities) {
				writer.write("-1 ");
				writer.write(city.getCityName());
				writer.write(", ");
				writer.write(city.getStateName());
				writer.newLine();
			}
		}
	}
	
	public static void main(String args[]) throws IOException, ParseException {
		if (args.length >= 1) {
			CitiesAndInterstates set = parseFile(args[0]);
			
			// Write Cities By Population
			BufferedWriter writer = new BufferedWriter(new PrintWriter("Cities_By_Population.txt", "UTF-8"));
			try {
				writeCitiesByPopulation(set.cities.values(), writer);
			} finally {
				writer.close();
			}
			
			// Write Interstates by City
			writer = new BufferedWriter(new PrintWriter("Interstates_By_City.txt", "UTF-8"));
			try {
				writeInterstatesByCity(set.interstates.values(), writer);
			} finally {
				writer.close();
			}
			
			// Write Degrees From City
			if (set.searchCity != null) {
				writer = new BufferedWriter(new PrintWriter("Degrees_From_Chicago.txt.", "UTF-8"));
				try {
					writeDegreesFromCity(set.cities.values(), set.searchCity, writer);
				} finally {
					writer.close();
				}
			}
		} else {
			throw new IllegalArgumentException("Must contain a path argument");
		}
	}
	
	private static class Degree {
		public City city;
		public int degree;
		
		public Degree(City city, int degree) {
			this.city = city;
			this.degree = degree;
		}
		
		@Override
		public int hashCode() {
			return city.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Degree) {
				return city.equals(((Degree)o).city);
			}
			return super.equals(o);
		}
		
		@Override
		public String toString() {
			return degree + " " + city.toString();
		}
	}
	
	private static class CitiesAndInterstates {
		public City searchCity;
		public Map<City, City> cities;
		public Map<Interstate, Interstate> interstates;
		
		public CitiesAndInterstates() {
			cities = new TreeMap<City, City>(new Comparator<City>() {
				@Override
				public int compare(City paramT1, City paramT2) {
					// Sort by largest population first
					if (paramT2.getPopulation() != paramT1.getPopulation()) {
						return paramT2.getPopulation() - paramT1.getPopulation();
					} else if (!paramT1.getStateName().equalsIgnoreCase(paramT2.getStateName())) {
						return paramT1.getStateName().compareToIgnoreCase(paramT2.getStateName());
					} else if (!paramT1.getCityName().equalsIgnoreCase(paramT2.getCityName())) {
						return paramT1.getCityName().compareToIgnoreCase(paramT2.getCityName());
					}
					return 0;
				}
			});
			interstates = new TreeMap<Interstate, Interstate>(new Comparator<Interstate>() {
				@Override
				public int compare(Interstate paramT1, Interstate paramT2) {
					// Sort by smallest numbered interstate first
					return paramT1.getNumber() - paramT2.getNumber();
				}
			});
		}
	}
}
