package org.ala.layers.util;

import java.io.File;
import java.sql.ResultSet;
import org.apache.log4j.Logger;

/**
 *
 * @author jac24n
 */
public class Intersect {
    
    final static String ALASPATIAL_OUTPUT_PATH = "/data/ala/runtime/output";
    final static String DATA_FILES_PATH = "/data/ala/data/envlayers/WorldClimCurrent/10minutes/";
    
    /**
     * Log4j instance
     */
    
    protected static Logger logger = Logger.getLogger("org.ala.layers.util.Intersect");
    /**
     * This method performs the layer intersection operation
     * - polygon intersections for contextual layers
     * - point values for raster layers
     * @param ids Layer ids to use for the intersection
     * @param lat Latitude of point
     * @param lng Longitude of point
     * @return JSON result of intersect operation
     */
    public static String Intersect(String ids, Double lat, Double lng){
               StringBuilder sb = new StringBuilder();

        System.out.println("====================================================");
        System.out.println("Got Intersect request");
        System.out.println("====================================================");

        sb.append("[");

        for(String id : ids.split(",")) {
            if(sb.length() > 1) {
                sb.append(",");
            }

            String s = "";

            System.out.println("Getting layer for " + id);

            Layer layer = Layer.getLayer(id);

            double [][] p = {{lng, lat}};

            if(layer != null) {
                System.out.println("layer: " + layer.getDisplayName() + ", is shape: " + layer.isShape());
                if(layer.isShape()) {
                    String query = "SELECT fid, id, name as value, \"desc\", '"
                            + layer.getDisplayName()
                            + "' as layername FROM objects WHERE fid='cl"
                            + layer.getId()
                            + "' AND ST_Within(ST_SETSRID(ST_Point("
                            + lng
                            + "," 
                            + lat
                            + "),4326), the_geom);";

                    System.out.println("query: " + query);
                    logger.info("Query is " + query);

                    ResultSet r = DBConnection.query(query);

                    s = Utils.resultSetToJSON(r);
                    s = s.substring(1,s.length() - 1);

                } else if (layer.isGrid()) {
                    Grid g = new Grid(DATA_FILES_PATH + layer.getPathOrig());

                    float [] v = g.getValues(p);

                    s = "{\"value\":" + v[0] + ",\"layername\":\"" + layer.getDisplayName() + "\"}";
                }       
            } else {
                String gid = null;
                String filename = null;
                String name = null;

                if(id.startsWith("species_")) {
                    //maxent layer
                    gid = id.substring(8);
                    filename = ALASPATIAL_OUTPUT_PATH + "/maxent/" + gid + "/" + gid;
                    name = "Prediction";
                } else if(id.startsWith("aloc_")) {
                    //aloc layer
                    gid = id.substring(8);
                    filename = ALASPATIAL_OUTPUT_PATH + "/aloc/" + gid + "/" + gid;
                    name = "Classification";
                }

                if (filename != null) {
                    Grid grid = new Grid(filename);

                    if(grid != null && (new File(filename + ".grd").exists()) ) {
                        float [] v = grid.getValues(p);
                        if(v != null) {
                            if(Float.isNaN(v[0])) {
                                s = "{\"value\":\"no data\",\"layername\":\"" + name + " (" + gid + ")\"}";
                            } else {
                                s = "{\"value\":" + v[0] + ",\"layername\":\"" + name + " (" + gid + ")\"}";
                            }
                        }
                    }
                }
            }
            
            sb.append(s);
        }

        sb.append("]");

        System.out.println("====================================================");

        return sb.toString();
    }
}