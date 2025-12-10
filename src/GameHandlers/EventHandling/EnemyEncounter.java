package GameHandlers.EventHandling;

//import GameHandlers.StepEvent;
import GameHandlers.*;
import Game_Characters.*;
import java.util.ArrayList;
import java.util.List;

//Step event that spawns enemy/enemies for battle

//Methods List:
// - spawnEnemy():  returns the primary enemy in the encounter
// - getAllEnemies():

//Abstract Methods (Implemented):
// - performEvent(BattleManager battleManager):  creates a battle with the encounter's enemies in the battle manager

public class EnemyEncounter extends StepEvent {
    private Enemy enemy;
    private List<Enemy> enemyList;
    private int enemyCount = 0;

    public EnemyEncounter(Enemy _enemy) {
        this.enemy = _enemy;
        enemyCount++;
    }

    public EnemyEncounter(Enemy... _enemies) {
        this.interaction = null;
        this.enemyList = new ArrayList<>();
        for (Enemy e : _enemies) {
            this.enemyList.add(e);
            enemyCount++;
        }
        // Set the first enemy as the primary enemy for now
        if (_enemies.length > 0) {
            this.enemy = _enemies[0];
        }
    }

    @Override
    public void performEvent(BattleManager battleManager) {
        if (enemyCount > 1) {
            System.out.println("Multiple enemies appear! You face " + enemyCount + " foes!");
        } else {
            System.out.println("An enemy appears!");
        }
        battleManager.startBattle(enemy.getTargetPlayer(), this);
    }

    public Enemy spawnEnemy() {
        return this.enemy;
    }
    
    /**
     * Gets all enemies in this encounter
     * @return List of all enemies, or a single-element list if only one enemy
     */
    public List<Enemy> getAllEnemies() {
        if (enemyList != null && !enemyList.isEmpty()) {
            return enemyList;
        }
        // Return single enemy as a list
        List<Enemy> singleEnemyList = new ArrayList<>();
        if (enemy != null) {
            singleEnemyList.add(enemy);
        }
        return singleEnemyList;
    }

}
