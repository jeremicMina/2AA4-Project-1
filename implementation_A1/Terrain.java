/**
 * Terrain enumeration, better implementation than Switch cases. This enumeration will be mainly used for tiles.
 * The terrain has the resources coupled with each terrain that is responsible for producing that specified resource
 */

public enum Terrain {
    MOUNTAIN,
    FOREST,
    HILLS,
    FIELDS,
    PASTURE,
    DESERT;
//
//    private final Resource resource;
//
//    /**
//     * Constrctor Terrain associated with the resource it produces
//     * @param resource
//     */
//    Terrain(Resource resource) {
//        this.resource = resource;
//    }
//
//    /**
//     * Getter method to return the resource of the terrain, will be implemented in the resourceProduction class
//     * @return
//     */
//    public Resource getResource() {
//        return resource;
//    }
}