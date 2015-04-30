package mapreduce.InvertedIndexMaster;


/*import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;*/

import indexer.storage.InvertedIndexDBWrapper;
import indexer.storage.URLMetrics;
import indexer.storage.WordCountDBWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.*;

//import crawler.storage.URLFrontierDBWrapper;

//import mapreduce.MyHttpClient;

public class InvertedIndexMasterServlet extends HttpServlet {

	static final long serialVersionUID = 455555001;
	/*private static Map<String, ArrayList<String>> statusMap; 
	private static String numMapThreads = "20";
	private static String numReduceThreads = "20";*/

	public void init(ServletConfig config) throws ServletException {
		//statusMap = new HashMap<String, ArrayList<String>>();
		System.out.println("Master init");

	}
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{

		/*//Status page
		if(request.getRequestURI().contains("/status")){
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();
			out.println("<html><head><title>Master</title></head><body>");
			out.println("<h1>Name: Daniel Salowe</h1>");
			out.println("<h1>Pennkey: salowed</h1>");
			out.println("<table border='1' style='width:100%'>");
			out.println("<tr>");
			out.println("<td>IP:port</td>");
			out.println("<td>Status</td>");
			out.println("</tr>");

			Map<String, ArrayList<String>> localStatusMap =  getStatusMap();

			//Add full rows with stored data
			for(String key: localStatusMap.keySet()){
				ArrayList<String> params = localStatusMap.get(key);
				//Check if in last 30 seconds
				if((System.currentTimeMillis() - Long.parseLong(params.get(1))) < 30000){
					out.println("<tr>");
					out.println("<td>"+ key+ "</td>");
					for(int i = 0; i < 2; i++){
						out.println("<td>"+ params.get(i)+ "</td>");
					}
					out.println("</tr>");
				}

			}


			out.println("</table>");


			//Post to itself first
			out.println("<form style='text-align:center;'  method='post'>");
			out.println("<p align='center'>Input directory: </p><input type='text' name='input'></br>");
			out.println("<p align='center'>Output directory: </p><input type='text' name='output'></br>");
			out.println("<p align='center'>Number of map threads: </p><input type='text' name='numMapThreads'></br>");
			out.println("<p align='center'>Number of reduce threads: </p><input type='text' name='numReduceThreads'></br>");
			out.println("<input type='submit' value='Start Job'></form>");
			out.println("</body></html>");


			out.flush();


		}


		 */

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws java.io.IOException
	{
		if(request.getRequestURI().contains("/pushdata")){
			System.out.println("Data received");
			addData(request);
			PrintWriter writer = response.getWriter();
			writer.close();
		}
	}


	/**
	 * Append data from POST into index DB
	 * @param request from POST
	 * @throws IOException
	 */
	public synchronized void addData(HttpServletRequest request) throws IOException{
		BufferedReader in = request.getReader();
		InvertedIndexDBWrapper indexDB = InvertedIndexDBWrapper.getInstance("/home/cis455/Index/indexdb");
		WordCountDBWrapper numWordsDB = WordCountDBWrapper.getInstance("/home/cis455/Index/numwordsdb");


		String line = in.readLine();
		if(line == null)
			return;

		String[] firstLine = line.split("\\s+");
		int num = Integer.parseInt(firstLine[1]);
		String url = firstLine[0];

		System.out.println("url adding:" + firstLine[0] + " " + num);
		numWordsDB.addWord(firstLine[0], num);
		System.out.println("Size of num words" + numWordsDB.getSize());

		numWordsDB.close();

		line = in.readLine();
		while(line != null){
			try{

				String[] docData = line.split("\\s+");
				System.out.println("docData: " + line);
				HashMap<String, URLMetrics> urlMap= indexDB.getUrls(docData[0]); //look up by word
				if(urlMap == null){
					urlMap = new HashMap<String, URLMetrics>();
				}
				urlMap.put(url, new URLMetrics(Integer.parseInt(docData[1]),0,0));

				indexDB.addWord(docData[0], urlMap);
			}catch(Exception e){
				//Caught exception
				System.out.println("<---Caught Exception--->: " + e);
			}
				line = in.readLine();

			
		}
		System.out.println("Index DB Size: " + indexDB.getSize());
		System.out.println();
		indexDB.close();

	}



	/*
		{
		//Status update url
		if(request.getRequestURI().contains("/workerstatus")){
			//Get query string params
			String status = request.getParameter("status");
			String port = request.getParameter("port");
			String IPPort = request.getLocalAddr() + ":" + port;
			String timeLastReceived = System.currentTimeMillis() + "";			

			Map<String, ArrayList<String>> localStatusMap =  getStatusMap();
			ArrayList<String> params = new ArrayList<String>();
			params.add(status);
			params.add(timeLastReceived);
			localStatusMap.put(IPPort, params);

			updateStatusMap(IPPort, params);


			//Send post request to /runreduce for each active cleint

			boolean allWaiting = true;
			for(String key: localStatusMap.keySet()){
				if(!localStatusMap.get(key).get(0).equals("waiting")){
					allWaiting = false;
				}
			}

			if(allWaiting){
				System.out.println("All workers waiting");
				//Previous reduce code below
				for(String iport: localStatusMap.keySet()){
					if((System.currentTimeMillis() - Long.parseLong(params.get(1))) < 30000){
						MyHttpClient client = new MyHttpClient(iport, "/InvertedIndexWorker/runreduce");
						client.addParams("numThreads", numReduceThreads);						
						client.sendPost();
					}
				}
			}
		}
		else{

			//inputDir = request.getParameter("input");
		    //outputDir = request.getParameter("output");
			numMapThreads = request.getParameter("numMapThreads");
			//numReduceThreads = request.getParameter("numReduceThreads");
			Map<String, ArrayList<String>> localStatusMap =  getStatusMap();

			//Post to /runmap on every active worker
			for(String worker: localStatusMap.keySet()){
				MyHttpClient client = new MyHttpClient(worker, "/InvertedIndexWorker/runmap");
				//client.addParams("input", inputDir);
				client.addParams("numThreads", numMapThreads);
				client.addParams("numWorkers", localStatusMap.size() + "");
				int counter = 1;
				for(String workerID: localStatusMap.keySet()){
					client.addParams("worker"+counter, workerID);
					counter++;
				}
				//System.out.println("Posting /runmap to "+ worker);
				client.sendPost();
			}
		}*/

	//}


	//	/**
	//	 * Gets the current status map synchronously and copies it to a new one
	//	 * @return a copy of the map
	//	 */
	//	private Map<String, ArrayList<String>> getStatusMap(){
	//		Map<String, ArrayList<String>> copyMap =  new HashMap<String, ArrayList<String>>();
	//		synchronized(statusMap){
	//			for(String key: statusMap.keySet()){
	//				copyMap.put(key, statusMap.get(key));
	//			}
	//		}
	//		return copyMap;
	//	}
	//
	//	/**
	//	 * Updates the status map synchronously
	//	 * @param key to add
	//	 * @param value to add 
	//	 */
	//	private void updateStatusMap(String key, ArrayList<String> value){
	//		synchronized(statusMap){
	//			statusMap.put(key, value);
	//		}
	//	}

}
