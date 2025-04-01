package com.hechen.mallchat.common.chat.dao;

import com.hechen.mallchat.common.chat.domain.entity.RoomFriend;
import com.hechen.mallchat.common.chat.mapper.RoomFriendMapper;
import com.hechen.mallchat.common.chat.service.IRoomFriendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 单聊房间表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/huichenjima">hechen</a>
 * @since 2025-04-01
 */
@Service
public class RoomFriendDao extends ServiceImpl<RoomFriendMapper, RoomFriend> {

}
