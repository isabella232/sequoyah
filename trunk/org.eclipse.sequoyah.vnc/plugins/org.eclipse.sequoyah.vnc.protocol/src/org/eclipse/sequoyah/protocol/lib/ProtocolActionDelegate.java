/********************************************************************************
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Fabio Rigo
 *
 * Contributors:
 * Daniel Barboza Franco - Bug [233775] - Does not have a way to enter the session password for the vnc connection
 * Fabio Rigo - Bug [238191] - Enhance exception handling
 * Daniel Barboza Franco (Eldorado Research Institute) - Bug [233064] - Add reconnection mechanism to avoid lose connection with the protocol
 ********************************************************************************/
package org.eclipse.tml.protocol.lib;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.tml.protocol.lib.exceptions.InvalidDefinitionException;
import org.eclipse.tml.protocol.lib.exceptions.InvalidMessageException;
import org.eclipse.tml.protocol.lib.exceptions.ProtocolException;
import org.eclipse.tml.protocol.lib.exceptions.ProtocolInitException;
import org.eclipse.tml.protocol.lib.exceptions.ProtocolRawHandlingException;
import org.eclipse.tml.protocol.lib.internal.model.ClientModel;
import org.eclipse.tml.protocol.lib.internal.model.ServerModel;
import org.eclipse.tml.protocol.lib.msgdef.ProtocolMsgDefinition;

/**
 * DESCRIPTION: This class contains every method call that can be used by a
 * module that uses the protocol framework. <br>
 * 
 * RESPONSIBILITY: Provide an unique entry point for actions demanded from
 * elements outside the protocol framework domain.<br>
 * 
 * COLABORATORS: None.<br>
 * 
 * USAGE: Call any of the public static methods to trigger actions on the
 * protocol framework.<br>
 * 
 */
public class ProtocolActionDelegate {

	/**
	 * Starts a protocol, taking part as client in the communication
	 * 
	 * @param allMessages
	 *            A map containing all messages that belong to the protocol. The
	 *            message code as key
	 * @param incomingMessages
	 *            A collection containing the message ids of all incoming
	 *            messages. The message ids much match the id field in a
	 *            ProtocolMsgDefinition object from the allMessages map
	 * @param outgoingMessages
	 *            A collection containing the message ids of all outgoing
	 *            messages. The message ids much match the id field in a
	 *            ProtocolMsgDefinition object from the allMessages map
	 * @param protocolImplementer
	 *            An instance of the implementer object. This object contains
	 *            the initialization procedure definition, as well as any needed
	 *            protocol instance particular data
	 * @param isBigEndianProtocol
	 *            True if the protocol is big endian, false if little endian
	 * @param host
	 *            The host where the server is running
	 * @param port
	 *            The ported where the server is listening for requests at host
	 * @param parameters
	 *            A Map with parameters other than host and port, for customization purposes. Accepts null if apply.           
	 * 
	 * @throws UnknownHostException
	 *             DOCUMENT ME!!
	 * @throws IOException
	 *             DOCUMENT ME!!
	 * @throws ProtocolInitException
	 *             DOCUMENT ME!!
	 */
	
	public static void startClientProtocol(
			Map<Long, ProtocolMsgDefinition> allMessages,
			Collection<String> incomingMessages,
			Collection<String> outgoingMessages,
			IProtocolImplementer protocolImplementer,
			IProtocolExceptionHandler exceptionHandler,
			Boolean isBigEndianProtocol,
			String host, int port,
			Map<String, Object> parameters)
			throws UnknownHostException, IOException, ProtocolInitException {

		ClientModel model = ClientModel.getInstance();
		model.startClientProtocol(allMessages, incomingMessages,
				outgoingMessages, protocolImplementer, exceptionHandler, isBigEndianProtocol, host, port, parameters);
	}

	/**
	 * Starts a protocol, taking part as server in the communication
	 * 
	 * 
	 * @param portToBind
	 *            The local port where to bind the server to
	 * @param allMessages
	 *            A map containing all messages that belong to the protocol. The
	 *            message code as key
	 * @param incomingMessages
	 *            A collection containing the message ids of all incoming
	 *            messages. The message ids much match the id field in a
	 *            ProtocolMsgDefinition object from the allMessages map
	 * @param outgoingMessages
	 *            A collection containing the message ids of all outgoing
	 *            messages. The message ids much match the id field in a
	 *            ProtocolMsgDefinition object from the allMessages map
	 * @param protocolImplementer
	 *            An instance of the implementer object. This object contains
	 *            the initialization procedure definition, as well as any needed
	 *            protocol instance particular data
	 * @param isBigEndianProtocol
	 *            True if the protocol is big endian, false if little endian
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!!
	 * @throws ProtocolInitException
	 *             DOCUMENT ME!!
	 */
	public static void startServerProtocol(int portToBind,
			Map<Long, ProtocolMsgDefinition> allMessages,
			Collection<String> incomingMessages,
			Collection<String> outgoingMessages,
			IProtocolImplementer protocolImplementer,
			IProtocolExceptionHandler exceptionHandler,
			boolean isBigEndianProtocol) throws IOException,
			ProtocolInitException {

		ServerModel model = ServerModel.getInstance();
		model.startListeningToPort(portToBind, allMessages, incomingMessages,
				outgoingMessages, protocolImplementer, exceptionHandler,
				isBigEndianProtocol);
	}

	/**
	 * Stops the provided protocol instance
	 * 
	 * @param protocolImplementer
	 *            The protocol instance that is to be stopped
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!!
	 */
	public static void stopProtocol(IProtocolImplementer protocolImplementer)
			throws IOException {
		ClientModel clientModel = ClientModel.getInstance();
		clientModel.stopClientProtocol(protocolImplementer);

		ServerModel serverModel = ServerModel.getInstance();
		serverModel.stopListeningToPort(protocolImplementer);
	}

	/**
	 * Restarts the provided protocol instance
	 * 
	 * @param protocolImplementer
	 *            The protocol instance that is to be restarted
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!!
	 * @throws ProtocolInitException
	 *             DOCUMENT ME!!
	 */
	public static void restartProtocol(IProtocolImplementer protocolImplementer)
			throws IOException, ProtocolInitException, ProtocolException {

		ClientModel clientModel = ClientModel.getInstance();
		clientModel.restartClientProtocol(protocolImplementer);

		ServerModel serverModel = ServerModel.getInstance();
		serverModel.restartServerProtocol(protocolImplementer);
	}

	/**
	 * Sends a message to the server part
	 * 
	 * @param protocolImplementer
	 *            The client protocol instance that is to send a message to the
	 *            server it is connected to
	 * 
	 * @param message
	 *            The message to send to the server
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!!
	 * @throws ProtocolRawHandlingException
	 *             DOCUMENT ME!!
	 * @throws InvalidMessageException
	 *             DOCUMENT ME!!
	 * @throws InvalidDefinitionException
	 *             DOCUMENT ME!!
	 */
	public static void sendMessageToServer(
			IProtocolImplementer protocolImplementer, ProtocolMessage message)
			throws IOException, ProtocolRawHandlingException,
			InvalidMessageException, InvalidDefinitionException {
		ClientModel model = ClientModel.getInstance();
		model.sendMessage(protocolImplementer, message);
	}
}