package crawler;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import mapreduce.ShuffleURLWorker.ShuffleURLMapThread;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.sleepycat.je.DatabaseException;

import crawler.storage.DocumentDBWrapper;
import crawler.storage.RobotsDBWrapper;
import crawler.storage.URLFrontierDBWrapper;
import crawler.storage.UnseenLinksDBWrapper;


public class ThreadPool {

	//private BlockingQueue bq = null;
    private List<Thread> threads = new ArrayList<Thread>();
    public ThreadPool(int noOfThreads, String documentDirectory, String frontierDirectory, String robotsDirectory, String unseenLinksDirectory,  int maxSize) throws DatabaseException, FileNotFoundException{
        //bq = new BlockingQueue();
    	DocumentDBWrapper docDB = DocumentDBWrapper.getInstance(documentDirectory);
    	URLFrontierDBWrapper frontierDB = URLFrontierDBWrapper.getInstance(frontierDirectory);
		RobotsDBWrapper robotsDB = RobotsDBWrapper.getInstance(robotsDirectory);
		UnseenLinksDBWrapper unseenLinksDB = UnseenLinksDBWrapper.getInstance(unseenLinksDirectory);
		try {
			DetectorFactory.loadProfile(System.getProperty("user.dir")+"/lib/profiles");
		} catch (LangDetectException e1) {
			e1.printStackTrace();
		}
		/*while (!db.isEmpty()) {
			Entry<Integer, QueueEntity> entry1 = db.getNextUrl();
			System.out.println(entry1.getKey()+": " + entry1.getValue().getUrl());
		}*/
		
		System.out.println("BEFORE document DB size: "+docDB.getSize());
		System.out.println("BEFORE unseen links size: "+unseenLinksDB.getSize());
		System.out.println("BEFORE frontier links size: "+frontierDB.getSize());
    	
        for(int i=0; i<noOfThreads; i++){
            threads.add(new CrawlerThread(docDB, frontierDB, robotsDB, unseenLinksDB, maxSize));
        }
        
        
        Thread timer = new TimerThread(docDB, frontierDB, robotsDB, unseenLinksDB);
        timer.start();
        
        for(Thread thread : threads){
            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            thread.start();
        }
        
        
		//Wait until all threads done
		for(Thread thread : threads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println("Map thread ended unnaturally");
			}
		}
		System.out.println("AFTER document DB size: "+docDB.getSize());
		System.out.println("AFTER frontier DB size: "+frontierDB.getSize());
		System.out.println("AFTER unseen links size: "+unseenLinksDB.getSize());
		System.out.println("Closing entire crawler");
		frontierDB.close();
		docDB.close();
		unseenLinksDB.close();
		robotsDB.close();
		if(timer.isAlive()){
			timer.interrupt();
		}

    }

    /*public synchronized void  execute(Socket request) throws Exception{
        if(this.isStopped) throw
            new IllegalStateException("ThreadPool is stopped");

        this.bq.enqueue(request);
    }*/

    public synchronized void stop(){
        notifyAll();
        for(Thread thread : threads){
           ((CrawlerThread) thread).doStop();
        }
    }
    
    public synchronized List<Thread> getThreadList() {
    	return this.threads;
    }
}
