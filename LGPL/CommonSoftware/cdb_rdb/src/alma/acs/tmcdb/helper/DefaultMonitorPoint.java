package alma.acs.tmcdb;
// Generated Jun 5, 2017 7:15:51 PM by Hibernate Tools 4.3.1.Final


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * DefaultMonitorPoint generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name="`DEFAULTMONITORPOINT`"
)
public class DefaultMonitorPoint extends alma.acs.tmcdb.translator.TmcdbObject implements java.io.Serializable {


     protected Integer defaultMonitorPointId;
     protected DefaultBaciProperty defaultBaciProperty;
     protected String monitorPointName;
     protected Integer indice;
     protected String dataType;
     protected String RCA;
     protected Boolean teRelated;
     protected String rawDataType;
     protected String worldDataType;
     protected String units;
     protected Double scale;
     protected Double offset;
     protected String minRange;
     protected String maxRange;
     protected String description;

    public DefaultMonitorPoint() {
    }
   
       @Id 

    
    @Column(name="`DEFAULTMONITORPOINTID`", unique=true, nullable=false)
    public Integer getDefaultMonitorPointId() {
        return this.defaultMonitorPointId;
    }
    
    public void setDefaultMonitorPointId(Integer defaultMonitorPointId) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("defaultMonitorPointId", this.defaultMonitorPointId, this.defaultMonitorPointId = defaultMonitorPointId);
        else
            this.defaultMonitorPointId = defaultMonitorPointId;
    }


@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="`DEFAULTBACIPROPERTYID`", nullable=false)
    public DefaultBaciProperty getDefaultBaciProperty() {
        return this.defaultBaciProperty;
    }
    
    public void setDefaultBaciProperty(DefaultBaciProperty defaultBaciProperty) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("defaultBaciProperty", this.defaultBaciProperty, this.defaultBaciProperty = defaultBaciProperty);
        else
            this.defaultBaciProperty = defaultBaciProperty;
    }


    
    @Column(name="`MONITORPOINTNAME`", nullable=false, length=128)
    public String getMonitorPointName() {
        return this.monitorPointName;
    }
    
    public void setMonitorPointName(String monitorPointName) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("monitorPointName", this.monitorPointName, this.monitorPointName = monitorPointName);
        else
            this.monitorPointName = monitorPointName;
    }


    
    @Column(name="`INDICE`", nullable=false)
    public Integer getIndice() {
        return this.indice;
    }
    
    public void setIndice(Integer indice) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("indice", this.indice, this.indice = indice);
        else
            this.indice = indice;
    }


    
    @Column(name="`DATATYPE`", nullable=false, length=16777216)
    public String getDataType() {
        return this.dataType;
    }
    
    public void setDataType(String dataType) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("dataType", this.dataType, this.dataType = dataType);
        else
            this.dataType = dataType;
    }


    
    @Column(name="`RCA`", nullable=false, length=16777216)
    public String getRCA() {
        return this.RCA;
    }
    
    public void setRCA(String RCA) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("RCA", this.RCA, this.RCA = RCA);
        else
            this.RCA = RCA;
    }


    
    @Column(name="`TERELATED`", nullable=false)
    public Boolean getTeRelated() {
        return this.teRelated;
    }
    
    public void setTeRelated(Boolean teRelated) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("teRelated", this.teRelated, this.teRelated = teRelated);
        else
            this.teRelated = teRelated;
    }


    
    @Column(name="`RAWDATATYPE`", nullable=false, length=16777216)
    public String getRawDataType() {
        return this.rawDataType;
    }
    
    public void setRawDataType(String rawDataType) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("rawDataType", this.rawDataType, this.rawDataType = rawDataType);
        else
            this.rawDataType = rawDataType;
    }


    
    @Column(name="`WORLDDATATYPE`", nullable=false, length=16777216)
    public String getWorldDataType() {
        return this.worldDataType;
    }
    
    public void setWorldDataType(String worldDataType) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("worldDataType", this.worldDataType, this.worldDataType = worldDataType);
        else
            this.worldDataType = worldDataType;
    }


    
    @Column(name="`UNITS`", length=16777216)
    public String getUnits() {
        return this.units;
    }
    
    public void setUnits(String units) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("units", this.units, this.units = units);
        else
            this.units = units;
    }


    
    @Column(name="`SCALE`", precision=64, scale=0)
    public Double getScale() {
        return this.scale;
    }
    
    public void setScale(Double scale) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("scale", this.scale, this.scale = scale);
        else
            this.scale = scale;
    }


    
    @Column(name="`OFFSET`", precision=64, scale=0)
    public Double getOffset() {
        return this.offset;
    }
    
    public void setOffset(Double offset) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("offset", this.offset, this.offset = offset);
        else
            this.offset = offset;
    }


    
    @Column(name="`MINRANGE`", length=16777216)
    public String getMinRange() {
        return this.minRange;
    }
    
    public void setMinRange(String minRange) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("minRange", this.minRange, this.minRange = minRange);
        else
            this.minRange = minRange;
    }


    
    @Column(name="`MAXRANGE`", length=16777216)
    public String getMaxRange() {
        return this.maxRange;
    }
    
    public void setMaxRange(String maxRange) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("maxRange", this.maxRange, this.maxRange = maxRange);
        else
            this.maxRange = maxRange;
    }


    
    @Column(name="`DESCRIPTION`", nullable=false, length=16777216)
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("description", this.description, this.description = description);
        else
            this.description = description;
    }





}


