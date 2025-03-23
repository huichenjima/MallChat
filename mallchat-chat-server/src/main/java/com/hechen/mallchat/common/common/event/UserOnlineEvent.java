package com.hechen.mallchat.common.common.event;

import com.hechen.mallchat.common.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * ClassName: UserOnlineEvent
 * Package: com.hechen.mallchat.common.common.event
 * Description:
 *
 * @Author 何琛
 * @Create 2025/3/22 16:26
 * @Version 1.0
 */
@Getter
public class UserOnlineEvent extends ApplicationEvent {
    private User user;

    public UserOnlineEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
