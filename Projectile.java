/* Projectile.java
Fatima Ali
The Projectile class is used to represent all the projectiles shot by both the ship and the enemies. 
It has variables for the projectile's position, size, type (0 - ship, 1-3 - enemy types 1-3), speed, damage, and animation frame. 
The direction is determined by the type of projectile (enemy or player), so is max frames and img
The projectile class contains methods to move the projectile, draw it, check if it's active, and animate it. 
The move method also checks if the projectile has gone off screen and deactivates it if so. 
 */
import java.awt.*;
import java.awt.image.BufferedImage;
public class Projectile {
    int x, y, width, height, type;
    BufferedImage img;
    boolean active;
    private double frame;
    private int[] max_frames;
    private int direction;
    static int[] speeds = {10,3,5,10};
    static int[] damages = {3, 4, 5, 6}; //player, enemy type 1-3
    private int speed;
    private int damage;

    //type 0 for ship projectile, 1-3 for enemy projectiles of different types
    /*Constructor method 
    Return type : none
    Parameters: int x, int y, int width, int height, int type
    Description: initializes all the projectile variables based on the parameters and type of projectile. The speed and damage are determined by the type of projectile, as well as the direction (ship projectiles move up, enemy projectiles move down). 
    The img is a buffered image so that collision detection is more precise, and is also determined by the type and starts at frame 1 of the animation cycle. The projectile is set to active when it's created.
     */
    public Projectile(int x, int y, int width, int height, int type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.active = true;
        this.frame = 1.0;
        this.speed = speeds[type];
        this.damage = damages[type];
        this.direction = (type == 0) ? -1 : 1; //ship projectiles move up, enemy projectiles move down
        this.max_frames = new int[]{10, 6, 6, 6};
        this.img = Util.loadBuffImage(String.format("assets//shot_%d/%d.png", type, Math.round(frame)), width, height);
    }

    /* get_damage():
    Return type : int
    Parameters: none
    Description: returns the damage value of the projectile based on its type
     */

    public int get_damage() {
        return damage;
    }
    /* is_enemy_shot():
    Return type : boolean
    Parameters: none
    Description: checks if the projectile is an enemy shot and returns true if so.
     */
    public boolean is_enemy_shot() {
        return type != 0;
    }

    /* kill():
    Return type : void
    Parameters: none
    Description: deactivates the projectile when it hits something or goes off screen
     */

    public void kill(){
        active = false;
    }
    /* draw(Graphics g):
    Return type : void
    Parameters: Graphics g
    Description: draws the projectile on the screen at its current position. 
    */

    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }

    /* move():
    Return type : void
    Parameters: none
    Description: moves the projectile based on its direction and speed. It also animates the projectile and checks if it has gone off screen.
     */

    public void move() {
        y += direction * speed;
        animate_projectile();
        if (y + height < 0 || y > 900) {
            kill();
        }
    }
    /* is_active():
    Return type : boolean
    Parameters: none
    Description: checks if the projectile is active and returns true if so.
     */
    public boolean is_active() {
        return active;
    }

    /* animate_projectile():
    Return type : void
    Parameters: none
    Description: animates the projectile by updating increasing the frame by 0.2 (so that the animation is slower and can be seen better) and updating the img based on the new rounded value of frame. 
    The animation cycles through a set of images for each type of projectile (contained in folders named according to said type), and resets back to the first image after reaching the max frame for that type.
     */

    public void animate_projectile(){
        frame+=0.2;
        if (frame > max_frames[type]) {
            frame = 1.0;
        }
        img = Util.loadBuffImage(String.format("assets//shot_%d/%d.png", type, Math.round(frame)), width, height);
    
    }
    
}
