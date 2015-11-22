//package connect4Server;


import java.io.*;

import java.net.*;
/**
 * This is the Game session it receives moves from the client and tracks game progress.
 * */
public class GameSession implements GameConstants {
	
	private Socket player1;
	private Socket player2;
	
	
	// Create and initialize cells
	  private String[][] cell =  new String[6][7];
	  
	  private DataInputStream fromPlayer1;
	  private DataOutputStream toPlayer1;
	  private DataInputStream fromPlayer2;
	  private DataOutputStream toPlayer2;

	  // Continue to play
	  private boolean continueToPlay = true;
	  
	  /** Construct a thread */

	  public GameSession(Socket player1, Socket player2) {

	    this.player1 = player1;
	    this.player2 = player2;

	    // Initialize cells with a blank String

	    for (int i = 0; i < 6; i++)
	      for (int j = 0; j < 7; j++)
	        cell[i][j] = " ";
	  }
	  
	  public void runGame() {

		    try {

		      // Create data input and output streams

		      DataInputStream fromPlayer1 = new DataInputStream(player1.getInputStream());
		      DataOutputStream toPlayer1 = new DataOutputStream(player1.getOutputStream());
		      DataInputStream fromPlayer2 = new DataInputStream(player2.getInputStream());
		      DataOutputStream toPlayer2 = new DataOutputStream(player2.getOutputStream());

		     //Once Server is ready continue message is sent to player 1

		      toPlayer1.writeInt(CONTINUE);
		      
		      while (true) {
		    	  
		    	  int check = fromPlayer1.readInt();//check if player quits when it is not their turn 
                  if(check == CLOSE){
                	  System.out.println("Player 2 wants to exit");
 		        	 toPlayer1.writeInt(CLOSE);
 		        	  Close(toPlayer2, check);
 		        	  
                  }
		    	  
                 
		          // Receive a move from player 1
                  
		    	  int close = fromPlayer1.readInt();//check if player quits while it is their turn
		    	  
		          if (close == CLOSE){
		        	  System.out.println("Player 1 wants to exit");
		        	 toPlayer2.writeInt(CLOSE);
		        	  Close(toPlayer2, close);
		         }
                  
                  //int close = fromPlayer1.readInt();
                  int row = fromPlayer1.readInt();
		          int column = fromPlayer1.readInt();
		          System.out.println("Recived move from player1");
		          
		        //  int row = dropCounter(column);
		          
		          cell[row][column] = "Blue";
                  
                  
		          if (isWon("Blue")) {
		              toPlayer1.writeInt(PLAYER1_WON);
		              toPlayer2.writeInt(PLAYER1_WON);
		              sendMove(toPlayer2, row, column);
		              
		              break; // Break the loop
		            }		        
		          else {

		              // Notify player 2 to take the turn - as this message is not '1' then
		              // this will switch to the relevant player at the client side

		              toPlayer2.writeInt(CONTINUE);
		              
		              
                         
		              // Send player 1's selected row and column to player 2
		              sendMove(toPlayer2, row, column);
		              System.out.println("Move sent to player 2");
		           }		          
		          
		         
		           
		        
		           check = fromPlayer2.readInt();//check if other player leaves before taking their turn
                  if(check == CLOSE){
                	  System.out.println("Player 1 wants to exit");
 		        	 toPlayer2.writeInt(CLOSE);
 		        	  Close(toPlayer1, check); 
 		        	  
                  }
		          
		       // Receive a move from Player 2
		          close = fromPlayer2.readInt();//check if player quits while it is their turn
                  if (close == CLOSE){
                	  System.out.println("player 2 wants to exit");
                	  toPlayer1.writeInt(CLOSE);
                  Close(toPlayer1, close);
                 }
                  
                 // close = fromPlayer2.readInt();
		          row = fromPlayer2.readInt();
		          column = fromPlayer2.readInt();
		          System.out.println("Recived move from playe2");

		          cell[row][column] = "Red";
		       // Check if Player 2 wins
		          if (isWon("Red")) {
		            toPlayer1.writeInt(PLAYER2_WON);
		            toPlayer2.writeInt(PLAYER2_WON);
		            sendMove(toPlayer1, row, column);
		            
		            break;
		          }
		          else if (isFull() == true) { // Check if all cells are filled
		        	  toPlayer2.writeInt(DRAW);
		        	  toPlayer1.writeInt(DRAW);		              
		              System.out.println("It is a draw");
		              sendMove(toPlayer1, row, column);
		              break;
		            }
		          else {
		            // Notify player 1 to take the turn
		            toPlayer1.writeInt(CONTINUE);
		            
		           

		            // Send player 2's selected row and column to player 1
		            sendMove(toPlayer1, row, column);
		          }   
		           
		          
		      }
		    }
		    catch(IOException ex){
		    	System.out.println("Gamesession error");
		    	System.err.println(ex);
		    	System.out.println(ex.getMessage());
		    	System.out.println("Player left when not their turn");
		          	
		    	
		    }
		    
		  }
	  
	  /** Send the move to other player */
	  private void sendMove(DataOutputStream out, int row, int column) throws IOException {
       // out.writeInt(close);
	    out.writeInt(row); // Send row index
	    out.writeInt(column); // Send column index
	  }
	  
	  
	  
	 

	  private boolean isFull() {

	    for (int i = 0; i < 6; i++)
	      for (int j = 0; j < 7; j++)
	        if (cell[i][j] == " "){
	        	System.out.println("Grid not full");
	          return false; // At least one cell is not filled
	          
	        }
	    // All cells are filled
	    return true;
	  }
	  /**
	   * This Method checks every turn to determine if the game has been won. 
	   * It first checks the first 3 rows (6-4 = 2 so 0,1,2) for the amount
	   * of columns which exist the reason for this is it is not possible
	   *  to have a connect four after this point the method, Connected(int row,int col ,String counter) 
	   * is called to do the actual checks. Next the first 4 columns are checked (7-4 = 3 so 0,1,2,3)
	   * as for the same reason above. 
	   * 
	   *    This algorithm was researched author- ryan maguire web link - http://ryanmaguiremusic.com/media_files/pdf/ConnectFourSource.pdf   
	   *  
	  */
	  private boolean isWon(String counter){
		 
            boolean connect = false;
             for(int i = 0; i < 6 && connect ==false; i++){
            	 if(i<=6-4){
            		 for(int j = 0; j < 7 && connect ==false; j++){
            			 connect = connected(i,j,counter);
            		 }
            	 }
            	 else{
            		 for(int j = 0; j <= 7 - 4 && connect == false; j++){
            			 connect = connected(i,j,counter);
            		 }
            	 }
             }
		   
		    
		    	return connect;
		    
	  
	  }
	  
	  /**
	   * This Method is in charge of checking all possible places a connect 4 could exist.
	   *  
	   *  It uses a series of if statements and for loops to systematically check through the 2 dimensional string array "cell"
	   *  The first condition checks downward with a starting point down to row 2 counting from 0 as this is the boundary row 
	   *  for having a vertical connect 4 if a counter which is not equal to the last dropped counter is found there is no connect 4.
	   *  
	   *  The second condition checks for a diagonal win with a starting point up to column 3 counting from 0 but will only execute if 
	   *  the previous condition was not met. The check will go down and to the right from the cell which a counter 
	   *  was just dropped in to if in the next three cells diagonally down to the right a counter not equal to the last dropped
	   *  counter is found there is no connect 4.
	   *   
	   *  The next condition checks for a horizontal win up to column 3 counting from 0 but will only execute if the previous 2 
	   *  conditions are not met. If in the next 3 cells to the right of the last dropped counter a counter not equal to the last
	   *  dropped counter is found there is no connect 4.
	   *  
	   *  The next condition checks for diagonal wins up and to the right this is when the row value is too high (meaning that 3
	   *  or less counters are currently staked in the given column) for there to be a vertical win.
	   */
	  
	  private boolean connected(int row, int col, String counter){
		  boolean connect = false;
		  //check down
		  if(row < 6 - 3){
			  connect = true;
			  for(int i = row; i < row + 4; i++){
				  if(cell[i][col] != counter){
					  connect = false;
			  }
			  
		  }
		  //check down and to right
		  if(col < 7-3 && connect ==false){
			  connect = true;
			  for(int i = row, j=col; i < row + 4; i++, j++){
				  if(cell[i][j] != counter){
					  connect = false;
			      }
		       }
		    }
	     }
		
		if(col < 7 - 3 && connect == false){
			//check right 
			connect = true;
		    for(int j = col; j < col + 4; j++){
				 if(cell[row][j] != counter){
					 connect = false;
				 }
			 }
		    //check up and to right
		    if(row > 2 && connect == false){
		    	connect = true;
		    	for(int i = row, j = col; j < col + 4; i--, j++){
		    		if(cell[i][j] != counter){
		    			connect = false;
		    		}
		    	}
		    }
		}
		return connect;
	}
	  
	  public void checkClose(DataInputStream in) throws IOException{
		  if (in.readInt() == CLOSE){
			  System.out.println("Player wants to exit");
			  
		  }
		  
	  }
	  public void Close(DataOutputStream out, int close) throws IOException{
		  out.writeInt(close);
		  
	  }
	  
	  
	
}
