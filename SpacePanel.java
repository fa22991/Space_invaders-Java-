
/* 
SpacePanel.java
Fatima Ali
This class is responsible for the main game panel, including the game loop, drawing, and handling the different screens (intro, game, lost, won). It also manages the ship, enemies, 
projectiles, particles, and shields via objects of their classes. The game logic for moving these objects, checking collisions, and making enemies(including the mystery ship) is also done here.
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

class SpacePanel extends JPanel implements KeyListener, ActionListener, MouseListener{
	public static final int INTRO=0, GAME=1, LOST=2, WON=3;
	private int screen = INTRO; //curent screen being displayed
	static int score = 0; // players score
	static boolean game_over = false; 
	private boolean []keys;
	private Timer timer;
	private Image intro, lost, won;
	private BufferedImage enemy1, enemy2, enemy3, enemy4;
	private long last_mystery_spawn; //used to track when to spawn the mystery ship
	private long mystery_delay; //used to determine how long to wait before spawning the mystery ship, it is randomized so the mystery ship doesn't always spawn at the same time
	static boolean mystery_shot = false;
	public static ArrayList<Particles> particles;
	private static final String FONT_PATH = "assets/raster-forge-font/RasterForgeRegular-JpBgm.ttf";
    private Font font;
    private GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

	//shield
	BufferedImage shield1, shield2, shield3;
	Graphics Shield;

	//ship dimensions
	private	int ship_width = 128;
	private	int ship_height = 75;
	private	int ship_x = 450 - ship_width/2;
	private	int ship_y = 725;
	private Ship ship;

	//enemy dimensions/arrays
	private	int enemy_width = 64;
	private	int enemy_height = 64;
	private ArrayList<Enemy> enemies1, enemies2, enemies3, enemies;
	static ArrayList<Projectile> projectiles;

	static int curr_lvl = 1;
	/* constructor method
	Loads images, initializea the ship, enemies, projectiles, and particles arrays, and starts the game timer. 
	Also sets up the key and mouse listeners and the font for drawing text on the screen.
	*/
	public SpacePanel(){
		try{
			font = Font.createFont(Font.TRUETYPE_FONT, new File(FONT_PATH)).deriveFont(24f);
			ge.registerFont(font);
		} 
		catch(Exception e) {
			font = new Font("Monospaced", Font.PLAIN, 24);
		}
		enemy1 = Util.loadBuffImage("assets/enemy1.png", ship_width, 90);
		enemy2 = Util.loadBuffImage("assets/enemy2.png", enemy_width, enemy_height);
		enemy3 = Util.loadBuffImage("assets/enemy3.png", enemy_width, enemy_height);
		enemy4 = Util.loadBuffImage("assets/mystery_red_ship.png", enemy_width*2, enemy_height);
		intro = new ImageIcon("assets//intro.png").getImage();
		lost = new ImageIcon("assets//lost.png").getImage();
		won = new ImageIcon("assets//won.png").getImage();
		ship = new Ship(ship_x, ship_y, ship_width, ship_height, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_SPACE);
		setPreferredSize(new Dimension(900, 900));
		setFocusable(true);
		requestFocus();
		addKeyListener(this);
		addMouseListener(this);
		timer = new Timer(20, this);
		timer.start();

		keys = new boolean[KeyEvent.KEY_LAST+1];
		shield1 = Util.loadBuffImage("assets/shield.png", ship_width, 90);
		shield2 = Util.loadBuffImage("assets/shield.png", ship_width, 90);
		shield3 = Util.loadBuffImage("assets/shield.png", ship_width, 90);
		enemies1 = new ArrayList<Enemy>();
		enemies2 = new ArrayList<Enemy>();
		enemies3 = new ArrayList<Enemy>();
		enemies = new ArrayList<Enemy>();
		projectiles = new ArrayList<Projectile>();
		particles = new ArrayList<Particles>();
	}
	/* make_enemies():
	Return type : void
	Parameters: none
	Description: uses a for loop to create enemies in their starting postions at the beginning of each level. The first loop creates the top row containing 10 type 3 enemies and worth the most points.
	The next two loops create the middle (20 enemies) and bottom (30 enemies) rows of enemies, which are types 2 and 1 respectively.
	 */
	public void make_enemies() {
		for(int j=0; j<10; j++){
			Enemy e = new Enemy(j * 75 + 30, 50, enemy_width, enemy_height, enemy3, 3, enemies);
			enemies3.add(e);
			enemies.add(e);
		}
		for(int i = 0; i<2; i++){
			for(int j=0; j<10; j++){
				//my image files are weirdly sized so I can't use enemy_width and enemy_height here
				Enemy e = new Enemy(j * 75 + 30, 120+i*60, enemy_width-4, enemy_height-4, enemy2, 2, enemies);
				enemies2.add(e);
				enemies.add(e);
			}
		}
		for(int i = 0; i<3; i++){
			for(int j=0; j<10; j++){
				//same issue with image files
				Enemy e = new Enemy(j * 75 + 30, 240+i*60, enemy_width-4, enemy_height-4, enemy1, 1, enemies);
				enemies1.add(e);
				enemies.add(e);
			}
		}
		mystery_spawn();
	}
	/* mystery_spawn():
	Return type : void
	Parameters: none
	Description: this method is called to spawn a mystery ship. It resets the timer for spawning the mystery ship and randomizes the delay until the next mystery ship, which is between 20 and 35 secs
	 */

	private void mystery_spawn(){
		last_mystery_spawn = System.currentTimeMillis();
		mystery_delay = 20000 + (int)(Math.random() * 15000);
	}

	/* has_active_mystery_ship():
	Return type : boolean
	Parameters: none
	Description: checks if there is currently an active mystery ship on the screen
	 */
	private boolean has_active_mystery_ship(){
		for(Enemy e : enemies){
			if(e.is_mystery_ship() && e.alive){
				return true;
			}
		}
		return false;
	}

	/* ship_explosion():
	Return type : boolean
	Parameters: none
	Description: checks if the ship is currently exploding so that i dont also draw the ship image while its exploding and the animation can work properly.
	 */

	private boolean ship_explosion() {
		for(Particles p : particles) {
			if(p.type == 0 && p.active) return true;
		}
		return false;
	}

	/* maybe_spawn_mystery_ship():
	Return type : void
	Parameters: none
	Description: checks if a mystery ship should be spawned by checking if there is already an active mystery ship in the game or if enough time has passed
	for another one to spawn. The mystery ship is also added to the enemies array so it can be drawn.
	 */

	private void maybe_spawn_mystery_ship(){
		if(has_active_mystery_ship()){
			return;
		}
		if(mystery_shot && curr_lvl == 1){
			return;
		}
		long now = System.currentTimeMillis();
		if(now - last_mystery_spawn >= mystery_delay){
			Enemy mystery_ship = new Enemy(-enemy_width*2, 25, enemy_width*2, enemy_height, enemy4, 4, enemies);
			enemies.add(mystery_ship);
			mystery_spawn();
			Particles.play_sound("mystery_enemy");
		}
	}
	/* ship_explosion():
	Return type : void
	Parameters: none
	Description: Handles all movement in the game including objects of the classes Ship, Enemy, Projectile, and Particles. Also handles the logic for when to spawn the mystery ship, when to move to the next level, and when the game is over or won.
	It does this by looping over all objects of these classes, checking if those objects are alive/active and then calling their move methods. It also checks for collisions between projectiles and the ship, enemies, and shields and handles those accordingly.
	Lastly, it calls the enemy_power and enemy_attack methods to make the game more difficult as the player kills more enemies and to make the enemies shoot at the player.
	*/
	public void move(){
		if(screen == INTRO){
			
		}
		else if(screen == GAME){
			//level 2
			if(!game_over && enemies.isEmpty()){
				Enemy.descent_count = 0;
				make_enemies();
				curr_lvl = 2;
				mystery_shot = false; //reset mystery_shot so that the mystery ship can spawn in level 2
			}
			maybe_spawn_mystery_ship();
			for(int i = enemies.size() - 1; i >= 0; i--) {
				Enemy e = enemies.get(i);
				e.check_hit(projectiles);
				if(e.alive){
					e.move();
				}
				else{
					enemies.remove(i);
				}
			}
			if(ship.is_alive()){
				ship.check_hit(projectiles);
			}
			if(!ship.is_alive()){
				if(Ship.lives <= 0 && !ship.can_respawn()){
					game_over = true;

				} else if(ship.can_respawn()){
					ship.respawn();
				}
			}
			enemy_power();
			enemy_attack();
			check_shield_hit(projectiles, 100, 630, ship_width, 90, shield1);
			check_shield_hit(projectiles, 390, 630, ship_width, 90, shield2);
			check_shield_hit(projectiles, 670, 630, ship_width, 90, shield3);
			ship.move(keys, projectiles);
			
			for(int i = projectiles.size() - 1; i >= 0; i--){
				Projectile p = projectiles.get(i);
				p.move();
				if(!p.is_active()) {
					projectiles.remove(i);
				}
			}
			for(int i = particles.size() - 1; i >= 0; i--){
				Particles part = particles.get(i);
				part.move();
				if(!part.active) {
					particles.remove(i);
				}
			}
		if(game_over){
			Particles.play_sound("game_over");
			screen = LOST;
		}
		else if(curr_lvl == 2 && enemies.isEmpty() && ship.is_alive()){
			screen = WON;
			Particles.play_sound("victory");
		}
		}
		
	}
	
	/* count_enemy_shots():
	Return type : int
	Parameters: none
	Description: counts the number of active enemy shots in the game by looping through all projectiles and checking if they are active and enemy shots. This is used to limit the number of enemy shots on the screen at once to make the game more playeable 
	 */

	private int count_enemy_shots(){
		int count = 0;
		for(Projectile p : projectiles){
			if(p.is_active() && p.is_enemy_shot()){
				count++;
			}
		}
		return count;
	}

	/* enemy_attack():
	Return type : void
	Parameters: none
	Description: handles the logic for enemy attacks. First it checks how many active enemy shots there are using the method above and if there are already enough shots on the screen, it doesn't do anything. 
	If there is room for more enemy shots, it selects a random enemy from the enemies array and checks if that enemy can shoot (enemies can only shoot every few seconds based on their attack speed). 
	If the enemy can shoot, it creates a new projectile and adds it to the projectiles array.
	*/

	private void enemy_attack(){
		int curr_shots = count_enemy_shots();
		if(curr_shots >= Enemy.frequency) {
			return;
		}

		if(enemies.isEmpty()){
			return;
		}
		//select a random enemy
		Enemy selected = enemies.get((int)(Math.random() * enemies.size()));
		if(!selected.alive || selected.is_mystery_ship()){
			return;
		}
		long now = System.currentTimeMillis();
		if(selected.can_shoot(now)){
			selected.shoot(projectiles);
		}
	}

	/* check_shield_hit(projectiles, x, y, width, height, img):
	Return type : void
	Parameters: ArrayList<Projectile> projectiles, int x, int y, int width, int height, BufferedImage img
	Description: This method is used to check if there is a PRECISE pixel collision betw the shields and any projectile (both enemy and from the player itself).
	It loops through all projectiles and checks if they intersect with the rectangle around the shield. If they do, it then checks for a more precise collision by checking if the 
	pixels of the projectile and the shield actually overlap (cause the images have a lot of transparent space).
	If there is a collision, it creates a hole in the shield by setting the pixels of the shield image to transparent in a radius (determined by the projectile's damage) around the point of collision.
	If the same spot is hit multiple times (determined by checking if the pixels around the collision point already are transparent), the hole grows bigger. 
	The final radius is the the current radius + projectiles damage/3 (i wanted to make it smaller but for some reason the hole just didn't grow if i made it /4 or even smaller, and just the damage alone was way too big).
 */

	public void check_shield_hit(ArrayList<Projectile> projectiles, int x, int y, int width, int height, BufferedImage img) {
    for (int i = projectiles.size() - 1; i >= 0; i--){
        Projectile p = projectiles.get(i);
        if(!p.is_active()) continue;
        if(new Rectangle(x, y, width, height).intersects(new Rectangle(p.x, p.y, p.width, p.height))) {
            if(Util.pixel_collide(x, y, img, p.x, p.y, p.img)){
                int holeX = (p.x + p.width / 2) - x;
                int holeY = (p.y + p.height / 2) - y;
                final int BLANK = 0x00000000;
                int curr_radius = 0;
                int max_rad = 15;
                for(int r = 1; r < max_rad; r++){
                    // if any of the 4 points are blank then the current radius becomes r. Initially when there is no hole at that spot, the current radius will be 
					// 0, so the final radius will become the projectile's damage. But, if you shoot at the same spot again, the final radius of the shot will grow since
					//curr_radius will be set to the previous projectile's damage. 
                    if(Util.is_transparent(img, holeX + r, holeY) || Util.is_transparent(img, holeX - r, holeY) ||
                        Util.is_transparent(img, holeX, holeY + r) && Util.is_transparent(img, holeX, holeY - r)) {
                        curr_radius = r;
                    }
					else{
                        break; 
                    }
                }

                int final_radius = curr_radius+p.get_damage()/3;

                for(int hx = holeX - final_radius; hx < holeX + final_radius; hx++) {
                    for(int hy = holeY - final_radius; hy < holeY + final_radius; hy++) {
                        if(hx >= 0 && hx < img.getWidth() && hy >= 0 && hy < img.getHeight()) {
                            img.setRGB(hx, hy, BLANK);
                        }
                    }
                }
                p.kill(); //kill the projectile after it hits the shield
            }
        }
    }
}

/* draw_stats(Graphics g):
 * Return type: void
 * Parameters: Graphics g
 * Description: draws the game stats (# of lives and score) on the screen.
 */

public void draw_stats(Graphics g){
	g.setFont(font);
	g.setColor(Color.white);
	g.drawString("Lives :  " + Ship.lives, 20, 30);
	g.drawString("Score :  " + score, 700, 30);
}

/* enemy_power():
 * Return type: void
 * Parameters: none
 * Description: as in the original space invaders, the enemies speed up and shoot more frequently as the player kills more of them. 
 * This method handles that logic by checking how many enemies are left and how many times they have descended and adjusting the speed and attack frequency accordingly
 */

public void enemy_power(){
	if(enemies.size() < 20){
		Enemy.frequency = 6;
		Enemy.speed = 5;
		Enemy.attack_speed = Math.max(500, Enemy.attack_speed - 1000);
	} else if(enemies.size() < 30){
		Enemy.frequency = 6;
		Enemy.speed = 4;
		Enemy.attack_speed = Math.max(500, Enemy.attack_speed - 1000);
	} else if(Enemy.descent_count >= 3 || enemies.size() < 40){
		Enemy.frequency = 5;
		Enemy.speed = 2;
		Enemy.attack_speed = Math.max(500, Enemy.attack_speed - 1000);
	}
}

/* reset_everything():
 * Return type: void
 * Parameters: none
 * Description: resets all game elements to their initial states to prep for a new game when the user clicks replay
 */

	public void reset_everything(){
		game_over = false;
		score = 0;
		curr_lvl = 1;
		ship = new Ship(ship_x, ship_y, ship_width, ship_height, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_SPACE);
		enemies1.clear();
		enemies2.clear();
		enemies3.clear();
		enemies.clear();
		projectiles.clear();
		particles.clear();
		mystery_shot = false;
		Enemy.frequency = 3;
		Enemy.speed = 1;
		Enemy.attack_speed = 5000;
		Enemy.descent_count = 0;
		shield1 = Util.loadBuffImage("assets/shield.png", ship_width, 90);
		shield2 = Util.loadBuffImage("assets/shield.png", ship_width, 90);
		shield3 = Util.loadBuffImage("assets/shield.png", ship_width, 90);
}

	@Override
	public void actionPerformed(ActionEvent e){
		move(); 
		repaint(); 

	}
	
	@Override
	public void keyReleased(KeyEvent ke){
		int key = ke.getKeyCode();
		keys[key] = false;
	}	
	
	@Override
	public void keyPressed(KeyEvent ke){
		int key = ke.getKeyCode();
		keys[key] = true;
	}
	
	@Override
	public void keyTyped(KeyEvent ke){}
	@Override
	public void	mouseClicked(MouseEvent e){}

	@Override
	public void	mouseEntered(MouseEvent e){}

	@Override
	public void	mouseExited(MouseEvent e){}

	@Override
	public void	mousePressed(MouseEvent e){
		if(screen == INTRO){
			screen = GAME;
			reset_everything();
			Particles.play_sound("intro");
			make_enemies();
		}	
		if(screen == LOST || screen == WON){
			screen = INTRO;
	        reset_everything();
		}
	}

	@Override
	public void	mouseReleased(MouseEvent e){}

	@Override
	/* paint(Graphics g):
	 * Return type: void
	 * Parameters: Graphics g
	 * Description: draws all game elements on to the screen based on the current screen (intro, game, lost, won).
	 * If the current screen is the game screen, it draws the ship, enemies, projectiles and particles (if they are alive), shields, and stats. 
	 * If the current screen is intro/lost/won, it just draws the corresponding image
	 */

	public void paint(Graphics g){
		if(screen == INTRO){
            g.drawImage(intro, 0,0, null);
		}

		else if(screen == GAME){
			g.setColor(Color.black);
			g.fillRect(0,0,getWidth(), getHeight());	

			
			for(int i = enemies.size() - 1; i >= 0; i--) {
				Enemy e = enemies.get(i);
				if(e.alive){
					e.draw(g);
				}else{
					enemies.remove(i);
				}
			}
			
			if(!ship_explosion()){
				ship.draw(g);	
			}
			g.drawImage(shield1,100,630,null); 	
			g.drawImage(shield2,390,630,null); 	
			g.drawImage(shield3,670,630,null); 

			for(int i = projectiles.size() - 1; i >= 0; i--) {
				Projectile p = projectiles.get(i);
				if(p.is_active()){
					p.draw(g);
				}
				else{
					projectiles.remove(i);
				}
			}
			for(int i = particles.size() - 1; i >= 0; i--) {
				Particles part = particles.get(i);
				if(part.active){
					part.draw(g);
				}
				else{
					particles.remove(i);
				}
			}

			draw_stats(g);
		}
		if(screen == LOST){
            g.drawImage(lost, 0,0, null);
		}
		else if(screen == WON){
			g.drawImage(won, 0,0, null);
		} 
	
    }
    
	//used to set the boolean that tracks if the mystery ship has been shot for level 1, which is used to determine if another mystery ship should spawn since in the original game there can only be one mystery ship and if you shoot it, another one won't spawn for the rest of the level
    public static void set_mystery_shot(){
        mystery_shot = true;
    }
    
    public static void main(String[] arguments) {
		new Space();		
    }

}
