package Game_Characters.Enemies;

import Game_Characters.Enemy;
import Game_Characters.Player;
//import Game_Characters.CharacterData;

//Methods List:
// - uniqueBehavior():

//Abstract Methods (Implemented):
// - run():
// - takeDamage(int damage):
// - dealDamage():
// - updateCharacterData():

public class TickiBird extends Enemy {
    // Implementation for TickiBird enemy
    private int counter = 0;

    //constructor
    public TickiBird(Player targetPlayer) {
        super("TickiBird", 10, 15, 10, 7, 10,  //stats (HP, ATK, Stealth, EXP, Gold)
        "A strange bird that craves attention and has an EXPLOSIVE personality! three turns and you're out...", //check message
        targetPlayer); //player
    }

    //Overrides
    @Override
    public synchronized void run() {
       System.out.println("TickiBird flutters menacingly.");
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
        System.out.println("TickiBird takes " + damage + " damage! Remaining health: " + health);
        if (health <= 0) {
            isAlive = false;
            System.out.println("TickiBird has been defeated!");
        }
    }

    @Override
    public int dealDamage() {
        int trueAttack = uniqueBehavior();
        System.out.println("TickiBird attacks " + getTargetPlayer().getName() + " for " + trueAttack + " damage!");
        return trueAttack;
    }

    @Override
    public CharacterData updateCharacterData() {
        this.characterData.health = this.health;
        this.characterData.maxHealth = this.maxHealth;
        this.characterData.attack = this.attack;
        this.characterData.stealth = this.stealth;
        return this.characterData;
    }

    //Unique behavior
    public int uniqueBehavior() {
        counter++;
        if (counter % 3 == 0) {
            System.out.println("TickiBird goes KABOOM!");
            return attack;
        }

        System.out.println("Tickibird watches closely.... Tick Tock Tick Tock.");
        return 0;
    }
}