package com.hechen.mallchat.common.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.PageUtil;
import com.hechen.mallchat.common.common.annotation.RedissonLock;
import com.hechen.mallchat.common.common.domain.vo.req.CursorPageBaseReq;
import com.hechen.mallchat.common.common.domain.vo.req.PageBaseReq;
import com.hechen.mallchat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.hechen.mallchat.common.common.domain.vo.resp.PageBaseResp;
import com.hechen.mallchat.common.common.event.UserApplyEvent;
import com.hechen.mallchat.common.common.exception.BusinessException;
import com.hechen.mallchat.common.common.utils.AssertUtil;
import com.hechen.mallchat.common.user.dao.UserApplyDao;
import com.hechen.mallchat.common.user.dao.UserDao;
import com.hechen.mallchat.common.user.dao.UserFriendDao;
import com.hechen.mallchat.common.user.domain.entity.User;
import com.hechen.mallchat.common.user.domain.entity.UserApply;
import com.hechen.mallchat.common.user.domain.entity.UserFriend;
import com.hechen.mallchat.common.user.domain.enums.ApplyReadStatusEnum;
import com.hechen.mallchat.common.user.domain.enums.ApplyStatusEnum;
import com.hechen.mallchat.common.user.domain.enums.DeleteStatusEnum;
import com.hechen.mallchat.common.user.domain.vo.req.friend.FriendApplyReq;
import com.hechen.mallchat.common.user.domain.vo.req.friend.FriendApproveReq;
import com.hechen.mallchat.common.user.domain.vo.req.friend.FriendCheckReq;
import com.hechen.mallchat.common.user.domain.vo.req.friend.FriendDeleteReq;
import com.hechen.mallchat.common.user.domain.vo.resp.friend.FriendApplyResp;
import com.hechen.mallchat.common.user.domain.vo.resp.friend.FriendCheckResp;
import com.hechen.mallchat.common.user.domain.vo.resp.friend.FriendResp;
import com.hechen.mallchat.common.user.domain.vo.resp.friend.FriendUnreadResp;
import com.hechen.mallchat.common.user.service.FriendService;
import com.hechen.mallchat.common.user.service.adapter.FriendAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName: FriendServiceImpl
 * Package: com.hechen.mallchat.common.user.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/28 16:10
 * @Version 1.0
 */
@Service
@Slf4j
public class FriendServiceImpl implements FriendService {

    @Autowired
    private UserFriendDao userFriendDao;
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserApplyDao userApplyDao;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    //游标分页查询当前用户好友列表
    @Override
    public CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq request) {
        //游标分页查询，最后返回的游标是这一页的最后一个uid
        CursorPageBaseResp<UserFriend> friendPage = userFriendDao.getFriendPage(uid, request);
        if (CollectionUtils.isEmpty(friendPage.getList())) {
            return CursorPageBaseResp.empty();
        }
        //所有的好友uid
        List<Long> friendUids = friendPage.getList()
                .stream().map(UserFriend::getFriendUid)
                .collect(Collectors.toList());
        //查询所有好友的详细信息
        List<User> userList = userDao.getFriendList(friendUids);
        return CursorPageBaseResp.init(friendPage, FriendAdapter.buildFriend(friendPage.getList(), userList));
    }

    //批量检查是否是自己好友
    @Override
    public FriendCheckResp check(Long uid, FriendCheckReq request) {
        List<UserFriend> friendList = userFriendDao.getByFriends(uid, request.getUidList());

        Set<Long> friendUidSet = friendList.stream().map(UserFriend::getFriendUid).collect(Collectors.toSet());
        List<FriendCheckResp.FriendCheck> friendCheckList = request.getUidList().stream().map(friendUid -> {
            FriendCheckResp.FriendCheck friendCheck = new FriendCheckResp.FriendCheck();
            friendCheck.setUid(friendUid);
            friendCheck.setIsFriend(friendUidSet.contains(friendUid));
            return friendCheck;
        }).collect(Collectors.toList());
        return new FriendCheckResp(friendCheckList);
    }

    //申请好友
    @Override
    @RedissonLock(key = "#uid") //分布式锁，这里是注解实现的，使用了切片
    public void apply(Long uid, FriendApplyReq request) {
        //是否有好友关系
        UserFriend friend = userFriendDao.getByFriend(uid, request.getTargetUid());
        AssertUtil.isEmpty(friend, "你们已经是好友了");
        //是否有待审批的申请记录(自己的)
        UserApply selfApproving = userApplyDao.getFriendApproving(uid, request.getTargetUid());
        if (Objects.nonNull(selfApproving)) {
            log.info("已有好友申请记录,uid:{}, targetId:{}", uid, request.getTargetUid());
            return;
        }
        //是否有待审批的申请记录(别人请求自己的)，请注意是待审批，因为可能对方删除了自己仍然保存了之前同意了的申请记录
        UserApply friendApproving = userApplyDao.getFriendApproving(request.getTargetUid(), uid);
        if (Objects.nonNull(friendApproving)) {
            //这里为了保证事务使用了aop代理，还有一种方法是注入自己
            ((FriendService) AopContext.currentProxy()).applyApprove(uid, new FriendApproveReq(friendApproving.getId()));
            return;
        }

        //确保可以添加了，进行申请,入库
        UserApply userApply = userApplyDao.addFriendApply(uid, request.getTargetUid(), request.getMsg());

        // todo 申请事件,发送消息 ，这里没有写完 ，发送消息服务没写
        applicationEventPublisher.publishEvent(new UserApplyEvent(this, userApply));



    }

    //删除好友
    @Override
    @Transactional
    public void deleteFriend(Long uid, Long targetUid) {
        List<UserFriend> userFriends = userFriendDao.getUserFriend(uid, targetUid);
        if (CollectionUtil.isEmpty(userFriends)) {
            log.info("没有好友关系：{},{}", uid, targetUid);
            return;
        }
        //有两条记录要删，双边都删掉
        boolean b = userFriendDao.deleteFriend(uid, targetUid);

        //禁用房间 todo ，因为删除好友了，所以他们两个的私聊房间要禁用掉
//        roomService.disableFriendRoom(Arrays.asList(uid, friendUid));


    }

    //分页查询好友申请记录 ，并且把查询到的好友申请状态改为已读
    @Override
    public PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq request) {
        PageBaseResp<UserApply> pages=userApplyDao.pageQuery(uid,request.getPageNo(),request.getPageSize());
        if (CollectionUtil.isEmpty(pages.getList())) {
            return PageBaseResp.empty();
        }

        //将这些申请列表设为已读

        readApples(uid, pages.getList());

        //转为FriendApplyResp
        List<UserApply> list = pages.getList();
        List<FriendApplyResp> collect = list.stream().map(userApply -> {
            FriendApplyResp friendApplyResp = BeanUtil.copyProperties(userApply, FriendApplyResp.class);
            friendApplyResp.setApplyId(userApply.getId());
            return friendApplyResp;

        }).collect(Collectors.toList());
        PageBaseResp<FriendApplyResp> result = PageBaseResp.init(pages, collect);
        return result;
    }

    //设置为已读
    private void readApples(Long uid, List<UserApply> list) {
        List<Long> applyIds = list.stream().map(UserApply::getId).collect(Collectors.toList());
        userApplyDao.readApples(uid, applyIds);
    }

    //好友申请未读数
    @Override
    public FriendUnreadResp unread(Long uid) {
        Integer unReadCount = userApplyDao.getUnReadCount(uid);
        return new FriendUnreadResp(unReadCount);
    }

    //同意好友申请，同样采用分布式锁
    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid")
    public void applyApprove(Long uid, FriendApproveReq request) {
        UserApply userApply = userApplyDao.getById(request.getApplyId());
        AssertUtil.isNotEmpty(userApply,"没有此好友申请哦");
        AssertUtil.equal(userApply.getTargetId(), uid, "不存在申请记录");
        AssertUtil.equal(userApply.getStatus(), ApplyStatusEnum.WAIT_APPROVAL.getCode(), "已同意好友申请");
        //先进行更新申请表的状态，完成更细申请表的审判状态和已读未读状态,同意申请
        boolean b = userApplyDao.applyApprove(uid, request.getApplyId());
        //下面建立两个用户的关系，插入两条朋友记录
        UserFriend build1 = UserFriend.builder().uid(uid).friendUid(userApply.getUid()).deleteStatus(DeleteStatusEnum.Normal.getCode()).build();
        UserFriend build2 = UserFriend.builder().uid(userApply.getUid()).friendUid(uid).deleteStatus(DeleteStatusEnum.Normal.getCode()).build();
        boolean save = userFriendDao.save(build1);
        boolean save1 = userFriendDao.save(build2);

        //todo 创立二者的私聊房间并且发送消息  创建一个聊天房间
//        RoomFriend roomFriend = roomService.createFriendRoom(Arrays.asList(uid, userApply.getUid()));
        //发送一条同意消息。。我们已经是好友了，开始聊天吧
//        chatService.sendMsg(MessageAdapter.buildAgreeMsg(roomFriend.getRoomId()), uid);


    }
}
