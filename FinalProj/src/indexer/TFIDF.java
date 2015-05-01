package indexer;

import indexer.storage.InvertedIndexDBWrapper;

public class TFIDF {

	public static void main(String args[]){
		String indexDirectory = args[0];
		InvertedIndexDBWrapper indexDB = InvertedIndexDBWrapper.getInstance(indexDirectory);
		indexDB.initIterator();
		
		Thread threadpool[] = new Thread[10];
        for(int i=0; i<threadpool.length; i++){
        	threadpool[i] = (new TFIDFThread(indexDB));
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
        
        System.out.println("TF/IDF done");
        indexDB.closeIterator();;
		indexDB.close();
		
	}
}