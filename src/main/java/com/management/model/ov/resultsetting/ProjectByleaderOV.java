package com.management.model.ov.resultsetting;

import lombok.Data;

import java.util.Date;

/**
 * program: projectByleaderOV
 * description: 领导查询负责项目的列表信息
 * author: dhp
 * create: 2019/10/12 16:06
 */
@Data
public class ProjectByleaderOV {
    private String projectApplicationId;

    private String projectCategoryId;

    private String projectName;

    private String projectDescription;

    private String projectIndex;

    private Integer projectIndexState;

    private Integer projectMoney;

    private String userId;

    private String reviewPrincipalId;

    private String reviewLeaderId;

    private String projectApplicationUploadAddress;

    private Integer isMeeting;

    private String meetingReviewMessage;

    private String reviewPhase;

    private String failureReason;

    private Date applicationTime;

    private  String applicantType;
}