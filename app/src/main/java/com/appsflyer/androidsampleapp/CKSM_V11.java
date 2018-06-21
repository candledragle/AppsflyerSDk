package com.appsflyer.androidsampleapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2018/6/2.
 *
 * @author sym
 */

public class CKSM_V11 {

    private static char[] field_arr_01 =
            new char[]{'닁', '\ue623', 'ᬞ', '䰕', '\ue17b', 'ᩨ', '佊', '\ue0f5', 'ᖼ', '亐', '\ue38e',
                    'ᓪ', '䧌', '\ue2c1', 'ᘹ', '䬭', 'ﰉ', 'ᄳ', '䩞', 'ｒ', '၈', '䖴', 'ﺎ', 'ᎃ', '䓷', '匿',
                    'ዋ', '䘒', 'שּׂ', 'Ⱇ', '䄇', '充', '⽥', '䁟', '1', '0', 'y', '咔', 'ꦣ', 'ﺾ', '厙', '꣬',
                    'ﷃ', '剖', '꜌', 'ﰱ', '儝', 'ꙧ', 'ﭔ', '偤', '꒛', '戀', '亣', 'ꏧ', '빋', '\ueaa5', 'ទ',
                    '䂋', '\uedfc', '᛭', '䎗', '\uec67', '\u1978', '䈘', '\uef5e', 'ᠵ', '䕔', '\uee42',
                    '\u1ae6', '䟹', '\uf0cd', '\u1df2', '䚷', '\uf3db', '\u1c99', '䤽', '\uf243', 'ὒ',
                    '䡵', '\uf535', 'Ḋ', '䪰', '\uf7f7', '\u20c0', '䷞', '\uf6ab', '뭢', '\uef80', 'ኽ',
                    '䖶', '\ue8d8', 'Ꮛ', '䛩', '\ue956', 'ᰄ', '䜥', '\uea6f', 'ᵮ', '䁾', '\ueb7e', 'ᾁ',
                    '䊅', '\uf5a1', 'ᣇ', '䏤', '\uf6f5', '᧩', '䰓', '\uf73a', 'ᨭ', '䵉', '㣍', '氯', '鄒',
                    '옙', '歷', '遤', '앆', '櫹', '龬', '쒘', '榜', '黧', '쏇', '棄', '鰨', '섪', '癒', '魂', '쁣',
                    '畕', '驛', '쾲', '璀', 'ሇ', '䛥', '믘', '\uecd3', '䆽', '몮', '\uef8c', '䀳', '땡',
                    '\uee40', '䌊', '됈', '\ue909', '䈖', '뛾', '\uebe6', '峢', '놺', '\ueabf', '徚', 'a',
                    '咃', 'ꦾ', 'ﺵ', '叛', '\ua8c8', '\ufdea', '剕', '꜆', 'ﰰ', '儶', 'ꘁ', 'שּ', '偻', '꒟',
                    '.', '\\', '哃', '\ueb41', '뾧', '䊇', 'ᖍ', '뢱', '䏣', 'ᛊ', '륾', '䰷', 'ᜓ', '먙', '䵠',
                    'ၞ', '뭐', '뱇', '\ue8e6', 'ᗓ', '䋌', '\uefb4', 'ᒬ', '䘇', 'ዮ', '\uefdc', '뢲', 'ᗮ',
                    '\ueea2', '뮏', 'ᑼ', '\ue16b', '멕', 'C', '咅', 'ꦿ', 'ﺤ', '叟', '꣒', 'ﷻ', '刖', 'ꜭ',
                    'ﰭ', '儡', 'Ꙋ', 'ﭬ', '偽', '꒟', '歷', '亾', '\uefc8', '묂', '䘽', 'ᄥ', '뱟', '䝋', 'ሠ',
                    '북', '䢉', '\u13fb', '뺫', '䧄', 'ᓦ', '뾧', '䬛', 'ᘌ', 'ꄽ', '䱛', 'ᝁ', 'ꈹ', '䵺', 'ᢞ',
                    'ꎤ', '亭', 'ᦖ', 'ꓜ', '俵', 'ᬅ', 'ꘊ', '煷', 'ᰭ', 'ꝅ', '牍', 'ᵦ', 'ꢄ', '玝', 'ẳ', 'ꦠ',
                    '瓎', 'ᾯ', 'ꪦ', 'g', '咈', 'ꦮ', 'ﺎ', '叚', '꣒', 'ﷺ', '刚', '꜄', 'ﰹ', '儧', 'ꙋ', 'ﭝ',
                    '偹', 'ꒆ', '憐', '亹', 'ꏞ', '\uf8cb', '䷣', 'ꋭ', '\uf71e', '䰰', 'ꄸ', 'C', '咅', 'ꦿ',
                    'ﺤ', '叟', '꣒', 'ﷻ', '刖', 'Ꜻ', 'ﰰ', '儤', 'ꙃ', 'ﭹ', '偪', 'ꒂ', '簾', '亨', 'ꏞ',
                    '\uf8cf', '䷧', 'ꋰ', '\uf718', '䰱', 'ꄥ', '戹', '㛳', '쯌', '鳔', 'ㆮ', '쪺', '\u9fd1',
                    'ね', '앹', '鹜', '㍒', '쐻', '餆', '㉖', '웻', '鯹', 'ⳉ', '솮', '骰', '⾋', '삏', '镫',
                    '\u2e45', '쌔', '鐪', '⤿', '숙', '雨', '\u2bfc', 'ﳂ', '醙', '⪻', 'ﾶ', '邆', '╭', '︸',
                    '鍎', '⑆', '爐', '鈁', '✇', 'ﯾ', '賴', '⇟', '節', '迬', '\u20c9'};
    private static long filed_l_01 = -285441353350097683L;
    private static int filed_i_01 = 0;
    private static int field_01 = 1;

    static String generate(Context var0, long var1) {
        int var10000 = 2 % 2;
        StringBuilder var3 = new StringBuilder();
        StringBuilder var4 = new StringBuilder();
        StringBuilder var5 = new StringBuilder();
        int var10001;
        String var17;
        if (ˊ(ˋ(34, '늠', 0).intern())) {
            var10001 = field_01 + 61;
            filed_i_01 = (field_01 + 61) % 128;
            var17 = var10001 % 2 != 0 ? ˋ(0, '\u0000', 88).intern() : ˋ(1, '\u0000', 34).intern();
        } else {
            var17 = ˋ(1, '\u0000', 35).intern();
        }

        var4.append(var17);
        StringBuilder var10 = new StringBuilder();
        String var7;
        String var8 = ˎ(var7 = var0.getPackageName());
        var4.append(ˋ(1, '\u0000', 34).intern());
        var10.append(var8);
        if (ˋ(var0) == null) {
            var4.append(ˋ(1, '\u0000', 35).intern());
            var10.append(var7);
        } else {
            var4.append(ˋ(1, '\u0000', 34).intern());
            var10.append(var7);
            var10001 = 2 % 2;
        }

        switch ((var8 = ˊ(var0)) == null ? 1 : 0) {
            case 0:
                var4.append(ˋ(1, '\u0000', 34).intern());
                var10.append(var8);
                var10001 = 2 % 2;
                break;
            case 1:
            default:
                var4.append(ˋ(1, '\u0000', 35).intern());
                var10.append(var7);
        }

        //todo
        // var10.append(ॱ(var0, var7));  // 不太清楚的地方
        var3.append(var10.toString());

        try {
            long var16 = var0.getPackageManager().getPackageInfo(var0.getPackageName(),
                    0).firstInstallTime;
            String var6 = ˋ(18, '\u0000', 36).intern();
            SimpleDateFormat var15;
            (var15 = new SimpleDateFormat(var6, Locale.US)).setTimeZone(
                    TimeZone.getTimeZone("UTC"));
            var3.append(var15.format(new Date(var16)));
        } catch (PackageManager.NameNotFoundException var11) {
            return ˋ(32, '븩', 54).intern();
        }

        var10000 = filed_i_01 + 63;
        field_01 = (filed_i_01 + 63) % 128;
        switch (var10000 % 2 == 0 ? 82 : 40) {
            case 40:
                var10000 = 2 % 2;
                break;
            case 82:
            default:
                var10000 = 4 % 5;
        }

        var3.append(var1);
        if (ˊ(ˋ(25, '묃', 86).intern())) {
            var17 = ˋ(1, '\u0000', 34).intern();
        } else {
            var17 = ˋ(1, '\u0000', 35).intern();
            int var10002 = field_01 + 105;
            filed_i_01 = (field_01 + 105) % 128;
            if (var10002 % 2 != 0) {
                ;
            }

            var10002 = 2 % 2;
        }

        var5.append(var17);
        switch (ˊ(ˋ(23, '㢬', 111).intern()) ? 1 : 0) {
            case 0:
            default:
                var17 = ˋ(1, '\u0000', 35).intern();
                break;
            case 1:
                var10001 = filed_i_01 + 95;
                field_01 = (filed_i_01 + 95) % 128;
                if (var10001 % 2 == 0) {
                    ;
                }

                var17 = ˋ(1, '\u0000', 34).intern();
        }

        var5.append(var17);
        if (ˊ(ˋ(20, 'ቦ', 134).intern())) {
            var10001 = filed_i_01 + 117;
            field_01 = (filed_i_01 + 117) % 128;
            switch (var10001 % 2 == 0 ? 1 : 86) {
                case 1:
                default:
                    var17 = ˋ(0, '\u0000', 121).intern();
                    break;
                case 86:
                    var17 = ˋ(1, '\u0000', 34).intern();
            }
        } else {
            var17 = ˋ(1, '\u0000', 35).intern();
        }

        var5.append(var17);
        switch (ˊ(ˋ(15, '\u0000', 154).intern()) ? 85 : 10) {
            case 10:
            default:
                var17 = ˋ(1, '\u0000', 35).intern();
                break;
            case 85:
                var10001 = filed_i_01 + 7;
                field_01 = (filed_i_01 + 7) % 128;
                if (var10001 % 2 == 0) {
                    ;
                }

                var17 = ˋ(1, '\u0000', 34).intern();
        }

        var5.append(var17);
        String var18 = q.ˊ(q.ˋ(var3.toString()));
        String var14 = var4.toString();
        String var12 = var18;
        StringBuilder var13;
        (var13 = new StringBuilder(var12)).setCharAt(17,
                Integer.toString(Integer.parseInt(var14, 2), 16).charAt(0));
        var18 = var13.toString();
        var14 = var5.toString();
        var12 = var18;
        (var13 = new StringBuilder(var12)).setCharAt(27,
                Integer.toString(Integer.parseInt(var14, 2), 16).charAt(0));
        return ॱ(var13.toString(), var1);
    }


    private static String ॱ(String var0, Long var1) {
        int var10000 = 2 % 2;
        var10000 = field_01 + 19;
        filed_i_01 = (field_01 + 19) % 128;
        if (var10000 % 2 != 0) {
            ;
        }

        if (var0 != null && var1 != null) {
            var10000 = filed_i_01 + 121;
            field_01 = (filed_i_01 + 121) % 128;
            if (var10000 % 2 == 0) {
                ;
            }

            if (var0.length() == 32) {
                StringBuilder var6 = new StringBuilder(var0);
                String var7 = var1.toString();
                long var4 = 0L;
                int var2 = 0;
                int var3 = 0;
                var10000 = filed_i_01 + 9;
                field_01 = (filed_i_01 + 9) % 128;
                if (var10000 % 2 == 0) {
                    var10000 = 4 * 5;
                } else {
                    var10000 = 2 % 2;
                }

                while (true) {
                    switch (var3 < var7.length() ? 1 : 0) {
                        case 0:
                        default:
                            String var9 = Integer.toHexString(var2);
                            var6.replace(7, 7 + var9.length(), var9);
                            int var8 = 0;
                            var10000 = 2 % 2;

                            while (var8 < var6.length()) {
                                var10000 = field_01 + 111;
                                filed_i_01 = (field_01 + 111) % 128;
                                if (var10000 % 2 != 0) {
                                    var4 /= (long) Character.getNumericValue(var6.charAt(var8));
                                    var8 += 19;
                                } else {
                                    var4 += (long) Character.getNumericValue(var6.charAt(var8));
                                    ++var8;
                                }
                            }

                            while (var4 > 100L) {
                                var4 %= 100L;
                                var10000 = 2 % 2;
                            }

                            var6.insert(23, (int) var4);
                            switch (var4 < 10L ? 0 : 1) {
                                case 0:
                                default:
                                    var6.insert(23, ˋ(1, '\u0000', 35).intern());
                                case 1:
                                    return var6.toString();
                            }
                        case 1:
                            var10000 = filed_i_01 + 47;
                            field_01 = (filed_i_01 + 47) % 128;
                            if (var10000 % 2 == 0) {
                                ;
                            }

                            var2 += Character.getNumericValue(var7.charAt(var3));
                            ++var3;
                    }
                }
            }
        }

        return ˋ(32, '븩', 54).intern();
    }

    /**
     * 2018-06-15 15:57:57.193 29168-29235/com.wifiup E/SYM: [ +++++ ]  android.telephony.TelephonyManager
     * 2018-06-15 15:57:57.209 29168-29235/com.wifiup E/SYM: [ +++++ ]  android.os.BatteryManager
     * 2018-06-15 15:57:57.209 29168-29235/com.wifiup E/SYM: [ +++++ ]  android.hardware.Sensor
     * 2018-06-15 15:57:57.209 29168-29235/com.wifiup E/SYM: [ +++++ ]  android.os.AsyncTask
     * 2018-06-15 15:57:57.209 29168-29235/com.wifiup E/SYM: [ +++++ ]  android.net.Uri
     *
     * @param p0
     * @return
     */
    // 判断指定的类是否存在
    private static boolean ˊ(String p0) {

        MainActivity.SLog.e(" ****** ", p0);
        try {
            Class.forName(p0);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static String ˎ(String var0) {
        int var10000 = 2 % 2;
        var10000 = field_01 + 23;
        filed_i_01 = (field_01 + 23) % 128;
        if (var10000 % 2 != 0) {
            ;
        }

        if (!var0.contains(ˋ(1, '\u0000', 169).intern())) {
            var10000 = field_01 + 65;
            filed_i_01 = (field_01 + 65) % 128;
            int var10001;
            String var5;
            switch (var10000 % 2 != 0 ? 59 : 20) {
                case 20:
                default:
                    var5 = var0;
                    break;
                case 59:
                    var5 = var0;
                    var10001 = ((Object[]) null).length;
            }

            var10001 = field_01 + 3;
            filed_i_01 = (field_01 + 3) % 128;
            if (var10001 % 2 != 0) {
                return var5;
            } else {
                return var5;
            }
        } else {
            String[] var4;
            int var1 = (var4 = var0.split(ˋ(2, '\u0000', 170).intern())).length;
            StringBuilder var2;
            (var2 = new StringBuilder()).append(var4[var1 - 1]).append(
                    ˋ(1, '\u0000', 169).intern());
            int var3 = 1;

            while (true) {
                switch (var3 < var1 - 1 ? 78 : 19) {
                    case 19:
                    default:
                        var2.append(var4[0]);
                        return var2.toString();
                    case 78:
                        var2.append(var4[var3]).append(ˋ(1, '\u0000', 169).intern());
                        ++var3;
                        var10000 = 2 % 2;
                }
            }
        }
    }


    //方法二
    private static String ˋ(Context context) {
        // $FF: Couldn't be decompiled

        String ret = null;

        Properties v0 = System.getProperties();
        int v1 = 0xe;
        char v2 = 0xeb2b;
        int v3 = 0xac;
        String v1_1 = ˋ(v1, v2, v3).intern();
        boolean flag = v0.containsKey(v1_1);//判断是否包含该属性

        String cachePath = context.getCacheDir().getPath(); //获取缓存路径
        int v1_0x6 = 0x6;
        char v3_0xbc68 = 0xbc68;
        int v4_0xba = 0xba;
        String intern = ˋ(v1_0x6, v3_0xbc68, v4_0xba).intern();
        String input = cachePath.replace(intern, "");

        int p1 = 0xa;
        char p2 = 0x4629;
        int p3 = 0xc0;
        String regex = ˋ(p1, p2, p3).intern(); //获取正则表达式

        int pV3 = 0x1;
        Matcher matcher = Pattern.compile(regex).matcher(input);


        return ret;
    }

    //todo
    private static String ˊ(Context context) {
        // $FF: Couldn't be decompiled
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 1);
            return packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取到字符串
     * @param var0
     * @param var1
     * @param var2
     * @return
     */
    private static String ˋ(int var0, char var1, int var2) {

        MainActivity.SLog.e(" ******* ", var0 + " : " + var1 + " : " + var2);

        int var10000 = 2 % 2;
        var10000 = field_01 + 125;
        filed_i_01 = (field_01 + 125) % 128;
        char[] var3;
        int var4;
        switch (var10000 % 2 != 0 ? 41 : 24) {
            case 24:
            default:
                var3 = new char[var0];
                var4 = 0;
                break;
            case 41:
                var3 = new char[var0];
                var4 = 0;
        }

        var10000 = 2 % 2;

        while (true) {
            switch (var4 < var0 ? 40 : 96) {
                case 40:
                    var3[var4] = (char) ((int) ((long) field_arr_01[var2 + var4]
                            ^ (long) var4 * filed_l_01 ^ (long) var1));
                    ++var4;
                    var10000 = field_01 + 13;
                    filed_i_01 = (field_01 + 13) % 128;
                    if (var10000 % 2 != 0) {
                    }

                    var10000 = 2 % 2;
                    break;
                case 96:
                default:
                    String var5 = new String(var3);
                    int var10001 = field_01 + 117;
                    filed_i_01 = (field_01 + 117) % 128;
                    if (var10001 % 2 != 0) {
                    }

                    MainActivity.SLog.e(" ****** ", var5);
                    return var5;
            }
        }
    }

    public void sayHello() {
        Log.e("SYM", "say hello ");
    }

}
