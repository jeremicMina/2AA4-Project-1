import java.util.*;

/**
 * BoardConfig is a class that is responsible for building the map, setup for Terrains, Tokens per their tiles
 * and the coordination for tiles and the rings as well as assigning the ids per position (vertex, axial, point and edge).
 */
final class BoardConfig {

    private BoardConfig() {}

    /**
     * Creates a valid 19-hex map with:
     * - 19 tiles
     * - 54 intersections
     * - edges between intersections
     *
     * Note: This is a valid Catan topology generator.
     * The "exact node id numbering from the picture" can be enforced here if needed later.
     */
    static void buildMap(List<Tile> tilesOut, List<Intersection> intersectionsOut, List<Edge> edgesOut) {
        // 1) Create tiles with axial coordinates
        List<Axial> tileCoords = tileIdAxialCoords();

        Terrain[] terrainById = terrainByTileId();
        int[] tokenById = tokenByTileId();

        // 2) Create unique intersections (vertices)
        Map<VertexKey, Intersection> vertexToIntersection = new HashMap<>();
        List<List<Intersection>> tileCorners = new ArrayList<>();
        double size = 10.0;

        for (int tileId = 0; tileId < 19; tileId++) {
            Axial a = tileCoords.get(tileId);
            Point center = axialToPointFlatTop(a, size);

            List<Point> corners = hexCornersFlatTop(center, size);
            List<Intersection> intersectionsForTile = new ArrayList<>(6);

            for (Point corner : corners) {
                VertexKey key = new VertexKey(corner.x, corner.y);
                Intersection inter = vertexToIntersection.get(key);
                if (inter == null) {
                    inter = new Intersection(-1); // ID assigned later
                    vertexToIntersection.put(key, inter);
                }
                intersectionsForTile.add(inter);
            }
            tileCorners.add(intersectionsForTile);
        }

        // 3) Assign node IDs BEFORE creating edges
        List<Intersection> allIntersections = new ArrayList<>(vertexToIntersection.values());
        assignNodeIds(allIntersections, tileCorners.get(0)); // BFS + deterministic IDs

        // 4) Create edges with proper nodeIDs
        Map<EdgeKey, Edge> edgeMap = new HashMap<>();
        for (int tileId = 0; tileId < 19; tileId++) {
            List<Intersection> corners = tileCorners.get(tileId);

            for (int i = 0; i < 6; i++) {
                Intersection a = corners.get(i);
                Intersection b = corners.get((i + 1) % 6);

                EdgeKey eKey = new EdgeKey(a.getNodeID(), b.getNodeID());
                if (!edgeMap.containsKey(eKey)) {
                    Edge e = new Edge(-1, a, b);
                    edgeMap.put(eKey, e);

                    a.addEdge(e);
                    b.addEdge(e);
                }
            }
        }

        List<Edge> allEdges = new ArrayList<>(edgeMap.values());
        allEdges.sort(Comparator.<Edge>comparingInt(e -> Math.min(e.getIntersection1().getNodeID(), e.getIntersection2().getNodeID()))
                .thenComparingInt(e -> Math.max(e.getIntersection1().getNodeID(), e.getIntersection2().getNodeID())));

        for (int i = 0; i < allEdges.size(); i++) {
            allEdges.get(i).setEdgeID(i);
        }


        // 5) Create tiles and link to intersections
        for (int tileId = 0; tileId < 19; tileId++) {
            List<Intersection> corners = tileCorners.get(tileId);

            Tile tile = new Tile(tileId, terrainById[tileId], tokenById[tileId], corners);

            for (Intersection inter : corners) {
                inter.addTile(tile);
            }

            tilesOut.add(tile);
        }

        // 6) Export final lists
        allIntersections.sort(Comparator.comparingInt(Intersection::getNodeID));
        intersectionsOut.addAll(allIntersections);
        edgesOut.addAll(allEdges);
    }

    /**
     * Took the same logic of the map in the catan game and implemented the terrains on the 19 tiles that way
     * @return list of terrains just like the map
     */
    private static Terrain[] terrainByTileId() {
        Terrain[] t = new Terrain[19];
        // Using your provided board (tile IDs + terrain):
        t[0] = Terrain.FOREST;   // WOOD
        t[1] = Terrain.FIELDS;   // WHEAT
        t[2] = Terrain.HILLS;    // BRICK
        t[3] = Terrain.MOUNTAIN; // ORE
        t[4] = Terrain.PASTURE;  // SHEEP
        t[5] = Terrain.PASTURE;  // SHEEP
        t[6] = Terrain.PASTURE;  // SHEEP
        t[7] = Terrain.FIELDS;   // WHEAT
        t[8] = Terrain.MOUNTAIN; // ORE
        t[9] = Terrain.FOREST;   // WOOD
        t[10] = Terrain.MOUNTAIN;// ORE
        t[11] = Terrain.FIELDS;  // WHEAT
        t[12] = Terrain.FOREST;  // WOOD
        t[13] = Terrain.HILLS;   // BRICK
        t[14] = Terrain.HILLS;   // BRICK
        t[15] = Terrain.FIELDS;  // WHEAT
        t[16] = Terrain.DESERT;  // DESERT
        t[17] = Terrain.FOREST;  // WOOD
        t[18] = Terrain.PASTURE; // SHEEP
        return t;
    }

    /**
     * Attributing the tokens to the 19 tiles following the map suggested in the assignment Catan guideline
     * @return list of integer tokens of the 19 tiles
     */
    private static int[] tokenByTileId() {
        int[] token = new int[19];
        token[0] = 10;
        token[1] = 11;
        token[2] = 8;
        token[3] = 3;
        token[4] = 11;
        token[5] = 5;
        token[6] = 12;
        token[7] = 3;
        token[8] = 6;
        token[9] = 4;
        token[10] = 6;
        token[11] = 9;
        token[12] = 5;
        token[13] = 9;
        token[14] = 8;
        token[15] = 4;
        token[16] = 0; // desert has no token; kept 0 to simplify checks
        token[17] = 2;
        token[18] = 10;
        return token;
    }

    /**
     * Method to return the list of axial tiles coordinates
     */
    private static List<Axial> tileIdAxialCoords() {
        // 19-hex "radius 2" layout:
        // We place tile 0 at (0,0). Then ring 1 and ring 2 in clockwise order.
        // This keeps tile IDs stable and separated from the rest of the system.
        List<Axial> coords = new ArrayList<>(19);

        coords.add(new Axial(0, 0)); // tile 0

        // ring 1 (6 tiles)
        coords.addAll(ringCoords(1));

        // ring 2 (12 tiles)
        coords.addAll(ringCoords(2));

        // total should be 19
        return coords;
    }

    /**
     * Returns axial coords for a ring of radius r around (0,0), in clockwise order.
     */
    private static List<Axial> ringCoords(int radius) {
        List<Axial> ring = new ArrayList<>(radius * 6);
        // directions in axial coordinates (flat-top)
        Axial[] dirs = new Axial[] {
                new Axial(1, 0),   // E
                new Axial(1, -1),  // NE
                new Axial(0, -1),  // NW
                new Axial(-1, 0),  // W
                new Axial(-1, 1),  // SW
                new Axial(0, 1)    // SE
        };

        // start at "south-west" corner of the ring to get a stable loop
        Axial cube = new Axial(-radius, radius);

        for (int side = 0; side < 6; side++) {
            for (int step = 0; step < radius; step++) {
                ring.add(cube);
                cube = cube.add(dirs[side]);
            }
        }
        return ring;
    }

    /**
     * Assignment node ID's of the intersections and the center tile corners intersections
     * @param all list of all intersections
     * @param centerTileCorners the center tile 6 intersections
     */
    private static void assignNodeIds(List<Intersection> all, List<Intersection> centerTileCorners) {
        // Assign 0..5 to center tile corners in a stable geometric order
        // We try to match the idea "center tile corners are first" (as in the spec).
        // Order target: top-right=0, right=1, bottom-right=2, bottom-left=3, left=4, top-left=5.

        // We don't have direct coordinates stored in Intersection. So we assign based on adjacency
        // order from the list produced by hexCornersFlatTop(...) which is already stable:
        // [top-left, top-right, right, bottom-right, bottom-left, left]
        // Map them into the desired IDs:
        Intersection topLeft = centerTileCorners.get(0);
        Intersection topRight = centerTileCorners.get(1);
        Intersection right = centerTileCorners.get(2);
        Intersection bottomRight = centerTileCorners.get(3);
        Intersection bottomLeft = centerTileCorners.get(4);
        Intersection left = centerTileCorners.get(5);

        topRight.setNodeID(0);
        right.setNodeID(1);
        bottomRight.setNodeID(2);
        bottomLeft.setNodeID(3);
        left.setNodeID(4);
        topLeft.setNodeID(5);

        // Assign remaining IDs (6..53) by BFS expanding outward from those 6 nodes.
        // This produces a "ring-like" numbering, deterministic for debugging/testing.
        Set<Intersection> seeded = new HashSet<>(centerTileCorners);

        // BFS frontier starts from the 6 center corners in ID order
        List<Intersection> start = List.of(topRight, right, bottomRight, bottomLeft, left, topLeft);
        Queue<Intersection> q = new ArrayDeque<>(start);

        int nextId = 6;

        while (!q.isEmpty()) {
            Intersection cur = q.poll();
            for (Intersection nb : cur.getAdjacentIntersections()) {
                if (nb.getNodeID() != -1) continue;
                nb.setNodeID(nextId++);
                q.add(nb);
            }
        }

        // If BFS didn’t reach everything (should be rare), assign remaining by insertion order
        if (nextId <= 53) {
            for (Intersection i : all) {
                if (i.getNodeID() == -1) {
                    i.setNodeID(nextId++);
                }
            }
        }

        // Safety check
        if (nextId != 54) {
            throw new IllegalStateException("Expected 54 intersections, assigned: " + nextId);
        }
    }

    /**
     * Converts axial hex coordinates (q, r) to Cartesian pixel coordinates (x, y)
     * for a "flat-top" hexagon orientation.
     * @param a The axial coordinate object containing q (column) and r (row).
     * @param size The distance from the center of the hex to any corner.
     * @return A Point object representing the center of the hexagon in 2D space.
     */
    private static Point axialToPointFlatTop(Axial a, double size) {
        // Horizontal distance between adjacent hex centers is 3/2 * size
        double x = size * (1.5 * a.q);
        // Vertical distance involves sqrt(3) due to the geometry of equilateral triangles within the hex
        double y = size * (Math.sqrt(3) * (a.r + a.q / 2.0));
        return new Point(x, y);
    }

    /**
     * Calculates the 2D Cartesian coordinates for all six corners of a flat-top hexagon.
     * Returns corners in a stable clockwise/counter-clockwise order to ensure
     * consistency when mapping edges and shared vertices.
     * * @param center The central (x, y) point of the hexagon.
     * @param size The radius/distance to corners.
     * @return A list of 6 Points representing the vertices.
     */
    private static List<Point> hexCornersFlatTop(Point center, double size) {
        // Pre-calculated offsets for a flat-top hex orientation.
        // These are derived from the trigonometric functions of the corner angles.
        double[][] offsets = new double[][]{
                {-size * 0.5, -size * Math.sqrt(3) / 2.0}, // top-left
                { size * 0.5, -size * Math.sqrt(3) / 2.0}, // top-right
                { size, 0},                               // right
                { size * 0.5, size * Math.sqrt(3) / 2.0},  // bottom-right
                {-size * 0.5, size * Math.sqrt(3) / 2.0},  // bottom-left
                {-size, 0}                                // left
        };

        List<Point> corners = new ArrayList<>(6);
        for (double[] o : offsets) {
            // Apply offsets to the center point to get absolute coordinates
            corners.add(new Point(center.x + o[0], center.y + o[1]));
        }
        return corners;
    }

    /**
     * Represents a coordinate in an Axial system (q = column, r = row).
     * Common in hex-grid math to simplify distance and neighbor calculations.
     */
    private static final class Axial {
        final int q;
        final int r;

        Axial(int q, int r) {
            this.q = q;
            this.r = r;
        }
        /** Helper to calculate neighbor coordinates by adding vector offsets. */
        Axial add(Axial other) {
            return new Axial(this.q + other.q, this.r + other.r);
        }
    }
    /** Simple container for 2D Cartesian coordinates. */
    private static final class Point {
        final double x;
        final double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

     /**
     * A utility class used as a key in HashMaps to identify unique intersections.
     * Since floating-point math (double) can have precision errors (e.g., 1.0000000001),
     * this class rounds coordinates to 6 decimal places to ensure that vertices shared
     * by multiple hexes are correctly identified as the exact same point.
     */
    private static final class VertexKey {
        final long rx;
        final long ry;

        VertexKey(double x, double y) {
            // round to 1e-6 scale
            this.rx = Math.round(x * 1_000_000.0);
            this.ry = Math.round(y * 1_000_000.0);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof VertexKey)) return false;
            VertexKey other = (VertexKey) o;
            return rx == other.rx && ry == other.ry;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rx, ry);
        }
    }

    /**
     * A utility class used to identify unique edges between two intersections.
     * It ensures that an edge from Node A to Node B is treated the same as
     * an edge from Node B to Node A (undirected) by sorting the IDs internally.
     */
    private static final class EdgeKey {
        final int a;
        final int b;

        EdgeKey(int n1, int n2) {
            // Sort to ensure (1, 2) is the same as (2, 1)
            this.a = Math.min(n1, n2);
            this.b = Math.max(n1, n2);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof EdgeKey)) return false;
            EdgeKey other = (EdgeKey) o;
            return a == other.a && b == other.b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }
}
