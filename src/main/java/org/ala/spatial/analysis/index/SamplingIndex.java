package org.ala.spatial.analysis.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.ala.spatial.util.Grid;
import org.ala.spatial.util.Layer;
import org.ala.spatial.util.Layers;
import org.ala.spatial.util.SimpleShapeFile;
import org.ala.spatial.util.SpatialLogger;
import org.ala.spatial.util.TabulationSettings;

/**
 * builder for sampling index.
 *
 * requires OccurrencesIndex to be up to date.
 *
 * operates on GridFiles
 * operates on Shape Files
 *
 * @author adam
 *
 */
public class SamplingIndex implements AnalysisIndexService {

    /**
     * prefix for continous/environmental sampling index files
     */
    static final String CONTINOUS_PREFIX = "SAM_D_";
    /**
     * prefix for catagorical/contextual sampling index files
     */
    static final String CATAGORICAL_PREFIX = "SAM_I_";
    /**
     * postfix for sampling index data files
     */
    static final String VALUE_POSTFIX = ".dat";
    /**
     * prefix for catagorical/contextual sampling index files values
     * lists
     */
    static final String CATAGORY_LIST_PREFIX = "SAM_C_";
    /**
     * postfix for catagorical/contextual sampling index files values
     * lists
     */
    static final String CATAGORY_LIST_POSTFIX = ".csv";
    /**
     * destination of loaded occurances
     */
    ArrayList<String[]> occurances;

    /**
     * default constructor
     */
    public SamplingIndex() {
        TabulationSettings.load();
    }

    /**
     * performs update of 'indexing' for new points data
     */
    @Override
    public void occurancesUpdate() {

        //OccurrencesIndex.loadIndexes();

        TabulationSettings.load();
        OccurrencesIndex.getPointsPairs();

        /*
         * for grid files
         */
        //intersectGrid(null);

        /*
         * for shape files of catagorical layers,
         * shape files instead of grids
         */
        //intersectCatagories(null);

        /* threaded building, needs more ram than one at a time */
        int threadcount = TabulationSettings.analysis_threads;
        ArrayList<String> layers = new ArrayList();
        int i;
        for (i = 0; i < TabulationSettings.geo_tables.length; i++) {
            layers.add(TabulationSettings.geo_tables[i].name);
        }
        for (i = 0; i < TabulationSettings.environmental_data_files.length; i++) {
            layers.add(TabulationSettings.environmental_data_files[i].name);
        }

        LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue(layers);

        SamplingIndexThread[] it = new SamplingIndexThread[threadcount];

        for (i = 0; i < threadcount; i++) {
            it[i] = new SamplingIndexThread(lbq);
        }

        System.out.println("Start SamplingIndex.build_all (" + threadcount + " threads): " + System.currentTimeMillis());

        //wait until all done
        try {
            boolean alive = true;
            while (alive) {
                Thread.sleep(2000);
                alive = false;
                for (i = 0; i < threadcount; i++) {
                    if (it[i].isAlive()) {
                        alive = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("End SamplingIndex.build_all: " + System.currentTimeMillis());
    }

    /**
     * performs update of 'indexing' for a new layer (grid or shapefile)
     *
     * @param layername name of the layer to update as String.  To update
     * all layers use null.
     */
    @Override
    public void layersUpdate(String layername) {
        System.out.println("layersUpdate:" + layername);

        /*
         * for grid files
         */
        intersectGrid(layername);

        /*
         * for shape files of catagorical layers,
         * shape files instead of grids
         */
        intersectCatagories(layername);
    }

    /**
     * method to determine if the index is up to date
     *
     * @return true if index is up to date
     */
    @Override
    public boolean isUpdated() {
        return true;
    }

    /**
     * joins sorted points to GridFiles
     *
     * TODO: load groups of whole rasters at a time
     */
    void intersectGrid(String layername) {
        /* load points, sorted */
        double[][] points = null;

        int i;

        /* for each grid file intersect and export in points order */
        Layer layer;

        for (i = 0; i < TabulationSettings.environmental_data_files.length; i++) {
            if (layername != null
                    && !layername.equalsIgnoreCase(TabulationSettings.environmental_data_files[i].name)) {
                continue;
            }
            if (points == null) {
                points = OccurrencesIndex.getPointsPairs();
            }

            layer = TabulationSettings.environmental_data_files[i];
            try {
                Grid grid = new Grid(
                        TabulationSettings.environmental_data_path
                        + layer.name);

                float[] values = grid.getValues2(points);

                /* export values - RAF for writeDouble() */
                RandomAccessFile raf = new RandomAccessFile(
                        TabulationSettings.index_path
                        + "SAM_D_" + layer.name + ".dat", "rw");

                byte[] b = new byte[values.length * 4];
                ByteBuffer bb = ByteBuffer.wrap(b);

                for (i = 0; i < values.length; i++) {
                    bb.putFloat((float) values[i]);
                }

                raf.write(b);
                raf.close();

            } catch (Exception e) {
                SpatialLogger.log("intersectGrid writing", e.toString());
            }
        }

    }

    /**
     * join number for each catagory name with all points
     *
     * export
     */
    void intersectCatagories(String layername) {
        String tablename;
        String fieldname;

        /* load points in correct order */
        double[][] points = null;
        int i = 0;

        /*
         * TODO: fix assumption that only catagorical fields are specified
         * in the tables
         */
        for (Layer l : TabulationSettings.geo_tables) {
            if (layername != null
                    && !layername.equalsIgnoreCase(l.name)) {
                continue;
            }

            if (points == null) {
                points = OccurrencesIndex.getPointsPairs();

                SpatialLogger.log("intersectCatagories, points> " + points.length);
            }

            String query = "";
            String longitude = "";
            String latitude = "";
            i = 0;
            try {
                tablename = l.name;

                SimpleShapeFile ssf = new SimpleShapeFile(
                        TabulationSettings.environmental_data_path
                        + l.name);
                SpatialLogger.log("shapefile open: " + l.name);

                /* export catagories
                 * TODO: operate on more than one field and remove assumption
                 * that there is one
                 */
                fieldname = l.fields[0].name;
                String filename_catagories = TabulationSettings.index_path
                        + CATAGORY_LIST_PREFIX + l.name + "_" + fieldname
                        + CATAGORY_LIST_POSTFIX;

                FileWriter fw = new FileWriter(filename_catagories);
                int column_idx = ssf.getColumnIdx(fieldname);

                /* column not found, log and substitute first column */
                if (column_idx < 0) {
                    SpatialLogger.log("intersectCatagories, missing col:" + fieldname + " in " + l.name);
                }

                String[] catagories = ssf.getColumnLookup(column_idx);
                for (i = 0; i < catagories.length; i++) {
                    fw.append(catagories[i]);
                    fw.append("\n");
                }
                fw.close();

                /* export file */
                RandomAccessFile raf = new RandomAccessFile(
                        TabulationSettings.index_path
                        + CATAGORICAL_PREFIX + tablename + VALUE_POSTFIX, "rw");

                //repeat for each point
                SpatialLogger.log("intersectCatagories, begin intersect: " + points.length);

                int[] values = ssf.intersect(points, catagories, column_idx);

                //save ssf
                ssf.saveRegion(TabulationSettings.index_path + l.name, column_idx);

                byte[] b = new byte[values.length * 2];
                ByteBuffer bb = ByteBuffer.wrap(b);

                for (i = 0; i < values.length; i++) {
                    bb.putShort((short) values[i]);
                }

                raf.write(b);
                raf.close();

                SpatialLogger.log("shapefile done: " + l.name);
            } catch (Exception e) {
                SpatialLogger.log("intersectCatagories",
                        e.toString() + "\r\n>query=" + query + "\r\n>i=" + i
                        + "\r\n>longitude, latitude=" + longitude + "," + latitude);
                e.printStackTrace();
            }
        }
    }

    /**
     * gets sampling intersection records for a layer between two records
     *
     * TODO: read properly - needs a change to the write functions as well
     *
     * @param layer layer name as String
     * @param record_start first record to read
     * @param record_end last record to read
     * @return crecords as String []
     */
    public static String[] getRecords(String layer_name, int record_start, int record_end) {
        /*
         * gridded data is 4byte double
         * catagorical data is 4byte int
         */
        try {
            int i;

            /* make filenames */
            String filenameD = TabulationSettings.index_path
                    + "SAM_D_" + layer_name + ".dat";
            String filenameI = TabulationSettings.index_path
                    + CATAGORICAL_PREFIX + layer_name + VALUE_POSTFIX;

            String[] output = new String[record_end - record_start + 1];
            int p = 0;

            String[] lookup_values = SamplingIndex.getLayerCatagories(
                    Layers.getLayer(layer_name));
            //System.out.println("lookupvalues=" + lookup_values);

            if ((new File(filenameD)).exists()) {
                //System.out.println("D file found");
                /* if continous file name sampling file exists, get values from it */
                RandomAccessFile raf = new RandomAccessFile(filenameD, "r");
                raf.seek(record_start * 4);
                float f;
                for (i = record_start; i <= record_end; i++) {
                    f = raf.readFloat();
                    if (Float.isNaN(f)) {
                        output[p++] = "";
                    } else {
                        output[p++] = String.valueOf(f);
                    }
                }
                raf.close();
            } else if ((new File(filenameI)).exists()) {
                //System.out.println("I file found");
                /* if continous file name sampling file exists, get values from it */
                RandomAccessFile raf = new RandomAccessFile(filenameI, "r");
                raf.seek(record_start * 2);
                short v;
                for (i = record_start; i <= record_end; i++) {
                    v = raf.readShort();
                    if (v >= 0 && v < lookup_values.length) {
                        output[p++] = lookup_values[v];
                    } else {
                        output[p++] = "";
                    }
                }
                raf.close();
            }

            return output;
        } catch (Exception e) {
            SpatialLogger.log("getRecords", e.toString());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * gets sampling intersection records for a layer from a list
     * of records
     *
     * TODO: read properly - needs a change to the write functions as well
     *
     * @param layer layer name as String
     * @param records array of records as int [] to read
     * @return records as String []
     */
    public static String[] getRecords(String layer_name, int[] records) {
        /*
         * gridded data is 4byte float
         * catagorical data is 4byte int
         */
        try {

            String filenameD = TabulationSettings.index_path
                    + "SAM_D_" + layer_name + ".dat";
            String filenameI = TabulationSettings.index_path
                    + CATAGORICAL_PREFIX + layer_name + VALUE_POSTFIX;

            ArrayList<String> output = new ArrayList<String>(records.length);

            if ((new File(filenameD)).exists()) {
                /* if continous file name sampling file exists, get values from it */
                RandomAccessFile raf = new RandomAccessFile(filenameD, "r");
                for (int k = 0; k < records.length; k++) {
                    raf.seek(records[k] * 4);
                    float f;

                    f = raf.readFloat();
                    if (Float.isNaN(f)) {
                        output.add("");
                    } else {
                        output.add(String.valueOf(f));
                    }
                }
                raf.close();
            } else if ((new File(filenameI)).exists()) {
                String[] lookup_values = SamplingIndex.getLayerCatagories(
                        Layers.getLayer(layer_name));

                /* if continous file name sampling file exists, get values from it */
                RandomAccessFile raf = new RandomAccessFile(filenameI, "r");
                for (int k = 0; k < records.length; k++) {
                    raf.seek(records[k] * 2);
                    short v = raf.readShort();
                    if (v >= 0 && v < lookup_values.length) {
                        output.add(lookup_values[v]);
                    } else {
                        output.add("");
                    }
                }
                raf.close();
            }

            if (output.size() > 0) {
                String str[] = new String[output.size()];
                output.toArray(str);
                return str;
            }
        } catch (Exception e) {
            SpatialLogger.log("getRecords", e.toString());
        }

        return null;
    }

    /**
     * gets all values in a layer
     *
     * @param layer
     * @return
     */
    static public String[] getLayerCatagories(Layer layer) {
        /* test for valid layer input */
        if (layer == null || layer.fields == null || layer.fields.length < 1) {
            return null;
        }
        File catagories_file = new File(
                TabulationSettings.index_path
                + SamplingIndex.CATAGORY_LIST_PREFIX
                + layer.name + "_" + layer.fields[0].name
                + SamplingIndex.CATAGORY_LIST_POSTFIX);

        /* confirm layer catagories file created */
        if (catagories_file.exists()) {
            try {
                /* load file */
                byte[] data = new byte[(int) catagories_file.length()];
                FileInputStream fis = new FileInputStream(catagories_file);
                fis.read(data);
                fis.close();

                /* convert to string */
                String str = new String(data);

                /* split by new line */
                String[] lines = str.split("\n");
                return lines;
            } catch (Exception e) {
                SpatialLogger.log("getLayerExtents(" + layer.name + "), catagorical",
                        e.toString());
            }
        }
        return null;
    }
}

class SamplingIndexThread implements Runnable {

    Thread t;
    LinkedBlockingQueue<String> lbq;
    int step;
    int[] target;

    public SamplingIndexThread(LinkedBlockingQueue<String> lbq_) {
        t = new Thread(this);
        lbq = lbq_;
        t.start();
    }

    public void run() {

        int i, idx;
        int hits = 0;

        /* get next batch */
        String next;
        try {
            synchronized (lbq) {
                if (lbq.size() > 0) {
                    next = lbq.take();
                } else {
                    next = null;
                }
            }

            System.out.println("A*: " + next);

            SamplingIndex si = new SamplingIndex();

            while (next != null) {
                /* update for this layer */
                si.layersUpdate(next);

                /* report */
                System.out.println("D*: " + next);

                /* get next available */
                synchronized (lbq) {
                    if (lbq.size() > 0) {
                        next = lbq.take();
                    } else {
                        next = null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAlive() {
        return t.isAlive();
    }
}
