package xyz.mercs.bluetoothlockdemo.util;

import com.cbj.sdk.libbase.utils.LOG;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author elijah
 * @date 2021/7/9
 * @Description
 */
public class Util {

    public static byte [] s_key = {
        0x3a,0x60,0x43,0x2a,0x5c,0x01,0x21,0x1f,0x29,0x1e,0x0f,0x4e,0x0c,0x13,0x28,0x25
    };


    /**
     * @Description 加密
     */
    public static byte [] encrypt(byte [] sSrc, byte [] sKey){
        try{
            SecretKeySpec skeySpec = new SecretKeySpec(sKey,"AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE,skeySpec);
            byte [] encrypted = cipher.doFinal(sSrc);
            return encrypted;
        } catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 解密
     * @param sSrc
     * @param sKey
     * @return
     */
    public static byte [] decrypt(byte [] sSrc,byte [] sKey){
        try{
            SecretKeySpec skeySpec = new SecretKeySpec(sKey,"AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,skeySpec);
            byte [] dncrypted = cipher.doFinal(sSrc);
            return dncrypted;
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static byte [] decryptMsg(byte [] src){
        byte [] buf = decrypt(src,s_key);
        return buf;
    }

    /**
     * @Description token
     */
    public static byte[] sendGetTokenProtocol(){
        byte [] buf = new byte[16];
        buf[0] = 0x06;
        buf[1] = 0x01;
        buf[2] = 0x01;
        buf[3] = 0x01;

        return encrypt(buf,s_key);
    }

    /**
     * @Description 开锁
     */
    public static byte [] openLock(byte [] token){
        byte [] buf = new byte[16];
        buf[0] = 0x05;
        buf[1] = 0x01;
        buf[2] = 0x06;
        buf[3] = 0x30;
        buf[4] = 0x30;
        buf[5] = 0x30;
        buf[6] = 0x30;
        buf[7] = 0x30;
        buf[8] = 0x30;
        buf[9] = token[0];
        buf[10] = token[1];
        buf[11] = token[2];
        buf[12] = token[3];


//        buf[0] = 0x05;
//        buf[1] = 0x01;
//        buf[2] = token[0];
//        buf[3] = token[1];
//        buf[4] = token[2];
//        buf[5] = token[3];
//        buf[6] = 0x06;
//        buf[7] = 0x30;
//        buf[8] = 0x30;
//        buf[9] = 0x30;
//        buf[10] = 0x30;
//        buf[11] = 0x30;
//        buf[12] = 0x30;



        return encrypt(buf,s_key);
    }

    /**
     * @Description  关锁
     */
    public static byte [] closeLock(byte [] token){
        byte [] buf = new byte[16];
        buf[0] = 0x05;
        buf[1] = 0x0c;
        buf[2] = 0x01;
        buf[3] = 0x01;
        buf[4] = token[0];
        buf[5] = token[1];
        buf[6] = token[2];
        buf[7] = token[3];

        return encrypt(buf,s_key);
    }

    /**
     * @Description 获取电量
     */
    public static byte [] getPower(byte [] token){
        byte [] buf = new byte[16];
        buf[0] = 0x02;
        buf[1] = 0x01;
        buf[2] = 0x01;
        buf[3] = 0x01;
        buf[4] = token[0];
        buf[5] = token[1];
        buf[6] = token[2];
        buf[7] = token[3];

        return encrypt(buf,s_key);
    }

    /**
     * @Description 获取状态
     */
    public static byte [] getStatus(byte [] token){
        byte [] buf = new byte[16];
        buf[0] = 0x05;
        buf[1] = 0x0e;
        buf[2] = 0x01;
        buf[3] = 0x01;
        buf[4] = token[0];
        buf[5] = token[1];
        buf[6] = token[2];
        buf[7] = token[3];
        return encrypt(buf,s_key);
    }

    /**
     * @Description 获取参数
     */
    public static byte [] getPam(byte [] token){
        LOG.INSTANCE.I("123","token="+Util.byte2HexStr(token));
        byte [] buf = new byte[16];
        buf[0] = 0x05;
        buf[1] = 0x4e;
        buf[2] = 0x01;
        buf[3] = 0x01;
        buf[4] = token[0];
        buf[5] = token[1];
        buf[6] = token[2];
        buf[7] = token[3];

        return encrypt(buf,s_key);
    }
















    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 字符串转换成十六进制字符串
     *
     * @param  str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String str2HexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    /**
     * 十六进制转换字符串
     *
     * @param hexStr str Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }



    /**
     * bytes转换成十六进制字符串
     *
     * @param  b byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String byte2HexStr(byte[] b) {
        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
//			sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param  src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStr2Bytes(String src) {
        int m = 0, n = 0;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     *
     * @param  strText 全角字符串
     * @return String 每个unicode之间无分隔符
     * @throws Exception
     */
    public static String strToUnicode(String strText) throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u" + strHex);
            else
                // 低位在前面补00
                str.append("\\u00" + strHex);
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     *
     * @param  hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }


    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data byte[]
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data        byte[]
     * @param toLowerCase <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
     * @return 十六进制char[]
     */
    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制char[]
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data byte[]
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data        byte[]
     * @param toLowerCase <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制String
     */
    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    /**
     * 将十六进制字符数组转换为字节数组
     *
     * @param data 十六进制char[]
     * @return byte[]
     * @throws RuntimeException 如果源十六进制字符数组是一个奇怪的长度，将抛出运行时异常
     */
    public static byte[] decodeHex(char[] data) {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * 将十六进制字符转换成一个整数
     *
     * @param ch    十六进制char
     * @param index 十六进制字符在字符数组中的位置
     * @return 一个整数
     * @throws RuntimeException 当ch不是一个合法的十六进制字符时，抛出运行时异常
     */
    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch
                    + " at index " + index);
        }
        return digit;
    }

}



