package Game_Characters.PlayerInput;

import java.util.Scanner;

// A handler for managing player input during step advancement and interactions

//Methods List:
// - waitForStepAdvance(Interaction interaction):  waits for player to press Enter to advance or handles interaction
// - handleInteraction(Interaction interaction):  handles a generic interaction
// - getValidChoice(int min, int max):   gets a valid integer choice from the player within the specified range
// - handleBattleInteraction(Interaction battleInteraction):   handles a battle interaction

//Getters:
// - getInputLock():  gets the input lock for synchronization
// - isWaitingForInput():  checks if the handler is waiting for input
// - getLastChoice():  gets the last choice made by the player

public class PlayerInputHandler {
    private Scanner scanner;
    private final Object inputLock = new Object();
    private boolean waitingForInput = false;
    private int lastChoice = -1;
    private Interaction currentInteraction = null;
    
    public PlayerInputHandler(Scanner scanner) {
        this.scanner = scanner;
    }
    
    /**
     * Gets the input lock for synchronization
     */
    public Object getInputLock() {
        return inputLock;
    }
    
    /**
     * Checks if the handler is waiting for input
     */
    public boolean isWaitingForInput() {
        return waitingForInput;
    }
    
    /**
     * Gets the last choice made by the player
     */
    public int getLastChoice() {
        return lastChoice;
    }
    
    /**
     * Waits for the player to advance. If there's an interaction, handles it.
     * Otherwise, just continues forward.
     * @param interaction The interaction to present, or null to auto-advance
     * @return 0 if player moved forward, -1 if interaction handled but no movement
     */
    public int waitForStepAdvance(Interaction interaction) {
        synchronized(inputLock) {
            waitingForInput = true;
            
            // If there's an interaction, handle it
            if (interaction != null) {
                handleInteraction(interaction);
                waitingForInput = false;
                inputLock.notifyAll();
                return -1; // Interaction handled, no auto-movement
            } else {
                // No interaction, player auto-advances
                System.out.println("\nThe path ahead is clear...");
                System.out.println("[Press ENTER to continue]");
                scanner.nextLine();
                waitingForInput = false;
                inputLock.notifyAll();
                return 0; // Auto-advance
            }
        }
    }
    
    /**
     * Handles an interaction and any follow-up interactions
     */
    public void handleInteraction(Interaction interaction) {
        currentInteraction = interaction;
        
        while (currentInteraction != null && !currentInteraction.isComplete()) {
            // Display the interaction
            currentInteraction.display();
            
            // Get player choice
            int choice = getValidChoice(0, currentInteraction.getOptionCount() - 1);
            
            // Execute the chosen option
            Interaction.Option selectedOption = currentInteraction.getOption(choice);
            if (selectedOption != null) {
                Interaction nextInteraction = selectedOption.execute();
                
                // If option leads to another interaction, continue the loop
                if (nextInteraction != null) {
                    currentInteraction = nextInteraction;
                } else {
                    // Interaction complete
                    currentInteraction.setComplete(true);
                    break;
                }
            }
        }
        
        currentInteraction = null;
    }
    
    /**
     * Gets a valid integer choice from the player within the specified range
     */
    private int getValidChoice(int min, int max) {
        while (true) {
            try {
                System.out.print("\nEnter your choice (" + min + "-" + max + "): ");
                String input = scanner.nextLine().trim();
                int choice = Integer.parseInt(input);
                
                if (choice >= min && choice <= max) {
                    lastChoice = choice;
                    return choice;
                } else {
                    System.out.println("Invalid choice. Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
    
    /**
     * Handles a battle interaction (similar to regular interactions but for combat)
     * @param battleInteraction The AttackInteraction to handle
     */
    public void handleBattleInteraction(Interaction battleInteraction) {
        synchronized(inputLock) {
            waitingForInput = true;
            handleInteraction(battleInteraction);
            waitingForInput = false;
            inputLock.notifyAll();
        }
    }
    
    /**
     * Generic method to wait for Enter key press with a custom message
     * @param message The message to display to the player
     */
    public void waitForEnter(String message) {
        synchronized(inputLock) {
            waitingForInput = true;
            System.out.println("\n" + message);
            scanner.nextLine();
            waitingForInput = false;
            inputLock.notifyAll();
        }
    }
}
