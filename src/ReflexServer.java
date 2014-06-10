import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.impressflow.net.Account;


public class ReflexServer {
	
	HashMap <Integer, Account> accounts;
	
	public ReflexServer() {
		// TODO Auto-generated constructor stub
		
		accounts = new HashMap <Integer, Account>(800);
	    Server server = new Server();
	    server.getKryo().register(Account.class);
	    
	    server.start();
	    try {
			server.bind(3000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    server.addListener(new Listener() {
	    	
	    	@Override
	        public void received (Connection connection, Object object) {
	    		  
	    		  //Registering new account!
	              if(object instanceof Account){
	            	  Account temp = (Account)object;
	            	  System.out.println("Registering new account: " + temp.name);
	            	  
	            	  accounts.put(connection.getID(), temp);
	            	  connection.sendTCP("account registered: " + temp.name + " " + connection.getID());
	              }

	        }
	    	
	    	@Override
	    	public void disconnected(Connection connection){
	    		accounts.remove(connection.getID());
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
