.class public Lcom/example/testlib/MyClass;
.super Ljava/lang/Object;
.source "MyClass.java"


# static fields
.field static a:I

.field private static field_01:I

.field private static field_arr_01:[C

.field private static filed_i_01:I

.field private static filed_l_01:J


# direct methods
.method static constructor <clinit>()V
    .registers 2

    .prologue
    .line 12
    const/16 v0, 0x64

    sput v0, Lcom/example/testlib/MyClass;->a:I

    .line 86
    const/4 v0, 0x0

    sput v0, Lcom/example/testlib/MyClass;->filed_i_01:I

    .line 87
    const/4 v0, 0x1

    sput v0, Lcom/example/testlib/MyClass;->field_01:I

    .line 88
    const/16 v0, 0x163

    new-array v0, v0, [C

    fill-array-data v0, :array_1c

    sput-object v0, Lcom/example/testlib/MyClass;->field_arr_01:[C

    .line 115
    const-wide v0, -0x3f6176606a3ab13L    # -3.156092966395767E289

    sput-wide v0, Lcom/example/testlib/MyClass;->filed_l_01:J

    return-void

    .line 88
    nop

    :array_1c
    .array-data 2
        -0x4d3fs
        -0x19dds
        0x1b1es
        0x4c15s
        -0x1e85s
        0x1a68s
        0x4f4as
        -0x1f0bs
        0x15bcs
        0x4e90s
        -0x1c72s
        0x14eas
        0x49ccs
        -0x1d3fs
        0x1639s
        0x4b2ds
        -0x3f7s
        0x1133s
        0x4a5es
        -0xaes
        0x1048s
        0x45b4s
        -0x172s
        0x1383s
        0x44f7s
        -0x615s
        0x12cbs
        0x4612s
        -0x4d3s
        0x2c17s
        0x4107s
        -0x58cs
        0x2f65s
        0x405fs
        0x31s
        0x30s
        0x79s
        0x5494s
        -0x565ds
        -0x142s
        0x5399s
        -0x5714s
        -0x23ds
        0x5256s
        -0x58f4s
        -0x3cfs
        0x511ds
        -0x5999s
        -0x4acs
        0x5064s
        -0x5b65s
        -0x670s
        0x4ea3s
        -0x5c19s
        -0x41b5s
        -0x155bs
        0x1791s
        0x408bs
        -0x1204s
        0x16eds
        0x4397s
        -0x1399s
        0x1978s
        0x4218s
        -0x10a2s
        0x1835s
        0x4554s
        -0x11bes
        0x1ae6s
        0x47f9s
        -0xf33s
        0x1df2s
        0x46b7s
        -0xc25s
        0x1c99s
        0x493ds
        -0xdbds
        0x1f52s
        0x4875s
        -0xacbs
        0x1e0as
        0x4ab0s
        -0x809s
        0x20c0s
        0x4ddes
        -0x955s
        -0x449es
        -0x1080s
        0x12bds
        0x45b6s
        -0x1728s
        0x13cbs
        0x46e9s
        -0x16aas
        0x1c04s
        0x4725s
        -0x1591s
        0x1d6es
        0x407es
        -0x1482s
        0x1f81s
        0x4285s
        -0xa5fs
        0x18c7s
        0x43e4s
        -0x90bs
        0x19e9s
        0x4c13s
        -0x8c6s
        0x1a2ds
        0x4d49s
        0x38cds
        0x6c2fs
        -0x6eees
        -0x39e7s
        0x6b77s
        -0x6f9cs
        -0x3abas
        0x6af9s
        -0x6054s
        -0x3b68s
        0x699cs
        -0x6119s
        -0x3c39s
        0x68c4s
        -0x63d8s
        -0x3ed6s
        0x7652s
        -0x64bes
        -0x3f9ds
        0x7555s
        -0x65a5s
        -0x304es
        0x7480s
        0x1207s
        0x46e5s
        -0x4428s
        -0x132ds
        0x41bds
        -0x4552s
        -0x1074s
        0x4033s
        -0x4a9fs
        -0x11c0s
        0x430as
        -0x4bf8s
        -0x16f7s
        0x4216s
        -0x4902s
        -0x141as
        0x5ce2s
        -0x4e46s
        -0x1541s
        0x5f9as
        0x61s
        0x5483s
        -0x5642s
        -0x14bs
        0x53dbs
        -0x5738s
        -0x216s
        0x5255s
        -0x58fas
        -0x3d0s
        0x5136s
        -0x59ffs
        -0x4b7s
        0x507bs
        -0x5b61s
        0x2es
        0x5cs
        0x54c3s
        -0x14bfs
        -0x4059s
        0x4287s
        0x158ds
        -0x474fs
        0x43e3s
        0x16cas
        -0x4682s
        0x4c37s
        0x1713s
        -0x45e7s
        0x4d60s
        0x105es
        -0x44b0s
        -0x43b9s
        -0x171as
        0x15d3s
        0x42ccs
        -0x104cs
        0x14acs
        0x4607s
        0x12ees
        -0x1024s
        -0x474es
        0x15ees
        -0x115es
        -0x4471s
        0x147cs
        -0x1e95s
        -0x45abs
        0x43s
        0x5485s
        -0x5641s
        -0x15cs
        0x53dfs
        -0x572es
        -0x205s
        0x5216s
        -0x58d3s
        -0x3d3s
        0x5121s
        -0x59b6s
        -0x494s
        0x507ds
        -0x5b61s
        -0x674s
        0x4ebes
        -0x1038s
        -0x44fes
        0x463ds
        0x1125s
        -0x43a1s
        0x474bs
        0x1220s
        -0x427fs
        0x4889s
        0x13fbs
        -0x4155s
        0x49c4s
        0x14e6s
        -0x4059s
        0x4b1bs
        0x160cs
        -0x5ec3s
        0x4c5bs
        0x1741s
        -0x5dc7s
        0x4d7as
        0x189es
        -0x5c5cs
        0x4eads
        0x1996s
        -0x5b24s
        0x4ff5s
        0x1b05s
        -0x59f6s
        0x7177s
        0x1c2ds
        -0x58bbs
        0x724ds
        0x1d66s
        -0x577cs
        0x739ds
        0x1eb3s
        -0x5660s
        0x74ces
        0x1fafs
        -0x555as
        0x67s
        0x5488s
        -0x5652s
        -0x172s
        0x53das
        -0x572es
        -0x206s
        0x521as
        -0x58fcs
        -0x3c7s
        0x5127s
        -0x59b5s
        -0x4a3s
        0x5079s
        -0x5b7as
        -0x671s
        0x4eb9s
        -0x5c22s
        -0x735s
        0x4de3s
        -0x5d13s
        -0x8e2s
        0x4c30s
        -0x5ec8s
        0x43s
        0x5485s
        -0x5641s
        -0x15cs
        0x53dfs
        -0x572es
        -0x205s
        0x5216s
        -0x58c6s
        -0x3d0s
        0x5124s
        -0x59bds
        -0x487s
        0x506as
        -0x5b7es
        -0x65as
        0x4ea8s
        -0x5c22s
        -0x731s
        0x4de7s
        -0x5d10s
        -0x8e8s
        0x4c31s
        -0x5edbs
        0x6239s
        0x36f3s
        -0x3434s
        -0x632cs
        0x31aes
        -0x3546s
        -0x602fs
        0x306ds
        -0x3a87s
        -0x61a4s
        0x3352s
        -0x3bc5s
        -0x66fas
        0x3256s
        -0x3905s
        -0x6407s
        0x2cc9s
        -0x3e52s
        -0x6550s
        0x2f8bs
        -0x3f71s
        -0x6a95s
        0x2e45s
        -0x3cecs
        -0x6bd6s
        0x293fs
        -0x3de7s
        -0x6918s
        0x2bfcs
        -0x33es
        -0x6e67s
        0x2abbs
        -0x4as
        -0x6f7as
        0x256ds
        -0x1c8s
        -0x6cb2s
        0x2446s
        -0x6ces
        -0x6dffs
        0x2707s
        -0x402s
        -0x730cs
        0x21dfs
        -0x553s
        -0x7014s
        0x20c9s
    .end array-data
.end method

.method public constructor <init>()V
    .registers 1

    .prologue
    .line 10
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static getDexCrc(Ljava/io/File;)Ljava/lang/String;
    .registers 7
    .param p0, "file"    # Ljava/io/File;

    .prologue
    .line 40
    :try_start_0
    new-instance v0, Ljava/util/zip/ZipFile;

    invoke-direct {v0, p0}, Ljava/util/zip/ZipFile;-><init>(Ljava/io/File;)V

    .line 41
    .local v0, "apkFile":Ljava/util/zip/ZipFile;
    const-string v5, "classes.dex"

    invoke-virtual {v0, v5}, Ljava/util/zip/ZipFile;->getEntry(Ljava/lang/String;)Ljava/util/zip/ZipEntry;

    move-result-object v1

    .line 42
    .local v1, "dexEntry":Ljava/util/zip/ZipEntry;
    invoke-virtual {v1}, Ljava/util/zip/ZipEntry;->getCrc()J

    move-result-wide v2

    .line 43
    .local v2, "crc":J
    invoke-static {v2, v3}, Ljava/lang/String;->valueOf(J)Ljava/lang/String;
    :try_end_12
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_12} :catch_14

    move-result-object v5

    .line 49
    .end local v0    # "apkFile":Ljava/util/zip/ZipFile;
    .end local v1    # "dexEntry":Ljava/util/zip/ZipEntry;
    .end local v2    # "crc":J
    :goto_13
    return-object v5

    .line 45
    :catch_14
    move-exception v4

    .line 46
    .local v4, "e":Ljava/io/IOException;
    invoke-virtual {v4}, Ljava/io/IOException;->printStackTrace()V

    .line 49
    const/4 v5, 0x0

    goto :goto_13
.end method

.method public static main([Ljava/lang/String;)V
    .registers 12
    .param p0, "args"    # [Ljava/lang/String;

    .prologue
    .line 16
    const/4 v0, 0x1

    .line 18
    .local v0, "a":I
    sget-object v8, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string v9, "0"

    invoke-static {v9}, Ljava/lang/Boolean;->valueOf(Ljava/lang/String;)Ljava/lang/Boolean;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/io/PrintStream;->println(Ljava/lang/Object;)V

    .line 20
    const-string v4, "/data/user/0/com.speed.booster.kim/cache"

    .line 23
    .local v4, "cachePath":Ljava/lang/String;
    invoke-static {v4}, Lcom/example/testlib/MyClass;->test(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v7

    .line 24
    .local v7, "test":Ljava/lang/String;
    sget-object v8, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v8, v7}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 25
    const-string v8, "f6544e4a"

    const/16 v9, 0x10

    invoke-static {v8, v9}, Ljava/lang/Long;->parseLong(Ljava/lang/String;I)J

    move-result-wide v2

    .line 26
    .local v2, "aLong":J
    sget-object v8, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v8, v2, v3}, Ljava/io/PrintStream;->println(J)V

    .line 28
    const-string v6, "/Users/sym/Documents/altamob/smali/smailtest/app/build/outputs/apk/debug/app-debug.apk"

    .line 30
    .local v6, "path":Ljava/lang/String;
    new-instance v1, Ljava/io/File;

    invoke-direct {v1, v6}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 31
    .local v1, "apkFile":Ljava/io/File;
    invoke-static {v1}, Lcom/example/testlib/MyClass;->getDexCrc(Ljava/io/File;)Ljava/lang/String;

    move-result-object v5

    .line 32
    .local v5, "dexCrc":Ljava/lang/String;
    sget-object v8, Ljava/lang/System;->out:Ljava/io/PrintStream;

    new-instance v9, Ljava/lang/StringBuilder;

    invoke-direct {v9}, Ljava/lang/StringBuilder;-><init>()V

    const-string v10, "dexCrc = "

    invoke-virtual {v9, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v9

    invoke-virtual {v9}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-virtual {v8, v9}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 34
    return-void
.end method

.method private static test(Ljava/lang/String;)Ljava/lang/String;
    .registers 14
    .param p0, "cachePath"    # Ljava/lang/String;

    .prologue
    .line 57
    const/4 v8, 0x0

    .line 58
    .local v8, "ret":Ljava/lang/String;
    const/4 v9, 0x6

    .line 59
    .local v9, "v1_0x6":I
    const v10, 0xbc68

    .line 60
    .local v10, "v3_0xbc68":C
    const/16 v11, 0xba

    .line 61
    .local v11, "v4_0xba":I
    invoke-static {v9, v10, v11}, Lcom/example/testlib/MyClass;->ˋ(ICI)Ljava/lang/String;

    move-result-object v12

    invoke-virtual {v12}, Ljava/lang/String;->intern()Ljava/lang/String;

    move-result-object v1

    .line 62
    .local v1, "intern":Ljava/lang/String;
    const-string v12, ""

    invoke-virtual {p0, v1, v12}, Ljava/lang/String;->replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;

    move-result-object v0

    .line 67
    .local v0, "input":Ljava/lang/String;
    sget-object v12, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v12, v1}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 68
    sget-object v12, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v12, v0}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 70
    const/16 v3, 0xa

    .line 71
    .local v3, "p1":I
    const/16 v4, 0x4629

    .line 72
    .local v4, "p2":C
    const/16 v5, 0xc0

    .line 73
    .local v5, "p3":I
    invoke-static {v3, v4, v5}, Lcom/example/testlib/MyClass;->ˋ(ICI)Ljava/lang/String;

    move-result-object v12

    invoke-virtual {v12}, Ljava/lang/String;->intern()Ljava/lang/String;

    move-result-object v7

    .line 74
    .local v7, "regex":Ljava/lang/String;
    sget-object v12, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {v12, v7}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 76
    const/4 v6, 0x1

    .line 77
    .local v6, "pV3":I
    invoke-static {v7}, Ljava/util/regex/Pattern;->compile(Ljava/lang/String;)Ljava/util/regex/Pattern;

    move-result-object v12

    invoke-virtual {v12, v0}, Ljava/util/regex/Pattern;->matcher(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;

    move-result-object v2

    .line 79
    .local v2, "matcher":Ljava/util/regex/Matcher;
    invoke-virtual {v2}, Ljava/util/regex/Matcher;->find()Z

    move-result v12

    if-eqz v12, :cond_45

    .line 80
    invoke-virtual {v2, v6}, Ljava/util/regex/Matcher;->group(I)Ljava/lang/String;

    move-result-object v8

    .line 83
    :cond_45
    return-object v8
.end method

.method private static ˋ(ICI)Ljava/lang/String;
    .registers 15
    .param p0, "var0"    # I
    .param p1, "var1"    # C
    .param p2, "var2"    # I

    .prologue
    .line 119
    const/4 v0, 0x0

    .line 120
    .local v0, "var10000":I
    sget v5, Lcom/example/testlib/MyClass;->field_01:I

    add-int/lit8 v0, v5, 0x7d

    .line 121
    sget v5, Lcom/example/testlib/MyClass;->field_01:I

    add-int/lit8 v5, v5, 0x7d

    rem-int/lit16 v5, v5, 0x80

    sput v5, Lcom/example/testlib/MyClass;->filed_i_01:I

    .line 124
    rem-int/lit8 v5, v0, 0x2

    if-eqz v5, :cond_37

    const/16 v5, 0x29

    :goto_13
    packed-switch v5, :pswitch_data_68

    .line 127
    new-array v2, p0, [C

    .line 128
    .local v2, "var3":[C
    const/4 v3, 0x0

    .line 135
    .local v3, "var4":I
    :goto_19
    const/4 v0, 0x0

    .line 138
    :goto_1a
    if-ge v3, p0, :cond_3e

    const/16 v5, 0x28

    :goto_1e
    packed-switch v5, :pswitch_data_6e

    .line 153
    new-instance v4, Ljava/lang/String;

    invoke-direct {v4, v2}, Ljava/lang/String;-><init>([C)V

    .line 154
    .local v4, "var5":Ljava/lang/String;
    sget v5, Lcom/example/testlib/MyClass;->field_01:I

    add-int/lit8 v1, v5, 0x75

    .line 155
    .local v1, "var10001":I
    sget v5, Lcom/example/testlib/MyClass;->field_01:I

    add-int/lit8 v5, v5, 0x75

    rem-int/lit16 v5, v5, 0x80

    sput v5, Lcom/example/testlib/MyClass;->filed_i_01:I

    .line 156
    rem-int/lit8 v5, v1, 0x2

    if-eqz v5, :cond_36

    .line 160
    :cond_36
    return-object v4

    .line 124
    .end local v1    # "var10001":I
    .end local v2    # "var3":[C
    .end local v3    # "var4":I
    .end local v4    # "var5":Ljava/lang/String;
    :cond_37
    const/16 v5, 0x18

    goto :goto_13

    .line 131
    :pswitch_3a
    new-array v2, p0, [C

    .line 132
    .restart local v2    # "var3":[C
    const/4 v3, 0x0

    .restart local v3    # "var4":I
    goto :goto_19

    .line 138
    :cond_3e
    const/16 v5, 0x60

    goto :goto_1e

    .line 140
    :pswitch_41
    sget-object v5, Lcom/example/testlib/MyClass;->field_arr_01:[C

    add-int v6, p2, v3

    aget-char v5, v5, v6

    int-to-long v6, v5

    int-to-long v8, v3

    sget-wide v10, Lcom/example/testlib/MyClass;->filed_l_01:J

    mul-long/2addr v8, v10

    xor-long/2addr v6, v8

    int-to-long v8, p1

    xor-long/2addr v6, v8

    long-to-int v5, v6

    int-to-char v5, v5

    aput-char v5, v2, v3

    .line 142
    add-int/lit8 v3, v3, 0x1

    .line 143
    sget v5, Lcom/example/testlib/MyClass;->field_01:I

    add-int/lit8 v0, v5, 0xd

    .line 144
    sget v5, Lcom/example/testlib/MyClass;->field_01:I

    add-int/lit8 v5, v5, 0xd

    rem-int/lit16 v5, v5, 0x80

    sput v5, Lcom/example/testlib/MyClass;->filed_i_01:I

    .line 145
    rem-int/lit8 v5, v0, 0x2

    if-eqz v5, :cond_65

    .line 149
    :cond_65
    const/4 v0, 0x0

    .line 150
    goto :goto_1a

    .line 124
    nop

    :pswitch_data_68
    .packed-switch 0x29
        :pswitch_3a
    .end packed-switch

    .line 138
    :pswitch_data_6e
    .packed-switch 0x28
        :pswitch_41
    .end packed-switch
.end method
