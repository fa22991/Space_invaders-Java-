/* 
Enemy.java
Fatima Ali
All enemies are objects of the Enemy class. Each enemy has variables for its position, size, image, type (1-4 for the different types of enemies), and a reference to the list of enemies (used to check if any of them are at the edge of the screen and needs to move down).
 */
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class Enemy {
    int x, y, width, height;
    BufferedImage img;
    private static int direction = 1; //1 for right, -1 for left
    private static boolean has_moved_down = false; 
    public static int descent_count = 0; //number of times the enemies have moved down
    boolean alive = true;
    private int type;
    private ArrayList<Enemy> enemies;
    private long last_shot; //used to track when the enemy last shot so that it can determine when it can shoot again based on its attack speed
    private final Random random;
    // private int damage;
    static int attack_speed;
    static int speed = 1, frequency = 2;
    final static int MYSTERY_SPEED = 3; //sopeed of mystery ship
    private int[] points;

    /* Constructor for the Enemy class 
    Return type: none
    Parameters: int x, int y, int width, int height, BufferedImage img, int type, ArrayList<Enemy> enemies
    Description: initializes the enemy object with the given parameters. The type determines the points the enemy is worth when killed, as well as its attack speed
    */
    public Enemy(int x, int y, int width, int height, BufferedImage img, int type, ArrayList<Enemy> enemies) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
        this.type = type;
        this.enemies = enemies;
        this.points = new int[]{0, 10, 20, 40, 100}; //points for each type of enemy, enemy indices start from 1 so slot 0 is just a placeholder
        this.random = new Random();
        attack_speed = 4000 + random.nextInt(800); //enemies can shoot every 4 seconds on average, but it is also random so they won't all shoot at the same time
        this.last_shot = System.currentTimeMillis() + random.nextInt(attack_speed);
    }

    /* draw():
    Return type: void
    Parameters: Graphics g
    Description: draws the enemy on the screen
     */

    public void draw(Graphics g){
        g.drawImage(img, x, y, width, height, null);
    }

    /* move():
    Return type: void
    Parameters: none
    Description: moves the enemy on the screen horizontally based on the current direction, and also checks if the enemy has reached the edge of the screen to determine if it needs to move down and change direction.
     It also checks if the enemy has reached the bottom of the screen, which would trigger a game over.
     */

    public void move() {
        if (type == 4) {
            x += MYSTERY_SPEED;
            if (x > 900) {
                alive = false;
            }
            return;
        }

        boolean move_down = false;
        for(Enemy e: this.enemies){
            if (e.type == 4) {
                continue;
            }
            if (e.x <= 0 || e.x + e.width >= 900) { 
                move_down = true;
                break;
            }
        }
        //if the enemy has reached the edge of the screen and needs to move down, it moves down and changes direction. 
        // The has_moved_down variable is used to ensure that the enemies only move down once per time they reach the edge, instead of moving down multiple times if some of the others are still at the edge in the next few frames while they are moving horizontally.
        if(move_down && !has_moved_down){
            if(!SpacePanel.game_over){
                for(Enemy e: this.enemies){
                    if (e.type == 4) {
                        continue;
                    }
                    e.y += 20;
                }
                descent_count++;
                direction *= -1;
                has_moved_down = true;
                
            }
        } 
        else if(!move_down){
            has_moved_down = false;
        }

        if(y >= 900 - height){
            //game over
            SpacePanel.game_over = true;
        }
        else{
            x += speed * direction;
        }
        
    }

    /* check_hit():
    Return type: void
    Parameters: ArrayList<Projectile> projectiles
    Description: checks if the enemy has been hit by any projectiles, skipping over the ones from other enemies or inactive projectiles. Again, its the same collision logic as the collisions between the ship and the enemy projectiles, projectiles and shields etc.
     */
    public void check_hit(ArrayList<Projectile> projectiles){
        for (int i = projectiles.size() - 1; i >= 0; i--) {
        Projectile p = projectiles.get(i);
        if (!p.is_active()) continue;
        if(p.is_enemy_shot()) continue;

     
        Rectangle rect1 = new Rectangle(x, y, width, height);
        Rectangle rect2 = new Rectangle(p.x, p.y, p.width, p.height);

        //check further if the rectangles actually intersect
        if (rect1.intersects(rect2)) {
            //check the exact image pixels to detect a more precise collision
            if (Util.pixel_collide(x, y, img, p.x, p.y, p.img)) {
                p.kill();
                this.kill();
                SpacePanel.score += points[type];
                if(type == 4){
                    SpacePanel.set_mystery_shot();
                }
                long current_time = System.currentTimeMillis();
                Particles explosion = new Particles(x, y, width, height, 1, current_time);
                SpacePanel.particles.add(explosion);
                Particles.play_sound("enemy_death");
                break;
            }
            }
    }

}
/* shoot():
    Return type: void
    Parameters: ArrayList<Projectile> projectiles
    Description: makes the enemy shoot a projectile of its corresponding type and adds it to the list of projectiles. 
     It also updates the last_shot variable to the current time so that the enemy can't shoot again until its attack speed cooldown has finished.
     */
public void shoot(ArrayList<Projectile> projectiles){
    Projectile p = new Projectile(x + width/2 - 15, y + height, 32, 32, this.type);
    projectiles.add(p);
    this.last_shot = System.currentTimeMillis();
}

/* kill():
    Return type: void
    Parameters: none
    Description: deactivates the enemy when it gets hit by a projectile
     */

public void kill(){
    this.alive = false;
}

/* is_mystery_ship():
    Return type: boolean
    Parameters: none
    Description: checks if the enemy is a mystery ship if its type is 4 and returns true if so.
*/

public boolean is_mystery_ship(){
    return this.type == 4;
}

/* can_shoot():
    Return type: boolean
    Parameters: long now
    Description: checks if the enemy can shoot based on its attack speed. Mystery ship can never shoot so it just returns false if the type is 4. 
    For the other enemies, it checks if the current time (passed in as now) minus the last_shot time is greater than the attack_speed, which means the cooldown has finished and the enemy can shoot again, so it returns true. Otherwise, it returns false.
*/
public boolean can_shoot(long now){
    if (this.type == 4) {
        return false;
    }
    if (now - last_shot < attack_speed) {
        return false;
    }
    return true;
}

/* animate_mystery_ship():
    Return type: void
    Parameters: none
    Description: animates the mystery ship by moving it horizontally across the screen while it is within the screen boundaries
 */
public void animate_mystery_ship(){
    if(this.alive){
        if(x > 0 && x < 900){
            x += MYSTERY_SPEED;
        }
    }
}

}
