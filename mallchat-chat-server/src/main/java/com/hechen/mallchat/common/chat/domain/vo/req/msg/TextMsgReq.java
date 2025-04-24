package com.hechen.mallchat.common.chat.domain.vo.req.msg;

import cn.hutool.core.util.ReUtil;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Description: 文本消息入参
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-06-04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextMsgReq {

    @NotBlank(message = "内容不能为空")
    @Size(max = 1024, message = "消息内容过长，服务器扛不住啊，兄dei")
    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("回复的消息id,如果没有别传就好")
    private Long replyMsgId;

    //这个@出用户的操作是前端实现的，后端没有使用接口如elastsearch去找，这里是根据是之前的懒加载
    @ApiModelProperty("艾特的uid")
    @Size(max = 10, message = "一次别艾特这么多人")
    private List<Long> atUidList;

//    public static void main(String[] args) {
//        String content = "这是一个很长的字符串再来 www.github.com，其中包含一个URL www.baidu.com,, 一个带有端口号的URL http://www.jd.com:80, 一个带有路径的URL http://mallchat.cn, 还有美团技术文章https://mp.weixin.qq.com/s/hwTf4bDck9_tlFpgVDeIKg";
//        Pattern pattern = Pattern.compile("((http|https)://)?(www.)?([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?");
//        List<String> matchList = ReUtil.findAll(pattern, content, 0);//hutool工具类
//        System.out.println(matchList);
//    }

    public static void main(String[] args) throws IOException {
        Connection connect = Jsoup.connect("https://www.bilibili.com");
        //document文件就是网站的html页面
        Document document = connect.get();
        //获取标题
        String title = document.title();
        String attr = document.getElementsByAttributeValue("name", "description").attr("content");
        System.out.println(title+":"+attr);
    }
}
