Previn Kumar, Minhal Gardezi
EECS 391 Project 1

Resource Agent
This agent tasks a Peasant with collecting wood or gold, whichever is currently less. Once there is enough resources, it creates a second peasant using the TownHall. The two peasants continue to collect resources until they can build a single Farm, a single Barracks, and then up to 3 Footmen in that order.

The documentation was helpful so far. It was confusing to try and build the farm and barracks at first, because the peasants are tasked with building them instead of the TownHall and in order to complete a compound building action, specific coordinates are needed.


Combat Agent
This agent revisits the combat example given in the tutorial with a new strategy. Instead of ignoring Unit type we focus our archers (long range) to combat the tower (long range, immobile) while the ballista and footmen focus on the enemy footmen. 

It is still hard to make a combat scenario that can beat the enemies in this game. Combat actions are often interrupted and need to be dealt with promptly in order to have a chance.