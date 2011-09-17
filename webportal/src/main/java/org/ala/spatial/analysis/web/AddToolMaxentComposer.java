/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ala.spatial.analysis.web;

import au.com.bytecode.opencsv.CSVReader;
import au.org.emii.portal.menu.MapLayer;
import au.org.emii.portal.menu.MapLayerMetadata;
import au.org.emii.portal.util.LayerUtilities;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ala.spatial.data.Facet;
import org.ala.spatial.data.Query;
import org.ala.spatial.util.CommonData;
import org.ala.spatial.data.QueryField;
import org.ala.spatial.data.SolrQuery;
import org.ala.spatial.data.UploadQuery;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Textbox;

/**
 *
 * @author ajay
 */
public class AddToolMaxentComposer extends AddToolComposer {

    int generation_count = 1;
    private Checkbox chkJackknife;
    private Checkbox chkRCurves;
    private Textbox txtTestPercentage;
    private String taxon = "";

    @Override
    public void afterCompose() {
        super.afterCompose();

        this.selectedMethod = "Prediction";
        this.totalSteps = 5;

        this.loadAreaLayers();
        this.loadSpeciesLayers();
        this.loadGridLayers(true, true);
        this.updateWindowTitle();

    }

    @Override
    public void onLastPanel() {
        super.onLastPanel();
        //this.updateName("My Prediction model for " + rgSpecies.getSelectedItem().getLabel());
        this.updateName(getMapComposer().getNextAreaLayerName("My Prediction"));

    }

    @Override
    public void onFinish() {
        //super.onFinish();

        if (searchSpeciesAuto.getSelectedItem() != null) {
            getMapComposer().mapSpeciesFromAutocomplete(searchSpeciesAuto, getSelectedArea());
        }

        runmaxent();
        lbListLayers.clearSelection();
        //this.detach();

    }

    public void runmaxent() {
        try {
            Query query = getSelectedSpecies().newWkt(getSelectedArea());
            String sbenvsel = getSelectedLayers();
            //String area = getSelectedArea();
            //String taxonlsid = taxon;
            if (searchSpeciesAuto.getSelectedItem() == null
                    || searchSpeciesAuto.getSelectedItem().getValue() == null) {
                //MapLayer ml = getMapComposer().getMapLayerSpeciesLSID(taxon);
                // taxonlsid = ml.getMapLayerMetadata().getSpeciesDisplayLsid();
            }

//            if (isSensitiveSpecies(taxon)) {
//                return;
//            }

            System.out.println("Selected species: " + taxon);
            System.out.println("Selected env vars");
            System.out.println(sbenvsel.toString());
            System.out.println("Selected options: ");
            System.out.println("Jackknife: " + chkJackknife.isChecked());
            System.out.println("Response curves: " + chkRCurves.isChecked());
            System.out.println("Test per: " + txtTestPercentage.getValue());

            StringBuffer sbProcessUrl = new StringBuffer();
            sbProcessUrl.append(CommonData.satServer + "/ws/maxent/processgeoq?");
//            sbProcessUrl.append("taxonid=" + URLEncoder.encode(taxon.replace(".", "__"), "UTF-8"));
//            sbProcessUrl.append("&taxonlsid=" + URLEncoder.encode(taxonlsid.replace(".", "__"), "UTF-8"));
            sbProcessUrl.append("&envlist=" + URLEncoder.encode(sbenvsel.toString(), "UTF-8"));
            if (chkJackknife.isChecked()) {
                sbProcessUrl.append("&chkJackknife=on");
            }
            if (chkRCurves.isChecked()) {
                sbProcessUrl.append("&chkResponseCurves=on");
            }
            sbProcessUrl.append("&txtTestPercentage=" + txtTestPercentage.getValue());

            // System.out.println("Calling Maxent: " + sbProcessUrl.toString() + "\narea: " + area);

            HttpClient client = new HttpClient();
            PostMethod get = new PostMethod(sbProcessUrl.toString());
            get.addParameter("area", getSelectedArea());
            String[] speciesData = getSpeciesData(query);
            get.addParameter("species", speciesData[0]);
            get.addParameter("removedspecies", speciesData[1]);
            get.addRequestHeader("Accept", "text/plain");

            int result = client.executeMethod(get);
            pid = get.getResponseBodyAsString();
            this.taxon = taxon;

            openProgressBar();

            StringBuffer sbParams = new StringBuffer();
            sbParams.append("Species: " + taxon);
            sbParams.append(";Jackknife: " + chkJackknife.isChecked());
            sbParams.append(";Response curves: " + chkRCurves.isChecked());
            sbParams.append(";Test per: " + txtTestPercentage.getValue());

            Map attrs = new HashMap();
            attrs.put("actionby", "user");
            attrs.put("actiontype", "analysis");
            //attrs.put("lsid", taxonlsid);
            attrs.put("useremail", "spatialuser");
            attrs.put("processid", pid);
            attrs.put("sessionid", "");
            attrs.put("layers", sbenvsel.toString());
            attrs.put("method", "maxent");
            attrs.put("params", sbParams.toString());
            attrs.put("downloadfile", "");
            getMapComposer().updateUserLog(attrs, "analysis result: " + CommonData.satServer + "/output/maxent/" + pid + "/species.html");
        } catch (Exception e) {
            System.out.println("Maxent error: ");
            e.printStackTrace(System.out);
        }
    }

    void openProgressBar() {
        MaxentProgressWCController window = (MaxentProgressWCController) Executions.createComponents("WEB-INF/zul/AnalysisMaxentProgress.zul", this, null);
        window.parent = this;
        window.start(pid);
        try {
            window.doModal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMap(Event event) {

        String mapurl = CommonData.geoServer + "/wms?service=WMS&version=1.1.0&request=GetMap&layers=ALA:species_" + pid + "&styles=alastyles&FORMAT=image%2Fpng";

        String legendurl = CommonData.geoServer
                + "/wms?REQUEST=GetLegendGraphic&VERSION=1.0.0&FORMAT=image/png&WIDTH=10&HEIGHT=20"
                + "&LAYER=ALA:species_" + pid
                + "&STYLE=alastyles";

        System.out.println(legendurl);

        //get job inputs
        String speciesName = "";
        try {
            for (String s : getJob("inputs").split(";")) {
                if (s.startsWith("scientificName")) {
                    speciesName = s.split(":")[1];
                    if (speciesName != null && speciesName.length() > 1) {
                        speciesName = speciesName.substring(0, 1).toUpperCase() + speciesName.substring(1);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (taxon == null) {
            taxon = "species";
        }

        String layername = tToolName.getValue();
        getMapComposer().addWMSLayer(layername, mapurl, (float) 0.5, null, legendurl, LayerUtilities.MAXENT, null, null);
        MapLayer ml = getMapComposer().getMapLayer(layername);
        String infoUrl = CommonData.satServer  + "/output/maxent/" + pid + "/species.html";
        MapLayerMetadata md = ml.getMapLayerMetadata();
        if (md == null) {
            md = new MapLayerMetadata();
            ml.setMapLayerMetadata(md);
        }
        md.setMoreInfo(infoUrl + "\nMaxent Output\npid:" + pid);
        md.setId(Long.valueOf(pid));

        try {
            // set off the download as well
            String fileUrl = CommonData.satServer + "/ws/download/" + pid;
            Filedownload.save(new URL(fileUrl).openStream(), "application/zip", tToolName.getValue().replaceAll(" ", "_") + ".zip"); // "ALA_Prediction_"+pid+".zip"
        } catch (Exception ex) {
            System.out.println("Error generating download for prediction model:");
            ex.printStackTrace(System.out);
        }

        this.detach();

        //getMapComposer().showMessage("Reference number to retrieve results: " + pid);

        //showInfoWindow("/output/maxent/" + pid + "/species.html");
    }

    String getJob(String type) {
        try {
            StringBuffer sbProcessUrl = new StringBuffer();
            sbProcessUrl.append(CommonData.satServer + "/ws/jobs/").append(type).append("?pid=").append(pid);

            System.out.println(sbProcessUrl.toString());
            HttpClient client = new HttpClient();
            GetMethod get = new GetMethod(sbProcessUrl.toString());

            get.addRequestHeader("Accept", "text/plain");

            int result = client.executeMethod(get);
            String slist = get.getResponseBodyAsString();
            System.out.println(slist);
            return slist;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * get CSV of speciesName, longitude, latitude in [0] and
     *
     * @param selectedSpecies
     * @param area
     * @return
     */
    private String[] getSpeciesData(Query query) {
        String sensitiveSpeciesRaw = query.newFacet(new Facet("sensitive:[* TO *]")).speciesList();
        List<String[]> sensitiveSpecies = null;
        try {
            sensitiveSpecies = new CSVReader(new StringReader(sensitiveSpeciesRaw)).readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        TreeSet<String> sensitiveSpeciesFound = new TreeSet<String>();

        ArrayList<QueryField> fields = new ArrayList<QueryField>();
        String lsidFieldName = query.getSpeciesIdFieldName();
        QueryField qf = null;
        if(lsidFieldName != null) {
            qf = new QueryField(query.getSpeciesIdFieldName());
            qf.setStored(true);
            fields.add(qf);
        }
        double[] points = query.getPoints(fields);
        StringBuilder sb = null;
        if (points != null) {
            sb = new StringBuilder();
            for (int i = 0; i < points.length; i += 2) {
                boolean isSensitive = false;
                if(qf != null && !(query instanceof UploadQuery)) {
                    String lsid = qf.getAsString(i / 2);
                    if (sensitiveSpeciesRaw.contains("\"" + lsid + "\"")
                            || sensitiveSpeciesRaw.contains("|" + lsid + "|")) {
                        //find sensitive record
                        for (int j = 0; j < sensitiveSpecies.size(); j++) {
                            if (sensitiveSpecies.get(j)[4].equals(lsid)) {
                                //append as lsid,name,rank
                                sensitiveSpeciesFound.add(sensitiveSpecies.get(j)[4]
                                        + "," + sensitiveSpecies.get(j)[1]
                                        + "," + sensitiveSpecies.get(j)[3]);
                                isSensitive = true;
                                break;
                            }
                        }
                    }
                }
                if (!isSensitive) {
                    if (sb.length() == 0) {
                        //header
                        sb.append("species,longitude,latitude");
                    }
                    sb.append("\nspecies,").append(points[i]).append(",").append(points[i + 1]);
                }
            }
        }

        //collate sensitive species found, no header
        StringBuilder sen = new StringBuilder();
        for (String s : sensitiveSpeciesFound) {
            sb.append(s).append("\n");
        }

        String[] out = {((sb == null) ? null : sb.toString()), (sen.length() == 0) ? null : sen.toString()};
        return out;
    }
}