package GameHandlers;

import GameHandlers.EventHandling.EnemyEncounter;
import Game_Characters.Enemy;
import Game_Characters.Player;
import Game_Characters.PlayerInput.AttackInteraction;

//Battle manager to handle battles between player and enemies

//Methods List:
// - startBattle(Player player, EnemyEncounter enemyEncounter):  
// - startBattle(Player player, Enemy enemy):
// - startBattle(Player player, List<Enemy> enemies):
// - conductBattle(Player player, List<Enemy> enemies):  

/*
* How it works:
* The BattleManager class manages battles between the player and enemies. It provides methods to start battles
* with single enemies, multiple enemies, or enemy encounters. The battle logic is conducted using the
* AttackInteraction system, allowing the player to choose actions during combat.
*/

public class BattleManager {
    private final GameManager gameManager;
    
    /**
     * Creates a new BattleManager
     * @param gameManager The main game manager for accessing game state and input handler
     */
    public BattleManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    /**
     * Starts a battle with an enemy from an encounter
     * @param player The player character
     * @param enemyEncounter The enemy encounter containing the enemy/enemies to fight
     */
    public void startBattle(Player player, EnemyEncounter enemyEncounter) {
        java.util.List<Enemy> enemies = enemyEncounter.getAllEnemies();
        startBattle(player, enemies);
    }
    
    /**
     * Starts a battle with a specific enemy
     * @param player The player character
     * @param enemy The enemy to fight
     */
    public void startBattle(Player player, Enemy enemy) {
        java.util.List<Enemy> enemies = new java.util.ArrayList<>();
        enemies.add(enemy);
        startBattle(player, enemies);
    }
    
    /**
     * Starts a battle with multiple enemies
     * @param player The player character
     * @param enemies The list of enemies to fight
     */
    public void startBattle(Player player, java.util.List<Enemy> enemies) {
        // Pause the game for battle
        gameManager.setBattleInProgress(true);
        
        System.out.println("\n>>> BATTLE BEGINS!");
        conductBattle(player, enemies);
        System.out.println(">>> BATTLE ENDS!\n");
        
        // Resume the game after battle
        gameManager.setBattleInProgress(false);
        
        // Notify the game loop to continue
        synchronized(gameManager.getStepLock()) {
            gameManager.getStepLock().notifyAll();
        }
    }
    
    /**
     * Conducts the battle using the AttackInteraction system
     * @param player The player character
     * @param enemies The list of enemies being fought
     */
    private void conductBattle(Player player, java.util.List<Enemy> enemies) {
        // Display initial battle status
        System.out.print(">>> " + player.getName() + " (HP: " + player.getHealth() + ") vs ");
        if (enemies.size() == 1) {
            System.out.println(enemies.get(0).getName() + " (HP: " + enemies.get(0).getHealth() + ")");
        } else {
            System.out.println(enemies.size() + " enemies:");
            for (int i = 0; i < enemies.size(); i++) {
                Enemy e = enemies.get(i);
                System.out.println("  [" + (i + 1) + "] " + e.getName() + " (HP: " + e.getHealth() + ")");
            }
        }
        
        // Create battle interaction with all enemies
        AttackInteraction battleInteraction = new AttackInteraction(player, enemies);
        
        // Let the input handler manage the battle interaction
        gameManager.getInputHandler().handleBattleInteraction(battleInteraction);
    }
}
