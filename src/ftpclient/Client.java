package src.ftpclient;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    // Method to establish the connection with FTP server and login with user
    // details

    // Story 1
    public static boolean serverConnect(String server, int port) {

        // Attempting to connect with FTP server
        boolean reply = false;
        try {
            myClient.connect(server, port);
            System.out.print(myClient.getReplyString());

            // Check for reply code after attempting the connection

            int replyCode = myClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                myClient.disconnect();
                System.err.println("Connection failed. Server reply code: " + replyCode);
                System.exit(1);
            } else {
                reply = true;
            }
        } catch (IOException ex) {
            System.out.println("serverConnect(): Unexpected exception");
            ex.printStackTrace();
        }
        return reply;
    }

    // Attempting to login
    public static boolean serverLogin(String user, String pass) {
        boolean res = false;
        try {
            boolean success = myClient.login(user, pass);
            System.out.print(myClient.getReplyString());
            if (!success) {
                System.out.println("Incorrect Credentials");

            } else {
                System.out.println("Success!!Logged into server");
                res = true;
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

    
    // This is to list remote files in the the given path and is called in the other methods of this class
    public static void listRemoteFiles(String directory) {
        
        String printDir = (directory.equals("."))? "root":directory;
        FTPFile[] files;
        try {
        	System.out.println("List of Remote Files in "+ printDir +":");
            files = myClient.listFiles("/"+directory);
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
    // Story 3 - This is to list all the remote files in a path given by the user..
    // This method is accessed only by inputing  option 2.
    public static void listRemoteFilesToUser(){
		System.out.println("Please enter the path for which you need to see a listing. Leave empty for the current directory:");
		String path = console.nextLine();
		if(path.length()!=0){	
			if(checkDirectoryExistsOnPath(path)){
				listRemoteFiles(path);
			}
			else{
				System.out.println("The entered path does not exist on remote.");
			}
		}
		else{
			listRemoteFiles(".");
		}	
    	
    }
    
    // Story 4: Accessible to user only through the menu option
    public static void listLocalFilesToUser() {
    	System.out.println("Please enter the path for which you need to see a listing. Leave empty for the current directory:");
    	String path = console.nextLine();
    	
		if(path.length()!=0){
			listLocalFiles(path);
		}
		else{
			listLocalFiles(".");
		}
    }
    
    public static void listLocalFiles(String path) {
    	File curDir = new File(path);
    	DateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(curDir.exists() && curDir.isDirectory())
		{
	        File[] files = curDir.listFiles();
	
	        for (File file : files) 
	        {
	            String details = file.getName();
	            if (file.isDirectory()) {
	                details = "[" + details + "]";
	            }
	            details += "\t\t" + file.length();
                details += "\t\t" + dateFormater.format(file.lastModified());
	            System.out.println(details);
	        }
		}
		else
		{
			System.out.println("Could not list for the entered path. Please recheck and try again.");
		}
    }

    // Stories 5 & 6 --get file(s) from remote server
    static boolean checkFileExists(String filePath) throws IOException {
        int rc, j = 0;
        boolean exist = false;

        // check for nested
        rc = checkNested(filePath);
        if (rc == -1)
            return false; // illegal nested. So, file not exists.
        else if (rc == 0) { // not nested. So check in current dir
            String[] rfiles = myClient.listNames();
            exist = Arrays.asList(rfiles).contains(filePath);
            // attempt to change directory. If we can
            // then the the input is a directory. Abort!
            if (exist) {
            	if (myClient.changeWorkingDirectory(filePath)) {
            		System.out.printf ("Downloading directory %s is not suppported. ", filePath);
            		System.out.println("Treating it as file not exist on remote server!!! ");
            		exist = false; // So, pretend it doesn't exist!
            	}
            }
            return exist;
        } else { // nested path! walk the filePath
            String[] dirPath = null;
            dirPath = filePath.split("\\\\"); // Windows!
            if (dirPath.length == 1) {
                dirPath = filePath.split("/"); // Mac!
            }
            if (dirPath != null && dirPath.length > 0) {
                for (String dir : dirPath) {
                    try {
                        exist = myClient.changeWorkingDirectory(dir);
                        j++;
                        if (!exist) { // not a directory, is it a file?
                            String[] rfiles = myClient.listNames();
                            return Arrays.asList(rfiles).contains(dir);
                        }
                        if (j == dirPath.length) {
                            System.out.printf("Downloading directory, %s, is not supported. ", filePath);
                            System.out.println("Treating it as file not exist on remote server!!! ");
                            continue;
                        } else
                            continue;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false; // if we get here, file doesn't exist.
    }

    // 0 = not nested
    // 1 = nested
    // -1 = illegal nested
    static int checkNested(String f) {
        int ch1, ch2, flag = 0;

        ch1 = f.indexOf('\\');
        ch2 = f.indexOf('/');

        // nuts! both / and \ are used for path
        if (ch1 != -1 && ch2 != -1) {
            System.out.println("Both '/' and '\\' cannot be used to indicate nested directories.");
            flag = -1;
        } else if (ch1 != -1 || ch2 != -1) {
            flag = 1;
        } else {
            flag = 0;
        }
        return flag;
    }


    // Story 5 and 6, get a file and get multiple files.
    // NOTE: this only work for downloading files. Do not pass a directory!
    public static void fileDownload() {
    	
        System.out.println("Enter file name(s) to download:");
        String stringfiles = console.nextLine();
        String savedRemoteDir = null;
        try {
            savedRemoteDir = myClient.printWorkingDirectory();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String savedLocalDir = System.getProperty("user.dir");

        String[] files = stringfiles.split("[ ]+");
        OutputStream outputstream;

        // check for when input is all blank spaces.
        if (files.length == 0) {
            System.out.println("Filename cannot be blank.\n");
            return;
        }

        try {
            for (String remotefilename : files) {
                // here's the starting point: restore default working
                // directories
                try {
                    myClient.changeWorkingDirectory(savedRemoteDir);
                    System.setProperty("user.dir", savedLocalDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // check if filename is blank
                if (remotefilename.equals("") || remotefilename.trim().isEmpty()) {
                    System.out.printf("Filename %s cannot be blank.\n", remotefilename);
                    continue;
                }
                // check if file is present on remote directory
                else if (!checkFileExists(remotefilename)) {
                    System.out.printf("File %s not on remote server.\n", remotefilename);
                    continue;
                } else {
                    // each remotefilename could be a/file/in/this/form. "form"
                    // has to be a file, not directory.
                    String[] dirPath;
                    dirPath = remotefilename.split("\\\\");
                    int j = 0;
                    if (dirPath.length == 1) {
                    	dirPath = remotefilename.split("/");
                        File check = new File(dirPath[0]);
                        if (check.isDirectory()) {
                            System.out.printf("Downloading directory, %s, is not supported.", dirPath[0]);
                            continue;
                        }
                    }
                    if (dirPath.length > 0 && dirPath != null) {
                        for (String dir : dirPath) {
                            // does dir exists on local?
                            String name = null;
                            name = System.getProperty("user.dir") + "/" + dir;
                            File curFile = new File(name);

                            try {
                                if (!curFile.exists()) {
                                    // the last of the dirPath has to be a file,
                                    // don't attempt to mkdir
                                    if (j != (dirPath.length - 1)) {
                                        curFile.mkdir();
                                        // change present working dir on local
                                        // machine
                                        System.setProperty("user.dir", name);

                                        try {
                                            myClient.changeWorkingDirectory(dir);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        // No support for downloading directory!
                                    } else if (curFile.isDirectory()) {
                                        System.out.printf("Downloading directory, %s, is not supported.", curFile);
                                        continue;
                                    } else {
                                        // here the local and remote server have
                                        // the same directory structure
                                        // ready to download file
                                        // (a/file/in/this/form, where form is a
                                        // file)
                                        remotefilename = dir; // don't be
                                                              // confused. dir
                                                              // here is a file
                                                              // that we want
                                        outputstream = new BufferedOutputStream(new FileOutputStream(remotefilename));
                                        boolean success = myClient.retrieveFile(remotefilename, outputstream);
                                        outputstream.close();
                                        if (success) {
                                            // NOTE: for some reasons the
                                            // downloaded file is put in the
                                            // savedLocalDir
                                            // it's not in the expected working
                                            // directory. So, we need this hack
                                            // move the file to where we expect
                                            // it
                                            File old = new File(savedLocalDir + "/" + dir);
                                            if (old.renameTo(new File(System.getProperty("user.dir") + "/" + dir))) {
                                                System.out.printf("Download %s completed.\n", remotefilename);
                                            } else {
                                                System.out.printf(
                                                        "Download %s completed, but failed to move file to its proper location.\n",
                                                        remotefilename);
                                            }
                                        } else {
                                            System.out.printf("Download %s FAILED.\n", remotefilename);
                                        }
                                    }
                                } else if (curFile.isFile()) {
                                    System.out.printf("%s exists on local. Remove it first.", name);
                                    j++;
                                    continue;
                                } else {
                                    System.setProperty("user.dir", name);
                                }
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                            j++;
                        }
                    }
                }
            }
            // restore working directories
            try {
                myClient.changeWorkingDirectory(savedRemoteDir);
                System.setProperty("user.dir", savedLocalDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static boolean checkFileExistsLocally(String filePath) throws FileNotFoundException,IOException {

    	int rv;
        // check if filePath entered is nested
        rv = checkNested(filePath);
        // illegal nested. So,file not exists.
        if (rv == -1)
            return false;
       // if not illegal nested,check to see if given path exists
        else  { 
        	   if (new File(filePath).isFile()) {
                  return true;
            }
              else
            	  return false; 
      }
    }
    
    // story 7 & 8
    public static void fileUpload() {
 	    
         System.out.println("Enter file name(s) to upload:");
         String stringfiles = console.nextLine();

         String savedRemoteDir = null;
         try {
             savedRemoteDir = myClient.printWorkingDirectory();
         } catch (IOException e) {
             e.printStackTrace();
         }
         InputStream inputstream = null;
         String[] files = stringfiles.split("[ ]+");

         // check for when input is all blank spaces.
         if (files.length == 0) {
             System.out.println("Filename cannot be blank.\n");
             return;
         }
         try {
             for (String localFilePath : files) {
                 // check if filename is blank
                 if (localFilePath.equals("") || localFilePath.trim().isEmpty()) {
                     System.out.printf("Filename %s cannot be blank.\n", localFilePath);
                     continue;
                     // check if file exists locally
                 } else if (!checkFileExistsLocally(localFilePath)) {
                 	System.out.println(localFilePath + "  is not a valid path / filename");
                     continue;
                 } else {
                 	int rv,k=0;
                 	rv = checkNested(localFilePath);
                 	// not nested. Upload the file in remote home directory
                     if (rv == 0) { 
                     	inputstream = new FileInputStream(localFilePath);
                         boolean success = myClient.storeFile(localFilePath, inputstream);
                         inputstream.close();
                         if (success)
                             System.out.printf("Upload %s completed.\n", localFilePath);
                         else
                             System.out.printf("Upload %s FAILED.\n", localFilePath);
                     	 
                     } 
                    // nested path! walk the Path on remote server by changing the working directory recursively
                     else { 
                         String[] dirPath = null;
                         dirPath = localFilePath.split("\\\\");
                         if (dirPath.length == 1) {
                             dirPath = localFilePath.split("/");
                         }
                         
                         if (dirPath != null && dirPath.length > 0) {
                             for (String dir : dirPath) {
                                 try {
                                 	// check if it is a directory or file name
                                 	if (k!=(dirPath.length - 1)){
                                        boolean exist = myClient.changeWorkingDirectory(dir);
                                        // if there is no such directory exists, create directory
                                        if (!exist && dir.length() != 0) {
                                           try {
                                                boolean reply = myClient.makeDirectory(dir);
                                                if(reply){
                                                          exist = myClient.changeWorkingDirectory(dir + "/");
                                                }
                                           } catch (IOException e) {
                                                e.printStackTrace();
                                           }
                                        }
                                        // if directory exists, then change the working directory
                                        else {
                                            try {
                                     	        exist = myClient.changeWorkingDirectory(dir +"/");
                                     	        
                                            } catch (IOException e) {
                                                 e.printStackTrace();
                                            }
                                        }    
                                       k++;
                                      }
                                 	// if it's a file name 
                                 	else { 
                                 		inputstream = new FileInputStream(localFilePath);
                                 		String remoteFileName = dir;
                                         boolean success = myClient.storeFile(remoteFileName, inputstream);
                                         inputstream.close();
                                         if (success){
                                             System.out.printf("Upload %s completed.\n", dir);
                                         }else
                                             System.out.printf("Upload %s FAILED.\n", dir);
                                   	}
                                 } catch (IOException e) {
                                        e.printStackTrace();
                                 }
                              }
                         }
                     }
                 }
              // restore working directory on remote server
                 try {
                     myClient.changeWorkingDirectory(savedRemoteDir);
                     } catch (IOException e) {
                     e.printStackTrace();
                 }
              }
         }catch (IOException e) {
             e.printStackTrace();
      }
    }    
    // story 9 create directories on remote server
    public static void createDirectory() {
 
        System.out.println("Enter name of the directory to create:");
        String dirName = console.nextLine();

        Boolean flag = false;
        int ch1 = 0, ch2 = 0;
        int checks[] = new int[7];
        
         // The following code will check if the user tries to enter nested
         // directories example Test\java. and if there are nested directories
         // displays the appropriate messages if not directory is created
         
        if (dirName.length() == 0) {
            System.out.println("Directory name cannot be blank. Please try again");
        } else {
            try {
                ch1 = dirName.indexOf('\\');
                ch2 = dirName.indexOf('/');
                checks[0] = dirName.indexOf(':');
                checks[1] = dirName.indexOf('*');
                checks[2] = dirName.indexOf('?');
                checks[3] = dirName.indexOf('"');
                checks[4] = dirName.indexOf('<');
                checks[5] = dirName.indexOf('>');
                checks[6] = dirName.indexOf('|');

                for (int i = 0; i < checks.length; i++) {
                    if (checks[i] != -1)
                        flag = true;
                }

            } catch (StringIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

            if ((ch1 != -1 || ch2 != -1) && !flag) {
                if (ch1 != -1 && ch2 != -1) {
                    System.out.println(
                            "Both '/' and '\\' cannot be used to indicate nested directories! Please try again!");
                } else {

                    if (ch1 != -1) {
                        createNestedDirectory(dirName, "\\");

                    } else {
                        createNestedDirectory(dirName, "/");

                    }

                }
            } else if (flag) {
                System.out.println("Directory name cannot contain /:*?\"<>| Please try again");
            } else {

                 
                 // The following code is to create the directory in the current
                 // directory on remote server
                 
                try {
                    myClient.makeDirectory(dirName);
                    System.out.printf("%s %s", dirName, myClient.getReplyString());

                } catch (IOException e) {
                    e.printStackTrace();
                }
               
            }
        }
    }

    public static void createNestedDirectory(String path, String c) {
        boolean exist = false, reply = false;
        String[] dirPath;
        if (c == "\\") {
            dirPath = path.split("\\\\");

        } else {
            dirPath = path.split("/");

        }
        if (dirPath != null && dirPath.length > 0) {
            for (String dir : dirPath) {
                try {
                    exist = myClient.changeWorkingDirectory(dir);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!exist && dir.length() != 0) {
                    try {

                        reply = myClient.makeDirectory(dir);
                        System.out.printf("%s %s", dir, myClient.getReplyString());
                        if (reply) {
                            exist = myClient.changeWorkingDirectory(dir);

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            try {
                exist = myClient.changeWorkingDirectory("/");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
   

    // story 10 delete files from remote server
    public static void deleteRemoteFiles() {
       
        System.out.println("Enter the filepath to delete \n"
                + " if the file is in current directory: enter the filename \n");
        String filename = console.nextLine();
        Boolean replycode = false;
        if (filename.length() == 0 || filename.trim().isEmpty()) {
            System.out.println("File name cannot be blank. Please try again!");
        } else {
            try {
                replycode = myClient.deleteFile(filename);
            

            } catch (IOException e) {

                e.printStackTrace();
            }

            if (replycode) {
            	System.out.println("'"+filename+"'"+" deleted Successfully.");
            } else {
                System.out.println("'"+filename+"'" +" not deleted.Please try again");
            }
        }

    }

    
	    
   
    // Story 13 --rename remote file and directories
    // example:give path as--- parent/child/child0(if we have to rename child0
    // to child7)
    // rename to ---- parent/child/child7
    // if filename give extension(example: file1.txt)
    public static void renameRemoteFileandDirectories() {

        try {

            System.out.println("Enter name of the file/directory to rename:");
            String oldname = console.nextLine();
            // if no directory/filename name is given ie, simply enter key is
            // pressed
            if (oldname.length() == 0) {
                System.out.println("File/Directory name cannot be blank. Please try again!!!");

            }

            else {
                boolean success = false;
                System.out.println("Enter new File/Directory name(Please enter the full path name of the file/Directory):\n");
                String newname = console.nextLine();
                // if no directory/filename name is given ie, simply enter key
                // is pressed
                if (newname.length() == 0) {
                    System.out.println("File/Directory name cannot be blank. Please try again!!!");

                }

                success = myClient.rename(oldname, newname);
                if (success) {
                    System.out.println(oldname + " was successfully renamed to: " + newname);
                } else {
                    System.out.println("Failed to rename: remote " + oldname);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    // story 14 Rename local files and directories
    // example:give path as :--- localdirectory/file.txt
    // rename to:--localdirectory/file4.txt
    public static void renameLocalFileandDirectories() {

        try {
            System.out.println("Enter name of the file/directory to rename:");
            String oldname = console.nextLine();
            // if no directory/filename name is given ie, simply enter key is
            // pressed.
            if (oldname.length() == 0) {
                System.out.println("File/Directory name cannot be blank. Please try again!!!");

            } else {
                boolean success1 = false;
                System.out.println("Enter new local File/Directory name(Please enter the full path name of the file/Directory):\n");
                String newname = console.nextLine();

                // if no directory/filename name is given ie, simply enter key
                // is pressed.
                if (newname.length() == 0) {
                    System.out.println("File/Directory name cannot be blank. Please try again!!!");

                }

                File oldfile = new File(oldname);
                File newfile = new File(newname);
                success1 = oldfile.renameTo(newfile);

                if (success1) {
                    System.out.println(oldname + " was successfully renamed to: " + newname);
                } else {
                    System.out.println("Failed to rename local " + oldname);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean checkDirectoryExistsOnPath(String cdpath){

        Boolean exist = false;
        try {
            exist = myClient.changeWorkingDirectory(cdpath);
            myClient.changeWorkingDirectory("/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exist;
    }
    
    public static void main(String[] args) {

        String server = "71.237.177.239";
        int port = 21;
        boolean connectres = false;
        boolean loginres = false;

        // Call to establish the connection with FTP server
        myClient = new FTPClient();

        connectres = serverConnect(server, port);
        if (!connectres)
            System.exit(0);

        // Getting login details from the user.
        // 3 invalid trials
        for (int i = 1; i <= 3; i++) {
            System.out.println("Enter the username");
            String user = console.nextLine();
            while (user.equals("") || user.trim().isEmpty()) {
                System.out.println("Username cannot be blank");
                System.out.println("Enter the username");
                user = console.nextLine();
            }

            System.out.println("Enter the password");
            String pass = console.nextLine();
            while (pass.equals("") || pass.trim().isEmpty()) {
                System.out.println("Password cannot be blank");
                System.out.println("Enter the password");
                pass = console.nextLine();
            }

            loginres = serverLogin(user, pass);
            if (loginres)
                break;
            else if (i == 3)
                System.exit(0);
            else
                continue;
        }

        // This is a protocol needed for downloading files
        try {
            myClient.setFileType(FTP.BINARY_FILE_TYPE);
            myClient.enterLocalPassiveMode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean notquit = true;
        while (loginres && notquit) {
            System.out.println("\nFTP Server Operation: \n" +
            		"\t1. Logoff \n" +
            		"\t2. List Files in a Directory \n" +
            		"\t3. Rename File or Directory \n" +
            		"\t4. Create Directory \n" + 	// our story doesn't care for "files"
            		"\t5. Delete File  \n" +
            		
            		
            		
            		"FTP Client Operation: \n" +
            		"\t11. List Files in a Directory \n" +
            		"\t12. Rename File or Directory \n" +
            		"\t13. File Upload \n" +
            		"\t14. File Download \n\n");
 
            String choice = console.nextLine();
            switch (choice) {
            case "1":
            	logoff();
                break;

            case "2":
            	listRemoteFilesToUser(); 	// files and directory
            	
                break;

            case "3":
            	renameRemoteFileandDirectories();
                break;

            case "4":
            	createDirectory();			// don't care about "FILE"
                break;
                
            case "5":
            	deleteRemoteFiles();               
                break;
                
           
     
                
            case "11":
            	listLocalFilesToUser(); 	// files and directories
                break;
                
            case "12":
            	renameLocalFileandDirectories();
                break;

            case "13":
            	fileUpload();
                break;
                
            case "14":
            	fileDownload();
            	break;

            default:
                System.out.println("Did not understand your selection.");
            }
        }
        System.out.println("Done");
        console.close();
        System.exit(0);

    }

}