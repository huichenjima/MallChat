package com.hechen.mallchat.common.chat.dao;

import com.hechen.mallchat.common.chat.domain.entity.Contact;
import com.hechen.mallchat.common.chat.mapper.ContactMapper;
import com.hechen.mallchat.common.chat.service.IContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 会话列表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-04-21
 */
@Service
public class ContactDao extends ServiceImpl<ContactMapper, Contact> implements IContactService {

    public void refreshOrCreateActiveTime(Long roomId, List<Long> memberUidList, Long messageId, Date activeTime) {

        this.baseMapper.refreshOrCreateActiveTime(roomId,memberUidList,messageId,activeTime);

    }

    public Contact get(Long uid, Long roomId) {
        return lambdaQuery()
                .eq(Contact::getUid, uid)
                .eq(Contact::getRoomId, roomId)
                .one();
    }
}
