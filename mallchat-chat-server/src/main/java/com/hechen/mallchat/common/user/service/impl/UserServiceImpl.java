package com.hechen.mallchat.common.user.service.impl;

import com.hechen.mallchat.common.common.annotation.RedissonLock;
import com.hechen.mallchat.common.common.event.UserBlackEvent;
import com.hechen.mallchat.common.common.event.UserRegisterEvent;
import com.hechen.mallchat.common.common.utils.AssertUtil;
import com.hechen.mallchat.common.user.dao.*;
import com.hechen.mallchat.common.user.domain.dto.ItemInfoDTO;
import com.hechen.mallchat.common.user.domain.dto.SummeryInfoDTO;
import com.hechen.mallchat.common.user.domain.entity.*;
import com.hechen.mallchat.common.user.domain.enums.BlackTypeEnum;
import com.hechen.mallchat.common.user.domain.enums.ItemEnum;
import com.hechen.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.hechen.mallchat.common.user.domain.vo.req.BlackReq;
import com.hechen.mallchat.common.user.domain.vo.req.ItemInfoReq;
import com.hechen.mallchat.common.user.domain.vo.req.SummeryInfoReq;
import com.hechen.mallchat.common.user.domain.vo.resp.BadgeResp;
import com.hechen.mallchat.common.user.domain.vo.resp.UserInfoResp;
import com.hechen.mallchat.common.user.service.UserService;
import com.hechen.mallchat.common.user.service.adapter.UserAdapter;
import com.hechen.mallchat.common.user.service.cache.ItemCache;
import com.hechen.mallchat.common.user.service.cache.UserCache;
import com.hechen.mallchat.common.user.service.cache.UserSummaryCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ClassName: UserServiceImpl
 * Package: com.hechen.mallchat.common.user.service.impl
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/15 15:53
 * @Version 1.0
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserBackpackDao userBackpackDao;

    @Autowired
    private ItemConfigDao itemConfigDao;

    @Autowired
    private ItemCache itemCache;
    //发送消息有两种方式，mq和spring自己的发送消息方式

    @Autowired
    private UserCache userCache;

    @Autowired
    private UserSummaryCache userSummaryCache;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private BlackDao blackDao;


    @Override
    public List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req) {
        //需要前端同步的uid ，也就是需要更新缓存的uid列表
        List<Long> uidList = getNeedSyncUidList(req.getReqList());
        //加载用户信息
        Map<Long, SummeryInfoDTO> batch = userSummaryCache.getBatch(uidList);
        return req.getReqList()
                .stream()
                .map(a -> batch.containsKey(a.getUid()) ? batch.get(a.getUid()) : SummeryInfoDTO.skip(a.getUid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemInfoDTO> getItemInfo(ItemInfoReq req) {//简单做，更新时间可判断被修改。没有用到更新时间，因为徽章数量小
        return req.getReqList().stream().map(a -> {
            ItemConfig itemConfig = itemCache.getById(a.getItemId()); //这里徽章信息缓存获取不是使用redis而是使用springcache
            if (Objects.nonNull(a.getLastModifyTime()) && a.getLastModifyTime() >= itemConfig.getUpdateTime().getTime()) {
                return ItemInfoDTO.skip(a.getItemId());
            }
            ItemInfoDTO dto = new ItemInfoDTO();
            dto.setItemId(itemConfig.getId());
            dto.setImg(itemConfig.getImg());
            dto.setDescribe(itemConfig.getDescribe());
            return dto;
        }).collect(Collectors.toList());
    }
    //sync是同步的意思，获取需要同步的uid列表
    private List<Long> getNeedSyncUidList(List<SummeryInfoReq.infoReq> reqList) {
        List<Long> needSyncUidList = new ArrayList<>();
        //获取缓存中的用户的最后刷新时间
        List<Long> userModifyTime = userCache.getUserModifyTime(reqList.stream().map(SummeryInfoReq.infoReq::getUid).collect(Collectors.toList()));
        //下面判断用户是否进行了更新，前端的缓存才需要进行更新
        for (int i = 0; i < reqList.size(); i++) {
            SummeryInfoReq.infoReq infoReq = reqList.get(i);
            Long modifyTime = userModifyTime.get(i);
            if (Objects.isNull(infoReq.getLastModifyTime()) || (Objects.nonNull(modifyTime) && modifyTime > infoReq.getLastModifyTime())) {
                needSyncUidList.add(infoReq.getUid());
            }
        }
        return needSyncUidList;
    }


    @Override
    @Transactional
    public Long register(User insert) {
        //todo 后序有其他业务 比如新注册用户送改名卡之类，涉及到多个数据库的操作，这里要有事务
        userDao.save(insert);
        //发送用户注册的事件,发送者
        applicationEventPublisher.publishEvent(new UserRegisterEvent(this,insert));

        //返回用户的uid
        return insert.getId();

    }

    @Override
    public UserInfoResp getUserInfo(Long uid) {
        //查询用户基本信息
        User user= userDao.getById(uid);
        //查询改名卡张数
        Integer modifyNameChance = userBackpackDao.getCountByValidItemId(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        return UserAdapter.buildUserInfo(user,modifyNameChance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid")
    public void modifyName(Long uid, String name) {
//        名字不能重复,重复则返回
        User oldUser = userDao.getByName(name);
        //断言简化异常抛出处理
        AssertUtil.isEmpty(oldUser,"名字已经被抢占了，请换一个");
        //先判断有没有改名卡了，查询第一张改名卡
        UserBackpack modifyNameItem = userBackpackDao.getFirstValidItem(uid, ItemEnum.MODIFY_NAME_CARD.getId());
        AssertUtil.isNotEmpty(modifyNameItem,"改名卡不够了，等后序活动送改名卡吧");
        //使用改名卡进行改名
        boolean success = userBackpackDao.useItem(modifyNameItem);
        if (success)
        {
            //再进行改名，
            userDao.modifyName(uid,name);
            //因为修改了用户名，删除用户缓存并且更新用户最后修改时间
            userCache.userInfoChange(uid);
        }
        //结束
    }

    @Override
    //本地缓存 ，不需要远端跟redis进行io操作
    public List<BadgeResp> badges(Long uid) {
        //通过缓存获取了所有的徽章信息
        List<ItemConfig> itemConfigs = itemCache.getByType(ItemTypeEnum.BADGE.getType());
        //查询用户拥有徽章
        List<UserBackpack> backpacks = userBackpackDao.getByItemIds(uid, itemConfigs.stream().map(ItemConfig::getId).collect(Collectors.toList()));
        //查询用户佩戴的徽章
        User user = userDao.getById(uid);

        return UserAdapter.buildBadgeResp(itemConfigs,backpacks,user);


    }
    //自己写的 有问题，因为把itemconfig的status当做是否佩戴了实际上是表示是否拥有该徽章，不过写的多表联查可以参考语法都是正确的
//    @Override
//    //本地缓存 ，不需要远端跟redis进行io操作
//    public List<BadgeResp> badges(Long uid) {
//        //这里应该关联背包和物品表 差物品表的描述
//
//        //进行联查找出当前用户有的徽章和使用情况
//        List<BadgeResp> hasBadgeResps =userBackpackDao.findUserBadges(uid,ItemTypeEnum.BADGE.getType());
//        //查找剩余没有的徽章
//        List<Long> longList = hasBadgeResps.stream().map(BadgeResp::getId).collect(Collectors.toList());
//        List<ItemConfig> retainItemconfig=itemConfigDao.findhasNoIdList(longList,ItemTypeEnum.BADGE.getType());
//        //设置为拥有与穿戴为0
//        List<BadgeResp> retainBadgeResps = retainItemconfig.stream().map(itemConfig -> BeanUtil.copyProperties(itemConfig, BadgeResp.class)).collect(Collectors.toList());
//        retainBadgeResps.forEach(retainBadgeResp->{retainBadgeResp.setObtain(0);retainBadgeResp.setWearing(0);});
//
//        //合并
//        hasBadgeResps.addAll(retainBadgeResps);
//
//        return hasBadgeResps;
//
//    }

    //佩戴徽章功能
    @Override
    public void wearingBadge(Long uid, Long itemId) {
        //确保有徽章
        UserBackpack firstValidItem = userBackpackDao.getFirstValidItem(uid, itemId);
        AssertUtil.isNotEmpty(firstValidItem,"您还没有这个徽章，快速获得吧！");
        //保证是徽章而不是改名卡
        ItemConfig itemConfig = itemConfigDao.getById(firstValidItem.getId());
        AssertUtil.equal(itemConfig.getType(),ItemTypeEnum.BADGE.getType(),"只有徽章才可以佩戴徽章哦");
        //保证了用户有当前徽章，进行佩戴即更新user表
        boolean b = userDao.wearingBadge(uid, itemId);
        //因为更新了用户佩戴徽章，所以删除缓存并且修改用户最后刷新时间
        userCache.userInfoChange(uid);
    }

    //拉黑用户
    @Override
    @Transactional
    public void black(BlackReq req) {
        //拉黑目标的uid
        Long uid=req.getUid();
        Black black = new Black();
        black.setTarget(uid.toString());
        black.setType(BlackTypeEnum.UID.getType());
        Black old = blackDao.getbyUid(uid.toString());
        AssertUtil.isEmpty(old,"已经拉黑过了该用户哦");
        blackDao.save(black);
        //拉黑用户的iP
        User user = userDao.getById(uid);
        if (Objects.nonNull(user.getIpInfo()))//这里要保证有ip信息，不然会空指针异常
        {
            blackIp(user.getIpInfo().getCreateIp());
            if (user.getIpInfo().getCreateIp()!=user.getIpInfo().getUpdateIp())
                blackIp(user.getIpInfo().getUpdateIp());
        }

        //拉黑完成,推送消息
        applicationEventPublisher.publishEvent(new UserBlackEvent(this,user));


    }

    private void blackIp(String ip) {
        if (Objects.isNull(ip))
            return;
        try {
            Black black = new Black();
            black.setType(BlackTypeEnum.IP.getType());
            black.setTarget(ip);
            blackDao.save(black);
        } catch (Exception e) {
            log.info("重复进行拉黑了哦");

        }
    }
}
