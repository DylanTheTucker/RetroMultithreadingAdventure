package Game_Characters;

import GameHandlers.GameManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

//Methods List:
// - setGameManager(GameManager gameManager):
// - notifyFled():  notifeies the game manager that the player fled
// - run():
// - stepForward():
// - determineSteps():
// - heal(float percentage):  heals the player for a percentage of their max health
// - gainExperience(int exp):  [SYNCHRONIZED] adds experience points and checks for level up
// - checkLevelUp():  checks if the player has enough experience to level up
// - addGold(int amount):  [SYNCHRONIZED with LOCK] adds gold to player's treasure
// - spendGold(int amount):  [SYNCHRONIZED with LOCK] spends gold if player has enough
// - collectLoot(String lootName, int goldValue):  thread-safe method to collect loot from battles

//Abstract Methods (Implemented):
// - takeDamage(int damage):
// - dealDamage():
// - updateCharacterData():
// - getCurrentStep():
// - setCurrentStep(int step):

//Getters:
// - getLevel():
// - getExperience():
// - getExperienceToNextLevel():
// - getGold():  [SYNCHRONIZED with LOCK] returns current gold amount

public class Player extends GameCharacter implements CanStep {

    //Variables
    private int level;
    private volatile int experience; // Volatile ensures visibility across threads
    private int experienceToNextLevel;
    private volatile int gold; // Shared resource: gold collected from battles and loot
    private final Object resourceLock = new Object(); // Lock for accessing shared resources
    
    // Battle Statistics (ArrayList for tracking items/enemies)
    private List<String> defeatedEnemies; // Track enemies defeated by name
    private List<String> itemsCollected; // Track items/loot collected
    private int battlesWon;
    private int battlesLost;
    private int battlesFled;
    
    public GameManager gameManager;

    //Constructor
    public Player() {
        super("Knight", 15, 3, 5);
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = 3;
        this.gold = 0; // Start with no gold
        
        // Initialize ArrayLists for tracking
        this.defeatedEnemies = new ArrayList<>();
        this.itemsCollected = new ArrayList<>();
        this.battlesWon = 0;
        this.battlesLost = 0;
        this.battlesFled = 0;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    public void notifyFled() {
        if (gameManager != null) {
            gameManager.setPlayerJustFled(true);
        }
    }

    public synchronized void run() {
        System.out.println("The brave Knight sets out on a quest to slay the dragon!");
        Random rand = new Random();
        try {
            while (isAlive && gameManager.isGameRunning()) {
                // Wait for the game manager to advance to the next step
                synchronized(gameManager.getStepLock()) {
                    int currentGameStep = gameManager.getGlobalStepCounter().get();
                    gameManager.getStepLock().wait();
                    
                    // Exit if game step hasn't advanced (game ending)
                    if (gameManager.getGlobalStepCounter().get() <= currentGameStep) {
                        break;
                    }
                }
                
                // Wait if a battle is in progress
                while (gameManager.isBattleInProgress()) {
                    Thread.sleep(100);
                }
                
                // Only step forward if player chose to move
                if (gameManager.shouldPlayerMove()) {
                    stepForward();
                    Thread.sleep(rand.nextInt(500) + 200);
                }
            }
            if (isAlive) {
                System.out.println("The Knight returns victorious!");
            }
        } catch (InterruptedException e) {
            // Thread was interrupted
            if (isAlive && !gameManager.isBattleInProgress()) {
                System.out.println("The Knight returns victorious!");
            }
        }
    }

    //Override methods

    //Interface methods
    @Override
    public void stepForward() {
        if (isAlive) {
            currentStep++;
            System.out.println(name + " steps forward to step " + currentStep + ".");
            // Signal that player has moved
            gameManager.setPlayerHasMoved(true);
        } else {
            System.out.println(name + " cannot move, they are defeated.");
        }
    }

    @Override
    public int determineSteps() {
        // For simplicity, the player moves 1 step per turn
        return 1;
    }

    @Override
    public int getCurrentStep() {
        return currentStep;
    }

    @Override
    public void setCurrentStep(int step) {
        this.currentStep = step;
    }

    //superclass methods
    @Override
    public void takeDamage(int damage) {
        if (isAlive) {
            health -= damage;
            System.out.println(name + " takes " + damage + " damage! Current health: " + health);
            if (health <= 0) {
                isAlive = false;
                health = 0;
                System.out.println(name + " has been defeated!");
            }
        }
    }
    
    /**
     * Heals the player for a percentage of their max health
     * @param percentage The percentage of max health to heal (0.0 to 1.0)
     */
    public void heal(float percentage) {
        if (isAlive) {
            int healAmount = (int)(maxHealth * percentage);
            int oldHealth = health;
            health = Math.min(health + healAmount, maxHealth);
            int actualHealing = health - oldHealth;
            System.out.println(name + " heals for " + actualHealing + " HP! Current health: " + health + "/" + maxHealth);
        }
    }

    @Override
    public int dealDamage() {
        if (isAlive) {
            System.out.println(name + " attacks for " + attack + " damage!");
        }
        return attack;
    }

    @Override
    public CharacterData updateCharacterData() {
        this.characterData.health = this.health;
        this.characterData.maxHealth = this.maxHealth;
        this.characterData.attack = this.attack;
        this.characterData.stealth = this.stealth;
        return this.characterData;
    }

    /**
     * Adds experience points to the player (thread-safe).
     * Synchronized to prevent race conditions when multiple enemies are defeated simultaneously.
     */
    public synchronized void gainExperience(int exp) {
        if (isAlive) {
            experience += exp;
            System.out.println(name + " gains " + exp + " experience points!");
            System.out.println(">>> Current EXP: " + experience + "/" + experienceToNextLevel);
            checkLevelUp();
        }
    }

    public void checkLevelUp() {
        while (experience >= experienceToNextLevel) {
            System.out.println(">>> Leveling up! (" + experience + " >= " + experienceToNextLevel + ")");
            experience -= experienceToNextLevel; // Subtract threshold, keeping remainder
            
            maxHealth += 5 * level;
            attack += 2 * level;
            stealth += 3 * level;
            level++;
            experienceToNextLevel += 5; // Increase threshold for next level
            health = maxHealth; // Heal to full on level up
            System.out.println("\n>>> LEVEL UP! " + name + " is now level " + level + "!");
            System.out.println(">>> Max Health: " + maxHealth + " | Attack: " + attack + " | Stealth: " + stealth);
            System.out.println(">>> Experience: " + experience + "/" + experienceToNextLevel + "\n");
        }
    }
    
    // ===== SHARED RESOURCE MANAGEMENT (Thread-Safe) =====
    
    /**
     * Adds gold to the player's treasure (thread-safe with explicit lock).
     * Uses explicit locking to prevent race conditions when collecting loot.
     * @param amount Amount of gold to add
     */
    public void addGold(int amount) {
        synchronized(resourceLock) {
            if (amount > 0 && isAlive) {
                gold += amount;
                System.out.println(">>> " + name + " collects " + amount + " gold! Total: " + gold + " gold");
            }
        }
    }
    
    /**
     * Attempts to spend gold (thread-safe with explicit lock).
     * @param amount Amount of gold to spend
     * @return true if transaction successful, false if insufficient funds
     */
    public boolean spendGold(int amount) {
        synchronized(resourceLock) {
            if (amount > 0 && gold >= amount && isAlive) {
                gold -= amount;
                System.out.println(">>> " + name + " spends " + amount + " gold. Remaining: " + gold + " gold");
                return true;
            }
            return false;
        }
    }
    
    /**
     * Gets current gold amount (thread-safe with explicit lock).
     * @return Current gold amount
     */
    public int getGold() {
        synchronized(resourceLock) {
            return gold;
        }
    }
    
    /**
     * Collects loot from defeated enemies (thread-safe).
     * Demonstrates concurrent access to shared resources (gold and experience).
     * @param lootName Name of the loot collected
     * @param goldValue Gold value of the loot
     */
    public void collectLoot(String lootName, int goldValue) {
        synchronized(resourceLock) {
            if (isAlive && goldValue > 0) {
                gold += goldValue;
                itemsCollected.add(lootName); // Track item in ArrayList
                System.out.println(">>> " + name + " found " + lootName + " worth " + goldValue + " gold!");
                System.out.println(">>> Total treasure: " + gold + " gold");
            }
        }
    }
    
    // ===== BATTLE STATISTICS (Lambda Expressions & Stream Operations) =====
    
    /**
     * Records an enemy defeat (thread-safe with ArrayList).
     * @param enemyName Name of defeated enemy
     */
    public synchronized void recordEnemyDefeated(String enemyName) {
        defeatedEnemies.add(enemyName);
    }
    
    /**
     * Records a battle victory (thread-safe).
     */
    public synchronized void recordBattleWon() {
        battlesWon++;
    }
    
    /**
     * Records a battle loss (thread-safe).
     */
    public synchronized void recordBattleLost() {
        battlesLost++;
    }
    
    /**
     * Records fleeing from battle (thread-safe).
     */
    public synchronized void recordBattleFled() {
        battlesFled++;
    }
    
    /**
     * Gets battle statistics summary using lambda expressions and streams.
     */
    public String getBattleStatistics() {
        synchronized(resourceLock) {
            StringBuilder stats = new StringBuilder();
            stats.append("\n" + "=".repeat(60) + "\n");
            stats.append(">>> KNIGHT'S BATTLE STATISTICS <<<\n");
            stats.append("=".repeat(60) + "\n");
            
            // Basic stats
            stats.append(String.format("Battles Won: %d | Lost: %d | Fled: %d\n", 
                battlesWon, battlesLost, battlesFled));
            stats.append(String.format("Level: %d | EXP: %d/%d | Gold: %d\n", 
                level, experience, experienceToNextLevel, gold));
            
            // Count total enemies defeated
            int totalEnemies = defeatedEnemies.size();
            stats.append(String.format("Total Enemies Defeated: %d\n", totalEnemies));
            
            // Use Stream to count enemy types (lambda expression with grouping)
            if (!defeatedEnemies.isEmpty()) {
                stats.append("\nEnemies Defeated By Type:\n");
                defeatedEnemies.stream()
                    .collect(Collectors.groupingBy(
                        name -> name.replaceAll(" #\\d+", ""), // Remove ID numbers
                        Collectors.counting()
                    ))
                    .forEach((enemyType, count) -> 
                        stats.append(String.format("  - %s: %d\n", enemyType, count))
                    );
            }
            
            // Use Stream to list items collected (lambda filtering)
            if (!itemsCollected.isEmpty()) {
                stats.append(String.format("\nItems Collected: %d\n", itemsCollected.size()));
                itemsCollected.stream()
                    .distinct() // Remove duplicates
                    .sorted() // Sort alphabetically
                    .forEach(item -> stats.append("  - " + item + "\n"));
            }
            
            // Use Stream to calculate total gold from items
            long itemGoldTotal = itemsCollected.stream()
                .filter(item -> item.contains("gold")) // Filter gold items
                .count();
            if (itemGoldTotal > 0) {
                stats.append(String.format("\nGold-related items found: %d\n", itemGoldTotal));
            }
            
            stats.append("=".repeat(60) + "\n");
            return stats.toString();
        }
    }
    
    /**
     * Gets a summary of most defeated enemy type using streams.
     * @return Name of most defeated enemy type
     */
    public String getMostDefeatedEnemyType() {
        if (defeatedEnemies.isEmpty()) {
            return "None";
        }
        
        return defeatedEnemies.stream()
            .collect(Collectors.groupingBy(
                name -> name.replaceAll(" #\\d+", ""), // Remove ID numbers
                Collectors.counting()
            ))
            .entrySet().stream()
            .max((e1, e2) -> Long.compare(e1.getValue(), e2.getValue()))
            .map(entry -> entry.getKey() + " (" + entry.getValue() + " defeated)")
            .orElse("None");
    }
    
    /**
     * Gets list of all defeated enemies (for logging/display).
     * @return Defensive copy of defeated enemies list
     */
    public List<String> getDefeatedEnemies() {
        synchronized(resourceLock) {
            return new ArrayList<>(defeatedEnemies); // Return defensive copy
        }
    }
    
    /**
     * Gets list of all collected items (for logging/display).
     * @return Defensive copy of items list
     */
    public List<String> getItemsCollected() {
        synchronized(resourceLock) {
            return new ArrayList<>(itemsCollected); // Return defensive copy
        }
    }
}