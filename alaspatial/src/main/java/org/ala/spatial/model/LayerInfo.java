package org.ala.spatial.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * This class serves as a model object for a list of layers
 * served by the ALA Spatial Portal
 *
 * @author ajay
 */

@Entity
@Table(name = "layers")
public class LayerInfo {
    @Id
    @GeneratedValue ( strategy = GenerationType.SEQUENCE, generator="layers_id_seq")
    @SequenceGenerator(name = "layers_id_seq", sequenceName = "layers_id_seq")
    @Column(name = "id", insertable = false, updatable = false)
    private long id;

    @Column(name="name")
    private String name;

    @Column(name="displayname")
    private String displayname;

    @Column(name="description")
    private String description;

    @Column(name="type")
    private String type;

    @Column(name="source")
    private String source;

    @Column(name="path")
    private String path;

    @Column(name="displaypath")
    private String displaypath;

    @Column(name="scale")
    private String scale;

    @Column(name="extents")
    private String extent;

    @Column(name="minlatitude")
    private double minlatitude;

    @Column(name="minlongitude")
    private double minlongitude;

    @Column(name="maxlatitude")
    private double maxlatitude;

    @Column(name="maxlongitude")
    private double maxlongitude;

    @Column(name="notes")
    private String notes;

    @Column(name="enabled")
    private boolean enabled;

    public LayerInfo() {
    }

    public LayerInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExtent() {
        return extent;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getMaxlatitude() {
        return maxlatitude;
    }

    public void setMaxlatitude(double maxlatitude) {
        this.maxlatitude = maxlatitude;
    }

    public double getMaxlongitude() {
        return maxlongitude;
    }

    public void setMaxlongitude(double maxlongitude) {
        this.maxlongitude = maxlongitude;
    }

    public double getMinlatitude() {
        return minlatitude;
    }

    public void setMinlatitude(double minlatitude) {
        this.minlatitude = minlatitude;
    }

    public double getMinlongitude() {
        return minlongitude;
    }

    public void setMinlongitude(double minlongitude) {
        this.minlongitude = minlongitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDisplaypath() {
        return displaypath;
    }

    public void setDisplaypath(String displaypath) {
        this.displaypath = displaypath;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScale() {
        return scale;
    }

    public void setScale(String scale) {
        this.scale = scale;
    }

}
