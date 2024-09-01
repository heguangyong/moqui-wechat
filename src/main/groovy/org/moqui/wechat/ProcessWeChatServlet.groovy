package org.moqui.wechat

import groovy.transform.CompileStatic;
import org.apache.commons.codec.digest.DigestUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.util.Base64;

@CompileStatic
public class ProcessWeChatServlet extends HttpServlet {
    protected final static Logger logger = LoggerFactory.getLogger(ProcessWeChatServlet.class)

    private static final String TOKEN = "moquiwechat";
    private static final String ENCODING_AES_KEY = "06MFgfg3RkV2CfT0lrrS9gZMQOJe5BIp55tNiBHIv26";
    private static final String IV = ENCODING_AES_KEY.substring(0, 16);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle WeChat URL verification
        String signature = req.getParameter("signature");
        String timestamp = req.getParameter("timestamp");
        String nonce = req.getParameter("nonce");
        String echostr = req.getParameter("echostr");

        if (verifySignature(signature, timestamp, nonce)) {
            resp.getWriter().write(echostr);
        } else {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle WeChat message callback
        String signature = req.getParameter("signature");
        String timestamp = req.getParameter("timestamp");
        String nonce = req.getParameter("nonce");
        String encryptedMsg = req.getParameter("encrypt");

        if (verifySignature(signature, timestamp, nonce)) {
            if (encryptedMsg != null) {
                try {
                    String decryptedMsg = decryptMessage(encryptedMsg);
                    // Log and handle the decrypted message
                    System.out.println("Decrypted message: " + decryptedMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                // Handle plain message if encryption is not used
                String message = req.getReader().lines().reduce("", String::concat);
                System.out.println("Received plain message: " + message);
            }

            resp.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
        } else {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private boolean verifySignature(String signature, String timestamp, String nonce) {
        // Signature verification using SHA1 hash
        String[] params = [TOKEN, timestamp, nonce]  // Groovy syntax for array declaration
        Arrays.sort(params)  // Sort the parameters alphabetically

        // Concatenate sorted strings
        String toHash = params.join("")  // In Groovy, .join() is a method directly on the array
        String calculatedSignature = DigestUtils.sha1Hex(toHash)  // Calculate SHA1 hash

        // Compare calculated signature with the received signature
        return calculatedSignature == signature
    }


    private String decryptMessage(String encryptedMsg) throws Exception {
        // Decrypt the message using AES
        byte[] aesKey = Base64.getDecoder().decode(ENCODING_AES_KEY + "=");
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedMsg);

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, "UTF-8").trim();
    }
}
