package org.my.facebookalgorithm;

import java.awt.Desktop;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import javax.swing.JOptionPane;

import org.cishell.framework.CIShellContext;
import org.cishell.framework.algorithm.Algorithm;
import org.cishell.framework.algorithm.AlgorithmExecutionException;
import org.cishell.framework.data.BasicData;
import org.cishell.framework.data.Data;
import org.cishell.framework.data.DataProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.my.facebookalgorithm.api.FaceBookAPI;
import org.my.facebookalgorithm.api.FriendsWithFriendsAPI;
import org.my.facebookalgorithm.api.MyFriendsAPI;
import org.my.facebookalgorithm.facade.Facade;
import org.my.facebookalgorithm.utilities.DownloadHandler.InvalidUrlException;
import org.my.facebookalgorithm.utilities.DownloadHandler.NetworkConnectionException;
import org.osgi.service.log.LogService;

public class facebookAlgo implements Algorithm {
    private Data[] data;
    private Dictionary parameters;
    private CIShellContext ciShellContext;
    private LogService logger;
    private List<FriendsPair> pairList;
    private Facade facade;
    
    public facebookAlgo(Data[] data,
    				  Dictionary parameters,
    				  CIShellContext ciShellContext) {
    	 this.data = data;
         this.parameters = parameters;
         this.ciShellContext = ciShellContext;
         this.logger = (LogService) ciShellContext.getService(LogService.class
     			.getName());
         
         pairList = new ArrayList<FriendsPair>();
         facade = new Facade(this.logger);
    }

    public Data[] execute() throws AlgorithmExecutionException {
    	this.logger.log(LogService.LOG_INFO, "Opening Facebook login page");
				
		
        String token = facade.getAccessToken();
		this.logger.log(LogService.LOG_INFO, "Access Token: "+ token);
		String data = "access_token="+token;
		String myName="";
		try {
			myName = facade.getMyName(data);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONObject obj;
		try {
		    FaceBookAPI fb = new MyFriendsAPI();
			obj = new JSONObject(fb.callAPI(data, ""));	
        
			JSONArray jsonArray = obj.getJSONArray("data");
			int len = jsonArray.length();
			for (int i = 0; i < len; i++) {
			                    JSONObject currentResult = jsonArray.getJSONObject(i);
			                    String friendOnename = currentResult.getString("name");
			                    String id = currentResult.getString("id");
			                    FriendsPair fp = new FriendsPair(myName, friendOnename);
			                    pairList.add(fp);
			                    
			                    //this.logger.log(LogService.LOG_INFO, "Name = "+friendOnename);
			                    //this.logger.log(LogService.LOG_INFO, "id = "+id);
			                  //code for friends of friends   
			                    FaceBookAPI ff = new FriendsWithFriendsAPI();
			                    String string =ff.callAPI(data, id);
			                    if(string.equals("No data") || string.isEmpty()) continue;
			                    JSONObject ffobj = new JSONObject(string);			                
			        			JSONArray friensArray = ffobj.getJSONArray("data");
			        		
			        			for (int j = 0; j < friensArray.length(); j++)
			        			{
				                    JSONObject innerResult = friensArray.getJSONObject(j);
				                    String friendTwoName = innerResult.getString("name");
				                    //this.logger.log(LogService.LOG_INFO, "friends friendName = "+friendOnename);
				                    this.logger.log(LogService.LOG_INFO, "friends friendName = "+friendTwoName);
				                    
				                    pairList.add(new FriendsPair(friendOnename,friendTwoName));
			        			}			                    	                 
			 }				
		} catch (JSONException e) {
			logger.log(LogService.LOG_INFO, e.getMessage());
		}
        try {
			facade.writeCSVFile(pairList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log(LogService.LOG_INFO, e.getMessage());
		}
		return null;
    }
 }