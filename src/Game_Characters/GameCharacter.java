package Game_Characters;
//Helper class CharacterData to store character information

//Methods List:
// - takeDamage(int damage): Reduces health by damage amount
// - dealDamage(GameCharacter target): Deals damage to target character 
// - heal(int healAmount): Increases health by healAmount up to maxHealth
// - updateCharacterData(): Updates and returns CharacterData object

//Getters List:
// - getName(): Returns character name
// - getHealth(): Returns current health
// - getStep(): Returns current step
// - getAttack(): Returns attack value  
// - getStealth(): Returns stealth value
// - getAlive(): Returns alive status
// - getDataObject(): Returns CharacterData object

//Setters List:
// - setName(String name): Sets character name
// - setHealth(int health): Sets current health
// - setStep(int step): Sets current step
// - setAttack(int attack): Sets attack value
// - setStealth(int stealth): Sets stealth value
// - setAlive(boolean alive): Sets alive status
// - setDataObject(): Updates CharacterData object with current values
// - setWithDataObject(CharacterData data): Sets character attributes from CharacterData object

public abstract class GameCharacter implements Runnable{
    //Variables
    protected String name;
    protected int health;
    protected int maxHealth;
    protected int attack;
    protected int stealth;
    protected int currentStep;
    protected Boolean isAlive;


    protected CharacterData characterData;
  

    public GameCharacter(String name, int maxHealth, int attack, int stealth) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attack = attack;
        this.stealth = stealth;
        this.currentStep = 0;
        this.isAlive = true;
        this.characterData = new CharacterData(name, health, maxHealth, attack, stealth);
    }

    /*
    * ALL CLASS FUNCTIONS
    */

    //Overrides
    @Override
    public abstract void run();

    //Abstract methods

    //Damage handlers
    public abstract void takeDamage(int damage);
    public abstract int dealDamage();

    public void heal(int healAmount) {
        if (healAmount > 0 && isAlive) {
            health = Math.min(health + healAmount, maxHealth);
            System.out.println(name + " heals for " + healAmount + " HP! Current health: " + health);
        }
    }

    //Update Character data
    public abstract CharacterData updateCharacterData();

    //Getters
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getStep() { return currentStep; }
    public int getAttack() { return attack; }
    public int getStealth() { return stealth; }
    public boolean getAlive() { return isAlive; }
    public CharacterData getDataObject() { return characterData; }

    //Setters
    public void setName(String name) { this.name = name; }
    public void setHealth(int health) { this.health = health; }
    public void setStep(int step) { this.currentStep = step; }
    public void setAttack(int attack) { this.attack = attack; }
    public void setStealth(int stealth) { this.stealth = stealth; }
    public void setAlive(boolean alive) { isAlive = alive; }

    public void setDataObject() { 
        this.characterData = new CharacterData(name, health, maxHealth, attack, stealth); 
    }

    public void setWithDataObject(CharacterData data) {
        this.name = data.name;
        this.health = data.health;
        this.maxHealth = data.maxHealth;
        this.attack = data.attack;
        this.stealth = data.stealth;
        this.characterData = data;
    }

    /*
    * DATAB OBJECT CLASS
    */

    public class CharacterData {
        public String name;
        public int health;
        public int maxHealth;
        public int attack;
        public int stealth;

        public CharacterData(String name, int health, int maxHealth, int attack, int stealth) {
            this.name = name;
            this.health = health;
            this.maxHealth = maxHealth;
            this.attack = attack;
            this.stealth = stealth;
        }

        public String toString() {
            return "Name: " + name 
            + ", Health: " + health + "/" + maxHealth 
            + ", Attack: " + attack 
            + ", Stealth: " + stealth;
        }
    }
}


