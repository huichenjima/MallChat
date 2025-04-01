package com.hechen.mallchat.common.user.service;

import com.hechen.mallchat.common.common.domain.vo.req.CursorPageBaseReq;
import com.hechen.mallchat.common.common.domain.vo.req.PageBaseReq;
import com.hechen.mallchat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.hechen.mallchat.common.common.domain.vo.resp.PageBaseResp;
import com.hechen.mallchat.common.user.domain.entity.UserFriend;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hechen.mallchat.common.user.domain.vo.req.friend.FriendApplyReq;
import com.hechen.mallchat.common.user.domain.vo.req.friend.FriendApproveReq;
import com.hechen.mallchat.common.user.domain.vo.req.friend.FriendCheckReq;
import com.hechen.mallchat.common.user.domain.vo.resp.friend.FriendApplyResp;
import com.hechen.mallchat.common.user.domain.vo.resp.friend.FriendCheckResp;
import com.hechen.mallchat.common.user.domain.vo.resp.friend.FriendResp;
import com.hechen.mallchat.common.user.domain.vo.resp.friend.FriendUnreadResp;

/**
 * <p>
 * 用户联系人表 服务类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-28
 */
public interface FriendService {

    CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request);

    FriendCheckResp check(Long uid, FriendCheckReq request);

    void apply(Long uid, FriendApplyReq request);

    void deleteFriend(Long uid, Long targetUid);

    PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request);

    FriendUnreadResp unread(Long uid);

    void applyApprove(Long uid, FriendApproveReq request);
}
