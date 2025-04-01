package com.hechen.mallchat.common.chat.dao;

import com.hechen.mallchat.common.chat.domain.entity.RoomGroup;
import com.hechen.mallchat.common.chat.mapper.RoomGroupMapper;
import com.hechen.mallchat.common.chat.service.IRoomGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 群聊房间表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-04-01
 */
@Service
public class RoomGroupDao extends ServiceImpl<RoomGroupMapper, RoomGroup> {

}
