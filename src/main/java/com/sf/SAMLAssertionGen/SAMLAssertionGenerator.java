package com.sf.SAMLAssertionGen;

import com.sf.SAMLAssertionGen.GenerateSamlToken.GenerateSaml;

public class SAMLAssertionGenerator {

	public static void main(String[] args)  {
		GenerateSaml generate = new GenerateSaml();
		generate.generatesaml(args);
	}
}