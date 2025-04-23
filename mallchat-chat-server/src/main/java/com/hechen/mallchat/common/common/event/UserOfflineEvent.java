package com.hechen.mallchat.common.common.event;

import com.hechen.mallchat.common.user.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * ClassName: UserOfflineEvent
 * Package: com.hechen.mallchat.common.common.event
 * Description:
 *
 * @Author 何琛
 * @Create 2025/4/23 10:47
 * @Version 1.0
 */
@Getter
public class UserOfflineEvent extends ApplicationEvent {
    private User user;

    public UserOfflineEvent(Object source, User user) {
        super(source);
        this.user = user;
    }
}
