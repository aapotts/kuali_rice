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
package org.kuali.rice.krad.datadictionary;

import org.kuali.rice.krad.datadictionary.exception.AttributeValidationException;
import org.kuali.rice.krad.datadictionary.mask.MaskFormatter;
import org.kuali.rice.krad.datadictionary.validator.ErrorReport;
import org.kuali.rice.krad.datadictionary.validator.TracerToken;

import java.util.ArrayList;

/**
 * Defines a set of restrictions that are possible on an attribute
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class AttributeSecurity extends DataDictionaryDefinitionBase {
	private static final long serialVersionUID = -7923499408946975318L;
	
	private boolean readOnly = false;
	private boolean hide = false;
	private boolean mask = false;
	private boolean partialMask = false;

	private MaskFormatter partialMaskFormatter;
	private MaskFormatter maskFormatter;

	/**
	 * @return the readOnly
	 */
	public boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * @param readOnly
	 *            the readOnly to set
	 */
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/**
	 * @return the hide
	 */
	public boolean isHide() {
		return this.hide;
	}

	/**
	 * @param hide
	 *            the hide to set
	 */
	public void setHide(boolean hide) {
		this.hide = hide;
	}

	/**
	 * @return the mask
	 */
	public boolean isMask() {
		return this.mask;
	}

	/**
	 * @param mask
	 *            the mask to set
	 */
	public void setMask(boolean mask) {
		this.mask = mask;
	}

	/**
	 * @return the partialMask
	 */
	public boolean isPartialMask() {
		return this.partialMask;
	}

	/**
	 * @param partialMask
	 *            the partialMask to set
	 */
	public void setPartialMask(boolean partialMask) {
		this.partialMask = partialMask;
	}

	/**
	 * @return the maskFormatter
	 */
	public MaskFormatter getMaskFormatter() {
		return this.maskFormatter;
	}

	/**
	 * @param maskFormatter
	 *            the maskFormatter to set
	 */
	public void setMaskFormatter(MaskFormatter maskFormatter) {
		this.maskFormatter = maskFormatter;
	}

	/**
	 * @return the partialMaskFormatter
	 */
	public MaskFormatter getPartialMaskFormatter() {
		return this.partialMaskFormatter;
	}

	/**
	 * @param partialMaskFormatter
	 *            the partialMaskFormatter to set
	 */
	public void setPartialMaskFormatter(MaskFormatter partialMaskFormatter) {
		this.partialMaskFormatter = partialMaskFormatter;
	}

	/**
	 * This overridden method ...
	 * 
	 * @see org.kuali.rice.krad.datadictionary.DataDictionaryDefinition#completeValidation(java.lang.Class,
	 *      java.lang.Class)
	 */
	public void completeValidation(Class rootBusinessObjectClass,
			Class otherBusinessObjectClass) {

		if (mask && maskFormatter == null) {
			throw new AttributeValidationException("MaskFormatter is required");
		}
		if (partialMask && partialMaskFormatter == null) {
			throw new AttributeValidationException(
					"PartialMaskFormatter is required");
		}
	}

    /**
     * Directly validate simple fields
     *
     * @see org.kuali.rice.krad.datadictionary.DataDictionaryEntry#completeValidation(TracerToken)
     */
    public ArrayList<ErrorReport> completeValidation(Class rootBusinessObjectClass, Class otherBusinessObjectClass, TracerToken tracer) {
        ArrayList<ErrorReport> reports = new ArrayList<ErrorReport>();
        tracer.addBean(this.getClass().getSimpleName(),TracerToken.NO_BEAN_ID);

        if (mask && maskFormatter == null) {
            ErrorReport error = ErrorReport.createError("MaskFormatter is required",tracer);
            error.addCurrentValue("mask = "+mask);
            error.addCurrentValue("maskFormatter = "+maskFormatter);
            reports.add(error);
        }
        if (partialMask && partialMaskFormatter == null) {
            ErrorReport error = ErrorReport.createError("PartialMaskFormatter is required",tracer);
            error.addCurrentValue("partialMask = "+partialMask);
            error.addCurrentValue("partialMaskFormatter = "+partialMaskFormatter);
            reports.add(error);
        }

        return reports;
    }


	/**
	 * Returns whether any of the restrictions defined in this class are true.
	 */
	public boolean hasAnyRestriction() {
		return readOnly || mask || partialMask || hide;
	}
	
	
	/**
	 * Returns whether any of the restrictions defined in this class indicate that the attribute value potentially needs
	 * to be not shown to the user (i.e. masked, partial mask, hide).  Note that readonly does not fall in this category.
	 * 
	 * @return
	 */
	public boolean hasRestrictionThatRemovesValueFromUI() {
		return mask || partialMask || hide;	
	}
}
