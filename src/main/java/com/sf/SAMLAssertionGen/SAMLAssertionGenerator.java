package com.sf.SAMLAssertionGen;

import com.sf.SAMLAssertionGen.GenerateSamlToken.GenerateSaml;

public class SAMLAssertionGenerator {

	public static void main(String[] args)  {
		GenerateSaml generator = new GenerateSaml();

		generator.generatesaml(
				"https://example.com/odata/token",
				"https://example.com/oauth/token",
				"your-client-id",
				"your-private-key",
				"your-user-api",
				"your-company-id"
		);
	}
}