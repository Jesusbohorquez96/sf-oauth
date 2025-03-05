package com.sf.SAMLAssertionGen;

import com.sf.SAMLAssertionGen.GenerateSamlToken.GenerateSaml;

public class SAMLAssertionGenerator {

	public static void main(String[] args)  {
		GenerateSaml generator = new GenerateSaml();

		String token = generator.generatesaml(
				"https://example.com/oauth/token",
				"your-client-id",
				"your-private-key",
				"your-user-api",
				"your-company-id"
		);
		System.out.println("Generated OAuth Token: " + token);
	}
}