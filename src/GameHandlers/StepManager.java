package GameHandlers;

import GameHandlers.EventHandling.EnemyEncounter;
import GameHandlers.EventHandling.EnemyFactory;
import GameHandlers.EventHandling.StepEvent;
import Game_Characters.Enemy;
import Game_Characters.Player;
import Game_Characters.PlayerInput.Interaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//Manages step-based events in the game, such as enemy encounters 
//This class is a thread though functionally different from steppable characters

//Methods List:
// - getInteractionForStep(int step):  gets the interaction for a specific step, if one exists
// - getEventAnnouncement(StepEvent event, int step):  creates an announcement message for an event
// - run():  main loop to monitor steps and trigger events
// - stop():  stops the step manager thread

/*
* How it works:
* The StepManager class monitors the player's steps and triggers events when the player reaches
* specific step counts. It maintains a list of step events, including enemy encounters, and uses
* the BattleManager to handle combat when an encounter is triggered.
*/

public class StepManager implements Runnable {
    //variables
    private Map<Integer, StepEvent> stepEvents;
    private Map<Integer, StepEvent> activeEvents;

    //Thread variables
    private GameManager gameManager;
    private Player player;
    private BattleManager battleManager;
    private EnemyFactory enemyFactory;
    private AtomicInteger globalStepCounter;
    private Object stepLock;
    private boolean running;

    //set up, similar to enemyspawner
    public StepManager(GameManager gameManager, Player player, BattleManager battleManager) {
        this.stepEvents = new HashMap<>();
        this.activeEvents = new HashMap<>();

        this.gameManager = gameManager;
        this.player = player;
        this.battleManager = battleManager;
        this.globalStepCounter = gameManager.getGlobalStepCounter();
        this.stepLock = gameManager.getStepLock();
        this.running = true;
        
        // Initialize enemy factory
        this.enemyFactory = new EnemyFactory(player);

        // Set up step events using string-based enemy types
        // Single enemy encounters
        stepEvents.put(2, enemyFactory.createEncounter("slime"));
        stepEvents.put(4, enemyFactory.createEncounter("slime"));
        stepEvents.put(5, enemyFactory.createEncounter("tickibird"));
        
        // Example: Multiple enemy encounter (uncomment to use)
        stepEvents.put(7, enemyFactory.createEncounter("slime", "slime", "slime")); // 3 slimes
        stepEvents.put(10, enemyFactory.createEncounter("slime", "tickibird")); // 2 tickibirds
        stepEvents.put(12, enemyFactory.createEncounter("slime", "slime", "tickibird")); // 2 slimes + 1 tickibird
        stepEvents.put(15, enemyFactory.createEncounter("tickibird", "tickibird", "tickibird")); // Wizard encounter
        stepEvents.put(18, enemyFactory.createEncounter("slime", "tickibird")); // 2 tickibirds
    }
    
    /**
     * Gets the interaction for a specific step, if one exists in that step's event
     * @param step The step number to check
     * @return The Interaction for that step, or null if no interaction
     */
    public Interaction getInteractionForStep(int step) {
        // Check if there's an active event at this step
        StepEvent event = activeEvents.get(step);
        if (event != null && event.hasInteraction()) {
            return event.getInteraction();
        }
        return null;
    }
    
    /**
     * Creates an announcement message for when an event appears
     * @param event The step event to announce
     * @param step The step number where it appears
     * @return Formatted announcement string
     */
    private String getEventAnnouncement(StepEvent event, int step) {
        if (event instanceof EnemyEncounter) {
            EnemyEncounter encounter = (EnemyEncounter) event;
            List<Enemy> enemies = encounter.getAllEnemies();
            
            if (enemies.size() == 1) {
                return ">>> A " + enemies.get(0).getName() + " appears at step " + step + "!";
            } else {
                return ">>> " + enemies.size() + " enemies appear at step " + step + "!";
            }
        }
        return ">>> Something appears at step " + step + "!";
    }

    @Override
    public void run() {

        try {
            while (running) {
                synchronized(stepLock) {
                    // Wait for the next game step
                    stepLock.wait();
                    
                    int currentStep = globalStepCounter.get();

                    //check game step
                    //This way events are quicker to load once player actually gets to them
                    if (stepEvents.containsKey(currentStep)) {
                        StepEvent event = stepEvents.get(currentStep);
                        
                        // Get the enemy name(s) for proper announcement
                        String announcement = getEventAnnouncement(event, currentStep);
                        System.out.println(announcement);

                        
                        // Move event to active events
                        activeEvents.put(currentStep, event);
                        stepEvents.remove(currentStep);
                    }

                    //check player step
                    int playerStep = player.getStep();
                    if (activeEvents.containsKey(playerStep)) {
                        StepEvent event = activeEvents.get(playerStep);
                        
                        // Perform this event using BattleManager
                        event.performEvent(battleManager);
                        
                        // Reset the enemy after battle for reuse
                        if (event instanceof EnemyEncounter) {
                            Enemy enemy = ((EnemyEncounter) event).spawnEnemy();
                            enemy.reset();
                        }
                        
                        // Remove the slime after battle
                        activeEvents.remove(playerStep);
                    }

                }
            }
        } catch (InterruptedException e) {
            // Thread interrupted - exit gracefully
            running = false;
            Thread.currentThread().interrupt();
        }
    }

    //check for step event
    /* 
    public void checkForStepEvent(int step) {
        if (stepEvents.containsKey(step)) {
            StepEvent event = stepEvents.get(step);
            event.performEvent();
        }
    }
        */

    //perform step event
    
    //stop
    public void stop() {
        running = false;
    }



    
}
