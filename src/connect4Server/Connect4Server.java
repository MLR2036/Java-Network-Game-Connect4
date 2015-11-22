//package connect4Server;


import java.io.*;
import java.net.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

public class Connect4Server extends JFrame implements GameConstants {
	
	private ServerSocket serverSocket;
	private Socket player1;
	private Socket player2;
	private GameSession game;
	
	private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem menuItem;
	
	public Connect4Server(){
		
		JTextArea serverLog = new JTextArea();
		
		// scroll pane to hold text area
        JScrollPane scrollPane = new JScrollPane(serverLog);
        // Add the scroll pane to the frame
        add(scrollPane, BorderLayout.CENTER);
        
        
        menuBar = new JMenuBar();
        menu = new JMenu("Options");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("Options Menu");
        menuItem = new JMenuItem("Close Server", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        KeyEvent.VK_1, ActionEvent.ALT_MASK));        
        menu.add(menuItem);
        menuBar.add(menu);
      
        //adding action listeners
        menuItem.addActionListener(new OptionsListener());
        addWindowListener(new exitListener());
        
        add(menuBar, BorderLayout.NORTH);
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(300, 300);
        setTitle("Connect4_Server");
        setVisible(true);
        
        try {

            // Create a server socket
            serverSocket = new ServerSocket(8000);

            serverLog.append(new Date() + ": Server started at socket 8000\n");

            // Number a session
            int sessionNo = 1;

            // Ready to create a session for every two players
            
            while (true) {

                serverLog.append(new Date() + ": Wait for players to join session " + sessionNo + '\n');

                // Connect to player 1
                player1 = serverSocket.accept();

                serverLog.append(new Date() + ": Player 1 joined session " + sessionNo + '\n');

                serverLog.append("Player 1's IP address" + player1.getInetAddress().getHostAddress() + '\n');

                // Notify that the player is Player 1

                new DataOutputStream(player1.getOutputStream()).writeInt(PLAYER1);
                
                // Connect to player 2 
                player2 = serverSocket.accept();
                
                serverLog.append(new Date() + ": Player 2 joined session " + sessionNo + '\n');
                
                serverLog.append("Player 2's IP address" + player2.getInetAddress().getHostAddress() + '\n');
                
                //Notify that the player is Player 2
                new DataOutputStream(player2.getOutputStream()).writeInt(PLAYER2);
            
                // Display this session and increment session number

                serverLog.append(new Date() + ": Start a thread for session " + sessionNo++ + '\n');
                
                game = new GameSession(player1, player2);
                
            
                
                game.runGame();
            }
        }
        catch(IOException ex){
        	 System.out.println("server side exception");
        	 System.err.println(ex);
        	 System.out.println(ex.getMessage());
        	
        }
              
		
	}
	
	public static void main(String args[]){
    	
		Connect4Server serverFrame = new Connect4Server();
    }
	
private class exitListener extends WindowAdapter{
		
		public void windowClosing(WindowEvent e){
					
			JOptionPane.showMessageDialog(getParent(), "The Server can not be closed while a game is in session use options",
					"Warning", JOptionPane.WARNING_MESSAGE);
					
				
			
		}
		
	}

private class OptionsListener implements ActionListener{

    
    public void actionPerformed(ActionEvent e) {
            
        if (e.getSource() == menuItem) {
        	
        	//close the clients then the server
        	
        	
        	
        	System.exit(0);
        //	try {        		
			     
				
			//} catch () {
				// TODO Auto-generated catch block
				
			//}
        
        } 
        
        }

}
}
