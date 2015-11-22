//package connect4Client;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.LineBorder;


public class Cell extends JPanel {
	
	//Indicate where this cell is on the game board
	private int row;
	private int column;
	private int dropRow;
	
	//Colour in this cell
	String colour = " ";
	
	private Connect4 parent;
	
	public Cell(int row, int column, Connect4 gui){
		this.row = row;
		this.column = column;
		this.parent = gui;
		
		
		setBackground(Color.green);
		 setBorder(new LineBorder(Color.black, 1));   // Set cell's border
		 
	     addMouseListener(new ClickListener());       // Register listener
	     
	}
	
	
    public String getColour() {
      return colour;
    }

   
    public void setColour(String c) {
      colour = c;
      repaint();
    }
    
    public void setDropRow(int row){
    	dropRow = row;
    	
    }
    
    
    
   

    protected void paintComponent(Graphics g) {

      super.paintComponent(g);
      
      
      if(colour == "Blue"){
    	  g.setColor(Color.BLUE);
    	  g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
    	  
      }
      else if(colour == "Red"){
    	  g.setColor(Color.RED);
    	  g.fillOval(10, 10, getWidth() - 20, getHeight() - 20);
      }
    }
    
    
	private class ClickListener extends MouseAdapter {

	    public void mouseClicked(MouseEvent e) {

	        // If cell is not occupied and the player has the turn
	    	
          
	        if ((colour == " ") && parent.getMyTurn()) {
	        	
	        	setDropRow(parent.getDropPos(column, parent.getMyColour()));
	         // setColour(parent.getMyColour());   // Set the player's token in the cell
	          
	          parent.setMyTurn(false);
	         //parent.setRowSelected(parent.getDropPos(column, parent.getMyColour()));
	          parent.setRowSelected(dropRow);
	          parent.setColumnSelected(column);
	          parent.setStatusMessage("Waiting for the other player to move");
	          parent.setWaiting(false);         // Just completed a successful move

	        }
	      
	    }
	 }

}
