/* Space.java
Fatima Ali
Creates a new Space Invaders game window */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Space extends JFrame{
	SpacePanel game= new SpacePanel();
		
    public Space() {
		super("Space Invaders");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		add(game);
		pack();
		setVisible(true);
    }
    
    public static void main(String[] arguments) {
		new Space();		
    }
}
