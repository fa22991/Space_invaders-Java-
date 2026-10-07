/*
Particles.java
Fatima Ali
THis is a special effects class that is used to create explosion particles when the ship or an enemy gets hit. It has variables for the particle's position, size, type (0 for player explosion, 1 for enemy explosion), image, animation frame, and start time (used to determine how long the particle has been active for).
 It also handles the sound effects for player and enemy deaths, player shots and the mystery ship. The move method updates the animation frame and image of the particle based on how long it has been active for, and deactivates it after a certain amount of time has passed (when the animation has finished).
 */
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


import org.w3c.dom.css.Rect;
public class Particles {
    int x, y, width, height, type;
    private Image img;
    boolean active;
    private double frame;
    long start_time;
    private boolean sound_played;

    /* Constructor 
    Return type: none
    Parameters: int x, int y, int width, int height, int type, long start_time
    Description: initializes a new Particle object with the specified position, size, type, and start time. It also has a flag for whether the sound effect for the particle has been played, which is used to ensure the sound only plays once when the particle is created. 
    The image for the particle is determined by its type and starts at the first frame of the animation cycle. The particle is set to active when it's created.
     */
    
    public Particles(int x, int y, int width, int height, int type, long start_time) {
        this.x = x; 
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.active = true;
        this.frame = 1;
        this.start_time = start_time;
        this.sound_played = false;
    }
    /* draw():
    return type: void
    Parameters: Graphics g
    Description: draws the particle on the screen using its current image and position
    */
    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }

    /* move():
    return type: void
    Parameters: none
    Description: selects which sound and explosion animation to play based on the type of particle (enemy or player) and whether it is active.
    */
    public void move() {
        if(active && type == 0){
            if(!sound_played){
                play_sound("player_explosion");
                sound_played = true;
            }
            explosion_player();
        }
        else if(active && type == 1){
            if(!sound_played){
                play_sound("enemy_explosion");
                sound_played = true;
            }
            explosion_enemy();
        }
    }
    /* explosion_enemy():
    Return type: void
    Parameters: none
    Description: updates the animation frame and image for the enemy explosion particle
     */
    public void explosion_enemy(){
        frame+=1;
        if (frame > 10) {
            active = false; //deactivate the particle after the animation has finished
        }
        img = new ImageIcon(String.format("assets//Explosion1/Explosion1_%d.png", (int)frame)).getImage();
    }
    /* explosion_player():
    Return type: void
    Parameters: none
    Description: updates the animation frame and image for the player explosion particle
     */

    public void explosion_player(){
        frame+=0.3;
        if (frame > 6) {
            active = false;
        }
        img = new ImageIcon(String.format("assets//Explosion0/%d.png", (int)frame)).getImage();
    }

    /* play_sound():
    Return type: void
    Parameters: String occasion (the name of the sound effect to play, which corresponds to the type of particle and the event that triggered it)
    Description: plays the specified sound effect
     */
    public static void play_sound(String occasion){
        String path = String.format("assets/Sounds/%s.wav", occasion);
        try {
        File soundFile = new File(path);
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        clip.start();
    } catch (Exception e) {
        e.printStackTrace();
    }
        
    }
    
    
}
