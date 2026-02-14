import java.util.List;

/**
 * Tile class is finale so no changes are implemented to it that may cause impact on it.
 * It implements the Terrain type, the token, the ID and the list of intersections that
 * surround it, that way we can keep track of each tile and have them structured through the map
 */
public final class Tile {
    private List<Intersection> intersections;
    private Terrain terrain;
    private int token;
    private int tileID;

    /**
     * Tile constructor needs an Id, a type Terrain to define what type the tile will have as well as a token
     * that will be used for further implementation, and a list of the intersections that the tile will border.
     * @param tileID The id of each tile so we can keep track of the 19 tiles of the board
     * @param terrain the type terrain the tile will have
     * @param token the token that will be used for further calculation
     * @param intersections the intersections that will border each tile
     */
    Tile(int tileID, Terrain terrain, int token, List<Intersection> intersections) {
        this.tileID = tileID;
        this.terrain = terrain;
        this.token = token;
        this.intersections = intersections;
    }
    // Getter method to return the intersections of the tile
    List<Intersection> getIntersections() {
        return intersections;
    }
    // Getter method to return the terrain type of the tile
    Terrain getTerrain() {
        return terrain;
    }
    // Getter method to return the token assigned to the tile
    int getToken() {
        return token;
    }
    // Getter method to return the ID of the tile
    int getTileID() {
        return tileID;
    }

    /**
     * Getter method to get the resource produced by that terrain
     * This method is implemented in the Terrain Enum class
     */
     public static Resource getResource(Terrain t) {
         switch (t) {
             case MOUNTAIN: return Resource.ORE;
             case FOREST:   return Resource.LUMBER;
             case HILLS:    return Resource.BRICK;
             case FIELDS:   return Resource.GRAIN;
             case PASTURE:  return Resource.WOOL;
             case DESERT:   return null;
             default:       return null;
         }
    }
}