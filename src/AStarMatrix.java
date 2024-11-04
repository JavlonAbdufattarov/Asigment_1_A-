    import java.util.*;

    public class AStarMatrix {
       private Scanner scanner = new Scanner(System.in);

        private static final int N = 9; // Size of the map 9x9
        private static final int UNKNOWN = -1; // Unknown
        private static final int FREE = 0; // Free cell
        private static final int BLOCKED = 1; // Blocked cell
        private static final int MARKO = 2; // Dangerous zone
        private static final int DANGER = 3; // Dangerous zone
        private static final int KEY = 3;
        private int[][] grid; // Map
        private Node[][] nodes; // Nodes for pathfinding
        private PriorityQueue<Node> openList; // Open list
        private Set<Node> closedList; // Closed list
        private int perceptionVariant; // Neo's perception zone
        private int goalX, goalY; // Target coordinates (Keymaker)

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            int perceptionVariant = scanner.nextInt();  // Neo's perception zone
            int goalX = scanner.nextInt();              // Keymaker's coordinates
            int goalY = scanner.nextInt();

            AStarMatrix solver = new AStarMatrix(perceptionVariant, goalX, goalY);
            solver.findPath(); // Start pathfinding
        }

        public AStarMatrix(int perceptionVariant, int goalX, int goalY) {
            this.grid = new int[N][N];
            this.nodes = new Node[N][N];
            this.openList = new PriorityQueue<>();
            this.closedList = new HashSet<>();
            this.perceptionVariant = perceptionVariant;
            this.goalX = goalX;
            this.goalY = goalY;

            // Initialize the map with -1 (unknown)
            for (int i = 0; i < N; i++) {
                Arrays.fill(grid[i], FREE);
                for (int j = 0; j < N; j++) {
                    nodes[i][j] = new Node(i, j);
                }
            }
        }

        public List<Node> findPath() {
            Node start = nodes[0][0]; // Стартовая позиция
            start.gCost = 0;
            start.hCost = calculateHeuristic(start);
            openList.add(start);
            boolean backtracking = false;

            while (!openList.isEmpty()) {
                Node current = openList.poll();

                // Проверка на достижение цели
                if (current.x == goalX && current.y == goalY) {
                    int shortestPathLength = calculateShortestPath();
                    return null;
                }

                // Проверка соседних клеток на наличие заблокированных
//                if (hasBlockedNeighbors(current) && !backtracking) {
//                    backtracking = true; // Переход в режим возврата к старту
//                    reverseToStart(current); // Возврат к начальной позиции
//                    // Начинаем новый поиск пути после возвращения
//                    return findAlternativePath(start);
//                }

                if (isDeadEnd(current)) {
                    continue; // Пропустить узел, если тупик
                }

                sendCommand("m " + current.x + " " + current.y); // Отправить команду передвижения
                List<String> dangers = getPerception(); // Получить информацию об опасностях
                updateGrid(current.x, current.y, dangers); // Обновить карту

                freeSurroundingCellsAfterMove(current); // Очистка зон после каждого движения
                exploreNeighbors(current); // Изучение соседних узлов
                closedList.add(current); // Добавить текущий узел в закрытый список
            }

            sendCommand("e -1"); // Если путь не найден
            return null;
        }

        // Метод для проверки соседних клеток на наличие заблокированных
        private boolean hasBlockedNeighbors(Node current) {
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Верх, низ, влево, вправо
            for (int[] direction : directions) {
                int neighborX = current.x + direction[0];
                int neighborY = current.y + direction[1];

                // Проверяем границы карты
                if (neighborX >= 0 && neighborX < grid.length && neighborY >= 0 && neighborY < grid[0].length) {
                    if (grid[neighborX][neighborY] == BLOCKED) {
                        return true; // Найдена заблокированная соседняя клетка
                    }
                }
            }
            return false; // Заблокированных соседей не найдено
        }

        // Метод для обратного перемещения
        private void reverseToStart(Node current) {
            while (current != null) {
                if (current.x==0&& current.y==0){break;}
                sendCommand("m " + current.x + " " + current.y); // Информация о текущей позиции
                current = current.parent; // Возвращение по родителям
                String Sss = scanner.nextLine();
            }

        }

        // Метод для поиска альтернативного пути
        private List<Node> findAlternativePath(Node start) {
            resetPathfindingState(); // Сброс состояния поиска, если необходимо
            openList.add(start); // Добавление начальной позиции в список открытых
            while (!openList.isEmpty()) {
                Node current = openList.poll();


                // Получаем второй элемент

                // Проверка на достижение цели
                if (current.x == goalX && current.y == goalY) {
                    int shortestPathLength = calculateShortestPath();
                    return null;
                }

                sendCommand("m " + current.x + " " + current.y); // Отправить команду передвижения
                List<String> dangers = getPerception(); // Получить информацию об опасностях
                updateGrid(current.x, current.y, dangers); // Обновить карту
                freeSurroundingCellsAfterMove(current); // Очистка зон после каждого движения
                exploreNeighbors(current); // Изучение соседних узлов
                closedList.add(current); // Добавить текущий узел в закрытый список
            }

            sendCommand("e -1"); // Если путь не найден
            return null;
        }

        // Метод для сброса состояния поиска пути
        private void resetPathfindingState() {
//            closedList.clear();
            openList.clear();
            // Возможно, нужно сбросить и другие переменные состояния, если они есть
        }


        private int calculateShortestPath() {
            // BFS to find the shortest path from (0, 0) to (goalX, goalY)
            boolean[][] visited = new boolean[N][N];
            Queue<Node> queue = new LinkedList<>();
            int[][] distance = new int[N][N];

            Node start = nodes[0][0];
            queue.add(start);
            visited[start.x][start.y] = true;

            // Initialize distances
            for (int i = 0; i < N; i++) {
                Arrays.fill(distance[i], Integer.MAX_VALUE);
            }
            distance[start.x][start.y] = 0;

            int[] dx = {-1, 1, 0, 0}; // Up, Down, Left, Right
            int[] dy = {0, 0, -1, 1}; // Up, Down, Left, Right

            while (!queue.isEmpty()) {
                Node current = queue.poll();

                // Check if we reached the goal
                if (current.x == goalX && current.y == goalY) {
                    for (int i = 0; i < N; i++) {
                        for (int j = 0; j < N; j++) {
                            if (grid[i][j] == UNKNOWN) {
                                grid[i][j] = FREE;
                            }
                        }
                    }
                    sendCommand("e " + distance[current.x][current.y]); // Output path length
                    return distance[current.x][current.y]; // Return path length
                }

                // Explore neighbors
                for (int i = 0; i < 4; i++) {
                    int newX = current.x + dx[i];
                    int newY = current.y + dy[i];

                    if (isValidCell(newX, newY) && !visited[newX][newY] && grid[newX][newY] == FREE) {
                        visited[newX][newY] = true;
                        distance[newX][newY] = distance[current.x][current.y] + 1; // Increment distance
                        queue.add(nodes[newX][newY]); // Add to the queue
                    }
                }
            }

            sendCommand("e -1"); // If no path is found
            return -1; // No path found
        }
        private boolean isDeadEnd(Node current) {
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // Up, Down, Left, Right
            boolean isDeadEnd = true;

            for (int[] dir : directions) {
                int newX = current.x + dir[0];
                int newY = current.y + dir[1];
                if (isValidCell(newX, newY) && grid[newX][newY] != BLOCKED && !closedList.contains(nodes[newX][newY])) {
                    isDeadEnd = false; // Found at least one valid neighboring cell
                    break;
                }
            }

            return isDeadEnd;
        }

        private void freeSurroundingCellsAfterMove(Node current) {
            // Check the perception variant to determine the radius of free cells to clear
            int radius = (perceptionVariant == 2) ? 2 : 1; // Use 2 for variant 2, 1 for variant 1

            // Iterate through cells within the radius around the current node
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int newX = current.x + dx;
                    int newY = current.y + dy;

                    // Ensure we stay within bounds and only free unknown cells
                    if (isValidCell(newX, newY) && (grid[newX][newY] == UNKNOWN || grid[newX][newY] == KEY)) {
                        grid[newX][newY] = FREE; // Free the cell
                    }
                }
            }
        }



        private void exploreNeighbors(Node current) {
            // Movement directions for straight moves (only priority)
            int[] dxStraight = {-1, 1, 0, 0}; // Up, Down, Left, Right
            int[] dyStraight = {0, 0, -1, 1}; // Up, Down, Left, Right
            int straightCost = 10; // Cost for straight moves
            // Explore straight moves
            for (int i = 0; i < 4; i++) {
                int newX = current.x + dxStraight[i];
                int newY = current.y + dyStraight[i];

                if (isValidCell(newX, newY) && grid[newX][newY] != BLOCKED) {
                    Node neighbor = nodes[newX][newY];

                    if (!closedList.contains(neighbor)) {
                        int newGCost = current.gCost + straightCost;

                        if (newGCost < neighbor.gCost) {
                            neighbor.gCost = newGCost;
                            neighbor.hCost = calculateHeuristic(neighbor);
                            neighbor.parent = current;
                        }

                        if (!openList.contains(neighbor)) {
                            openList.add(neighbor);
                        }
                    }
                }
            }
        }




        private int calculateHeuristic(Node node) {
            int highCost = 100; // High cost for blocked cells

            int currentCost;

            if (grid[node.x][node.y] == BLOCKED) {
                currentCost = highCost; // Highest cost for blocked cells
            }  else {
                currentCost = 0; // No cost for free cells
            }

            // Calculate Manhattan distance (unchanged)
            int manhattanDistance = Math.abs(node.x - goalX) + Math.abs(node.y - goalY);

            // Combine costs
            return currentCost + manhattanDistance;
        }



        private boolean isValidCell(int x, int y) {
            return x >= 0 && x < N && y >= 0 && y < N; // Boundary check
        }

        public void updateGrid(int x, int y, List<String> dangers) {
            // Update the map based on received data
            for (String danger : dangers) {
                String[] parts = danger.split(" ");
                int dangerX = Integer.parseInt(parts[0]);
                int dangerY = Integer.parseInt(parts[1]);
                String dangerType = parts[2];

                if (dangerType.equals("P") ) {
                    grid[dangerX][dangerY] = BLOCKED; // Dangerous zone
                }
                else if ( dangerType.equals("A")){
                    grid[dangerX][dangerY] = MARKO;
                }
                else if (dangerType.equals("S")) {
                    grid[dangerX][dangerY] = DANGER;
                }
                else if (dangerType.equals("K")) {
                    goalX = dangerX; // Update the goal if the Keymaker is found
                    goalY = dangerY;
                }
                else if (dangerType.equals("B")){
                    grid[dangerX][dangerY] = KEY;

                }

            }
        }
        private void freeSurroundingCells(int dangerType) {
            // Проходим по всей карте, чтобы найти зоны "Марко"

            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (grid[i][j] == dangerType) {
                        // Очищаем клетки вокруг "Марко"
                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dy = -1; dy <= 1; dy++) {
                                int newX = i + dx;
                                int newY = j + dy;
                                if (isValidCell(newX, newY) && grid[newX][newY] != BLOCKED) {
                                    grid[newX][newY] = UNKNOWN; // Очищаем клетки вокруг "Марко"
                                }
                            }
                        }
                    }
                }
            }

        }


        private List<Node> reconstructPath(Node current) {
            List<Node> path = new ArrayList<>();
            while (current != null) {
                path.add(current);
                current = current.parent;
            }
            Collections.reverse(path); // Reverse the path
            return path;
        }

        private List<String> getPerception() {
            Scanner scanner = new Scanner(System.in);
            int numberOfDangers = scanner.nextInt(); // Number of dangers
            List<String> dangers = new ArrayList<>();

            for (int i = 0; i < numberOfDangers; i++) {
                String danger = scanner.next() + " " + scanner.next() + " " + scanner.next();
                dangers.add(danger); // Add danger information
            }

            return dangers;
        }

        private void sendCommand(String command) {
            System.out.println(command); // Send command to the interactor
        }

        private void printGrid() {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (i == goalX && j == goalY) {

                        System.out.print("K "); // Keymaker
                    } else if (grid[i][j] == BLOCKED) {
                        System.out.print("X "); // Blocked cell
                    } else if (grid[i][j] == MARKO) {
                        System.out.print("M "); // Blocked cell
                    } else if (grid[i][j] == DANGER) {
                        System.out.print("D "); // Dangerous zone
                    } else if (grid[i][j] == KEY) {
                        System.out.print("B "); // Free cell
                    } else if (i == 0 && j == 0) {
                        System.out.print("N "); // Neo's starting position
                    } else if (grid[i][j] == FREE) {
                        System.out.print("0 ");
                    }
                    else {
                        System.out.print("? "); // Unknown cell
                    }
                }
                System.out.println(); // New line for the next row
            }
            System.out.println(); // Extra line for spacing
        }
    }

    class Node implements Comparable<Node> {
        int x, y;
        int gCost, hCost; // gCost - cost of the path, hCost - heuristic estimate
        Node parent;

        public Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.gCost = Integer.MAX_VALUE; // Initialize to infinity
            this.hCost = 0;
            this.parent = null;
        }

        public int getFCost() {
            return hCost; // Use only hCost for greedy strategy
        }
        public int getgCost(){
            return gCost;
        }

        @Override
        public int compareTo(Node other) {
            return Comparator.comparingInt(Node::getFCost) // Use only hCost for greedy strategy
                    .thenComparingInt(Node::getgCost)
                    .compare(this, other);
        }
    }

