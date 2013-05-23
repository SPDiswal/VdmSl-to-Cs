package org.overture.ast.intf.lex;

public interface ILexIdentifierToken extends ILexToken
{


	public ILexNameToken getClassName();

	public boolean getOld();

	
	ILexIdentifierToken clone();



	public boolean isOld();

	public String getName();

	
	public ILexLocation getLocation();


	
	
	

}
