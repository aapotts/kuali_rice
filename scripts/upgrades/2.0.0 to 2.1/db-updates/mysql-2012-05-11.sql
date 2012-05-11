insert into krim_perm_t (perm_id, perm_tmpl_id, nmspc_cd, nm, desc_txt, actv_ind, ver_nbr, obj_id)
values ((select perm_id from (select (max(cast(perm_id as decimal)) + 1) as perm_id from krim_perm_t where perm_id is not NULL and cast(perm_id as decimal) < 10000)
         as tmptable),
        (select perm_tmpl_id from krim_perm_tmpl_t where nm = 'Send Ad Hoc Request' and nmspc_cd = 'KR-NS'),
        'KR-SYS','Send Complete Request Kuali Document','Authorizes users to send Complete ad hoc requests for Kuali Documents','Y',1,uuid())
;

insert into krim_perm_attr_data_t
(attr_data_id, perm_id, kim_typ_id, kim_attr_defn_id, attr_val, ver_nbr, obj_id)
values ((select attr_data_id from
          (select (max(cast(attr_data_id as decimal)) + 1) as attr_data_id from krim_perm_attr_data_t where attr_data_id is not NULL and cast(attr_data_id as decimal) < 10000)
         as tmptable),
        (select perm_id from krim_perm_t where nm = 'Send Complete Request Kuali Document' and nmspc_cd = 'KR-SYS'),
        (select kim_typ_id from krim_typ_t where nm = 'Ad Hoc Review' and nmspc_cd = 'KR-WKFLW'),
        (select kim_attr_defn_id from krim_attr_defn_t where nm = 'documentTypeName'), 'KualiDocument',1,uuid())
;


insert into krim_perm_attr_data_t
(attr_data_id, perm_id, kim_typ_id, kim_attr_defn_id, attr_val, ver_nbr, obj_id)
values ((select attr_data_id from
          (select (max(cast(attr_data_id as decimal)) + 1) as attr_data_id from krim_perm_attr_data_t where attr_data_id is not NULL and cast(attr_data_id as decimal) < 10000)
         as tmptable),
        (select perm_id from krim_perm_t where nm = 'Send Complete Request Kuali Document' and nmspc_cd = 'KR-SYS'),
        (select kim_typ_id from krim_typ_t where nm = 'Ad Hoc Review' and nmspc_cd = 'KR-WKFLW'),
        (select kim_attr_defn_id from krim_attr_defn_t where nm = 'actionRequestCd'), 'C',1,uuid())
;

insert into krim_role_perm_t
(role_perm_id, role_id, perm_id, actv_ind, ver_nbr, obj_id)
values ((select role_perm_id from
          (select (max(cast(role_perm_id as decimal)) + 1) as role_perm_id from krim_role_perm_t where role_perm_id is not NULL and cast(role_perm_id as decimal) < 10000)
         as tmptable),
        (select role_id from krim_role_t where role_nm = 'Document Opener' and nmspc_cd = 'KR-NS'),
        (select perm_id from krim_perm_t where nm = 'Send Complete Request Kuali Document' and nmspc_cd = 'KR-SYS'),
        'Y', 1, uuid())
;

insert into krim_role_perm_t
(role_perm_id, role_id, perm_id, actv_ind, ver_nbr, obj_id)
values ((select role_perm_id from
          (select (max(cast(role_perm_id as decimal)) + 1) as role_perm_id from krim_role_perm_t where role_perm_id is not NULL and cast(role_perm_id as decimal) < 10000)
         as tmptable),
        (select role_id from krim_role_t where role_nm = 'Initiator or Reviewer' and nmspc_cd = 'KR-WKFLW'),
        (select perm_id from krim_perm_t where nm = 'Edit Kuali ENROUTE Document Route Status Code R' and nmspc_cd = 'KUALI'),
        'Y', 1, uuid())
;

insert into krim_role_perm_t
(role_perm_id, role_id, perm_id, actv_ind, ver_nbr, obj_id)
values ((select role_perm_id from
          (select (max(cast(role_perm_id as decimal)) + 1) as role_perm_id from krim_role_perm_t where role_perm_id is not NULL and cast(role_perm_id as decimal) < 10000)
         as tmptable),
        (select role_id from krim_role_t where role_nm = 'Initiator or Reviewer' and nmspc_cd = 'KR-WKFLW'),
        (select perm_id from krim_perm_t where nm = 'Cancel Document' and nmspc_cd = 'KUALI'),
        'Y', 1, uuid())
;