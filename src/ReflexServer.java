import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.impressflow.net.Account;
import com.impressflow.net.ConnectionRequest;
import com.impressflow.net.PetInfo;
import com.impressflow.net.Status;


public class ReflexServer extends Server {
	
	HashMap <Integer, Account> accounts;
	
	public ReflexServer() {
		// TODO Auto-generated constructor stub
		
		accounts = new HashMap <Integer, Account>(800);
	  
	    getKryo().register(String[].class);
	    getKryo().register(Account.class);
	    getKryo().register(Status.class);
	    getKryo().register(ConnectionRequest.class);
	    getKryo().register(PetInfo.class);
	    
	    start();
	    try {
			bind(3000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    addListener(new Listener() {
	    	
	    	@Override
	        public void received (Connection connection, Object object) {
	    		  
	              if(object instanceof Status){
	            	  //If we get a status, blindly forward it
	            	  Status temp = (Status) object;
	            	  sendToTCP(temp.toId, temp);
	              }
	              
	              else if(object instanceof PetInfo){
	            	  PetInfo temp = (PetInfo) object;
	            	  sendToTCP(temp.toId, temp);
	              }
	    		
	    		  //Registering new account!
	              else if(object instanceof Account){
	            	  Account temp = (Account) object;
	            	  System.out.println("Registering new account: " + temp.emails);
	            	  
	            	  accounts.put(connection.getID(), temp);
	            	  connection.sendTCP(temp);
	              }
	              
	              else if(object instanceof ConnectionRequest){
	            	  ConnectionRequest temp = (ConnectionRequest) object;
	            	  System.out.println("Received connection request from: " + connection.getID());
	            	  if(temp.random == true){
	            		  
	            		  System.out.println("Random is true");
	            		  
	            		  Set<Integer> keys = accounts.keySet();
	            		  for(Integer key : keys){
	            			  Account account = accounts.get(key);
	            			  System.out.println("Current key: " + key + ", isConnected: " + account.isConnected + ", random: " + account.random);
	            			  
	            			  //If this account is not connected AND it is not the account associated with this connection
	            			  //AND the user must want to connect randomly
	            			  if(!account.isConnected && account.random && !key.equals(connection.getID())){
	            				  
	            				  System.out.println("Success! Connecting " + key + " to " + connection.getID());
	            				  
	            				  account.isConnected = true;
	            				  account.toId = connection.getID();
	            				  
	            				  //Do the opposite for the other connection
	            				  account = accounts.get(connection.getID());
	            				  account.isConnected = true;
	            				  account.toId = key;
	            				  
	            				  Status init = new Status();
	            				  init.init = true;
	            				  
	            				  init.fromId = key;
	            				  sendToTCP(connection.getID(), init);
	            				  
	            				  init.fromId = connection.getID();
	            				  sendToTCP(key, init);
	            				  
	            				  return;
	            			  }
	            		  }
	            	  }
	            	  
	            	  else{}
	              }


	        }
	    	
	    	@Override
	    	public void disconnected(Connection connection){
	    		Account account = accounts.get(connection.getID());
	    		
	    		if(account.toId >= 0){
	    			sendToTCP(account.toId, "remoteDisconnect");
	    			
	    			Account remote = accounts.get(account.toId);
	    			remote.toId = -1;
	    			remote.isConnected = false;	    			
	    		}
	    		
	    		accounts.remove(connection.getID());
	    		account.toId = -1;
	    	}
	     });
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReflexServer reflexServer = new ReflexServer();
	}

}
