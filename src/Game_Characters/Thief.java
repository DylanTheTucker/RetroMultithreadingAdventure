package Game_Characters;

//Abstract Methods (Implemented):
// - performCustomBehavior():
// - stepForward():
// - takeDamage(int damage):
// - dealDamage():
// - determineSteps():
// - updateCharacterData():
// - getCurrentStep():
// - setCurrentStep(int step):

public class Thief extends CharacterEnemy {
    
    public Thief(Player targetPlayer) {
        super("Thief", 60, 7, 20, 15, 25, //stats (HP, ATK, Stealth, EXP, Gold)
        "A tough thief with a decent attack. With a sntealth of 20, running away might take some time.",  //check message
        targetPlayer);  //player

        // Set initial position to step 25 (will move toward player from here)
        this.setCurrentStep(25);  
        this.setStepRange(-1, -1);  // Move backwards by 1 step when player is behind
        this.setChaseForwardSteps(2);  // Skip 2 steps when chasing forward
        this.setFreezeTurns(3);  // Freeze for 3 turns after player flees
    }

    //OVERRRIDES

    @Override
    public void performCustomBehavior() {
        // Custom behavior for thief (if needed in the future)
        // Battle encounters are handled by GameManager.checkCharacterEnemyEncounters()
    }

    @Override
    public void stepForward() {
        if (isAlive) {
            int movement = getIntelligentMovement();
            if (movement < 0) {
                // Moving backward
                currentStep += movement;
                System.out.println(name + " stalks backward to step " + currentStep + ".");
            } else if (movement > 0) {
                // Chasing forward
                currentStep += movement;
                System.out.println(name + " dashes forward to step " + currentStep + " in pursuit!");
            } else {
                // No movement (already at player position)
                System.out.println(name + " waits at step " + currentStep + ".");
            }
        }
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
        System.out.println(name + " the Thief takes " + damage + " damage! Remaining health: " + health);
        if (health <= 0) {
            isAlive = false;
            System.out.println(name + " the Thief has been defeated!");
        }
    }

    @Override
    public int dealDamage() {
        System.out.println(name + " the Thief attacks for " + attack + " damage!");
        return attack;
    }

    @Override
    public int determineSteps() {
        // Always return 1 - the intelligent movement in stepForward() handles direction and distance
        return 1;
    }

    @Override
    public CharacterData updateCharacterData() {
        this.characterData.health = this.health;
        this.characterData.maxHealth = this.maxHealth;
        this.characterData.attack = this.attack;
        this.characterData.stealth = this.stealth;
        return this.characterData;
    }

    //Interface methods
    @Override
    public int getCurrentStep() {
        return currentStep;
    }

    @Override
    public void setCurrentStep(int step) {
        this.currentStep = step;
    }

    // ===== Enemy Behavior Implementation =====
    
}
