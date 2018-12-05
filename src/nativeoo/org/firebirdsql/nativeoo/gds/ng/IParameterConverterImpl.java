package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ConnectionParameterBuffer;
import org.firebirdsql.gds.ParameterTagMapping;
import org.firebirdsql.gds.ng.AbstractConnection;
import org.firebirdsql.gds.ng.AbstractParameterConverter;
import org.firebirdsql.gds.ng.IAttachProperties;
import org.firebirdsql.gds.ng.WireCrypt;

import java.sql.SQLException;

public class IParameterConverterImpl extends AbstractParameterConverter<NativeDatabaseConnection, IServiceConnectionImpl> {

    @Override
    protected void populateAuthenticationProperties(final AbstractConnection connection,
                                                    final ConnectionParameterBuffer pb) throws SQLException {
        IAttachProperties props = connection.getAttachProperties();
        ParameterTagMapping tagMapping = pb.getTagMapping();
        if (props.getUser() != null) {
            pb.addArgument(tagMapping.getUserNameTag(), props.getUser());
        }
        if (props.getPassword() != null) {
            pb.addArgument(tagMapping.getPasswordTag(), props.getPassword());
        }

        if (props.getWireCrypt() != WireCrypt.DEFAULT) {
            // Need to do this differently when having to add multiple configs
            String configString = "WireCrypt = " + props.getWireCrypt();
            pb.addArgument(tagMapping.getConfigTag(), configString);
        }
    }
}
