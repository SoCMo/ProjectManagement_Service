package com.management.model.ov.resultsetting;

import com.management.model.jsonrequestbody.ProjectMembers;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @program: management
 * @description: 项目详细信息
 * @author: ggmr
 * @create: 2018-12-26 18:43
 */
@Data
public class ProjectMoreInfo {
    private String applicationId;//new
    private String projectCategoryId;//new
    private String projectName;
    private List<ProjectMembers> members;
    private String description;
    private String applicationAddress;
    private String interimAddress;
    private String concludingAddress;
    private Integer projectMoney;
    private String projectIndex;
    private List<ExpertListInfo> expertList;

    private String projectProgressId;
    private String userId;
    private String interimReportUploadAddress;
    private Date interimReportTime;
    private Integer isFinishedInterimReport;
    private String interimReportFailureReason;
    private String concludingReportUploadAddress;
    private Date concludingReportTime;
    private Integer isFinishedConcludingReport;
    private String concludingReportFailureReason;
    private String projectProcess;
    private Date projectcreatetime;

    private String projectCategoryName;
    private String projectCategoryDescription;
    private String projectCategoryDescriptionAddress;
    private String projectType;
    private String principalId;
    private String principalPhone;
    private String applicantType;
    private String maxMoney;
    private String reviewLeaderId;
    private Integer isExistMeetingReview;
    private String projectApplicationDownloadAddress;
    private Date applicationStartTime;
    private Date applicationEndTime;
    private Date projectStartTime;
    private Date projectEndTime;
    private Integer isInterimReportActivated;
    private Date interimReportStartTime;
    private Date interimReportEndTime;
    private String interimReportDownloadAddress;
    private Integer isConcludingReportActivated;
    private Date concludingReportStartTime;
    private Date concludingReportEndTime;
    private String concludingReportDownloadAddress;
    private Integer statistics;
    private Integer isApproved;
    private String failureReason;
    private Integer modifyFrequency;
}
