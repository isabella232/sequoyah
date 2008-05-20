/********************************************************************************
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Fabio Rigo
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/
package org.eclipse.tml.protocol.internal.reader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tml.protocol.internal.model.ProtocolBean;
import org.eclipse.tml.protocol.lib.IMessageHandler;
import org.eclipse.tml.protocol.lib.IProtocolImplementer;
import org.eclipse.tml.protocol.lib.IRawDataHandler;
import org.eclipse.tml.protocol.lib.exceptions.ProtocolException;
import org.eclipse.tml.protocol.lib.msgdef.NullMessageHandler;
import org.eclipse.tml.protocol.lib.msgdef.ProtocolMsgDefinition;
import org.eclipse.tml.protocol.lib.msgdef.databeans.FixedSizeDataBean;
import org.eclipse.tml.protocol.lib.msgdef.databeans.IMsgDataBean;
import org.eclipse.tml.protocol.lib.msgdef.databeans.IteratableBlockDataBean;
import org.eclipse.tml.protocol.lib.msgdef.databeans.RawDataBean;
import org.eclipse.tml.protocol.lib.msgdef.databeans.VariableSizeDataBean;

/**
 * DESCRIPTION: This class is a reader for protocol framework data declared via
 * extension points. <br>
 * 
 * RESPONSIBILITY: Collect protocol framework data out of the contributed
 * protocolImplementer, protocolMessage and protocolMessageOrientation
 * extensions.<br>
 * 
 * COLABORATORS: None.<br>
 * 
 * USAGE: Call the public methods to retrieve data out of the extensions.<br>
 * 
 */
public class ProtocolExtensionsReader implements IExtensionConstants {

	/**
	 * Reads the definition of the protocol identified by the provided id
	 * 
	 * @param protocolId
	 *            The id of the protocol to have all data retrieved out of its
	 *            extension
	 * 
	 * @return A bean containing all data referring to the provided protocol
	 * 
	 * @throws ProtocolException
	 *             DOCUMENT ME!!
	 */
	public static ProtocolBean readProtocolImplDef(String protocolId)
			throws ProtocolException {
		// Create the bean
		ProtocolBean bean = new ProtocolBean();
		bean.setProtocolId(protocolId);

		// Retrieve the primitive data and set to bean
		IConfigurationElement confElem = getProtocolConfElem(protocolId);
		bean.setParentProtocol(getImmediateProtocolParent(protocolId));
		bean.setBigEndianProtocol(Boolean.parseBoolean(confElem
				.getAttribute(PROTOCOL_IS_BIG_ENDIAN_ATTR)));
		bean.setServerPort(Integer.parseInt(confElem
				.getAttribute(PROTOCOL_SERVER_PORT_ATTR)));

		// Instantiate the "seed" protocol implementer and set to the bean
		try {
			Object implementerSeedObj = confElem
					.createExecutableExtension(PROTOCOL_CLASS_ATTR);
			if (implementerSeedObj instanceof IProtocolImplementer) {
				bean
						.setProtocolImplementerSeed((IProtocolImplementer) implementerSeedObj);
			} else {
				throw new ProtocolException(
						"The protocol has not declared a valid implementer");
			}
		} catch (CoreException e) {
			throw new ProtocolException(e.getMessage(), e);
		}

		return bean;
	}

	/**
	 * Reads all protocolMessages extensions to collect the messages owned by
	 * the provided protocol and its parent protocols.
	 * 
	 * @param protocolId
	 *            The identifier of the protocol to read message definitions
	 *            from.
	 * 
	 * @return A map containing all protocol messages read, having its code as
	 *         key.
	 * 
	 * @throws ProtocolException
	 *             If the protocol extensions do not follow the specifications
	 *             or are badly formed.
	 */
	public static Map<Long, ProtocolMsgDefinition> readMessageDefinitions(
			String protocolId) throws ProtocolException {
		return getAllProtocolMessages(getAllParentProtocols(protocolId), true);
	}

	/**
	 * Reads all server message ids of the protocol identified by the provided
	 * id
	 * 
	 * @param protocolId
	 *            The identifier of the protocol to read server message ids
	 *            from.
	 * 
	 * @return A collection containing the ids of all messages that belongs to
	 *         the server part of the protocol
	 * 
	 * @throws ProtocolException
	 *             DOCUMENT ME!!
	 */
	public static Collection<String> readServerMessages(String protocolId)
			throws ProtocolException {
		return getMessagesOrientations(getAllParentProtocols(protocolId),
				PROTOCOL_MESSAGE_ORIENTATION_SERVER_ELEM);
	}

	/**
	 * Reads all client message ids of the protocol identified by the provided
	 * id
	 * 
	 * @param protocolId
	 *            The identifier of the protocol to read client message ids
	 *            from.
	 * 
	 * @return A collection containing the ids of all messages that belongs to
	 *         the client part of the protocol
	 * 
	 * @throws ProtocolException
	 *             DOCUMENT ME!!
	 */
	public static Collection<String> readClientMessages(String protocolId)
			throws ProtocolException {
		return getMessagesOrientations(getAllParentProtocols(protocolId),
				PROTOCOL_MESSAGE_ORIENTATION_CLIENT_ELEM);
	}

	/**
	 * Retrieves the immediate parent of the provided protocol.
	 * 
	 * @param protocolId
	 *            The protocol which parent is desired.
	 * 
	 * @return The parent protocol id.
	 */
	private static String getImmediateProtocolParent(String protocolId) {
		String parentId = null;
		IConfigurationElement protocolConfElem = getProtocolConfElem(protocolId);
		if (protocolConfElem != null) {
			parentId = protocolConfElem
					.getAttribute(PROTOCOL_PARENT_PROTOCOL_ATTR);
		}

		return parentId;
	}

	/**
	 * Retrieves the configuration element that describes the provided protocol.
	 * 
	 * @param protocolId
	 *            The protocol which configuration element is desired.
	 * 
	 * @return The base configuration element that describes the provided
	 *         protocol.
	 */
	private static IConfigurationElement getProtocolConfElem(String protocolId) {
		IConfigurationElement returnElement = null;

		// Get all Protocol extensions
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint protocolExtPoint = registry
				.getExtensionPoint(PROTOCOL_EXTENSION_POINT);
		IExtension[] allProtocolExtensions = protocolExtPoint.getExtensions();

		// Iterate on the protocol extensions to find the one identified by the
		// protocol parameter. To better understand the extension structure,
		// consult its schema.
		for (IExtension protocolExtension : allProtocolExtensions) {
			IConfigurationElement[] protocolConfArray = protocolExtension
					.getConfigurationElements();
			for (IConfigurationElement protocolConf : protocolConfArray) {
				if (protocolConf.getName().equals(PROTOCOL_ELEM)) {
					String extensionProtocolId = protocolConf
							.getAttribute(PROTOCOL_ID_ATTR);
					if (extensionProtocolId.equals(protocolId)) {
						returnElement = protocolConf;
						break;
					}
				}
			}

			// Interrupt search if the configuration element is found
			if (returnElement != null) {
				break;
			}
		}

		return returnElement;
	}

	/**
	 * Retrieve a list of all parents of the provided protocol.<br>
	 * <br>
	 * The list is provided in ascending order, being the first element the
	 * protocol itself, the second element its immediate parent, the third
	 * element the immediate parent of the protocol immediate parent and so on.
	 * The last element is the parent of all, which do not have parents itself.
	 * 
	 * @param protocolId
	 *            The protocol that we want all parents to be found.
	 * 
	 * @return The list of parents of the provided protocol.
	 */
	private static List<String> getAllParentProtocols(String protocolId) {
		List<String> allParents = new ArrayList<String>();
		allParents.add(protocolId);
		String aParent = protocolId;
		do {
			aParent = getImmediateProtocolParent(aParent);
			if (aParent != null) {
				allParents.add(aParent);
			}
		} while (aParent != null);

		return allParents;
	}

	/**
	 * Reads all messages that belongs to the provided list of protocols.
	 * 
	 * @param protocols
	 *            The list of protocols that we want to have messages read from.<br>
	 *            <b>Important</b>: The list must be sorted in a way that the
	 *            most important protocol is in the first position and the least
	 *            important protocol is in the last position. If a message with
	 *            given code was already read and another message with same code
	 *            follows it, the second message will <i>NOT</i> replace the
	 *            one already read. This approach guarantee that a overwritten
	 *            message will not be replaced the parent's version.
	 * 
	 * @return A map containing all protocol messages read, having its code as
	 *         key.
	 * 
	 * @throws ProtocolException
	 *             If the protocol extensions do not follow the specifications
	 *             or are badly formed.
	 */
	private static Map<Long, ProtocolMsgDefinition> getAllProtocolMessages(
			List<String> protocols, boolean readFields)
			throws ProtocolException {
		Map<Long, ProtocolMsgDefinition> messageDefCollection = new HashMap<Long, ProtocolMsgDefinition>();

		// Get all Protocol Message extensions
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint protocolMsgExtPoint = registry
				.getExtensionPoint(PROTOCOL_MESSAGE_EXTENSION_POINT);
		IExtension[] allProtocolMsgExtensions = protocolMsgExtPoint
				.getExtensions();

		// Collects the messages in the list order
		for (String protocol : protocols) {
			// Search all protocol message extensions for messages belonging to
			// "protocol"
			for (IExtension protocolMsgExtension : allProtocolMsgExtensions) {
				IConfigurationElement[] protocolMsgConfArray = protocolMsgExtension
						.getConfigurationElements();
				for (IConfigurationElement protocolMsgConf : protocolMsgConfArray) {
					String extensionProtocolId = protocolMsgConf
							.getAttribute(PROTOCOL_MESSAGE_PROTOCOL_ID_ATTR);
					String extensionMsgCode = protocolMsgConf
							.getAttribute(PROTOCOL_MESSAGE_CODE_ATTR);

					// A message is read if it belongs to the protocol AND a
					// message with same
					// code was not read yet
					if ((protocol.equals(extensionProtocolId))
							&& (!messageDefCollection
									.containsKey(extensionMsgCode))) {
						readMsgDefToCollection(protocolMsgConf,
								messageDefCollection, readFields);
					}
				}
			}
		}

		return messageDefCollection;
	}

	/**
	 * Reads a message defined by the given configuration element to the
	 * provided collection.
	 * 
	 * @param protocolMsgConf
	 *            The configuration element that defines the message.
	 * @param messageDefMap
	 *            The map to store the read message.
	 * 
	 * @throws ProtocolException
	 *             If the message do not follow the specifications or is badly
	 *             formed.
	 */
	private static void readMsgDefToCollection(
			IConfigurationElement protocolMsgConf,
			Map<Long, ProtocolMsgDefinition> messageDefMap, boolean readFields)
			throws ProtocolException {
		try {
			// Creates the bean and sets the message code to it
			ProtocolMsgDefinition bean = new ProtocolMsgDefinition();
			long code = Long.decode(protocolMsgConf
					.getAttribute(PROTOCOL_MESSAGE_CODE_ATTR));
			String id = protocolMsgConf.getAttribute(PROTOCOL_MESSAGE_ID_ATTR);

			boolean codeSigned = Boolean.parseBoolean(protocolMsgConf
					.getAttribute(PROTOCOL_MESSAGE_CODE_SIGNED_ATTR));
			int codeSize = Integer.decode(protocolMsgConf
					.getAttribute(PROTOCOL_MESSAGE_CODE_SIZE_ATTR));

			// If the message handler attribute is not blank, that means the
			// user has declared a handler for the message and wants it to be
			// handled in its own provided way. If the handler is not provided,
			// use the null message handler as the message handler.
			IMessageHandler handler = null;
			if (protocolMsgConf.getAttribute(PROTOCOL_MESSAGE_HANDLER_ATTR) != null) {
				Object aObject = protocolMsgConf
						.createExecutableExtension(PROTOCOL_MESSAGE_HANDLER_ATTR);
				if (!(aObject instanceof IMessageHandler)) {
					throw new ProtocolException(
							"Error at message declaration. The message handler must be an instance of IMessageHandler");
				} else {
					handler = (IMessageHandler) aObject;
				}
			} else {
				handler = new NullMessageHandler();
			}

			// Collects all definitions of message fields
			//
			// IMPORTANT NOTE: it is extremely important to store the message
			// fields in a collection that preserves the input order. The whole
			// protocol is based on this order, as the iteration is done on the
			// model to define which bytes read from the stream belongs to each
			// field
			List<IMsgDataBean> msgDataList = new ArrayList<IMsgDataBean>();
			if (readFields) {

				IConfigurationElement[] msgDataConfArray = protocolMsgConf
						.getChildren();
				for (IConfigurationElement msgDataConf : msgDataConfArray) {
					IMsgDataBean msgData = readMsgData(msgDataConf);
					msgDataList.add(msgData);
				}
			}

			// Fills the bean with information collected previously
			bean.setCode(code);
			bean.setId(id);
			bean.setMsgCodeSigned(codeSigned);
			bean.setMsgCodeSizeInBytes(codeSize);
			bean.setHandler(handler);
			bean.setMessageData(msgDataList);

			// Stores the bean at the provided map
			messageDefMap.put(code, bean);

		} catch (CoreException e) {
			// Skip the erroneous message
		}
	}

	/**
	 * Reads a message field.
	 * 
	 * @param msgDataConf
	 *            The configuration element that contains the field to read.
	 * 
	 * @return A message field bean containing the definition of the field.
	 * 
	 * @throws ProtocolException
	 *             If the message field does not follow the specifications or is
	 *             badly formed.
	 * @throws CoreException
	 *             If the field is a raw data field and the defined handler
	 *             cannot be created.
	 */
	private static IMsgDataBean readMsgData(IConfigurationElement msgDataConf)
			throws ProtocolException, CoreException {
		IMsgDataBean bean = null;

		// Firstly, define the field type
		if (msgDataConf.getName().equals(PROTOCOL_MESSAGE_FIXED_DATA_ELEM)) {
			// If it is a fixed data field, create a fixed data bean
			FixedSizeDataBean fixedBean = new FixedSizeDataBean();

			// Read data from the configuration element
			String fieldName = msgDataConf
					.getAttribute(PROTOCOL_MESSAGE_FIXED_FIELD_NAME_ATTR);
			boolean isFieldSigned = Boolean.parseBoolean(msgDataConf
					.getAttribute(PROTOCOL_MESSAGE_FIXED_FIELD_SIGNED_ATTR));
			int fieldSizeInBytes = Integer.decode(msgDataConf
					.getAttribute(PROTOCOL_MESSAGE_FIXED_FIELD_SIZE_ATTR));
			String value = msgDataConf
					.getAttribute(PROTOCOL_MESSAGE_FIXED_FIELD_VALUE_ATTR);

			// Sets the bean with values collected
			fixedBean.setFieldName(fieldName);
			fixedBean.setFieldSigned(isFieldSigned);
			fixedBean.setFieldSizeInBytes(fieldSizeInBytes);
			if (value != null) {
				fixedBean.setValue(Integer.decode(value));
			}

			// Sets the bean to return
			bean = fixedBean;
		} else if (msgDataConf.getName().equals(
				PROTOCOL_MESSAGE_VARIABLE_DATA_ELEM)) {
			// If it is a variable data field, create a variable data bean
			VariableSizeDataBean varBean = new VariableSizeDataBean();

			// Read data from the configuration element
			boolean isSizeFieldSigned = Boolean
					.parseBoolean(msgDataConf
							.getAttribute(PROTOCOL_MESSAGE_VARIABLE_SIZE_FIELD_SIGNED_ATTR));
			int sizeFieldSizeInBytes = Integer
					.decode(msgDataConf
							.getAttribute(PROTOCOL_MESSAGE_VARIABLE_SIZE_FIELD_SIZE_ATTR));
			String valueFieldName = msgDataConf
					.getAttribute(PROTOCOL_MESSAGE_VARIABLE_VALUE_FIELD_NAME_ATTR);
			String value = msgDataConf
					.getAttribute(PROTOCOL_MESSAGE_VARIABLE_VALUE_FIELD_VALUE_ATTR);

			// Sets the bean with values collected
			varBean.setSizeFieldSigned(isSizeFieldSigned);
			varBean.setSizeFieldSizeInBytes(sizeFieldSizeInBytes);
			varBean.setValueFieldName(valueFieldName);
			if (value != null) {
				varBean.setValue(value);
			}

			// Sets the bean to return
			bean = varBean;
		} else if (msgDataConf.getName().equals(
				PROTOCOL_MESSAGE_RAW_DATA_HANDLER_ELEM)) {
			// If it is a raw data handler field, create a raw data bean
			RawDataBean rawBean = new RawDataBean();

			// Read data from the configuration element
			Object aObject = msgDataConf
					.createExecutableExtension(PROTOCOL_MESSAGE_RAW_DATA_EXECUTABLE_ATTR);
			if (!(aObject instanceof IRawDataHandler)) {
				throw new ProtocolException(
						"Error at message declaration. The raw data handler must be an instance of IRawDataHandler");
			} else {
				// Sets the bean with value collected
				rawBean.setHandler((IRawDataHandler) aObject);
			}

			// Sets the bean to return
			bean = rawBean;
		} else if (msgDataConf.getName().equals(
				PROTOCOL_MESSAGE_ITERATABLE_BLOCK_ELEM)) {
			// If it is a iteratable block field, create a iteratable block bean
			IteratableBlockDataBean iteratableBean = new IteratableBlockDataBean();

			// Read data from the configuration element
			String iterateOnField = msgDataConf
					.getAttribute(PROTOCOL_MESSAGE_ITERATABLE_BLOCK_ITERATE_ON_ATTR);
			String iteratableBlockId = msgDataConf
					.getAttribute(PROTOCOL_MESSAGE_ITERATABLE_BLOCK_ID_ATTR);
			Collection<IMsgDataBean> dataBeans = new ArrayList<IMsgDataBean>();

			// Sets the bean with values collected
			iteratableBean.setIterateOnField(iterateOnField);
			iteratableBean.setId(iteratableBlockId);

			// An iteratable block contains a set of internal fields that needs
			// to be handled several times. Adds the internal fields to a
			// collection and sets the collection to the iteratable block bean
			IMsgDataBean internalBean;
			IConfigurationElement[] internalElements = msgDataConf
					.getChildren();
			for (IConfigurationElement internal : internalElements) {
				// Recursive call. This allows the internal fields to be read
				// without writing more code
				internalBean = readMsgData(internal);
				dataBeans.add(internalBean);
			}
			iteratableBean.setDataBeans(dataBeans);

			// Sets the bean to return
			bean = iteratableBean;
		} else {
			// If it is an unknown field (different from fixed, variable, raw
			// data reader/writer, iteratable block)
			throw new ProtocolException("Unkown data element");
		}

		return bean;
	}

	/**
	 * Reads the message ids of all messages which belongs to the provided
	 * sorted list of protocols, and that are defined under the provided
	 * messageOrientationElem element in protocolMessageOrientation extension
	 * definition.
	 * 
	 * @param protocols
	 *            A sorted list of protocol ids that are to have their messages
	 *            searched
	 * @param messageOrientationElem
	 *            The name of the element that represents the client or server
	 *            message orientations. Can be one of the following:
	 *            IExtensionConstants.PROTOCOL_MESSAGE_ORIENTATION_CLIENT_ELEM
	 *            or
	 *            IExtensionConstants.PROTOCOL_MESSAGE_ORIENTATION_SERVER_ELEM
	 * 
	 * @return A collection of message ids (owned by one of the provided
	 *         protocols) that are declared as server/client messages, depending
	 *         on the provided messageOrientationElem parameter
	 * 
	 * @throws ProtocolException
	 *             DOCUMENT ME!!
	 */
	private static Collection<String> getMessagesOrientations(
			List<String> protocols, String messageOrientationElem)
			throws ProtocolException {

		Collection<String> messageOrientations = new HashSet<String>();

		// Get all messages referring to the given protocols
		// Once the messages are retrieved, build a collection with all
		// messages ids associated to the provided protocols
		Map<Long, ProtocolMsgDefinition> allMessagesDefMap = getAllProtocolMessages(
				protocols, false);
		Collection<ProtocolMsgDefinition> allMessagesDef = allMessagesDefMap
				.values();
		Collection<String> allMessagesIdsFromDefs = new HashSet<String>();
		for (ProtocolMsgDefinition aMessageDef : allMessagesDef) {
			allMessagesIdsFromDefs.add(aMessageDef.getId());
		}

		// Get all Protocol Message Orientation extensions
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint msgOrientationExtPoint = registry
				.getExtensionPoint(PROTOCOL_MESSAGE_ORIENTATION_EXTENSION_POINT);
		IExtension[] allMsgOrientationExtensions = msgOrientationExtPoint
				.getExtensions();

		// For each Protocol Message Orientation extension, get the
		// clientMessage
		// or serverMessage elements (depending on the messageOrientationElem
		// parameter)
		for (IExtension anExtension : allMsgOrientationExtensions) {
			IConfigurationElement[] allMsgOrientationConfElem = anExtension
					.getConfigurationElements();

			for (IConfigurationElement aConfElem : allMsgOrientationConfElem) {
				if (aConfElem.getName().equals(messageOrientationElem)) {

					// Once a clientMessage or serverMessage element is found,
					// get its data and validate them against the protocols
					// and allMessagesIdsFromDefs collections. If both tests
					// return true, that means a valid orientation is found.
					// Then add the message id to the collection to be returned.
					String extPointProtocolId = aConfElem
							.getAttribute(PROTOCOL_MESSAGE_ORIENTATION_PROTOCOL_ID_ATTR);
					String extPointMessageId = aConfElem
							.getAttribute(PROTOCOL_MESSAGE_ORIENTATION_MESSAGE_ID_ATTR);

					if ((protocols.contains(extPointProtocolId))
							&& (allMessagesIdsFromDefs
									.contains(extPointMessageId))) {
						messageOrientations.add(extPointMessageId);
					}
				}
			}
		}

		return messageOrientations;
	}
}
