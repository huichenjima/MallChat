package com.hechen.mallchat.common.user.dao;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.hechen.mallchat.common.common.domain.vo.req.CursorPageBaseReq;
import com.hechen.mallchat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.hechen.mallchat.common.common.utils.AssertUtil;
import com.hechen.mallchat.common.common.utils.CursorUtils;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.entity.UserFriend;
import com.hechen.mallchat.common.user.domain.vo.resp.friend.FriendResp;
import com.hechen.mallchat.common.user.mapper.UserFriendMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 用户联系人表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-28
 */
@Service
public class UserFriendDao extends ServiceImpl<UserFriendMapper, UserFriend> {


    public List<UserFriend> getByFriends(Long uid, List<Long> uidList) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .in(UserFriend::getFriendUid, uidList)
                .list();
    }

    public CursorPageBaseResp<UserFriend> getFriendPage(Long uid, CursorPageBaseReq request) {
        return CursorUtils.getCursorPageByMysql(this, request,
                wrapper -> wrapper.eq(UserFriend::getUid, uid), UserFriend::getId);



    }

    public boolean isFriend(Long uid, Long friendId) {
        UserFriend one = lambdaQuery().eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, friendId)
                .one();
        //如果不为空则true说明是朋友，反之返回false说明不是朋友
        return Objects.nonNull(one);
    }
    @Transactional
    public boolean deleteFriend(Long uid, Long targetUid) {
        boolean remove = this.remove(lambdaQuery().eq(UserFriend::getUid, uid).eq(UserFriend::getFriendUid, targetUid));
        boolean remove2 = this.remove(lambdaQuery().eq(UserFriend::getUid, targetUid).eq(UserFriend::getFriendUid, uid));
        return remove&&remove2;
    }


    public UserFriend getByFriend(Long uid, Long targetUid) {
        return lambdaQuery().eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, targetUid)
                .one();
    }
    //这里两条记录都查了双向的
    public List<UserFriend> getUserFriend(Long uid, Long friendUid) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, friendUid)
                .or()
                .eq(UserFriend::getFriendUid, uid)
                .eq(UserFriend::getUid, friendUid)
                .select(UserFriend::getId)
                .list();
    }
}
