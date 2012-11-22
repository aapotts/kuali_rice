/**
 * Copyright 2005-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sampleu.travel.approval;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.util.KRADPropertyConstants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.LinkedHashMap;

@Entity
@Table(name="TRVL_TRANS_MD_DTL_T")
public class TransportationModeDetail extends PersistableBusinessObjectBase {

    private String documentNumber;
    private String transportationModeCode;
    private TransportationMode transportationMode;
    private TravelAuthorizationDocument travelAuthorizationDocument;


    /**
     *
     * This method returns the document number this TransportationModeDetail object is associated with
     * @return document number
     */
    @Column(name="doc_nbr")
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     *
     * This method sets the document number this TransportationModeDetail object will be associated with
     * @param documentNumber
     */
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    @Column(name="TRANS_MODE_CD",length=3, nullable=false)
    public String getTransportationModeCode() {
        return transportationModeCode;
    }


    public void setTransportationModeCode(String transportationModeCode) {
        this.transportationModeCode = transportationModeCode;
    }

    @ManyToOne
    @JoinColumn(name="TRANS_MODE_CD")
    public TransportationMode getTransportationMode() {
        return transportationMode;
    }


    public void setTransportationMode(TransportationMode transportationMode) {
        this.transportationMode = transportationMode;
    }

    public TravelAuthorizationDocument getTravelAuthorizationDocument() {
        return travelAuthorizationDocument;
    }


    public void setTravelAuthorizationDocument(TravelAuthorizationDocument travelAuthorizationDocument) {
        this.travelAuthorizationDocument = travelAuthorizationDocument;
    }


    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put(KRADPropertyConstants.DOCUMENT_NUMBER, this.documentNumber);
        m.put("transportationModeCode", this.transportationModeCode);
        return m;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        TransportationModeDetail detail = (TransportationModeDetail)obj;

        if(this.transportationModeCode.equals(detail.getTransportationModeCode()) && this.documentNumber.equals(detail.getDocumentNumber())) {
            return true;
        }
        return false;
    }

}
