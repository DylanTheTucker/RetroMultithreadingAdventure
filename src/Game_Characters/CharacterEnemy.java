package Game_Characters;

import GameHandlers.GameManager;
import java.util.Random;

//Abstract class for all enemy characters that can move/step in the game world
//In the future, moving NPCs could have their own abstract class following a similar but personalized pattern
//Implements canStep for movement behavior

//Methods List:
// - setGameManager(GameManager gameManager):
// - setStepRange(int min, int max):
// - setChaseForwardSteps(int steps):
// - setFreezeTurns(int turns):
// - freezeAfterFlee():  ensures that the enemy is frozen for a set number of turns after the player flees
// - run():  main thread method for the enemy character
// - randomStep(int min, int max):
// - isAtPlayerStep():  
// - getIntelligentMovement():  determines movement direction based on player position

//Abstract Methods (Implemented):
// - stepForward():
// - determineSteps():
// - getCurrentStep():
// - setCurrentStep(int step):

//Abstract Methods (Not Implemented):
// - performCustomBehavior():   hook for subclasses to implement custom behavior each turn
// - takeDamage(int damage):
// - dealDamage():
// - updateCharacterData():

public abstract class CharacterEnemy extends Enemy implements CanStep {
    
    protected GameManager gameManager;
    protected Random random;
    protected int minStepsPerTurn;
    protected int maxStepsPerTurn;
    protected int chaseForwardSteps; // Steps to skip when chasing player forward
    protected int turnsToFreeze; // Turns to freeze after player flees
    protected int frozenTurnsRemaining; // Counter for remaining frozen turns
    
    public CharacterEnemy(String name, int maxHealth, int attack, int stealth, int ExpOnDefeat, int goldValue, String checkMessage, Player targetPlayer) {
        super(name, maxHealth, attack, stealth, ExpOnDefeat, goldValue, checkMessage, targetPlayer);
        this.random = new Random();
        this.minStepsPerTurn = 1;  // Default: move 1-2 steps per turn
        this.maxStepsPerTurn = 2;
        this.chaseForwardSteps = 2;  // Default: skip 2 steps when chasing forward
        this.turnsToFreeze = 3;  // Default: freeze for 3 turns after player flees
        this.frozenTurnsRemaining = 0;
    }
    
    /**
     * Sets the GameManager reference so this enemy can synchronize with game steps
     */
    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    /**
     * Sets the movement range for this character enemy
     * @param min Minimum steps per turn
     * @param max Maximum steps per turn
     */
    public void setStepRange(int min, int max) {
        this.minStepsPerTurn = min;
        this.maxStepsPerTurn = max;
    }
    
    /**
     * Sets how many steps this enemy skips when chasing forward
     * @param steps Number of steps to skip when player is ahead
     */
    public void setChaseForwardSteps(int steps) {
        this.chaseForwardSteps = steps;
    }
    
    /**
     * Sets how many turns this enemy freezes after player flees
     * @param turns Number of turns to remain frozen
     */
    public void setFreezeTurns(int turns) {
        this.turnsToFreeze = turns;
    }
    
    /**
     * Freezes this enemy for the configured number of turns.
     * Called when player successfully flees.
     */
    public void freezeAfterFlee() {
        this.frozenTurnsRemaining = turnsToFreeze;
        System.out.println(">>> " + name + " is stunned and can't pursue for " + turnsToFreeze + " turns!");
    }
    
    /**
     * Main run method for the CharacterEnemy thread.
     * Synchronizes with game steps and moves the enemy around.
     */
    @Override
    public void run() {
        System.out.println(name + " enters the realm...");
        try {
            while (isAlive && gameManager != null) {
                // Wait for the next game step - enemies wait for SECOND notification (even number)
                synchronized(gameManager.getStepLock()) {
                    int currentGameStep = gameManager.getGlobalStepCounter().get();
                    int startNotificationCount = gameManager.getNotificationCounter().get();
                    
                    // Wait until we get an even notification number (after player has moved)
                    while (gameManager.getNotificationCounter().get() <= startNotificationCount || 
                           gameManager.getNotificationCounter().get() % 2 != 0) {
                        gameManager.getStepLock().wait();
                    }
                    
                    // Exit if game step hasn't advanced (game ending)
                    if (gameManager.getGlobalStepCounter().get() <= currentGameStep) {
                        break;
                    }
                }
                
                // Wait if a battle is in progress
                while (gameManager.isBattleInProgress()) {
                    Thread.sleep(100);
                }
                
                // Check if frozen (stunned after player flee)
                if (frozenTurnsRemaining > 0) {
                    frozenTurnsRemaining--;
                    System.out.println(name + " is still recovering... (" + frozenTurnsRemaining + " turns remaining)");
                    // Perform custom behavior but don't move
                    performCustomBehavior();
                    continue;
                }
                
                // Determine and take steps
                int steps = determineSteps();
                int absSteps = Math.abs(steps);
                for (int i = 0; i < absSteps && isAlive; i++) {
                    stepForward();
                    Thread.sleep(random.nextInt(300) + 100);
                }
                
                // Perform any custom behavior for this enemy type
                performCustomBehavior();
            }
            
            if (isAlive) {
                System.out.println(name + " retreats into the shadows...");
            }
        } catch (InterruptedException e) {
            if (isAlive) {
                System.out.println(name + " vanishes mysteriously...");
            }
        }
    }
    
    /**
     * Hook method for subclasses to implement custom behavior each turn
     */
    protected abstract void performCustomBehavior();
    
    // ===== CanStep Interface Implementation =====
    
    @Override
    public void stepForward() {
        if (isAlive) {
            currentStep++;
            System.out.println(name + " moves to step " + currentStep + ".");
        }
    }
    
    @Override
    public int determineSteps() {
        // Random steps within the configured range
        return randomStep(minStepsPerTurn, maxStepsPerTurn);
    }
    
    @Override
    public int getCurrentStep() {
        return currentStep;
    }
    
    @Override
    public void setCurrentStep(int step) {
        this.currentStep = step;
    }
    
    // ===== Helper Methods =====
    
    /**
     * Generates a random step count between min and max (inclusive)
     * @param min Minimum number of steps
     * @param max Maximum number of steps  
     * @return Random number of steps to take
     */
    protected int randomStep(int min, int max) {
        if (min >= max) return min;
        return random.nextInt(max - min + 1) + min;
    }
    
    /**
     * Checks if this enemy is at the same step as the target player
     * @return true if enemy and player are at the same step
     */
    protected boolean isAtPlayerStep() {
        return targetPlayer != null && currentStep == targetPlayer.getStep();
    }
    
    /**
     * Intelligently determines movement direction based on player position.
     * If player is behind (lower step), move backward slowly.
     * If player is ahead (higher step), chase forward quickly.
     * @return Number of steps to move (negative = backward, positive = forward)
     */
    protected int getIntelligentMovement() {
        if (targetPlayer == null) {
            return minStepsPerTurn; // Default movement
        }
        
        int playerStep = targetPlayer.getStep();
        
        if (playerStep < currentStep) {
            // Player is behind - move backward slowly (default behavior)
            return -randomStep(Math.abs(minStepsPerTurn), Math.abs(maxStepsPerTurn));
        } else if (playerStep > currentStep) {
            // Player is ahead - chase forward quickly
            return chaseForwardSteps;
        } else {
            // At same position - don't move
            return 0;
        }
    }
}
