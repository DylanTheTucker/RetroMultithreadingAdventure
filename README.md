---------READ ME-------------
DYLAN TUCKER
CSC 325 - FA25

PROJECT NAME:
- The Knight's Journey

SUMMARY:

- You play as a knight going on an adventure to defeat a powerful wizard. Along the way the knight encounters strange foes, and has to flee the wizard until they've gained enough strength to fight...

TECHNOLOGIES:

- Java ExecutorService for thread pool management (ExecutorService created but not used; direct Thread instantiation preferred for join() synchronization)
- Multithreading with synchronized blocks and wait/notify
- AtomicInteger for thread-safe counters
- Volatile variables for visibility guarantees across threads
- Thread.join() for proper thread synchronization at shutdown
- Stream API with lambda expressions for functional programming (groupingBy, forEach, filter, max, map, distinct, sorted)
- ArrayList for tracking battle statistics and loot collection
- Player input interaction system

Setup instructions:

- Run the project through VS Code or compile and execute `App.java` as the main entry point.

CONTRIBUTORS AND RESPONSIBILITIES:

- Dylan Tucker > Developer

Multithreading Use:

    - Will primarily occur in the GameManager class so that each game character can be passed in as a thread and an AtomicInteger counter will keep track of every "step." This way all multithreading implementation is kept in one place when necessary, with the only exception being implementing the Runnable interface.
    
    Thread Architecture:
    - Player Thread: Independent thread for player character, waits on stepLock for notifications
    - StepManager Thread: Monitors global step counter and spawns enemy encounters
    - CharacterEnemy Threads (Thief, Wizard): Independent threads that intelligently pursue/retreat
    - Basic enemies (Slimes, TickiBirds) implement Runnable but do NOT run as separate threads; they execute battles synchronously on the game thread
    
    Thread Coordination:
    - Uses wait/notify pattern with stepLock for step synchronization
    - Volatile flags (battleInProgress, shouldPlayerMove, playerHasMoved) for state management
    - AtomicInteger for thread-safe counters (globalStepCounter, notificationCounter)
    - Thread.join() for proper thread termination at game end
    
    Concurrency Mechanisms:
    - Synchronized methods: Player.gainExperience(), record statistics methods
    - Synchronized blocks with explicit locks: Player gold/loot management (resourceLock)
    - Dedicated locks: stepLock (game progression), resourceLock (player resources), inputLock (user input)
    - Prevents race conditions on shared resources (experience, gold, battle statistics)

API IDEA:

    - An idea for an API I could use is one meant to enable multiplayer in the game. Maybe players either step together and get a turn to attack, or they step separately and the game is sort of like a race to the end. The API would call for data shared between the players and then use the data to sync their gameplay together.

CONTINUATION IDEAS:
    - Breaking apart/ simplifying GameManager and StepManager
    - Adding more enemies or character enemies
    - Giving Thief and wizard more unique abilities
    - Player unlocking attacks every few levels
    - Items
    - Creating non-enemy steppable characters like a wandering salesman or somthing similar

Classes, Interfaces, Superclasses, cont.:

    Superclasses:

    Abstract GameCharacter
    - An Abstract class meant to set up all game characters from the player, to different enemy types

    Abstract DataHolder
    - Every GameCharacter will use a helper class to set up a "DataHolder" object that can be used by the game to communicate between objects. This class is meant to set up the standard for these helper classes.

    GameManager
    - Will control the main flow of the game though outside the actual main class, mostly for wrapping up the game flow to be more abstract/ readable
    - Will include the executor service which controls primary threads. 
    - Will also include other objects game will depend on. 

    StepManager
    - An object which controls the game's full progression. Sets the maximum number of steps, and what events will occur at each step.
    - Runs as its own thread, monitoring global step counter and triggering events
    - Spawns enemy encounters (Slimes, TickiBirds) at predefined steps using EnemyFactory
    - Will use a helper class to set up step events
    - Step events will likely be contained in a switch statement until I come up with a better solution

    BattleManager
    - Manages all combat encounters between the player and enemies
    - Handles battle initialization, turn order, and battle completion
    - Sets battleInProgress flag to pause other game threads during combat
    - Works with AttackInteraction and PlayerInputHandler for battle flow

    Intermediate Abstract Classes:

    Abstract Enemy
    - extends the GameCharacter class, but does not implement all methods. Will not be instantiated on its own, so it cannot be made concrete.
    - Will set up common functions of all enemies
    - Main functionality is that it will trigger a battle when the player step is the same as the enemy's step

    Abstract CharacterEnemy
    - extends the Enemy class, abstract since there will still be multiple types of these with different implementations of some funtions
    - Main function is that it can change step where player will be encountered


    Subclasses:

    Player
    - extends GameCharacter
    - Main player character, has different attacks, values, and takes a certain amount of steps each run

    Slime
    - extends Enemy class
    - Basic enemy with low health and attack, spawned at specific steps by StepManager
    - Implements Runnable but does not run as a separate thread; participates in battles on the game thread

    TickiBird
    - extends Enemy class
    - More dangerous enemy with explosive behavior mechanic
    - Spawned by StepManager at specific steps, participates in battles synchronously

    Thief, Wizard
    - both extend CharacterEnemy class
    - Unlike normal enemies, these enemies will move along steps like the player and are generally stronger. Both will be more relevant to story than other smaller enemies.
    - Run as independent threads that intelligently pursue/retreat from the player

    Interfaces:

    CanStep
    - Allows character to change the step it is on/ move around

    Helper Classes and Event Handling:

    CharacterData
    - Data holder class that stores character stats (health, attack, stealth, etc.)
    - Used for communication between game objects and the GameManager
    - Implements abstract DataHolder pattern

    NOTE: CharacterData ended up being unused, but I am leaving it in as an example of something that could be implemented later on. If for example multiplayer was added, this would be a way to more carefully manage player data without constant interactions over the cloud, but instead sending updated info consistently in one package. For now the current set-up works but CharacterData would be good for when the project might reach a larger size; and I felt leaving this as a note was more constructive than removing any trace of it!

    StepEvent
    - Abstract class representing events that occur at specific game steps
    - Subclasses define what happens when player reaches that step
    - Used by StepManager to organize game progression

    EnemyEncounter
    - extends StepEvent
    - Represents a battle encounter with enemies at a specific step
    - Spawns enemies using EnemyFactory and triggers battles through BattleManager
    - Can spawn single enemies or groups of enemies

    EnemyFactory
    - Factory pattern class for creating enemy instances
    - Maintains prefab templates for Slimes and TickiBirds
    - Handles enemy ID assignment and instantiation
    - Used by StepManager to spawn enemies at specific steps

    Player Input System:

    PlayerInputHandler
    - Manages all user input during gameplay and battles
    - Uses dedicated inputLock for thread-safe input coordination
    - Provides menu options and waits for player choices
    - Works with Scanner for console input

    Interaction (Abstract)
    - Base class for all player interactions (battles, events, etc.)
    - Defines common behavior for turn-based interactions
    - Subclasses implement specific interaction types

    AttackInteraction
    - extends Interaction
    - Manages combat encounters with enemies
    - Provides attack, defend, heal, flee, and check enemy options
    - Handles loot drops using random array selection and lambda expressions
    - Awards experience and gold through synchronized methods
    - Tracks battle statistics (wins, losses, fled) for functional enhancements

    AttackChosen
    - Helper class for AttackInteraction
    - Handles specific attack target selection and damage calculation
    - Uses lambda expression with removeIf() to process defeated enemies
    - Awards experience and gold when enemies are defeated


METHOD IMPLEMENTATION:

- See UML diagram for overview of all class functions (will be updated as development progresses)
- Will not cover individual getter and setter methods, as they should all be self-explanatory.

GameCharacter

    updateDataObject(DataHolder) - this method will be used for objects to communicate with their main data holder in the GameManager to update it.

    takeDamage and dealDamage are somewhat self-explanatory but do need some explanation. The actual conditions and behaviors when characters deal/ take damage will be different but all end in the same way, therefore these functions are abstract for the sake of polymorphism but the inner conditions will be added in the form of helper methods. Different attacks are an example of this.

Player

    levelUp - adds the given experience to the player's experience variable, but also checks if it has passed some threshold. If it has, the player's level is increased and experience is given a modulo of the threshold (instead of going to zero, that way remainder can be preserved)

    gainExperience - synchronized method that adds experience and checks for level up, prevents race conditions when multiple enemies are defeated simultaneously

    addGold, spendGold, getGold - synchronized methods using explicit resourceLock for thread-safe gold management

    collectLoot - synchronized method for collecting items and gold from battles, adds to itemsCollected ArrayList

    recordEnemyDefeated, recordBattleWon, recordBattleLost, recordBattleFled - synchronized methods that track battle statistics in ArrayLists

    getBattleStatistics - uses Stream API with lambda expressions to aggregate and display battle data (groupingBy, forEach, filter, distinct, sorted)

    getMostDefeatedEnemyType - uses Stream API with max() aggregation and lambda comparator to find most common enemy

Enemy

    attackPlayer - self-explanatory for the most part, like the deal/ take damage will be useful for wrapping behaviors

    canSpawn - When the enemy will exist as a separate thread/ when it will run. Will be overriden by CharacterEnemy class

    reset - resets enemy stats after battle for reuse by EnemyFactory

    getExpOnDefeat, getGoldValue - return rewards for defeating this enemy

Slime

    run - prints flavor text but does NOT create a separate thread; called for display purposes only

TickiBird

    run - prints flavor text for TickiBird behavior

    uniqueBehavior - implements explosive attack mechanic that increases damage over turns 

CharacterEnemy

    getIntelligentMovement - calculates movement based on player position (chase forward or retreat backward)

    freeze - stuns enemy for multiple turns after player flees from battle

Thief and Wizard

    stepForward - overrides movement behavior with unique flavor text for each character

    all other methods are helper methods for attack and behavior implementation

DataHolder

    not entirely sure how the implementation will end up but it should all just be getter and setter methods

GameManager

    add/remove GameCharacter - adds and removes the passed gameCharacter from the active characters list. Will be used for switching out minor enemies primarily.

    startCharacterThreads - creates and starts Thread objects for Player, StepManager, and CharacterEnemies

    globalStepForward - increments global step counter and uses stepLock.notifyAll() to coordinate thread actions in two phases (player movement, then enemy movement)

    checkCharacterEnemyEncounters - detects when player and CharacterEnemy are on same step, triggers battle

    endGame - uses thread.join() for proper thread synchronization and cleanup, displays battle statistics using stream operations

StepManager

    run - main thread loop that monitors globalStepCounter using wait/notify pattern, announces events, and triggers battles when player reaches event steps

    getInteractionForStep - retrieves Interaction for a specific step if one exists

    stop - safely stops the StepManager thread

BattleManager

    startBattle - initializes battle with enemies, sets battleInProgress flag, creates AttackInteraction

    endBattle - cleans up after battle, resets battleInProgress flag

EnemyFactory

    createEnemy - factory method that instantiates enemies by type with proper IDs

    spawnEnemies - creates multiple enemies for group encounters

EnemyEncounter

    performEvent - triggers battle through BattleManager when player reaches this step

    spawnEnemy - creates enemy instance using EnemyFactory

StepEvent

    performEvent - abstract method that subclasses override to define step behavior

    hasInteraction - checks if this event has an associated Interaction

PlayerInputHandler

    waitForStepAdvance - uses inputLock with wait/notify to coordinate user input with game thread

    getMenuChoice - displays menu options and returns player selection

    clearInput - resets input state for next turn

AttackInteraction

    checkBattleStatus - determines if battle continues or ends, awards experience/gold/loot, records statistics

    dropRandomLoot - uses random.ints().mapToObj() with lambda expressions to generate random loot from String array

    Inner classes (AttackOption, DefendOption, HealOption, FleeOption, CheckEnemyOption) - represent player battle actions

AttackChosen

    execute - handles attack logic and enemy defeat processing using removeIf() lambda expression

    checkAndRemoveDefeatedEnemies - uses lambda with removeIf() to remove defeated enemies and award rewards

INTERFACES

    CanStep

        determineSteps - calculate steps the character will perform

        changeStep - update character step and perform any actions relating to this update


