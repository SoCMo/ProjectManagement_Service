package com.management.controller;

import com.management.model.jsonrequestbody.ExpertJudgeInfo;
import com.management.model.ov.Result;
import com.management.security.UserContext;
import com.management.service.ExpertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @program: management
 * @description: 专家controller
 * @author: ggmr
 * @create: 2018-08-01 17:01
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/expert")
@Api(value = "ExpertService对应的controller")
public class ExpertController {
    @Resource
    @ApiParam("和专家相关的业务操作")
    private ExpertService expertService;


    @GetMapping("/unJudgeProject")
    @ApiOperation(value = "专家查询负责项目大类的待审核项目申请")
    public Result findProjectApplication() {
        String userId = UserContext.getCurrentUser().getUserId();
        return expertService.findProjectApplication(userId);
    }

    @PostMapping("/judgeProject")
    @ApiOperation(value = "专家审核项目申请并提交审核意见")
    public Result judgeProjectApplication(@RequestBody ExpertJudgeInfo expertJudgeInfo){
        String userId = UserContext.getCurrentUser().getUserId();
        return expertService.judgeProjectApplication(userId,expertJudgeInfo);
    }


}
