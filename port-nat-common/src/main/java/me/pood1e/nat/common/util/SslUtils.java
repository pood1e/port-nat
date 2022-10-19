package me.pood1e.nat.common.util;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * @author pood1e
 */
@Slf4j
public class SslUtils {

	private final static String DN = "CN=CN";
	private final static int DAYS = 365;

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static SslContext getClientSsl() {
		try {
			return SslContextBuilder.forClient().trustManager(new TrustAllManager()).build();
		} catch (SSLException e) {
			throw new RuntimeException(e);
		}
	}

	public static SslContext getServerSsl() {
		try {
			KeyPair keyPair = RSAUtils.getRsaKeypair();
			return SslContextBuilder.forServer(keyPair.getPrivate(), generate(keyPair))
					.trustManager(InsecureTrustManagerFactory.INSTANCE)
					.build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static X509Certificate generate(KeyPair keyPair) throws CertificateException {
		try {
			AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256WithRSAEncryption");
			AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
			AsymmetricKeyParameter asymmetricKeyParameter = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
			SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
			ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(asymmetricKeyParameter);
			X500Name name = new X500Name(DN);
			Date from = new Date();
			Date to = new Date(from.getTime() + DAYS * 86400000L);
			BigInteger sn = new BigInteger(64, new SecureRandom());
			X509v3CertificateBuilder v3CertGen = new X509v3CertificateBuilder(name, sn, from, to, name, subPubKeyInfo);
			X509CertificateHolder certificateHolder = v3CertGen.build(sigGen);
			return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(certificateHolder);
		} catch (Exception e) {
			throw new CertificateException(e);
		}
	}

	private static final class TrustAllManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

		}

		@Override
		public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	}
}



