package com.elephant.ediyou.util.alipay;

public class PartnerConfig {

	// 合作商户ID。用签约支付宝账号登录ms.alipay.com后，在账户信息页面获取。
	public static final String PARTNER = "2088901291567284";
	// 商户收款的支付宝账号
	public static final String SELLER = "account2@ediyou.cn";
	// 商户（RSA）私钥
	public static final String RSA_PRIVATE = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAM3oYtXe7LIbN/dJeTXCiYQ2+E6TvQ/HTac4cshf/+8rMNFMWSehMI9LYpBXSgQ1gSXWP/y65CLGQVWTMA49K0CuZsaBU32zIBeZOE3rHKv9pjQVojuMu+378ircgMcm7yBy3U3bh5/JtzAy/GHywcjycCRlyeG+UHrd+25pjrltAgMBAAECgYA0u58URtM3ieD7Y8W3DutJAQr2lL+GmvQ4Lyy2RF+3UMhf0b3DeHSHPjcv4CsEiO9aUVRx1Ss3/K4ezGMyQlgg8trJ6kbtR9xz+pCXcs9ruT6L19wU3F9hWrdwMnx6gteCNUEIOlj32PpVnrWxpLcybFgw1IqpxGC39uWXLjafwQJBAO5hACRqEIOJmEqGjtRTfsEOpGbW4X86FWW6tr/u/wBad3ZhsrYtSNTc662JFOgU8+W+sR1MFWaGtVatDjh+A98CQQDdIOmr9/Qo2awapMgREW974uSC/W4t5m0N6Deujqe0GB8m5Y8DqCfToS7oW94Wf5NZKRLvupcoCkEkQy3caowzAkApYbt1ltgRDaTbYyqOX4REnXcaxzjULlAindw1y5aHCC1u5pzwlUHsegGL2Z1AgMqqWoVCsb+RUddPGbG7Stz/AkBot/lxEw9xeQPj6j1Et/kPtjd5QQM4gXMLrvkPlUceJn46MWQ02yFhO2e3aioWhXwGMUlidf63W4PV5GqHg325AkAXBfcptHAMysM8KqZFwb6ZYzP2Yvk4nnZqDGAy2I7Xj+1X3B6sV3XqSTM+oLK2qMk5yKKQEKgNaPM3W29LoVt2";
	// 支付宝（RSA）公钥 用签约支付宝账号登录ms.alipay.com后，在密钥管理页面获取。
	public static final String RSA_ALIPAY_PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCW2TuERYxLsHPC0caYK3sep2VOW/W8yw8HMuQg 4ZVpgc/cgIKo+cO41z+DQ3IKZZ6zs1jWoizyJyjAHgjBoXph7iGRz++s5pU+uy04uLFosnn3h88q8WKtWJgG5nig4VnuF9wQd/3cDP/Rxzqjd34FxcFXl0weWajz3PVWq+HfDwIDAQAB";
	// 支付宝安全支付服务apk的名称，必须与assets目录下的apk名称一致
	public static final String ALIPAY_PLUGIN_NAME = "Alipay_msp_online.apk";

}
