package ftpclient;
import java.io.IOException;
import java.util.Scanner;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
public class Client {

	
	// Method to establish the connection with FTP server and login with user details
	
	public static void serverLogin(FTPClient myClient,String server,int port,String user,String pass){
        
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
           System.out.println("Oops! Unknown issue");
           ex.printStackTrace();
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
	        
	        FTPClient myClient = new FTPClient();
	        
	        //Call to establish the connection with FTP server and login 
	        
	        serverLogin(myClient,server,port,user,pass);
	        //close console 
	        console.close();
	         
	}
}
