package com.hechen.mallchat.common.chat.dao;

import com.hechen.mallchat.common.chat.domain.entity.Room;
import com.hechen.mallchat.common.chat.mapper.RoomMapper;
import com.hechen.mallchat.common.chat.service.IRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-04-01
 */
@Service
public class RoomDao extends ServiceImpl<RoomMapper, Room>  {

    public void refreshActiveTime(Long roomId, Long msgId, Date msgTime) {
        lambdaUpdate()
                .eq(Room::getId , roomId)
                .lt(Room::getLastMsgId,msgId) //保证最后一条消息是正确的，防止消息回滚覆盖掉，请注意初始化房间lastMsgId设置为0，不然为null直接报错
                .set(Room::getLastMsgId,msgId)
                .set(Room::getActiveTime,msgTime)
                .update();
    }
}
