ALTER TABLE FS_UNIVERSAL_USR_T
ADD (CONSTRAINT FS_UNIVERSAL_USR_TR1 FOREIGN KEY (
      CAMPUS_CD )
REFERENCES SH_CAMPUS_T (
      CAMPUS_CD ))
ADD (CONSTRAINT FS_UNIVERSAL_USR_TR2 FOREIGN KEY (
      EMP_STAT_CD )
REFERENCES SH_EMP_STAT_T (
      EMP_STAT_CD ))      
ADD (CONSTRAINT FS_UNIVERSAL_USR_TR3 FOREIGN KEY (
      EMP_TYPE_CD )
REFERENCES SH_EMP_TYP_T (
      EMP_TYP_CD ))
/