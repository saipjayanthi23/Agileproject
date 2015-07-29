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
			
			Boolean replycode = null,flag= false;
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
		    
		    	if(ch!=-1 && !flag)
		    	{
		    		//System.out.println("Nested Directories are not supported! Please try again");
		    		createNestedDirectory(dirName);
		    		listRemoteFiles();
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
		
		// create nested directories		
		
		public static void createNestedDirectory( String path)
		{
			boolean exist = false,reply=false;
		    //Get the name of the directory to be created from user
//			System.out.println ("Enter name of the directory to create:");
//			String path = console.nextLine();
					
			String[] dirPath = path.split("\\\\");
			if (dirPath != null && dirPath.length > 0) 
			{
				for(String dir:dirPath)
				{
					try 
					{
						exist = myClient.changeWorkingDirectory(dir);
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				
					if(!exist && dir.length()!=0)
					{
						try 
						{
								reply = myClient.makeDirectory(dir);
								System.out.print(myClient.getReplyString());
								if(reply)
								{
									exist = myClient.changeWorkingDirectory(dir);
								}
								
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if(!reply)
						break;
				}
				try 
				{
					exist = myClient.changeWorkingDirectory("/");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
		
		//story 12 delete directory from remote server
		//removing single directory and multiple directories are separate method to distinguish
		//between [Test folder] and [Test] [Folder]. To remove the first folder we need to call
		//removeRemoteDirectory() but to delete multiple directories [Test] and [Folder] we need 
		//to call removeRemoteDirectories()

			public static void removeRemoteDirectory()
			{
				//Get the name of the directory to be deleted from user
				System.out.println("Enter the name of the directory to delete \n");
				String filename = console.nextLine();
				Boolean replycode=null;
				if(filename.length()==0)
				{
					System.out.println("Directory name cannot be blank. Please try again!");
				}
				else
				{
					try
					{
						replycode = myClient.removeDirectory(filename);
						System.out.println(myClient.getReplyString());

					}catch(IOException e){

						e.printStackTrace();
						System.out.println("removeRemoteDirectory(): Unexpected exception");
					}

					if(replycode)
					{
						listRemoteFiles();
						System.out.printf("Directory %s deleted Successfully.",filename);
					}
					else
					{
						System.out.printf("Directory %s not deleted.Please try again",filename);
					}
				}

			}
			//story12 for multiple directories
			




			public static void removeRemoteDirectories() {

				System.out.println ("Enter directory names to remove:");
				String stringDirectory = console.nextLine();


				String[] directories = stringDirectory.split("[ ]+");
				// check for when input is all blank spaces.
				if (directories.length == 0) {
					System.out.println ("Directory name cannot be blank.\n");
					return;
				}


				try {
					for (String remotedirectoryname :directories) {
						// check for when input is all blank spaces.
						if (remotedirectoryname.equals("")||remotedirectoryname.trim().isEmpty()) {
							System.out.println ("Directory name cannot be blank.\n");
							continue;

						}
						else if(!checkDirectoryExists(remotedirectoryname)){
							System.out.printf("Directory %s not on remote server.\n", remotedirectoryname);
							continue;
						}

						Boolean deleted= null;

						deleted = myClient.removeDirectory(remotedirectoryname);


						if (deleted) {
							System.out.printf("The directory %s was removed successfully.\n",remotedirectoryname);
							System.out.println(myClient.getReplyString());
						} else {
							System.out.printf("The directory %s cannot be deleted.\n",remotedirectoryname);
							System.out.println(myClient.getReplyString());

						}
					}
				} catch (IOException e) {

					
					System.out.println("removeRemoteDirectories(): Unexpected exception");
					e.printStackTrace();
				}

			}

			

				public static boolean checkDirectoryExists(String filePath) throws IOException {
					//boolean directoryExists = myClient.changeWorkingDirectory(filePath);
					boolean directoryExists=false;
					FTPFile[] subFiles = myClient.listFiles();
					for  (FTPFile aFile : subFiles) 
					{
						String details = aFile.getName();
						if ((aFile.isDirectory())&&details.equals(filePath)) 
						{
		                 
							directoryExists=true;
						
		                }
						
					}
					return directoryExists;

				}


		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
	
		//Stories 13 & 14
		public static void rename(String mode) {
			
			System.out.println ("Enter name of the file/directory to rename:");
			String oldname = console.nextLine();
			boolean success = false;
			String newname = "";
			
			
			if(oldname.length()==0)
			{
				System.out.println("File/Directory name cannot be blank. Please try again!!!");
				
			}	
			else
			{
					
			try {
				if(mode.equals("remote"))
				{
					
										    				
					if(!checkFileExists(oldname))
					{
						System.out.printf("File/Directory %s does not exist on remote server.\n", oldname);	
						listRemoteFiles();
		        	}
					
					else{
						//File oldfile = new File(oldname);
						//File newfile = new File(newname);
						//success = oldfile.renameTo(newfile);
						System.out.println ("Enter new name:");
						newname = console.nextLine();
						if(newname.length()==0)
						{
							System.out.println("Remote File/Directory name cannot be blank. Please try again!!!");
							listRemoteFiles();
						}
						
					
							boolean success1 = myClient.rename(oldname, newname);
								           
						
						if(success1) 
						{
			                System.out.printf(oldname + " was successfully renamed to: " + newname);
			                
			                listRemoteFiles();
			            }
						else
		                	{
		                	System.out.println("File/Directory name cannot contain special characters.Please try again :" + newname);
		                	
		                listRemoteFiles();
		                	}
						
						}
					
					
				    }
				
				else if(mode.equals("local"))
				{
							
					if(!checkFileExistsLocally(oldname))
					{
						System.out.printf("File/Directory %s does not exist in local directory.\n", oldname);	
						
						listLocalFiles();
		        	}
					else
					{
						System.out.println ("Enter new name:");
						newname = console.nextLine();
						
						if(newname.length()==0)
						{
							System.out.println("Local File/ Directory name cannot be blank. Please try again");
						}
						File oldfile = new File(oldname);
						File newfile = new File(newname);
						success = oldfile.renameTo(newfile);
			if (success) 
					{
	                System.out.println(oldname + " was successfully renamed to: " + newname);
	                listLocalFiles();
					}
	                else
	                {
	                	System.out.println("File/Directory name cannot contain special characters.Please try again :" + newname);
	                	
	                listLocalFiles();
					}
				}
				}	
				
				} 
			catch (IOException e) {		
				
								e.printStackTrace();
									}
		}
		
			
		    
		}

	// change directory option 12.. can remove if not needed. added for testing story 9!!
		
		public static void  changeDirectory()
		{
			System.out.println("enter the directory name or path(/ to go to root directory):");
			String cdpath = console.nextLine();
			Boolean exist=false;
			try 
			{
				exist = myClient.changeWorkingDirectory(cdpath);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(exist)
			{
				System.out.println("Directory changed to "+ cdpath);
			}
			else
			{
				System.out.println("Directory cannot be changed. Please try again");
			}
		}
		
		
	public static void main(String[] args) {
	        
		   // String server = "10.200.27.150";
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
	        			+ "8. Delete single directory on remote server \n"
	        			+ "9. Delete multiple directories on remote server \n"
	        			+ "10.Rename file/directory on local machine \n"
	        			+ "11.Rename file/directory on remote server \n"
	        			+ "12. change directory (for create nested directories) \n"
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
	        	case "8":  removeRemoteDirectory();
	        				break;
	        	case "9": removeRemoteDirectories();
							break;
    						
	        	case"10":	rename("local");
	        				break;
	        				
	        	case"11":	rename("remote");
							break;
							
	        	case "12": changeDirectory();
	        	           break;
	        	        				
            	default: 	System.out.println("Did not understand your selection.");
	        	
	        	}
	        }
	        System.out.println("Done");
	        console.close();
	        System.exit(0);
	         
	}


}