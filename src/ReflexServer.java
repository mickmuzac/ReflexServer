import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.impressflow.net.Account;
import com.impressflow.net.ConnectionRequest;
import com.impressflow.net.Status;


public class ReflexServer {
	
	HashMap <Integer, Account> accounts;
	
	public ReflexServer() {
		// TODO Auto-generated constructor stub
		
		accounts = new HashMap <Integer, Account>(800);
	    final Server server = new Server();
	  
	    server.getKryo().register(String[].class);
	    server.getKryo().register(Account.class);
	    server.getKryo().register(Status.class);
	    server.getKryo().register(ConnectionRequest.class);
	    
	    server.start();
	    try {
			server.bind(3000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    server.addListener(new Listener() {
	    	
	    	@Override
	        public void received (Connection connection, Object object) {
	    		  
	              if(object instanceof Status){
	            	  //If we get a status, blindly forward it
	            	  Status temp = (Status) object;
	            	  server.sendToTCP(temp.toId, temp);
	              }
	    		
	    		  //Registering new account!
	              else if(object instanceof Account){
	            	  Account temp = (Account) object;
	            	  System.out.println("Registering new account: " + temp.emails);
	            	  
	            	  accounts.put(connection.getID(), temp);
	            	  connection.sendTCP("account registered: " + temp.emails + " " + connection.getID());
	              }
	              
	              else if(object instanceof ConnectionRequest){
	            	  ConnectionRequest temp = (ConnectionRequest) object;
	            	  
	            	  if(temp.random == true){
	            		  Set<Integer> keys = accounts.keySet();
	            		  for(Integer key : keys){
	            			  Account account = accounts.get(key);
	            			  
	            			  //If this account is not connected AND it is not the account associated with this connection
	            			  //AND the user must want to connect randomly
	            			  if(!account.isConnected && !key.equals(connection.getID())){
	            				  account.isConnected = true;
	            				  account.toId = connection.getID();
	            				  
	            				  //Do the opposite for the other connection
	            				  account = accounts.get(connection.getID());
	            				  account.isConnected = true;
	            				  account.toId = key;
	            				  
	            				  server.sendToTCP(connection.getID(), new Status());
	            				  return;
	            			  }
	            		  }
	            	  }
	            	  
	            	  else{}
	              }


	        }
	    	
	    	@Override
	    	public void disconnected(Connection connection){
	    		accounts.remove(connection.getID());
	    		
	    		//To-do: Disconnect the account that was connected to this one, if it exists...
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
