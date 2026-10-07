/* 
Ship.java
Fatima Ali
This class represents the player's ship in the Space Invaders game. It has variables for the ship's position, size, image, and controls (left, right, shoot). 
It also has static variables for whether the ship is alive, how many lives it has, and the time of death (used to determine when the ship can respawn after being destroyed). 
The ship can move left and right based on input from yser, shoot projectiles, check for collisions with enemy projectiles, and handle its own death and respawn logic.
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.*;

public class Ship {
    private int x, y, width, height; //coordinates and dimensions of the ship
    private BufferedImage img, ship_img; //current image, represented by a BufferedImage for more precise collision detection
    private int left, right, shoot;
    private static final int SPEED = 3;
    private static boolean alive = true;
    static int lives = 3;
    private long death_time= -1; //current time in milliseconds when the ship was killed (-1 if it hasnt been killed yet), used to determine when it can respawn
    private static final long RESPAWN_DELAY = 900;

   /* Ship(int x, int y, int width, int height, int l, int r, int s):
    Return type: none
    Parameters: int x, int y, int width, int height, int l, int r, int s
    Description: Constructor for the Ship class that initializes the ship's position, size, image, and controls based on the parameters. The ship starts alive with 3 lives and a death time of -1 (indicating it hasn't been killed yet).
     */
    public Ship(int x, int y, int width, int height, int l, int r, int s) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = Util.loadBuffImage("assets//ship.png", width, height);
        this.left = l;
        this.right = r;
        this.shoot = s;
        // need to put these in here again for some reason otherwise the ship doesnt respawn
        alive = true;
        lives = 3;
        death_time = -1;
    }

    /* draw(Graphics g):
    Return type: void
    Parameters: Graphics g
    Description: draws the ship on the screen using the current image and position
     */

    public void draw(Graphics g){
        g.drawImage(img, x,y, null);
    }

    /* get_bounds():
    Return type: Rectangle
    Parameters: none
    Description: returns the bounding rectangle for the ship
     */

    public Rectangle get_bounds() {
        return new Rectangle(x, y, width, height);
    }

    /* is_alive():
    Return type: boolean
    Parameters: none
    Description: returns whether the ship is currently alive or not
     */

    public boolean is_alive(){
        return alive;
    }

    /* can_respawn():
    Return type: boolean
    Parameters: none
    Description: returns whether the ship can respawn based on its death time and the respawn delay
     */

    public boolean can_respawn(){
        return death_time > 0 && System.currentTimeMillis() - death_time >= RESPAWN_DELAY;
    }

    /* respawn():
    Return type: void
    Parameters: none
    Description: respawns the ship at its starting position when it is killed, and resets the alive status and death time. The ship can only respawn if it has lives remaining and the respawn delay has passed since it was killed.
     */

    public void respawn(){
        alive = true;
        death_time = -1;
        x = 450 - width/2;
        y = 725;
    }

    /* kill():
    Return type: void
    Parameters: none
    Description: kills the ship and updates its status and death time
     */

    public void kill(){
        lives -= 1;
        alive = false;
        death_time = System.currentTimeMillis();
    }

    /* move():
    Return type: void
    Parameters: boolean []keys (user input), ArrayList<Projectile> projectiles (list of projectiles in the game)
    Description: moves the ship based on the keys pressed and handles shooting
     */
    public void move(boolean []keys, ArrayList<Projectile> projectiles){
        if(!alive){
            return;
        }
    	if(keys[left] && x > 0){
			x -= SPEED;		
     	}
     	if(keys[right] && x < 900-width){
			x += SPEED;		
     	}
        //in the original space invaders there can only be one shot on the screen at a time, so I implemented it that way here
        ArrayList<Projectile> ship_shots = new ArrayList<>();
        for(Projectile p: projectiles){
            if (!p.is_enemy_shot() && p.is_active()){
                ship_shots.add(p);
                break;
            }
        }
        if(keys[shoot] && ship_shots.isEmpty()){
            shoot(projectiles);
            Particles.play_sound("shoot");
        }
    }

    /* shoot():
    Return type: void
    Parameters: ArrayList<Projectile> projectiles
    Description: shoots a projectile from the ship's position
     */

    public void shoot(ArrayList<Projectile> projectiles){
        Projectile p = new Projectile(x + width/2 - 15, y, 32, 32, 0);
        projectiles.add(p);
    }

    /* check_hit():
    Return type: void
    Parameters: ArrayList<Projectile> projectiles
    Description: checks if the ship has been hit by any enemy projectiles. Works exactly the same way as the check_hit method for the Enemy class, but it checks for enemy shots instead of player shots and it kills the ship and creates player explosion particles instead of killing an enemy and creating enemy explosion particles.
     */

    public void check_hit(ArrayList<Projectile> projectiles){
        for (int i = projectiles.size() - 1; i >= 0; i--) {
        Projectile p = projectiles.get(i);
        if(!p.is_active()) continue;
        if(!p.is_enemy_shot()) continue;
     
        Rectangle rect1 = new Rectangle(x, y, width, height);
        Rectangle rect2 = new Rectangle(p.x, p.y, p.width, p.height);
        //check further if the rectangles actually intersect
        if (rect1.intersects(rect2)) {
            //check the exact image pixels to detect a more precise collision
            if (Util.pixel_collide(x, y, img, p.x, p.y, p.img)) {
                p.kill();
                this.kill();
                long current_time = System.currentTimeMillis();
                Particles explosion = new Particles(x, y, width, height, 0, current_time);
                SpacePanel.particles.add(explosion);
                Particles.play_sound("player_death");
                break;
            }
            }
    }
    }
}
