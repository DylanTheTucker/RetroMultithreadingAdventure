package GameHandlers;

import Game_Characters.CharacterEnemy;
import Game_Characters.Player;
import Game_Characters.PlayerInput.Interaction;
import Game_Characters.PlayerInput.PlayerInputHandler;
import Game_Characters.Thief;
import Game_Characters.Wizard;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//Game Manager to handle overall game state and flow

//Methods List:
// - initializeCharacters():  create any steppable characters here (p[layer, characterenemies, etc.)
// - start():  
// - startGame():
// - startCharacterThreads():  submits all character threads to executor service
// - gameLoop(): main game loop handling step advancement and interactions
// - globalStepForward():  increments global step and notifies characters
// - checkCharacterEnemyEncounters():  checks if player encounters any CharacterEnemies
// - triggerFinalWizardBattle():  triggers the final battle with the Wizard
// - endGame():  cleans up threads and resources at game end
 
//Getters:
// - getGlobalStepCounter():  gets the global step counter
// - getStepLock():  gets the step lock for synchronization
// - getPlayer():  gets the player character
// - getBattleManager():  
// - isBattleInProgress():  checks if a battle is currently in progress
// - isGameRunning():  checks if the game is currently running
// - shouldPlayerMove():  checks if the player should move this step
// - getNotificationCounter():  gets the notification counter for step synchronization
// - getInputHandler():  gets the player input handler
// - getActiveCharacterEnemies():  gets the list of active CharacterEnemies

//Setters:
// - setBattleInProgress(boolean inProgress):  sets whether a battle is in progress
// - setPlayerHasMoved(boolean moved):  sets whether the player has moved this step
// - setPlayerJustFled(boolean fled):  sets whether the player just fled from battle

public class GameManager {
    //Multithreading
    private ExecutorService executorService;
    private AtomicInteger globalStepCounter;
    
    // Thread references for proper join() synchronization
    private Thread playerThread;
    private Thread stepManagerThread;
    private List<Thread> characterEnemyThreads;

    private static final int MAX_GAMESTEPS = 20;
    private static final int THREAD_POOL_SIZE = 5;

    //Characters
    private Player player;
    private StepManager stepManager;

    private List<CharacterEnemy> activeCharacterEnemies;
    

    //Other
    private Scanner scanner;
    private PlayerInputHandler inputHandler;
    private boolean gameRunning;
    private volatile boolean battleInProgress;
    private volatile boolean shouldPlayerMove;
    private volatile boolean playerHasMoved;
    private volatile boolean playerJustFled;
    private AtomicInteger notificationCounter;
    private final Object stepLock = new Object();
    private BattleManager battleManager;

    public GameManager() {
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.globalStepCounter = new AtomicInteger(0);
        this.notificationCounter = new AtomicInteger(0);
        this.activeCharacterEnemies = new ArrayList<>();
        this.characterEnemyThreads = new ArrayList<>();
        this.scanner = new Scanner(System.in);
        this.inputHandler = new PlayerInputHandler(scanner);
        this.gameRunning = false;
        this.battleInProgress = false;
        this.shouldPlayerMove = false;
        this.playerHasMoved = false;
        this.playerJustFled = false;
        
        initializeCharacters();
        
        // Initialize these AFTER player is created
        this.battleManager = new BattleManager(this);
        this.stepManager = new StepManager(this, player, battleManager);
    }

    private void initializeCharacters() {
        // Initialize player and enemies here
        this.player = new Player();
        this.player.setGameManager(this);

        // Initialize CharacterEnemies (enemies that move and have custom behavior)
        Thief thief = new Thief(player);
        thief.setGameManager(this);
        activeCharacterEnemies.add(thief);
        
        Wizard wizard = new Wizard(player);
        wizard.setGameManager(this);
        activeCharacterEnemies.add(wizard);
    }

    public AtomicInteger getGlobalStepCounter() {
        return globalStepCounter;
    }

    public Object getStepLock() {
        return stepLock;
    }

    public Player getPlayer() {
        return player;
    }
    
    public BattleManager getBattleManager() {
        return battleManager;
    }

    public void setBattleInProgress(boolean inProgress) {
        this.battleInProgress = inProgress;
    }

    public boolean isBattleInProgress() {
        return battleInProgress;
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
    
    public boolean shouldPlayerMove() {
        return shouldPlayerMove;
    }
    
    public AtomicInteger getNotificationCounter() {
        return notificationCounter;
    }
    
    public PlayerInputHandler getInputHandler() {
        return inputHandler;
    }

    public void start() {
        startGame();
    }

    private void startGame() {
        gameRunning = true;
        System.out.println("\n=== The Knight's Journey Begins ===");
        System.out.println("The realm is filled with danger and adventure!");
        System.out.println("Navigate through " + MAX_GAMESTEPS + " steps to complete your quest.");
        System.out.println();
        
        // Start all character threads
        startCharacterThreads();
        
        // Begin the main game loop
        gameLoop();
        
        // Clean up when game ends
        endGame();
    }

    private void startCharacterThreads() {
        System.out.println("Starting character threads...");

        // Create and start player thread
        playerThread = new Thread(player, "Player-Thread");
        playerThread.start();
        
        // Create and start step manager thread
        stepManagerThread = new Thread(stepManager, "StepManager-Thread");
        stepManagerThread.start();
        
        // Create and start all CharacterEnemy threads
        for (CharacterEnemy enemy : activeCharacterEnemies) {
            Thread enemyThread = new Thread(enemy, enemy.getName() + "-Thread");
            characterEnemyThreads.add(enemyThread);
            enemyThread.start();
        }

        System.out.println("All character threads started.");
    }


    //Use this function to add new game functionalities.
    //Add new logic as a function then build it separately. 
    //This should give a clear overview to the game flow.
    private void gameLoop() {
        while (gameRunning) {
            try {
                
                // Wait while battle is in progress
                while (battleInProgress) {
                    Thread.sleep(100);
                }
                
                // Check if there's an interaction at this step
                Interaction interaction = stepManager.getInteractionForStep(player.getStep() + 1);
                
                // Wait for player input (with or without interaction)
                int choice = inputHandler.waitForStepAdvance(interaction);
                
                // If there was an interaction, it's been handled - now advance
                // If no interaction and choice is 0, advance
                if (interaction != null) {
                    // Interaction was handled, now advance the step
                    shouldPlayerMove = true;
                    playerHasMoved = false;
                    globalStepForward();

                } else if (choice == 0) {

                    shouldPlayerMove = true;
                    playerHasMoved = false;
                    globalStepForward();

                } else {

                    shouldPlayerMove = false;
                   
                    synchronized(stepLock) {
                        stepLock.notifyAll();
                    }
                }
                
                // Brief pause to allow threads to process the step
                Thread.sleep(500);

                // Check if player died
                if (!player.getAlive()) {
                    System.out.println("\n>>> GAME OVER <<<");
                    System.out.println("The Knight has fallen in battle...");
                    gameRunning = false;
                    break;
                }

                // Check if player has reached the final step
                if (player.getStep() >= MAX_GAMESTEPS) {
                    // Trigger final Wizard battle instead of ending game
                    triggerFinalWizardBattle();
                }

            } catch (InterruptedException e) {
                System.out.println("Game loop interrupted!");
                gameRunning = false;
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Game loop has exited, now end the game
        endGame();
    }

    private void globalStepForward() {
        synchronized(stepLock) {
            int currentStep = globalStepCounter.incrementAndGet();
            System.out.println("\n--- Game Step " + currentStep + " (Player at step " + player.getStep() + ") ---");

            // First notification - only for player to move
            notificationCounter.incrementAndGet();
            stepLock.notifyAll();
        }
        
        // Wait for player to actually move
        int maxWait = 50; // 50 * 10ms = 500ms timeout
        int waited = 0;
        while (!playerHasMoved && waited < maxWait) {
            try {
                Thread.sleep(10);
                waited++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Now check if player moved to any CharacterEnemy's location
        synchronized(stepLock) {
            checkCharacterEnemyEncounters();
            
            // If battle started, don't send notification for enemies to move
            if (battleInProgress) {
                return;
            }
            
            // Second notification - for enemies to move
            notificationCounter.incrementAndGet();
            stepLock.notifyAll();
        }
        
        // Wait briefly for enemies to finish moving
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Check again if any enemy moved onto player's position
        synchronized(stepLock) {
            checkCharacterEnemyEncounters();
        }
    }
    
    /**
     * Checks if the player is at the same step as any CharacterEnemy and triggers battle if so.
     * This prevents CharacterEnemies from moving away when the player moves to their location.
     */
    private void checkCharacterEnemyEncounters() {
        // Skip encounter if player just fled (give them one turn to move)
        if (playerJustFled) {
            System.out.println(">>> You catch your breath after fleeing...");
            playerJustFled = false; // Reset the flag after skipping one turn
            return;
        }
        
        for (CharacterEnemy enemy : activeCharacterEnemies) {
            if (enemy != null && enemy.getAlive() && 
                player.getStep() == enemy.getCurrentStep()) {
                System.out.println(">>> You encounter the " + enemy.getName() + " at step " + player.getStep() + "!");
                
                // Set battle in progress to prevent enemy from moving during battle
                battleInProgress = true;
                
                // Trigger battle
                System.out.println(">>> BATTLE BEGINS!");
                battleManager.startBattle(player, enemy);
                
                battleInProgress = false;
                
                // Check if enemy was defeated and remove from active list
                if (!enemy.getAlive()) {
                    System.out.println(">>> " + enemy.getName() + " has been permanently defeated!");
                    activeCharacterEnemies.remove(enemy);
                    
                    // Check if defeated enemy was the Wizard (final boss)
                    if (enemy instanceof Wizard) {
                        System.out.println("\n" + "=".repeat(60));
                        System.out.println(">>> VICTORY! The mighty Wizard has been vanquished!");
                        System.out.println(">>> The Knight has proven their valor and skill!");
                        System.out.println(">>> The realm is saved from the Wizard's tyranny!");
                        System.out.println("=".repeat(60) + "\n");
                        gameRunning = false; // End the game
                    }
                    
                    // Brief pause after victory
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Exit loop since we modified the list
                    break;
                }
                
                // Check if player fled from this battle
                if (playerJustFled) {
                    System.out.println("\n>>> You escape and move forward!");
                    
                    // Freeze enemy
                    enemy.freezeAfterFlee();
                    
                    // Set up for automatic movement
                    shouldPlayerMove = true;
                    playerHasMoved = false;
                    
                    // Move forward by incrementing step counter and notifying threads
                    synchronized(stepLock) {
                        int currentStep = globalStepCounter.incrementAndGet();
                        System.out.println("\n--- Flee Step " + currentStep + " ---");
                        
                        // Notify player to move
                        notificationCounter.incrementAndGet();
                        stepLock.notifyAll();
                    }
                    
                    // Wait for player to move
                    int maxWait = 50;
                    int waited = 0;
                    while (!playerHasMoved && waited < maxWait) {
                        try {
                            Thread.sleep(10);
                            waited++;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    
                    // Notify enemies (second notification)
                    synchronized(stepLock) {
                        notificationCounter.incrementAndGet();
                        stepLock.notifyAll();
                    }
                    
                    // Brief pause after flee movement
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    
                    // Return to skip normal battle pause
                    return;
                }
                
                // Brief pause after battle to let player process results
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // Only one battle per step
                break;
            }
        }
    }

    /**
     * Triggers the final Wizard battle when player reaches MAX_GAMESTEPS.
     * The Wizard teleports to the player's location to initiate combat.
     */
    private void triggerFinalWizardBattle() {
        // Find the Wizard in active enemies
        Wizard wizard = null;
        for (CharacterEnemy enemy : activeCharacterEnemies) {
            if (enemy instanceof Wizard && enemy.getAlive()) {
                wizard = (Wizard) enemy;
                break;
            }
        }
        
        if (wizard != null) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println(">>> You have reached the end of your journey!");
            System.out.println(">>> Suddenly, reality warps around you...");
            System.out.println(">>> The Wizard appears in a blinding flash of light!");
            System.out.println(">>> \"You dare challenge me, Knight? Face your destiny!\"");
            System.out.println("=".repeat(60) + "\n");
            
            // Teleport wizard to player's location
            wizard.setCurrentStep(player.getStep());
            
            // Brief dramatic pause
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Trigger the battle
            battleInProgress = true;
            battleManager.startBattle(player, wizard);
            battleInProgress = false;
            
            // Check if wizard was defeated
            if (!wizard.getAlive()) {
                System.out.println(">>> The Wizard has been permanently defeated!");
                activeCharacterEnemies.remove(wizard);
                
                System.out.println("\n" + "=".repeat(60));
                System.out.println(">>> VICTORY! The mighty Wizard has been vanquished!");
                System.out.println(">>> The Knight has proven their valor and skill!");
                System.out.println(">>> The realm is saved from the Wizard's tyranny!");
                System.out.println("=".repeat(60) + "\n");
                gameRunning = false; // End the game
            } else if (!player.getAlive()) {
                // Player was defeated by the Wizard
                System.out.println("\n>>> GAME OVER <<<");
                System.out.println("The Knight has fallen to the Wizard's power...");
                gameRunning = false;
            } else {
                // Player fled from the Wizard
                System.out.println(">>> You have fled from the final battle!");
                System.out.println(">>> The Wizard remains at step " + wizard.getCurrentStep() + ", waiting for your return...");
                playerJustFled = true;
                wizard.freezeAfterFlee();
            }
        } else {
            // Wizard was already defeated earlier in the game
            System.out.println("\n" + "=".repeat(60));
            System.out.println(">>> You have reached the end of your journey!");
            System.out.println(">>> With the Wizard already defeated, peace reigns!");
            System.out.println(">>> The Knight returns home victorious!");
            System.out.println("=".repeat(60) + "\n");
            gameRunning = false;
        }
    }

    private void endGame() {
        System.out.println("\nGame Over. Thank you for playing!");
        
        System.out.println(player.getBattleStatistics());
        
        // Display most defeated enemy type using stream aggregation
        System.out.println(">>> Most Defeated Enemy: " + player.getMostDefeatedEnemyType());
        
        // Use stream to filter and display CharacterEnemy status
        System.out.println("\n>>> Character Enemy Status:");
        activeCharacterEnemies.stream()
            .forEach(enemy -> System.out.println("  - " + enemy.getName() + ": " + 
                (enemy.getAlive() ? "Still Alive" : "Defeated")));
        
        // Stop the step manager
        stepManager.stop();
        
        // Notify all waiting threads so they can exit
        synchronized(stepLock) {
            stepLock.notifyAll();
        }
        
        // Use join() to ensure proper synchronization and closure
        System.out.println("Waiting for all threads to complete...");
        
        try {
            // Join player thread
            if (playerThread != null && playerThread.isAlive()) {
                playerThread.join(2000); // Wait up to 2 seconds
                System.out.println("Player thread completed.");
            }
            
            // Join step manager thread
            if (stepManagerThread != null && stepManagerThread.isAlive()) {
                stepManagerThread.join(2000); // Wait up to 2 seconds
                System.out.println("StepManager thread completed.");
            }
            
            // Join all character enemy threads
            for (Thread enemyThread : characterEnemyThreads) {
                if (enemyThread != null && enemyThread.isAlive()) {
                    enemyThread.join(2000); // Wait up to 2 seconds
                    System.out.println(enemyThread.getName() + " completed.");
                }
            }
            
            System.out.println("All threads have been synchronized and closed properly.");
            
        } catch (InterruptedException e) {
            System.out.println("Thread synchronization interrupted!");
            Thread.currentThread().interrupt();
        }
        
        // Shutdown executor service gracefully
        executorService.shutdown();
        
        try {
            if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        
        scanner.close();
    }
    
    public List<CharacterEnemy> getActiveCharacterEnemies() {
        return activeCharacterEnemies;
    }
    
    public void setPlayerHasMoved(boolean moved) {
        this.playerHasMoved = moved;
    }
    
    public void setPlayerJustFled(boolean fled) {
        this.playerJustFled = fled;
    }
}