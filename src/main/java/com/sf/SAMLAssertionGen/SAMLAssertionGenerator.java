package com.sf.SAMLAssertionGen;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.AssertionMarshaller;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.security.SecurityConfiguration;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;
import java.io.*;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SAMLAssertionGenerator {

	public static void main(String[] args) throws Exception {
		{
			String tokenUrl = null, clientId = null, username = null, privateKey = null, userId = null;
			boolean useUserNameAsUserId = false;
			int expireInMinutes = 10;

			if (args.length == 0) {
				System.out.println("Property file path is not provided, exit!");
				return;
			}
			try {
				Properties properties = new Properties();
				BufferedReader bufferedReader = new BufferedReader(new FileReader(args[0]));
				properties.load(bufferedReader);
				tokenUrl = properties.getProperty("tokenUrl");
				clientId = properties.getProperty("clientId");
				userId = properties.getProperty("userId");
				username = properties.getProperty("userName");
				privateKey = properties.getProperty("privateKey");
				expireInMinutes = properties.getProperty("expireInMinutes") != null
						&& Integer.parseInt(properties.getProperty("expireInMinutes")) > 0
								? Integer.parseInt(properties.getProperty("expireInMinutes"))
								: 10;

				if ((userId == null || userId.trim().length() == 0) && (username != null && username.length() != 0)) {
					System.out.println("Using username as userId..");
					userId = username;
					useUserNameAsUserId = true;
				}
				if (tokenUrl != null && clientId != null && privateKey != null && userId != null) {
					System.out.println("All properties are set, generating the SAML Assertion...");

					String signedSAMLAssertion = generateSignedSAMLAssertion(clientId, userId, tokenUrl, privateKey,
							expireInMinutes, useUserNameAsUserId);

					System.out.println("The generated Signed SAML Assertion is:");
					System.out.println(signedSAMLAssertion);
					try
					{
						String token = getOAuthToken(signedSAMLAssertion);
						System.out.println(token);
					} catch (Exception e) {
						System.out.println("Fail to get OAuth Token due to " + e.getMessage());
					}

				} else {
					System.out.println("One or more parameter is not provided, exit!");
				}
			} catch (Exception e) {
				System.out.println("Fail to generate SAML Assertion due to " + e.getMessage());
			}
		}
	}

	public static String generateSignedSAMLAssertion(String clientId, String username, String tokenUrl,
			String privateKeyString, int expireInMinutes, boolean userUserNameAsUserId) throws Exception {

		Assertion unsignedAssertion = buildDefaultAssertion(clientId, username, tokenUrl, expireInMinutes,
				userUserNameAsUserId);
		PrivateKey privateKey = generatePrivateKey(privateKeyString);
		Assertion assertion = sign(unsignedAssertion, privateKey);
		String signedAssertion = getSAMLAssertionString(assertion);

		return signedAssertion;
	}

	private static Assertion buildDefaultAssertion(String clientId, String userId, String tokenUrl, int expireInMinutes,
			boolean userUserNameAsUserId) {
		try {
			DateTime currentTime = new DateTime();
			DefaultBootstrap.bootstrap();

			Assertion assertion = create(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
			assertion.setIssueInstant(currentTime);
			assertion.setID(UUID.randomUUID().toString());
			assertion.setVersion(SAMLVersion.VERSION_20);
			Namespace xsNS = new Namespace("http://www.w3.org/2001/XMLSchema", "xs");
			assertion.addNamespace(xsNS);
			Namespace xsiNS = new Namespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
			assertion.addNamespace(xsiNS);

			Issuer issuer = create(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
			issuer.setValue("www.successfactors.com");
			assertion.setIssuer(issuer);

			Subject subject = create(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
			NameID nameID = create(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
			nameID.setValue(userId);
			nameID.setFormat(NameIdentifier.UNSPECIFIED);
			subject.setNameID(nameID);
			SubjectConfirmation subjectConfirmation = create(SubjectConfirmation.class,
					SubjectConfirmation.DEFAULT_ELEMENT_NAME);
			subjectConfirmation.setMethod("urn:oasis:names:tc:SAML:2.0:cm:bearer");
			SubjectConfirmationData sconfData = create(SubjectConfirmationData.class,
					SubjectConfirmationData.DEFAULT_ELEMENT_NAME);
			sconfData.setNotOnOrAfter(currentTime.plusMinutes(expireInMinutes));
			sconfData.setRecipient(tokenUrl);
			subjectConfirmation.setSubjectConfirmationData(sconfData);
			subject.getSubjectConfirmations().add(subjectConfirmation);
			assertion.setSubject(subject);

			Conditions conditions = buildConditions(currentTime, expireInMinutes);

			AudienceRestriction audienceRestriction = create(AudienceRestriction.class,
					AudienceRestriction.DEFAULT_ELEMENT_NAME);
			Audience audience = create(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
			audience.setAudienceURI("www.successfactors.com");
			List<Audience> audienceList = audienceRestriction.getAudiences();
			audienceList.add(audience);
			List<AudienceRestriction> audienceRestrictions = conditions.getAudienceRestrictions();
			audienceRestrictions.add(audienceRestriction);
			assertion.setConditions(conditions);

			AuthnStatement authnStatement = create(AuthnStatement.class, AuthnStatement.DEFAULT_ELEMENT_NAME);
			authnStatement.setAuthnInstant(currentTime);
			authnStatement.setSessionIndex(UUID.randomUUID().toString());
			AuthnContext authContext = create(AuthnContext.class, AuthnContext.DEFAULT_ELEMENT_NAME);
			AuthnContextClassRef authnContextClassRef = create(AuthnContextClassRef.class,
					AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
			authnContextClassRef.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
			authContext.setAuthnContextClassRef(authnContextClassRef);
			authnStatement.setAuthnContext(authContext);
			assertion.getAuthnStatements().add(authnStatement);

			AttributeStatement attributeStatement = create(AttributeStatement.class,
					AttributeStatement.DEFAULT_ELEMENT_NAME);
			Attribute apiKeyAttribute = createAttribute("api_key", clientId);
			attributeStatement.getAttributes().add(apiKeyAttribute);
			assertion.getAttributeStatements().add(attributeStatement);

			if (userUserNameAsUserId) {
				AttributeStatement useUserNameAsUserIdStatement = create(AttributeStatement.class,
						AttributeStatement.DEFAULT_ELEMENT_NAME);
				Attribute useUserNameKeyAttribute = createAttribute("use_username", "true");
				useUserNameAsUserIdStatement.getAttributes().add(useUserNameKeyAttribute);
				assertion.getAttributeStatements().add(useUserNameAsUserIdStatement);
			}

			return assertion;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static <T> T create(Class<T> cls, QName qname) {
		return (T) ((XMLObjectBuilder) Configuration.getBuilderFactory().getBuilder(qname)).buildObject(qname);
	}

	private static Attribute createAttribute(String name, String value) {
		Attribute result = create(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
		result.setName(name);
		XSStringBuilder stringBuilder = (XSStringBuilder) Configuration.getBuilderFactory()
				.getBuilder(XSString.TYPE_NAME);
		XSString stringValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
		stringValue.setValue(value);
		result.getAttributeValues().add(stringValue);
		return result;
	}

	private static Conditions buildConditions(DateTime currentTime, int expireInMinutes) {

		Conditions conditions = create(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
		conditions.setNotBefore(currentTime.minusMinutes(10));
		conditions.setNotOnOrAfter(currentTime.plusMinutes(expireInMinutes));
		return conditions;
	}

	private static String getSAMLAssertionString(Assertion assertion) {
		AssertionMarshaller marshaller = new AssertionMarshaller();
		Element element = null;
		try {
			element = marshaller.marshall(assertion);
		} catch (MarshallingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		String unencodedSAMLAssertion = XMLHelper.nodeToString(element);
		Base64 base64 = new Base64();
		try {
			return base64.encodeToString(unencodedSAMLAssertion.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	private static Assertion sign(Assertion assertion, PrivateKey privateKey) throws Exception {
		BasicX509Credential credential = new BasicX509Credential();
		credential.setPrivateKey(privateKey);

		if (assertion.getSignature() != null) {
			throw new RuntimeException("SAML assertion is already signed");
		}

		if (privateKey == null) {
			throw new RuntimeException("Invalid X.509 private key");
		}

		try {
			Signature signature = (Signature) Configuration.getBuilderFactory()
					.getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);
			signature.setSigningCredential(credential);
			SecurityConfiguration secConfig = Configuration.getGlobalSecurityConfiguration();
			String keyInfoGeneratorProfile = null;
			SecurityHelper.prepareSignatureParams(signature, credential, secConfig, keyInfoGeneratorProfile);

			signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);

			assertion.setSignature(signature);
			Configuration.getMarshallerFactory().getMarshaller(assertion).marshall(assertion);
			Signer.signObject(signature);
		} catch (Exception e) {
			throw new Exception("Failure in signing the SAML2 assertion", e);
		}
		return assertion;
	}

	private static PrivateKey generatePrivateKey(String privateKeyString) {
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		String pk2 = privateKeyString;
		try {
			pk2 = new String(Base64.decodeBase64(pk2), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		String[] strs = pk2.split("###");
		if (null != strs && strs.length == 2) {
			privateKeyString = strs[0];
		}
		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyString));
		PrivateKey privateKey = null;
		try {
			privateKey = keyFactory.generatePrivate(privateKeySpec);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return privateKey;
	}

	private static Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		String configPath = "config.properties";

		try (InputStream input = new FileInputStream(configPath)) {
			properties.load(input);
		}
		return properties;
	}

	public static String getOAuthToken(String assertion) throws Exception {
		Properties properties = loadProperties();
		String tokenUrl = properties.getProperty("tokenUrl");
		String clientId = properties.getProperty("clientId");
		String companyId = properties.getProperty("companyId");

		String postData = "company_id=" + companyId +
				"&client_id=" + clientId +
				"&grant_type=urn:ietf:params:oauth:grant-type:saml2-bearer" +
				"&assertion=" + assertion;

		URL url = new URL(tokenUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setDoOutput(true);

		try (OutputStream os = conn.getOutputStream()) {
			os.write(postData.getBytes(StandardCharsets.UTF_8));
			os.flush();
		}

		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				StringBuilder response = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				return response.toString();
			}
		} else {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
				StringBuilder errorResponse = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					errorResponse.append(line);
				}
				throw new RuntimeException("Error en la solicitud OAuth: Código HTTP " + responseCode + "\n" + errorResponse);
			}
		}
	}
}