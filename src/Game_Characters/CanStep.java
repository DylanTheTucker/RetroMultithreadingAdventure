package Game_Characters;

//Interface to make a character able to step/move

//Abstract Methods:
// - determineSteps(): 
// - getCurrentStep():
// - setCurrentStep(int step):
// - stepForward():

public interface CanStep {
    /**
     * Determines how many steps this character will take in the current turn.
     * This method allows for different movement patterns between character types:
     * - Players might move a fixed number of steps
     * - Enemies might move randomly or based on AI logic
     * 
     * @return The number of steps this character will move
     */
    public abstract int determineSteps();

    /**
     * Gets the current step position of this character.
     * This is used by the game manager to determine encounters
     * and coordinate character interactions.
     * 
     * @return The current step position (0-based indexing)
     */
    public abstract int getCurrentStep();

    /**
     * Sets the current step position of this character.
     * This method is typically used for initialization or
     * when external forces move the character.
     * 
     * @param step The new step position to set
     */
    public abstract void setCurrentStep(int step);

    void stepForward();
}
