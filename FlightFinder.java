package flightfinder;
import java.io.*;
import java.util.*;
import javafx.util.Pair;

class Authenticator
{
    private static final String password = "dsp2017";
    
    private boolean isLoggedIn;
    public Authenticator() {
        isLoggedIn = false;
    }
    public boolean Authenticate(String password) {
        isLoggedIn = password.equals(Authenticator.password);
        return isLoggedIn;
    }
    public void LogOut() {
        isLoggedIn = false;
    }
    public boolean IsLoggedIn() {
        return isLoggedIn;
    }
}

//TODO fix problem with price for flight from one airport to the same
class AirportSearchEngine
{
	class Airport
	{
		private final String code;
		private final String name;
		private final String location;
		Airport(String sCode, String sName, String sLocation) {
			code 		= sCode;
			name 		= sName;
			location 	= sLocation;
		}
                @Override
		public String toString()
		{
			return name + "; " + location + "(" + code + ")";
		}
	}
	private ArrayList<Airport> airportDatabase;
	private ArrayList<ArrayList<Integer>> flightPriceMatrix;
	private String databaseFile;
	private String priceFile;
	
	
	AirportSearchEngine()
	{
		//databaseFile 		= "airports.csv";
		databaseFile 		= "airports_test.csv";
		//priceFile 			= "prices.csv";
		priceFile 			= "prices_test.csv";
		
		airportDatabase 	= new ArrayList<Airport>();
		flightPriceMatrix 	= new ArrayList<ArrayList<Integer>>();
	}
	
	private int FindAirportId(String airportCode)
	{
            if (airportCode == null)
                return -1;
            int lowerBound = 0;
            int upperBound = airportDatabase.size() - 1;
            while (lowerBound <= upperBound) {
                int idx = (lowerBound + upperBound) / 2;
                int cmp = airportDatabase.get(idx).code.compareTo(airportCode);
                if (cmp == 0)
                    return idx;
                if (cmp > 0)// idx > airportCode -----> look in the left half
                    upperBound = idx - 1;
                else
                    lowerBound = idx + 1;
            }   
            return -1;
            /*for (int i = 0; i < airportDatabase.size(); ++i)
		if (airportDatabase.get(i).code.equals(airportCode))
                    return i;
            return -1;*/
	}
        
        private ArrayList<Integer> Dijkstra(String fromAirport, String toAirport)
        {
            ArrayList<Integer> distances = new ArrayList<>();
            int fromAirportId = FindAirportId(fromAirport);
            int destinationId = FindAirportId(toAirport);
            for (int i = 0; i < airportDatabase.size(); i++) {
                distances.add(Integer.MAX_VALUE);
            }
            distances.set(fromAirportId, 0);            
            PriorityQueue<Pair<Integer,Integer>> queue = new PriorityQueue<>((a,b) -> a.getValue() - b.getValue());
            for (int i = 0; i < airportDatabase.size(); i++) {
                queue.add(new Pair(i, distances.get(i)));                
            }
            while (! queue.isEmpty()) {
                Pair<Integer, Integer> head = queue.poll();
                if (head.getValue() == Integer.MAX_VALUE || head.getKey() == destinationId)
                    break;
                for (int i = 0; i < airportDatabase.size(); i++) {
                    if (head.getKey() == i)
                        continue;
                    if (flightPriceMatrix.get(head.getKey()).get(i) == 0) // no flight between head and i
                        continue;
                    int cmpDist = distances.get(head.getKey()) + flightPriceMatrix.get(head.getKey()).get(i);
                    if (cmpDist < distances.get(i)) {
                        queue.remove(new Pair(i, distances.get(i)));      
                        distances.set(i, cmpDist);                  
                        queue.add(new Pair(i, distances.get(i)));
                    }                    
                }
            }
            return distances;
        }
        
        private ArrayList<Integer> Dijkstra(String fromAirport)
        {
            return Dijkstra(fromAirport, null);
        }
        
        public void PrintAirports()
        {
            for (Airport airport: airportDatabase)
                System.out.println(airport);
        }
	
	//return true if loaded correctly
	public Boolean LoadDatabase()
	{
		String line 	= null;
		String delims 	= ";";
		int lineNumber 	= 0;
		int columnNumber;
		String[] tokens;
		
		try{
			FileReader fileReader 			= new FileReader(databaseFile);
			BufferedReader bufferReader 	= new BufferedReader(fileReader);
			
			while((line = bufferReader.readLine()) != null)
			{
				tokens 		= line.split(delims);
				Airport ap 	= new Airport(tokens[0],tokens[1],tokens[2]);
				AddAirport(ap);
			}
			
			fileReader 			= new FileReader(priceFile);
			bufferReader 		= new BufferedReader(fileReader);
			
			while((line = bufferReader.readLine()) != null)
			{
                                columnNumber = 0;
				tokens = line.split(delims);
				for (String token : tokens)
                                    AddFlight(lineNumber, columnNumber++, Integer.parseInt(token));
				lineNumber++;
			}
                        
                    /*    for (int i = 0; i < airportDatabase.size(); ++i)
                        {
                            for (int j = 0; j < airportDatabase.size(); ++j)
                                    System.out.print(flightPriceMatrix.get(i).get(j) + " ");
                            System.out.println();
                        }*/
		}
		catch(Exception ex)
		{
			System.out.println("Exception occured while reading " + ex.toString());
			return false;
		}
		return true;
	}
	
	//return true if saved correctly
	//save in exactly the same format as loaded ... airports are sorted by code
	public Boolean SaveDatabase()
	{
            try {
                FileWriter fileWriter = new FileWriter(databaseFile);
                String delim = ";";
                StringBuilder stringBuilder = new StringBuilder();
                for (Airport airport: airportDatabase) {
                    stringBuilder.append(airport.code).append(delim);
                    stringBuilder.append(airport.name).append(delim);
                    stringBuilder.append(airport.location).append(System.lineSeparator());
                }
                fileWriter.write(stringBuilder.toString());
                fileWriter.close();
            }
            catch (IOException ex) {
                System.err.println("Error saving database file: " + ex.getMessage());
                return false;
            }
            try {
                FileWriter fileWriter = new FileWriter(priceFile);
                String delim = ";";
                StringBuilder stringBuilder = new StringBuilder();
                for (int from = 0; from < airportDatabase.size(); from++) {
                    int to = 0; for (; to < airportDatabase.size() - 1; to++) {
                        stringBuilder.append(flightPriceMatrix.get(from).get(to).toString()).append(delim);
                    }
                    stringBuilder.append(flightPriceMatrix.get(from).get(to).toString()).append(System.lineSeparator());
                }
                fileWriter.write(stringBuilder.toString());
                fileWriter.close();
            }
            catch (IOException ex) {
                System.err.println("Error saving price file: " + ex.getMessage());
                return false;
            }
            return true;
	}
	
	//return false if Airport already exists (code check)
	//check if airport already exists if so return false
	private Boolean AddAirport(Airport newAirport)
	{
		if (FindAirportId(newAirport.code) != -1)
			return false;
		airportDatabase.add(newAirport);
		flightPriceMatrix.add(new ArrayList<Integer>());
		for(int i = 0; i < airportDatabase.size(); ++i)
                {
			flightPriceMatrix.get(i).add(0);
                        if (airportDatabase.size() > i + 1)
                            flightPriceMatrix.get(airportDatabase.size() - 1).add(0);
                }
		/*for (int i = 0; i < airportDatabase.size(); ++i)
		{
			for (int j = 0; j < airportDatabase.size(); ++j)
				System.out.print(flightPriceMatrix.get(i).get(j) + " ");
			System.out.println();
		}
                System.out.println();*/
		return true;
			
	}
        
        public Boolean AddAirport(String code, String name, String location) {
            Airport airport = new Airport(code, name, location);
            return AddAirport(airport);
        }
	
	//return true if inserted correctly (both airport exist and no flight exists or price is lower than previous)
	public Boolean AddFlight(String fromAirport, String toAirport, int price)
	{
            if (price <= 0)
                return false;
            int iFromAirport 	= FindAirportId(fromAirport);
            int iToAirport 		= FindAirportId(toAirport);
            
            if (iFromAirport == -1 || iToAirport == -1)
                return false;
            //new price is higher than previous one (and exists)
            if (flightPriceMatrix.get(iFromAirport).get(iToAirport) < price && flightPriceMatrix.get(iFromAirport).get(iToAirport) > 0)
                return false;
            flightPriceMatrix.get(iFromAirport).set(iToAirport,price);
            return true;
	}
        //return true if inserted correctly (both airport exist and no flight exists or price is lower than previous)
	private Boolean AddFlight(int fromAirport, int toAirport, int price)
	{
            if (fromAirport < airportDatabase.size() && toAirport < airportDatabase.size())
            {
                flightPriceMatrix.get(fromAirport).set(toAirport,price);
                return true;
            }
            return false;
	}
	
	//return true if deleted correctly
	public Boolean DeleteFlight(String fromAirport, String toAirport)
	{
		return true;
	}
	
	//return true if deleted correctly
	public Boolean DeleteAirport(Airport toDelete)
	{
		return true;
	}
	
	//shows price of direct flight if available (return true)
	public Boolean SearchDirectFlight(String fromAirport, String toAirport)
	{
		int iFromAirport 	= FindAirportId(fromAirport);
		int iToAirport 		= FindAirportId(toAirport);
		
		//System.out.println(iFromAirport + "->" + iToAirport);
		
		if ( iFromAirport == -1 || iToAirport == -1)
			return false;
		
		int price = flightPriceMatrix.get(iFromAirport).get(iToAirport);
		if (price == 0) {
                    System.out.println("It is not possible to fly from:\n" + 
			airportDatabase.get(iFromAirport).toString() + "\nto:\n" + 
			airportDatabase.get(iToAirport).toString() + ".");
                    return false;
                }
		System.out.println("You can fly from:\n " + 
			airportDatabase.get(iFromAirport).toString() + "\nto:\n" + 
			airportDatabase.get(iToAirport).toString() + "\nprice:" + price);
		return true;
	}
	
	//shows price of flight if available (return true) and transfer airports
	//Dijkstra's
	public Boolean SearchFlight(String fromAirport, String toAirport)
	{
            int fromAirportId = FindAirportId(fromAirport);
            int toAirportId = FindAirportId(toAirport);
            if (fromAirportId == -1 || toAirportId == -1) {
                return false;
            }
            ArrayList<Integer> distances =  Dijkstra(fromAirport, toAirport);
            if (distances.get(toAirportId) == 0 || distances.get(toAirportId) == Integer.MAX_VALUE) {
                System.out.println("It is not possible to fly from:\n" + 
			airportDatabase.get(fromAirportId).toString() + "\nto:\n" + 
			airportDatabase.get(toAirportId).toString() + ".");
                return false;
            }
            System.out.println("You can fly from:\n " + 
			airportDatabase.get(fromAirportId).toString() + "\nto:\n" + 
			airportDatabase.get(toAirportId).toString() + "\nprice:" + distances.get(toAirportId));
            return true;
	}
	
	//shows all direct flights from destination sorted by price ascending
        //solve case where airport is correct but no flights available
	public Boolean AllDirectFlights(String fromAirport)
	{
            int airportId = FindAirportId(fromAirport);
            if (airportId == -1)
                return false;
            System.out.println("From airport " + airportDatabase.get(airportId).toString() + " you can fly to:");
            for (int i = 0; i < airportDatabase.size(); ++i)
            {
                if (flightPriceMatrix.get(airportId).get(i) != 0)
                    System.out.println("\t" + airportDatabase.get(i).toString() + " for $" + flightPriceMatrix.get(airportId).get(i));
            }
            return true;
	}
        
        //shows all direct flights from destination sorted by price ascending
        //Dijkstra's
        public Boolean AllFlights(String fromAirport)
	{
            int airportId = FindAirportId(fromAirport);
            if (airportId == -1)
                return false;
            ArrayList<Integer> distances =  Dijkstra(fromAirport);
            System.out.println("From airport " + airportDatabase.get(airportId).toString() + " you can fly to:");
            for (int i = 0; i < distances.size(); ++i)
            {
                if (distances.get(i) != 0 && distances.get(i) != Integer.MAX_VALUE)
                    System.out.println("\t" + airportDatabase.get(i).toString() + " for $" + distances.get(i));
            }
            return true;
	}
}
public class FlightFinder {
    
    public static String infoMessage = "What would you like to do? Type \"help\" for list of commands.";
    public static String helpCommandMessage = "   help" + System.lineSeparator() + "Display this message" + System.lineSeparator() +
                                       "   list" + System.lineSeparator() + "Display list of airports" + System.lineSeparator() +
                                       "   dflight" + System.lineSeparator() + "Look for a direct flight from A to B - use airport codes" + System.lineSeparator() +
                                       "   flight" + System.lineSeparator() + "Look for a cheapest flight from A to B - use airport codes" + System.lineSeparator() +
                                       "   alldflights" + System.lineSeparator() + "Look for all direct flights from A - use airport code" + System.lineSeparator() +
                                       "   allflights" + System.lineSeparator() + "Look for all cheapest flights from A - use airport code" + System.lineSeparator() +
                                       "   admin" + System.lineSeparator() + "Log in as admin" + System.lineSeparator() +
                                       "   logout" + System.lineSeparator() + "(ADMIN COMMAND) Log out as admin" + System.lineSeparator() +
                                       "   load" + System.lineSeparator() + "(ADMIN COMMAND) Load database from file" + System.lineSeparator() +
                                       "   save" + System.lineSeparator() + "(ADMIN COMMAND) Save database to file" + System.lineSeparator() +
                                       "   addairport" + System.lineSeparator() + "(ADMIN COMMAND) Add an airport to database" + System.lineSeparator() +
                                       "   addflight" + System.lineSeparator() + "(ADMIN COMMAND) Add a flight from A to B to database" + System.lineSeparator() +
                                       "   exit" + System.lineSeparator() + "Exit the program";
    public static String originMessage = "Enter the code of your origin airport: ";
    public static String destinationMessage = "Enter the code of your destination airport: ";
    public static String priceMessage = "Enter the price of the new flight: ";
    public static String codeMessage = "Enter the code of the new airport: ";
    public static String nameMessage = "Enter the name of the new airport: ";
    public static String locationMessage = "Enter the location of the new airport: ";
    public static String successMessage = "Successfully added to database.";
    public static String failedMessage = "Failed to add to database.";
    public static String adminPrompt = "Enter the administrator password: ";
    public static String adminSuccess = "Successfully logged in. You may now use admin commands.";
    public static String adminFailed = "Wrong password!";
    public static String adminLogOut = "Logged out.";
    public static String permissionMessage = "You need admin rights to use this command!";
    public static String invalidCommandMessage = "Invalid command. Type \"help\" for list of commands.";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        AirportSearchEngine ASE = new AirportSearchEngine();
        ASE.LoadDatabase();
        Scanner scanner = new Scanner(System.in);
        boolean run = true;
        Authenticator auth = new Authenticator();
        while (run) {
            System.out.println(infoMessage);
            String input = scanner.nextLine();
            switch (input) {
                case "help":
                    System.out.println(helpCommandMessage);
                    break;
                case "list":
                    ASE.PrintAirports();
                    break;
                case "dflight": {
                    System.out.print(originMessage);
                    String fromAirport = scanner.nextLine();
                    System.out.print(destinationMessage);
                    String toAirport = scanner.nextLine();
                    ASE.SearchDirectFlight(fromAirport, toAirport);
                    break; }
                case "flight": {
                    System.out.print(originMessage);
                    String fromAirport = scanner.nextLine();
                    System.out.print(destinationMessage);
                    String toAirport = scanner.nextLine();
                    ASE.SearchFlight(fromAirport, toAirport);   
                    break; }
                case "alldflights": {
                    System.out.print(originMessage);
                    String fromAirport = scanner.nextLine();
                    ASE.AllDirectFlights(fromAirport);
                    break; }
                case "allflights": {
                    System.out.print(originMessage);
                    String fromAirport = scanner.nextLine();
                    ASE.AllFlights(fromAirport);
                    break; }
                case "admin": {
                    System.out.print(adminPrompt);
                    String password = scanner.nextLine();
                    boolean isLoggedIn = auth.Authenticate(password);
                    if (isLoggedIn)
                        System.out.println(adminSuccess);
                    else
                        System.out.println(adminFailed);
                    break; }        
                case "logout":
                    auth.LogOut();
                    System.out.println(adminLogOut);
                    break;
                case "load":
                    if (auth.IsLoggedIn())
                        System.out.println(ASE.LoadDatabase());
                    else
                        System.out.println(permissionMessage);
                    break;
                case "save":
                    if (auth.IsLoggedIn())
                        System.out.println(ASE.SaveDatabase());
                    else
                        System.out.println(permissionMessage);
                    break;
                case "addairport":
                    if (auth.IsLoggedIn()) {
                        System.out.print(codeMessage);
                        String code = scanner.nextLine();
                        System.out.print(nameMessage);
                        String name = scanner.nextLine();
                        System.out.print(locationMessage);
                        String location = scanner.nextLine();
                        if (ASE.AddAirport(code, name, location))
                            System.out.println(successMessage);
                        else
                            System.out.println(failedMessage);
                    }
                    else
                        System.out.println(permissionMessage);
                    break;
                case "addflight":
                    if (auth.IsLoggedIn()) {
                        System.out.print(originMessage);
                        String from = scanner.nextLine();
                        System.out.print(destinationMessage);
                        String to = scanner.nextLine();
                        System.out.print(priceMessage);
                        Integer price = Integer.parseInt(scanner.nextLine());
                        if (ASE.AddFlight(from, to, price))
                            System.out.println(successMessage);
                        else
                            System.out.println(failedMessage);
                    }
                    else
                        System.out.println(permissionMessage);
                    break;
                case "exit":
                    run = false;
                    break;
                default:
                    System.out.println(invalidCommandMessage);
                    break;
            }
        }        
    }
}