package GameHandlers.EventHandling;

import Game_Characters.Enemies.Slime;
import Game_Characters.Enemies.TickiBird;
import Game_Characters.Enemy;
import Game_Characters.Player;
import java.util.HashMap;
import java.util.Map;

//Class used to streamline the creation of enemies and enemy encounters

//Methods List:
// - initializePrefabs():  initializes reusable enemy prefabs
// - getEnemy(String enemyType):  gets an enemy instance by type name
// - createNewEnemy(String enemyType):   creates a new enemy instance by type name
// - createEncounter(String enemyType):  creates a new EnemyEncounter for the specified enemy type
// - createEncounter(String... enemyTypes):   creates a new EnemyEncounter with multiple enemies
// - createNewEnemyWithId(String enemyType, int id):   creates a new enemy instance with a specific ID number

/*
* How it works:
* The EnemyFactory class maintains a collection of enemy prefabs that can be reused for single encounters
* to save memory. For multiple enemy encounters, it creates new instances of enemies to ensure each has
* independent state. The factory provides methods to create EnemyEncounter objects based on enemy type names.
*/

public class EnemyFactory {
    private Player player;
    private Map<String, Enemy> enemyPrefabs;
    private int nextEnemyId = 1;
    
    public EnemyFactory(Player player) {
        this.player = player;
        this.enemyPrefabs = new HashMap<>();
        initializePrefabs();
    }
    
    /**
     * Initialize all enemy prefabs that can be reused
     */
    private void initializePrefabs() {
        
        enemyPrefabs.put("slime", new Slime(player, nextEnemyId++));
        enemyPrefabs.put("tickibird", new TickiBird(player));
        
    }
    /**
     * Gets an enemy instance by type name.
     * Returns the reusable prefab for single encounters.
     * 
     * @param enemyType The type of enemy (e.g., "slime", "goblin")
     * @return The enemy instance, or null if type not found
     */
    public Enemy getEnemy(String enemyType) {
        Enemy enemy = enemyPrefabs.get(enemyType.toLowerCase());
        if (enemy == null) {
            System.err.println("Warning: Unknown enemy type '" + enemyType + "'");
        }
        return enemy;
    }
    
    /**
     * Creates a new EnemyEncounter for the specified enemy type
     * 
     * @param enemyType The type of enemy to encounter
     * @return EnemyEncounter instance, or null if enemy type not found
     */
    public EnemyEncounter createEncounter(String enemyType) {
        Enemy enemy = getEnemy(enemyType);
        if (enemy != null) {
            return new EnemyEncounter(enemy);
        }
        return null;
    }

    /**
     * Creates a new EnemyEncounter with multiple enemies
     * Each enemy gets its own instance with independent state.
     * 
     * @param enemyTypes Variable number of enemy type strings (e.g., "slime", "slime", "goblin")
     * @return EnemyEncounter instance with multiple enemies, or null if any enemy type not found
     */
    public EnemyEncounter createEncounter(String... enemyTypes) {
        if (enemyTypes == null || enemyTypes.length == 0) {
            System.err.println("Warning: No enemy types provided");
            return null;
        }
        
        // Create NEW instances for each enemy, using position-based numbering
        Enemy[] enemies = new Enemy[enemyTypes.length];
        for (int i = 0; i < enemyTypes.length; i++) {
            Enemy enemy = createNewEnemyWithId(enemyTypes[i], i + 1);
            if (enemy == null) {
                System.err.println("Warning: Could not create encounter - enemy type '" + enemyTypes[i] + "' not found");
                return null;
            }
            enemies[i] = enemy;
        }
        
        // Create encounter with all enemies
        return new EnemyEncounter(enemies);
    }
    
    /**
     * Creates a new enemy instance with a specific ID number.
     * 
     * @param enemyType The type of enemy (e.g., "slime", "goblin")
     * @param id The ID number to assign to this enemy
     * @return A new enemy instance, or null if type not found
     */
    private Enemy createNewEnemyWithId(String enemyType, int id) {
        String type = enemyType.toLowerCase();
        switch (type) {
            case "slime":
                return new Slime(player, id);
            case "tickibird":
                return new TickiBird(player);
            default:
                System.err.println("Warning: Unknown enemy type '" + enemyType + "'");
                return null;
        }
    }
}
