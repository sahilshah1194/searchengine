package indexer;


import mapreduce.MyHttpClient;
import crawler.storage.DocumentDBWrapper;

public class Indexer{
	private static String master = "52.10.8.98:80";


	public static void main(String args[]){
		String documentDirectory = args[0];
		DocumentDBWrapper documentDB = DocumentDBWrapper.getInstance(documentDirectory);
		documentDB.initIterator();
		
		Thread threadpool[] = new Thread[10];
        for(int i=0; i<threadpool.length; i++){
        	threadpool[i] = (new IndexerThread(documentDB));
        }
        
        for(int i=0; i<threadpool.length; i++){
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
            threadpool[i].start();
        }
        
        
		//Wait until all threads done
        for(int i=0; i<threadpool.length; i++){
			try {
				threadpool[i].join();
			} catch (InterruptedException e) {
				System.err.println("Index thread ended unnaturally");
			}
		}
        
        MyHttpClient client = new MyHttpClient(master, "/InvertedIndexMaster/close");
		client.sendPost();
		client.setBody("");
        
        System.out.println("Indexing done");
		
		documentDB.closeIterator();
        documentDB.close();
		
	}
	
	
}
