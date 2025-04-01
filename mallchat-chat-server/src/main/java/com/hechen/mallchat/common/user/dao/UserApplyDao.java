package com.hechen.mallchat.common.user.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hechen.mallchat.common.common.domain.vo.resp.PageBaseResp;
import com.hechen.mallchat.common.user.domain.entity.UserApply;
import com.hechen.mallchat.common.user.domain.enums.ApplyReadStatusEnum;
import com.hechen.mallchat.common.user.domain.enums.ApplyStatusEnum;
import com.hechen.mallchat.common.user.domain.enums.ApplyTypeEnum;
import com.hechen.mallchat.common.user.mapper.UserApplyMapper;
import com.hechen.mallchat.common.user.service.IUserApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 用户申请表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-28
 */
@Service
public class UserApplyDao extends ServiceImpl<UserApplyMapper, UserApply>{

    //好友申请
    public UserApply addFriendApply(Long uid, Long targetUid, String msg) {
        UserApply userApply = new UserApply();
        userApply.setUid(uid);
        userApply.setTargetId(targetUid);
        userApply.setMsg(msg);
        userApply.setStatus(ApplyStatusEnum.WAIT_APPROVAL.getCode());
        userApply.setType(ApplyTypeEnum.ADD_FRIEND.getCode());
        userApply.setReadStatus(ApplyReadStatusEnum.UNREAD.getCode());
        boolean save = this.save(userApply);
        return userApply;
    }
    //分页查询当前用户的好友申请
    public PageBaseResp<UserApply> pageQuery(Long uid, Integer pageNo, Integer pageSize) {
        Page<UserApply> page = this.page(new Page<>(pageNo, pageSize), lambdaQuery()
                .eq(UserApply::getTargetId, uid)
                .eq(UserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .orderByDesc(UserApply::getCreateTime)
        );
        PageBaseResp<UserApply> userApplyPageBaseResp = new PageBaseResp<>();
        userApplyPageBaseResp.setPageNo(pageNo);
        userApplyPageBaseResp.setPageSize((int)page.getSize());
        userApplyPageBaseResp.setIsLast(!Objects.equals(pageSize,userApplyPageBaseResp.getPageSize()));
        userApplyPageBaseResp.setList(page.getRecords());
        userApplyPageBaseResp.setTotalRecords(page.getTotal());
        return userApplyPageBaseResp;
    }


    //修改申请状态
    public boolean applyApprove(Long uid, Long applyId) {
        boolean update = lambdaUpdate().eq(UserApply::getId, applyId)
                .set(UserApply::getStatus, ApplyStatusEnum.AGREE.getCode())
                .set(UserApply::getReadStatus, ApplyReadStatusEnum.READ.getCode())
                .update();
        return update;
    }

    public UserApply getFriendApproving(Long uid, Long targetUid) {
        return lambdaQuery().eq(UserApply::getUid, uid)
                .eq(UserApply::getTargetId, targetUid)
                .eq(UserApply::getStatus, ApplyStatusEnum.WAIT_APPROVAL)
                .eq(UserApply::getType, ApplyTypeEnum.ADD_FRIEND.getCode())
                .one();
    }

    //批量更新已读状态
    public void readApples(Long uid, List<Long> applyIds) {
        lambdaUpdate()
                .set(UserApply::getReadStatus, ApplyReadStatusEnum.READ.getCode())
                .eq(UserApply::getReadStatus, ApplyReadStatusEnum.UNREAD.getCode())
                .in(UserApply::getId, applyIds)
                .eq(UserApply::getTargetId, uid)
                .update();
    }

    public Integer getUnReadCount(Long targetId) {
        return lambdaQuery().eq(UserApply::getTargetId, targetId)
                .eq(UserApply::getReadStatus, ApplyReadStatusEnum.UNREAD.getCode())
                .count();
    }
}
