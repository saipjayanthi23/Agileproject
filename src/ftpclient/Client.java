package src.ftpclient;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
public class Client {
	static FTPClient myClient;
	
	// Method to establish the connection with FTP server and login with user details
	
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
	
	public static void main(String[] args) {
	        String server = "www.yourserver.net";
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
	        	System.out.println("Pick an option:\n1. List files and directories on remote.\nQ. Quit.");
	        	String choice = console.nextLine();
	        	switch(choice){
	        	case "1": 	listRemoteFiles();
	        				break;
	        				
	        	//case "2": //do something
	        	//			break;
	        				
	        	case "Q":
	        	case "q": 	notquit = false; 
	        				break;
	        				
	        	default: 	System.out.println("Did not understand your selection.");
	        	
	        	}
	        }
	        System.out.println("Done");
	         
	}
}
