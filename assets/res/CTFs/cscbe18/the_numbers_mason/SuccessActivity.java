package com.vasco.notes;

import a.a.a.a;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.c;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import c.e;
import c.f;
import c.g.a;
import c.w;
import c.w.a;
import c.z;
import c.z.a;
import java.io.IOException;

public class SuccessActivity extends c {
	private static String l = a.a(102);
	private w m = new w();
	private EditText n;
	private String o;
	private String p;

	private void k() {
		z localz = new z.a().a(l + a.a(100)).b(a.a(101), this.o).a();
		this.m.a(localz).a(new f() {
			/* Error */
			public void a(final e paramAnonymouse, c.ab paramAnonymousab) {
				// Byte code:
				// 0: aload_2
				// 1: invokevirtual 33 c/ab:f ()Lc/ac;
				// 4: astore 4
				// 6: aconst_null
				// 7: astore_3
				// 8: aload_2
				// 9: invokevirtual 37 c/ab:c ()Z
				// 12: ifne +50 -> 62
				// 15: new 39 java/io/IOException
				// 18: dup
				// 19: new 41 java/lang/StringBuilder
				// 22: dup
				// 23: invokespecial 42 java/lang/StringBuilder:<init> ()V
				// 26: ldc 44
				// 28: invokevirtual 48 java/lang/StringBuilder:append
				// (Ljava/lang/String;)Ljava/lang/StringBuilder;
				// 31: aload_2
				// 32: invokevirtual 51 java/lang/StringBuilder:append
				// (Ljava/lang/Object;)Ljava/lang/StringBuilder;
				// 35: invokevirtual 55 java/lang/StringBuilder:toString ()Ljava/lang/String;
				// 38: invokespecial 58 java/io/IOException:<init> (Ljava/lang/String;)V
				// 41: athrow
				// 42: astore_2
				// 43: aload_2
				// 44: athrow
				// 45: astore_1
				// 46: aload 4
				// 48: ifnull +12 -> 60
				// 51: aload_2
				// 52: ifnull +150 -> 202
				// 55: aload 4
				// 57: invokevirtual 63 c/ac:close ()V
				// 60: aload_1
				// 61: athrow
				// 62: aload 4
				// 64: invokevirtual 66 c/ac:d ()Ljava/lang/String;
				// 67: astore_1
				// 68: ldc 68
				// 70: aload_1
				// 71: invokestatic 73 android/util/Log:d
				// (Ljava/lang/String;Ljava/lang/String;)I
				// 74: pop
				// 75: new 75 org/json/JSONObject
				// 78: dup
				// 79: aload_1
				// 80: invokespecial 76 org/json/JSONObject:<init> (Ljava/lang/String;)V
				// 83: ldc 78
				// 85: invokevirtual 82 org/json/JSONObject:getString
				// (Ljava/lang/String;)Ljava/lang/String;
				// 88: astore_1
				// 89: new 75 org/json/JSONObject
				// 92: dup
				// 93: aload_0
				// 94: getfield 19 com/vasco/notes/SuccessActivity$1:a
				// Lcom/vasco/notes/SuccessActivity;
				// 97: invokestatic 85 com/vasco/notes/SuccessActivity:a
				// (Lcom/vasco/notes/SuccessActivity;)Ljava/lang/String;
				// 100: ldc 87
				// 102: aload_1
				// 103: invokestatic 92 org/a/b/b/b:a
				// (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
				// 106: invokespecial 76 org/json/JSONObject:<init> (Ljava/lang/String;)V
				// 109: astore_1
				// 110: aload_1
				// 111: ldc 94
				// 113: invokevirtual 98 org/json/JSONObject:has (Ljava/lang/String;)Z
				// 116: ifeq +33 -> 149
				// 119: aload_1
				// 120: ldc 94
				// 122: invokevirtual 82 org/json/JSONObject:getString
				// (Ljava/lang/String;)Ljava/lang/String;
				// 125: astore_1
				// 126: getstatic 104 java/lang/System:out Ljava/io/PrintStream;
				// 129: aload_1
				// 130: invokevirtual 109 java/io/PrintStream:println (Ljava/lang/String;)V
				// 133: aload_0
				// 134: getfield 19 com/vasco/notes/SuccessActivity$1:a
				// Lcom/vasco/notes/SuccessActivity;
				// 137: new 13 com/vasco/notes/SuccessActivity$1$1
				// 140: dup
				// 141: aload_0
				// 142: aload_1
				// 143: invokespecial 112 com/vasco/notes/SuccessActivity$1$1:<init>
				// (Lcom/vasco/notes/SuccessActivity$1;Ljava/lang/String;)V
				// 146: invokevirtual 116 com/vasco/notes/SuccessActivity:runOnUiThread
				// (Ljava/lang/Runnable;)V
				// 149: aload 4
				// 151: ifnull +12 -> 163
				// 154: iconst_0
				// 155: ifeq +32 -> 187
				// 158: aload 4
				// 160: invokevirtual 63 c/ac:close ()V
				// 163: return
				// 164: astore_1
				// 165: aload_1
				// 166: invokevirtual 119 java/lang/Exception:printStackTrace ()V
				// 169: goto -20 -> 149
				// 172: astore_1
				// 173: aload_3
				// 174: astore_2
				// 175: goto -129 -> 46
				// 178: astore_1
				// 179: new 121 java/lang/NullPointerException
				// 182: dup
				// 183: invokespecial 122 java/lang/NullPointerException:<init> ()V
				// 186: athrow
				// 187: aload 4
				// 189: invokevirtual 63 c/ac:close ()V
				// 192: return
				// 193: astore_3
				// 194: aload_2
				// 195: aload_3
				// 196: invokevirtual 126 java/lang/Throwable:addSuppressed
				// (Ljava/lang/Throwable;)V
				// 199: goto -139 -> 60
				// 202: aload 4
				// 204: invokevirtual 63 c/ac:close ()V
				// 207: goto -147 -> 60
				// Local variable table:
				// start length slot name signature
				// 0 210 0 this 1
				// 0 210 1 paramAnonymouse e
				// 0 210 2 paramAnonymousab c.ab
				// 7 167 3 localObject Object
				// 193 3 3 localThrowable Throwable
				// 4 199 4 localac c.ac
				// Exception table:
				// from to target type
				// 8 42 42 java/lang/Throwable
				// 62 149 42 java/lang/Throwable
				// 165 169 42 java/lang/Throwable
				// 43 45 45 finally
				// 62 149 164 java/lang/Exception
				// 8 42 172 finally
				// 62 149 172 finally
				// 165 169 172 finally
				// 158 163 178 java/lang/Throwable
				// 55 60 193 java/lang/Throwable
			}

			public void a(e paramAnonymouse, IOException paramAnonymousIOException) {
				paramAnonymousIOException.printStackTrace();
			}
		});
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(2131361820);
		a((Toolbar) findViewById(2131230891));
		this.m = new w.a().a(new g.a().a(a.a(96), new String[] { a.a(97) }).a()).a();
		this.n = ((EditText) findViewById(2131230787));
		paramBundle = getIntent();
		this.o = paramBundle.getStringExtra(a.a(98));
		this.p = paramBundle.getStringExtra(a.a(99));
		k();
	}
}
