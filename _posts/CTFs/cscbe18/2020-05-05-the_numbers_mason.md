---
layout: posts
title:  CSCBE18 - The numbers, Mason!
date:   2020-05-05
categories: [CTFs, cscbe18]
---

[~$ cd ..](../)

The challenge consists of an [APK file](/assets/res/CTFs/cscbe18/the_numbers_mason/notes.apk). As usual, we renamed it as a ".zip" in order to extract files. We then found the well-known [classes.dex](/assets/res/CTFs/cscbe18/the_numbers_mason/classes.dex) and used `dex2jar` to decompile.
## Step 1
The code we got was quite messy, but we located the two interesting classes [LoginActivity](/assets/res/CTFs/cscbe18/the_numbers_mason/LoginActivity.java) and [SuccessActivity](/assets/res/CTFs/cscbe18/the_numbers_mason/SuccessActivity.java) (shown as they appear in `jd-gui`).
In LoginActivity, we saw that some strings were generated using a weird algorithm:
> ```java
>public static final String l = a.a.a.a.a(123);
>public static final String m = a.a.a.a.a(124);
>public static final String n = a.a.a.a.a(125);
>private static final String[] o = { a.a.a.a.a(126), a.a.a.a.a(127) };
>private static String y = a.a.a.a.a(128);
> ```

We then looked into the class [a.a.a.a](/assets/res/CTFs/cscbe18/the_numbers_mason/a.a.a.a.java) and tried to retrieve all possible strings by recreating the [file](/assets/res/CTFs/cscbe18/the_numbers_mason/Decode.java) and got a list of 129 strings:

> ```
>0: UTF-8
>1: UTF-8
>2: AES
>3: AES/CBC/PKCS5PADDING
>4: dynamics.joint.sameBody
>5: UTF-8
>6: UTF-8
>7: AES
>8: AES/CBC/PKCS5PADDING
>...
>57: com.noshufou.android.su
>58: com.noshufou.android.su.elite
>59: eu.chainfire.supersu
>60: com.koushikdutta.superuser
>61: com.thirdparty.superuser
>62: com.yellowes.su
>63: com.koushikdutta.rommanager
>64: com.koushikdutta.rommanager.license
>65: com.dimonvideo.luckypatcher
>66: com.chelpus.lackypatch
>67: com.ramdroid.appquarantine
>68: com.ramdroid.appquarantinepro
>69: com.devadvance.rootcloak
>70: com.devadvance.rootcloakplus
>71: de.robv.android.xposed.installer
>72: com.saurik.substrate
>73: com.zachspong.temprootremovejb
>74: com.amphoras.hidemyroot
>75: com.amphoras.hidemyrootadfree
>76: com.formyhm.hiderootPremium
>77: com.formyhm.hideroot
>78: /data/local/
>79: /data/local/bin/
>80: /data/local/xbin/
>81: /sbin/
>82: /su/bin/
>83: /system/bin/
>84: /system/bin/.ext/
>85: /system/bin/failsafe/
>86: /system/sd/xbin/
>87: /system/usr/we-need-root/
>88: /system/xbin/
>89: /system
>90: /system/bin
>91: /system/sbin
>92: /system/xbin
>93: /vendor/bin
>94: /sbin
>95: /etc
>96: notes.challenges.cybersecuritychallenge.be
>97: sha256/MaQXQRucvxgWT5IVeVwJvaq8Jz+tI7MyQPp8/LxTsco=
>98: SESSION
>99: KEY
>100: super-awesome-notes-v2
>101: session
>102: https://notes.challenges.cybersecuritychallenge.be/
>103: notes.challenges.cybersecuritychallenge.be
>104: sha256/MaQXQRucvxgWT5IVeVwJvaq8Jz+tI7MyQPp8/LxTsco=
>105: KEY
>106: SESSION
>107: init
>108: pin
>109: username
>110: 12AV59BC29IE02CD
>111: m
>112: login
>113: session
>114: POST
>115: android.permission.READ_CONTACTS
>116: android.permission.READ_CONTACTS
>117: android.permission.READ_CONTACTS
>118: @
>119: data
>120: mimetype = ?
>121: vnd.android.cursor.item/email_v2
>122: is_primary DESC
>123: 12AV59BC29IE02CD
>124: KEY
>125: SESSION
>126: foo@example.com:hello
>127: bar@example.com:world
>128: https://notes.challenges.cybersecuritychallenge.be/  
> ```

However, a big part of the code was absolutely incomprehensible, since we didn't have the source of the libraries used by the app.
Fortunately, there was a directory named "okhttp3" among the files given by the zip. We then downloaded it, and began to rebuild the
project in AndroidStudio (okhttp3, okio, and conscrypt were also necessary to run the program). Once done, it was easier to debug and understand what this weird APK did.
However, "easier" doesn't mean "easy"!
We began with `onCreate()`, and tried to rewrite a readable code:
> ```java
>this.z = new w.a().a(new g.a().a(a.a.a.a.a(103), new String[] { a.a.a.a.a(104) }).a()).a();
> ```

stands for:

> ```java
>this.client = new OkHttpClient.Builder().certificatePinner(new CertificatePinner.Builder().add("notes.challenges.cybersecuritychallenge.be", new String[] { "sha256/MaQXQRucvxgWT5IVeVwJvaq8Jz+tI7MyQPp8/LxTsco="}).build()).build();
> ```

which is a crucial element of the code.
We then did the same for the routine `n()` (called in `onCreate()` line 475):

> ```java
>private void n() {
>	Request req = new Request.Builder().url("https://notes.challenges.cybersecuritychallenge.be/init").build();
>	this.client.newCall(req).enqueue(new Callback() {
>		public void onResponse(e paramAnonymouse, c.ab paramAnonymousab) {???}
>		public void onFailure(Call call, IOException ioe) {
>			LoginActivity.this.runOnUiThread(new Runnable() {
>				public void run() {
>					Toast.makeText(LoginActivity.this.getApplicationContext(), "Connection issue!", 0).show();
>				}
>			});
>		}
>	});
>}
> ```

By making a GET request on this URL, we get something like this:

> ```
>{
>  "key": "24253af5eb005eef448b3f4311125118",
>  "session": "172fc591068b73a709527cd2ce6e294d"
>}
> ```

Quite interesting, because we have the strings "KEY" and "SESSION"!
We then continued to analyze the code by rewriting the validation process in the callback routine (line 479).
We knew that the app used the library android-pinpad [(com.hextremelabs.pinpad)](https://github.com/hextremelabs/android-pinpad), and then we rewrote the
following piece of code:

> ```java
>this.pinpad.setCallback(new PinpadView.Callback() {
>	public void onHelpRequest() {}
>	public void onPasscodeComplete(String password) {
>		String login = this.login.getText().toString();
>		...
>		try {
>			login(key, session, login, password);
>			return;
>		} catch (JSONException paramAnonymousString) {
>			paramAnonymousString.printStackTrace();
>		}
>	}
>});
> ```

We guessed that the two first parameters were the key and the session we previously got, because in the routine `a(...)̀` / `login(...)`  we had:

> ```java
>this.w = paramString1;
>this.x = paramString2;
>```

and in another routine `l()` (line 143):

> ```java
>private void l(){
>    Intent localIntent = new Intent(this, SuccessActivity.class);
>    localIntent.putExtra(a.a.a.a.a(105), this.w); // a.a.a.a.a(105) = KEY
>    localIntent.putExtra(a.a.a.a.a(106), this.x); // a.a.a.a.a(105) = SESSION
>    startActivity(localIntent);
>}
> ```

We had then to recover the routine `a(...)` / `login(...)`, which became:

> ```java
>public void login(String key, String session, String login, String pin){
>	this.key = key;
>	this.session = session;
>	JSONObject jsonO = new JSONObject();
>	jsonO.put("pin", ???);
>	jsonO.put("username", "admin"); // we were not 100% sure, but it was the most likely username
>	String ciphertext = cipher(key, jsonO.toString(), true);
>	FormBody form = new FormBody.Builder().add("m", res).build();
>	Request req = new Request.Builder().url("https://notes.challenges.cybersecuritychallenge.be/" + "login").addHeader("session", session).method("POST", form).build();
> ```

where `cipher()` is (modified version; the 2nd parameter was always the constant "12AV59BC29IE02CD"):

> ```java
>public static String cipher(String keyString, String text, boolean encrypt){
>	try {
>		IvParameterSpec iv = new IvParameterSpec("12AV59BC29IE02CD".getBytes("UTF-8"));
>       SecretKeySpec key = new SecretKeySpec(keyString.getBytes("UTF-8"), "AES");
>       Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
>       if (encrypt){
>			localCipher.init(Cipher.ENCRYPT_MODE, key, iv);
>           return Base64.encodeToString(localCipher.doFinal(text.getBytes()), 0);
>       }
>       else{
>			byte[] ciphertext = Base64.decode(text.getBytes(),0);
>           localCipher.init(Cipher.DECRYPT_MODE, key, iv);
>           return new String(localCipher.doFinal(ciphertext));
>		}
>	}
>   catch (Exception e){e.printStackTrace();}
>	return "nope";
>}
> ```

Okay, at this moment we knew that:
* the app sends a GET on /init to get a key and a session ID
* these two strings are used to encrypt the credentials
* in case of success, the `SuccessActivity` is launched. Let's rewrite it:

> ```java
>public class SuccessActivity extends Activity {
>	private static String url = "https://notes.challenges.cybersecuritychallenge.be/"
>	private OkHttpClient client;
>	private String session;
>	private String key;
>
>	private void nextpage() {
>		Request req = new Request.Builder().url("https://notes.challenges.cybersecuritychallenge.be/super-awesome-notes-v2").addHeader("session", session).build();
>		this.client.newCall(req).enqueue(new Callback() {
>            @Override
>            public void onFailure(Call call, IOException e) {
>				e.printStackTrace();
>            }
>
>            @Override
>            public void onResponse(Call call, Response response) throws IOException {
>                ???
>            }
>        });
>	}
>
>	protected void onCreate(Bundle paramBundle) {
>		...
>		this.client = new OkHttpClient.Builder().certificatePinner(new CertificatePinner.Builder().add("notes.challenges.cybersecuritychallenge.be", new String[] { "sha256/MaQXQRucvxgWT5IVeVwJvaq8Jz+tI7MyQPp8/LxTsco="}).build()).build();
>		paramBundle = getIntent();
>		this.session = paramBundle.getStringExtra("SESSION");
>		this.key = paramBundle.getStringExtra("KEY");
>		nextpage();
>	}
>}
> ```

## Step 2
We tried the PIN code `0000`, and got the following response:

> ```
> {
>	"m": "8ZYvqiUnfwU+ooyhp7PbHH/PYoNDyxAco7N/NkVL4pXPtGMymVqjusuzOsjHp42k\n"
>}
> ```

It didn't give us an ASCII string, so we tried to decrypt the answer (changed every time) using the key

Output:

> ```
>I/Response: +MBNwo2pf/xbXJo1Z/Cm6xwdMrc43rnGKkwD3gyMaOl1H/D6TpcQrg9p3U9PYzEU
>I/Decoded: {"key": "0940537021d8023a1ead1477b8157622"}
> ```

We then tried to reach the page `super-awesome-notes-v2`:
* init: get the first key and session ID
* encrypt credentials using the first key and send it to `/login`
* get a second key
* send a GET to `/super-awesome-notes/v2`
* use the second key to decrypt the response

Output:

> ```
>I/1st key: {
>	"key": "bfd947f44a846380d89a8d29d75b5cb8",
>   "session": "9539bd898e0657885c23c4a13a81e9d7"
>}
>I/JSON: {
>	"m": "MpmYOTEXvR2oY5C/qi/cUiE5GtRGb1vF6pwShxrzc8NFGohW76mXFzVGctqIWf41\n" <- /login resp.
>}
>I/2nd key: 9e84b05e742ae551213110e5990c7450 <- decrypted
>I/JSON: {
>	"m": "sKt1rGgY8kcgHsizf2JG4/flvV549AQte1OrZMKL7dU=\n" <- ./super-awesome-notes-v2 resp.
>}
>I/Next page: {"status": "access denied"} <- decrypted
> ```

We received an "Access denied", because the PIN code was wrong. We assumed that he username was "admin", and then tried to bruteforce the PIN code (only 10'000 possible codes),
and with 7169, we finally got:
**{"flag": "Note to self: Buy more dogecoin. It can only go up. CSCBE{SSLPinBruteForceRootEvasion}"}**

See [Full Android code](/assets/res/CTFs/cscbe18/the_numbers_mason/MainActivity.java)
