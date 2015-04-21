package crawler.storage;

import java.io.File;
import java.io.FileNotFoundException;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;

public class UnseenLinksDBWrapper {
	private static String envDirectory = null;

	private Environment myEnv;
	private EntityStore unseenLinksStore;
	
	private PrimaryIndex<String, UnseenLinksData> unseenLinksIndex;
	
	private static UnseenLinksDBWrapper wrapper = null;

	private UnseenLinksDBWrapper(String directory) throws DatabaseException, FileNotFoundException {
		envDirectory = directory;
		
		File file = new File(System.getProperty("user.dir"), envDirectory);
		if (!file.exists()) {
			if(file.mkdir()){
				System.out.println("Creating directory " + file.getAbsolutePath());
			}
			else{
				System.out.println("Failed creating directory " + file.getAbsolutePath());
			};
			
		}
		else{
			System.out.println("Database directory exists");
		}
		
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setAllowCreate(true);
		
		StoreConfig storeConfig = new StoreConfig();
		storeConfig.setAllowCreate(true);
		

		myEnv = new Environment(file, envConfig);	   
		
		//URL Database
		unseenLinksStore = new EntityStore(myEnv, "unseenLinks", storeConfig);
		unseenLinksIndex = unseenLinksStore.getPrimaryIndex(String.class, UnseenLinksData.class);
		

	}


	public synchronized void close() throws DatabaseException{
		if (unseenLinksStore != null) {
			try {
				unseenLinksStore.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing store: " + dbe.toString());
				System.exit(-1);
			}
		}
		
		if (myEnv != null) {
			try {
				myEnv.close();
			} catch (DatabaseException dbe) {
				System.err.println("Error closing environment: " + dbe.toString());
				System.exit(-1);
			}
		}
		wrapper = null;
	}
	
	public synchronized Environment getEnvironment(){
		return myEnv;
	}
	
	/**
	 * Add robots.txt data to database
	 * @param hostname - host for robots.txt file
	 * @param allowedLinks - links allowed for this host
	 * @param disallowedLinks - links disallowed for this host
	 * @param crawlDelay - delay for this host
	 */
	public synchronized void addURL(String url){
		unseenLinksIndex.put(new UnseenLinksData(url));
	}
	
	/**
	 * Get UnseenLinksData from url
	 * @param hostName - host to get robots.txt for
	 * @return RobotsTxtData
	 */
	public synchronized UnseenLinksData getUnseenLinksData(String url){
		return unseenLinksIndex.get(url);
	}
	
	/**
	 * Delete RobotsTxtData 
	 * @param hostname - host to delete
	 */
	public synchronized void deleteUnseenLinksData(String url){
		unseenLinksIndex.delete(url);
	}
	
	public static synchronized UnseenLinksDBWrapper getInstance(String directory) throws DatabaseException, FileNotFoundException {
		if(wrapper == null) {
			wrapper = new UnseenLinksDBWrapper(directory);
		}
		return wrapper;
	}
}
