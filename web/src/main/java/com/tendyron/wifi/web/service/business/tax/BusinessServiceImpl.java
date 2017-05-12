package com.tendyron.wifi.web.service.business.tax;

import com.tendyron.wifi.web.dao.business.tax.AgencyDao;
import com.tendyron.wifi.web.dao.business.tax.BusinessCategoryDao;
import com.tendyron.wifi.web.dao.business.tax.BusinessDao;
import com.tendyron.wifi.web.dao.business.tax.BusinessIssueDao;
import com.tendyron.wifi.web.dao.system.UserDao;
import com.tendyron.wifi.web.entity.business.tax.*;
import com.tendyron.wifi.web.entity.system.UserEntity;
import com.tendyron.wifi.web.model.PagingModel;
import com.tendyron.wifi.web.model.business.tax.*;
import com.tendyron.wifi.web.query.business.tax.BusinessQuery;
import com.tendyron.wifi.web.query.business.tax.StatementQuery;
import com.tendyron.wifi.web.service.BaseServiceImpl;
import com.tendyron.wifi.web.service.ServiceException;
import com.tendyron.wifi.web.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Neo on 2017/5/9.
 */
@Service("businessService")
public class BusinessServiceImpl extends BaseServiceImpl<BusinessEntity> implements BusinessService {

    private static final Logger logger = LoggerFactory.getLogger(BusinessServiceImpl.class);
    @Autowired
    private BusinessDao businessDao;

    @Autowired
    private BusinessCategoryDao businessCategoryDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private BusinessIssueDao businessIssueDao;

    @Autowired
    private AgencyDao agencyDao;

    @Transactional
    @Override
    public PagingModel paging(BusinessQuery query) throws ServiceException {
        queryBasicAssert(query);
        List<BusinessModel> businessModels = new ArrayList<>();
        PagingModel paging = new PagingModel();
        Long total = null;

        try {
            List<BusinessEntity> businessEntities = businessDao.paging(query);
            for (BusinessEntity businessEntity : businessEntities) {

                BusinessModel businessModel = new BusinessModel();
                BeanUtils.copyProperties(businessEntity, businessModel);

                AgencyEntity agencyEntity = businessEntity.getAgency();
                if (agencyEntity != null) {
                    businessModel.setAgencyId(agencyEntity.getId());
                    businessModel.setAgencyName(agencyEntity.getName());
                }

                BusCategoryEntity categoryEntity = businessEntity.getCategory();
                if (categoryEntity != null) {
                    businessModel.setCategoryId(categoryEntity.getId());
                    businessModel.setCategoryName(categoryEntity.getName());
                }

                BusCategoryTypeEntity categoryTypeEntity = categoryEntity.getType();
                if (categoryTypeEntity != null) {
                    businessModel.setCategoryTypeId(categoryTypeEntity.getId());
                    businessModel.setCategoryTypeName(categoryTypeEntity.getName());
                }

                BusIssueEntity issueEntity = businessEntity.getIssue();
                if (issueEntity != null) {
                    businessModel.setIssueId(issueEntity.getId());
                    businessModel.setIssueName(issueEntity.getName());
                }

                UserEntity create = businessEntity.getCreate();
                if (create != null) {
                    businessModel.setCreateId(create.getId());
                    businessModel.setCreateName(create.getName());
                }

                UserEntity check = businessEntity.getCheck();
                if (check != null) {
                    businessModel.setCheckId(check.getId());
                    businessModel.setCheckName(check.getName());
                }

                UserEntity finalCheck = businessEntity.getFinalCheck();
                if (finalCheck != null) {
                    businessModel.setFinalCheckId(finalCheck.getId());
                    businessModel.setFinalCheckName(finalCheck.getName());
                }

                businessModels.add(businessModel);
            }
            total = businessDao.getCount(query);
        } catch (Exception e) {
            logger.error("", e);
            throw new ServiceException();
        }

        paging.setRows(businessModels);
        paging.setTotal(total);
        return paging;
    }

    @Override
    @Transactional
    public void add(BusinessModel businessModel) throws ServiceException {

        if (businessModel == null) {
            throw new ServiceException("业务对象不能为空！");
        }

        BusinessEntity businessEntity = new BusinessEntity();

        BeanUtils.copyProperties(businessModel, businessEntity);

        try {
            if (!StringTools.isEmpty(businessModel.getCategoryId())) {
                BusCategoryEntity categoryEntity = businessCategoryDao.getById(businessModel.getCategoryId());
                businessEntity.setCategory(categoryEntity);
            }
            if (businessModel.getHasIssue() && !StringTools.isEmpty(businessModel.getIssueId())) {
                BusIssueEntity busIssueEntity = businessIssueDao.getById(businessModel.getIssueId());
                businessEntity.setIssue(busIssueEntity);
            }
            if (!StringTools.isEmpty(businessModel.getUserId())) {
                UserEntity create = userDao.getById(businessModel.getUserId());
                businessEntity.setCreate(create);
                businessEntity.setAgency(create.getAgency());
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        businessEntity.setCreateTime(new Date());
        businessEntity.setId(StringTools.randomUUID());
        businessDao.save(businessEntity);
    }


    @Override
    @Transactional
    public void edit(BusinessModel business) throws ServiceException {

        try {
            BusinessEntity businessEntity = businessDao.getById(business.getId());
            if (businessEntity == null) {
                throw new ServiceException("ID为" + business.getId() + " 的记录已经不存在。");
            }

            businessEntity.setTaxpayerCode(business.getTaxpayerCode());
            businessEntity.setTaxpayerName(business.getTaxpayerName());
            BusCategoryEntity categoryEntity = businessCategoryDao.getById(business.getCategoryId());
            if (categoryEntity != null) {
                businessEntity.setCategory(categoryEntity);
            }
            if (business.getHasIssue() && !StringTools.isEmpty(business.getIssueId())) {
                BusIssueEntity issueEntity = businessIssueDao.getById(business.getIssueId());
                if (issueEntity != null) {
                    businessEntity.setIssue(issueEntity);
                }
            } else {
                businessEntity.setHasIssue(false);
                businessEntity.setIssue(null);
            }
            businessEntity.setBusTime(business.getBusTime());
            businessEntity.setDescription(business.getDescription());

            businessEntity.setModifyTime(new Date());
            businessDao.save(businessEntity);
        } catch (Exception e) {
            logger.error("", e);
            throw new ServiceException();
        }

    }

    @Override
    @Transactional
    public Integer del(String[] ids) throws ServiceException {
        Integer result = null;
        try {
            result = businessDao.delByIds(ids);
        } catch (Exception e) {
            logger.error("", e);
            throw new ServiceException();
        }
        return result;
    }

    @Override
    @Transactional
    public List<StatementModel> statement(StatementQuery query) throws ServiceException {
        BusinessQuery bQuery = new BusinessQuery();
        bQuery.setCreateTimeStart(query.getStartCreate());
        bQuery.setCreateTimeEnd(query.getEndCreate());

        List<StatementModel> sms = new ArrayList<>();

        try {
            for (String agencyId : query.getAgencyIds()) {
                bQuery.setAgencyId(agencyId);
                bQuery.setCreateTimeStart(query.getStartCreate());
                bQuery.setCreateTimeEnd(query.getEndCreate());

                AgencyEntity agency = agencyDao.getById(agencyId);
                StatementModel sm = new StatementModel();
                sm.setAgencyName(agency.getName());
                sm.setAgencyId(agency.getId());
                List<BusinessEntity> bes = businessDao.getList(bQuery);
                List<StatementCategoryTypeModel> sctms = new ArrayList<>();

                for (BusinessEntity be : bes) {
                    StatementCategoryTypeModel sctmTmp = null;
                    BusCategoryTypeEntity bcte = be.getCategory().getType();

                    for (StatementCategoryTypeModel sctm : sctms) {
                        if (sctm.getId().equals(bcte.getId())) {
                            sctmTmp = sctm;
                            break;
                        }
                    }
                    if (sctmTmp == null) {
                        sctmTmp = new StatementCategoryTypeModel();
                        sctmTmp.setName(bcte.getName());
                        sctmTmp.setId(bcte.getId());
                        sctms.add(sctmTmp);
                    }

                    BusCategoryEntity bce = be.getCategory();

                    List<StatementCategoryModel> scms = sctmTmp.getRecs();
                    StatementCategoryModel scmTmp = null;

                    if (scms != null) {
                        for (StatementCategoryModel scm : scms) {
                            if (scm.getId().equals(bce.getId())) {
                                scmTmp = scm;
                                break;
                            }
                        }
                    } else {
                        scms = new ArrayList<>();
                        sctmTmp.setRecs(scms);
                    }

                    if (scmTmp == null) {
                        scmTmp = new StatementCategoryModel();
                        scmTmp.setName(bce.getName());
                        scmTmp.setId(bce.getId());
                        scms.add(scmTmp);
                    }
                    scmTmp.setCount(scmTmp.getCount() + 1);
                    sm.setDetailCount(sm.getDetailCount() + 1);
                    BusIssueEntity busIssueEntity = be.getIssue();
                    if (busIssueEntity != null) {
                        scmTmp.getIssueNames().add(busIssueEntity.getName());
                    }
                }
                sm.setRecs(sctms);
                sms.add(sm);
            }
        } catch (Exception e) {
            logger.error("", e);
            throw new ServiceException();
        }
        return sms;
    }
}
