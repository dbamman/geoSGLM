package geosglm.ark.cs.cmu.edu;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.google.common.collect.Maps;

/**
 * Code for resolving location strings to states. Relies on external file
 * ("city.txt") that lists city/state combinations for US cities (for
 * high-precision matching like "Reno, NV")
 * 
 */
public class StateLevelGeoResolver {

	HashMap<String, Integer> stateKey;
	HashMap<Integer, String> reverseStateKey;

	HashMap<String, String> cityToStates;
	HashMap<String, String> states;
	HashMap<String, String> reverseStates;

	public void setStates() {
		states = Maps.newHashMap();
		states.put("alabama", "AL");
		states.put("alaska", "AK");
		states.put("arizona", "AZ");
		states.put("arkansas", "AR");
		states.put("california", "CA");
		states.put("colorado", "CO");
		states.put("connecticut", "CT");
		states.put("district of columbia", "DC");
		states.put("delaware", "DE");
		states.put("florida", "FL");
		states.put("georgia", "GA");
		states.put("hawaii", "HI");
		states.put("idaho", "ID");
		states.put("illinois", "IL");
		states.put("indiana", "IN");
		states.put("iowa", "IA");
		states.put("kansas", "KS");
		states.put("kentucky", "KY");
		states.put("louisiana", "LA");
		states.put("maine", "ME");
		states.put("maryland", "MD");
		states.put("massachusetts", "MA");
		states.put("michigan", "MI");
		states.put("minnesota", "MN");
		states.put("mississippi", "MS");
		states.put("missouri", "MO");
		states.put("montana", "MT");
		states.put("nebraska", "NE");
		states.put("nevada", "NV");
		states.put("new hampshire", "NH");
		states.put("new jersey", "NJ");
		states.put("new mexico", "NM");
		states.put("new york", "NY");
		states.put("north carolina", "NC");
		states.put("north dakota", "ND");
		states.put("ohio", "OH");
		states.put("oklahoma", "OK");
		states.put("oregon", "OR");
		states.put("pennsylvania", "PA");
		states.put("rhode island", "RI");
		states.put("south carolina", "SC");
		states.put("south dakota", "SD");
		states.put("tennessee", "TN");
		states.put("texas", "TX");
		states.put("utah", "UT");
		states.put("vermont", "VT");
		states.put("virginia", "VA");
		states.put("washington", "WA");
		states.put("west virginia", "WV");
		states.put("wisconsin", "WI");
		states.put("wyoming", "WY");

		reverseStates = Maps.newHashMap();
		for (String name : states.keySet()) {
			String val = states.get(name);
			reverseStates.put(val, name);
		}
	}

	public void addCities(HashMap<String, String> c) {
		for (String key : c.keySet()) {
			cityToStates.put(key, c.get(key));
		}
	}

	public HashMap<String, String> getReverseStates() {
		return reverseStates;
	}

	public void readCities(String infile) {

		try {
			BufferedReader in1 = new BufferedReader(new InputStreamReader(
					new FileInputStream(infile), "UTF-8"));
			String str1;

			while ((str1 = in1.readLine()) != null) {
				try {
					String[] parts = str1.trim().split("\t");
					String city = parts[0].replaceAll("[^A-Za-z ]", "");

					String state = parts[1];

					cityToStates.put(
							city.toLowerCase() + " " + state.toLowerCase(),
							state);
					cityToStates.put(city.toLowerCase() + " "
							+ reverseStates.get(state).toLowerCase(), state);

					if (city.matches(".*Saint.*")) {
						city = city.replaceAll("Saint", "St");
						cityToStates.put(
								city.toLowerCase() + " " + state.toLowerCase(),
								state);
						cityToStates
								.put(city.toLowerCase()
										+ " "
										+ reverseStates.get(state)
												.toLowerCase(), state);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void append() {

		cityToStates.put("new york ny", "NY");
		cityToStates.put("nyc", "NY");
		cityToStates.put("ny", "NY");
		cityToStates.put("nj", "NJ");

		cityToStates.put("atl", "GA");
		cityToStates.put("bay area", "CA");

		cityToStates.put("la", "CA");
		cityToStates.put("cali", "CA");
		cityToStates.put("va", "VA");
		cityToStates.put("nc", "NC");
		cityToStates.put("ct", "CT");

		cityToStates.put("brooklyn", "NY");
		cityToStates.put("brooklyn ny", "NY");

		cityToStates.put("jersey", "NJ");
		cityToStates.put("philly", "PA");

		cityToStates.put("southern california", "CA");

		cityToStates.put("washington dc", "DC");

		cityToStates.put("new york city", "NY");
		cityToStates.put("brooklyn, new york", "NY");
		cityToStates.put("long island, new york", "NY");

		cityToStates.put("los angeles", "CA");
		cityToStates.put("chicago", "IL");
		cityToStates.put("houston", "TX");
		cityToStates.put("philadelphia", "PA");
		cityToStates.put("phoenix", "AZ");
		cityToStates.put("san antonio", "TX");
		cityToStates.put("san diego", "CA");
		cityToStates.put("dallas", "TX");
		cityToStates.put("detroit", "MI");
		cityToStates.put("st louis", "MO");
		cityToStates.put("jacksonville", "FL");
		cityToStates.put("indianapolis", "IN");
		cityToStates.put("columbus", "OH");
		cityToStates.put("san francisco", "CA");
		cityToStates.put("austin", "TX");
		cityToStates.put("memphis", "TN");
		cityToStates.put("fort worth", "TX");
		cityToStates.put("baltimore", "MD");
		cityToStates.put("charlotte", "NC");
		cityToStates.put("boston", "MA");
		cityToStates.put("milwaukee", "WI");
		cityToStates.put("seattle", "WA");
		cityToStates.put("el paso", "TX");
		cityToStates.put("denver", "CO");
		cityToStates.put("portland", "OR");
		cityToStates.put("oklahoma city", "OK");
		cityToStates.put("nashville", "TN");
		cityToStates.put("tucson", "AZ");
		cityToStates.put("albuquerque", "NM");
		cityToStates.put("new orleans", "LA");
		cityToStates.put("long beach", "CA");
		cityToStates.put("las vegas", "NV");
		cityToStates.put("sacramento", "CA");
		cityToStates.put("fresno", "CA");
		cityToStates.put("cleveland", "OH");
		cityToStates.put("kansas city", "MO");
		cityToStates.put("virginia beach", "VA");
		cityToStates.put("atlanta", "GA");
		cityToStates.put("oakland", "CA");
		cityToStates.put("mesa", "AZ");
		cityToStates.put("tulsa", "OK");
		cityToStates.put("omaha", "NE");
		cityToStates.put("miami", "FL");
		cityToStates.put("honolulu", "HI");
		cityToStates.put("minneapolis", "MN");
		cityToStates.put("colorado springs", "CO");
		cityToStates.put("wichita", "KS");
		cityToStates.put("west raleigh", "NC");
		cityToStates.put("arlington", "TX");
		cityToStates.put("anaheim", "CA");
		cityToStates.put("tampa", "FL");
		cityToStates.put("saint louis", "MO");
		cityToStates.put("pittsburgh", "PA");
		cityToStates.put("toledo", "OH");
		cityToStates.put("cincinnati", "OH");
		cityToStates.put("riverside", "CA");
		cityToStates.put("bakersfield", "CA");
		cityToStates.put("stockton", "CA");
		cityToStates.put("newark", "NJ");
		cityToStates.put("buffalo", "NY");
		cityToStates.put("corpus christi", "TX");
		cityToStates.put("aurora", "CO");
		cityToStates.put("raleigh", "NC");
		cityToStates.put("saint paul", "MN");
		cityToStates.put("lexington-fayette", "KY");
		cityToStates.put("anchorage", "AK");
		cityToStates.put("plano", "TX");
		cityToStates.put("saint petersburg", "FL");
		cityToStates.put("louisville", "KY");
		cityToStates.put("lincoln", "NE");
		cityToStates.put("glendale", "AZ");
		cityToStates.put("henderson", "NV");
		cityToStates.put("jersey city", "NJ");
		cityToStates.put("norfolk", "VA");
		cityToStates.put("chandler", "AZ");
		cityToStates.put("greensboro", "NC");
		cityToStates.put("birmingham", "AL");
		cityToStates.put("fort wayne", "IN");
		cityToStates.put("lexington", "KY");
		cityToStates.put("hialeah", "FL");
		cityToStates.put("madison", "WI");
		cityToStates.put("baton rouge", "LA");
		cityToStates.put("garland", "TX");
		cityToStates.put("modesto", "CA");
		cityToStates.put("paradise", "NV");
		cityToStates.put("chula vista", "CA");
		cityToStates.put("lubbock", "TX");
		cityToStates.put("rochester", "NY");
		cityToStates.put("laredo", "TX");
		cityToStates.put("akron", "OH");
		cityToStates.put("orlando", "FL");
		cityToStates.put("durham", "NC");
		cityToStates.put("north glendale", "CA");
		cityToStates.put("scottsdale", "AZ");

	}

	public static int MA_KEY = 0;
	public static int CA_KEY = 1;
	public static int IL_KEY = 2;
	public static int GA_KEY = 3;
	public static int PA_KEY = 4;
	public static int TX_KEY = 5;
	public static int NY_KEY = 6;
	public static int FL_KEY = 7;
	public static int OH_KEY = 8;
	public static int NC_KEY = 9;
	public static int MI_KEY = 10;
	public static int NJ_KEY = 11;
	public static int VA_KEY = 12;
	public static int MD_KEY = 13;
	public static int TN_KEY = 14;
	public static int IN_KEY = 15;
	public static int LA_KEY = 16;
	public static int WA_KEY = 17;
	public static int MO_KEY = 18;
	public static int AZ_KEY = 19;
	public static int AL_KEY = 20;
	public static int SC_KEY = 21;
	public static int NV_KEY = 22;
	public static int MN_KEY = 23;
	public static int KY_KEY = 24;
	public static int WI_KEY = 25;
	public static int CT_KEY = 26;
	public static int CO_KEY = 27;
	public static int DC_KEY = 28;
	public static int OK_KEY = 29;
	public static int OR_KEY = 30;
	public static int KS_KEY = 31;
	public static int MS_KEY = 32;
	public static int IA_KEY = 33;
	public static int AR_KEY = 34;
	public static int NE_KEY = 35;
	public static int WV_KEY = 36;
	public static int RI_KEY = 37;
	public static int UT_KEY = 38;
	public static int DE_KEY = 39;
	public static int NM_KEY = 40;
	public static int HI_KEY = 41;
	public static int NH_KEY = 42;
	public static int ME_KEY = 43;
	public static int AK_KEY = 44;
	public static int ID_KEY = 45;
	public static int SD_KEY = 46;
	public static int ND_KEY = 47;
	public static int VT_KEY = 48;
	public static int MT_KEY = 49;
	public static int WY_KEY = 50;

	public void setStateKeys() {
		stateKey = Maps.newHashMap();
		reverseStateKey = Maps.newHashMap();

		stateKey.put("MA", MA_KEY);
		reverseStateKey.put(MA_KEY, "MA");

		stateKey.put("CA", CA_KEY);
		reverseStateKey.put(CA_KEY, "CA");

		stateKey.put("IL", IL_KEY);
		reverseStateKey.put(GA_KEY, "IL");

		stateKey.put("GA", GA_KEY);
		reverseStateKey.put(GA_KEY, "GA");

		stateKey.put("PA", PA_KEY);
		reverseStateKey.put(PA_KEY, "PA");

		stateKey.put("CA", CA_KEY);
		reverseStateKey.put(CA_KEY, "CA");

		stateKey.put("TX", TX_KEY);
		reverseStateKey.put(TX_KEY, "TX");

		stateKey.put("NY", NY_KEY);
		reverseStateKey.put(NY_KEY, "NY");

		stateKey.put("FL", FL_KEY);
		reverseStateKey.put(FL_KEY, "FL");

		stateKey.put("GA", GA_KEY);
		reverseStateKey.put(GA_KEY, "GA");

		stateKey.put("IL", IL_KEY);
		reverseStateKey.put(IL_KEY, "IL");

		stateKey.put("OH", OH_KEY);
		reverseStateKey.put(OH_KEY, "OH");

		stateKey.put("PA", PA_KEY);
		reverseStateKey.put(PA_KEY, "PA");

		stateKey.put("NC", NC_KEY);
		reverseStateKey.put(NC_KEY, "NC");

		stateKey.put("MI", MI_KEY);
		reverseStateKey.put(MI_KEY, "MI");

		stateKey.put("NJ", NJ_KEY);
		reverseStateKey.put(NJ_KEY, "NJ");

		stateKey.put("MA", MA_KEY);
		reverseStateKey.put(MA_KEY, "MA");

		stateKey.put("VA", VA_KEY);
		reverseStateKey.put(VA_KEY, "VA");

		stateKey.put("MD", MD_KEY);
		reverseStateKey.put(MD_KEY, "MD");

		stateKey.put("TN", TN_KEY);
		reverseStateKey.put(TN_KEY, "TN");

		stateKey.put("IN", IN_KEY);
		reverseStateKey.put(IN_KEY, "IN");

		stateKey.put("LA", LA_KEY);
		reverseStateKey.put(LA_KEY, "LA");

		stateKey.put("WA", WA_KEY);
		reverseStateKey.put(WA_KEY, "WA");

		stateKey.put("MO", MO_KEY);
		reverseStateKey.put(MO_KEY, "MO");

		stateKey.put("AZ", AZ_KEY);
		reverseStateKey.put(AZ_KEY, "AZ");

		stateKey.put("AL", AL_KEY);
		reverseStateKey.put(AL_KEY, "AL");

		stateKey.put("SC", SC_KEY);
		reverseStateKey.put(SC_KEY, "SC");

		stateKey.put("NV", NV_KEY);
		reverseStateKey.put(NV_KEY, "NV");

		stateKey.put("MN", MN_KEY);
		reverseStateKey.put(MN_KEY, "MN");

		stateKey.put("KY", KY_KEY);
		reverseStateKey.put(KY_KEY, "KY");

		stateKey.put("WI", WI_KEY);
		reverseStateKey.put(WI_KEY, "WI");

		stateKey.put("CT", CT_KEY);
		reverseStateKey.put(CT_KEY, "CT");

		stateKey.put("CO", CO_KEY);
		reverseStateKey.put(CO_KEY, "CO");

		stateKey.put("DC", DC_KEY);
		reverseStateKey.put(DC_KEY, "DC");

		stateKey.put("OK", OK_KEY);
		reverseStateKey.put(OK_KEY, "OK");

		stateKey.put("OR", OR_KEY);
		reverseStateKey.put(OR_KEY, "OR");

		stateKey.put("KS", KS_KEY);
		reverseStateKey.put(KS_KEY, "KS");

		stateKey.put("MS", MS_KEY);
		reverseStateKey.put(MS_KEY, "MS");

		stateKey.put("IA", IA_KEY);
		reverseStateKey.put(IA_KEY, "IA");

		stateKey.put("AR", AR_KEY);
		reverseStateKey.put(AR_KEY, "AR");

		stateKey.put("NE", NE_KEY);
		reverseStateKey.put(NE_KEY, "NE");

		stateKey.put("WV", WV_KEY);
		reverseStateKey.put(WV_KEY, "WV");

		stateKey.put("RI", RI_KEY);
		reverseStateKey.put(RI_KEY, "RI");

		stateKey.put("UT", UT_KEY);
		reverseStateKey.put(UT_KEY, "UT");

		stateKey.put("DE", DE_KEY);
		reverseStateKey.put(DE_KEY, "DE");

		stateKey.put("NM", NM_KEY);
		reverseStateKey.put(NM_KEY, "NM");

		stateKey.put("HI", HI_KEY);
		reverseStateKey.put(HI_KEY, "HI");

		stateKey.put("NH", NH_KEY);
		reverseStateKey.put(NH_KEY, "NH");

		stateKey.put("ME", ME_KEY);
		reverseStateKey.put(ME_KEY, "ME");

		stateKey.put("AK", AK_KEY);
		reverseStateKey.put(AK_KEY, "AK");

		stateKey.put("ID", ID_KEY);
		reverseStateKey.put(ID_KEY, "ID");

		stateKey.put("SD", SD_KEY);
		reverseStateKey.put(SD_KEY, "SD");

		stateKey.put("ND", ND_KEY);
		reverseStateKey.put(ND_KEY, "ND");

		stateKey.put("VT", VT_KEY);
		reverseStateKey.put(VT_KEY, "VT");

		stateKey.put("MT", MT_KEY);
		reverseStateKey.put(MT_KEY, "MT");

		stateKey.put("WY", WY_KEY);
		reverseStateKey.put(WY_KEY, "WY");

	}

	public StateLevelGeoResolver() {

		setStates();
		setStateKeys();
		cityToStates = Maps.newHashMap();
		append();

	}

	public boolean isUSA(String location) {
		String loc = bigResolve(location);
		return loc != null;
	}

	public String bigResolve(String location) {
		location = location.trim();
		String trial = resolveCity(location);
		if (trial != null) {
			return trial;
		}
		trial = resolveState(location);
		return trial;

	}

	public Integer getState(String location) {
		String resolved = bigResolve(location);
		if (resolved != null && stateKey.containsKey(resolved)) {
			// System.out.println("found " + location + "\t" + resolved);
			return stateKey.get(resolved);

		}
		return null;
	}

	public String resolveState(String location) {
		location = location.toLowerCase().replaceAll("[^A-Za-z ]", "");
		if (states.containsKey(location)) {
			// System.out.println("matching:" + location);
			return states.get(location);
		}
		return null;
	}

	public String resolveCity(String location) {
		location = location.toLowerCase().replaceAll("[^A-Za-z ]", "");
		if (cityToStates.containsKey(location)) {
			// System.out.println("matching:" + location);
			return cityToStates.get(location);
		}
		return null;
	}

	/**
	 * Read in a TSV file and resolve the user location to a state if possible.
	 * Print the id, resolved location and the message for resolvable lines.
	 * 
	 * @param infile
	 */
	public void convert(String infile) {

		// set these to whatever format the input data is in.
		int idColumn = 0;
		int messageColumn = 11;
		int locationColumn = 7;
		
		
		try {
			BufferedReader in1 = new BufferedReader(new InputStreamReader(
					new FileInputStream(infile), "UTF-8"));
			String str1;
			while ((str1 = in1.readLine()) != null) {
				try {
					String[] parts = str1.trim().split("\t");
					String id = parts[idColumn];
					String words = parts[messageColumn];
					String location = parts[locationColumn];
					String resolved = bigResolve(location);

					if (resolved != null) {
						System.out.println(String.format("%s\t%s\t%s", id,
								resolved, words));
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			in1.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		StateLevelGeoResolver geo = new StateLevelGeoResolver();
		geo.readCities(args[0]);
		geo.convert(args[1]);
	}
}