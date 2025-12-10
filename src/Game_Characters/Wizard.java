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

public class Wizard extends CharacterEnemy {
    
    public Wizard(Player targetPlayer) {
        super("Wizard", 100, 15, 0, 100, 100, //stats (HP, ATK, Stealth, EXP, Gold)
        "The wizard of the lowest realms... defeat this enemy to complete your jouney! If you are too weak, don't be afraid to flee!",  //check message
        targetPlayer); //player

        // Set initial position to step 25 (will move toward player from here)
        this.setCurrentStep(15);
        this.setStepRange(-1, -1);  // Move backwards by 1 step when player is behind
        this.setChaseForwardSteps(3);  // Skip 3 steps when chasing forward (faster than Thief!)
        this.setFreezeTurns(4);  // Freeze for 4 turns after player flees
    }

    @Override
    public void performCustomBehavior() {
        // Wizard's custom behavior can be added here
        // Battle encounters are handled by GameManager.checkCharacterEnemyEncounters()
    }

    @Override
    public void stepForward() {
        if (isAlive) {
            int movement = getIntelligentMovement();
            if (movement < 0) {
                // Moving backward
                currentStep += movement;
                System.out.println(name + " floats backward to step " + currentStep + ".");
            } else if (movement > 0) {
                // Chasing forward
                currentStep += movement;
                System.out.println(name + " teleports forward to step " + currentStep + " with magic!");
            } else {
                // No movement (already at player position)
                System.out.println(name + " levitates at step " + currentStep + ".");
            }
        }
    }

    @Override
    public void takeDamage(int damage) {
        health -= damage;
        System.out.println(name + " the Wizard takes " + damage + " damage! Remaining health: " + health);
        if (health <= 0) {
            isAlive = false;
            System.out.println(name + " the Wizard has been defeated!");
        }
    }

    @Override
    public int dealDamage() {
        System.out.println(name + " the Wizard casts a spell for " + attack + " damage!");
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

    @Override
    public int getCurrentStep() {
        return currentStep;
    }

    @Override
    public void setCurrentStep(int step) {
        this.currentStep = step;
    }
}
