package Game_Characters.PlayerInput;

import Game_Characters.Enemy;
import Game_Characters.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//Methods List:
// - addExpGained(int exp):
// - triggerEnemiesTurn():
// - checkStatus():
// - allEnemiesTurn():
// - checkBattleStatus():

//Abstract Methods (Implemented):
// - initializeOptions():

//Inner Classes:
// - AttackOption:
//   - execute():
// - DefendOption:
//   - execute():
// - HealOption:
//   - execute():
// - CheckEnemyOption:
//   - execute():
// - FleeOption:
//   - execute():

public class AttackInteraction extends Interaction {
    
    private Player player;
    private List<Enemy> enemies;
    private boolean playerDefending;
    private int expGained = 0;
    private int goldGained = 0; // Track gold from defeated enemies
    private int failedFleeAttempts = 0;
    
    // Loot table (Array of possible loot items) - Requirement 4
    private static final String[] LOOT_ITEMS = {
        "Health Potion", "Rusty Sword", "Ancient Coin", "Magic Scroll",
        "Leather Armor", "Gold Ring", "Silver Dagger", "Emerald Gem"
    };
    private static final Random random = new Random();
    
    /**
     * Adds experience gained from defeating an enemy.
     * Called by AttackChosen interaction when enemies are defeated.
     */
    public void addExpGained(int exp) {
        this.expGained += exp;
    }
    
    /**
     * Adds gold gained from defeating an enemy (demonstrates concurrent resource access).
     * Called by AttackChosen interaction when enemies are defeated.
     */
    public void addGoldGained(int gold) {
        this.goldGained += gold;
    }
    
    /**
     * Triggers all enemies to take their turn.
     * Called by AttackChosen interaction after player attacks.
     */
    public void triggerEnemiesTurn() {
        allEnemiesTurn();
    }
    
    /**
     * Checks battle status and returns appropriate interaction.
     * Called by AttackChosen interaction after attacks resolve.
     */
    public Interaction checkStatus() {
        return checkBattleStatus();
    }
    
    /**
     * Creates a new battle interaction with a single enemy
     * @param player The player character
     * @param enemy The enemy being fought
     */
    public AttackInteraction(Player player, Enemy enemy) {
        super("Battle", "Choose your action:");
        this.player = player;
        this.enemies = new ArrayList<>();
        this.enemies.add(enemy);
        this.playerDefending = false;
    }
    
    /**
     * Creates a new battle interaction with multiple enemies
     * @param player The player character
     * @param enemies The list of enemies being fought
     */
    public AttackInteraction(Player player, List<Enemy> enemies) {
        super("Battle", "Choose your action:");
        this.player = player;
        this.enemies = new ArrayList<>(enemies);
        this.playerDefending = false;
    }

    @Override
    protected void initializeOptions() {
        addOption(new AttackOption());
        addOption(new DefendOption());
        addOption(new HealOption());
        addOption(new CheckEnemyOption());
        addOption(new FleeOption());
    }

    /**
     * Option to attack the enemy
     */
    public class AttackOption extends Option {
        public AttackOption() {
            super("Attack");
        }

        @Override
        public Interaction execute() {
            // Return the AttackChosen interaction - let the interaction system handle it
            return new AttackChosen(player, enemies, AttackInteraction.this);
        }
    }
    
    /**
     * Option to defend, reducing incoming damage
     */
    public class DefendOption extends Option {
        public DefendOption() {
            super("Defend");
        }

        @Override
        public Interaction execute() {
            System.out.println("\n>>> " + player.getName() + " takes a defensive stance!");
            playerDefending = true;
            
            // All enemies attack but player defends
            allEnemiesTurn();
            
            // Check battle status
            return checkBattleStatus();
        }
    }
    
    /**
     * Option to heal, restoring health
     */
    public class HealOption extends Option {
        public HealOption() {
            super("Heal");
        }

        @Override
        public Interaction execute() {
            System.out.println("\n>>> " + player.getName() + " focuses on healing!");
            player.heal(0.3f); // Heal 30% of max health
            playerDefending = false;
            
            // All enemies attack while player heals
            allEnemiesTurn();
            
            // Check battle status
            return checkBattleStatus();
        }
    }
    
    /**
     * Option to check enemy stats
     */
    public class CheckEnemyOption extends Option {
        public CheckEnemyOption() {
            super("Check Enemy");
        }

        @Override
        public Interaction execute() {
            // Always check the first enemy (current target)
            if (enemies.isEmpty()) {
                System.out.println("\nNo enemies to check!");
                return AttackInteraction.this;
            }
            
            Enemy targetEnemy = enemies.get(0);
            
            System.out.println(targetEnemy.getCheckMessage());
            System.out.println("\n=== Enemy Stats ===");
            System.out.println("Name: " + targetEnemy.getName());
            System.out.println("Health: " + targetEnemy.getHealth() + "/" + targetEnemy.getMaxHealth());
            System.out.println("Attack Power: " + targetEnemy.getAttack());
            System.out.println("Enemy Stealth: " + targetEnemy.getStealth());
            
            if (enemies.size() > 1) {
                System.out.println("\n" + (enemies.size() - 1) + " other enemy(ies) waiting...");
            }
            System.out.println("==================\n");
            
            // Checking takes a turn - enemies attack
            System.out.println(">>> While you examine the enemy, they strike!");
            allEnemiesTurn();
            
            // Check battle status
            return checkBattleStatus();
        }
    }

    /**
     * Option to flee from battle
     */
    public class FleeOption extends Option {
        public FleeOption() {
            super("Flee");
        }

        @Override
        public Interaction execute() {
            int baseStealth = player.getStealth();
            // Effective stealth increases with each failed flee attempt
            // Formula: baseStealth * (1 + failedFleeAttempts)
            int effectiveStealth = baseStealth * (1 + failedFleeAttempts);
            
            System.out.println("\n>>> " + player.getName() + " attempts to flee...");
            if (failedFleeAttempts > 0) {
                System.out.println(">>> Desperation increases flee chance! (Effective Stealth: " + effectiveStealth + ")");
            }
            
            // Need to beat all enemies' stealth to flee
            boolean canFlee = true;
            for (Enemy e : enemies) {
                if (effectiveStealth <= e.getStealth()) {
                    canFlee = false;
                    break;
                } 
            }

            if (canFlee) {
                System.out.println(">>> SUCCESS! " + player.getName() + " flees from battle!");
                player.notifyFled();
                player.recordBattleFled(); // Track battle fled (functional enhancement)
                setComplete(true);
                return null;
            } else {
                failedFleeAttempts++;
                System.out.println(">>> FAILED! " + player.getName() + " couldn't escape! (Attempt #" + failedFleeAttempts + ")");
            }
            
            // Failed flee attempt - all enemies get free attack
            allEnemiesTurn();
            
            // Check battle status
            return checkBattleStatus();
        }
    }
    
    //UNDERHOOD METHODS

    /**
     * Executes all living enemies' turns in combat
     */
    private void allEnemiesTurn() {
        for (Enemy enemy : enemies) {
            if (enemy.getAlive()) {
                System.out.println(">>> " + enemy.getName() + " attacks!");
                int enemyDamage = enemy.dealDamage();
                
                // Apply defense reduction if player is defending
                if (playerDefending) {
                    enemyDamage = enemyDamage / 2;
                }
                
                player.takeDamage(enemyDamage);
            }
        }
        
        // Show defense message once if defending
        if (playerDefending) {
            System.out.println(">>> " + player.getName() + "'s defense reduces all damage!");
            playerDefending = false;
        }
    }
    
    /**
     * Generates random loot drops using array and Random (Requirement 4)
     * Uses lambda expression to filter and display loot
     */
    private void dropRandomLoot() {
        // 50% chance to drop loot per defeated enemy
        int lootCount = random.nextInt(enemies.size() + 1);
        
        if (lootCount > 0) {
            System.out.println("\n>>> Loot dropped:");
            // Use stream and lambda to generate random loot from array
            random.ints(lootCount, 0, LOOT_ITEMS.length)
                  .mapToObj(i -> LOOT_ITEMS[i])
                  .forEach(item -> {
                      // Random gold value between 5-50
                      int goldValue = 5 + random.nextInt(46);
                      player.collectLoot(item, goldValue);
                  });
        }
    }
    
    /**
     * Checks if the battle should continue or end
     * @return null if battle is over, this interaction if battle continues
     */
    private Interaction checkBattleStatus() {
        if (!player.getAlive()) {
            System.out.println(">>> Defeat! " + player.getName() + " has fallen!");
            player.recordBattleLost(); // Track battle loss (functional enhancement)
            setComplete(true);
            return null;
        }
        
        if (enemies.isEmpty()) {
            System.out.println(">>> Victory! All enemies have been defeated!");
            
            // Award experience (synchronized method)
            if (expGained > 0) {
                System.out.println(">>> " + player.getName() + " gains " + expGained + " EXP!");
                player.gainExperience(expGained);
            }
            
            // Award gold as loot (synchronized with explicit lock)
            if (goldGained > 0) {
                player.addGold(goldGained);
            }
            
            // Drop random loot items using array and lambdas (Requirement 4)
            dropRandomLoot();
            
            // Record battle victory (functional enhancement with ArrayList tracking)
            player.recordBattleWon();
            
            // Brief pause to let player see level up before next battle
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            setComplete(true);
            return null;
        }
        
        // Battle continues - return this interaction for next turn
        return this;
    }
}
