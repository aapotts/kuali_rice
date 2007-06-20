/*
 * Copyright 2005-2006 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
CREATE TABLE SH_LOCK_TYP_DESC_T(
        TRN_SMPHR_TYP_CD               VARCHAR2(10) CONSTRAINT SH_LOCK_TYP_DESC_TN1 NOT NULL,
        OBJ_ID                         VARCHAR2(36) DEFAULT SYS_GUID() CONSTRAINT SH_LOCK_TYP_DESC_TN2 NOT NULL,
        VER_NBR                        NUMBER(8) DEFAULT 1 CONSTRAINT SH_LOCK_TYP_DESC_TN3 NOT NULL,
        PRSN_UPDTABLTY_NBR             NUMBER(12),
        TRN_SMPHR_DESC                 VARCHAR2(60),
     CONSTRAINT SH_LOCK_TYP_DESC_TP1 PRIMARY KEY (
        TRN_SMPHR_TYP_CD) USING INDEX TABLESPACE KUL_IDX03,
     CONSTRAINT SH_LOCK_TYP_DESC_TC0 UNIQUE (OBJ_ID) USING INDEX TABLESPACE KUL_IDX03
)
TABLESPACE KUL03
/
