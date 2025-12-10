package Game_Characters;

//Class that sets up all enemies. An enemy is any character that can battle against the player.

//Methods List:
// - reset():  resets an enemy prefab when needed

//Abstract Methods (Not Implemented):
// - run():
// - takeDamage(int damage):
// - dealDamage():
// - updateCharacterData():

//Getters:
// - getCheckMessage():  a check message is the description shown when the player inspects the enemy
// - getTargetPlayer():
// - getExpOnDefeat():
// - getGoldValue():  returns the gold value of defeating this enemy

public abstract class Enemy extends GameCharacter {
    
    protected Player targetPlayer;
    protected int ExpOnDefeat;
    protected int goldValue; // Gold awarded when enemy is defeated (shared resource)
    protected String checkMessage;

    //constructor
    public Enemy(String name, int maxHealth, int attack, int stealth, int ExpOnDefeat, int goldValue, String _checkMessage, Player targetPlayer) {
        super(name, maxHealth, attack, stealth);
        this.targetPlayer = targetPlayer;
        this.ExpOnDefeat = ExpOnDefeat;
        this.goldValue = goldValue;
        this.checkMessage = _checkMessage;
    }

    //Getters and Setters
    public String getCheckMessage() {
        return checkMessage;
    }

    //Overrides
    @Override
    public abstract void run();

    //Abstract methods
    @Override
    public abstract void takeDamage(int damage);

    @Override
    public abstract int dealDamage();
    
    @Override
    public abstract CharacterData updateCharacterData();

    //get target player
    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public int getExpOnDefeat() {
        return ExpOnDefeat;
    }
    
    /**
     * Gets the gold value awarded when this enemy is defeated (shared resource).
     * @return Gold value
     */
    public int getGoldValue() {
        return goldValue;
    }
    
    /**
     * Resets the enemy to its initial state (full health, alive).
     * Used for non-CharacterEnemy types like Slime to reuse instances.
     */
    public void reset() {
        this.health = this.maxHealth;
        this.isAlive = true;
    }

}
