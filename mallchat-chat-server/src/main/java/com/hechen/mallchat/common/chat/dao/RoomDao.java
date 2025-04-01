package com.hechen.mallchat.common.chat.dao;

import com.hechen.mallchat.common.chat.domain.entity.Room;
import com.hechen.mallchat.common.chat.mapper.RoomMapper;
import com.hechen.mallchat.common.chat.service.IRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
