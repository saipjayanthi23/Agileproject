package src.ftpclient;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
public class Client {
	static FTPClient myClient;
	
	// Method to establish the connection with FTP server and login with user details
	
	
	//Story 1
	//public static void serverLogin(FTPClient myClient,String server,int port,String user,String pass){
	public static void serverLogin(String server,int port,String user,String pass){
        
		// Attempting to connect with FTP server
		
		try {
           myClient.connect(server, port);
           System.out.print(myClient.getReplyString());

        // Check for reply code after attempting the connection
           
           int replyCode = myClient.getReplyCode();
           if (!FTPReply.isPositiveCompletion(replyCode)) {
        	  myClient.disconnect(); 
              System.err.println("Connection failed. Server reply code: " + replyCode);
              System.exit(1);
           }
        
        // Attempting to login
           
           boolean success = myClient.login(user, pass);
           System.out.print(myClient.getReplyString());
           if (!success) {
               System.out.println("Authentication failed. Could not login into server");
               return;
           } else {
               System.out.println("Success!!Logged into server");
           }
       
		} catch (IOException ex) {
			System.out.println("serverLogin(): Unexpected exception");
           ex.printStackTrace();
       }
		
	}
	
	// Story 2
	public static void logoff() {
		
		try {
			if (myClient.isConnected())
				myClient.disconnect();
			else
				myClient.logout();
			System.exit(0);
		} catch (FTPConnectionClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
		 	e.printStackTrace();
		}
	}
        
	//Story 3
	public static void listRemoteFiles(){
		System.out.println("listRemoteFiles");
		// lists files and directories in the current working directory
		// Can't test without a connection
		FTPFile[] files;
		try {
			
			files = myClient.listFiles();
			// iterates over the files and prints details for each
			DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			for (FTPFile file : files) {
			    String details = file.getName();
			    if (file.isDirectory()) {
			        details = "[" + details + "]";
			    }
			    details += "\t\t" + file.getSize();
			    details += "\t\t" + dateFormater.format(file.getTimestamp().getTime());
			    System.out.println(details);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("listRemoteFiles(): Unexpected exception");
			e.printStackTrace();
		}
	}
	
	//Story 4
	public static void listLocalFiles(){
		File curDir = new File(".");
		File[] files = curDir.listFiles();
	
		for (File file : files) {
		    String details = file.getName();
		    if (file.isDirectory()) {
		        details = "[" + details + "]";
		    }	    
		    System.out.println(details);
		}
	}
	
	public static void main(String[] args) {
	        String server = "linux.cs.pdx.edu";
	        int port = 21;
	        Scanner console = new Scanner(System.in);
	       
	        //Getting login details from the user
	        
	        System.out.println("Enter the username");
	        String user = console.nextLine();
	        
	        System.out.println("Enter the password");
	        String pass = console.nextLine();
	        
	        myClient = new FTPClient();
	        
	        //Call to establish the connection with FTP server and login 
	        
	        serverLogin(server,port,user,pass);
	        boolean notquit = true;
	        while(notquit){
	        	System.out.println("\nPick an option:\n1. List files and directories on remote.\n"
	        			+ "2. List files and directories on local system (current directory.)\n"
	        			+ "3. Logoff from server.\n"
	        			+ "Q. Quit.");
	        	String choice = console.nextLine();
	        	switch(choice){
	        	case "1": 	listRemoteFiles();
	        				break;
	        	
	        	case "2": 	listLocalFiles();
							break;
	        				
	        	case "3":  	logoff();
	        				break;
	        				
	        	case "Q":
	        	case "q": 	notquit = false;
	        				logoff();
	        				break;
	        				
	        	default: 	System.out.println("Did not understand your selection.");
	        	
	        	}
	        }
	        System.out.println("Done");
	        console.close();
	        System.exit(0);
	         
	}
}
