package com.example.testlib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyClass {

    static int a = 100;

    public static void main(String args[]) {

        System.out.println(Boolean.valueOf("0"));

        String cachePath = "/data/user/0/com.speed.booster.kim/cache";


        String test = test(cachePath);
        System.out.println(test);

    }


    //方法二
    private static String test(String cachePath) {
        // $FF: Couldn't be decompiled

        String ret =null;
        int v1_0x6 = 0x6;
        char v3_0xbc68 = 0xbc68;
        int v4_0xba = 0xba;
        String intern = ˋ(v1_0x6, v3_0xbc68, v4_0xba).intern();
        String input =cachePath.replace(intern,"");

        /*System.out.println(v1_0x6);
        System.out.println(v3_0xbc68);
        System.out.println(v4_0xba);*/
        System.out.println(intern);
        System.out.println(input);

        int p1 = 0xa;
        char p2 = 0x4629;
        int p3 = 0xc0;
        String regex = ˋ(p1, p2, p3).intern(); //获取正则表达式
        System.out.println(regex);

        int pV3 = 0x1;
        Matcher matcher = Pattern.compile(regex).matcher(input);

        if(matcher.find()){
            ret = matcher.group(pV3);
        }

        return ret;
    }

    private static int filed_i_01 = 0;
    private static int field_01 = 1;
    private static char[] field_arr_01 = new char[]{'닁', '\ue623', 'ᬞ', '䰕', '\ue17b', 'ᩨ', '佊', '\ue0f5', 'ᖼ', '亐', '\ue38e', 'ᓪ', '䧌', '\ue2c1', 'ᘹ', '䬭', 'ﰉ', 'ᄳ', '䩞', 'ｒ', '၈', '䖴', 'ﺎ', 'ᎃ', '䓷', '匿', 'ዋ', '䘒', 'שּׂ', 'Ⱇ', '䄇', '充', '⽥', '䁟', '1', '0', 'y', '咔', 'ꦣ', 'ﺾ', '厙', '꣬', 'ﷃ', '剖', '꜌', 'ﰱ', '儝', 'ꙧ', 'ﭔ', '偤', '꒛', '戀', '亣', 'ꏧ', '빋', '\ueaa5', 'ទ', '䂋', '\uedfc', '᛭', '䎗', '\uec67', '\u1978', '䈘', '\uef5e', 'ᠵ', '䕔', '\uee42', '\u1ae6', '䟹', '\uf0cd', '\u1df2', '䚷', '\uf3db', '\u1c99', '䤽', '\uf243', 'ὒ', '䡵', '\uf535', 'Ḋ', '䪰', '\uf7f7', '\u20c0', '䷞', '\uf6ab', '뭢', '\uef80', 'ኽ', '䖶', '\ue8d8', 'Ꮛ', '䛩', '\ue956', 'ᰄ', '䜥', '\uea6f', 'ᵮ', '䁾', '\ueb7e', 'ᾁ', '䊅', '\uf5a1', 'ᣇ', '䏤', '\uf6f5', '᧩', '䰓', '\uf73a', 'ᨭ', '䵉', '㣍', '氯', '鄒', '옙', '歷', '遤', '앆', '櫹', '龬', '쒘', '榜', '黧', '쏇', '棄', '鰨', '섪', '癒', '魂', '쁣', '畕', '驛', '쾲', '璀', 'ሇ', '䛥', '믘', '\uecd3', '䆽', '몮', '\uef8c', '䀳', '땡', '\uee40', '䌊', '됈', '\ue909', '䈖', '뛾', '\uebe6', '峢', '놺', '\ueabf', '徚', 'a', '咃', 'ꦾ', 'ﺵ', '叛', '\ua8c8', '\ufdea', '剕', '꜆', 'ﰰ', '儶', 'ꘁ', 'שּ', '偻', '꒟', '.', '\\', '哃', '\ueb41', '뾧', '䊇', 'ᖍ', '뢱', '䏣', 'ᛊ', '륾', '䰷', 'ᜓ', '먙', '䵠', 'ၞ', '뭐', '뱇', '\ue8e6', 'ᗓ', '䋌', '\uefb4', 'ᒬ', '䘇', 'ዮ', '\uefdc', '뢲', 'ᗮ', '\ueea2', '뮏', 'ᑼ', '\ue16b', '멕', 'C', '咅', 'ꦿ', 'ﺤ', '叟', '꣒', 'ﷻ', '刖', 'ꜭ', 'ﰭ', '儡', 'Ꙋ', 'ﭬ', '偽', '꒟', '歷', '亾', '\uefc8', '묂', '䘽', 'ᄥ', '뱟', '䝋', 'ሠ', '북', '䢉', '\u13fb', '뺫', '䧄', 'ᓦ', '뾧', '䬛', 'ᘌ', 'ꄽ', '䱛', 'ᝁ', 'ꈹ', '䵺', 'ᢞ', 'ꎤ', '亭', 'ᦖ', 'ꓜ', '俵', 'ᬅ', 'ꘊ', '煷', 'ᰭ', 'ꝅ', '牍', 'ᵦ', 'ꢄ', '玝', 'ẳ', 'ꦠ', '瓎', 'ᾯ', 'ꪦ', 'g', '咈', 'ꦮ', 'ﺎ', '叚', '꣒', 'ﷺ', '刚', '꜄', 'ﰹ', '儧', 'ꙋ', 'ﭝ', '偹', 'ꒆ', '憐', '亹', 'ꏞ', '\uf8cb', '䷣', 'ꋭ', '\uf71e', '䰰', 'ꄸ', 'C', '咅', 'ꦿ', 'ﺤ', '叟', '꣒', 'ﷻ', '刖', 'Ꜻ', 'ﰰ', '儤', 'ꙃ', 'ﭹ', '偪', 'ꒂ', '簾', '亨', 'ꏞ', '\uf8cf', '䷧', 'ꋰ', '\uf718', '䰱', 'ꄥ', '戹', '㛳', '쯌', '鳔', 'ㆮ', '쪺', '\u9fd1', 'ね', '앹', '鹜', '㍒', '쐻', '餆', '㉖', '웻', '鯹', 'ⳉ', '솮', '骰', '⾋', '삏', '镫', '\u2e45', '쌔', '鐪', '⤿', '숙', '雨', '\u2bfc', 'ﳂ', '醙', '⪻', 'ﾶ', '邆', '╭', '︸', '鍎', '⑆', '爐', '鈁', '✇', 'ﯾ', '賴', '⇟', '節', '迬', '\u20c9'};
    private static long filed_l_01 = -285441353350097683L;


    private static String ˋ(int var0, char var1, int var2) {
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
                    var3[var4] = (char) ((int) ((long) field_arr_01[var2 + var4] ^ (long) var4 * filed_l_01 ^ (long) var1));
                    ++var4;
                    var10000 = field_01 + 13;
                    filed_i_01 = (field_01 + 13) % 128;
                    if (var10000 % 2 != 0) {
                        ;
                    }

                    var10000 = 2 % 2;
                    break;
                case 96:
                default:
                    String var5 = new String(var3);
                    int var10001 = field_01 + 117;
                    filed_i_01 = (field_01 + 117) % 128;
                    if (var10001 % 2 != 0) {
                        ;
                    }

                    return var5;
            }
        }
    }

}
