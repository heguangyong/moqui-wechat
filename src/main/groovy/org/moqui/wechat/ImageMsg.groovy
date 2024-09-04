package org.moqui.wechat

import groovy.xml.MarkupBuilder

class ImageMsg {
    String ToUserName
    String FromUserName
    String CreateTime
    String MsgType = 'image'
    String MediaId

    ImageMsg(xmlData) {
        this.ToUserName = xmlData.ToUserName.text()
        this.FromUserName = xmlData.FromUserName.text()
        this.CreateTime = xmlData.CreateTime.text()
        this.MediaId = xmlData.MediaId.text()
    }

    ImageMsg(String toUser, String fromUser, String mediaId) {
        this.ToUserName = toUser
        this.FromUserName = fromUser
        this.MediaId = mediaId
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
            Image {
                MediaId(MediaId)
            }
        }
        return writer.toString()
    }
}
