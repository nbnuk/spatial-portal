/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package au.org.ala.spatial.util;

import au.com.bytecode.opencsv.CSVReader;
import au.org.ala.spatial.StringConstants;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam
 */
public class LsidCounts {
    private static final Logger LOGGER = Logger.getLogger(LsidCounts.class);

    private Long[] lft = new Long[0];
    private Long[] count  = new Long[0];

    public LsidCounts() {
//        HttpClient client = new HttpClient();
//
//        CSVReader csv = null;
//        String url = null;
//        try {
//            url = CommonData.getBiocacheServer()
//                    + "/mapping/legend?cm=lft&q="
//                    + URLEncoder.encode("geospatial_kosher:*", StringConstants.UTF_8)
//                    + CommonData.getBiocacheQc();
//            LOGGER.debug(url);
//            GetMethod get = new GetMethod(url);
//            Map<Long, Long> map = new HashMap<Long, Long>();
//            client.getHttpConnectionManager().getParams().setSoTimeout(30000);
//
//            client.executeMethod(get);
//
//            csv = new CSVReader(new InputStreamReader(get.getResponseBodyAsStream()));
//
//            String[] row;
//            while ((row = csv.readNext()) != null) {
//                //parse name and count
//                try {
//                    long name = Long.parseLong(row[0]);
//                    long c = Long.parseLong(row[4]);
//                    map.put(name, c);
//                } catch (NumberFormatException e) {
//                    //ignore errors. Expect one for 'unknown' and first row 'name'
//                }
//            }
//
//            //sort keys
//            lft = new Long[map.size()];
//            map.keySet().toArray(lft);
//            java.util.Arrays.sort(lft);
//
//            //get sorted values
//            count = new Long[map.size()];
//            for (int i = 0; i < lft.length; i++) {
//                count[i] = map.get(lft[i]);
//            }
//        } catch (Exception e) {
//            LOGGER.error("error getting LSID count for : " + url, e);
//        } finally {
//            try {
//                if (csv != null) {
//                    csv.close();
//                }
//            } catch (Exception e) {
//                LOGGER.error("failed to close csv", e);
//            }
//        }
    }

    public long getCount(long left, long right) {
        if (lft == null) {
            return 0;
        }
        int pos = java.util.Arrays.binarySearch(lft, left);
        if (pos < 0) {
            pos = -1 * pos - 1;
        }

        long sum = 0;
        while (lft[pos] < right) {
            sum += count[pos++];
        }
        return sum;
    }

    public int getSize() {
        return (lft == null) ? 0 : lft.length;
    }
}
