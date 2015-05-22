package org.overture.codegen.vdm2jml;

public abstract class AbstractTypeInfo
{
	protected boolean optional;

	public AbstractTypeInfo(boolean optional)
	{
		this.optional = optional;
	}

	public void makeOptional()
	{
		this.optional = true;
	}

	abstract public boolean allowsNull();
}
