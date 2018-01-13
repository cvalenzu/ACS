package alma.acs.tmcdb;
// Generated Jun 5, 2017 7:15:51 PM by Hibernate Tools 4.3.1.Final


import alma.hibernate.util.StringEnumUserType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

/**
 * AcaCorrSet generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@Table(name="`ACACORRSET`"
)
@TypeDef(name="AcaCSetBBEnum", typeClass=StringEnumUserType.class,
   parameters={ @Parameter(name="enumClassName", value="alma.acs.tmcdb.AcaCSetBBEnum") })
public class AcaCorrSet extends alma.acs.tmcdb.translator.TmcdbObject implements java.io.Serializable {


     protected Integer baseElementId;
     protected BaseElement baseElement;
     protected AcaCSetBBEnum baseBand;
     protected String IP;

    public AcaCorrSet() {
    }
   
       @GenericGenerator(name="generator", strategy="foreign", parameters=@Parameter(name="property", value="baseElement"))@Id @GeneratedValue(generator="generator")

    
    @Column(name="`BASEELEMENTID`", unique=true, nullable=false)
    public Integer getBaseElementId() {
        return this.baseElementId;
    }
    
    public void setBaseElementId(Integer baseElementId) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("baseElementId", this.baseElementId, this.baseElementId = baseElementId);
        else
            this.baseElementId = baseElementId;
    }


@OneToOne(fetch=FetchType.LAZY)@PrimaryKeyJoinColumn
    public BaseElement getBaseElement() {
        return this.baseElement;
    }
    
    public void setBaseElement(BaseElement baseElement) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("baseElement", this.baseElement, this.baseElement = baseElement);
        else
            this.baseElement = baseElement;
    }


    
    @Column(name="`BASEBAND`", nullable=false, length=128)
	@Type(type="AcaCSetBBEnum")
    public AcaCSetBBEnum getBaseBand() {
        return this.baseBand;
    }
    
    public void setBaseBand(AcaCSetBBEnum baseBand) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("baseBand", this.baseBand, this.baseBand = baseBand);
        else
            this.baseBand = baseBand;
    }


    
    @Column(name="`IP`", nullable=false, length=128)
    public String getIP() {
        return this.IP;
    }
    
    public void setIP(String IP) {    
        if( propertyChangeSupport != null )
            propertyChangeSupport.firePropertyChange("IP", this.IP, this.IP = IP);
        else
            this.IP = IP;
    }





}

