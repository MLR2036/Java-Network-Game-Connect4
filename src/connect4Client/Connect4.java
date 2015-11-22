//package connect4Client;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.io.*;
import java.net.*;
/**
 * This is the Connect 4 client it is in charge of setting the turns for players
 * and receiving status messages from the server.  
 * */

public class Connect4 extends JFrame implements Runnable, GameConstants {
 
	//Indicates if player has the turn
	private boolean myTurn = false;
	//The players counter
    private String myColour = " ";
   //other players counter
    private String otherColour = " ";
    
    public static boolean connected;
    
 // Create and initialize cells
    private Cell[][] cell =  new Cell[6][7];
    private int[][] dropPos = new int[6][7];
    
   private Socket socket;
    
    	
    
    //Title label 
    private JLabel lblTitle = new JLabel();
    //Status
    private JLabel lblStatus = new JLabel();
    
    //Selected row and column on current turn
    private int close = CONTINUE;
    private int check = CONTINUE;
    private int selectedRow;
    private int selectedColumn;
    
    //Input\Output streams for server
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    
    //continue game?
    private boolean continueToPlay = true;
    
    //Wait for player to take turn
    private boolean waiting = true;
    
    private String host = "localHost";
    
    public Connect4(String title){
    	
    	super(title);
    	connected = true;
    	//Panel to hold cells
    	JPanel gameBoard = new JPanel();
    	
    	gameBoard.setLayout(new GridLayout(6,7,0,0));
    	
    	for(int i = 0; i < 6; i++)
    		for(int j = 0; j < 7; j++)   		
    		gameBoard.add(cell[i][j] = new Cell(i,j,this));
    	 //fill the drop positions with 0
    	
    	for(int i = 0; i < 6; i++){
        	for(int j = 0; j < 7; j++){
        		dropPos[i][j] = 0;
        	}
        }
    	
    	
    	//set components properties
    	gameBoard.setBorder(new LineBorder(Color.red,5));
    	
    	    lblTitle.setHorizontalAlignment(JLabel.CENTER);
    	    lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
    	    lblTitle.setBorder(new LineBorder(Color.black, 1));
    	    lblStatus.setBorder(new LineBorder(Color.black, 1));
    	    
    	    //add label and gameBord panel to frame
    	    
    	    setLayout(new BorderLayout()); // implicit anyway
       
    	    add(lblTitle, BorderLayout.NORTH);
    	    add(gameBoard, BorderLayout.CENTER);
    	    add(lblStatus, BorderLayout.SOUTH);
    	    addWindowListener(new exitListener());
    	    //make connection to server 
    	    
    	    
    	    connectToServer();
    	
    }
    
    private void connectToServer() {

        try {
          // Create a socket to connect to the server
            Socket socket;
            
         
            
          socket = new Socket(host, 8000);
          
          // Create an input stream to receive data from the server
          fromServer = new DataInputStream(socket.getInputStream());

          // Create an output stream to send data to the server
          toServer = new DataOutputStream(socket.getOutputStream());
        }
        catch (Exception ex) {
          System.err.println(ex);
          JOptionPane.showMessageDialog(getParent(),"There is no service running","Error Messgae",JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        // Control the game on a separate thread
        Thread thread = new Thread(this);
        thread.start();
      }
    /**
     * This is the game loop in the while loop which runs 
     * while continue to play is true a check message is sent before the move is 
     * sent over. This is to check if the other player exited when it was not their turn
     */
	public void run() {
		
		try{
			// Get notification from the server
		      int player = fromServer.readInt();
		      
		      if(player == PLAYER1)
		      {
		    	  setMyColour("Blue");
		    	  setOtherColour("Red");
		    	  setTitleMessage("Player1 with colour bule");
		    	  setStatusMessage("Waiting for player2 to join");
		    	  
		    	  //startup notification from server
		    	  fromServer.readInt(); // first continue message start or continue now that p2 has joined
		    	  
		    	// The other player has joined
		          setStatusMessage("Player 2 has joined. I start first");
		          
		       // It is my turn
		          setMyTurn(true);		    	  
		      }
		      else if(player == PLAYER2)
		      {
		    	  setMyColour("Red");
		    	  setOtherColour("Blue");
		    	  setTitleMessage("Player2 with colour red");
		    	  setStatusMessage("Waiting for player 1 to move");
		    	  
		      }
		      
		      while(continueToPlay){
		    	  
		    	  if(player == PLAYER1){
		    		  //toServer.writeInt(check);
		    		  waitForPlayerAction(); // Wait for player 1 to move
		    		  toServer.writeInt(check);
		              sendMove(); // Send the move to the server
		              System.out.println("move sent by p1");
		              receiveInfoFromServer(); // Receive info from the server
		    	  }
		    	  else if(player == PLAYER2){
		    		  //toServer.writeInt(check);
		    		  receiveInfoFromServer();	//Receive info from server
		    		   waitForPlayerAction();//wait for player 2 to move
		    		  toServer.writeInt(check);
		    		  sendMove(); //send move to server
		    		  System.out.println("move sent by p2");
		    	  }
		      }       
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(getParent(), "The Server Has Stopped running", "Game Message", JOptionPane.INFORMATION_MESSAGE);			
			System.out.println("Service no longer running");
			System.exit(0);
			
		}
		
		
	}
	
	public void waitForPlayerAction() throws InterruptedException{
		while(isWaiting()){ //continue to check if local player has taken their turn
			Thread.sleep(100); 
		}
		
		//local player has taken their turn by this point so move will now be sent to server
		setWaiting(true); 
	}
	
	/**
	 * a close message is sent first to allow the server to check if the 
	 * client wants to close
	 * */ 
	  private void sendMove() throws IOException {
		 
        toServer.writeInt(close);    
	    toServer.writeInt(getRowSelected()); // Send the selected row
	    toServer.writeInt(getColumnSelected()); // Send the selected column
	  }
	  
	  /** Receive info from the server 
	   * This method contains all the status checking 
	   * if the status is equal to close the close method is called and 
	   * the applicable close procedure will take place  
	 * @throws IOException */
	  
	public void receiveInfoFromServer() throws IOException{
		int status = fromServer.readInt();
		
		if(status == PLAYER1_WON){
			//player1 won, stop playing
			continueToPlay = false;
			if(getMyColour() == "Blue"){
				setStatusMessage("I won! (Blue)");
				
			}
			else if(getMyColour() == "Red"){
				setStatusMessage("Player 1 (Bule) has won");
				receiveMove();
			}
			JOptionPane.showMessageDialog(this, "GAME OVER", "Sever Message",
					JOptionPane.INFORMATION_MESSAGE);
		 
		  System.exit(0);
			
		}
		else if(status == PLAYER2_WON){
			//player2 won, stop playing
			continueToPlay = false;
			if(getMyColour() == "Red"){
				setStatusMessage("I won! (Red)");
			}
			else if(getMyColour() == "Blue"){
				setStatusMessage("Player 2 (Red) has won");
				receiveMove();
			}
			
			JOptionPane.showMessageDialog(this, "GAME OVER", "Sever Message",
					JOptionPane.INFORMATION_MESSAGE);
		 
		  System.exit(0);
			
			
		}
		else if(status == DRAW){
			//no winners, GameOver
			continueToPlay = false;
			setStatusMessage("Game is a draw");
			
			if(getMyColour() == "Blue"){
				receiveMove();
			}
			
			JOptionPane.showMessageDialog(this, "GAME OVER", "Sever Message",
					JOptionPane.INFORMATION_MESSAGE);
		 
		  System.exit(0);
		}
		else if(status == CLOSE){
			System.out.println("got close message");			
			this.closeProcedure();
			
			
			
		    
		}
		else{
			receiveMove();
			setStatusMessage("My Turn");
			setMyTurn(true);
		}
		
			
			
	
		
	}
	private void receiveMove() throws IOException {
	    // Get the other player's move
		//int close = fromServer.readInt();
		//if(close == CLOSE){
		//	closeProcedure();
		//}
	    int row = fromServer.readInt();
	    int column = fromServer.readInt();
	    cell[row][column].setColour(otherColour);
	  }
	
	// accessors/mutators

	  public void setMyTurn(boolean b) {
		  myTurn = b;
	  }

	  public boolean getMyTurn() {
		  return myTurn;
	  }

	  public String getMyColour() {
		  return myColour;
	  }

	  public void setMyColour(String c) {
		  myColour = c;
	  }

	  public String getOtherColour() {
		  return otherColour;
	  }
	  /**
	   * 	
	   *This forces the counters to drop to the bottom of the column it uses an, if else statement to determine the next available row.
	   * It starts by checking the bottom row and if that is taken it will progress to the next until it finds a free row.      
	   * @param col The column to be checked
	   * @param counter The counter to put into the cell
	   * @return Returns the row to send to the server
	   */
	  public int getDropPos(int col, String counter){
		
			
		
			 if(cell[5][col].getColour() == " "){				 
			    cell[5][col].setColour(counter);
					 return 5;
							
			 }
			 else if(cell[4][col].getColour() == " "){
				 cell[4][col].setColour(counter);
				 
				 return 4;
			 }
			 else if(cell[3][col].getColour() == " "){
				     cell[3][col].setColour(counter);
				     return 3;
			 }
			 else if(cell[2][col].getColour() == " "){
				     cell[2][col].setColour(counter);
				     return 2;
			 }
			 else if(cell[1][col].getColour() == " "){
				     cell[1][col].setColour(counter);
				     return 1;
			 }
			 else{
				 cell[0][col].setColour(counter);
				 return 0;
			 }
	  }
				
		
			  
	  

	  public void setOtherColour(String c) {
		  otherColour = c;
	  }

	  public void setRowSelected(int r) {		  
		  selectedRow = r;  	  
		 
	  }

	  public int getRowSelected() {
		  return selectedRow;
	  }

	  public void setColumnSelected(int c) {
		  selectedColumn = c;
	  }

	  public int getColumnSelected() {
		  return selectedColumn;
	  }

	  public void setWaiting(boolean b) {
		  waiting = b;
	  }

	  public boolean isWaiting() {
		  return waiting;
	  }

	  public void setStatusMessage(String msg) {
	      lblStatus.setText(msg);
	  }

	  public void setTitleMessage(String msg) {
	      lblTitle.setText(msg);
	  }
	  
	  public void closeProcedure() throws IOException{
		  JOptionPane.showMessageDialog(this, "The other player has left", "Sever Message",
					JOptionPane.INFORMATION_MESSAGE);
		 
		  System.exit(0);
	  }
	
	public static void main(String args[]){
		//Create frame
		Connect4 frame = new Connect4("Connect4 Client");
		
		// Display the frame
	    frame.setSize(500, 500);
	    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    frame.setVisible(true);
		
	}
	/**
	 * This is in control of sending the close messages to the server
	 * and confirming if the player wants to quit
	 *  
	 * */
	private class exitListener extends WindowAdapter{
		
		public void windowClosing(WindowEvent e){
			int confirm = JOptionPane.showOptionDialog(Connect4.this, "Are you sure you want to quit", "Exit confirmation",JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,null,null,null);
				if(confirm == 0){											
				     Connect4.this.close = CLOSE;
				     try {
				    	 if(getMyTurn() == true){
				    	 setMyTurn(false);
				    	 setStatusMessage("Closing session");
				         setWaiting(false);
						Connect4.this.sendMove();
						System.out.println("closeing");
						System.exit(0);
				    	 }
				    	 else{ 		 
				    				    		
				    		setStatusMessage("Closing session");
				    		
				    		 //Connect4.this.socket.shutdownOutput();	
				    		
				    		Connect4.this.check = CLOSE;
				    		
				    		 JOptionPane.showMessageDialog(Connect4.this, "Session will end in one turn", "Sever Message",
				 					JOptionPane.INFORMATION_MESSAGE);
				    		
				    	 }
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						System.out.println("failed to close");
					}
					
				    
					
					 
					
		}
		
	}
	
	
	}
	
}
