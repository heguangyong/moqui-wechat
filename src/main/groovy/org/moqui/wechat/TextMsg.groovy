package org.moqui.wechat

import groovy.xml.MarkupBuilder

class TextMsg {
    String ToUserName
    String FromUserName
    String CreateTime
    String MsgType = 'text'
    String Content

    TextMsg(xmlData) {
        this.ToUserName = xmlData.ToUserName.text()
        this.FromUserName = xmlData.FromUserName.text()
        this.CreateTime = xmlData.CreateTime.text()
        this.Content = xmlData.Content.text()
    }

    TextMsg(String toUser, String fromUser, String content) {
        this.ToUserName = toUser
        this.FromUserName = fromUser
        this.Content = content
        this.CreateTime = (System.currentTimeMillis() / 1000).toString()
    }

    String send() {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.xml {
            ToUserName(ToUserName)
            FromUserName(FromUserName)
            CreateTime(CreateTime)
            MsgType(MsgType)
            Content(Content)
        }
        return writer.toString()
    }
}
