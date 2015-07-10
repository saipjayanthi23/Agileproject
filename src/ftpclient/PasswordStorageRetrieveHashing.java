package ftpclient;

import java.util.*;
import java.security.*;

import javax.swing.JOptionPane;

public class PasswordStorageRetrieveHashing {
	
	Map< String,String> UserPasswordDatabase = new HashMap< String,String>();
	public static final String SALT= "abcde";

	public static void main(String args[]){
		PasswordStorageRetrieveHashing example= new PasswordStorageRetrieveHashing();
		
		
		JOptionPane.showMessageDialog(null, "**************USER SIGNUP*****************");
		String inputusername,inputuserpassword;
	
		inputusername=JOptionPane.showInputDialog(null,"enter a username for signup");
		inputuserpassword=JOptionPane.showInputDialog(null,"enter your passwordfor signup");
		
		example.signup(inputusername,inputuserpassword);
		
		
		
		JOptionPane.showMessageDialog(null, "**************Verifying USER*****************");
		String verifyusername,verifyuserpassword;
		
		verifyusername=JOptionPane.showInputDialog(null,"enter a username for signin");
		verifyuserpassword=JOptionPane.showInputDialog(null,"enter your passwordfor signin");
		if (example.login(verifyusername,verifyuserpassword))
			JOptionPane.showMessageDialog(null, "**************LOGIN SUCCESSFUL*****************");

		
		if (example.login(verifyusername,verifyuserpassword))
			JOptionPane.showMessageDialog(null, "**************login SUCCESSFUL*****************");
		else
			JOptionPane.showMessageDialog(null, "**************LOGIN UNSUCCESSFUL*****************");
	}
	
	private void signup(String username, String password) {
		
		// TODO Auto-generated method stub
		String saltedPassword= SALT+password;
		String hashedPassword= generateHash(saltedPassword);
		UserPasswordDatabase.put(username, hashedPassword);
		
	}
	
	
	public Boolean login(String username, String password) {
		Boolean isAuthenticated = false;

	
		String saltedPassword = SALT + password;
		System.out.println(saltedPassword);
		String hashedPassword = generateHash(saltedPassword);
		System.out.println(hashedPassword);

		String storedPasswordHash = UserPasswordDatabase.get(username);
		if(hashedPassword.equals(storedPasswordHash)){
			isAuthenticated = true;
		}else{
			isAuthenticated = false;
		}
		return isAuthenticated;
	}

	
	
	private String generateHash(String passwordWithSalt) {
		// TODO Auto-generated method stub
		StringBuilder hash= new StringBuilder();
		try{
			MessageDigest messageDigest= MessageDigest.getInstance("SHA-1");
			byte[] hashedBytes= messageDigest.digest(passwordWithSalt.getBytes());
			for(int i=0; i< hashedBytes.length ;i++)
            {
                hash.append(Integer.toString((hashedBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
			
		}catch (NoSuchAlgorithmException e){
			e.printStackTrace();
		}
		
		return hash.toString();
	}

}
