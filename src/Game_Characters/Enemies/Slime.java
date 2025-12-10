package Game_Characters.Enemies;

import Game_Characters.Enemy;
import Game_Characters.Player;
//import Game_Characters.CharacterData;

//Abstract Methods (Implemented):
// - run():
// - takeDamage(int damage):
// - dealDamage():
// - updateCharacterData():

public class Slime extends Enemy {
    
    //Constructor
    //Overrides initial arguments so all slimes are the same

    private int id;

    public Slime(Player targetPlayer, int id) {  //remove ID later
     
        super("Slime #" + id, 5, 1, 0, 2, 3,   //Stats (HP, ATK, Stealth, EXP, Gold)
        "This is a slime. Weak but persistent, would be a lovely fellow if it weren't acidic.",  //check message
        targetPlayer); //player
        this.id = id;

    }

    //Overrides
    @Override
    public synchronized void run() {
       System.out.println("Slime #" + id + " jiggles forward menacingly.");
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
        System.out.println("Slime #" + id + " takes " + damage + " damage! Remaining health: " + health);
        if (health <= 0) {
            isAlive = false;
            System.out.println("Slime #" + id + " has been defeated!");
        }
    }

    @Override
    public int dealDamage() {
        System.out.println("Slime #" + id + " attacks " + getTargetPlayer().getName() + " for " + attack + " damage!");
        return attack;
    }

    @Override
    public CharacterData updateCharacterData() {
        this.characterData.health = this.health;
        this.characterData.maxHealth = this.maxHealth;
        this.characterData.attack = this.attack;
        this.characterData.stealth = this.stealth;
        return this.characterData;
    }

}
