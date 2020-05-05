---
layout: posts
title:  CSCBE19 - Cert Trouble (Crypto)
date:   2020-05-05
categories: [CTFs, cscbe19]
---

[~$ cd ..](/ctfs/cscbe19/2020/05/05/index.html)

We didn't manage to solve this challenge during the event, but found the solution afterwards.

We were given the following [XML file](/assets/res/CTFs/cscbe19/cert_trouble/cert_trouble.xml):

> ```xml
><data>
>    this is not a flag
>    <ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
>        <ds:SignedInfo>
>            <ds:CanonicalizationMethod Algorithm="http://www.w3.org/2006/12/xml-c14n11"/>
>            <ds:SignatureMethod Algorithm="http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"/>
>            <ds:Reference URI="">
>                <ds:Transforms>
>                    <ds:Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature"/>
>                    <ds:Transform Algorithm="http://www.w3.org/2006/12/xml-c14n11"/>
>                </ds:Transforms>
>                <ds:DigestMethod Algorithm="http://www.w3.org/2001/04/xmlenc#sha256"/>
>                <ds:DigestValue>P+Kg7g77+CCL44r3+Uzb3i38pZixeKoWXvbPawyAp+Q=</ds:DigestValue>
>            </ds:Reference>
>        </ds:SignedInfo>
>        <ds:SignatureValue>
>            FUsF0F88fy3teCN6MKpACtXoFQYrZDa3jMt+08PNgYtcTnfKrwtqM+jS6Una3nXxuIEyWCL+iWFlUsDq7UCcICEXDjGsCo94Lvrgiu6JBFtDSrEZNbhgqlRPDOlVqQdKp54PuVzgGBOR1ySKNW2l6T8elkUtWwIr1shNobnLnXXyhSpVZQyfyRKzoB5Q4cz9MdHe0O0zs/9NKFEOdbVmEawdtLPPzD5TeUGzd3lyxhwBjI24WgG3eIz5rZuTpFRTiH51dSLuMOW9WWf0FrHTFAyQ57XrwXhpIZuP+X9vIechOqxj4ZW6loRvtr1pXulLtUsPBTCeRMb+WbpjuJinhw==
>        </ds:SignatureValue>
>        <ds:KeyInfo>
>            <ds:X509Data>
>                <ds:X509Certificate>
>MIIDnjCCAoagAwIBAgIJAKBLPcL3x/S2MA0GCSqGSIb3DQEBCwUAMGQxCzAJBgNV
>BAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBX
>aWRnaXRzIFB0eSBMdGQxHTAbBgNVBAMMFFRoaXMgaXMgbm90IHRoZSBmbGFnMB4X
>DTE5MDEyNjExMzIxNloXDTIwMDEyNjExMzIxNlowZDELMAkGA1UEBhMCQVUxEzAR
>BgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0IFdpZGdpdHMgUHR5
>IEx0ZDEdMBsGA1UEAwwUVGhpcyBpcyBub3QgdGhlIGZsYWcwggEiMA0GCSqGSIb3
>DQEBAQUAA4IBDwAwggEKAoIBAQCeTBLgJldxMO5Gzd5x6TK55oyRRWfMJGIFtl4C
>iYQuDuj4vZYn3Opz6juNPfU/KhikfbwKnkOPD2L3JmHzLodocEyfwhAKPBogacRZ
>TbY6LzGv4BJcAsN9nkPWrni3ZZlmyAELTreEu/lmpije8ZizKFa2WLhAvHucCJGE
>GsqIH7RDjDbMl68UEhdkTXKOY/lFqyJupwN+piTmX31wt5jqP52u/bt+00jQOpgX
>U+wR80IkuxUcc6TR4bcmSGxjk6EfMm+jh8dGeKkfOnj7nxNj6EFycPRyYSqxrNP5
>dwM3hcfsawOZBqFjdblb64VXNWVLm6tm5i0zC7XPyVI8CBStAgMBAAGjUzBRMB0G
>A1UdDgQWBBRUxFe6CsUKvE/0Bwfm5kzpSq4ygTAfBgNVHSMEGDAWgBRUxFe6CsUK
>vE/0Bwfm5kzpSq4ygTAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IB
>AQAHMzWpDFdkyZwdrEJ7sb9yG/Pj4wPeV+3+2O/wE79BzWdyy4HlvXrVwEScKocO
>QGiQFce1NUuFcsFV7yRK1lPF7wL2plyeWFFLMVXXIbM1fSSSM9UUK0n3OleLySRO
>h02yBH/UF1YTP4AVFiSNac/wkeReF3sriNijG2e8BJI/5Hb3aW84lkWZ3o7MdZEe
>lso+gfCZiS7VRKGcHlr+P8kCTZuvF4lvareYrPwemIP3wAF2xDUFIb8UIw2fFcbb
>mk25g8bsFf4mpyLs2VjiPYr2twu/myxmrQvbGl/GnkiCyKqR/jmDTfTJd4QLx6Lr
>RJjuXnWI3fLdlDMmK7PpMMAq
>                </ds:X509Certificate>
>            </ds:X509Data>
>        </ds:KeyInfo>
>    </ds:Signature>
></data>
> ```

We didn't really know what we were supposed to look for, so we first extracted the [certificate](/assets/res/CTFs/cscbe19/cert_trouble/cert.pem):

> ```
>-----BEGIN CERTIFICATE-----
>MIIDnjCCAoagAwIBAgIJAKBLPcL3x/S2MA0GCSqGSIb3DQEBCwUAMGQxCzAJBgNV
>BAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5ldCBX
>aWRnaXRzIFB0eSBMdGQxHTAbBgNVBAMMFFRoaXMgaXMgbm90IHRoZSBmbGFnMB4X
>DTE5MDEyNjExMzIxNloXDTIwMDEyNjExMzIxNlowZDELMAkGA1UEBhMCQVUxEzAR
>BgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoMGEludGVybmV0IFdpZGdpdHMgUHR5
>IEx0ZDEdMBsGA1UEAwwUVGhpcyBpcyBub3QgdGhlIGZsYWcwggEiMA0GCSqGSIb3
>DQEBAQUAA4IBDwAwggEKAoIBAQCeTBLgJldxMO5Gzd5x6TK55oyRRWfMJGIFtl4C
>iYQuDuj4vZYn3Opz6juNPfU/KhikfbwKnkOPD2L3JmHzLodocEyfwhAKPBogacRZ
>TbY6LzGv4BJcAsN9nkPWrni3ZZlmyAELTreEu/lmpije8ZizKFa2WLhAvHucCJGE
>GsqIH7RDjDbMl68UEhdkTXKOY/lFqyJupwN+piTmX31wt5jqP52u/bt+00jQOpgX
>U+wR80IkuxUcc6TR4bcmSGxjk6EfMm+jh8dGeKkfOnj7nxNj6EFycPRyYSqxrNP5
>dwM3hcfsawOZBqFjdblb64VXNWVLm6tm5i0zC7XPyVI8CBStAgMBAAGjUzBRMB0G
>A1UdDgQWBBRUxFe6CsUKvE/0Bwfm5kzpSq4ygTAfBgNVHSMEGDAWgBRUxFe6CsUK
>vE/0Bwfm5kzpSq4ygTAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IB
>AQAHMzWpDFdkyZwdrEJ7sb9yG/Pj4wPeV+3+2O/wE79BzWdyy4HlvXrVwEScKocO
>QGiQFce1NUuFcsFV7yRK1lPF7wL2plyeWFFLMVXXIbM1fSSSM9UUK0n3OleLySRO
>h02yBH/UF1YTP4AVFiSNac/wkeReF3sriNijG2e8BJI/5Hb3aW84lkWZ3o7MdZEe
>lso+gfCZiS7VRKGcHlr+P8kCTZuvF4lvareYrPwemIP3wAF2xDUFIb8UIw2fFcbb
>mk25g8bsFf4mpyLs2VjiPYr2twu/myxmrQvbGl/GnkiCyKqR/jmDTfTJd4QLx6Lr
>RJjuXnWI3fLdlDMmK7PpMMAq
>-----END CERTIFICATE-----
> ```

and in a readable format:

> ```
>$ openssl x509 -in cert.pem -text -noout
>Certificate:
>    Data:
>        Version: 3 (0x2)
>        Serial Number:
>            a0:4b:3d:c2:f7:c7:f4:b6
>    Signature Algorithm: sha256WithRSAEncryption
>        Issuer: C = AU, ST = Some-State, O = Internet Widgits Pty Ltd, CN = This is not the flag
>        Validity
>            Not Before: Jan 26 11:32:16 2019 GMT
>            Not After : Jan 26 11:32:16 2020 GMT
>        Subject: C = AU, ST = Some-State, O = Internet Widgits Pty Ltd, CN = This is not the flag
>        Subject Public Key Info:
>            Public Key Algorithm: rsaEncryption
>                Public-Key: (2048 bit)
>                Modulus:
>                    00:9e:4c:12:e0:26:57:71:30:ee:46:cd:de:71:e9:
>                    32:b9:e6:8c:91:45:67:cc:24:62:05:b6:5e:02:89:
>                    84:2e:0e:e8:f8:bd:96:27:dc:ea:73:ea:3b:8d:3d:
>                    f5:3f:2a:18:a4:7d:bc:0a:9e:43:8f:0f:62:f7:26:
>                    61:f3:2e:87:68:70:4c:9f:c2:10:0a:3c:1a:20:69:
>                    c4:59:4d:b6:3a:2f:31:af:e0:12:5c:02:c3:7d:9e:
>                    43:d6:ae:78:b7:65:99:66:c8:01:0b:4e:b7:84:bb:
>                    f9:66:a6:28:de:f1:98:b3:28:56:b6:58:b8:40:bc:
>                    7b:9c:08:91:84:1a:ca:88:1f:b4:43:8c:36:cc:97:
>                    af:14:12:17:64:4d:72:8e:63:f9:45:ab:22:6e:a7:
>                    03:7e:a6:24:e6:5f:7d:70:b7:98:ea:3f:9d:ae:fd:
>                    bb:7e:d3:48:d0:3a:98:17:53:ec:11:f3:42:24:bb:
>                    15:1c:73:a4:d1:e1:b7:26:48:6c:63:93:a1:1f:32:
>                    6f:a3:87:c7:46:78:a9:1f:3a:78:fb:9f:13:63:e8:
>                    41:72:70:f4:72:61:2a:b1:ac:d3:f9:77:03:37:85:
>                    c7:ec:6b:03:99:06:a1:63:75:b9:5b:eb:85:57:35:
>                    65:4b:9b:ab:66:e6:2d:33:0b:b5:cf:c9:52:3c:08:
>                    14:ad
>                Exponent: 65537 (0x10001)
>        X509v3 extensions:
>            X509v3 Subject Key Identifier:
>                54:C4:57:BA:0A:C5:0A:BC:4F:F4:07:07:E6:E6:4C:E9:4A:AE:32:81
>            X509v3 Authority Key Identifier:
>                keyid:54:C4:57:BA:0A:C5:0A:BC:4F:F4:07:07:E6:E6:4C:E9:4A:AE:32:81
>
>            X509v3 Basic Constraints: critical
>                CA:TRUE
>    Signature Algorithm: sha256WithRSAEncryption
>         07:33:35:a9:0c:57:64:c9:9c:1d:ac:42:7b:b1:bf:72:1b:f3:
>         e3:e3:03:de:57:ed:fe:d8:ef:f0:13:bf:41:cd:67:72:cb:81:
>         e5:bd:7a:d5:c0:44:9c:2a:87:0e:40:68:90:15:c7:b5:35:4b:
>         85:72:c1:55:ef:24:4a:d6:53:c5:ef:02:f6:a6:5c:9e:58:51:
>         4b:31:55:d7:21:b3:35:7d:24:92:33:d5:14:2b:49:f7:3a:57:
>         8b:c9:24:4e:87:4d:b2:04:7f:d4:17:56:13:3f:80:15:16:24:
>         8d:69:cf:f0:91:e4:5e:17:7b:2b:88:d8:a3:1b:67:bc:04:92:
>         3f:e4:76:f7:69:6f:38:96:45:99:de:8e:cc:75:91:1e:96:ca:
>         3e:81:f0:99:89:2e:d5:44:a1:9c:1e:5a:fe:3f:c9:02:4d:9b:
>         af:17:89:6f:6a:b7:98:ac:fc:1e:98:83:f7:c0:01:76:c4:35:
>         05:21:bf:14:23:0d:9f:15:c6:db:9a:4d:b9:83:c6:ec:15:fe:
>         26:a7:22:ec:d9:58:e2:3d:8a:f6:b7:0b:bf:9b:2c:66:ad:0b:
>         db:1a:5f:c6:9e:48:82:c8:aa:91:fe:39:83:4d:f4:c9:77:84:
>         0b:c7:a2:eb:44:98:ee:5e:75:88:dd:f2:dd:94:33:26:2b:b3:
>         e9:30:c0:2a
> ```

We then extracted the [public key](/assets/res/CTFs/cscbe19/cert_trouble/pubkey)

> ```sh
>$ openssl x509 -inform pem -in cert.pem -pubkey -noout | tee pubkey
>-----BEGIN PUBLIC KEY-----
>MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnkwS4CZXcTDuRs3eceky
>ueaMkUVnzCRiBbZeAomELg7o+L2WJ9zqc+o7jT31PyoYpH28Cp5Djw9i9yZh8y6H
>aHBMn8IQCjwaIGnEWU22Oi8xr+ASXALDfZ5D1q54t2WZZsgBC063hLv5ZqYo3vGY
>syhWtli4QLx7nAiRhBrKiB+0Q4w2zJevFBIXZE1yjmP5RasibqcDfqYk5l99cLeY
>6j+drv27ftNI0DqYF1PsEfNCJLsVHHOk0eG3JkhsY5OhHzJvo4fHRnipHzp4+58T
>Y+hBcnD0cmEqsazT+XcDN4XH7GsDmQahY3W5W+uFVzVlS5urZuYtMwu1z8lSPAgU
>rQIDAQAB
>-----END PUBLIC KEY-----
> ```

The goal was not to break the public key, but to verifiy the [signature](/assets/res/CTFs/cscbe19/cert_trouble/sig) (knowing that it was a self-signed certificate). We put the signature we found in the XML in a new file, and ran:

> ```sh
>$ openssl base64 -d -in sig -out sigraw
>$ openssl rsautl -verify -in sigraw -inkey pubkey -pubin
>	RSA operation error
>	140395534970944:error:04070066:rsa routines:RSA_padding_check_PKCS1_type_1:bad fixed header decrypt:../crypto/rsa/rsa_pk1.c:88:
>	140395534970944:error:04067072:rsa routines:rsa_ossl_public_decrypt:padding check failed:../crypto/rsa/rsa_ossl.c:573:
> ```

The error message reports an issue regarding the padding. We knew that RSA in real world uses padding, so we added the expected parameter:

> ```sh
>$ openssl rsautl -verify -in sigraw -inkey pubkey -pubin -raw | strings
>	CSC{s3cr3t_s1gn4tur3s_f0r_th3_w1n}
>	26OFOY
> ```
