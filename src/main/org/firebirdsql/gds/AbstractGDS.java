package org.firebirdsql.gds;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;

/**
 * Created by IntelliJ IDEA.
 * User: Ryan Baldwin
 * Date: 26-May-2003
 * Time: 02:14:22
 * To change this template use Options | File Templates.
 */
public abstract class AbstractGDS implements GDS, Externalizable
	{
	public AbstractGDS()
		{

		}
	public AbstractGDS(GDSFactory.GdsType gdsType)
		{
		this.gdsType = gdsType;
		}

	public void writeExternal(ObjectOutput out) throws IOException
		{
		out.writeObject(gdsType);
		}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
		{
		gdsType = (GDSFactory.GdsType)in.readObject();
		}

	public Object readResolve(  )
		{
	    return GDSFactory.getGDSForType(gdsType);
		}

	public void close()
		{
		}

	private GDSFactory.GdsType gdsType;
	}
