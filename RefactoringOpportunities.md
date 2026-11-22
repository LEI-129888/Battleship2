| local | nome do cheiro no código | nome da refabricação     | número de alun@ |
| :--- |:-------------------------|:-------------------------|:----------------|
| `Fleet::printStatus` | Dead Code                | Safe Delete              | 129867          |
| `Fleet::colisionRisk` | Complex Condition        | Invert Boolean           | 129888          |
| `Fleet::printAllShips` | Speculative Generality   | Safe Delete              | 129865          |
| `Fleet::printFloatingShips` | Speculative Generality   | Safe Delete              | 129867          |
| `Fleet::printShipsByCategory` | Speculative Generality   | Safe Delete              | 129888          |
| `Carrack` | Dead Code                | Safe Delete              | 129867          |
| `Compass::charToCompass` | Switch Statements        | Extract Method           | 129867          |
| `Ship::buildShip` | Switch Statements        | Extract Method           | 129865          |
| `Tasks::menu` | Redundant Assignment     | Inline Variable          | 129888          |
| `Ship::isInsideBoard` | Complex Condition        | Introduce Variable       | 129865          |
| `Game::jsonShots` | Redundant Assignment     | Safe Delete              | 129867         |
| `Caravel` | Duplicated Code          | Extract Method           | 129867          |
| `Carrack` | Duplicated Code          | Extract Method           | 129888          |
| `Frigate` | Duplicated Code          | Extract Method           | 129865          |
| `Tasks::menu` | Primitive Obsession      | Replace Type with Object | 129888          |
| `Tasks::executeTurns` | Long Method              | Extract Method           | 129888          |
| `Game::randomEnemyFire` | Long Method              | Extract Method           | 129865          |
| `Ship::positions` | Data Class               | Encapsulate Fields       | 129865 |
| `Game::fillShotsFromCandidates` | Unused Parameter         | Change Signature         | 129865 |
| `Compass::charToCompass` | Redundant Assignment     | Inline Variable          | 129867 |
| `Ship::getAdjacentPositions` | Duplicated Code          | Introduce Variable       | 129867 |
| `Fleet::createRandom` | Duplicated Code          | Replace with Constant    | 129867 |
