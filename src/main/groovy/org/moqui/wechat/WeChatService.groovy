package org.moqui.wechat

import org.moqui.context.ExecutionContext
import java.security.MessageDigest

class WeChatServices {
    static void processWeChatCallback(ExecutionContext ec) {
        // 获取微信回调的请求参数
        def requestParams = ec.web.getRequestParameters()

        // 微信回调的签名验证参数
        def signature = requestParams.get("signature")
        def timestamp = requestParams.get("timestamp")
        def nonce = requestParams.get("nonce")
        def echostr = requestParams.get("echostr")

        // 你在微信平台配置的 Token，应该从配置文件或安全存储中读取
        def token = "d70d79af4512e4c49b162c28810ec8c7"

        // 验证签名逻辑
        if (isSignatureValid(token, timestamp, nonce, signature)) {
            // 签名验证成功
            if (echostr) {
                // 如果包含 echostr，说明是初次验证，直接返回给微信服务器
                ec.web.response.setContentType("text/plain")
                ec.web.response.getWriter().write(echostr)
                ec.web.response.flushBuffer()
            } else {
                // 处理实际的消息或事件推送逻辑
                ec.message.add("WeChat Callback Received: ${requestParams}")
                // 在这里添加对事件推送的处理，比如解析 XML 消息并根据用户的操作执行相应的服务
            }
        } else {
            // 签名验证失败，返回错误响应
            ec.web.response.setContentType("text/plain")
            ec.web.response.getWriter().write("Invalid signature")
            ec.web.response.flushBuffer()
        }
    }

    // 签名验证方法
    static boolean isSignatureValid(String token, String timestamp, String nonce, String signature) {
        // 将 token、timestamp、nonce 排序并连接成一个字符串
        def tempArr = [token, timestamp, nonce].sort().join("")

        // 使用 SHA-1 算法进行加密
        def sha1Digest = MessageDigest.getInstance("SHA-1")
        def hashBytes = sha1Digest.digest(tempArr.bytes)
        def hashString = hashBytes.encodeHex().toString()

        // 返回签名验证结果
        return hashString == signature
    }
}
