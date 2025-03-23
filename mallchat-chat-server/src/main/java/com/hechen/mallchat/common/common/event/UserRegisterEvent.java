package com.hechen.mallchat.common.common.event;

import com.hechen.mallchat.common.user.domain.entity.User;
import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

/**
 * ClassName: UserRegisterEvent
 * Package: com.hechen.mallchat.common.common.enent
 * Description:用户注册事件
 *
 * @Author 何琛
 * @Create 2025/3/22 15:03
 * @Version 1.0
 */
@Getter
public class UserRegisterEvent extends ApplicationEvent {
    private User user;
    public UserRegisterEvent(Object source, User user) {
        super(source);
        this.user=user;
    }
}
