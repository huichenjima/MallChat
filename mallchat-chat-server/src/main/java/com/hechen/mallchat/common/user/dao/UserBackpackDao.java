package com.hechen.mallchat.common.user.dao;

import com.hechen.mallchat.common.common.domain.enums.YesOrNoEnum;
import com.hechen.mallchat.common.user.domain.entity.UserBackpack;
import com.hechen.mallchat.common.user.domain.enums.ItemTypeEnum;
import com.hechen.mallchat.common.user.domain.vo.resp.BadgeResp;
import com.hechen.mallchat.common.user.mapper.UserBackpackMapper;
import com.hechen.mallchat.common.user.service.IUserBackpackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户背包表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-03-20
 */
@Service
public class UserBackpackDao extends ServiceImpl<UserBackpackMapper, UserBackpack> {

    @Autowired
    private UserBackpackMapper userBackpackMapper;
    //统计当前uid用户下有多少张未使用的改名卡
    public Integer getCountByValidItemId(Long uid, Long itemId) {
        Integer count = this.lambdaQuery().eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .count();
        return count;

    }

    //获得当前用户的第一张有用的改名卡，没有找到返回null
    public UserBackpack getFirstValidItem(Long uid, Long itemId) {
        UserBackpack one = lambdaQuery().eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getItemId, itemId)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .orderByAsc(UserBackpack::getId)  //按照时间升序，最老的一张改名卡在最上面
                .last("limit 1")  //只取一条记录
                .one();
        return one;
    }

    public boolean useItem(UserBackpack item) {
        //乐观锁
        //在更新中加了状态判断，也就是是否改名卡已经被用了
        return lambdaUpdate().eq(UserBackpack::getId,item.getId())
                .eq(UserBackpack::getStatus,YesOrNoEnum.NO.getStatus())
                .set(UserBackpack::getStatus,YesOrNoEnum.YES.getStatus())
                .update();


    }
    //多表联查，找出当前用户拥有的徽章
    public List<BadgeResp> findUserBadges(Long uid, Integer type) {
       return userBackpackMapper.findBadgeResp(uid,type);
    }

    public List<UserBackpack> getByItemIds(Long uid,List<Long> ids) {
        //status是表示有没有该徽章的键，自己写的版本理解成了status表示用户佩戴状态所以很复杂
        return lambdaQuery().eq(UserBackpack::getUid, uid)
                .eq(UserBackpack::getStatus,YesOrNoEnum.NO.getStatus())
                .in(UserBackpack::getItemId, ids)
                .list();

    }
    //重载了 ，这里两个参数都是列表
    public List<UserBackpack> getByItemIds(List<Long> uids, List<Long> itemIds) {
        return lambdaQuery().in(UserBackpack::getUid, uids)
                .in(UserBackpack::getItemId, itemIds)
                .eq(UserBackpack::getStatus, YesOrNoEnum.NO.getStatus())
                .list();
    }

    public UserBackpack getByIdempotent(String idempotent) {
        return lambdaQuery().eq(UserBackpack::getIdempotent,idempotent).one();
    }

    public UserBackpack acquireItem(Long uid, Long itemId, String idempotent) {
        UserBackpack userBackpack = new UserBackpack();
        userBackpack.setItemId(itemId);
        userBackpack.setUid(uid);
        userBackpack.setIdempotent(idempotent);
        userBackpack.setStatus(YesOrNoEnum.NO.getStatus());
        boolean save = this.save(userBackpack);
        return userBackpack;
    }
}
