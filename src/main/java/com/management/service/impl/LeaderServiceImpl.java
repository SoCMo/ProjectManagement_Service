package com.management.service.impl;

import com.management.dao.*;
import com.management.model.entity.*;
import com.management.model.jsonrequestbody.IsProjectCategoryPassedPostInfo;
import com.management.model.jsonrequestbody.LeaderJudgeInfo;
import com.management.model.jsonrequestbody.ProjectMembers;
import com.management.model.ov.Result;
import com.management.model.ov.resultsetting.*;
import com.management.service.LeaderService;
import com.management.tools.ResultTool;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.management.model.ov.resultsetting.ConstCorrespond.*;
import static com.management.tools.TimeTool.timeToString1;
import static com.management.tools.TimeTool.timetoString;

/**
 * @program: management
 * @description: 领导层接口的实现
 * @author: ggmr
 * @create: 2018-07-31 09:34
 */
@Service
public class LeaderServiceImpl implements LeaderService {
    @Resource
    private ProjectCategoryMapper projectCategoryMapper;

    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    @Resource
    private ProjectProgressMapper projectProgressMapper;

    @Resource
    private ProjectMemberMapper projectMemberMapper;

    @Resource
    private ReviewExpertMapper reviewExpertMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AdminServiceImpl adminService;

    private static final int STATE_TWO = 2;
    private static final int STATE_THREE = 3;
    static final int PROJECT_INDEX_STATE = 7; //待提交任务书阶段
    private static final int FAIL_CHACK_PROJECT = 6;//项目申请审核失败阶段

    /**
     * @Description: isProjectCategoryPassed接口的实现
     * @Param: [info]
     * @Return: com.management.model.ov.Result
     * @Author: ggmr
     * @Date: 18-8-15
     */
    @Override
    public Result isProjectCategoryPassed(IsProjectCategoryPassedPostInfo info) {
        String projectCategoryId = info.getProjectCategoryId();
        ProjectCategory projectCategory = projectCategoryMapper
                .selectByPrimaryKey(projectCategoryId);
        if (projectCategory == null) {
            return ResultTool.error("不存在这个id的项目大类");
        }
        Integer isPassed = info.getIsPassed();
        if (isPassed == STATE_TWO) {
            projectCategory.setFailureReason(info.getMessage());
        }
        projectCategory.setIsApproved(isPassed);
        projectCategoryMapper.updateByPrimaryKeySelective(projectCategory);
        return ResultTool.success();
    }

    /**
     * @Description: findAllSubordinate的实现
     * @Param: [leaderId]
     * @Return: com.management.model.ov.Result
     * @Author: ggmr
     * @Date: 18-8-15
     */
    @Override
    public Result findAllSubordinate(String leaderId) {
        UserExample userExample = new UserExample();
        userExample.createCriteria()
                .andLeaderIdEqualTo(leaderId);
        List<User> list = userMapper.selectByExample(userExample);
        if (list.isEmpty()) {
            return ResultTool.error("领导并没有业务员下属");
        }
        List<LeaderSubordinateInfo> resList = new LinkedList<>();
        for (User user : list) {
            LeaderSubordinateInfo res = new LeaderSubordinateInfo();
            res.setPhone(user.getPhone());
            res.setUserId(user.getUserId());
            res.setUserName(user.getUserName());
            resList.add(res);
        }
        return ResultTool.success(resList);
    }

    /**
     * @Description: waitJudgeProjectCategoryList接口的实现
     * @Param: [userId]
     * @Return: com.management.model.ov.Result
     * @Author: ggmr
     * @Date: 18-8-15
     */
    @Override
    public Result waitJudgeProjectCategoryList(String leaderId) {
        ProjectCategoryExample pExacmple = new ProjectCategoryExample();
        pExacmple.createCriteria()
                .andReviewLeaderIdEqualTo(leaderId)
                .andIsApprovedEqualTo(STATE_THREE);
        List<ProjectCategory> list = projectCategoryMapper.selectByExample(pExacmple);
        if (list.isEmpty()) {
            return ResultTool.error("当前并没有需要处理的项目大类申请");
        }
        List<WaitJudgeProjectCategoryInfo> resList = new LinkedList<>();
        for (ProjectCategory projectCategory : list) {
            WaitJudgeProjectCategoryInfo res = new WaitJudgeProjectCategoryInfo();
            UserBaseInfo userBaseInfo = userMapper
                    .selectUserInfoByUserId(projectCategory.getPrincipalId());
            res.setAdminId(projectCategory.getPrincipalId());
            res.setAdminName(userBaseInfo.getUserName());
            res.setProjectCategoryId(projectCategory.getProjectCategoryId());
            res.setProjectCategoryName(projectCategory.getProjectCategoryName());
            res.setType(projectCategory.getProjectType());
            resList.add(res);
        }
        return ResultTool.success(resList);
    }

    /**
     * @Description: 查找负责的项目申请信息 type=1: 待审核的项目申请 type=2: 审核通过的项目申请 type=3:审核不通过的项目申请
     * @Param: [userId]
     * @Return: com.management.model.ov.Result
     * @Author: xw
     * @Date: 18-12-27
     */
    public Result findUnJudgeProjectApplication(String userId, int type) {

        ProjectCategoryExample example = new ProjectCategoryExample();
        example.createCriteria()
                .andReviewLeaderIdEqualTo(userId)
                .andApplicationEndTimeGreaterThan(new Date());
        List<ProjectCategory> list = projectCategoryMapper.selectByExample(example);
        if (list.isEmpty()) {
            return ResultTool.error("您没有下属业务员创建项目大类");
        }
        List<AdminJudgeTotalInfo> resList = new LinkedList<>();
        for (ProjectCategory category : list) {
            ProjectApplicationExample applicationExample = new ProjectApplicationExample();
            if (type == 1) {
                applicationExample.createCriteria()
                        .andProjectCategoryIdEqualTo(category.getProjectCategoryId())
                        .andReviewPhaseEqualTo(4);
            }
            if (type == 2) {
                applicationExample.createCriteria()
                        .andProjectCategoryIdEqualTo(category.getProjectCategoryId())
                        .andReviewPhaseEqualTo(5);
            }
            if (type == 3) {
                applicationExample.createCriteria()
                        .andProjectCategoryIdEqualTo(category.getProjectCategoryId())
                        .andReviewPhaseEqualTo(6);
            }
            List<ProjectApplication> applicationList = projectApplicationMapper.selectByExample(applicationExample);
            if (applicationList.isEmpty()) continue;
            for (ProjectApplication application : applicationList) {
                AdminJudgeTotalInfo res = new AdminJudgeTotalInfo();
                res.setApplicationDeadLine(timetoString(category.getApplicationEndTime()));
                res.setProjectCategoryId(category.getProjectCategoryId());
                res.setProjectCategoryName(category.getProjectCategoryName());
                res.setProjectId(application.getProjectApplicationId());
                res.setProjectDownloadAddress(application.getProjectApplicationUploadAddress());
                res.setProjectName(application.getProjectName());
                res.setDescription(application.getProjectDescription());
                res.setExpertOpinion(adminService.getExpertOpinionList(application.getProjectApplicationId()));
                resList.add(res);
            }
        }
        return ResultTool.success(resList);
    }

    /**
     * @Description: 领导审核待审核的用户项目申请
     * @Param: [projectApplicationId]
     * @Return: Result
     * @Author: xw
     * @Date: 18-12-27
     */
    @Override
    public Result judgeProjectApplication(LeaderJudgeInfo leaderJudgeInfo) {
        try {
            ProjectApplication projectApplication = projectApplicationMapper
                    .selectByPrimaryKey(leaderJudgeInfo.getProjectApplicationId());
            if (leaderJudgeInfo.getJudge()) {
                //领导审核通过后进入待提交任务书
                projectApplication.setReviewPhase(PROJECT_INDEX_STATE);
            } else {
                projectApplication.setReviewPhase(FAIL_CHACK_PROJECT);
                projectApplication.setFailureReason(leaderJudgeInfo.getMsg());
            }
            projectApplicationMapper.updateByPrimaryKey(projectApplication);
            return ResultTool.success();
        } catch (Exception e) {
            return ResultTool.error("操作失败!");
        }
    }

    @Override
    public Result judgeFinalReport(LeaderJudgeInfo info) {
        ProjectProgress projectProgress = projectProgressMapper
                .selectByPrimaryKey(info.getProjectApplicationId());
        if (info.getJudge()) {
            projectProgress.setProjectProcess(FINISH_PROJECT);
        } else {
            projectProgress.setProjectProcess(FINISH_PROGRESS_FAILED);
            projectProgress.setConcludingReportFailureReason(info.getMsg());
            projectProgress.setIsFinishedConcludingReport(2);
        }
        projectProgressMapper.updateByPrimaryKeySelective(projectProgress);
        return ResultTool.success();
    }

    @Override
    public Result findWaitFinalJudgeList(String leaderId) {


        List<FinalReportInfo> resList = userMapper
                .selectFinalProgressByLeaderId(leaderId);
        if (resList.isEmpty()) {
            return ResultTool.success("没有待终审项目");
        }
        for(FinalReportInfo reportInfo: resList){
            reportInfo.setConcludingReportEndTime(timetoString(reportInfo.getEndTime()));
            String id = reportInfo.getProjectProgressId();
            reportInfo.setExpertOpinion(adminService.getExpertOpinionList(id));
        }
        return ResultTool.success(resList);
    }

    /**
     * @Description: 领导查询负责的已立项和审核失败的项目
     * @Param: leaderId
     * @Return: Result
     * @Author: xw
     * @Date: 19-1-25
     */
    public Result leaderQueryMyProject(String leaderId){
        List<ProjectTotalInfo> buildProject = new LinkedList<>();
        List<ProjectTotalInfo> middleProject = new LinkedList<>();
        List<ProjectTotalInfo> finalProject = new LinkedList<>();
        List<ProjectTotalInfo> finishProject = new LinkedList<>();
        List<ProjectTotalInfo> failedProject = new LinkedList<>();
        //查找到业务员负责的项目申请信息列表
        List<ProjectApplication> applicationList = projectApplicationMapper
                .queryAllProgressAndFailProject(leaderId);

        for(ProjectApplication application : applicationList){
            ProjectProgress progress = projectProgressMapper
                    .selectByPrimaryKey(application.getProjectApplicationId());
            ProjectTotalInfo info = new ProjectTotalInfo();
            info.setDescription(application.getProjectDescription());
            info.setProjectApplicationId(application.getProjectApplicationId());
            info.setProjectName(application.getProjectName());
            //查询到相应的项目大类
            ProjectCategory projectCategory = projectCategoryMapper
                    .selectByPrimaryKey(application.getProjectCategoryId());
            info.setProjectCategory(projectCategory.getProjectCategoryName());
            info.setProjectCategoryId(projectCategory.getProjectCategoryId());
            UserBaseInfo userBaseInfo = userMapper
                    .selectUserInfoByUserId(projectCategory.getPrincipalId());
            info.setAdminName(userBaseInfo.getUserName());
            //处理已经立项的项目
            if(application.getReviewPhase().equals(5)){
                info.setReviewPhase(ConstCorrespond
                        .PROJECT_PROGRESS[progress.getProjectProcess()]);
                //获取当前时间,只给用户提供在中期报告和结题报告提交时间段内的项目
                Date nowTime = new Date();
                switch (progress.getProjectProcess()) {
                    case 1 : {
                        info.setTime(timetoString(progress.getProjectcreatetime()));
                        info.setStatus(1);
                        buildProject.add(info);
                        break;
                    }
                    case 2 : {
                        Date InterimReportEndTime = projectCategory.getInterimReportEndTime();
                        Date InterimReportStartTime = projectCategory.getInterimReportStartTime();
                        if(projectCategory.getIsInterimReportActivated()==1) {
                            if(!InterimReportStartTime.before(nowTime)) {
                                info.setStatus(3);
                            } else if(InterimReportEndTime.after(nowTime)) {
                                info.setReportAddress(projectCategory.getInterimReportDownloadAddress());
                                info.setStatus(1);
                            } else {
                                info.setStatus(2);
                            }
                        }
                        info.setTime(timetoString(InterimReportEndTime));
                        middleProject.add(info);
                        break;
                    }
                    case 3 :{
                        Date ConcludingReportEndTime = projectCategory.getConcludingReportEndTime();
                        Date ConcludingReportStartTime = projectCategory.getConcludingReportStartTime();
                        if(projectCategory.getIsConcludingReportActivated() == 1) {
                            if(!ConcludingReportStartTime.before(nowTime)) {
                                info.setStatus(3);
                            } else if(ConcludingReportEndTime.after(nowTime)) {
                                info.setReportAddress(projectCategory.getConcludingReportDownloadAddress());
                                info.setStatus(1);
                            } else {
                                info.setStatus(2);
                            }
                        }
                        info.setTime(timetoString(ConcludingReportEndTime));
                        finalProject.add(info);
                        break;
                    }
                    case 4: {
                        info.setTime(timetoString(projectCategory.getProjectEndTime()));
                        info.setStatus(1);
                        finishProject.add(info);
                        break;
                    }
                    case 5: {
                        info.setFailMessage(progress.getConcludingReportFailureReason());
                        failedProject.add(info);
                    }
                }
            }

            if(application.getReviewPhase().equals(6)){
                info.setFailMessage(application.getFailureReason());
                failedProject.add(info);
            }
        }
        FindProjectInfo res = new FindProjectInfo();
        res.setBuildProject(buildProject);
        res.setMiddleProject(middleProject);
        res.setFinalProject(finalProject);
        res.setFinishProject(finishProject);
        res.setFailProject(failedProject);
        return ResultTool.success(res);
    }

    /**
     * @Description: 给领导返回数据统计分析
     * @Param:
     * @Return:
     * @Author: xw
     * @Date: 19-3-27
     */
    public Result leaderDataStatistics(String leaderId){
        DataStatistics dataStatistics = new DataStatistics();
        List<projectStatistic> projectStatisticList = new ArrayList<>();
        List<projectTypeStatistic> projectTypeStatisticList = new ArrayList<>();

        projectStatistic JudgePassProject = new projectStatistic();
        projectStatistic JudgeFailProject = new projectStatistic();
        projectStatistic JudgeOneJudgeProject = new projectStatistic();
        projectStatistic JudgeFinalJudgeProject = new projectStatistic();

        JudgePassProject.setValue(userMapper.countJudgePassProject(leaderId));
        JudgePassProject.setName("已通过");
        projectStatisticList.add(JudgePassProject);
        JudgeFailProject.setValue(userMapper.countJudgeFailProject(leaderId));
        JudgeFailProject.setName("已驳回");
        projectStatisticList.add(JudgeFailProject);
        JudgeOneJudgeProject.setValue(userMapper.countOneJudgeProject(leaderId));
        JudgeOneJudgeProject.setName("待初审");
        projectStatisticList.add(JudgeOneJudgeProject);
        JudgeFinalJudgeProject.setValue(userMapper.countFinalJudgeProject(leaderId));
        JudgeFinalJudgeProject.setName("待终审");
        projectStatisticList.add(JudgeFinalJudgeProject);
        dataStatistics.setProjectStatistic(projectStatisticList);

        Integer maxNum = ConstCorrespond.PROJECT_TYPE.length;
        for(int i = maxNum-1;i>=1;i--){
            projectTypeStatistic typeStatistic = new projectTypeStatistic();
            typeStatistic.setType(ConstCorrespond.PROJECT_TYPE[i]);
            typeStatistic.setNum(userMapper.countByProjectType(leaderId,i));
            projectTypeStatisticList.add(typeStatistic);
        }
        dataStatistics.setProjectTypeList(projectTypeStatisticList);

        return ResultTool.success(dataStatistics);

    }

    /**
    * @Description: 给领导返回可申请项目
    * @Param: []
    * @Return: com.management.model.ov.Result
    * @Author: dhp
    * @Date: 2019/9/29
    */
    @Override
    public Result AllAviProject() {
        List<ProjectCategory> projectCategoryList = projectCategoryMapper
                .selectCanProjectCategory(new Date());
        if (projectCategoryList.isEmpty()) {
            return ResultTool.success(new LinkedList<AviProjectCategoryInfo>());
        }

        //根据排序好的结果，先遍历的创建包含这些type的内容，
        // 然后去遍历projectCategoryList内容找到符合当前条件的加进去
        List<AviProjectCategoryInfo> resList = new LinkedList<>();
        for (ProjectCategory projectCategory : projectCategoryList) {
            AviProjectCategoryInfo info = new AviProjectCategoryInfo();
            info.setType(ConstCorrespond.PROJECT_TYPE[projectCategory.getProjectType()]);
            info.setDeadLine(timeToString1(projectCategory.getApplicationEndTime()));
            info.setIntroduce(projectCategory.getProjectCategoryDescription());
            info.setProjectId(projectCategory.getProjectCategoryId());
            info.setProjectName(projectCategory.getProjectCategoryName());
            info.setIsMeeting(projectCategory.getIsExistMeetingReview() == 1 ? "true" : "false");
            info.setDownLoadAddress(ConstCorrespond.downloadAddress +
                    projectCategory.getProjectApplicationDownloadAddress());
            info.setProjectMaxMoney(projectCategory.getMaxMoney());
            resList.add(info);
        }
        return ResultTool.success(resList);
    }

    /**
    * @Description:获取领导负责的所有项目
    * @Param: [leaderId]
    * @Return: com.management.model.ov.Result
    * @Author: dhp
    * @Date: 2019/9/30
    */
    @Override
    public Result ProjectByLeader(String leaderId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria()
                .andReviewLeaderIdEqualTo(leaderId);
        List<ProjectApplication> list = projectApplicationMapper.selectByExample(example);
        if (list.isEmpty()) {
            return ResultTool.error("负责项目为空");
        }

        List<ProjectByleaderOV> resList = new ArrayList<>();

        for(ProjectApplication application : list){
            ProjectByleaderOV projectByleaderOV = new ProjectByleaderOV();
            BeanUtils.copyProperties(application, projectByleaderOV);

            ProjectCategory projectCategory = projectCategoryMapper.selectByPrimaryKey(application.getProjectCategoryId());

            projectByleaderOV.setReviewPhase(reviewPhrase[application.getReviewPhase()]);
            projectByleaderOV.setApplicantType(ConstCorrespond.PROJECT_TYPE[Integer.parseInt(projectCategory.getApplicantType())]);
            resList.add(projectByleaderOV);
        }

        return ResultTool.success(resList);
    }

    /**
    * @Description: 查看用户一个项目的详细信息
    * @Param: [applicationId]
    * @Return: com.management.model.ov.Result
    * @Author: dhp
    * @Date: 2019/9/30
    */
    @Override
    public Result MoreInfo(String applicationId) {
        ProjectApplication resApplication = projectApplicationMapper
                .selectByPrimaryKey(applicationId);
        ProjectProgress resProgress = projectProgressMapper
                .selectByPrimaryKey(applicationId);
        ProjectMoreInfo res = new ProjectMoreInfo();
        res.setApplicationAddress(ConstCorrespond.downloadAddress +
                resApplication.getProjectApplicationUploadAddress());
        res.setApplicationId(resApplication.getProjectApplicationId());
        res.setProjectCategoryId(resApplication.getProjectCategoryId());
        if(resProgress != null) {
            if (resProgress.getIsFinishedInterimReport() == 1) {
                res.setInterimAddress(ConstCorrespond.downloadAddress +
                        resProgress.getInterimReportUploadAddress());
            }
            if (resProgress.getIsFinishedConcludingReport() == 1) {
                res.setConcludingAddress(ConstCorrespond.downloadAddress +
                        resProgress.getConcludingReportUploadAddress());
            }
        }
        res.setDescription(resApplication.getProjectDescription());
        res.setProjectName(resApplication.getProjectName());
        res.setProjectMoney(resApplication.getProjectMoney());
        if(resApplication.getProjectIndex() != null){
            res.setProjectIndex(resApplication.getProjectIndex());
        }
        //根据ProjectApplicationId找到项目人员
        List<ProjectMember> members = projectMemberMapper.selectByApplicationId(resApplication.getProjectApplicationId());
        List<ProjectMembers> resMembers = new LinkedList<>();
        //先添加负责人的信息
        UserBaseInfo userBaseInfo = userMapper.selectUserInfoByUserId(resApplication.getUserId());
        ProjectMembers firstMember = new ProjectMembers();
        firstMember.setDepartment(userBaseInfo.getDepartment());
        firstMember.setMail(userBaseInfo.getMail());
        firstMember.setPhone(userBaseInfo.getPhone());
        firstMember.setUserId(resApplication.getUserId());
        firstMember.setUserName(userBaseInfo.getUserName());
        resMembers.add(firstMember);
        for(ProjectMember member : members) {
            ProjectMembers resMember = new ProjectMembers();
            resMember.setDepartment(member.getDepartment());
            resMember.setMail(member.getMail());
            resMember.setPhone(member.getPhone());
            resMember.setUserId(member.getUserId());
            resMember.setUserName(member.getUserName());
            resMembers.add(resMember);
        }
        ReviewExpertExample expertExample = new ReviewExpertExample();
        expertExample.createCriteria()
                .andProjectApplicationIdEqualTo(applicationId);
        List<ReviewExpert> list = reviewExpertMapper.selectByExample(expertExample);
        if(!list.isEmpty()) {
            List<ExpertListInfo> resList = new LinkedList<>();
            for(ReviewExpert expert : list) {
                User user = userMapper.selectByPrimaryKey(expert.getExpertId());
                ExpertListInfo info = new ExpertListInfo();
                info.setDepartment(user.getDepartment());
                info.setMail(user.getMail());
                info.setPhone(user.getPhone());
                info.setUserId(user.getUserId());
                info.setUserName(user.getUserName());
                resList.add(info);
            }
            res.setExpertList(resList);
        }
        res.setMembers(resMembers);

        ProjectProgress projectProgress = projectProgressMapper.selectByPrimaryKey(applicationId);
        if(projectProgress != null){
            BeanUtils.copyProperties(projectProgress, res);
            res.setProjectProcess(PROJECT_PROGRESS[projectProgress.getProjectProcess()]);
        }

        ProjectCategory projectCategory = projectCategoryMapper.selectByPrimaryKey(res.getProjectCategoryId());
        if (projectCategory != null){
            BeanUtils.copyProperties(projectCategory, res);
            res.setProjectType(PROJECT_TYPE[projectCategory.getProjectType()]);
        }

        return ResultTool.success(res);
    }
}
