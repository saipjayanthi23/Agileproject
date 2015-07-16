package src.ftpclient;
import java.io.File;
import java.io.FileOutputStream;
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
	public static boolean serverConnect(String server,int port){
        
		// Attempting to connect with FTP server
		boolean reply=false;
		try {
           myClient.connect(server,port);
           System.out.print(myClient.getReplyString());

        // Check for reply code after attempting the connection
           
           int replyCode = myClient.getReplyCode();
           if (!FTPReply.isPositiveCompletion(replyCode)) {
        	  myClient.disconnect(); 
              System.err.println("Connection failed. Server reply code: " + replyCode);
              System.exit(1);
           }
           else{
        	   reply=true;
           }
		} catch (IOException ex) {
			System.out.println("serverConnect(): Unexpected exception");
           ex.printStackTrace();
       }
	   return reply;
	}	
       
	// Attempting to login
	public static boolean serverLogin(String user,String pass){ 
		boolean res=false;
        try {
		     boolean success = myClient.login(user, pass);
             System.out.print(myClient.getReplyString());
             if (!success) {
                System.out.println("Incorrect Credentials");
                
             } else {
               System.out.println("Success!!Logged into server");
               res=true;
             }
    	} catch (IOException ex) {
			System.out.println("serverLogin(): Unexpected exception");
           ex.printStackTrace();
       }
	    return res;       
	}   
		
		
	// Story 2
	public static void logoff() {
		
		try {
			if (myClient.isConnected())
				myClient.disconnect();
			
			else
				myClient.logout();
			
		  System.out.println("Successfully Logged out from server!!!");	
		  //System.exit(0);
		} catch (FTPConnectionClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
		 	e.printStackTrace();
		}
	}
        
	//Story 3
	public static void listRemoteFiles(){
		System.out.println("list Remote Files");
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
	
	
	
	
	//Story 5
	//get file from remote server
	public static void fileDownload(){
		FileOutputStream fileOutputstream = null;
		Scanner scanner = new Scanner(System.in);
		try{
			System.out.println("Enter the filename you want to download");
	        
			String remotefilename = scanner.nextLine();
			fileOutputstream = new FileOutputStream(remotefilename);
            myClient.retrieveFile("/" + remotefilename, fileOutputstream);
		}catch(IOException e) {
            e.printStackTrace();
		}finally {
            try {
                if (fileOutputstream != null) {
                	fileOutputstream.close();
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		scanner.close();
	}
	
	
	public static void main(String[] args) {
	        
		    String server = "71.237.177.239";
	        
		    int port = 21;
	        boolean connectres=false;
	        boolean loginres=false;
	        
	        Scanner console = new Scanner(System.in);
	        
	      //Call to establish the connection with FTP server
	        myClient = new FTPClient();
	        
	        connectres=serverConnect(server,port);
	        if(!connectres)
	        	System.exit(0);
	        
	        //Getting login details from the user. 
	        for (int i=1;i<=3;i++){
	        	System.out.println("Enter the username");
		        String user = console.nextLine();
		        while(user.equals("") || user.trim().isEmpty()){
		        	System.out.println("Username cannot be blank");
		        	System.out.println("Enter the username");
			        user = console.nextLine();
		        }
		        
		        System.out.println("Enter the password");
		        String pass = console.nextLine();
		        while(pass.equals("") || pass.trim().isEmpty()){
		        	System.out.println("Password cannot be blank");
		        	System.out.println("Enter the password");
			        pass = console.nextLine();
		        }
		        
		        loginres= serverLogin(user,pass);
		        if(loginres)
		          break;
		        else
		          if (i==3)
		        	  System.exit(0);
		          else 
		        	  continue;
		     }
		           
	        boolean notquit = true;
	        while(loginres && notquit){
	        	System.out.println("\nPick an option:\n1. List files and directories on remote.\n"
	        			+ "2. List files and directories on local system (current directory.)\n"
	        			+ "3. Logoff from server.\n"
	        			+ "4. Get file from remote server.\n"
	        			+ "Q. Quit.");
	        	String choice = console.nextLine();
	        	switch(choice){
	        	case "1": 	listRemoteFiles();
	        				break;
	        	
	        	case "2": 	listLocalFiles();
							break;
	        				
	        	case "3":  	logoff();
	        				break;
	        				
	        	case "4":  fileDownload();
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
