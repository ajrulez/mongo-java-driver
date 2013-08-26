/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.operation;

import org.mongodb.Codec;
import org.mongodb.CommandResult;
import org.mongodb.Document;
import org.mongodb.codecs.DocumentCodec;
import org.mongodb.codecs.PrimitiveCodecs;
import org.mongodb.command.Command;
import org.mongodb.connection.BufferProvider;
import org.mongodb.connection.ClusterDescription;
import org.mongodb.connection.ServerSelector;
import org.mongodb.operation.protocol.CommandProtocol;
import org.mongodb.session.ServerConnectionProviderOptions;
import org.mongodb.session.Session;

import static org.mongodb.operation.CommandReadPreferenceHelper.getCommandReadPreference;
import static org.mongodb.operation.CommandReadPreferenceHelper.isQuery;

public class CommandOperation extends OperationBase<CommandResult> {
    private final DocumentCodec commandEncoder = new DocumentCodec(PrimitiveCodecs.createDefault());
    private final Command command;
    private final Codec<Document> codec;
    private final String database;
    private final ClusterDescription clusterDescription;

    public CommandOperation(final String database, final Command command, final Codec<Document> codec,
                            final ClusterDescription clusterDescription, final BufferProvider bufferProvider,
                            final Session session, final boolean closeSession) {
        super(bufferProvider, session, closeSession);
        this.database = database;
        this.clusterDescription = clusterDescription;
        this.command = command;
        this.codec = codec;
    }

    @Override
    public CommandResult execute() {
        try {
            final ServerConnectionProviderOptions options = new ServerConnectionProviderOptions(isQuery(command.toDocument()),
                                                                                                getServerSelector());
            final ServerConnectionProvider provider = getSession().createServerConnectionProvider(options);
            return new CommandProtocol(database, command.toDocument(), commandEncoder, codec,
                                       getBufferProvider(), provider.getServerDescription(), provider.getConnection(), true).execute();
        } finally {
            if (isCloseSession()) {
                getSession().close();
            }
        }
    }

    private ServerSelector getServerSelector() {
        return new ReadPreferenceServerSelector(getCommandReadPreference(command, clusterDescription));
    }
}
