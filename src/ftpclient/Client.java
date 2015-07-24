package src.ftpclient;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;


public class Client {
	static FTPClient myClient;
	static Scanner console = new Scanner(System.in);
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
		  System.exit(0);
		} catch (FTPConnectionClosedException e) {
			e.printStackTrace();
		} catch (IOException e) {
		 	e.printStackTrace();
		}
	}
        
	//Story 3
	public static void listRemoteFiles(){
		System.out.println("List of Remote Files:");
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
	
	
	
	
	//Stories 5 & 6 --get file(s) from remote server
	
	static boolean checkFileExists(String filePath) throws IOException {
		String[] files = myClient.listNames();
		return Arrays.asList(files).contains(filePath);
	}
	
	public static void fileDownload(){
		
		System.out.println ("Enter file name to download:");
		String stringfiles = console.nextLine();

		OutputStream outputstream = null;
		String[] files = stringfiles.split("[ ]+");
		
		// check for when input is all blank spaces.
		if (files.length == 0) {
			System.out.println ("Filename cannot be blank.\n");
			return;
		}
		try{
			for (String remotefilename : files) {
				// check if filename is blank
				if(remotefilename.equals("") || remotefilename.trim().isEmpty()){
			        System.out.printf("Filename %s cannot be blank.\n", remotefilename);
			        continue;
				}
				//check if file is present on remote directory
				else if(!checkFileExists(remotefilename)){
					System.out.printf("File %s not on remote server.\n", remotefilename);
					continue;
	        		}
				else  {
					outputstream = new BufferedOutputStream(new FileOutputStream(remotefilename));
					boolean success = myClient.retrieveFile(remotefilename, outputstream);
					outputstream.close();
					if (success)
						System.out.printf ("Download %s completed.\n", remotefilename);
					else
						System.out.printf ("Download %s FAILED.\n", remotefilename);
				}
			}
		} catch(IOException e) {
            e.printStackTrace();
		}finally {
			
            try {
            	          
                if (outputstream != null) {
                	outputstream.close();
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
           
        }
		
	}

	//Stories 7 & 8 put file(s) on remote server
	
	static boolean checkFileExistsLocally(String filePath) throws IOException {
	   
		File curDir = new File(".");
		boolean check = new File(curDir,filePath).exists();
		return check;
	}
	 
	public static void fileUpload() {
		
		System.out.println ("Enter file name to upload:");
		String stringfiles = console.nextLine();
	
	   InputStream inputstream = null;
		String[] files = stringfiles.split("[ ]+");

		// check for when input is all blank spaces.
		if (files.length == 0) {
			System.out.println ("Filename cannot be blank.\n");
			return;
		}
		try{
			for (String localfilename : files) {
				//check if filename is blank
		    	if(localfilename.equals("") || localfilename.trim().isEmpty()){
			       System.out.printf("Filename %s cannot be blank.\n", localfilename);
			       continue;
			    // check if file exists locally    
				}else if(!checkFileExistsLocally(localfilename)){
					System.out.printf("File %s not on local machine.\n", localfilename);
					continue;
		       	}
				else  {
					inputstream = new FileInputStream(localfilename);
					boolean success = myClient.storeFile(localfilename, inputstream);
					inputstream.close();
					if (success)
			        	System.out.printf ("Upload %s completed.\n", localfilename);
					else
						System.out.printf ("Upload %s FAILED.\n", localfilename);
					}
				}
			} catch(IOException e) {
	            e.printStackTrace();
			}			
		}
	
	//story 9  create directories on remote server
		public static void createDirectory()
		{
			//Get the name of the directory to be created from user
			System.out.println ("Enter name of the directory to create:");
			String dirName = console.nextLine();
			
			Boolean replycode = null,flag=null;
			int ch = 0;
			int checks[]= new int[8];
			/* 
			 * The following code will check if the user tries to enter nested directories
			 * example Test\java. and if there are nested directories displays the appropriate messages
			 * if not directory is created
			 */
		    if(dirName.length()==0)
			{
				System.out.println("Directory name cannot be blank. Please try again");
			}
		    else
		    {
		    	try
				{
					ch = dirName.indexOf('\\');
					checks[0] =dirName.indexOf('/') ;
					checks[1] = dirName.indexOf(':');
					checks[2] =  dirName.indexOf('*');
					checks[3] =  dirName.indexOf('?');
					checks[4] =  dirName.indexOf('"');
					checks[5] =  dirName.indexOf('<');
					checks[6] =  dirName.indexOf('>');
					checks[7] =  dirName.indexOf('|');
					
					for(int i=0;i<checks.length;i++)
					{
						if (checks[i]!=-1)
							flag=true;
					}
					
									
				}catch(StringIndexOutOfBoundsException e){
					e.printStackTrace();				
				}		
		    
		    	if(ch!=-1)
		    	{
		    		System.out.println("Nested Directories are not supported! Please try again");
		    	}
		    	else if(flag )
		    	{
		    		System.out.println("Directory name cannot contain /:*?\"<>| Please try again");
		    	}
		    	else
		    	{
				
				/*
				 * The following code is to create the directory in the current directory on remote server
				 */
		    		try
		    		{
		    			replycode = myClient.makeDirectory(dirName);
		    			System.out.print(myClient.getReplyString());
					
		    		}catch(IOException e)
		    		{
		    			e.printStackTrace();
		    		}	
		    		if (replycode)
		    		{
		    			listRemoteFiles();
						System.out.println("Directory created Successfully: ");
				
		    		}
		    		else
					{
		    			System.out.println("Failed to create directory");
					}
		    	}
		    }
		}
		
		//story 10 delete files from remote server
		public static void deleteRemoteFiles()
		{
			//Get the name of the directory to be created from user
			System.out.println("Enter the name of the file to delete "
					+ "(with the path eg:\\test2\\ftp ,where ftp is the file to delete.\n"
					+ " if the file is in current directory just enter the filename \n");
			String filename = console.nextLine();
			Boolean replycode=null;
			if(filename.length()==0)
			{
				System.out.println("File name cannot be blank. Please try again!");
			}
			else
			{
				try
				{
					replycode = myClient.deleteFile(filename);
					System.out.println(myClient.getReplyString());
					
				}catch(IOException e){
					
					e.printStackTrace();
				}
				
				if(replycode)
				{
					listRemoteFiles();
					System.out.println("File deleted Successfully.");
				}
				else
				{
					System.out.println("File not deleted.Please try again");
				}
			}
			
		}
	
		//Stories 13 & 14
		public static void rename(String mode) {
			System.out.println ("Enter name of the file/directory to rename:");
			String oldname = console.nextLine();
			
			boolean success = false;
			String newname = "";
			
			try {
				if(mode.equals("remote")){
					if(!checkFileExists(oldname))
					{
						System.out.printf("File %s not on remote server.\n", oldname);	
		        	}
					else
					{
						System.out.println ("Enter new name:");
						newname = console.nextLine();
						success = myClient.rename(oldname, newname);
					}
				}
				else if(mode.equals("local")){
					if(!checkFileExistsLocally(oldname))
					{
						System.out.printf("File %s does not exist in current directory.\n", oldname);	
		        	}
					else
					{
						System.out.println ("Enter new name:");
						newname = console.nextLine();
						
						File oldfile = new File(oldname);
						File newfile = new File(newname);
						success = oldfile.renameTo(newfile);
					}
				}
			} catch (IOException e) {
				System.out.println("rename(): Unexpected exception");
				e.printStackTrace();
			}
			
			
			if (success) 
			{
                System.out.println(oldname + " was successfully renamed to: " + newname);
            }
			else 
            {
                System.out.println("Failed to rename: " + newname);
            }
		}
		
	public static void main(String[] args) {
	        
		    String server = "71.237.177.239";
	        
		    int port = 21;
	        boolean connectres=false;
	        boolean loginres=false;
	        
	        
	        
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
		    
	        // Need this for downloading
	        try {
	        	myClient.setFileType(FTP.BINARY_FILE_TYPE);
	        	myClient.enterLocalPassiveMode();
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
	        
	        boolean notquit = true;
	        while(loginres && notquit){
	        	System.out.println("\nPick an option:\n1. List files and directories on remote.\n"
	        			+ "2. List files and directories on local system (current directory.)\n"
	        			+ "3. Logoff from server.\n"
	        			+ "4. Get file(s) from remote server. (download) \n"
	        			+ "5. Put file(s) on remote server. (upload) \n"
	        			+ "6. Create directory on remote server. \n"
	        			+ "7. Delete files on remote server. \n"
	        			+ "10.Rename file/directory on local machine \n"
	        			+ "11.Rename file/directory on remote server \n"
	        			);

	        	String choice = console.nextLine();
	        	switch(choice){
	        	case "1": 	listRemoteFiles();
	        				break;
	        	
	        	case "2": 	listLocalFiles();
							break;
	        				
	        	case "3":  	logoff();
	        				break;
	        				
	        	case "4":	fileDownload();
	        				break;
	        	case "5":	fileUpload();
		        			break;	
		        			
	        	case "6":   createDirectory();
    						break;
    						
	        	case "7":  deleteRemoteFiles();
	        	           break;
    						
	        	case"10":	rename("local");
	        				break;
	        				
	        	case"11":	rename("remote");
							break;
	        				
            	default: 	System.out.println("Did not understand your selection.");
	        	
	        	}
	        }
	        System.out.println("Done");
	        console.close();
	        System.exit(0);
	         
	}


}