package com.vasco.notes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.ContactsContract.Profile;
import android.support.design.widget.Snackbar;
import android.support.v7.app.c;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewPropertyAnimator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;
import c.e;
import c.f;
import c.g.a;
import c.q.a;
import c.w;
import c.w.a;
import c.z;
import c.z.a;
import com.a.a.b;
import com.hextremelabs.pinpad.PinTextView;
import com.hextremelabs.pinpad.PinpadView;
import com.hextremelabs.pinpad.PinpadView.a;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends c implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String l = a.a.a.a.a(123);
	public static final String m = a.a.a.a.a(124);
	public static final String n = a.a.a.a.a(125);
	private static final String[] o = { a.a.a.a.a(126), a.a.a.a.a(127) };
	private static String y = a.a.a.a.a(128);
	private b p = null;
	private AutoCompleteTextView q;
	private EditText r;
	private View s;
	private View t;
	private PinTextView u;
	private PinpadView v;
	private String w;
	private String x;
	private w z = new w();

	private void a(List<String> paramList) {
		paramList = new ArrayAdapter(this, 17367050, paramList);
		this.q.setAdapter(paramList);
	}

	@TargetApi(13)
	private void b(final boolean paramBoolean) {
		float f2 = 1.0F;
		int k = 8;
		int j = 0;
		if (Build.VERSION.SDK_INT >= 13) {
			k = getResources().getInteger(17694720);
			localObject = this.t;
			if (paramBoolean) {
				i = 8;
				((View) localObject).setVisibility(i);
				localObject = this.t.animate().setDuration(k);
				if (!paramBoolean) {
					break label157;
				}
				f1 = 0.0F;
				label70: ((ViewPropertyAnimator) localObject).alpha(f1).setListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator paramAnonymousAnimator) {
						paramAnonymousAnimator = LoginActivity.g(LoginActivity.this);
						if (paramBoolean) {
						}
						for (int i = 8;; i = 0) {
							paramAnonymousAnimator.setVisibility(i);
							return;
						}
					}
				});
				localObject = this.s;
				if (!paramBoolean) {
					break label162;
				}
				i = j;
				label103: ((View) localObject).setVisibility(i);
				localObject = this.s.animate().setDuration(k);
				if (!paramBoolean) {
					break label169;
				}
			}
			label157: label162: label169: for (float f1 = f2;; f1 = 0.0F) {
				((ViewPropertyAnimator) localObject).alpha(f1).setListener(new AnimatorListenerAdapter() {
					public void onAnimationEnd(Animator paramAnonymousAnimator) {
						paramAnonymousAnimator = LoginActivity.h(LoginActivity.this);
						if (paramBoolean) {
						}
						for (int i = 0;; i = 8) {
							paramAnonymousAnimator.setVisibility(i);
							return;
						}
					}
				});
				return;
				i = 0;
				break;
				f1 = 1.0F;
				break label70;
				i = 8;
				break label103;
			}
		}
		Object localObject = this.s;
		if (paramBoolean) {
			i = 0;
			((View) localObject).setVisibility(i);
			localObject = this.t;
			if (!paramBoolean) {
				break label223;
			}
		}
		label223: for (int i = k;; i = 0) {
			((View) localObject).setVisibility(i);
			return;
			i = 8;
			break;
		}
	}

	private void l() {
		Intent localIntent = new Intent(this, SuccessActivity.class);
		localIntent.putExtra(a.a.a.a.a(105), this.w);
		localIntent.putExtra(a.a.a.a.a(106), this.x);
		startActivity(localIntent);
	}

	private void m() {
		runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(LoginActivity.this.getApplicationContext(), "Access denied", 0).show();
			}
		});
	}

	private void n() {
		z localz = new z.a().a(y + a.a.a.a.a(107)).a();
		this.z.a(localz).a(new f() {
			/* Error */
			public void a(e paramAnonymouse, c.ab paramAnonymousab) {
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
				// 52: ifnull +104 -> 156
				// 55: aload 4
				// 57: invokevirtual 63 c/ac:close ()V
				// 60: aload_1
				// 61: athrow
				// 62: new 65 org/json/JSONObject
				// 65: dup
				// 66: aload 4
				// 68: invokevirtual 68 c/ac:d ()Ljava/lang/String;
				// 71: invokespecial 69 org/json/JSONObject:<init> (Ljava/lang/String;)V
				// 74: astore_1
				// 75: aload_0
				// 76: getfield 19 com/vasco/notes/LoginActivity$3:a
				// Lcom/vasco/notes/LoginActivity;
				// 79: aload_1
				// 80: ldc 71
				// 82: invokevirtual 75 org/json/JSONObject:getString
				// (Ljava/lang/String;)Ljava/lang/String;
				// 85: invokestatic 78 com/vasco/notes/LoginActivity:a
				// (Lcom/vasco/notes/LoginActivity;Ljava/lang/String;)Ljava/lang/String;
				// 88: pop
				// 89: aload_0
				// 90: getfield 19 com/vasco/notes/LoginActivity$3:a
				// Lcom/vasco/notes/LoginActivity;
				// 93: aload_1
				// 94: ldc 80
				// 96: invokevirtual 75 org/json/JSONObject:getString
				// (Ljava/lang/String;)Ljava/lang/String;
				// 99: invokestatic 83 com/vasco/notes/LoginActivity:b
				// (Lcom/vasco/notes/LoginActivity;Ljava/lang/String;)Ljava/lang/String;
				// 102: pop
				// 103: aload 4
				// 105: ifnull +12 -> 117
				// 108: iconst_0
				// 109: ifeq +32 -> 141
				// 112: aload 4
				// 114: invokevirtual 63 c/ac:close ()V
				// 117: return
				// 118: astore_1
				// 119: aload_1
				// 120: invokevirtual 86 java/lang/Exception:printStackTrace ()V
				// 123: goto -20 -> 103
				// 126: astore_1
				// 127: aload_3
				// 128: astore_2
				// 129: goto -83 -> 46
				// 132: astore_1
				// 133: new 88 java/lang/NullPointerException
				// 136: dup
				// 137: invokespecial 89 java/lang/NullPointerException:<init> ()V
				// 140: athrow
				// 141: aload 4
				// 143: invokevirtual 63 c/ac:close ()V
				// 146: return
				// 147: astore_3
				// 148: aload_2
				// 149: aload_3
				// 150: invokevirtual 93 java/lang/Throwable:addSuppressed
				// (Ljava/lang/Throwable;)V
				// 153: goto -93 -> 60
				// 156: aload 4
				// 158: invokevirtual 63 c/ac:close ()V
				// 161: goto -101 -> 60
				// Local variable table:
				// start length slot name signature
				// 0 164 0 this 3
				// 0 164 1 paramAnonymouse e
				// 0 164 2 paramAnonymousab c.ab
				// 7 121 3 localObject Object
				// 147 3 3 localThrowable Throwable
				// 4 153 4 localac c.ac
				// Exception table:
				// from to target type
				// 8 42 42 java/lang/Throwable
				// 62 103 42 java/lang/Throwable
				// 119 123 42 java/lang/Throwable
				// 43 45 45 finally
				// 62 103 118 java/lang/Exception
				// 8 42 126 finally
				// 62 103 126 finally
				// 119 123 126 finally
				// 112 117 132 java/lang/Throwable
				// 55 60 147 java/lang/Throwable
			}

			public void a(e paramAnonymouse, IOException paramAnonymousIOException) {
				LoginActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(LoginActivity.this.getApplicationContext(), "Connection issue!", 0).show();
					}
				});
			}
		});
	}

	private void o() {
		if (!p()) {
			return;
		}
		getLoaderManager().initLoader(0, null, this);
	}

	private boolean p() {
		if (Build.VERSION.SDK_INT < 23) {
		}
		while (checkSelfPermission(a.a.a.a.a(115)) == 0) {
			return true;
		}
		if (shouldShowRequestPermissionRationale(a.a.a.a.a(116))) {
			Snackbar.a(this.q, 2131492911, -2).a(17039370, new View.OnClickListener() {
				@TargetApi(23)
				public void onClick(View paramAnonymousView) {
					LoginActivity.this.requestPermissions(new String[] { "android.permission.READ_CONTACTS" }, 0);
				}
			});
		}
		for (;;) {
			return false;
			requestPermissions(new String[] { a.a.a.a.a(117) }, 0);
		}
	}

	public void a(Loader<Cursor> paramLoader, Cursor paramCursor) {
		paramLoader = new ArrayList();
		paramCursor.moveToFirst();
		while (!paramCursor.isAfterLast()) {
			paramLoader.add(paramCursor.getString(0));
			paramCursor.moveToNext();
		}
		a(paramLoader);
	}

	public void a(final String paramString1, String paramString2, String paramString3, String paramString4) {
		this.w = paramString1;
		this.x = paramString2;
		paramString2 = new JSONObject();
		paramString2.put(a.a.a.a.a(108), paramString4);
		paramString2.put(a.a.a.a.a(109), paramString3);
		paramString2 = org.a.b.a.a.a(paramString1, a.a.a.a.a(110), paramString2.toString());
		paramString2 = new q.a().a(a.a.a.a.a(111), paramString2).a();
		paramString2 = new z.a().a(y + a.a.a.a.a(112)).b(a.a.a.a.a(113), this.x).a(a.a.a.a.a(114), paramString2).a();
		this.z.a(paramString2).a(new f() {
			/* Error */
			public void a(e paramAnonymouse, c.ab paramAnonymousab) {
				// Byte code:
				// 0: aload_2
				// 1: invokevirtual 37 c/ab:f ()Lc/ac;
				// 4: astore_3
				// 5: aconst_null
				// 6: astore_1
				// 7: aload_2
				// 8: invokevirtual 41 c/ab:e ()Lc/s;
				// 11: astore_2
				// 12: new 43 org/json/JSONObject
				// 15: dup
				// 16: aload_3
				// 17: invokevirtual 49 c/ac:d ()Ljava/lang/String;
				// 20: invokespecial 52 org/json/JSONObject:<init> (Ljava/lang/String;)V
				// 23: ldc 54
				// 25: invokevirtual 58 org/json/JSONObject:getString
				// (Ljava/lang/String;)Ljava/lang/String;
				// 28: astore 4
				// 30: new 43 org/json/JSONObject
				// 33: dup
				// 34: aload_0
				// 35: getfield 22 com/vasco/notes/LoginActivity$4:a Ljava/lang/String;
				// 38: ldc 60
				// 40: aload 4
				// 42: invokestatic 65 org/a/b/b/b:a
				// (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
				// 45: invokespecial 52 org/json/JSONObject:<init> (Ljava/lang/String;)V
				// 48: astore 4
				// 50: aload_0
				// 51: getfield 20 com/vasco/notes/LoginActivity$4:b
				// Lcom/vasco/notes/LoginActivity;
				// 54: aload 4
				// 56: ldc 67
				// 58: invokevirtual 58 org/json/JSONObject:getString
				// (Ljava/lang/String;)Ljava/lang/String;
				// 61: invokestatic 70 com/vasco/notes/LoginActivity:a
				// (Lcom/vasco/notes/LoginActivity;Ljava/lang/String;)Ljava/lang/String;
				// 64: pop
				// 65: aload_2
				// 66: ldc 72
				// 68: invokevirtual 76 c/s:a (Ljava/lang/String;)Ljava/lang/String;
				// 71: ifnull +23 -> 94
				// 74: aload_0
				// 75: getfield 20 com/vasco/notes/LoginActivity$4:b
				// Lcom/vasco/notes/LoginActivity;
				// 78: invokestatic 79 com/vasco/notes/LoginActivity:e
				// (Lcom/vasco/notes/LoginActivity;)V
				// 81: aload_3
				// 82: ifnull +11 -> 93
				// 85: iconst_0
				// 86: ifeq +53 -> 139
				// 89: aload_3
				// 90: invokevirtual 82 c/ac:close ()V
				// 93: return
				// 94: aload_0
				// 95: getfield 20 com/vasco/notes/LoginActivity$4:b
				// Lcom/vasco/notes/LoginActivity;
				// 98: invokestatic 84 com/vasco/notes/LoginActivity:f
				// (Lcom/vasco/notes/LoginActivity;)V
				// 101: goto -20 -> 81
				// 104: astore_2
				// 105: aload_2
				// 106: invokevirtual 87 java/lang/Exception:printStackTrace ()V
				// 109: goto -28 -> 81
				// 112: astore_1
				// 113: aload_1
				// 114: athrow
				// 115: astore_2
				// 116: aload_3
				// 117: ifnull +11 -> 128
				// 120: aload_1
				// 121: ifnull +32 -> 153
				// 124: aload_3
				// 125: invokevirtual 82 c/ac:close ()V
				// 128: aload_2
				// 129: athrow
				// 130: astore_1
				// 131: new 89 java/lang/NullPointerException
				// 134: dup
				// 135: invokespecial 90 java/lang/NullPointerException:<init> ()V
				// 138: athrow
				// 139: aload_3
				// 140: invokevirtual 82 c/ac:close ()V
				// 143: return
				// 144: astore_3
				// 145: aload_1
				// 146: aload_3
				// 147: invokevirtual 94 java/lang/Throwable:addSuppressed
				// (Ljava/lang/Throwable;)V
				// 150: goto -22 -> 128
				// 153: aload_3
				// 154: invokevirtual 82 c/ac:close ()V
				// 157: goto -29 -> 128
				// 160: astore_2
				// 161: goto -45 -> 116
				// Local variable table:
				// start length slot name signature
				// 0 164 0 this 4
				// 0 164 1 paramAnonymouse e
				// 0 164 2 paramAnonymousab c.ab
				// 4 136 3 localac c.ac
				// 144 10 3 localThrowable Throwable
				// 28 27 4 localObject Object
				// Exception table:
				// from to target type
				// 12 81 104 java/lang/Exception
				// 94 101 104 java/lang/Exception
				// 7 12 112 java/lang/Throwable
				// 12 81 112 java/lang/Throwable
				// 94 101 112 java/lang/Throwable
				// 105 109 112 java/lang/Throwable
				// 113 115 115 finally
				// 89 93 130 java/lang/Throwable
				// 124 128 144 java/lang/Throwable
				// 7 12 160 finally
				// 12 81 160 finally
				// 94 101 160 finally
				// 105 109 160 finally
			}

			public void a(e paramAnonymouse, IOException paramAnonymousIOException) {
				LoginActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(LoginActivity.this.getApplicationContext(), "Connection error", 0).show();
					}
				});
			}
		});
	}

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(2131361819);
		this.q = ((AutoCompleteTextView) findViewById(2131230782));
		this.z = new w.a().a(new g.a().a(a.a.a.a.a(103), new String[] { a.a.a.a.a(104) }).a()).a();
		if (new b(getApplicationContext()).a()) {
			finish();
		}
		n();
		this.u = ((PinTextView) findViewById(2131230833));
		this.v = ((PinpadView) findViewById(2131230832));
		this.v.setViewProvider(this.u);
		this.v.setCallback(new PinpadView.a() {
			public void a() {
			}

			public void a(String paramAnonymousString) {
				String str = LoginActivity.a(LoginActivity.this).getText().toString();
				LoginActivity.b(LoginActivity.this).a();
				if (str.isEmpty()) {
					LoginActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(LoginActivity.this.getApplicationContext(), "Enter username first.", 0)
									.show();
						}
					});
				}
				while ((LoginActivity.c(LoginActivity.this) == null) || (LoginActivity.d(LoginActivity.this) == null)
						|| (LoginActivity.c(LoginActivity.this).isEmpty())
						|| (LoginActivity.d(LoginActivity.this).isEmpty()) || (str.isEmpty())
						|| (paramAnonymousString.isEmpty())) {
					LoginActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(LoginActivity.this.getApplicationContext(),
									"Connection issue. Please restart.", 0).show();
						}
					});
					return;
					LoginActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(LoginActivity.this.getApplicationContext(), "Authenticating...", 0).show();
						}
					});
				}
				try {
					LoginActivity.this.a(LoginActivity.c(LoginActivity.this), LoginActivity.d(LoginActivity.this), str,
							paramAnonymousString);
					return;
				} catch (JSONException paramAnonymousString) {
					paramAnonymousString.printStackTrace();
				}
			}
		});
	}

	public Loader<Cursor> onCreateLoader(int paramInt, Bundle paramBundle) {
		paramBundle = Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, a.a.a.a.a(119));
		String[] arrayOfString = a.a;
		String str1 = a.a.a.a.a(120);
		String str2 = a.a.a.a.a(121);
		String str3 = a.a.a.a.a(122);
		return new CursorLoader(this, paramBundle, arrayOfString, str1, new String[] { str2 }, str3);
	}

	public void onLoaderReset(Loader<Cursor> paramLoader) {
	}

	public void onRequestPermissionsResult(int paramInt, String[] paramArrayOfString, int[] paramArrayOfInt) {
		if ((paramInt == 0) && (paramArrayOfInt.length == 1) && (paramArrayOfInt[0] == 0)) {
			o();
		}
	}

	private static abstract interface a {
		public static final String[] a = { "data1", "is_primary" };
	}

	public class b extends AsyncTask<Void, Void, Boolean> {
		private final String b;
		private final String c;

		protected Boolean a(Void... paramVarArgs) {
			for (;;) {
				int i;
				try {
					Thread.sleep(2000L);
					paramVarArgs = LoginActivity.k();
					int j = paramVarArgs.length;
					i = 0;
					if (i >= j) {
						break;
					}
					String[] arrayOfString = paramVarArgs[i].split(":");
					if (arrayOfString[0].equals(this.b)) {
						return Boolean.valueOf(arrayOfString[1].equals(this.c));
					}
				} catch (InterruptedException paramVarArgs) {
					return Boolean.valueOf(false);
				}
				i += 1;
			}
			return Boolean.valueOf(true);
		}

		protected void a(Boolean paramBoolean) {
			LoginActivity.a(this.a, null);
			LoginActivity.a(this.a, false);
			if (paramBoolean.booleanValue()) {
				this.a.finish();
				return;
			}
			LoginActivity.i(this.a).setError(this.a.getString(2131492902));
			LoginActivity.i(this.a).requestFocus();
		}

		protected void onCancelled() {
			LoginActivity.a(this.a, null);
			LoginActivity.a(this.a, false);
		}
	}
}
